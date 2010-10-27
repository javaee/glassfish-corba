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

package com.sun.corba.se.impl.activation;

/**
 *
 * @author      Rohit Garg
 * @author      Ken Cavanaugh
 * @author      Hemanth Puttaswamy
 * @since       JDK1.2
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import com.sun.corba.se.spi.activation.EndPointInfo;
import com.sun.corba.se.spi.activation.ORBPortInfo;
import com.sun.corba.se.spi.activation.Repository;
import com.sun.corba.se.spi.activation.LocatorPackage.ServerLocation;
import com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB;
import com.sun.corba.se.spi.activation.RepositoryPackage.ServerDef;
import com.sun.corba.se.spi.activation._ServerManagerImplBase;
import com.sun.corba.se.spi.activation.ServerAlreadyActive;
import com.sun.corba.se.spi.activation.ServerAlreadyInstalled;
import com.sun.corba.se.spi.activation.ServerAlreadyUninstalled;
import com.sun.corba.se.spi.activation.ServerNotRegistered;
import com.sun.corba.se.spi.activation.ORBAlreadyRegistered;
import com.sun.corba.se.spi.activation.ServerHeldDown;
import com.sun.corba.se.spi.activation.ServerNotActive;
import com.sun.corba.se.spi.activation.NoSuchEndPoint;
import com.sun.corba.se.spi.activation.InvalidORBid;
import com.sun.corba.se.spi.activation.Server;
import com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.TaggedComponent ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.protocol.ForwardException;
import com.sun.corba.se.spi.transport.CorbaTransportManager;

import com.sun.corba.se.spi.logging.ActivationSystemException ;

import com.sun.corba.se.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.transport.CorbaAcceptor;

public class ServerManagerImpl extends _ServerManagerImplBase
    implements BadServerIdHandler
{
    private static final long serialVersionUID = -8150406906204281113L;
    // Using HashMap, since synchronization should be done by the calling
    // routines
    Map<Integer,ServerTableEntry> serverTable;
    Repository repository;

    CorbaTransportManager transportManager;
    int initialPort;
    ORB orb;
    ActivationSystemException wrapper;
    String dbDirName;
    boolean debug = false ;
 
    private int serverStartupDelay;

    ServerManagerImpl(ORB orb, CorbaTransportManager transportManager,
		      Repository repository, String dbDirName, boolean debug)
    {
        this.orb = orb;
	wrapper = ActivationSystemException.self ;

        this.transportManager = transportManager; // REVISIT - NOT USED.
	this.repository = repository;
	this.dbDirName = dbDirName;
	this.debug = debug ;

	LegacyServerSocketEndPointInfo endpoint =
	    orb.getLegacyServerSocketManager()
	        .legacyGetEndpoint(LegacyServerSocketEndPointInfo.BOOT_NAMING);

	initialPort = ((CorbaAcceptor)endpoint)
	    .getServerSocket().getLocalPort();
        serverTable = new HashMap<Integer,ServerTableEntry>();

        // The ServerStartupDelay is the delay added after the Server registers
        // end point information. This is to allow the server to completely
        // initialize after ORB is instantiated.
        serverStartupDelay = ORBConstants.DEFAULT_SERVER_STARTUP_DELAY;
        String  delay = System.getProperty( ORBConstants.SERVER_STARTUP_DELAY);
        if( delay != null ) {
            try {
                serverStartupDelay = Integer.parseInt( delay );
            } catch ( Exception e ) {
                // Just use the default 1000 milliseconds as the default
            }
        }

        Class<?> cls = orb.getORBData( ).getBadServerIdHandler();
        if( cls == null ) {
            orb.setBadServerIdHandler( this );
        } else {
	    orb.initBadServerIdHandler() ;
        }

        orb.connect(this);
        ProcessMonitorThread.start( serverTable );
    }

    public void activate(int serverId)
        throws ServerAlreadyActive, ServerNotRegistered, ServerHeldDown
    {

        ServerLocation   location;
        ServerTableEntry entry;

	synchronized(serverTable) {
            entry = serverTable.get(serverId);
	}

        if (entry != null && entry.isActive()) {
	    if (debug)
		System.out.println( "ServerManagerImpl: activate for server Id " +
				    serverId + " failed because server is already active. " +
				    "entry = " + entry ) ;

            throw new ServerAlreadyActive( serverId );
	}

        // locate the server
        try {

	    // We call getEntry here so that state of the entry is
	    // checked for validity before we actually go and locate a server

	    entry = getEntry(serverId);

            if (debug)
                System.out.println( "ServerManagerImpl: locateServer called with " +
                                " serverId=" + serverId + " endpointType="
                                + IIOP_CLEAR_TEXT.value + " block=false" ) ;

	    location = locateServer(entry, IIOP_CLEAR_TEXT.value, false);

	    if (debug)
		System.out.println( "ServerManagerImpl: activate for server Id " +
				    serverId + " found location " +
				    location.hostname + " and activated it" ) ;
        } catch (NoSuchEndPoint ex) {
            if (debug)
                System.out.println( "ServerManagerImpl: activate for server Id " +
                                    " threw NoSuchEndpoint exception, which was ignored" );
	}
    }

    public void active(int serverId, Server server) throws ServerNotRegistered
    {
        synchronized (serverTable) {
            ServerTableEntry entry = serverTable.get(serverId);

            if (entry == null) {
		if (debug)
		    System.out.println( "ServerManagerImpl: active for server Id " +
					serverId + " called, but no such server is registered." ) ;

		throw wrapper.serverNotExpectedToRegister() ;
            } else {
		if (debug)
		    System.out.println( "ServerManagerImpl: active for server Id " +
					serverId + " called.  This server is now active." ) ;

		entry.register(server);
	    }
        }
    }

    public void registerEndpoints( int serverId, String orbId,
	EndPointInfo [] endpointList ) throws NoSuchEndPoint, ServerNotRegistered,
	ORBAlreadyRegistered
    {
	// orbId is ignored for now
        synchronized (serverTable) {
            ServerTableEntry entry = serverTable.get(serverId);

            if (entry == null) {
		if (debug)
		    System.out.println(
			"ServerManagerImpl: registerEndpoint for server Id " +
			serverId + " called, but no such server is registered." ) ;

		throw wrapper.serverNotExpectedToRegister() ;
            } else {
		if (debug)
		    System.out.println(
			"ServerManagerImpl: registerEndpoints for server Id " +
			serverId + " called.  This server is now active." ) ;

		entry.registerPorts( orbId, endpointList );
               
	    }
	}
    }

    public int[] getActiveServers()
    {
        int[] list = null;

        synchronized (serverTable) {
	    List<ServerTableEntry> servers = 
		new ArrayList<ServerTableEntry>() ;

	    try {
		for (Map.Entry<Integer,ServerTableEntry> entry : 
		    serverTable.entrySet()) {
		    ServerTableEntry def = entry.getValue() ;
		    if (def.isValid() && def.isActive())	
			servers.add( def ) ;
		}
	    } catch (NoSuchElementException e) {
	        // all done
	    }

	    // collect the active entries
	    list = new int[servers.size()];
	    int i = 0 ;
	    for (ServerTableEntry entry : servers) 
		list[i++] = entry.getServerId() ;
        }

	if (debug) {
	    StringBuffer sb = new StringBuffer() ;
	    for (int ctr=0; ctr<list.length; ctr++) {
		sb.append( ' ' ) ;
		sb.append( list[ctr] ) ;
	    }

	    System.out.println( "ServerManagerImpl: getActiveServers returns" +
				sb.toString() ) ;
	}

        return list;
    }

    public void shutdown(int serverId) throws ServerNotActive
    {
	synchronized(serverTable) {
            ServerTableEntry entry = serverTable.remove(serverId);

            if (entry == null) {
	        if (debug)
		    System.out.println( "ServerManagerImpl: shutdown for server Id " +
				    serverId + " throws ServerNotActive." ) ;

	        throw new ServerNotActive( serverId );
	    }

            try {
                entry.destroy();

	        if (debug)
		    System.out.println( "ServerManagerImpl: shutdown for server Id " +
				    serverId + " completed." ) ;
            } catch (Exception e) {
	        if (debug)
		    System.out.println( "ServerManagerImpl: shutdown for server Id " +
				    serverId + " threw exception " + e ) ;
	    }
	}
    }

    private ServerTableEntry getEntry( int serverId )
	throws ServerNotRegistered
    {
	synchronized (serverTable) {
            ServerTableEntry entry = serverTable.get(serverId);

	    if (debug) {
		if (entry == null) {
		    System.out.println( "ServerManagerImpl: getEntry: " +
					"no active server found." ) ;
		} else {
		    System.out.println( "ServerManagerImpl: getEntry: " +
					" active server found " + entry + "." ) ;
		}
	    }

            if ((entry != null) && (!entry.isValid())) {
                serverTable.remove(serverId);
                entry = null;
            }

            if (entry == null) {
		ServerDef serverDef = repository.getServer(serverId);

                entry = new ServerTableEntry( wrapper,
		    serverId, serverDef, initialPort, dbDirName, false, debug);
		serverTable.put(serverId, entry);
		entry.activate() ;
            }

	    return entry ;
        }
    }

    private ServerLocation locateServer (ServerTableEntry entry, String endpointType,
					boolean block)
        throws NoSuchEndPoint, ServerNotRegistered, ServerHeldDown
    {
	ServerLocation location = new ServerLocation() ;

	// if server location is desired, then wait for the server
	// to register back, then return location

	ORBPortInfo [] serverORBAndPortList;
	if (block) {
	    try {
	            serverORBAndPortList = entry.lookup(endpointType);
            } catch (Exception ex) {
		if (debug)
		    System.out.println( "ServerManagerImpl: locateServer: " +
					"server held down" ) ;

	        throw new ServerHeldDown( entry.getServerId() );
	    }

            String host = 
		orb.getLegacyServerSocketManager()
		    .legacyGetEndpoint(LegacyServerSocketEndPointInfo.DEFAULT_ENDPOINT).getHostName();
            location.hostname = host ;
	    int listLength;
	    if (serverORBAndPortList != null) {
	        listLength = serverORBAndPortList.length;
	    } else {
	        listLength = 0;
	    }
	    location.ports = new ORBPortInfo[listLength];
	    for (int i = 0; i < listLength; i++) {
	        location.ports[i] = new ORBPortInfo(serverORBAndPortList[i].orbId,
			serverORBAndPortList[i].port) ;

	        if (debug)
	            System.out.println( "ServerManagerImpl: locateServer: " +
				    "server located at location " +
				    location.hostname + " ORBid  " +
	                            serverORBAndPortList[i].orbId +
	                            " Port " + serverORBAndPortList[i].port) ;
	    }
	}

	return location;
    }

    private ServerLocationPerORB locateServerForORB (ServerTableEntry entry, String orbId,
					boolean block)
        throws InvalidORBid, ServerNotRegistered, ServerHeldDown
    {
	ServerLocationPerORB location = new ServerLocationPerORB() ;

	// if server location is desired, then wait for the server
	// to register back, then return location

	EndPointInfo [] endpointInfoList;
	if (block) {
	    try {
	        endpointInfoList = entry.lookupForORB(orbId);
	    } catch (InvalidORBid ex) {
		throw ex;
            } catch (Exception ex) {
		if (debug)
		    System.out.println( "ServerManagerImpl: locateServerForORB: " +
					"server held down" ) ;

	        throw new ServerHeldDown( entry.getServerId() );
	    }

            String host = 
		orb.getLegacyServerSocketManager()
		    .legacyGetEndpoint(LegacyServerSocketEndPointInfo.DEFAULT_ENDPOINT).getHostName();
            location.hostname = host ;
	    int listLength;
	    if (endpointInfoList != null) {
	        listLength = endpointInfoList.length;
	    } else {
	        listLength = 0;
	    }
	    location.ports = new EndPointInfo[listLength];
	    for (int i = 0; i < listLength; i++) {
	        location.ports[i] = new EndPointInfo(endpointInfoList[i].endpointType,
			endpointInfoList[i].port) ;

	        if (debug)
	            System.out.println( "ServerManagerImpl: locateServer: " +
				    "server located at location " +
				    location.hostname + " endpointType  " +
	                            endpointInfoList[i].endpointType +
	                            " Port " + endpointInfoList[i].port) ;
	    }
	}

	return location;
    }

    public String[] getORBNames(int serverId)
	throws ServerNotRegistered
    {
	try {
	    ServerTableEntry entry = getEntry( serverId ) ;
	    return (entry.getORBList());
	} catch (Exception ex) {
	    throw new ServerNotRegistered(serverId);
	}
    }

    private ServerTableEntry getRunningEntry( int serverId )
	throws ServerNotRegistered
    {
	ServerTableEntry entry = getEntry( serverId ) ;

	try {
	    // this is to see if the server has any listeners
	    ORBPortInfo [] serverORBAndPortList = entry.lookup(IIOP_CLEAR_TEXT.value) ;
	} catch (Exception exc) {
	    return null ;
	}
	return entry;

    }

    public void install( int serverId )
	throws ServerNotRegistered, ServerHeldDown, ServerAlreadyInstalled
    {
	ServerTableEntry entry = getRunningEntry( serverId ) ;
	if (entry != null) {
	    repository.install( serverId ) ;
	    entry.install() ;
	}
    }

    public void uninstall( int serverId )
	throws ServerNotRegistered, ServerHeldDown, ServerAlreadyUninstalled
    {
	ServerTableEntry entry = serverTable.get( serverId );
	if (entry != null) {
            entry = serverTable.remove(serverId);

            if (entry == null) {
                if (debug)
                    System.out.println( "ServerManagerImpl: shutdown for server Id " +
                                    serverId + " throws ServerNotActive." ) ;

                throw new ServerHeldDown( serverId );
            }

	    entry.uninstall();
	}
    }

    public ServerLocation locateServer (int serverId, String endpointType)
        throws NoSuchEndPoint, ServerNotRegistered, ServerHeldDown
    {
	ServerTableEntry entry = getEntry( serverId ) ;
        if (debug)
            System.out.println( "ServerManagerImpl: locateServer called with " +
                                " serverId=" + serverId + " endpointType=" +
				endpointType + " block=true" ) ;

	// passing in entry to eliminate multiple lookups for
	// the same entry in some cases

	return locateServer(entry, endpointType, true);
    }

    /** This method is used to obtain the registered ports for an ORB.
    * This is useful for custom Bad server ID handlers in ORBD.
    */
    public ServerLocationPerORB locateServerForORB (int serverId, String orbId)
        throws InvalidORBid, ServerNotRegistered, ServerHeldDown
    {
	ServerTableEntry entry = getEntry( serverId ) ;

	// passing in entry to eliminate multiple lookups for
	// the same entry in some cases

	if (debug)
	    System.out.println( "ServerManagerImpl: locateServerForORB called with " +
				" serverId=" + serverId + " orbId=" + orbId +
				" block=true" ) ;
	return locateServerForORB(entry, orbId, true);
    }


    public void handle(ObjectKey okey) 
    {
	IOR newIOR = null;
	ServerLocationPerORB location;

	// we need to get the serverid and the orbid from the object key
	ObjectKeyTemplate oktemp = okey.getTemplate();
	int serverId = oktemp.getServerId() ;
	String orbId = oktemp.getORBId() ;

	try {
	    // get the ORBName corresponding to the orbMapid, that was
	    // first registered by the server
	    ServerTableEntry entry = getEntry( serverId ) ;
	    location = locateServerForORB(entry, orbId, true);
             
	    if (debug)
	        System.out.println( "ServerManagerImpl: handle called for server id" +
			serverId + "  orbid  " + orbId) ;

	    // we received a list of ports corresponding to an ORB in a
            // particular server, now retrieve the one corresponding
	    // to IIOP_CLEAR_TEXT, and for other created the tagged
	    // components to be added to the IOR

	    int clearPort = 0;
	    EndPointInfo[] listenerPorts = location.ports;
	    for (int i = 0; i < listenerPorts.length; i++) {
	        if ((listenerPorts[i].endpointType).equals(IIOP_CLEAR_TEXT.value)) {
	            clearPort = listenerPorts[i].port;
	            break;
                }
	    }

	    // create a new IOR with the correct port and correct tagged
            // components
	    IIOPAddress addr = IIOPFactories.makeIIOPAddress( 
		location.hostname, clearPort ) ;
	    IIOPProfileTemplate iptemp = 
		IIOPFactories.makeIIOPProfileTemplate(
                    orb, GIOPVersion.V1_2, addr ) ;
	    if (GIOPVersion.V1_2.supportsIORIIOPProfileComponents()) {
		iptemp.add((TaggedComponent)IIOPFactories.makeCodeSetsComponent(orb));
		iptemp.add(IIOPFactories.makeMaxStreamFormatVersionComponent());
	    }
	    IORTemplate iortemp = IORFactories.makeIORTemplate(oktemp) ;
	    iortemp.add( iptemp ) ;

	    newIOR = iortemp.makeIOR(orb, "IDL:org/omg/CORBA/Object:1.0", 
		okey.getId() );
	} catch (Exception e) {
	    throw wrapper.errorInBadServerIdHandler( e ) ;
	}

	if (debug)
	    System.out.println( "ServerManagerImpl: handle " +
				"throws ForwardException" ) ;

        
        try {
            // This delay is required in case of Server is activated or 
            // re-activated the first time. Server needs some time before 
            // handling all the requests. 
            // (Talk to Ken to see whether there is a better way of doing this).
            Thread.sleep( serverStartupDelay );
        } catch ( Exception e ) {
            System.out.println( "Exception = " + e );
            e.printStackTrace();
        } 

	throw new ForwardException(orb, newIOR);
    }

    public int getEndpoint(String endpointType) throws NoSuchEndPoint
    {
        return orb.getLegacyServerSocketManager()
	    .legacyGetTransientServerPort(endpointType);
    }

    public int getServerPortForType(ServerLocationPerORB location,
				    String endPointType)
	throws NoSuchEndPoint
    {
	EndPointInfo[] listenerPorts = location.ports;
	for (int i = 0; i < listenerPorts.length; i++) {
	    if ((listenerPorts[i].endpointType).equals(endPointType)) {
		return listenerPorts[i].port;
	    }
	}
	throw new NoSuchEndPoint();
    }

}
