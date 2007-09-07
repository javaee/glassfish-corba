/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1993-2007 Sun Microsystems, Inc. All rights reserved.
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
/* 
 * @(#)RepositoryImpl.java 1.1 97/10/17 
 *
 * Copyright 1993-1997 Sun Microsystems, Inc. 901 San Antonio Road,
 * Palo Alto, California, 94303, U.S.A.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * CopyrightVersion 1.2
 *
 */

package com.sun.corba.se.impl.activation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;

import com.sun.corba.se.spi.activation.BadServerDefinition;
import com.sun.corba.se.spi.activation.RepositoryPackage.ServerDef;
import com.sun.corba.se.spi.activation._RepositoryImplBase;
import com.sun.corba.se.spi.activation.ServerAlreadyRegistered;
import com.sun.corba.se.spi.activation.ServerAlreadyInstalled;
import com.sun.corba.se.spi.activation.ServerAlreadyUninstalled;
import com.sun.corba.se.spi.activation.ServerNotRegistered;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.transport.SocketOrChannelAcceptor;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.orbutil.ORBConstants;

import com.sun.corba.se.impl.logging.ActivationSystemException;

/**
 * 
 * @version 	1.1, 97/10/17
 * @author	Rohit Garg
 * @since	JDK1.2
 */
public class RepositoryImpl extends _RepositoryImplBase
    implements Serializable
{

    // added serialver computed by the tool
    private static final long serialVersionUID = 8458417785209341858L;

    RepositoryImpl(ORB orb, File dbDir, boolean debug)
    {
	this.debug = debug ;
	this.orb = orb;
	wrapper =  orb.getLogWrapperTable().get_ORBD_REPOSITORY_Activation() ;

	// if databse does not exist, create it otherwise read it in
	File dbFile = new File(dbDir, "servers.db");
	if (!dbFile.exists()) {
	    db = new RepositoryDB(dbFile);
	    db.flush();
	} else {
            try {
                FileInputStream fis = new FileInputStream(dbFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                db = (RepositoryDB) ois.readObject();
                ois.close();
            } catch (Exception e) {
		throw wrapper.cannotReadRepositoryDb( e ) ;
            }
	}

	// export the repository
	orb.connect(this);
    }

    private String printServerDef( ServerDef sd )
    {
	return "ServerDef[applicationName=" + sd.applicationName +
	    " serverName=" + sd.serverName +
	    " serverClassPath=" + sd.serverClassPath +
	    " serverArgs=" + sd. serverArgs +
	    " serverVmArgs=" + sd.serverVmArgs +
	    "]" ;
    }

    public int registerServer(ServerDef serverDef, int theServerId)
        throws ServerAlreadyRegistered
    {
        synchronized (db) {
	    int serverId;
	    for (DBServerDef server : db.serverTable.values() ) {
                if (serverDef.applicationName.equals(server.applicationName)) {
		    if (debug)
			System.out.println( 
			    "RepositoryImpl: registerServer called " + 
			    "to register ServerDef " + 
			    printServerDef( serverDef ) +
			    " with " + ((theServerId==illegalServerId) ?
			"a new server Id" : ("server Id " + theServerId)) +
					   " FAILED because it is already registered." ) ;

                    throw new ServerAlreadyRegistered(server.id);
                }
	    }

	    // generate a new server id
	    if (theServerId == illegalServerId) 
	        serverId = db.incrementServerIdCounter();
	    else 
		serverId = theServerId;
     
	    // add server def to the database
	    DBServerDef server = new DBServerDef(serverDef, serverId);
	    db.serverTable.put(serverId, server);
	    db.flush();
    
	    if (debug)
		if (theServerId==illegalServerId)
		    System.out.println( "RepositoryImpl: registerServer called " +
					"to register ServerDef " + printServerDef( serverDef ) + 
					" with new serverId " + serverId ) ;
		else
		    System.out.println( "RepositoryImpl: registerServer called " +
					"to register ServerDef " + printServerDef( serverDef ) + 
					" with assigned serverId " + serverId ) ;

	    return serverId;
        }
    }

    public int registerServer(ServerDef serverDef)
        throws ServerAlreadyRegistered, BadServerDefinition
    {
	// verify that the entry is valid
	LegacyServerSocketEndPointInfo endpoint =
	    orb.getLegacyServerSocketManager()
 	        .legacyGetEndpoint(LegacyServerSocketEndPointInfo.BOOT_NAMING);
	int initSvcPort = ((SocketOrChannelAcceptor)endpoint)
	    .getServerSocket().getLocalPort();
	ServerTableEntry entry = new ServerTableEntry( wrapper,
	    illegalServerId, serverDef, (int) initSvcPort, "", true, debug );

	switch (entry.verify()) {
	case ServerMain.OK:
	    break;
	case ServerMain.MAIN_CLASS_NOT_FOUND:
	    throw new BadServerDefinition("main class not found.");
	case ServerMain.NO_MAIN_METHOD:
	    throw new BadServerDefinition("no main method found.");
	case ServerMain.APPLICATION_ERROR:
	    throw new BadServerDefinition("server application error.");
	default: 
	    throw new BadServerDefinition("unknown Exception.");
	}

	return registerServer(serverDef, illegalServerId);
    }

    public void unregisterServer(int serverId) throws ServerNotRegistered {

        DBServerDef server = null;
        synchronized (db) {

	    // check to see if the server is registered
	    server = db.serverTable.get(serverId);
	    if (server == null)  {
		if (debug)
		    System.out.println( 
				       "RepositoryImpl: unregisterServer for serverId " + 
				       serverId + " called: server not registered" ) ;

		throw (new ServerNotRegistered());
	    }

	    // remove server from the database
	    db.serverTable.remove(serverId);
	    db.flush();
	}

	if (debug)
	    System.out.println( 
			       "RepositoryImpl: unregisterServer for serverId " + serverId + 
			       " called" ) ;
    }

    private DBServerDef getDBServerDef(int serverId) throws ServerNotRegistered
    {
	DBServerDef server = db.serverTable.get(serverId);

	if (server == null) 
	    throw new ServerNotRegistered( serverId );

	return server ;
    }

    public ServerDef getServer(int serverId) throws ServerNotRegistered 
    {
	DBServerDef server = getDBServerDef( serverId ) ;

	ServerDef serverDef = new ServerDef(server.applicationName, server.name, 
					    server.classPath, server.args, server.vmArgs);

	if (debug)
	    System.out.println( 
			       "RepositoryImpl: getServer for serverId " + serverId + 
			       " returns " + printServerDef( serverDef ) ) ;

	return serverDef;
    }

    public boolean isInstalled(int serverId) throws ServerNotRegistered {
	DBServerDef server = getDBServerDef( serverId ) ;
	return server.isInstalled ;	
    }

    public void install( int serverId ) 
	throws ServerNotRegistered, ServerAlreadyInstalled 
    {
	DBServerDef server = getDBServerDef( serverId ) ;

	if (server.isInstalled) 
	    throw new ServerAlreadyInstalled( serverId ) ;
	else {
	    server.isInstalled = true ;
	    db.flush() ;
	}
    }

    public void uninstall( int serverId ) 
	throws ServerNotRegistered, ServerAlreadyUninstalled 
    {
	DBServerDef server = getDBServerDef( serverId ) ;

	if (!server.isInstalled) 
	    throw new ServerAlreadyUninstalled( serverId ) ;
	else {
	    server.isInstalled = false ;
	    db.flush() ;
	}
    }

    public int[] listRegisteredServers() {
        synchronized (db) {
            int i=0;
            int servers[] = new int[db.serverTable.size()];
	    for (DBServerDef server : db.serverTable.values() ) {
		servers[i++] = server.id ;
	    }

	    if (debug) {
		StringBuffer sb = new StringBuffer() ;
		for (int ctr=0; ctr<servers.length; ctr++) {
		    sb.append( ' ' ) ;
		    sb.append( servers[ctr] ) ;
		}

		System.out.println( 
				   "RepositoryImpl: listRegisteredServers returns" +
				   sb.toString() ) ;
	    }

            return servers;
        }
    }

    public int getServerID(String applicationName) throws ServerNotRegistered {
	synchronized (db) {
	    int result = -1 ;

	    for (Map.Entry<Integer,DBServerDef> entry : db.serverTable.entrySet() ) {
		    if (entry.getValue().applicationName.equals(applicationName)) {
			result = entry.getKey() ;
			break ;
		    }
	    }

	    if (debug)
		System.out.println("RepositoryImpl: getServerID for " + 
				   applicationName + " is " + result ) ;
	    
	    if (result == -1) {
	        throw (new ServerNotRegistered());
	    } else {
	        return result ;
	    }
	}
    }
    
    public String[] getApplicationNames() {
	synchronized (db) {
	    List<String> result = new ArrayList<String>() ;
	    for (Map.Entry<Integer,DBServerDef> entry : db.serverTable.entrySet() ) {
		if (!entry.getValue().applicationName.equals(""))
		    result.add( entry.getValue().applicationName ) ;
	    }

	    String[] apps = result.toArray( new String[result.size()] ) ;

	    if (debug) {
		StringBuffer sb = new StringBuffer() ;
		for (int ctr=0; ctr<apps.length; ctr++) {
		    sb.append( ' ' ) ;
		    sb.append( apps[ctr] ) ;
		}

		System.out.println( "RepositoryImpl: getApplicationNames returns " +
				    sb.toString() ) ;
	    }

	    return apps;
	}
    }
    /** 
     * Typically the Repositoy is created within the ORBd VM but it can 
     * be independently started as well.
     */
    public static void main(String args[]) {
	boolean debug = false ;
	for (int ctr=0; ctr<args.length; ctr++)
	    if (args[ctr].equals("-debug"))
		debug = true ;
	
	try {
	    // See Bug 4396928 for more information about why we are 
	    // initializing the ORBClass to PIORB (now ORBImpl, but see the bug).
	    Properties props = new Properties();
	    props.put("org.omg.CORBA.ORBClass", 
		"com.sun.corba.se.impl.orb.ORBImpl");
	    ORB orb = (ORB) ORB.init(args, props);

	    // create the repository object
	    String db = System.getProperty( ORBConstants.DB_PROPERTY, 
		    ORBConstants.DEFAULT_DB_NAME );
	    new RepositoryImpl(orb, new File(db), debug);

	    // wait for shutdown
	    orb.run();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    transient private boolean debug = false;

    final static int illegalServerId = -1;

    transient private RepositoryDB db = null;

    transient ORB orb = null;

    transient ActivationSystemException wrapper ;

    class RepositoryDB implements Serializable
    {
	private File				db;
	private Map<Integer,DBServerDef> 	serverTable;
	private Integer				serverIdCounter;

	RepositoryDB(File dbFile) {
	    
	    db = dbFile;

	    // initialize the Server Id counter and hashtable.
	    // the lower id range is reserved for system servers
	    serverTable     = new HashMap<Integer,DBServerDef>(255);
	    serverIdCounter = Integer.valueOf(256); 
	}

	int incrementServerIdCounter()
	{
	    int value = serverIdCounter.intValue();
	    serverIdCounter = Integer.valueOf(++value);
 
	    return value;
	}

	void flush()
	{
	    try {
		db.delete();
		FileOutputStream fos = new FileOutputStream(db);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.flush();
		oos.close();
	    } catch (Exception ex) {
		throw wrapper.cannotWriteRepositoryDb( ex ) ;
            }
	}
    }

    class DBServerDef implements Serializable
    {
	public String toString() {
	    return "DBServerDef(applicationName=" + applicationName +
		", name=" + name +
		", classPath=" + classPath +
		", args=" + args + 
		", vmArgs=" + vmArgs +
		", id=" + id + 
		", isInstalled=" + isInstalled + ")" ;
	}

	DBServerDef(ServerDef server, int server_id) {
	    applicationName	= server.applicationName ;
	    name 	= server.serverName;
	    classPath 	= server.serverClassPath;
	    args   	= server.serverArgs;
	    vmArgs 	= server.serverVmArgs;
	    id     	= server_id;
	    isInstalled = false ;
	}

	String applicationName;
	String name;
	String classPath;
	String args;
	String vmArgs;
	boolean isInstalled ;
	int    id;
    }
}
