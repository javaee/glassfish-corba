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

package com.sun.corba.ee.spi.logging ;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;
import com.sun.corba.ee.spi.logex.corba.CS;
import com.sun.corba.ee.spi.logex.corba.CSValue;

import com.sun.corba.ee.spi.logex.corba.ORBException ;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension ;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.NamingGroup )
public interface NamingSystemException {
    NamingSystemException self = WrapperGenerator.makeWrapper( 
        NamingSystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=0 )
    @Message( "Port 0 is not a valid port in the transient name server" )
    BAD_PARAM transientNameServerBadPort(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "A null hostname is not a valid hostname in the "
        + "transient name server" )
    BAD_PARAM transientNameServerBadHost(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Object is null" )
    BAD_PARAM objectIsNull() ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Bad host address in -ORBInitDef" )
    BAD_PARAM insBadAddress(  ) ;
    
    @Log( level=LogLevel.WARNING, id=0 )
    @Message( "Updated context failed for bind" )
    UNKNOWN bindUpdateContextFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "bind failure" )
    UNKNOWN bindFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Resolve conversion failed" )
    @CS( CSValue.MAYBE )
    UNKNOWN resolveConversionFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Resolve failure" )
    @CS( CSValue.MAYBE )
    UNKNOWN resolveFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Unbind failure" )
    @CS( CSValue.MAYBE )
    UNKNOWN unbindFailure( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=50 )
    @Message( "SystemException in transient name service while initializing" )
    INITIALIZE transNsCannotCreateInitialNcSys( @Chain SystemException exc  ) ;
    
    @Log( level=LogLevel.WARNING, id=51 )
    @Message( "Java exception in transient name service while initializing" )
    INITIALIZE transNsCannotCreateInitialNc( @Chain Exception exc ) ;

    String namingCtxRebindAlreadyBound = 
        "Unexpected AlreadyBound exception in rebind" ;

    @Log( level=LogLevel.WARNING, id=0 )
    @Message( namingCtxRebindAlreadyBound )
    INTERNAL namingCtxRebindAlreadyBound( @Chain Exception exc ) ;

    @Log( level=LogLevel.WARNING, id=0 )
    @Message( namingCtxRebindAlreadyBound )
    INTERNAL namingCtxRebindAlreadyBound() ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Unexpected AlreadyBound exception in rebind_context" )
    INTERNAL namingCtxRebindctxAlreadyBound( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Bad binding type in internal binding implementation" )
    INTERNAL namingCtxBadBindingtype(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Object reference that is not CosNaming::NamingContext "
        + "bound as a context" )
    INTERNAL namingCtxResolveCannotNarrowToCtx(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Error in creating POA for BindingIterator" )
    INTERNAL namingCtxBindingIteratorCreate( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=100 )
    @Message( "Bind implementation encountered a previous bind" )
    INTERNAL transNcBindAlreadyBound(  ) ;
    
    @Log( level=LogLevel.WARNING, id=101 )
    @Message( "list operation caught an unexpected Java exception while "
        + "creating list iterator" )
    INTERNAL transNcListGotExc( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=102 )
    @Message( "new_context operation caught an unexpected Java exception "
        + "creating the NewContext servant" )
    INTERNAL transNcNewctxGotExc( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=103 )
    @Message( "Destroy operation caught a Java exception while "
        + "disconnecting from ORB" )
    INTERNAL transNcDestroyGotExc( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=105 )
    @Message( "Stringified object reference with unknown protocol specified" )
    INTERNAL insBadSchemeName(  ) ;
    
    @Log( level=LogLevel.WARNING, id=107 )
    @Message( "Malformed URL in -ORBInitDef" )
    INTERNAL insBadSchemeSpecificPart(  ) ;
    
    @Log( level=LogLevel.WARNING, id=108 )
    @Message( "Malformed URL in -ORBInitDef" )
    INTERNAL insOther(  ) ;

    @Log( level=LogLevel.WARNING, id=109 )
    @Message( "Initial port value {0} is not a valid number" )
    INTERNAL badInitialPortValue(String ips, @Chain NumberFormatException e);
}
