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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.HashMap;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.trace.StreamFormatVersion;

@StreamFormatVersion
public abstract class OutputStreamHook extends ObjectOutputStream
{
    private HookPutFields putFields = null;
    
    /**
     * Since ObjectOutputStream.PutField methods specify no exceptions,
     * we are not checking for null parameters on put methods.
     */
    private class HookPutFields extends ObjectOutputStream.PutField {
	private Map<String,Object> fields = new HashMap<String,Object>();

	/**
	 * Put the value of the named boolean field into the persistent field.
	 */
	public void put(String name, boolean value){
	    fields.put(name, Boolean.valueOf(value));
	}
		
	/**
	 * Put the value of the named char field into the persistent fields.
	 */
	public void put(String name, char value){
	    fields.put(name, Character.valueOf(value));
	}
		
	/**
	 * Put the value of the named byte field into the persistent fields.
	 */
	public void put(String name, byte value){
	    fields.put(name, Byte.valueOf(value));
	}
		
	/**
	 * Put the value of the named short field into the persistent fields.
	 */
	public void put(String name, short value){
	    fields.put(name, Short.valueOf(value));
	}
		
	/**
	 * Put the value of the named int field into the persistent fields.
	 */
	public void put(String name, int value){
	    fields.put(name, Integer.valueOf(value));
	}
		
	/**
	 * Put the value of the named long field into the persistent fields.
	 */
	public void put(String name, long value){
	    fields.put(name, Long.valueOf(value));
	}
		
	/**
	 * Put the value of the named float field into the persistent fields.
	 *
	 */
	public void put(String name, float value){
	    fields.put(name, Float.valueOf(value));
	}
		
	/**
	 * Put the value of the named double field into the persistent field.
	 */
	public void put(String name, double value){
	    fields.put(name, Double.valueOf(value));
	}
		
	/**
	 * Put the value of the named Object field into the persistent field.
	 */
	public void put(String name, Object value){
	    fields.put(name, value);
	}
		
	/**
	 * Write the data and fields to the specified ObjectOutput stream.
	 */
	public void write(ObjectOutput out) throws IOException {
            OutputStreamHook hook = (OutputStreamHook)out;

            ObjectStreamField[] osfields = hook.getFieldsNoCopy();

            // Write the fields to the stream in the order
            // provided by the ObjectStreamClass.  (They should
            // be sorted appropriately already.)
            for (int i = 0; i < osfields.length; i++) {

                Object value = fields.get(osfields[i].getName());

                hook.writeField(osfields[i], value);
            }
	}
    }

    abstract void writeField(ObjectStreamField field, Object value) throws IOException;

    public OutputStreamHook()
	throws java.io.IOException {
	super();
		
    }

    @StreamFormatVersion
    @Override
    public void defaultWriteObject() throws IOException {
        writeObjectState.defaultWriteObject(this);

        defaultWriteObjectDelegate();
    }

    public abstract void defaultWriteObjectDelegate();
	
    @Override
    public ObjectOutputStream.PutField putFields()
	throws IOException {
	putFields = new HookPutFields();
	return putFields;
    }

    // Stream format version, saved/restored during recursive calls
    protected byte streamFormatVersion = 1;

    // Return the stream format version currently being used
    // to serialize an object
    public byte getStreamFormatVersion() {
        return streamFormatVersion;
    }

    abstract ObjectStreamField[] getFieldsNoCopy();

    // User uses PutFields to simulate default data.
    // See java.io.ObjectOutputStream.PutFields
    @Override
    @StreamFormatVersion
    public void writeFields()
	throws IOException {

        writeObjectState.defaultWriteObject(this);

        putFields.write(this);
    }

    public abstract org.omg.CORBA_2_3.portable.OutputStream getOrbStream();

    protected abstract void beginOptionalCustomData();


    // The following is a State pattern implementation of what
    // should be done when a Serializable has a 
    // writeObject method.  This was especially necessary for
    // RMI-IIOP stream format version 2.  Please see the
    // state diagrams in the docs directory of the workspace.

    protected WriteObjectState writeObjectState = NOT_IN_WRITE_OBJECT;
    
    @StreamFormatVersion
    protected void setState(WriteObjectState newState) {
        writeObjectState = newState;
    }

    protected static final WriteObjectState NOT_IN_WRITE_OBJECT = new DefaultState();
    protected static final WriteObjectState IN_WRITE_OBJECT = new InWriteObjectState();
    protected static final WriteObjectState WROTE_DEFAULT_DATA = new WroteDefaultDataState();
    protected static final WriteObjectState WROTE_CUSTOM_DATA = new WroteCustomDataState();

    // Description of possible actions
    @StreamFormatVersion
    protected static class WriteObjectState {
        private final String name ;

        public WriteObjectState() {
            String className = this.getClass().getName() ;
            int index = className.indexOf( '$' ) ;
            name = className.substring( index + 1 ) ;
        }

        @StreamFormatVersion
        public final void enterWriteObject(OutputStreamHook stream) throws IOException {
            enterWriteObjectOverride( stream ) ;
        }

