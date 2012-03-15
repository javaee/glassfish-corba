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

import com.sun.corba.ee.spi.logex.corba.ORBException ;
import com.sun.corba.ee.spi.logex.corba.CorbaExtension ;

import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.OBJECT_NOT_EXIST;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.ActivationGroup )
public interface ActivationSystemException {
    ActivationSystemException self = WrapperGenerator.makeWrapper( 
        ActivationSystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Cannot read repository datastore" )
    INITIALIZE cannotReadRepositoryDb( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Cannot add initial naming" )
    INITIALIZE cannotAddInitialNaming(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Cannot write repository datastore" )
    INTERNAL cannotWriteRepositoryDb( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Server not expected to register" )
    INTERNAL serverNotExpectedToRegister(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Unable to start server process" )
    INTERNAL unableToStartProcess(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Server is not running" )
    INTERNAL serverNotRunning(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Error in BadServerIdHandler" )
    OBJECT_NOT_EXIST errorInBadServerIdHandler( @Chain Exception exc  ) ;
}
