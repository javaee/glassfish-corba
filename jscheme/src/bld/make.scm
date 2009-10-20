{
<title>Makefile for JScheme</title>
<h1>Makefile for JScheme</h1>
<pre>
}

;; KMC: derived from the original by removing the web components
;; which resulted in dependencies on non-JDK classes

(import "java.text.DateFormat")

(load "elf/basic.scm")
(load "using/run.scm")
(load "using/command.scm")
(load "elf/classpath.scm")

(define (commandDefault)
  ;; Override the no argument behavior.
  (-all))

(define appName "JScheme")

;;; Assumes a subdirectory is the only thing on classpath.
(define appDir (.getCanonicalFile
		(.getParentFile
                  (.getCanonicalFile
		   (File. ($ "java.class.path"))))))
(define srcDir (File. appDir "src"))
(define (src name) (File. srcDir name))
(define classDir srcDir)
(define libDir (File. appDir "lib"))
(define extDir (File. appDir "ext"))

(define (findJavacBin)
  ;; Find the bin/ containing javac.
  ;; KRA 19MAR02: New rules from Derek Upham. 
  ;; 1. ${java.home}/bin
  ;; 2. ${java.home}/../bin
  ;; 3. every directory in "java.library.path"
  ;; 4. the "bin" sibling directory of every directory in "java.library.path".
  (define (sibling dname sname) (File. (.getParent (File. dname)) sname))
  (let* ((pathdirs (unpath ($ "java.library.path")))
	 (ds (append (list
                        (sibling ($ "java.home") "bin")
                        (File. ($ "java.home") "bin"))
		     (map File. pathdirs)
		     (map (lambda (dname) (sibling dname "bin"))
			  pathdirs)))
	 (result
	  (apply append
		 (map (lambda (d)
			(files* d (lambda (f)
				    (let ((name (.getName f)))
				      (or (.equals name "javac")
					  (.startsWith name "javac."))))))
		      ds))))
    (if (null? result)
	(error "\nCan't find javac in path variable! \nPlease
fix, and 
try again.\nExiting !!!\n")
	(car result))))

(define bin (findJavacBin))
(define tools (File. (.getParentFile
		      (.getParentFile
		       bin))
		     "lib/tools.jar"))

(define directories
  ;; Compile java in these directories recursively.
  (map src
       '("build"			; references sun.tools.javac.Main,
	 "dclass"
	 "elf"
	 "interact"
	 "jscheme"
	 "jsint"
	 ;; "slib"				; no Java.
	 "using"
	 )))