        @StreamFormatVersion
        public final void exitWriteObject(OutputStreamHook stream) throws IOException {
            exitWriteObjectOverride( stream ) ;
        }

        @StreamFormatVersion
        public final void defaultWriteObject(OutputStreamHook stream) throws IOException {
            defaultWriteObjectOverride( stream ) ;
        }

        @StreamFormatVersion
        public final void writeData(OutputStreamHook stream) throws IOException {
            writeDataOverride( stream ) ;
        }

        public void enterWriteObjectOverride(OutputStreamHook stream) throws IOException {}
        public void exitWriteObjectOverride(OutputStreamHook stream) throws IOException {}
        public void defaultWriteObjectOverride(OutputStreamHook stream) throws IOException {}
        public void writeDataOverride(OutputStreamHook stream) throws IOException {}

        @Override
        public String toString() {
            return name ;
        }
    }

    @StreamFormatVersion
    protected static class DefaultState extends WriteObjectState {
        @Override
        @StreamFormatVersion
        public void enterWriteObjectOverride(OutputStreamHook stream) throws IOException {
            stream.setState(IN_WRITE_OBJECT);
        }
    }

    @StreamFormatVersion
    protected static class InWriteObjectState extends WriteObjectState {

        @Override
        @StreamFormatVersion
        public void enterWriteObjectOverride(OutputStreamHook stream) throws IOException {
	    // XXX I18N, logging needed.
            throw new IOException("Internal state failure: Entered writeObject twice");
        }
        
        @Override
        @StreamFormatVersion
        public void exitWriteObjectOverride(OutputStreamHook stream) throws IOException {

            // We didn't write any data, so write the
            // called defaultWriteObject indicator as false
            stream.getOrbStream().write_boolean(false);

            // If we're in stream format verison 2, we must
            // put the "null" marker to say that there isn't
            // any optional data
            if (stream.getStreamFormatVersion() == 2)
                stream.getOrbStream().write_long(0);

            stream.setState(NOT_IN_WRITE_OBJECT);
        }

        @Override
        @StreamFormatVersion
        public void defaultWriteObjectOverride(OutputStreamHook stream) throws IOException {

            // The writeObject method called defaultWriteObject
            // or writeFields, so put the called defaultWriteObject
            // indicator as true
            stream.getOrbStream().write_boolean(true);

            stream.setState(WROTE_DEFAULT_DATA);
        }

        @Override
        @StreamFormatVersion
        public void writeDataOverride(OutputStreamHook stream) throws IOException {

            // The writeObject method first called a direct
            // write operation.  Write the called defaultWriteObject
            // indicator as false, put the special stream format
            // version 2 header (if stream format version 2, of course),
            // and write the data
            stream.getOrbStream().write_boolean(false);
            stream.beginOptionalCustomData();
            stream.setState(WROTE_CUSTOM_DATA);
        }
    }

    @StreamFormatVersion
    protected static class WroteDefaultDataState extends InWriteObjectState {
        @Override
        @StreamFormatVersion
        public void exitWriteObjectOverride(OutputStreamHook stream) throws IOException {

            // We only wrote default data, so if in stream format
            // version 2, put the null indicator to say that there
            // is no optional data
            if (stream.getStreamFormatVersion() == 2)
                stream.getOrbStream().write_long(0);
            
            stream.setState(NOT_IN_WRITE_OBJECT);
        }

        @Override
        @StreamFormatVersion
        public void defaultWriteObjectOverride(OutputStreamHook stream) throws IOException {
	    // XXX I18N, logging needed.
            throw new IOException("Called defaultWriteObject/writeFields twice");
        }

        @Override
        @StreamFormatVersion
        public void writeDataOverride(OutputStreamHook stream) throws IOException {

            // The writeObject method called a direct write operation.
            // If in stream format version 2, put the fake valuetype
            // header.
            stream.beginOptionalCustomData();
            
            stream.setState(WROTE_CUSTOM_DATA);
        }
    }

    @StreamFormatVersion
    protected static class WroteCustomDataState extends InWriteObjectState {
        @Override
        @StreamFormatVersion
        public void exitWriteObjectOverride(OutputStreamHook stream) throws IOException {
            // In stream format version 2, we must tell the ORB
            // stream to close the fake custom valuetype.
            if (stream.getStreamFormatVersion() == 2)
                ((org.omg.CORBA.portable.ValueOutputStream)stream.getOrbStream()).end_value();

            stream.setState(NOT_IN_WRITE_OBJECT);
        }

        @Override
        @StreamFormatVersion
        public void defaultWriteObjectOverride(OutputStreamHook stream) 
	    throws IOException {
	    
	    // XXX I18N, logging needed.
            throw new IOException(
		"Cannot call defaultWriteObject/writeFields after"
		+ " writing custom data in RMI-IIOP");
        }

        // We don't have to do anything special here, just let
        // the stream write the data.
        @Override
        public void writeDataOverride(OutputStreamHook stream) throws IOException {}
    }
}
