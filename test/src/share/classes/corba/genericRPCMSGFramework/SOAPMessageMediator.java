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
// Created       : 2001 Nov 29 (Thu) 02:03:18 by Harold Carr.
// Last Modified : 2003 Apr 03 (Thu) 21:36:16 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.util.Iterator;

import java.io.IOException;

import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.Selector;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ContactInfo;

import com.sun.corba.se.spi.orb.ORB;

/**
 * @author Harold Carr
 */
public class SOAPMessageMediator 
    implements
	MessageMediator,
	ResponseHandler
{
    private Broker broker;
    // private ORB corbaBroker; // REVISIT debug
    private SOAPOutputObject outputObject;
    private SOAPConnection connection;
    private String methodName;
    private boolean isOneWay;
    private SOAPContactInfo soapContactInfo;
    private SOAPMethodSerializer soapMethodSerializer;
    private Iterator parameterSerializers;
    private String lf = "\n";

    // Server;
    private String uri;
    private SOAPInputObject inputObject;
    private boolean isServer;

    private SOAPMessageMediator(Broker broker, boolean isServer,
				Connection connection)
    {
	this.broker = broker;
	// this.corbaBroker = (CorbaBroker) broker;
	this.isServer = isServer;
	this.connection = (SOAPConnection) connection;
    }

    public SOAPMessageMediator(Broker broker, Connection connection,
			     String methodName, boolean isOneWay,
			     SOAPContactInfo soapContactInfo)
    {
	this(broker, false, connection);
	this.methodName = methodName;
	this.isOneWay = isOneWay;
	this.soapContactInfo = soapContactInfo;
	this.soapMethodSerializer = 
	    soapContactInfo.getSerializers().getSerializer(methodName);
	this.parameterSerializers = 
	    soapMethodSerializer.getParameterSerializers().iterator();
    }

    public SOAPMessageMediator(Broker broker, Connection connection, String uri)
    {
	this(broker, true, connection);
	this.uri = uri;
    }

    ////////////////////////////////////////////////////
    //
    // MessageMediator
    //

    public boolean handleInput()
	throws
	    IOException
    {
	throw new RuntimeException("SOAPMessageMediator.handleInput");
    }

    ////////////////////////////////////////////////////
    //
    // SOAP Specific
    //

    public boolean handleInput(InputObject inputObject)
    {
	this.inputObject = (SOAPInputObject) inputObject;
	// corbaBroker.dprint(CorbaConstants.PROTOCOL, "SOAPMessageMediator.handleInput");
	// REVISIT:
	// Here we would use the URI to lookup the serializers,
	// tie and servant for the service.
	// We just hardwire echo for now.
	soapMethodSerializer =
	    new EchoSerializers().getSerializer(this.inputObject.getBody());
	this.parameterSerializers =
	    soapMethodSerializer.getParameterSerializers().iterator();
	BasePortServant basePortServant = null;
	try {
	    basePortServant = new BasePortServant();
	} catch (java.rmi.RemoteException e) {
	    System.out.println("SOAPMessageMediator.handleInput: " + e);
	}
	_BasePortServant_Tie basePortServantTie = new _BasePortServant_Tie();
	basePortServantTie.setTarget(basePortServant);
	outputObject = (SOAPOutputObject)
	    basePortServantTie._invoke(
                "echoString", 
		(org.omg.CORBA.portable.InputStream)inputObject, 
		this);
	endSoapBody();
	putBodyInEnvelope();
	addHttpResponseHeader();
	connection.sendWithoutLock(outputObject);
	Selector selector = broker.getTransportManager().getSelector(0);
	selector.unregisterForEvent(connection);
	try {
	    connection.getSocket().close();
	    /*
	    connection.getSocket().getInputStream().close();
	    connection.getSocket().getOutputStream().close();
	    */
	} catch (Exception e) {
	    System.out.println("SOAPMessageMediator.handleInput: " + e);
	}
	// NOTE: We could do leader/follower here. Put not for test.
	return false;
    }

    public SOAPParameterSerializer getParameterSerializer()
    {
	return (SOAPParameterSerializer) parameterSerializers.next();
    }

    public SOAPMethodSerializer getSOAPMethodSerializer()
    {
	return soapMethodSerializer;
    }

    private void endSoapBody()
    {
	String namespace = soapMethodSerializer.getNamespace();
	String name = isServer ? soapMethodSerializer.getResponseName() :
	                         soapMethodSerializer.getRequestName();
	outputObject.append(
	    "</" +
	    ((namespace != null) ? (namespace + ":") :  "") +
	    name +
	    ">" +
	    "</SOAP-ENV:Body>");
    }

    private void putBodyInEnvelope()
    {
	outputObject.prepend(
	    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	    "<SOAP-ENV:Envelope " +
	    "SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" " +
	    "xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" " +
	    "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
	    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
	    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");

	outputObject.append("</SOAP-ENV:Envelope>");
    }

    private void addHttpRequestHeader()
    {
	outputObject.prepend(
            "POST " + soapContactInfo.getURI() + " HTTP/1.0" + lf +
	    "Host: www.openhc.org" + lf +
	    "User-Agent: openhc SOAP Client/0.1" + lf +
	    //"Content-Type: text/xml; charset=\"utf-8\"" lf
	    "Content-Type: text/xml; charset=\"us-ascii\"" + lf +
	    "Content-Length: " + outputObject.toString().length() + lf +
	    "SOAPAction: \"http://soapinterop.org/\"" + lf + lf);
    }

    private void addHttpResponseHeader()
    {
	outputObject.prepend(
	    "HTTP/1.1 200" + lf +
	    "Server: genericRPCMessagingFramework/0.1" + lf +
	    "Content-length: " + outputObject.toString().length() + lf +
	    "HTTP-Version: HTTP/1.0" + lf +
	    "Content-type: text/xml" + lf + lf);
    }

    public java.io.Serializable read_value()
    {
	if (isServer) {
	    SOAPParameterSerializer soapParameterSerializer = 
		getParameterSerializer();
	    return 
		soapParameterSerializer.getSerializer().deserialize(inputObject);
	}
	SOAPSerializer soapSerializer =
	    soapMethodSerializer.getReturnSerializer();
	return soapSerializer.deserialize(inputObject);
    }

    public void write_value(java.io.Serializable value)
    {
	if (isServer) {
	    SOAPSerializer soapSerializer =
		soapMethodSerializer.getReturnSerializer();
	    soapSerializer.serialize(outputObject, value);
	    return;
	}
	SOAPParameterSerializer soapParameterSerializer = 
	    getParameterSerializer();
	soapParameterSerializer.getSerializer().serialize(outputObject, value);
    }

    ////////////////////////////////////////////////////
    //
    // MessageMediator methods.
    //

    public Broker getBroker()
    {
	return broker;
    }

    public ContactInfo  getContactInfo()
    {
	return null;
    }

    public Connection getConnection()
    {
	return connection;
    }

    public void initializeMessage()
    {
	String namespace = soapMethodSerializer.getNamespace();
	String name = isServer ? soapMethodSerializer.getResponseName() :
                                 soapMethodSerializer.getRequestName();
	String attributes = soapMethodSerializer.getAttributes();
	outputObject.prepend(
	    "<SOAP-ENV:Body>" +
	    "<" +
	    ((namespace != null) ? (namespace + ":") : "") +
	    name +
	    " " +
	    attributes +
	    ">");
    }

    public void finishSendingRequest()
    {
	endSoapBody();
	putBodyInEnvelope();
	addHttpRequestHeader();
	connection.sendWithoutLock(outputObject);
    }

    public InputObject waitForResponse()
    {
	inputObject = (SOAPInputObject) connection.waitForResponse(this);
	inputObject.setMessageMediator(this);
	return inputObject;
    }

    public void setOutputObject(OutputObject outputObject)
    {
	this.outputObject = (SOAPOutputObject) outputObject;
    }

    public OutputObject getOutputObject()
    {
	return outputObject;
    }

    public void setInputObject(InputObject inputObject)
    {
    }

    public InputObject getInputObject()
    {
	return null;
    }

    ////////////////////////////////////////////////////
    //
    // ResponseHandler methods.
    //

    public OutputStream createReply()
    {
	SOAPOutputObject soapOutputObject = new SOAPOutputObject(broker, this);
	initializeMessage();
	return soapOutputObject;
    }

    public OutputStream createExceptionReply()
    {
	return null;
    }
}


// End of file.
