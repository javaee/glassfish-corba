/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package corba.dynamicrmiiiop;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;

import corba.dynamicrmiiiop.testclasses.*;
import com.sun.corba.ee.impl.presentation.rmi.IDLTypesUtil ;

public class TestRMIIDLTypes extends TestCase {

    private IDLTypesUtil idlTypesUtil;

    public static Test suite() 
    {
        return new TestSuite(TestRMIIDLTypes.class);
    }

    protected void setUp()
    {
        idlTypesUtil = new IDLTypesUtil();
    }

    protected void tearDown()
    {
    }

    public void testPrimitiveTypes() 
    {
        Class[] primitives = {
            Void.TYPE, Boolean.TYPE, Byte.TYPE, Character.TYPE,
            Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
        };

        for(int i = 0; i < primitives.length; i++) {
            Class primitive = primitives[i];
            String msg = primitive.getName();
            Assert.assertTrue(msg, idlTypesUtil.isPrimitive(primitive));
            Assert.assertFalse(msg,idlTypesUtil.isRemoteInterface(primitive));
            Assert.assertFalse(msg,idlTypesUtil.isValue(primitive));
            Assert.assertFalse(msg,idlTypesUtil.isArray(primitive));
            Assert.assertFalse(msg,idlTypesUtil.isException(primitive));
            Assert.assertFalse(msg,idlTypesUtil.isObjectReference(primitive));
            Assert.assertFalse(msg,idlTypesUtil.isEntity(primitive));
        }

        Class[] nonPrimitives = {
            Byte.class, Short.class, Integer.class, Long.class, Float.class, 
            Double.class, String.class, java.util.Date.class, Object.class
        };

        for(int i = 0; i < nonPrimitives.length; i++) {
            Class nonPrimitive = nonPrimitives[i];
            String msg = nonPrimitive.getName();
            Assert.assertFalse(msg, idlTypesUtil.isPrimitive(nonPrimitive));
        }
        
    }

    public void testRemoteInterfaceTypes() 
    {

        Class[] remoteInterfaces = ValidRemotes.CLASSES;

        for(int i = 0; i < remoteInterfaces.length; i++) {
            Class remoteIntf = remoteInterfaces[i];
            String msg = remoteIntf.getName();

            Assert.assertTrue(msg, idlTypesUtil.isRemoteInterface(remoteIntf));

            Assert.assertFalse(msg,idlTypesUtil.isPrimitive(remoteIntf));
            Assert.assertFalse(msg,idlTypesUtil.isValue(remoteIntf));
            Assert.assertFalse(msg,idlTypesUtil.isArray(remoteIntf));
            Assert.assertFalse(msg,idlTypesUtil.isException(remoteIntf));
            Assert.assertFalse(msg,idlTypesUtil.isObjectReference(remoteIntf));
            Assert.assertFalse(msg,idlTypesUtil.isEntity(remoteIntf));
        }

        // NOTE invalid remote interfaces are tested in TestIDLNameTranslator
    }
    
    public void testValueTypes() 
    {

        Class[] values = ValidValues.CLASSES;

        for(int i = 0; i < values.length; i++) {
            Class value = values[i];
            String msg = value.getName();

            Assert.assertTrue(msg,idlTypesUtil.isValue(value));

            Assert.assertFalse(msg, idlTypesUtil.isPrimitive(value));
            Assert.assertFalse(msg,idlTypesUtil.isRemoteInterface(value));
            Assert.assertFalse(msg,idlTypesUtil.isArray(value));
            Assert.assertFalse(msg,idlTypesUtil.isException(value));
            Assert.assertFalse(msg,idlTypesUtil.isObjectReference(value));
            Assert.assertFalse(msg,idlTypesUtil.isEntity(value));
        }

        Class[] nonValues = InvalidValues.CLASSES;

        for(int i = 0; i < nonValues.length; i++) {
            Class nonValue = nonValues[i];
            String msg = nonValue.getName();
            Assert.assertFalse(msg, idlTypesUtil.isValue(nonValue));
        }
        
    }

    public void testExceptionTypes()
    {

        Class[] exceptions = ValidExceptions.CLASSES;

        for(int i = 0; i < exceptions.length; i++) {
            Class excep = exceptions[i];
            String msg = excep.getName();

            Assert.assertTrue(msg,idlTypesUtil.isException(excep));
            // a valid exception is always a valid value type !
            Assert.assertTrue(msg,idlTypesUtil.isValue(excep));

            Assert.assertFalse(msg, idlTypesUtil.isPrimitive(excep));
            Assert.assertFalse(msg,idlTypesUtil.isRemoteInterface(excep));
            Assert.assertFalse(msg,idlTypesUtil.isArray(excep));
            Assert.assertFalse(msg,idlTypesUtil.isObjectReference(excep));
            Assert.assertFalse(msg,idlTypesUtil.isEntity(excep));
        }

        Class[] nonExceptions = InvalidExceptions.CLASSES;

        for(int i = 0; i < nonExceptions.length; i++) {
            Class nonException = nonExceptions[i];
            String msg = nonException.getName();
            Assert.assertFalse(msg, idlTypesUtil.isException(nonException));
        }        
    }

    public void testObjRefs()
    {

        Class[] objRefs = ValidObjRefs.CLASSES;

        for(int i = 0; i < objRefs.length; i++) {
            Class objRef = objRefs[i];
            String msg = objRef.getName();

            Assert.assertTrue(msg,idlTypesUtil.isObjectReference(objRef));

            Assert.assertFalse(msg, idlTypesUtil.isPrimitive(objRef));
            Assert.assertFalse(msg,idlTypesUtil.isRemoteInterface(objRef));
            Assert.assertFalse(msg,idlTypesUtil.isValue(objRef));
            Assert.assertFalse(msg,idlTypesUtil.isArray(objRef));
            Assert.assertFalse(msg,idlTypesUtil.isException(objRef));
            Assert.assertFalse(msg,idlTypesUtil.isEntity(objRef));
        }

        Class[] nonObjRefs = InvalidObjRefs.CLASSES;

        for(int i = 0; i < nonObjRefs.length; i++) {
            Class nonObjRef = nonObjRefs[i];
            String msg = nonObjRef.getName();
            Assert.assertFalse(msg, idlTypesUtil.isObjectReference(nonObjRef));
        }        

    }

    public void testEntities()
    {

        Class[] entities = ValidEntities.CLASSES;

        for(int i = 0; i < entities.length; i++) {
            Class entity = entities[i];
            String msg = entity.getName();

            Assert.assertTrue(msg,idlTypesUtil.isEntity(entity));
            // An entity type is always a value type
            Assert.assertTrue(msg,idlTypesUtil.isValue(entity));

            Assert.assertFalse(msg, idlTypesUtil.isPrimitive(entity));
            Assert.assertFalse(msg,idlTypesUtil.isRemoteInterface(entity));
            Assert.assertFalse(msg,idlTypesUtil.isArray(entity));
            Assert.assertFalse(msg,idlTypesUtil.isException(entity));
            Assert.assertFalse(msg,idlTypesUtil.isObjectReference(entity));

        }

        Class[] nonEntities = InvalidEntities.CLASSES;

        for(int i = 0; i < nonEntities.length; i++) {
            Class nonEntity = nonEntities[i];
            String msg = nonEntity.getName();
            Assert.assertFalse(msg, idlTypesUtil.isEntity(nonEntity));
        }        

    }

}
