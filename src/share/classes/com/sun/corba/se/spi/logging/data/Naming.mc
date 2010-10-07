;  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
;  
;  Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
;  
;  The contents of this file are subject to the terms of either the GNU
;  General Public License Version 2 only ("GPL") or the Common Development
;  and Distribution License("CDDL") (collectively, the "License").  You
;  may not use this file except in compliance with the License.  You can
;  obtain a copy of the License at
;  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
;  or packager/legal/LICENSE.txt.  See the License for the specific
;  language governing permissions and limitations under the License.
;  
;  When distributing the software, include this License Header Notice in each
;  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
;  
;  GPL Classpath Exception:
;  Oracle designates this particular file as subject to the "Classpath"
;  exception as provided by Oracle in the GPL Version 2 section of the License
;  file that accompanied this code.
;  
;  Modifications:
;  If applicable, add the following below the License Header, with the fields
;  enclosed by brackets [] replaced by your own identifying information:
;  "Portions Copyright [year] [name of copyright owner]"
;  
;  Contributor(s):
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

("com.sun.corba.se.impl.logging" "NamingSystemException" NAMING
    (
	(BAD_PARAM
	    (TRANSIENT_NAME_SERVER_BAD_PORT 0 WARNING "Port 0 is not a valid port in the transient name server")
	    (TRANSIENT_NAME_SERVER_BAD_HOST 1 WARNING "A null hostname is not a valid hostname in the transient name server")
	    (OBJECT_IS_NULL 2 WARNING "Invalid object reference passed in rebind or bind operation")
	    (INS_BAD_ADDRESS 3 WARNING "Bad host address in -ORBInitDef"))
	(UNKNOWN
	    (BIND_UPDATE_CONTEXT_FAILED 0 WARNING "Updated context failed for bind")
	    (BIND_FAILURE 1 WARNING "bind failure")
	    (RESOLVE_CONVERSION_FAILURE 2 WARNING "Resolve conversion failed")
	    (RESOLVE_FAILURE 3 WARNING "Resolve failure")
	    (UNBIND_FAILURE 4 WARNING "Unbind failure"))
	(INITIALIZE
	    (TRANS_NS_CANNOT_CREATE_INITIAL_NC_SYS 50 WARNING "SystemException in transient name service while initializing")
	    (TRANS_NS_CANNOT_CREATE_INITIAL_NC 51 WARNING "Java exception in transient name service while initializing"))
	(INTERNAL
	    (NAMING_CTX_REBIND_ALREADY_BOUND 0 WARNING "Unexpected AlreadyBound exception in rebind")
	    (NAMING_CTX_REBINDCTX_ALREADY_BOUND 1 WARNING "Unexpected AlreadyBound exception in rebind_context")
	    (NAMING_CTX_BAD_BINDINGTYPE 2 WARNING "Bad binding type in internal binding implementation")
	    (NAMING_CTX_RESOLVE_CANNOT_NARROW_TO_CTX 3 WARNING "Object reference that is not CosNaming::NamingContext bound as a context")
	    (NAMING_CTX_BINDING_ITERATOR_CREATE 4 WARNING "Error in creating POA for BindingIterator")
	    (TRANS_NC_BIND_ALREADY_BOUND 100 WARNING "Bind implementation encountered a previous bind")
	    (TRANS_NC_LIST_GOT_EXC 101 WARNING "list operation caught an unexpected Java exception while creating list iterator")
	    (TRANS_NC_NEWCTX_GOT_EXC 102 WARNING "new_context operation caught an unexpected Java exception creating the NewContext servant")
	    (TRANS_NC_DESTROY_GOT_EXC 103 WARNING "Destroy operation caught a Java exception while disconnecting from ORB")
	    (INS_BAD_SCHEME_NAME 105 WARNING "Stringified object reference with unknown protocol specified")
	    (INS_BAD_SCHEME_SPECIFIC_PART 107 WARNING "Malformed URL in -ORBInitDef")
	    (INS_OTHER 108 WARNING "Malformed URL in -ORBInitDef"))))
