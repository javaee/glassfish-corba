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

package com.sun.corba.se.spi.orbutil.logex.corba ;

import com.sun.corba.se.org.omg.CORBA.SUNVMCID;
import com.sun.corba.se.spi.orbutil.logex.ExceptionWrapper;
import com.sun.corba.se.spi.orbutil.logex.Log;
import java.lang.reflect.Constructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.ACTIVITY_COMPLETED;
import org.omg.CORBA.ACTIVITY_REQUIRED;
import org.omg.CORBA.BAD_CONTEXT;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_QOS;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.CODESET_INCOMPATIBLE;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.FREE_MEM;
import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INTF_REPOS;
import org.omg.CORBA.INVALID_ACTIVITY;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.INV_FLAG;
import org.omg.CORBA.INV_IDENT;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NO_MEMORY;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.NO_RESPONSE;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.OMGVMCID;
import org.omg.CORBA.PERSIST_STORE;
import org.omg.CORBA.REBIND;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.TRANSACTION_UNAVAILABLE;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.UNKNOWN;

public class CorbaExtension extends StandardLogger {
    public static final CorbaExtension self = new CorbaExtension() ;

    private CorbaExtension() {}

    public static final int ORBUtilGroup = 0 ;
    public static final int ActivationGroup = 1 ;
    public static final int NamingGroup = 2 ;
    public static final int InterceptorsGroup = 3 ;
    public static final int POAGroup = 4 ;
    public static final int IORGroup = 5 ;
    public static final int UtilGroup = 6 ;

    private static final Class<?>[] SYS_EX_CLASSES = {
        UNKNOWN.class, BAD_PARAM.class, NO_MEMORY.class, 
	IMP_LIMIT.class, COMM_FAILURE.class, INV_OBJREF.class,
	NO_PERMISSION.class, INTERNAL.class, MARSHAL.class,
	INITIALIZE.class, NO_IMPLEMENT.class, BAD_TYPECODE.class,
        BAD_OPERATION.class, NO_RESOURCES.class, NO_RESPONSE.class, 
	PERSIST_STORE.class, BAD_INV_ORDER.class, TRANSIENT.class,
	FREE_MEM.class, INV_IDENT.class, INV_FLAG.class,
	INTF_REPOS.class, BAD_CONTEXT.class, OBJ_ADAPTER.class,
        DATA_CONVERSION.class, OBJECT_NOT_EXIST.class, TRANSACTION_REQUIRED.class, 
	TRANSACTION_ROLLEDBACK.class, INVALID_TRANSACTION.class, INV_POLICY.class,
	CODESET_INCOMPATIBLE.class, REBIND.class, TIMEOUT.class,
	TRANSACTION_UNAVAILABLE.class, BAD_QOS.class, INVALID_ACTIVITY.class,
        ACTIVITY_COMPLETED.class, ACTIVITY_REQUIRED.class } ;

    @SuppressWarnings("unchecked")
    private static final List<Constructor<SystemException>> SYS_EX_CONSTRUCTORS =
	new ArrayList<Constructor<SystemException>>(
            SYS_EX_CLASSES.length) ;

    static {
	Class<?>[] ptypes = { String.class, int.class,
	    CompletionStatus.class } ;

        for (Class<?> cls : SYS_EX_CLASSES) {
		try {
                    @SuppressWarnings("unchecked")
		    final Constructor<SystemException> cons =
                        (Constructor<SystemException>)cls.getDeclaredConstructor(
                            ptypes);
		    SYS_EX_CONSTRUCTORS.add(cons) ;
		} catch (Exception ex) {
		    throw new RuntimeException(
			"Cound not find constructor for " + cls, ex ) ;
		}


        }
    }

    private int findClass( Class<?> cls ) {
        for (int ctr=0; ctr<SYS_EX_CLASSES.length; ctr++ ) {
            if (cls.equals( SYS_EX_CLASSES[ctr] )) {
                return ctr ;
            }
        }

        throw new RuntimeException(
	    cls + " is not a subclass of SystemException" ) ;
    }

    private ORBException getORBException( Method method) {
        final Class<?> cls = method.getDeclaringClass() ;
	final ORBException orbex = cls.getAnnotation( ORBException.class ) ;
	return orbex ;
    }

    private Log getLog( Method method ) {
	Log log = method.getAnnotation( Log.class );
	if (log == null) {
		throw new RuntimeException(
		    "No Log annotation present on " + method ) ;
        } else {
	    return log ;
        }
    }

    private int getExceptionId( Method method ) {
        final Class<?> rtype = method.getReturnType() ;
	final int exceptionId = findClass( rtype ) ;
	return exceptionId ;
    }

    private int getMinorCode( ORBException orbex, Log log ) {
	return 200*orbex.group() + log.id() ;
    }

    public int getMinorCode( Method method ) {
	final ORBException orbex = getORBException( method ) ;
	final Log log = getLog( method ) ;
	final int minorCode = getMinorCode( orbex, log ) ;
	return minorCode ;
    }

    public int getMinorCode( Class<?> cls, String methodName ) {
        Method method = null ;
        for (Method m : cls.getDeclaredMethods()) {
            if (methodName.equals( m.getName())) {
                method = m ;
                break ;
            }
        }

        if (method == null) {
            return -1 ;
        } else {
            return getMinorCode( method ) ;
        }
    }

    // Format of result:  ExceptionId OmgID MinorCode, where
    // ExceptionId is the ordinal position of the return type in SYS_EX_Classes,
    //     padded to 3 places
    // OmgId is 0 for OMG exceptions, 1 for Old Sun exceptions
    // MinorCode is 200*groupId + id
    @Override
    public String getLogId( Method method ) {
	final ORBException orbex = getORBException( method ) ;
	final Log log = getLog( method ) ;
	final int minorCode = getMinorCode( orbex, log ) ;
	final int exceptionId = getExceptionId( method ) ;

	final int omgId = orbex.omgException() ? 0 : 1 ;

	final String result = String.format( "%03d%1d%04d",
	    exceptionId, omgId, minorCode ) ;

	return result ;
    }

    @Override
    public Exception makeException( String msg, Method method ) {
	try {
	    final ORBException orbex = getORBException( method ) ;
	    final Log log = getLog( method ) ;
	    final int minorCode = getMinorCode( orbex, log ) ;
	    final int exceptionId = getExceptionId(method) ;

	    final Constructor<SystemException> cons = SYS_EX_CONSTRUCTORS.get(exceptionId) ;

	    final CS cs = method.getAnnotation( CS.class ) ;
	    final CSValue csv = cs == null ? CSValue.NO : cs.value() ;

	    final int base = orbex.omgException() ?
		SUNVMCID.value :
		OMGVMCID.value ;

	    SystemException result = cons.newInstance(msg, base + minorCode,
                csv.getCompletionStatus()) ;

	    return result ;
	} catch (Exception exc) {
	    throw new RuntimeException( exc ) ;
	}
    }

    private static final String CLASS_NAME_SUFFIX = "SystemException" ;

    @Override
    public String getLoggerName( Class<?> cls ) {
        final ExceptionWrapper ew = cls.getAnnotation(
            ExceptionWrapper.class ) ;

        String str = ew.loggerName() ;
        if (str.length() == 0) {
            str = cls.getSimpleName() ;
            if (str.endsWith(CLASS_NAME_SUFFIX)) {
                str = str.substring( 0,
                    str.length() - CLASS_NAME_SUFFIX.length()) ;
            }

            return StandardLogger.CORBA_LOGGER_PREFIX + "." + str ;
        }

        return str ;
    }
}
