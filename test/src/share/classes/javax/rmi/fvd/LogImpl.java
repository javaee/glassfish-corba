/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

package javax.rmi.fvd;
import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import java.util.Hashtable;
import java.util.Properties;

public class LogImpl implements Log/*, java.awt.event.ActionListener*/ {
    private static String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };
    private static Log logServer = null;
    /*
      Frame f = null;
      TextArea ta = null;
        
      public void actionPerformed(ActionEvent e){
      try{
      logMssg("LogImpl","Shutting down");
      TheTest.shutdown();
      logMssg("LogImpl","Shutdown completed");
      System.exit(1);
      }
      catch(Throwable t){
      logMssg("LogImpl",t.getMessage());

      }

      }
      private void setup(){

      f = new Frame("Log Window");
      Button b = new Button("Shutdown test");
      f.setLayout(new BorderLayout());
      f.add("South", b);
      b.addActionListener(this);
      ta = new TextArea();
      f.add("Center", ta);
      f.show();
      f.reshape(100,20,350,350);
                
      }
    */
    public void log(String who, String what) throws java.rmi.RemoteException {
        /*
          if (f == null)
          setup();
          String text = ta.getText();
          text = text + "\n" + who + ":"+what;
          ta.setText(text);
        */
    }   

    public static void logMssg(String who, String what){
        try{
            if (logServer == null)
                connect();
            logServer.log(who,what);
        }
        catch(Throwable t){}
    }

    public static void open(){
        try{
            Properties props = System.getProperties();
        
            props.put(  "java.naming.factory.initial",
                        JndiConstants.COSNAMING_CONTEXT_FACTORY);
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");
            
            props.put(  "org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
            
            // create an RMI Servant.  The Servant will actually
            // handle the users request.
            
            LogImpl servant = new LogImpl();
            
            // Let use PortableRemoteObject to export our servant.
            // This same method works for JRMP and IIOP.
            
            PortableRemoteObject.exportObject(servant);
            
            // Once the Object is exported we are going to link it to
            // our ORB.  To do this we need to get the Tie associated
            // with our Servant.  PortableRemoteObject.export(...) 
            // create a Tie for us.  All we have to do is to retrieve the
            // Tie from javax.rmi.CORBA.Util.getTie(...);
            
            Tie servantsTie = javax.rmi.CORBA.Util.getTie(servant);
            
            // Now lets set the orb in the Tie object.  The Sun/IBM
            // ORB will perform a orb.connect.  So at this point the
            // Tie is connected to the ORB and ready for work.
            servantsTie.orb(orb);

        
            // We are using JNDI/CosNaming to export our object so we
            // need to get the root naming context.  We use the properties
            // set above to initialize JNDI.
            
            Hashtable env = new Hashtable();
            env.put(  "java.naming.corba.orb", orb);
            
            Context ic = new InitialContext(env);

            // Now lets Export our object by publishing the object
            // with JNDI
            ic.rebind("LogServer", servantsTie);
            java.lang.Object objref  = ic.lookup("LogServer");
                        
            // This test is designed to verify PortableRemoteObject.narrow
                        
            logServer= (Log)
                PortableRemoteObject.narrow(objref,Log.class);
        }
        catch(Throwable t){}
    }

    private static void connect(){
        try{
            Properties props = System.getProperties();
            
            props.put(  "java.naming.factory.initial",
                        JndiConstants.COSNAMING_CONTEXT_FACTORY);
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");
            
            props.put(  "org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
                
            // We are going to use JNDI/CosNaming so lets go ahead and
            // create our root naming context.  NOTE:  We setup CosNaming
            // as our naming plug-in for JNDI by setting properties above.
            Hashtable env = new Hashtable();
            env.put(  "java.naming.corba.orb", orb);
            Context ic = new InitialContext(env);
            
            // Let the test begin...
            // Resolve the Object Reference using JNDI/CosNaming
            java.lang.Object objref  = ic.lookup("LogServer");
                        
            // This test is designed to verify PortableRemoteObject.narrow
                        
            logServer= (Log)
                PortableRemoteObject.narrow(objref,Log.class);

            // com.sun.corba.ee.impl.io.ValueUtility.logEnabled = true;
        }
        catch(Throwable t){}
    }
}
