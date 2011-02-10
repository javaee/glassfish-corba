/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.se.impl.encoding.fast;

import com.sun.corba.se.spi.orbutil.logex.Chain;
import com.sun.corba.se.spi.orbutil.logex.ExceptionWrapper;
import com.sun.corba.se.spi.orbutil.logex.Log;
import com.sun.corba.se.spi.orbutil.logex.LogLevel;
import com.sun.corba.se.spi.orbutil.logex.Message;
import com.sun.corba.se.spi.orbutil.logex.WrapperGenerator;
import com.sun.corba.se.spi.orbutil.logex.stdcorba.StandardLogger;
import java.io.InvalidClassException;
import java.lang.reflect.Field;

/** Exception wrapper class.  The logex WrapperGenerator uses this interface
 * to generate an implementation which returns the appropriate exception, and
 * generates a log report when the method is called.  This is used for all
 * implementation classes in this package.
 *
 * The exception IDs are allocated in blocks of EXCEPTIONS_PER_CLASS, which is
 * a lot more than is needed, but we have 32 bits for IDs, and multiples of
 * a suitably chosen EXCEPTIONS_PER_CLASS (like 100 here) are easy to read in
 * error messages.
 *
 * @author ken
 */
@ExceptionWrapper( idPrefix="ORBOCOPY" )
public interface Exceptions {
    static final Exceptions self = WrapperGenerator.makeWrapper(
        Exceptions.class, StandardLogger.self ) ;

    // Allow 100 exceptions per class
    static final int EXCEPTIONS_PER_CLASS = 100 ;

// ClassAnalyzerImpl
    static final int CA_START = 1 ;

    @Message( "no serial fields in class {0}" )
    @Log( id = CA_START + 0, level = LogLevel.FINE )
    void noSerialFields( Class<?> cl, @Chain InvalidClassException e ) ;

    @Message( "no serialPersistentFields in class {0}" )
    @Log( id = CA_START + 1, level = LogLevel.FINE )
    void noSerialPersistentFields( Class<?> cl, @Chain Exception ex ) ;

    @Message( "field {0} not found in class {0}" )
    @Log( id = CA_START + 2, level = LogLevel.FINE )
    void fieldNotFound(Field f, Class<?> cls,
        @Chain NoSuchFieldException ex);

    @Message( "No suitable constructor available in Externalizable class {0}" )
    @Log( id = CA_START + 3, level = LogLevel.FINE )
    void noExternalizableConstructor( Class<?> cl, Exception ex ) ;

// ClassMarshaller
    static final int CM_START = CA_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Bad writeObject call for object {0}" )
    @Log( id = CM_START + 0, level = LogLevel.WARNING )
    public IllegalArgumentException badWriteObjectCall(Object obj);
}
