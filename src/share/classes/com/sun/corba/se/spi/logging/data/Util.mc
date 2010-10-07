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

("com.sun.corba.se.impl.logging" "UtilSystemException" UTIL
    (
	(BAD_OPERATION
	    (STUB_FACTORY_COULD_NOT_MAKE_STUB 1 FINE 
	     "StubFactory failed on makeStub call")
	    (ERROR_IN_MAKE_STUB_FROM_REPOSITORY_ID 2 FINE 
	     "Error in making stub given RepositoryId") 
	    (FAILURE_IN_MAKE_STUB_FROM_REPOSITORY_ID 3 WARNING 
	     "Failure in making stub given RepositoryId") 
	    (CLASS_CAST_EXCEPTION_IN_LOAD_STUB 4 FINE 
	     "ClassCastException in loadStub")
	    (EXCEPTION_IN_LOAD_STUB 5 WARNING 
	     "Exception in loadStub")
	    (COULD_NOT_MAKE_STUB_FROM_REPOSITORY_ID 6 WARNING 
	     "Unable to make stub from any of the repository IDs of the interface")
	    (EXCEPTION_IN_CREATE_IIOP_OUTPUT_STREAM 7 WARNING
	     "An IOException occurred while creating an IIOPOutputStream")
	    (EXCEPTION_IN_CREATE_IIOP_INPUT_STREAM 8 WARNING
	     "An IOException occurred while creating an IIOPInputStream")
	    (ONLY_ONE_CALL_TO_CONSTRUCTOR_ALLOWED 
	     9 WARNING "Only one call to the Util constructor is allowed; normally Util.getInstance should be called")
	    )
	(BAD_PARAM
	    (NO_POA 2 WARNING "Error in loadStubAndUpdateCache caused by _this_object")
	    (CONNECT_WRONG_ORB 3 FINE "Tried to connect already connected Stub Delegate to a different ORB")
	    (CONNECT_NO_TIE    4 WARNING "Tried to connect unconnected Stub Delegate but no Tie was found")
	    (CONNECT_TIE_WRONG_ORB 5 WARNING "Tried to connect unconnected stub with Tie in a different ORB")
	    (CONNECT_TIE_NO_SERVANT 6 WARNING "Tried to connect unconnected stub to unconnected Tie")
	    (LOAD_TIE_FAILED 7 FINE "Failed to load Tie of class {0}")
	    )
	(DATA_CONVERSION 
	    (BAD_HEX_DIGIT 1 WARNING "Bad hex digit in string_to_object"))
	(MARSHAL
	    (UNABLE_LOCATE_VALUE_HELPER 2 WARNING "Could not locate value helper")
	    (INVALID_INDIRECTION 3 WARNING "Invalid indirection {0}"))
	(INV_OBJREF 
	    (OBJECT_NOT_CONNECTED 1 WARNING "{0} did not originate from a connected object")
	    (COULD_NOT_LOAD_STUB 2 WARNING "Could not load stub for class {0}")
	    (OBJECT_NOT_EXPORTED 3 WARNING "Class {0} not exported, or else is actually a JRMP stub"))
	(INTERNAL
	    (ERROR_SET_OBJECT_FIELD 1 WARNING "Error in setting object field {0} in {1} to {2}")
	    (ERROR_SET_BOOLEAN_FIELD 2 WARNING "Error in setting boolean field {0} in {1} to {2}")
	    (ERROR_SET_BYTE_FIELD 3 WARNING "Error in setting byte field {0} in {1} to {2}")
	    (ERROR_SET_CHAR_FIELD 4 WARNING "Error in setting char field {0} in {1} to {2}")
	    (ERROR_SET_SHORT_FIELD 5 WARNING "Error in setting short field {0} in {1} to {2}")
	    (ERROR_SET_INT_FIELD 6 WARNING "Error in setting int field {0} in {1} to {2}")
	    (ERROR_SET_LONG_FIELD 7 WARNING "Error in setting long field {0} in {1} to {2}")
	    (ERROR_SET_FLOAT_FIELD 8 WARNING "Error in setting float field {0} in {1} to {2}")
	    (ERROR_SET_DOUBLE_FIELD 9 WARNING "Error in setting double field {0} in {1} to {2}")
	    (ILLEGAL_FIELD_ACCESS 10 WARNING "IllegalAccessException while trying to write to field {0}")
	    (BAD_BEGIN_UNMARSHAL_CUSTOM_VALUE 11 WARNING "State should be saved and reset first")
	    (CLASS_NOT_FOUND 12 WARNING "Failure while loading specific Java remote exception class: {0}")
	    (COULD_NOT_FIND_JDK_VALUE_HANDLER 13 WARNING
	     "Could not find the expected Value Handler implementation in the JDK: Wrong JDK Version?")
	    (HANDLE_SYSTEM_EXCEPTION 14 FINE
	     " Bad Operation or Bad Invocation Order : The Servant has not been associated with an ORB instance")
            (TEST_EXCEPTION 15 INFO
             "This is a test exception with number {0}")
            (SIMPLE_TEST_EXCEPTION 16 WARNING
             "This is another test exception with no parameters"))
	(UNKNOWN
	    (UNKNOWN_SYSEX 1 WARNING "Unknown System Exception"))))
