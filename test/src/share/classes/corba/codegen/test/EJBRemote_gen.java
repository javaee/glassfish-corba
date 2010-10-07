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
package corba.codegen.test ;

import corba.codegen.ClassGeneratorFactory ;

import corba.codegen.lib.Hello ;
import corba.codegen.lib.HelloRemote ;

import java.lang.reflect.Method;
import java.io.*;
import java.util.*;

import static java.lang.reflect.Modifier.*;
import static com.sun.corba.se.spi.orbutil.codegen.Wrapper.*;

import com.sun.corba.se.spi.orbutil.codegen.ClassGenerator;
import com.sun.corba.se.spi.orbutil.codegen.Type;
import com.sun.corba.se.spi.orbutil.codegen.Expression;

public class EJBRemote_gen 
    implements ClassGeneratorFactory {

    private String remoteInterfaceName;
    private Class businessInterface;
    private String remoteClientClassName;
    private String remoteClientPackageName;
    private String remoteClientSimpleName;
    private Method[] bizMethods;

    public String getGeneratedClass() {
        return remoteClientClassName;
    }
    
    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }

    private String getPackageName(String className) {
        int dot = className.lastIndexOf('.');
        if (dot == -1)
            return null;
        return className.substring(0, dot);
    }

    private String getBaseName(String className) {
        int dot = className.lastIndexOf('.');
        if (dot == -1)
            return className;
        return className.substring(dot+1);
    }

    public EJBRemote_gen( ) {
        this( Hello.class.getName(), HelloRemote.class.getName() ) ; 
    }

    /**
     * Construct the Wrapper generator with the specified deployment
     * descriptor.
     */
    protected EJBRemote_gen( String businessIntfName, String remoteIntfName) {
        remoteInterfaceName = remoteIntfName;

        try {
            this.businessInterface = getClass().getClassLoader().loadClass(businessIntfName);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException( businessIntfName + " not found " ) ;
            // throw new InvalidBean(
                // localStrings.getLocalString(
                // "generator.remote_interface_not_found",
                // "Business interface " + businessInterface + " not found "));
        }

        if( javax.ejb.EJBObject.class.isAssignableFrom(businessInterface) ) {
            throw new RuntimeException("Invalid Remote Business Interface " +
                 businessInterface + ". A Remote Business interface MUST " +
                 "not extend javax.ejb.EJBObject.");
        }

        remoteClientClassName = businessInterface.getName() + "_Wrapper" ;
        // remoteClientClassName = EJBUtils.
            // getGeneratedRemoteWrapperName(businessInterface.getName());

        remoteClientPackageName = getPackageName(remoteClientClassName);
        remoteClientSimpleName = getBaseName(remoteClientClassName);

        bizMethods = businessInterface.getMethods();

        // NOTE : no need to remove ejb object methods because EJBObject
        // is only visible through the RemoteHome view.
    }

    public ClassGenerator evaluate() {
        _clear();

        if (remoteClientPackageName != null) {
	    _package(remoteClientPackageName);
        } else {
            // no-arg _package() call is required for default package
            _package();
        }

        _class(PUBLIC, remoteClientSimpleName, 
               _t("com.sun.ejb.containers.RemoteBusinessWrapperBase"), 
               _t(businessInterface.getName()));
        
        _data(PRIVATE, _t(remoteInterfaceName), "delegate_");

        _constructor( PUBLIC ) ;
        _arg(_t(remoteInterfaceName), "stub");
        _arg(_String(), "busIntf");

        _body();
        _expr(_super(_s(_void(), _t("java.rmi.Remote"), _String()),
                     _v("stub"), _v("busIntf"))) ;
        _assign(_v("delegate_"), _v("stub"));
        _end();

        for(int i = 0; i < bizMethods.length; i++) {
	    printMethodImpl(bizMethods[i]);
	}

        _end();

        /* Not needed for test
        try {
            java.util.Properties p = new java.util.Properties();
            p.put("Wrapper.DUMP_AFTER_SETUP_VISITOR", "true");
            p.put("Wrapper.TRACE_BYTE_CODE_GENERATION", "true");
            p.put("Wrapper.USE_ASM_VERIFIER", "true");
            _byteCode(loader, p);
        } catch(Exception e) {
            System.out.println("Got exception when generating byte code");
            e.printStackTrace();
        }
        */

        return _classGenerator() ;
    }

    private void printMethodImpl(Method m) {
        List<Type> exceptionList = new LinkedList<Type>();
	for(Class exception : m.getExceptionTypes()) {
            exceptionList.add(Type.type(exception));
	}

        _method( PUBLIC, Type.type(m.getReturnType()),
                 m.getName(), exceptionList);

        int i = 0;
        List<Type> expressionListTypes = new LinkedList<Type>();
        List<Expression> expressionList = new LinkedList<Expression>();
        for(Class param : m.getParameterTypes()) {
            String paramName = "param" + i;
            _arg(Type.type(param), paramName);
            i++;
            expressionListTypes.add(Type.type(param));
            expressionList.add(_v(paramName));
	}

        _body();
        _try();

        Class returnType = m.getReturnType();

        if( returnType == void.class ) {
            _expr( _call( _v("delegate_"), m.getName(), 
                          _s(Type.type(returnType), expressionListTypes), 
                          expressionList));
        } else {
            _return( _call( _v("delegate_"), m.getName(), 
                            _s(Type.type(returnType), expressionListTypes), 
                            expressionList) );
        }

        boolean doExceptionTranslation = 
            !java.rmi.Remote.class.isAssignableFrom(businessInterface);
        if( doExceptionTranslation ) {

            _catch( _t("javax.transaction.TransactionRolledbackException"),
                    "trex");

                _define( _t("java.lang.RuntimeException"), "r", 
                         _new( _t("javax.ejb.EJBTransactionRolledbackException"), 
                           _s(_void())));
                _expr( _call( _v("r"), "initCause",
                              _s(_t("java.lang.Throwable"), 
                                 _t("java.lang.Throwable")), 
                              _v("trex")));
                _throw(_v("r"));

            _catch( _t("javax.transaction.TransactionRequiredException"),
                    "treqex");

                _define( _t("java.lang.RuntimeException"), "r", 
                         _new( _t("javax.ejb.EJBTransactionRequiredException"), 
                           _s(_void())));
                _expr( _call( _v("r"), "initCause",
                              _s(_t("java.lang.Throwable"), 
                                 _t("java.lang.Throwable")), 
                              _v("treqex")));
                _throw(_v("r"));

            _catch( _t("java.rmi.NoSuchObjectException"),
                    "nsoe");

                _define( _t("java.lang.RuntimeException"), "r", 
                         _new( _t("javax.ejb.NoSuchEJBException"), 
                           _s(_void())));
                _expr( _call( _v("r"), "initCause",
                              _s(_t("java.lang.Throwable"), 
                                 _t("java.lang.Throwable")), 
                              _v("nsoe")));
                _throw(_v("r"));
/* This exception apparently is never thrown, so let's comment it out for now
            _catch(_t("com.sun.ejb.containers.InternalEJBContainerException"),
                   "iejbcEx");
            // There's only one kind of InternalEJBContainerException, so
            // we know it's a ParallelAccessException
            _define(_t("com.sun.ejb.containers.ParallelAccessException"),
                    "paEx", 
                    _cast(_t("com.sun.ejb.containers.ParallelAccessException"),
                          _v("iejbcEx")));
                    
                _define(_t("javax.ejb.ConcurrentAccessException"), "r", _new(
                        _t("javax.ejb.ConcurrentAccessException"), _s(_void())));
                _expr(_call(_v("r"), "initCause", _s(_t("java.lang.Throwable"),
                        _t("java.lang.Throwable")), _v("paEx")));
                _throw(_v("r"));
*/
            _catch( _t("java.rmi.RemoteException"), "re");
            
                _throw( _new( _t("javax.ejb.EJBException"), 
                              _s(_void(), _t("java.lang.Exception")), 
                              _v("re")));
            
            _catch( _t("org.omg.CORBA.SystemException"), "corbaSysEx");
                _define( _t("java.lang.RuntimeException"), "r", 
                         _new( _t("javax.ejb.EJBException"), 
                           _s(_void())));
                _expr( _call( _v("r"), "initCause",
                              _s(_t("java.lang.Throwable"), 
                                 _t("java.lang.Throwable")), 
                              _v("corbaSysEx")));
                _throw(_v("r"));

            _end();

        } else {
            _catch(_t("com.sun.ejb.containers.InternalEJBContainerException"), "iejbcEx");
                _throw( _new( _t("com.sun.ejb.containers.InternalRemoteException"), 
                              _s(_void(), _t("com.sun.ejb.containers.InternalEJBContainerException")), 
                              _v("iejbcEx")));
            _end();
        }

        _end();
    }

}
