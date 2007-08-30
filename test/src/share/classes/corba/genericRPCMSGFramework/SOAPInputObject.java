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
// Last Modified : 2003 Feb 10 (Mon) 16:34:06 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import org.omg.CORBA.Any;
import org.omg.CORBA.Principal;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.SystemException;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.encoding.InputObject;

import com.sun.corba.se.spi.orb.ORB;

import java.io.ByteArrayInputStream;

import org.jdom.input.DOMBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

public class SOAPInputObject
    extends
	org.omg.CORBA_2_3.portable.InputStream
    implements
	InputObject
{
    private ORB corbaBroker;
    private String envelopeString;
    private SOAPMessageMediator messageMediator;
    private Element body;

    public SOAPInputObject(Broker broker, String envelopeString)
    {
	this.corbaBroker = (ORB) broker;
	this.envelopeString = envelopeString;

	DOMBuilder builder = 
           new DOMBuilder("org.jdom.adapters.XercesDOMAdapter");
	Document doc = null;
	try {
	    doc = builder.build(new ByteArrayInputStream(envelopeString.getBytes()));
	} catch (JDOMException e) {
	    System.out.println("SOAPInputObject: " + e);
	}
	Element root = doc.getRootElement();
	Namespace soapEnvNamespace =
	    Namespace.getNamespace(
	        "SOAP-ENV",
		"http://schemas.xmlsoap.org/soap/envelope/");
	body = root.getChild("Body", soapEnvNamespace);
    }

    public Element getBody()
    {
	return body;
    }

    //
    // InputObject methods.
    //

    public void setMessageMediator(MessageMediator messageMediator)
    {
	this.messageMediator = (SOAPMessageMediator) messageMediator;
    }

    public MessageMediator getMessageMediator()
    {
	return messageMediator;
    }

    //
    // ValueBase operations.
    //

    public String[] _truncatable_ids()
    {
	return null;
    }

    //
    // DataInputStream operations.
    //

    public final java.lang.Object read_Abstract () 
    {
	return null;
    }

    public java.io.Serializable read_Value ()
    {
	return null;
    }

    public void read_any_array (org.omg.CORBA.AnySeqHolder seq, int offset, int length)
    {
    }

    public void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq, int offset, int length)
    {
    }


    public void read_char_array (org.omg.CORBA.CharSeqHolder seq, int offset, int length)
    {
    }


    public void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq, int offset, int length)
    {
    }


    public void read_octet_array (org.omg.CORBA.OctetSeqHolder seq, int offset, int length)
    {
    }


    public void read_short_array (org.omg.CORBA.ShortSeqHolder seq, int offset, int length)
    {
    }


    public void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq, int offset, int length)
    {
    }


    public void read_long_array (org.omg.CORBA.LongSeqHolder seq, int offset, int length)
    {
    }


    public void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq, int offset, int length)
    {
    }


    public void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length)
    {
    }


    public void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq, int offset, int length)
    {
    }


    public void read_float_array (org.omg.CORBA.FloatSeqHolder seq, int offset, int length)
    {
    }


    public void read_double_array (org.omg.CORBA.DoubleSeqHolder seq, int offset, int length)
    {
    }
    

    //
    // InputStream operations
    //

    public boolean	read_boolean()
    { 
	return false;
    }
    public char	read_char()
    { 
	return 'c';
    }
    public char	read_wchar()
    { 
	return 'c';
    }
    public byte	read_octet()
    { 
	return (byte)8;
    }
    public short	read_short()
    { 
	return (short)8;
    }

    public short	read_ushort()
    { 
	return 3;
    }

    public int		read_long()
    { 
	return 8;
    }

    public int		read_ulong()
    { 
	return read_long();
    }

    public long	read_longlong()
    { 
	return 3;
    }

    public long	read_ulonglong()
    { 
	return 3;
    }

    public float	read_float()
    { 
	return (float)3.0;
    }

    public double	read_double()
    { 
	return 3.0;
    }

    public String	read_string()
    { 
	return null;
    }

    public String	read_wstring()
    { 
	return null;
    }


    public void	read_boolean_array(boolean[] value, int offset, int length)
    { 
	return;
    }

    public void	read_char_array(char[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_wchar_array(char[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_octet_array(byte[] value, int offset, int length)
    { 
    }

    public void	read_short_array(short[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_ushort_array(short[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_long_array(int[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_ulong_array(int[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_longlong_array(long[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_ulonglong_array(long[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_float_array(float[] value, int offset, int length)
    { 
	return ;
    }

    public void	read_double_array(double[] value, int offset, int length)
    { 
	return ;
    }


    public org.omg.CORBA.Object read_Object()
    { 
	return null;
    }

    public TypeCode	read_TypeCode()
    { 
	return null;
    }

    public     Any		read_any()
    { 
	return null;
    }


    public  Principal	read_Principal()
    { 
	return null;
    }

    public int read() throws java.io.IOException 
    {
	return 1;
    }

    public java.math.BigDecimal read_fixed() {
	return  null;
    }


    public org.omg.CORBA.Context read_Context()
    { 
	return null;
    }



    public  org.omg.CORBA.Object read_Object(java.lang.Class clz) 
    {
	return null;
    }

    public java.io.Serializable read_value(java.lang.Class clz) 
    { 
	return messageMediator.read_value();
    }


    public org.omg.CORBA.ORB orb()
    { 
	return corbaBroker;
    }
}

// End of file.
