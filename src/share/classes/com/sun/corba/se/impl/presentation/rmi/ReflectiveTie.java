/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.corba.se.impl.presentation.rmi ;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.Tie;

import java.lang.reflect.Method ;
import java.lang.reflect.InvocationTargetException ;

import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults ;
import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator ;
import com.sun.corba.se.spi.presentation.rmi.DynamicMethodMarshaller ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.proxy.DynamicAccessPermission ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.oa.poa.POAManagerImpl ;

public final class ReflectiveTie extends Servant implements Tie 
{
    private Remote target = null ;
    private PresentationManager pm ;
    private PresentationManager.ClassData classData = null ;
    private ORBUtilSystemException wrapper = null ;

    public ReflectiveTie( PresentationManager pm, ORBUtilSystemException wrapper )
    {
        if (!PresentationDefaults.inAppServer()) {
            SecurityManager s = System.getSecurityManager();
            if (!PresentationDefaults.inAppServer() && (s != null)) {
                s.checkPermission(new DynamicAccessPermission("access"));
            }
        }

	this.pm = pm ;
	this.wrapper = wrapper ;
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, 
	byte[] objectId)
    {
	return classData.getTypeIds() ;
    }

    public void setTarget(Remote target) 
    {
        this.target = target;

	if (target == null) {
	    classData = null ;
	} else {
	    Class targetClass = target.getClass() ;
	    classData = pm.getClassData( targetClass ) ;
	}
    }
    
    public Remote getTarget() 
    {
        return target;
    }
    
    public org.omg.CORBA.Object thisObject() 
    {
        return _this_object();
    }
    
    public void deactivate() 
    {
        try{
	    _poa().deactivate_object(_poa().servant_to_id(this));
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy exception){
	    // ignore 
        } catch (org.omg.PortableServer.POAPackage.ObjectNotActive exception){
	    // ignore 
        } catch (org.omg.PortableServer.POAPackage.ServantNotActive exception){
	    // ignore 
        }
    }
    
    public org.omg.CORBA.ORB orb() {
        return _orb();
    }
    
    public void orb(org.omg.CORBA.ORB orb) {
        try {
            ((org.omg.CORBA_2_3.ORB)orb).set_delegate(this);
        } catch (ClassCastException e) {
	    throw wrapper.badOrbForServant( e ) ;
        }
    }
   
    public Object dispatchToMethod( Method javaMethod, Remote target, Object[] args ) 
        throws InvocationTargetException {

        try {
            return javaMethod.invoke( target, args ) ;
	} catch (IllegalAccessException ex) {
	    throw wrapper.invocationErrorInReflectiveTie( ex, 
		javaMethod.getName(), 
		    javaMethod.getDeclaringClass().getName() ) ;
	} catch (IllegalArgumentException ex) {
	    throw wrapper.invocationErrorInReflectiveTie( ex, 
		javaMethod.getName(), 
		    javaMethod.getDeclaringClass().getName() ) ;
        }
    }

    public org.omg.CORBA.portable.OutputStream  _invoke(String method, 
	org.omg.CORBA.portable.InputStream _in, ResponseHandler reply) 
    {
	Method javaMethod = null ;
	DynamicMethodMarshaller dmm = null;

        try {
            InputStream in = (InputStream) _in;

	    javaMethod = classData.getIDLNameTranslator().getMethod( method ) ;
	    if (javaMethod == null)
		throw wrapper.methodNotFoundInTie( method, 
		    target.getClass().getName() ) ;

	    dmm = pm.getDynamicMethodMarshaller( javaMethod ) ;

	    Object[] args = dmm.readArguments( in ) ;

            Object result = dispatchToMethod( javaMethod, target, args ) ;

	    OutputStream os = (OutputStream)reply.createReply() ;

	    dmm.writeResult( os, result ) ; 

	    return os ;
	} catch (InvocationTargetException ex) {
	    // Unwrap the actual exception so that it can be wrapped by an
	    // UnknownException or thrown if it is a system exception.
	    // This is expected in the server dispatcher code.
	    Throwable thr = ex.getCause() ;
	    if (thr instanceof SystemException)
		throw (SystemException)thr ;
	    else if ((thr instanceof Exception) && 
		dmm.isDeclaredException( thr )) {
		OutputStream os = (OutputStream)reply.createExceptionReply() ;
		dmm.writeException( os, (Exception)thr ) ;
		return os ;	
	    } else
		throw new UnknownException( thr ) ;
        }
    }
}
