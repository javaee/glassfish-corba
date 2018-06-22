/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package test;

import java.rmi.Remote;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import org.omg.CORBA.ORB;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.IOException;
import javax.rmi.PortableRemoteObject;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServantContext implements ServantManager {
    
    // If a System property exists called "LOCAL_SERVANTS", the servant
    // manager will be started in the current process.
    
    public static final String LOCAL_SERVANTS_FLAG = "LOCAL_SERVANTS";
    public static boolean LOCAL_SERVANTS = false;
    
    static final String SERVANT_MANAGER_CLASS = "test.ServantManagerImpl";
    static final String SERVANT_MANAGER_NAME = "ServantManager";
      
    private static Hashtable contexts = new Hashtable();
   
    private ORB orb = null;
    private String nameServerHost = null;
    private int nameServerPort = 0;
    private Process nameServerProcess = null;
    private Process servantManagerProcess = null;
    private Context nameContext = null;
    private ServantManager servantManager = null;
    private String name = null;
    private boolean iiop;
    private String orbDebugFlags ;
    private String key = null;
   
    public Context getNameContext () {
        return nameContext;
    }
   
    public ORB getORB () {
        return orb;
    }
    
    private ServantContext ( String nameServerHost, int nameServerPort, 
                             boolean iiop, String key, String orbDebugFlags ) throws Exception {
        Test.dprint( "ServantContext constructor called: nameServerHost = " + 
                     nameServerHost + " nameServerPort = " + nameServerPort + 
                     " iiop = " + iiop + " key = " + key ) ;
        this.nameServerHost = nameServerHost;
        this.nameServerPort = nameServerPort;
        this.iiop = iiop;
        this.orbDebugFlags = orbDebugFlags ;
        boolean failed = true;
        this.key = key;

        try {
            if (nameServerHost == null) {
                try {
                    nameServerProcess = Util.startNameServer(nameServerPort,iiop);
                    if (nameServerProcess == null) {
                        throw new IOException( 
                            "ServantContext: could not start name server"); 
                    }
                } catch (Error e) {
                    if (Test.debug)
                        e.printStackTrace() ;

                    if (e.getMessage().indexOf( "bootstrap service on port " + 
                                                nameServerPort) < 0) {
                        throw e;
                    } else {
                        System.out.println("Name server already started.");
                    }
                }
                
                // Are we supposed to start the servant manager in this process?
                if (!LOCAL_SERVANTS) {
                    servantManagerProcess = Util.startServer( 
                        SERVANT_MANAGER_CLASS, getName(), nameServerHost, 
                        nameServerPort, iiop );

                    if (servantManagerProcess == null) {
                        throw new IOException( 
                            "ServantContext: could not start servant manager"); 
                    }
                }
            }
            
            if (iiop) {
                Test.dprint( "starting an ORB" ) ;
                // Start up the orb...
                orb = Util.createORB( nameServerHost,nameServerPort,
                                      orbDebugFlags );
            }
            
            nameContext = Util.getInitialContext(iiop, 
                nameServerHost, nameServerPort, orb ) ;
            
            // Are we supposed to start the servant manager in this process?
            if (!LOCAL_SERVANTS) {
                Test.dprint( "LOCAL_SERVANTS is false: looking for servant manager" ) ;
                Object stub = nameContext.lookup(getName());
                servantManager = (ServantManager) PortableRemoteObject.narrow( 
                    stub, test.ServantManager.class);
            } else {
                Test.dprint( "LOCAL_SERVANTS is true: starting servant manager" ) ;
                ServantManagerImpl impl = new ServantManagerImpl(orb,nameContext);
                PortableRemoteObject.exportObject(impl);

                // Before calling toStub(), we need to make sure that the impl has
                // been connected to the orb we created above.  To do so, just 
                // look up the tie, and set the orb...
                javax.rmi.CORBA.Tie tie = javax.rmi.CORBA.Util.getTie(impl);
                tie.orb(orb);
                
                // Now get the stub...
                servantManager = (ServantManager) PortableRemoteObject.toStub( 
                    impl);
            }
            
            // Did we succeed?
            if (servantManager.ping().equalsIgnoreCase("Pong")) {
                // Yep.
                failed = false;
            }
        } finally {
            Test.dprint( "ServantContext constructor is exiting" ) ;
            if (failed) {
                // Make sure we clean up...
                destroy();
            }
        }
    }
   
        
    /**
     * Return this context's name.
     */
    public synchronized String getName () {
        if (name == null) {
            
            if (!LOCAL_SERVANTS) {
                String host = null;
                String date = Long.toString(System.currentTimeMillis());
                try {
                    host = InetAddress.getLocalHost().toString().replace('/',';');
                } catch (UnknownHostException e) {}
                name = SERVANT_MANAGER_NAME + "[" + host + ";" + date + "]";
            } else {
                
                // We're debugging the server, so use a canonical name...

                name = SERVANT_MANAGER_NAME;
            }
        }
        
        return name;
    }
    
    /**
     * Start a servant in the remote process.
     * @param servantClass The class of the servant object. Must have a default constructor.
     * @param servantName The name by which this servant should be known.
     * @param publishName True if the name should be published in the name server.
     * @param iiop True if iiop.
     */
    public Remote startServant( String servantClass,
                                String servantName,
                                boolean publishName,
                                boolean iiop) throws java.rmi.RemoteException {
                                    
        return servantManager.startServant( servantClass,
                                            servantName,
                                            publishName,
                                            nameServerHost == null ? "" : nameServerHost,
                                            nameServerPort,
                                            iiop);
    }
 
    /**
     * Start a servant in the remote process.
     * @param servantClass The class of the servant object. Must have a default constructor.
     * @param servantName The name by which this servant should be known.
     * @param publishName True if the name should be published in the name server.
     * @param nameServerHost The name server host. May be null if local host.
     * @param nameServerPort The name server port.
     * @param iiop True if iiop.
     */
    public Remote startServant( String servantClass,
                                String servantName,
                                boolean publishName,
                                String nameServerHost,
                                int nameServerPort,
                                boolean iiop) throws java.rmi.RemoteException {
                                    
        return servantManager.startServant( servantClass,
                                            servantName,
                                            publishName,
                                            nameServerHost,
                                            nameServerPort,
                                            iiop);
    }

    /**
     * Unexport the specified servant. If the servant was published, will be unpublised.
     */
    public void stopServant(String servantName) throws java.rmi.RemoteException {
        servantManager.stopServant(servantName);
    }

    /**
     * Stop all servants in this context.
     */
    public void stopAllServants() throws java.rmi.RemoteException {
        servantManager.stopAllServants();
    }

    /**
     * @Return String the String "Pong"
     */
    public String ping() throws java.rmi.RemoteException {
        return servantManager.ping();
    }

    /**
     * Destroy this context.
     */
    public void destroy () {
        Test.dprint( "ServantContext.destroy called on " + name ) ;

        // Remove self from table...
        synchronized (this) {
            contexts.remove(key);
        }
        
        // Destroy the name server process if needed...
        
        try {
            if (nameServerProcess != null) {
                Test.dprint( "destroying nameServerProcess" ) ;
                nameServerProcess.destroy();
                nameServerProcess = null;
            }
        } catch (Exception e1) {}
        
        // Destroy the servant manager process if needed...
        
        try {
            if (servantManagerProcess != null) {
                Test.dprint( "destroying servantManagerProcess" ) ;
                servantManagerProcess.destroy();
                nameServerProcess = null;
            }
        } catch (Exception e2) {}
    }
    
    /**
     * Destroy all contexts. MUST BE CALLED PRIOR TO PROCESS EXIT!!
     */
    public static synchronized void destroyAll () {
        for (Enumeration e = contexts.elements() ; e.hasMoreElements() ;) {
            try {
                ((ServantContext)e.nextElement()).destroy();
            } catch (Exception e1) {}
        }
    }
   
    /**
     * Get the default ServantContext.
     */
    public static ServantContext getDefaultContext(boolean iiop) 
        throws Exception {
        return getContext( null, 1070, true, iiop, null );
    }
    
    /**
     * Get or create a ServantContext.
     * @param nameServerHost The host on which the name server should run. If not
     * null, the name server will be started if needed.
     * @param nameServerPort The port on which the name server should run.
     * @param createIfNeeded If true and a context does not already exist, one will
     * be created.
     * @param iiop True if iiop.
     */
    public static synchronized ServantContext getContext(String nameServerHost,
                                                         int nameServerPort, boolean createIfNeeded, boolean iiop,
                                                         String orbDebugFlags ) throws Exception {
        
        Test.dprint( "Entering ServantContext.getContext" ) ;
        Test.dprint( "/tnameServerHost = " + nameServerHost ) ;
        Test.dprint( "/tnameServerPort = " + nameServerPort ) ;
        Test.dprint( "/tcreateIfNeeded = " + createIfNeeded ) ;
        Test.dprint( "/tiiop = " + iiop ) ;
        Test.dprint( "/torbDebugFlags = " + orbDebugFlags ) ;

        ServantContext result = null;
        
        try {
            if (System.getProperty(LOCAL_SERVANTS_FLAG) != null) {
                LOCAL_SERVANTS = true;
            } else {
                LOCAL_SERVANTS = false;
            }
      
            String key = nameServerHost + ":" + Integer.toString(nameServerPort);
            
            Object it = contexts.get(key);
            
            if (it != null) {
                result = (ServantContext) it;
            } else {
                if (createIfNeeded) {
                    result = new ServantContext( nameServerHost, nameServerPort,
                                                 iiop, key, orbDebugFlags );
                    contexts.put(key,result);
                }
            }
        } finally {
            Test.dprint( "Exiting ServantContext.getContext" ) ;
        }
        
        return result;
    }
}
