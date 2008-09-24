;  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
; 
;  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

("com.sun.corba.se.impl.logging" "IORSystemException" IOR
    (
	(INTERNAL 
	    (ORT_NOT_INITIALIZED 1 WARNING "ObjectReferenceTemplate is not initialized")
	    (NULL_POA 2 WARNING "Null POA")
	    (BAD_MAGIC 3 WARNING "Bad magic number {0} in ObjectKeyTemplate")
	    (STRINGIFY_WRITE_ERROR 4  WARNING "Error while stringifying an object reference")
	    (TAGGED_PROFILE_TEMPLATE_FACTORY_NOT_FOUND 5 WARNING "Could not find a TaggedProfileTemplateFactory for id {0}")
	    (INVALID_JDK1_3_1_PATCH_LEVEL 6 WARNING "Found a JDK 1.3.1 patch level indicator with value {0} less than JDK 1.3.1_01 value of 1")
	    (GET_LOCAL_SERVANT_FAILURE 7 FINE "Exception occurred while looking for ObjectAdapter {0} in IIOPProfileImpl.getServant")
	    (IOEXCEPTION_DURING_STREAM_CLOSE 8 WARNING "Exception occurred while closing an IO stream object"))
	(BAD_OPERATION
	    (ADAPTER_ID_NOT_AVAILABLE 1 WARNING "Adapter ID not available")
	    (SERVER_ID_NOT_AVAILABLE 2 WARNING "Server ID not available")
	    (ORB_ID_NOT_AVAILABLE 3 WARNING "ORB ID not available")
	    (OBJECT_ADAPTER_ID_NOT_AVAILABLE 4 WARNING "Object adapter ID not available"))
	(BAD_PARAM 
	    (BAD_OID_IN_IOR_TEMPLATE_LIST 1 WARNING "Profiles in IOR do not all have the same Object ID, so conversion to IORTemplateList is impossible")
	    (INVALID_TAGGED_PROFILE 2 WARNING "Error in reading IIOP TaggedProfile")
	    (BAD_IIOP_ADDRESS_PORT 3 WARNING "Attempt to create IIOPAddress with port {0}, which is out of range"))
	(INV_OBJREF
	    (IOR_MUST_HAVE_IIOP_PROFILE 1 WARNING "IOR must have at least one IIOP profile"))))