(define compileClasspath
  ;; Grow the compiling classpath to include tools like javac and jar.
  ;; and any lib/*.jar or lib/*.zip files.
  (path classDir (files* (File. "lib") isJarFile) tools))

(define (javacMessage dir files)
  (let ((L (length files)))
    (display
     {Javacing [L] file[(if (= L 1) "" "s")][(if dir { in [dir]} "")]\n})))

(define javaUpdate? (needsUpdate? (java->class srcDir classDir)))

(define compileUpdate? (needsUpdate? (s->o srcDir classDir ".scm" ".class")))

(define (javac . files)
  (let ((files (filter javaUpdate? files)))
    (if (> (length files) 0)
	(begin 
	  (javacMessage #f files)
	  (out (run (cmd javac -g -classpath ,compileClasspath
		    -d ,classDir ,@files))))
	#t)))

(define (javacAll dir)
  ;; Compile all files below directory.
  (apply javac (files** dir isJavaFile)))

(define (compile class package)
  ;; .scm -> .java -> .class
  (let ((file (src (string-append class ".scm"))))
    (if (compileUpdate? file)
	(begin
	  (display {Compiling [class] to package [package]\n})
	  (compile-file (.toString file) package)
	  (javac (src (string-append class ".java")))))))

(define (-C dir files)
  (map (lambda (f) (list "-C" dir (relativize dir f))) files))

(define (make-version)
  ;; Version and build date displayed when JScheme starts.
    (call-with-output-file (src "jsint/version.txt")
      (lambda (s)
	(display
	 (let* ((d (.format (DateFormat.getDateTimeInstance DateFormat.SHORT$
							    DateFormat.SHORT$)
			    (Date.))))
	 {JScheme 7.2 ([d]) http://jscheme.sourceforge.net\n}
	 ) s))))

(define (isSchemeFile f)
  ;; Local definition to capture elf/eopl2/jscheme/psyntax.*
  ;; And slib jscheme.init files. 
  (let ((f (.toString (.getName f))))
    (or (.endsWith f ".scm")
        (.endsWith f ".sch")		; gabriel
        (.endsWith f ".ss")		; eopl2
        (.endsWith f ".pp")		; eopl2
        (.endsWith f ".init")		; slib/jscheme.init
        (.equals f "version.txt")	; jsint/version.txt
        )))

(define (make-compiler)
  ;; Compile the compiler.
  ;; temporarily turn off the javadot warnings
  (define showJavaDotWarnings jsint.DynamicEnvironment.showJavaDotWarnings$)
  (set! jsint.DynamicEnvironment.showJavaDotWarnings$ #f)
  ;; Compile the .scm -> .java compiler.
  (display {Compiling the compiler....\n})
  (begin
   (load "jsint/compiler/Reflect.scm")
   (load "jsint/compiler/CompileReflection.scm")
   ;; compile utility procedures
   (javac (src "jsint/Function.java") (src "jsint/LCO.java"))
   (load "jsint/compiler/Compiler.scm") ; Needs jsint.Function.
   (compile "jsint/compiler/Reflect" "jsint.compiler")
   (compile "jsint/compiler/CompileReflection" "jsint.compiler")

   ;; compile the compiler
   (compile "jsint/compiler/Compiler" "jsint.compiler")
   ;; compile the toplevel interface to compiler
   (compile "jsint/Compile" "jsint")
   ;; Load the compiler.
   (jsint.Compile.load)
   (set! jsint.DynamicEnvironment.showJavaDotWarnings$ showJavaDotWarnings)
   ))

(define (copyFiles** from to predicate)
  (copyFiles from to (files** from predicate)))

(define (notDirectory? f) (not (.isDirectory f)))


;;;
;;; Commands.
;;;

(define-command (-help)
  "Print help message"
  (display {[commandAppName] command*\n
The default command is -all
command ::= -key arg*\n
Where command is one of:\n})
  (for-each (lambda (c)
	      (display (commandDocumentation c))
	      (newline))
	    (reverse commands)))

(define-command (-all)
  "Build everything."
  (display "-all\n")
  (-clean)
  (-javac)
  (-jar)
  (-test)
  (-javadoc)
  )

(define-command (-clean)
  "Remove all .class files."
  (display "-clean\n")
  (for-each .delete (files** classDir isClassFile)))

(define-command (-javac)
  "Compile Java classes."
  (display "-javac\n")
  (make-version)
  (for-each
   (lambda (f)
     (let ((f (src f)))
       (if (compileUpdate? f) (load f))))
   ;; Loading these files will cause java classes to be created.
   '("jsint/listener.scm" "jsint/primitives.scm"))
  (javacAll (src "jsint"))
  (for-each javacAll directories)
  (make-compiler))
  
(define-command (-jar)
  "Make lib/jscheme.jar" 
  ;; ... but not applet.jar and lib/jschemelite.jar"
  ;; Build jscheme.jar with everything in it.
  (display "-jar\n")
  (out (run (cmd jar cfm lib/jscheme.jar ,(src "jsint/manifest")
                 ,(-C classDir (files** classDir isClassFile))
                 ,(-C srcDir (files** srcDir isSchemeFile)))))
  (display {\nlib/jscheme.jar built.\n})
  )

(define-command  (-test)
  "Run tests on lib/jscheme.jar"
  (display "-test\n")
  (out (run (cmd java -classpath lib/jscheme.jar jscheme.REPL
		 jscheme/SchemeTests.scm "(exit)"))))

(define-command (-javadoc)
  "Make Java api documentation."
  (display "-javadoc\n")
  (let* ((packageFile (.getCanonicalFile (File. "doc/java-packages.txt")))
         (javaFile (.getCanonicalFile (File. "doc/java-files.txt")))
	 (args (list 
		"-overview" (File. "doc/overview.html")
		"-classpath" compileClasspath
		"-d" (mkdirs (File. "doc/api"))
                "-sourcepath" (File. "src")
		"-use"
		"-version"
		"-author"
		"-windowtitle" {[appName] API}
		"-doctitle" appName
		{@[packageFile]}
		)))
    ;; Generate the packageFile.
    (call-with-output-file
	packageFile
      (lambda (s)
	(for-each
	 (lambda (p) (display (.getName p) s) (newline s))
	 directories)))
    (call-with-output-file
	javaFile
      (lambda (s)
	(for-each
	 (lambda (f) (display f s) (newline s))
	 (apply append (map (lambda (d) (files* d isJavaFile)) directories)))))
    (out (run (cmd javadoc ,args)))))

(define-command (-repl)
  "Start a Read Eval Print Loop"
  (display "-repl\n")
  (Scheme.readEvalWriteLoop "> "))
