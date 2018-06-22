/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.presentation.rmi ;

import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.IOException ;
import java.io.Serializable ;

import java.rmi.RemoteException ;

import org.omg.CORBA_2_3.portable.ObjectImpl ;

import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA.portable.OutputStream ;

import org.omg.CORBA.ORB ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub ;
import com.sun.corba.ee.impl.ior.StubIORImpl ;
import com.sun.corba.ee.impl.util.RepositoryId ;
import com.sun.corba.ee.impl.util.JDKBridge ;

public class DynamicStubImpl extends ObjectImpl 
    implements DynamicStub, Serializable
{
    private static final long serialVersionUID = 4852612040012087675L;

    private String[] typeIds ;
    private StubIORImpl ior ;
    private DynamicStub self = null ;  // The actual DynamicProxy for this stub.

    public void setSelf( DynamicStub self ) 
    {
        this.self = self ;
    }

    public DynamicStub getSelf()
    {
        return self ;
    }

    public DynamicStubImpl( String[] typeIds ) 
    {
        this.typeIds = typeIds ;
        ior = null ;
    }

    public void setDelegate( Delegate delegate ) 
    {
        _set_delegate( delegate ) ;
    }

    public Delegate getDelegate() 
    {
        return _get_delegate() ;
    }

    public ORB getORB()
    {
        return _orb() ;
    }

    public String[] _ids() 
    {
        return typeIds.clone() ;
    }

    public String[] getTypeIds() 
    {
        return _ids() ;
    }

    public void connect( ORB orb ) throws RemoteException 
    {
        ior = StubConnectImpl.connect( ior, self, this, orb ) ;
    }

    public boolean isLocal()
    {
        return _is_local() ;
    }

    public OutputStream request( String operation, 
        boolean responseExpected ) 
    {
        return _request( operation, responseExpected ) ; 
    }
    
    private void readObject( ObjectInputStream stream ) throws 
        IOException, ClassNotFoundException
    {
        ior = new StubIORImpl() ;
        ior.doRead( stream ) ;
    }

    private void writeObject( ObjectOutputStream stream ) throws
        IOException
    {
        if (ior == null) {
            ior = new StubIORImpl(this);
        }
        ior.doWrite( stream ) ;
    }

    public Object readResolve()
    {
        String repositoryId = ior.getRepositoryId() ;
        String cname = RepositoryId.cache.getId( repositoryId ).getClassName() ; 

        Class<?> cls = null ;

        try {
            cls = JDKBridge.loadClass( cname, null, null ) ;
        } catch (ClassNotFoundException exc) {
            Exceptions.self.readResolveClassNotFound( exc, cname ) ;
        }

        PresentationManager pm = 
            com.sun.corba.ee.spi.orb.ORB.getPresentationManager() ;
        PresentationManager.ClassData classData = pm.getClassData( cls ) ;
        InvocationHandlerFactoryImpl ihfactory = 
            (InvocationHandlerFactoryImpl)classData.getInvocationHandlerFactory() ;
        return ihfactory.getInvocationHandler( this ) ;
    }
}
