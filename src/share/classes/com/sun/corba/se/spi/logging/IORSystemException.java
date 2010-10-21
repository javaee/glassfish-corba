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

package com.sun.corba.se.spi.logging ;

import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.orbutil.logex.Chain;
import com.sun.corba.se.spi.orbutil.logex.Log ;
import com.sun.corba.se.spi.orbutil.logex.Message ;
import com.sun.corba.se.spi.orbutil.logex.LogLevel ;
import com.sun.corba.se.spi.orbutil.logex.ExceptionWrapper ;
import com.sun.corba.se.spi.orbutil.logex.WrapperGenerator ;

import com.sun.corba.se.spi.orbutil.logex.corba.ORBException ;
import com.sun.corba.se.spi.orbutil.logex.corba.CorbaExtension ;
import java.io.IOException;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_OBJREF;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.IORGroup )
public interface IORSystemException {
    IORSystemException self = WrapperGenerator.makeWrapper( 
        IORSystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "ObjectReferenceTemplate is not initialized" )
    INTERNAL ortNotInitialized(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Null POA" )
    INTERNAL nullPoa(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Bad magic number {0} in ObjectKeyTemplate" )
    INTERNAL badMagic( int magic ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Error while stringifying an object reference" )
    INTERNAL stringifyWriteError( @Chain IOException exc ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Could not find a TaggedProfileTemplateFactory for id {0}" )
    INTERNAL taggedProfileTemplateFactoryNotFound( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Found a JDK 1.3.1 patch level indicator with value {0} "
        + "less than JDK 1.3.1_01 value of 1" )
    INTERNAL invalidJdk131PatchLevel( int arg0 ) ;
    
    @Log( level=LogLevel.FINE, id=7 )
    @Message( "Exception occurred while looking for ObjectAdapter {0} "
        + "in IIOPProfileImpl.getServant" )
    INTERNAL getLocalServantFailure( @Chain Exception exc, 
        ObjectAdapterId oaid ) ;

    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Exception occurred while closing an IO stream object" )
    INTERNAL ioexceptionDuringStreamClose( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Adapter ID not available" )
    BAD_OPERATION adapterIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Server ID not available" )
    BAD_OPERATION serverIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "ORB ID not available" )
    BAD_OPERATION orbIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Object adapter ID not available" )
    BAD_OPERATION objectAdapterIdNotAvailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Profiles in IOR do not all have the same Object ID, " +
        "so conversion to IORTemplateList is impossible" )
    BAD_PARAM badOidInIorTemplateList(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Error in reading IIOP TaggedProfile" )
    BAD_PARAM invalidTaggedProfile(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Attempt to create IIOPAddress with port {0}, which is out of range" )
    BAD_PARAM badIiopAddressPort( int arg0 ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "IOR must have at least one IIOP profile" )
    INV_OBJREF iorMustHaveIiopProfile(  ) ;
}
