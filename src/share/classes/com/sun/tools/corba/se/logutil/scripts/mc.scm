;  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
; 
;  Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
; 
;  The contents of this file are subject to the terms of either the GNU
;  General Public License Version 2 only ("GPL") or the Common Development
;  and Distribution License("CDDL") (collectively, the "License").  You
;  may not use this file except in compliance with the License. You can obtain
;  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
;  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
;  language governing permissions and limitations under the License.
; 
;  When distributing the software, include this License Header Notice in each
;  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
;  Sun designates this particular file as subject to the "Classpath" exception
;  as provided by Sun in the GPL Version 2 section of the License file that
;  accompanied this code.  If applicable, add the following below the License
;  Header, with the fields enclosed by brackets [] replaced by your own
;  identifying information: "Portions Copyrighted [year]
;  [name of copyright owner]"
; 
;  Contributor(s):
; 
;  If you wish your version of this file to be governed by only the CDDL or
;  only the GPL Version 2, indicate your decision by adding "[Contributor]
;  elects to include this software in this distribution under the [CDDL or GPL
;  Version 2] license."  If you don't indicate a single choice of license, a
;  recipient has the option to distribute your version of this file under
;  either the CDDL, the GPL Version 2 or to extend the choice of license to
;  its licensees as provided above.  However, if you add GPL Version 2 code
;  and therefore, elected the GPL Version 2 license, then the option applies
;  only if the new code is made subject to such option by the copyright
;  holder.

; Scheme program to produce CORBA standard exceptions class and log wrapper table
; requires Jscheme Java extensions
; Makes use of some custom Java classes also
; now requires at least JScheme 7.2

(import "com.sun.tools.corba.se.logutil.IndentingPrintWriter" ) 
(import "com.sun.tools.corba.se.logutil.StringUtil" ) 
(import "java.io.FileOutputStream")

(use-module "elf/iterate.scm")
(use-module "elf/trace.scm")

(define version-string "1.27")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utility functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define null '())

; reload this file (convenience definition)
(define (reload) (load "mc.scm"))

; read the contents of a file containing an S-expression
(define (read-file fname)
    (read (open-input-file fname)))

; Simple little function to report an error
(define (error msg)
    (throw (Error. msg)))

