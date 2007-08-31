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
// Created       : 2001 Nov 15 (Thu) 08:45:02 by Harold Carr.
// Last Modified : 2002 Apr 24 (Wed) 15:36:23 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

// XXX Convert this to JAXP DOM.
import org.jdom.Namespace;
import org.jdom.Element;

public class EchoSerializers
    extends
	SOAPSerializers
{
    public EchoSerializers()
    {
        serializers = new LinkedList();
	serializers.add(
	  new SOAPMethodSerializer() 
	  {
	      public String getRequestName() { return "echoString"; }
	      public String getResponseName() { return "echoStringResponse"; }
	      public String getNamespace() { return "m"; }
	      public String getAttributes() 
	      { return "xmlns:m=\"http://soapinterop.org/\""; }
	      public List getParameterSerializers() 
	      { 
		  LinkedList parameterSerializers = new LinkedList();
		  parameterSerializers.add(new SOAPParameterSerializer() 
                  {
		      public String getName() { return "inputString"; }
		      public SOAPSerializer getSerializer()
		      {
			  return new SOAPSerializer() 
			  {
			      public void serialize(
                                  SOAPOutputObject soapOutputObject,
				  Serializable x) 
			      {
				  soapOutputObject.append(
							  "<inputString>" +
							  x.toString() + 
							  "</inputString>");
			      }

                              public Serializable deserialize(
                                  SOAPInputObject soapInputObject)
			      {
				  // This part could be factored.
				  Namespace requestNamespace =
				      Namespace.getNamespace(
			                 "ns3", 
					 "http://soapinterop.org/");
				  Element request =
				      soapInputObject.getBody()
				      .getChild("echoString",
						requestNamespace);
				  // This is the real work.
				  Element result =
				      request.getChild("inputString");
				  return result.getTextTrim();
			      }
			  };
		      }
		  });
		  return parameterSerializers;
	      }
	public SOAPSerializer getReturnSerializer()
	{
	    return new SOAPSerializer()
	    {
		public void serialize(SOAPOutputObject soapOutputObject,
				      Serializable x)
		{
		    soapOutputObject.append(
		        // REVISIT - this is done in SOAPMessageMediator.
		        //"<m:echoStringResponse xmlns:m=\"http://soapinterop.org/\">" +
			"<return>" + 
			x.toString() +
			"</return>");
		        //"</m:echoStringResponse>"
		}

		public Serializable deserialize(SOAPInputObject soapInputObject)
		{
		    Namespace responseNamespace =
			Namespace.getNamespace(
                            "ns3", 
			    "http://soapinterop.org/");
		    Element response = 
			soapInputObject.getBody()
			.getChild("echoStringResponse", responseNamespace);
		    Element result = response.getChild("return");
		    return result.getTextTrim();
		}
	    };
	}
	});
    }
}

// End of file.

