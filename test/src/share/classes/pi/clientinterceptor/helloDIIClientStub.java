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

package pi.clientinterceptor;

import org.omg.CORBA.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Utility class to encapsulate the logic to make DII calls on the 
 * hello interface.
 */
public class helloDIIClientStub {

  /** The CORBA object to make DII calls on */
  private org.omg.CORBA.Object object;

  /** The ORB to use to create objects */
  private ORB orb;

  /**
   * Creates a new helloDIIClientStub which will make DII calls on the given
   * corba object, and will create objects using the given ORB.
   */
  public helloDIIClientStub( ORB orb, org.omg.CORBA.Object object ) {
      this.object = object;
      this.orb = orb;
  }

  public org.omg.CORBA.Object getObject() {
      return object;
  }

  String sayHello() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_string( "dummy" );
      NamedValue resultVal = orb.create_named_value( "result", result, 
	  org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "sayHello", 
	  argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_string();
  }

  String saySystemException() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_string( "dummy" );
      NamedValue resultVal = orb.create_named_value( "result", result, 
	  org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "saySystemException", 
	  argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_string();
  }

  void sayOneway() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method as a oneway:
      Request thisReq = object._create_request( null, "sayOneway", 
	  argList, null );
      thisReq.send_oneway();
  }

  boolean _is_a( String repository_id ) {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      Any a1 = orb.create_any();
      a1.insert_string( repository_id );
      argList.add_value( "repository_id", a1, ARG_IN.value );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_boolean( true );
      NamedValue resultVal = orb.create_named_value( "result", result, 
	  org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "_is_a", 
	  argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_boolean();
  }

  boolean _non_existent() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_boolean( false );
      NamedValue resultVal = orb.create_named_value( "result", result, 
	  org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "_non_existent", 
	  argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_boolean();
  }

  org.omg.CORBA.Object _get_interface_def() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_Object( (org.omg.CORBA.Object)null );
      NamedValue resultVal = orb.create_named_value( "result", result, 
	  org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "_get_interface_def", 
	  argList, resultVal );

      try {
          thisReq.invoke();
      }
      catch( BAD_OPERATION e ) {
	  // expected, since we do not implement _get_interface_def in our ORB.
      }

      // Return result:
      result = thisReq.result().value();
      return result.extract_Object();
  }

  void clearInvoked() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method:
      Request thisReq = object._create_request( null, "clearInvoked", 
	  argList, null );
      thisReq.invoke();
  }

  boolean wasInvoked() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // Create result parameter:
      Any result = orb.create_any();
      result.insert_boolean( false );
      NamedValue resultVal = orb.create_named_value( "result", result, 
	  org.omg.CORBA.ARG_OUT.value );

      // Invoke method:
      Request thisReq = object._create_request( null, "wasInvoked", 
	  argList, resultVal );
      thisReq.invoke();

      // Return result:
      result = thisReq.result().value();
      return result.extract_boolean();
  }

  void resetServant() {
      // Create parameter list:
      NVList argList = orb.create_list( 0 );

      // No result parameter:

      // Invoke method:
      Request thisReq = object._create_request( null, "resetServant", 
	  argList, null );
      thisReq.invoke();
  }

}