; some debug support
(define debug #f)

(define (dprint msg)
    (if debug
	(.println System.out$ msg)))

; Replace dprint with noprint to avoid seeing messages when debug is #t
(define (noprint msg) ())

; Helper function present so that a scheme method taking strings as args 
; can be easily run from a command line.
; arg:	    vector containing argument strings. Element 0 is the function name
;	    to execute
(define (main arg)
    (let*
	(
	    (arg-list (vector->list arg))
	    (function-symbol (string->symbol (car arg-list)))
	    (args (cdr arg-list)))
	(apply (eval function-symbol) args)))

; Returns the position of key in lst, numbering from 0.  key is matched using eqv?
(define (get-list-position key lst)
    (letrec
	(
	    (helper (lambda (k l accum)
		(cond 
		    ((null? l) (error (string-append "Could not find " k)))
		    ((eqv? k (car l)) accum)
		    (else (helper k (cdr l) (+ accum 1))) ))))
	(begin 
	    (noprint (string-append "get-list-position called with key " key " lst " lst ))
	    (helper key lst 0))))

; Return a string representing number in decimal padded to length with leading 0s.
(define (pad-number-string number length)
    (let*
	(
	    (number-string (number->string number))
	    (pad-length (- length (string-length number-string)))
	)
	(string-append (make-string pad-length #\0) number-string)))

; There are two different S-expression formats used in this script.
;
; The S-expression used for minor codes must have the structure:
;
;   (package-name class-name exception-group-name
;	(exception
;	    (name value level explanation)
;	    ...
;	)
;	...
;   )
;
; The S-expression used for log domains must have the structure:
;
;   (top-level-domain
;	(domain
;	    (domain )
;	    ... )
;   )
;
; That is, we jave a top level domain, then a series of nested domains.
; This data is used to generate the CORBALogDomains file and the LogWrapperTable
; interface and implementations.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions for handling major system exceptions and exception groups
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Function to find the base ID given an exception group name.  Result is a function that
; maps the minor code into the Java expression for that minor code's actual value.
(define (get-base group-name)
	(if (eqv? group-name 'OMG)
	    (lambda (minor-code)
		(string-append "OMGVMCID.value + " (number->string minor-code)))
	    (let  ; bind base-number outside the lambda so it is only evaluated once
		(
		    (base-number (get-sun-base-number group-name)))
		(lambda (minor-code)
		    (string-append "SUNVMCID.value + " (number->string (+ base-number minor-code)))))))

(define sun-mc-exception-groups (list 'ORBUtil 'Activation 'Naming 'Interceptors 'POA 'IOR 'Util))

(define sun-exception-groups (map 
    (lambda (symbol) 
	(string->symbol (.toUpperCase (symbol->string symbol))))
     sun-mc-exception-groups))

(define all-exception-groups `(,@sun-mc-exception-groups OMG))

; Function to get a base value for the group-name
(define (get-sun-base-number group-name)
    (let*
	(
	    (lst `(SUNBASE ,@sun-exception-groups))
	    (subsystem-size 200))
	(* subsystem-size (get-list-position group-name lst))))

; Function to get a 3 digit number for a system exception
(define (get-exception-id exception-name)
    (let
	(
	    (lst (list 'UNKNOWN 'BAD_PARAM 'NO_MEMORY 'IMP_LIMIT 'COMM_FAILURE 'INV_OBJREF 'NO_PERMISSION 
		'INTERNAL 'MARSHAL 'INITIALIZE 'NO_IMPLEMENT 'BAD_TYPECODE 'BAD_OPERATION 'NO_RESOURCES 
		'NO_RESPONSE 'PERSIST_STORE 'BAD_INV_ORDER 'TRANSIENT 'FREE_MEM 'INV_IDENT 'INV_FLAG 
		'INTF_REPOS 'BAD_CONTEXT 'OBJ_ADAPTER 'DATA_CONVERSION 'OBJECT_NOT_EXIST 'TRANSACTION_REQUIRED 
		'TRANSACTION_ROLLEDBACK 'INVALID_TRANSACTION 'INV_POLICY 'CODESET_INCOMPATIBLE 'REBIND 
		'TIMEOUT 'TRANSACTION_UNAVAILABLE 'BAD_QOS 'INVALID_ACTIVITY 'ACTIVITY_COMPLETED 
		'ACTIVITY_REQUIRED )))
	(pad-number-string (get-list-position exception-name lst) 3)))

; Return the message id string for any system exception
;
(define (get-message-id exception-type group-name minor)
    (if (eqv? group-name 'OMG)
	(get-standard-message-id exception-type minor)
	(get-sun-message-id exception-type group-name minor)))

; Return the message id string for a particular standard exception
;
(define (get-standard-message-id exception-type minor)
    (string-append 
	"IOP" 
	(get-exception-id exception-type) 
	"0" 
	(pad-number-string (number->string minor) 4)))

; Return the sun message id for this exception-type, group-name, and minor code.
(define (get-sun-message-id exception-type group-name minor)
    (string-append 
	"IOP" 
	(get-exception-id exception-type) 
	"1"
	(pad-number-string (+ (get-sun-base-number group-name) minor) 4)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; visitor framework for the exception file format
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (visit-top obj func1)
    (let*
	(
	    (package (car obj))
	    (class (cadr obj))
	    (group (caddr obj))
	    (func2 (func1 package class group))
	    (exceptions (cadddr obj)))
	(visit-exceptions exceptions func2)))

; visit the elements of an arbitrary list
; lst:		the list to visit
; func:		the function to apply to each element of lst
; next-level	the function on lst element and func that visits the next level
(define (visit-list lst func next-level)
    (if (null? (cdr lst))
	(next-level #t (car lst) func)
	(begin
	    (next-level #f (car lst) func)
	    (visit-list (cdr lst) func next-level))))

(define (visit-exceptions exceptions func2)
    (visit-list exceptions func2 (lambda (last-flag element func) (visit-exception last-flag element func))))

(define (visit-exception last-flag exception func2)
    (let*
	(
	    (major (car exception))
	    (minor-codes (cdr exception))
	    (func3 (func2 last-flag major)))
	(visit-minor-codes minor-codes func3)))

(define (visit-minor-codes minor-codes func3)
    (visit-list minor-codes func3 (lambda (last-flag element func) (visit-minor-code last-flag element func))))

(define (visit-minor-code last-flag minor-code func3)
    (let*   
	(
	    (name (car minor-code))
	    (minor (cadr minor-code))
	    (level (caddr minor-code))
	    (msg (cadddr minor-code)))
	(func3 last-flag name minor level msg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Visitor framework for log domain format
;;
;; Example of input:
;; (top 
;;	(d1 
;;	 d13
;;	    (d2 
;;		(d3 
;;		 d4
;;		    (d11
;;		     d12)
;;		 d5) 
;;	    d6 
;;		(d9
;;		 d10)
;;	     d7) 
;;	 d8)
;;	 )
;;
;; func2 = (func top)
;; Call func2 successively on:
;; (() d1)
;; (() d13)
;; ((d13) d2)
;; ((d2 d13) d3)
;; ((d2 d13) d4)
;; ((d4 d2 d13) d11)
;; ((d4 d2 d13) d12)
;; ((d2 d13) d5)
;; ((d13) d6)
;; ((d6 d13) d9)
;; ((d6 d13) d10)
;; ((d13) d7)
;; (()) d8)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; sample data for testing
(define domain-test-data
    '(top (d1 d13 (d2 (d3 d4 (d11 d12) d5) d6 (d9 d10) d7) d8))
)

;; accum is a list of the context of the current element
;; data is the data being scanned (a nested list)
;; prev is the previous first element (needed for constructing accum)
;; func takes two arguments, the first must be a list
;;
;; outer call looks like (visit-domain-list-helper '() data '() func)
(define (visit-domain-list-helper accum data prev func)
    (let* (
	    (first (car data))
	    (rest (cdr data)))
	(if (list? first)
	    (let* (
		    (head (car first))
		    (tail (cdr first))
		    (this-accum 
			(if (null? prev)
			    accum
			    (cons prev accum))))
	        (func this-accum head)
		(if (not (null? tail))
		    (visit-domain-list-helper this-accum tail head func)))
	    (func accum first))
	(if (not (null? rest))
	    (visit-domain-list-helper accum rest first func))))

(define (visit-domain-list lst func)
    (let* (
	    (root-domain	(car lst))
	    (domain-list	(cadr lst))
	    (func2		(func root-domain)))
	(visit-domain-list-helper null domain-list null func2)))

(define (echo-domain-list-visitor root-domain)
    (let* (
	    (pw (IndentingPrintWriter. System.out$)))
	(.indent pw)
	(.printMsg pw "OUT>>>root-domain=@" (list root-domain))
	(.flush pw)
	(lambda (accum element) 
	    (begin
		(.printMsg pw "OUT>>>(@) @" (list accum element))
		(.flush pw)))))

(define (trace-domain)
    (begin
        (trace 'visit-domain-list)
	(trace 'visit-domain-list-helper)
	(trace 'echo-domain-list-visitor)
    ))

(define (fix-up-data accum element)
    `(,@(reverse accum) ,element))

(define (fold-method-constant-name x so-far)
    (let* (
	(start 
	    (if (eqv? 0 (.length so-far))
		so-far
		(.concat so-far "_"))))
	(if (null? x)
	    so-far
	    (.concat start (.toUpperCase (symbol->string x))))))
;
; convert ((a b c) d) into "C_B_A_D"
(define (make-log-domain-constant-name accum element)
    (foldL (fix-up-data accum element) fold-method-constant-name "" ))

(define (fold-method-value x so-far)
    (let* (
	(start 
	    (if (eqv? 0 (.length so-far))
		so-far
		(.concat so-far "."))))
	(if (null? x)
	    so-far
	    (.concat start (.toLowerCase (symbol->string x))))))

; convert ((a b c) d) into "c.b.a.d"
(define (make-log-domain-value accum element)
    (foldL (fix-up-data accum element) fold-method-value "" ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Definitions for CORBALogDomains file
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (standard-java-preamble infile)
#{/*
 * CORBALogDomains.java #[ version-string ]# #[ (java.util.Date. ) ]#
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * This file was generated from #[ infile ]#.  
 * Please do not edit this file by hand!
 */

package com.sun.corba.se.impl.logging ;
}# )

(define (CORBALogDomains-preamble infile)
#{
#[ (standard-java-preamble infile) ]#

/** Defines constants for all of the logging domains used in the ORB.
 * Note that this is the suffix to the log domain.  The full domain is given by
 * <code>javax.enterprise.resource.corba.{ORBId}.{Log domain}</code>
 * where {ORBId} is the ORB ID of the ORB instance doing the logging.
 */

public class CORBALogDomains {
    private CORBALogDomains() {}
}# )

(define (make-tld-string root-domain)
#{
    // Top level log domain for CORBA
    public static final String TOP_LEVEL_DOMAIN = "#[root-domain]#" ;
}# )

(define (make-java-constant-string constant-name value)
#{
    public static final String #[constant-name]# = "#[value]#" ;
}# )

(define (make-CORBALogDomains-domain-list-visitor pw) 
    (lambda (root-domain)
	(begin
	    (.print pw (make-tld-string root-domain))
	    (lambda (accum element)
		(let* (
		    (constant-name (make-log-domain-constant-name accum element))
		    (value	   (make-log-domain-value accum element)))
		(.print pw (make-java-constant-string constant-name value)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Definitions for generating LogWrapperTable interface and implementations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (CORBALogDomains-interface-preamble infile)
#{
#[ (standard-java-preamble infile) ]#

public interface LogWrapperTable {
}# )

(define (CORBALogDomains-impl-preamble infile class-name)
#{
#[ (standard-java-preamble infile) ]#

import com.sun.corba.se.spi.orb.ORB ;

public class #[ class-name ]# implements LogWrapperTable {
    private ORB orb ;

    public #[ class-name ]#( ORB orb ) {
	this.orb = orb ;
    }
}# )

(define (CORBALogDomains-static-impl-preamble infile class-name)
#{
#[ (standard-java-preamble infile) ]#

public class #[ class-name ]# implements LogWrapperTable {
}# )

(define (make-exception-group-type exception-group)
#{#[ exception-group ]#SystemException}# )

(define (make-method-header log-domain exception-group)
#{ #[ (make-exception-group-type exception-group) ]# get_#[ log-domain ]#_#[ exception-group ]#() }# )

(define (make-field-name log-domain exception-group) 
#{#[ log-domain ]#_#[ exception-group ]#_value}# )

(define (CORBALogDomains-interface-method log-domain exception-group) 
#{
    #[ (make-method-header log-domain exception-group) ]# ;
}# )

(define (make-CORBALogDomains-interface-domain-list-visitor pw) 
    (lambda (root-domain)
	(begin
	    (lambda (accum element)
		(let* (
		    (constant-name (make-log-domain-constant-name accum element)))
		(iterate all-exception-groups
		    (lambda (exception-group)
			(.print pw (CORBALogDomains-interface-method constant-name exception-group)))))))))

(define (make-CORBALogDomains-impl-method log-domain exception-group is-static?)
    (let* (
	    (log-domain-type (make-exception-group-type exception-group))
	    (field-name (make-field-name log-domain exception-group))
	    (first-arg (if is-static?  "" "orb,")))
#{
    private volatile #[ log-domain-type ]# #[ field-name ]# = null ;

    public #[ (make-method-header log-domain exception-group) ]# {
	if (#[ field-name ]# == null) {
	    synchronized(this) {
		if (#[ field-name ]# == null) {
		    #[ field-name ]# = #[ log-domain-type ]#.get( #[ first-arg ]#
			CORBALogDomains.#[ log-domain ]# ) ;
		}
	    }
	}

	return #[ field-name ]# ;
    }
    }# ))

(define (make-CORBALogDomains-impl-domain-list-visitor pw is-static?)
    (lambda (root-domain)
	(lambda (accum element)
	    (let* (
		    (constant-name (make-log-domain-constant-name accum element)))
		(iterate all-exception-groups
		    (lambda (exception-group)
		      (.print pw (make-CORBALogDomains-impl-method constant-name exception-group is-static?))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; general-purpose function for generating classes derived from log domain data
(define (generate-CORBALogDomains-class infile outdir outfile-name obj preamble-func visitor-func-generator) 
    (let* (
	(outfile (FileOutputStream. (string-append outdir java.io.File.separator$ outfile-name)))  
	(pw   (IndentingPrintWriter. outfile)))
    (preamble-func pw infile)
    (visit-domain-list obj (visitor-func-generator pw))
    (.println pw "}")
    (.flush pw)
    (.close pw)))

; Functions for generating the 4 log domain related files
(define (write-CORBALogDomains infile outdir obj)
    (generate-CORBALogDomains-class infile outdir "CORBALogDomains.java" obj
	(lambda (pw infile) 
	    (.print pw (CORBALogDomains-preamble infile)))
	(lambda (pw) 
	    (make-CORBALogDomains-domain-list-visitor pw))))

(define (write-CORBALogDomains-interface infile outdir obj)
    (generate-CORBALogDomains-class infile outdir "LogWrapperTable.java" obj
	(lambda (pw infile)
	    (.print pw (CORBALogDomains-interface-preamble infile)))
	(lambda (pw)
	    (make-CORBALogDomains-interface-domain-list-visitor pw))))

(define (write-CORBALogDomains-impl infile outdir obj)
    (let* (
	    (class-name "LogWrapperTableImpl")
	    (class-file-name (.concat class-name ".java")))
	(generate-CORBALogDomains-class infile outdir class-file-name obj
	    (lambda (pw infile)
		(.print pw (CORBALogDomains-impl-preamble infile class-name)))
	    (lambda (pw)
		(make-CORBALogDomains-impl-domain-list-visitor pw #f)))))

(define (write-CORBALogDomains-static-impl infile outdir obj)
    (let* (
	    (class-name "LogWrapperTableStaticImpl")
	    (class-file-name (.concat class-name ".java")))
	(generate-CORBALogDomains-class infile outdir class-file-name obj
	    (lambda (pw infile)
		(.print pw (CORBALogDomains-static-impl-preamble infile class-name)))
	    (lambda (pw)
		(make-CORBALogDomains-impl-domain-list-visitor pw #t)))))

; ENTRY POINT: main function for generating the log domains files.
(define (make-CORBALogDomains-classes infile outdir)
    (tryCatch 
	(let* (
		(obj (read-file infile)))
	    (write-CORBALogDomains-interface infile outdir obj)
	    (write-CORBALogDomains-impl infile outdir obj)
	    (write-CORBALogDomains-static-impl infile outdir obj)
	    (write-CORBALogDomains infile outdir obj))
	(lambda (exc) 
	    (begin
		(.println System.out$ (string-append "make-CORBALogDomains failed with exception " (.toString exc)))
		))))
		; (System.exit 1)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The visitors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; A simple visitor that just echoes the input for test purposes
(define (simple-visitor package class group)
    (let* 
	(
	    (pw (IndentingPrintWriter. System.out$)))
	(begin
	    (.indent pw)
	    (.printMsg pw "package=@ class=@ group=@" (list package class group))
	    (.flush pw)
	    (lambda (last-flag major)
		(begin
		    (.indent pw)
		    (.printMsg pw "major=@" (list major))
		    (.flush pw)
		    (lambda (last-flag name minor level message)
			(begin
			    (if last-flag (.undent pw))
			    (.printMsg pw "name=@ minor=@ level=@ message=@" (list name minor level message))
			    (.flush pw))))))))

; Function that returns a visitor that writes out the resource file in the form:
;   id="MSGID: explanation"
; outdir: Output directory 
(define (resource-visitor outdir)
    (lambda (package class group)
	(let* 
	    (
		(file-name (string-append outdir java.io.File.separator$ class ".resource"))
		(pw (IndentingPrintWriter. (FileOutputStream. file-name))))
	    (begin 
		(dprint (string-append "package= " package " class=" class " group=" group " file-name=" file-name))
		(lambda (last-flag1 major)
		    (begin
			; (dprint (string-append "last-flag1=" last-flag1 " major=" major))
			(lambda (last-flag2 name minor level message)
			    (begin
				; (dprint (string-append "last-flag2=" last-flag2 " name=" name 
				    ; " minor=" minor " level=" level " message=" message))
				(let*
				    (
					(msgid (get-message-id major group minor))
					(ident (StringUtil.toMixedCase (symbol->string name))))
				    (begin
					; (dprint (string-append "msgid=" msgid " ident=" ident))
					(.printMsg pw "@.@=\"@: (@) @\"" (list group ident msgid major message))
					(.flush pw)
					(if (and last-flag1 last-flag2) 
					    (begin
						; (dprint "closing file")
						(.close pw)))))))))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Top-level functions for creating the products.  All have names of the form make-xxx
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Read the minor codes from the infile and write out a resource file. 
(define (make-resource infile outdir)
    (tryCatch 
	(visit-top (read-file infile) (resource-visitor outdir))
	(lambda (exc) 
	    (begin
		(.println System.out$ (string-append "make-resource failed with exception " (.toString exc)))
		(System.exit 1)))))

; Read the minor codes from the infile and write a Java implementation to
; handle them to outfile under outdir
(define (make-class infile outdir)
    (tryCatch 
	(write-class infile outdir (read-file infile))
	(lambda (exc) 
	    (begin
		(.println System.out$ (string-append "make-class failed with exception " (.toString exc)))
		(System.exit 1)))))
	
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The original make-class implementation (this should be replaced by two visitors)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Write out the Java source code for the StandardExceptions class
; outdir:  Output directory to write the generated files
; obj:	    the data from the input file
(define (write-class infile outdir obj)
    (let* 
	( 
	    (package-name (car obj))
	    (class-name (cadr obj))
	    (exception-group-name (caddr obj))
	    (exceptions (cadddr obj))
	    (file (FileOutputStream. (string-append outdir java.io.File.separator$  class-name ".java")))  
	    (pw   (IndentingPrintWriter. file))
	)
	(begin
	    (write-class-header infile package-name class-name exception-group-name pw)
	    (.printMsg pw "package @ ;"
		(list package-name))
	    (.println pw)
	    (.println pw "import java.util.logging.Level ;")
	    (.println pw)
	    (.println pw "import org.omg.CORBA.OMGVMCID ;")
	    (.println pw "import com.sun.corba.se.org.omg.CORBA.SUNVMCID ;")
	    (.println pw "import org.omg.CORBA.CompletionStatus ;")
	    (.println pw "import org.omg.CORBA.SystemException ;")
	    (.println pw)
	    (.println pw "import com.sun.corba.se.spi.orb.ORB ;")
	    (.println pw)
	    (.println pw "import com.sun.corba.se.spi.logging.LogWrapperFactory;")
	    (.println pw "import com.sun.corba.se.spi.logging.LogWrapperBase;")
	    (.println pw)
	    (write-imports exceptions pw)
	    (.println pw)
	    (.indent pw)
	    (.printMsg pw "public class @ extends LogWrapperBase {"
		(list class-name))
	    (.println pw)
	    (.printMsg pw "public @( String logger )"
		(list class-name))
	    (.indent pw)
	    (.println pw "{")
	    (.undent pw)
	    (.println pw "super( logger ) ;")
	    (.println pw "}")
	    (.println pw)
	    (.flush pw)
	    (write-factory-method class-name exception-group-name pw)
	    (write-exceptions exception-group-name exceptions (get-base exception-group-name) class-name pw)
	    (.undent pw)
	    (.println pw )
	    (.println pw "}")
	    (.flush pw)
	    (.close pw)
	)))

; Write out the header for the resource file
(define (write-class-header infile package class group pw)
    (begin
	(if (eqv? group 'OMG)
	    (.println pw "// Log wrapper class for standard exceptions")
	    (.printMsg pw "// Log wrapper class for Sun private system exceptions in group @" (list group)))
	(.println pw "//")
	(.printMsg pw "// Generated by mc.scm version @, DO NOT EDIT BY HAND!" (list version-string))
	(.printMsg pw "// Generated from input file @ on @" (list infile (java.util.Date.)))
	(.println pw)))

(define (write-factory-method class-name exception-group-name pw)
    (begin
	(.indent pw)
	(.println pw "public static final LogWrapperFactory FACTORY = new LogWrapperFactory() {")
	(.println pw "public LogWrapperBase create( String logger )" )
	(.indent pw)
	(.println pw "{")
	(.undent pw)
	(.printMsg pw "return new @( logger ) ;" (list class-name))
	(.undent pw)
	(.println pw "}" )
	(.println pw "} ;" )
	(.println pw)
	(.printMsg pw "public static final String EXCEPTION_GROUP_NAME = \"@\" ;" 
	    (list exception-group-name))
	(.println pw)
	(.printMsg pw "public static @ get( ORB orb, String logDomain )" (list class-name))
	(.indent pw)	
	(.println pw "{")
	(.indent pw)	
	(.printMsg pw "@ wrapper = "
	    (list class-name))
	(.indent pw)
	(.printMsg pw "(@) orb.getLogWrapper( logDomain, " 
	    (list class-name))
	(.undent pw)	
	(.undent pw)	
	(.println pw "EXCEPTION_GROUP_NAME, FACTORY ) ;" )
	(.undent pw)	
	(.println pw "return wrapper ;" )
	(.println pw "} " )
	(.println pw)
	(.printMsg pw "public static @ get( String logDomain )" (list class-name))
	(.indent pw)	
	(.println pw "{")
	(.indent pw)	
	(.printMsg pw "@ wrapper = "
	    (list class-name))
	(.indent pw)
	(.printMsg pw "(@) ORB.staticGetLogWrapper( logDomain, " 
	    (list class-name))
	(.undent pw)	
	(.undent pw)	
	(.println pw "EXCEPTION_GROUP_NAME, FACTORY ) ;" )
	(.undent pw)	
	(.println pw "return wrapper ;" )
	(.println pw "} " )
	(.println pw)))

; Write out the import list for the exceptions listed in obj
; obj:	    the data from the input file
; pw:	    an IndentingPrintWriter for the output file
(define (write-imports obj pw)
    (if (null? obj)
	()
	(let 
	    (
		(exception (caar obj))
	    )
	    (begin
		(.print pw "import org.omg.CORBA.")
		(.print pw exception)
		(.println pw " ;")
		(write-imports (cdr obj) pw)
	    ))))

; Write out the list of exceptions starting with the first one
; obj:	    the data from the input file
; base:	    the lambda that returns the string defining the minor code value
; pw:	    an IndentingPrintWriter for the output file
(define (write-exceptions group-name obj base class-name pw)
    (if (null? obj) 
	()
	(let* 
	    (
		(record (car obj))
		(exception (car record))
	        (minor-codes (cdr record))
	    )
	    (begin
		(write-exception group-name exception minor-codes base class-name pw)
		(write-exceptions group-name (cdr obj) base class-name pw)
	    ))))

; Write out a single exception
; exception:	the CORBA SystemException type
; base:		the base for the minor code value
; minor-codes:	a list of minor code data for each minor exception type
; pw:		an IndentingPrintWriter for the output file
(define (write-exception group-name exception minor-codes base class-name pw)
    (begin
	(.println pw "///////////////////////////////////////////////////////////")
	(.printMsg pw "// @" (list exception))
	(.println pw "///////////////////////////////////////////////////////////")
	(.println pw)
	(write-methods group-name exception minor-codes base class-name pw)
	(.flush pw)))

; Write all of the methods for a single exception
; exception:	the CORBA SystemException type
; base:		the base for the minor code value
; minor-codes:	a list of minor code data for each minor exception type
; pw:		an IndentingPrintWriter for the output file
(define (write-methods group-name exception minor-codes base class-name pw)
    (if (null? minor-codes)
	()
	(begin
	    (write-method group-name exception (car minor-codes) base class-name pw)
	    (write-methods group-name exception (cdr minor-codes) base class-name pw)
	)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Code that writes out the Java methods for exception handling
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Write the methods for a single minor code within an exception
; exception:	the CORBA SystemException type
; minor-code:	minor code data for one minor exception type 
;		(name value level explanation)
; base:		the base for the minor code value
; pw:		an IndentingPrintWriter for the output file
(define (write-method group-name exception minor-code base class-name pw)
    (let* 
	(
	    (x (symbol->string (car minor-code)))
	    (ident (cons x (StringUtil.toMixedCase x)))
	    (value (cadr minor-code))
	    (level (symbol->string (caddr minor-code)))
	    (explanation (cadddr minor-code))
	    (num-params (StringUtil.countArgs explanation)))
	(begin
	    (.printMsg pw "public static final int @ = @ ;"
		(list x (base value)))
	    (.println pw )
	    (.flush pw )
	    (write-method-status-cause group-name exception ident level num-params class-name pw)
	    (.println pw)
	    (.flush pw)
	    (write-method-status exception ident level num-params pw)
	    (.println pw)
	    (.flush pw)
	    (write-method-cause exception ident level num-params pw)
	    (.println pw)
	    (.flush pw)
	    (write-method-no-args exception ident level num-params pw)
	    (.println pw)
	    (.flush pw))))

; Construct a string of the form arg1, ..., argn where n is num-params
(define (make-arg-string fixed leading-comma-flag num-args)
    (let
	(
	    (helper (lambda (lcf n)
		(let*
		    (
			(numstr (number->string (- n 1))))
		    (if (or lcf (> n 1))
			(string-append ", " fixed numstr)
			(string-append " " fixed numstr))))))
	(cond 
	    ((eqv? num-args 0) " ")
	    ((eqv? num-args 1) (helper leading-comma-flag 1))
	    (else (string-append 
		(make-arg-string fixed leading-comma-flag (- num-args 1)) 
		(helper leading-comma-flag num-args ))))))

(define (make-decl-args leading-comma-flag num-args)
    (make-arg-string "Object arg" leading-comma-flag num-args))

(define (make-call-args leading-comma-flag num-args)
    (make-arg-string "arg" leading-comma-flag num-args))

; make-xxx-args patterns:
; leading-comma-flag #t
;
;   0   " "
;   1   ", arg0"
;   2   ", arg0, arg1"
;   3   ", arg0, arg1, arg2"
;
;   0   " "
;   1   ", Object arg0"
;   2   ", Object arg0, Object arg1"
;   3   ", Object arg0, Object arg1, Object arg2"
;
; leading-comma-flag #f
;
;   0   " "
;   1   " arg0"
;   2   " arg0, arg1"
;   3   " arg0, arg1, arg2"
;
;   0   " "
;   1   " Object arg0"
;   2   " Object arg0, Object arg1"
;   3   " Object arg0, Object arg1, Object arg2"

(define (emit-assignments num pw)
    (let 
	(
	    (helper 
		(lambda (n) 
		    (.printMsg pw "parameters[@] = arg@ ;" (list n n)))))
	(if (= num 1)
	    (helper (- num 1))
	    (begin
		(emit-assignments (- num 1) pw)
		(helper (- num 1))))))

; Write a method for an exception that takes a CompletionStatus and a cause
; exception:	the CORBA system exception type
; id:		the identifier for this exception in the form ( ident . mixed-case-ident )
; level:	the logging level
; num-params:	number of parameters in the explanation string, which determines
;		how many argn parameters we need
; pw:		the indenting print writer we are using
(define (write-method-status-cause group-name exception id level num-params class-name pw)
    (let*
	(
	    (ident (car id))
	    (ident-mc (cdr id)))
    (begin
	(.indent pw)
	(.printMsg pw "public @ @( CompletionStatus cs, Throwable t@) {"
	    (list exception ident-mc (make-decl-args #t num-params)))
	(.printMsg pw "@ exc = new @( @, cs ) ;"
	    (list exception exception ident ))

	(.indent pw)
	(.println pw "if (t != null)" )
	(.undent pw)
	(.println pw "exc.initCause( t ) ;" )	
	(.println pw)

	(.indent pw)
	(.printMsg pw "if (getLogger().isLoggable( Level.@ )) {"
	    (list level))
	
	(if (> num-params 0)
	    (begin
		(.printMsg pw "Object[] parameters = new Object[@] ;"
		    (list (number->string num-params)))
		(emit-assignments num-params pw)
	    )
	    (begin
		(.println pw "Object[] parameters = null ;"
	    )))

	(.indent pw)
	(.printMsg pw "doLog( Level.@, \"@.@\"," (list level group-name ident-mc))
	(.undent pw)
	(.undent pw)
	(.printMsg pw "parameters, @.class, exc ) ;" (list class-name))
	(.println pw "}")
	(.println pw)

	(.undent pw)
	(.println pw "return exc ;")

	(.println pw "}"))))

; Write a method for an exception that takes a CompletionStatus.  The cause is null. 
;
; exception:	the CORBA system exception type
; id:		the identifier for this exception in the form ( ident . mixed-case-ident )
; level:	the logging level
; num-params:	number of parameters in the explanation string, which determines
;		how many argn parameters we need
; pw:		the indenting print writer we are using
(define (write-method-status exception id level num-params pw)
    (let*
	(
	    (ident-mc (cdr id)))
	(begin
	    (.indent pw)
	    (.printMsg pw "public @ @( CompletionStatus cs@) {"
		(list exception ident-mc (make-decl-args #t num-params)))
	    (.undent pw)
	    (.printMsg pw "return @( cs, null@ ) ;"
		(list ident-mc (make-call-args #t num-params)))
	    (.println pw "}"))))

; Write a method for an exception that takes a cause.  The status is COMPLETED_NO. 
;
; exception:	the CORBA system exception type
; id:		the identifier for this exception in the form ( ident . mixed-case-ident )
; level:	the logging level
; num-params:	number of parameters in the explanation string, which determines
;		how many argn parameters we need
; pw:		the indenting print writer we are using
(define (write-method-cause exception id level num-params pw)
    (let*
	(
	    (ident-mc (cdr id)))
	(begin
	    (.indent pw)
	    (.printMsg pw "public @ @( Throwable t@) {"
		(list exception ident-mc (make-decl-args #t num-params)))
	    (.undent pw)
	    (.printMsg pw "return @( CompletionStatus.COMPLETED_NO, t@ ) ;"
		(list ident-mc (make-call-args #t num-params)))
	    (.println pw "}"))))

; Write a method for an exception that takes no arguments.  This is COMPLETED_NO and
; a null cause.
;
; exception:	the CORBA system exception type
; id:		the identifier for this exception in the form ( ident . mixed-case-ident )
; level:	the logging level
; num-params:	number of parameters in the explanation string, which determines
;		how many argn parameters we need
; pw:		the indenting print writer we are using
(define (write-method-no-args exception id level num-params pw)
    (let*
	(
	    (ident-mc (cdr id)))
	(begin
	    (.indent pw)
	    (.printMsg pw "public @ @( @) {"
		(list exception ident-mc (make-decl-args #f num-params)))
	    (.undent pw)
	    (.printMsg pw "return @( CompletionStatus.COMPLETED_NO, null@ ) ;"
		(list ident-mc (make-call-args #t num-params)))
	    (.println pw "}"))))

;;; end of file
