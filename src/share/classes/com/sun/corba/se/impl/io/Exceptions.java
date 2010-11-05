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

package com.sun.corba.se.impl.io;

import com.sun.corba.se.spi.orbutil.logex.Chain;
import com.sun.corba.se.spi.orbutil.logex.ExceptionWrapper;
import com.sun.corba.se.spi.orbutil.logex.Log;
import com.sun.corba.se.spi.orbutil.logex.LogLevel;
import com.sun.corba.se.spi.orbutil.logex.Message;
import com.sun.corba.se.spi.orbutil.logex.WrapperGenerator;
import com.sun.corba.se.spi.orbutil.logex.stdcorba.StandardLogger;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

/** Logging and Exception handling for the io package.
 *
 * @author ken
 */
@ExceptionWrapper( idPrefix="ORBIO" )
public interface Exceptions {
    public Exceptions self = WrapperGenerator.makeWrapper( Exceptions.class,
        StandardLogger.self );

    // Allow 100 exceptions per class
    static final int EXCEPTIONS_PER_CLASS = 100 ;

// IIOPInputStream
    static final int IIS_START = 1 ;

    @Message( "No optional data exception constructor available" )
    @Log( id = IIS_START + 0 )
    ExceptionInInitializerError noOptionalDataExceptionConstructor(
        @Chain Exception exc );

    @Message( "Can't create optional data exception")
    @Log( id = IIS_START + 1 )
    Error cantCreateOptionalDataException(@Chain Exception ex);

    @Message( "readLine method not supported")
    @Log( id = IIS_START + 2 )
    IOException readLineMethodNotSupported();

    @Message( "registerValidation method not supported")
    @Log( id = IIS_START + 3 )
    Error registerValidationNotSupport();

    @Message( "resolveClass method not supported")
    @Log( id = IIS_START + 4 )
    IOException resolveClassNotSupported();

    @Message( "resolveObject method not supported")
    @Log( id = IIS_START + 5 )
    IOException resolveObjectNotSupported();

    @Message( "IllegalAccessException when invoking readObject")
    @Log( id = IIS_START + 6 )
    void illegalAccessInvokingObjectRead(@Chain IllegalAccessException e);

    @Message( "Bad type {0} for primitive field")
    @Log( id = IIS_START + 7 )
    InvalidClassException invalidClassForPrimitive(String name);

    @Message( "Unknown call type {0} while reading object field: "
        + "possible stream corruption")
    @Log( id = IIS_START + 8 )
    StreamCorruptedException unknownCallType(int callType);

    @Message( "Unknown typecode kind {0} while reading object field: "
        + "possible stream corruption")
    @Log( id = IIS_START + 9 )
    StreamCorruptedException unknownTypecodeKind(int value);

    @Message( "Assigning instance of class {0} to field {1}" )
    @Log( id = IIS_START + 10 )
    ClassCastException couldNotAssignObjectToField(
        @Chain IllegalArgumentException exc, String className,
        String fieldName );

    @Message( "Not setting field {0} on class {1}: "
        + "likely that class has evolved")
    @Log( level=LogLevel.FINE, id = IIS_START + 11 )
    void notSettingField( String fieldName, String className );

    @Message( "Stream corrupted" )
    @Log( id = IIS_START + 12 )
    StreamCorruptedException streamCorrupted(Throwable t);

    @Log( id= IIS_START + 13 ) 
    @Message( "Could not unmarshal enum with cls {0}, value {1} using EnumDesc" )
    IOException couldNotUnmarshalEnum( String cls, String value ) ;

// IIOPOutputStream
    int IOS_START = IIS_START + EXCEPTIONS_PER_CLASS ;

    @Message( "method annotateClass not supported" )
    @Log( id = IOS_START + 1 )
    IOException annotateClassNotSupported();

    @Message( "method replaceObject not supported" )
    @Log( id = IOS_START + 2 )
    IOException replaceObjectNotSupported();

    @Message( "serialization of ObjectStreamClass not supported" )
    @Log( id = IOS_START + 3 )
    IOException serializationObjectStreamClassNotSupported();

    @Message( "serialization of ObjectStreamClass not supported" )
    @Log( id = IOS_START + 4 )
    NotSerializableException notSerializable(String name);

    @Message( "Invalid class {0} for writing field" )
    @Log( id = IOS_START + 5 )
    InvalidClassException invalidClassForWrite(String name);

// InputStreamHook
    int ISH_START = IOS_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Default data already read" )
    @Log( id = ISH_START + 1 )
    StreamCorruptedException defaultDataAlreadyRead();

    @Message( "Default data must be read first" )
    @Log( id = ISH_START + 2 )
    StreamCorruptedException defaultDataMustBeReadFirst();

    @Message( "Default data not sent or already read" )
    @Log( id = ISH_START + 3 )
    StreamCorruptedException defaultDataNotPresent();

// ObjectStreamClass
    int OSC_START = ISH_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Default data not sent or already read" )
    @Log( level=LogLevel.FINE, id = OSC_START + 1 )
    void couldNotAccessSerialPersistentFields( @Chain Exception e,
        String name);

    @Message( "Field type mismatch in Class {0} for field (name {1}, type {2})"
        + "and reflected field (name {3}, type {4})")
    @Log( level=LogLevel.FINE, id = OSC_START + 2 )
    void fieldTypeMismatch( String cname, String fldName,
        Class<?> fldType, String rfldName, Class<?> rfldType ) ;

    @Message( "Could not find field {1} in class {0}" )
    @Log( level=LogLevel.FINE, id = OSC_START + 3 )
    void noSuchField( @Chain NoSuchFieldException e, String className,
        String fieldName );

    @Message( "Could not hasStaticInitializer method in class {0}" )
    @Log( id = OSC_START + 4 )
    InternalError cantFindHasStaticInitializer(String cname);

    @Message( "Could not invoke hasStaticInitializer method" )
    @Log( id = OSC_START + 5 )
    InternalError errorInvokingHasStaticInitializer(@Chain Exception ex);

// OutputStreamHook
    int OSH_START = OSC_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Call writeObject twice" )
    @Log( id = OSH_START + 1 )
    IOException calledWriteObjectTwice();

    @Message( "Call defaultWriteObject or writeFields twice" )
    @Log( id = OSH_START + 2 )
    IOException calledDefaultWriteObjectTwice();

    @Message( "Cannot call defaultWriteObject or writeFields after "
        + "writing custom data")
    @Log( id = OSH_START + 3 )
    IOException defaultWriteObjectAfterCustomData();

// ValueHandleImpl
    int VHI_START = OSH_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Invalid primitive type {0}")
    @Log( id = VHI_START + 1 )
    Error invalidPrimitiveType(String name);

    @Message( "Invalid primitive component type {0}")
    @Log( id = VHI_START + 2 )
    Error invalidPrimitiveComponentType(String name);
}
