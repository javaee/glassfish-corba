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

package com.sun.corba.ee.impl.presentation.rmi;

import com.sun.corba.ee.spi.logex.stdcorba.StandardLogger;
import javax.naming.Context;
import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

/**
 *
 * @author ken
 */
@ExceptionWrapper( idPrefix="ORBPRES" )
public interface Exceptions {

    Exceptions self = WrapperGenerator.makeWrapper( Exceptions.class,
        StandardLogger.self ) ;

    int EXCEPTIONS_PER_CLASS = 100 ;

// JNDISateFactoryImpl
    int JSFI_START = 0 ;

    @Message( "No stub could be created" )
    @Log( level=LogLevel.FINE, id = JSFI_START + 1 )
    void noStub(@Chain Exception exc);

    @Message( "Could not connect stub" )
    @Log( level=LogLevel.FINE, id = JSFI_START + 2 )
    void couldNotConnect(@Chain Exception exc);

    @Message( "Could not get ORB from naming context" )
    @Log( level=LogLevel.FINE, id = JSFI_START + 2 )
    void couldNotGetORB(@Chain Exception exc, Context nc );

// DynamicStubImpl
    int DSI_START = JSFI_START + EXCEPTIONS_PER_CLASS ;

    @Message( "ClassNotFound exception in readResolve on class {0}")
    @Log( level=LogLevel.FINE, id = DSI_START + 1 )
    void readResolveClassNotFound(@Chain ClassNotFoundException exc, String cname);
}
