/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2001 Nov 29 (Thu) 02:19:41 by Harold Carr.
// Last Modified : 2003 Feb 10 (Mon) 16:34:24 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.MessageMediator;
import org.omg.CORBA.Any;
import org.omg.CORBA.Principal;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA.ORB;

/**
 * @author Harold Carr
 */
public class SOAPOutputObject extends OutputStream implements OutputObject
{
    private Broker broker;
    private SOAPMessageMediator soapMessageMediator;
    private StringBuffer buffer; // REVISIT - find non-sync version.
    
    public SOAPOutputObject(Broker broker, MessageMediator messageMediator) 
    {
	this.broker = broker;
	this.soapMessageMediator = (SOAPMessageMediator) messageMediator;
	this.soapMessageMediator.setOutputObject(this);
	this.buffer = new StringBuffer();
    }

    public void setMessageMediator(MessageMediator messageMediator)
    {
	this.soapMessageMediator = (SOAPMessageMediator) messageMediator;
    }

    public MessageMediator getMessageMediator() 
    {
	return soapMessageMediator;
    }

    public void prepend(String x)
    {
	buffer.insert(0, x);
    }

    public void append(String x)
    {
	buffer.append(x);
    }

    public String toString()
    {
	return buffer.toString();
    }

    //
    // CORBA ops.
    //

    public InputStream create_input_stream()
    {
	return null;
    }
    
    public void write_boolean(boolean value) 
    {
    }
    public void write_char(char value) 
    {
    }
    public void write_wchar(char value) 
    {
    }
    public void write_octet(byte value) 
    {
    }
    public void write_short(short value) 
    {
    }
    public void write_ushort(short value) 
    {
    }
    public void write_long(int value) 
    {
    }
    public void write_ulong(int value) 
    {
    }
    public void write_longlong(long value) 
    {
    }
    public void write_ulonglong(long value) 
    {
    }
    public void write_float(float value) 
    {
    }
    public void write_double(double value) 
    {
    }
    public void write_string(String value) 
    {
    }
    public void write_wstring(String value) 
    {
    }
    public void write_boolean_array(boolean[] value, int offset, int length) 
    {
    }
    public void write_char_array(char[] value, int offset, int length) 
    {
    }
    public void write_wchar_array(char[] value, int offset, int length) 
    {
    }
    public void write_octet_array(byte[] value, int offset, int length) 
    {
    }
    public void write_short_array(short[] value, int offset, int length) 
    {
    }
    public void write_ushort_array(short[] value, int offset, int length) 
    {
    }
    public void write_long_array(int[] value, int offset, int length) 
    {
    }
    public void write_ulong_array(int[] value, int offset, int length) 
    {
    }
    public void write_longlong_array(long[] value, int offset, int length) 
    {
    }
    public void write_ulonglong_array(long[] value, int offset, int length) 
    {
    }
    public void write_float_array(float[] value, int offset, int length) 
    {
    }
    public void write_double_array(double[] value, int offset, int length) 
    {
    }
    public void write_Object(org.omg.CORBA.Object value) 
    {
    }
    public void write_TypeCode(TypeCode value) 
    {
    }
    public void write_any(Any value) 
    {
    }
    public void write_Principal(Principal value) 
    {
    }
    public void write(int b) throws java.io.IOException 
    {
    }
    public void write_fixed(java.math.BigDecimal value) 
    {
    }
    public void write_Context(org.omg.CORBA.Context ctx, org.omg.CORBA.ContextList contexts) 
    {
    }
    public ORB orb() 
    {
	return (ORB)broker ;
    }

    public void write_value(java.io.Serializable value) 
    {
    }

    public void write_value(java.io.Serializable value, java.lang.Class clz) 
    {
	soapMessageMediator.write_value(value);
    }
}

// End of file.
