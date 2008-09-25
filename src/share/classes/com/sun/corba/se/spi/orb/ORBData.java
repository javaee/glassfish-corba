/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orb ;


import org.omg.PortableInterceptor.ORBInitializer ;

import com.sun.corba.se.pept.transport.Acceptor;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.IORToSocketInfo;
import com.sun.corba.se.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.se.spi.transport.TcpTimeouts;
import com.sun.corba.se.spi.orbutil.generic.Pair;

import com.sun.corba.se.impl.legacy.connection.USLPort;
import com.sun.corba.se.impl.encoding.CodeSetComponentInfo ;

public interface ORBData {
    public String getORBInitialHost() ;

    public int getORBInitialPort() ;

    public String getORBServerHost() ;

    public int getORBServerPort() ;

    public boolean getListenOnAllInterfaces();

    public com.sun.corba.se.spi.legacy.connection.ORBSocketFactory getLegacySocketFactory () ;

    public com.sun.corba.se.spi.transport.ORBSocketFactory getSocketFactory();

    public USLPort[] getUserSpecifiedListenPorts () ;

    public IORToSocketInfo getIORToSocketInfo();
    public void            setIORToSocketInfo(IORToSocketInfo x);

    public IIOPPrimaryToContactInfo getIIOPPrimaryToContactInfo();
    public void                     setIIOPPrimaryToContactInfo(
						  IIOPPrimaryToContactInfo x);

    public String getORBId() ;

    public boolean isLocalOptimizationAllowed() ;

    public GIOPVersion getGIOPVersion() ;

    public int getHighWaterMark() ;

    public int getLowWaterMark() ;

    public int getNumberToReclaim() ;

    public int getGIOPFragmentSize() ;

    public int getGIOPBufferSize() ;

    public int getGIOPBuffMgrStrategy(GIOPVersion gv) ;

    /**
     * @return the GIOP Target Addressing preference of the ORB.
     * This ORB by default supports all addressing dispositions unless specified
     * otherwise via a java system property ORBConstants.GIOP_TARGET_ADDRESSING
     */
    public short getGIOPTargetAddressPreference() ;

    public short getGIOPAddressDisposition() ;

    public boolean useByteOrderMarkers() ;

    public boolean useByteOrderMarkersInEncapsulations() ;

    public boolean alwaysSendCodeSetServiceContext() ;

    public boolean getPersistentPortInitialized() ;

    public int getPersistentServerPort();

    public boolean getPersistentServerIdInitialized() ;

    /** Return the persistent-server-id of this server. This id is the same
     *  across multiple activations of this server. 
     *  The user/environment is required to supply the 
     *  persistent-server-id every time this server is started, in 
     *  the ORBServerId parameter, System properties, or other means.
     *  The user is also required to ensure that no two persistent servers
     *  on the same host have the same server-id.
     */
    public int getPersistentServerId();

    public boolean getServerIsORBActivated() ;

    public Class getBadServerIdHandler();

    /**
    * Get the prefered code sets for connections. Should the client send the 
    * code set service context on every request?
    */
    public CodeSetComponentInfo getCodeSetComponentInfo() ;

    public ORBInitializer[] getORBInitializers();

    /** Added to allow user configurators to add ORBInitializers
     * for PI.  This makes it possible to add interceptors from
     * an ORBConfigurator.
     */
    public void addORBInitializer( ORBInitializer init ) ;

    public Pair<String,String>[] getORBInitialReferences();

    public String getORBDefaultInitialReference() ;

    public String[] getORBDebugFlags();

    public Acceptor[] getAcceptors();

    public CorbaContactInfoListFactory getCorbaContactInfoListFactory();

    public String acceptorSocketType();
    public boolean acceptorSocketUseSelectThreadToWait();
    public boolean acceptorSocketUseWorkerThreadForEvent();
    public String connectionSocketType();
    public boolean connectionSocketUseSelectThreadToWait();
    public boolean connectionSocketUseWorkerThreadForEvent();

    public long getCommunicationsRetryTimeout();
    public long getWaitForResponseTimeout();
    public TcpTimeouts getTransportTcpTimeouts();
    public TcpTimeouts getTransportTcpConnectTimeouts();
    public boolean disableDirectByteBufferUse() ;
    public boolean isJavaSerializationEnabled();
    public boolean useRepId();

    public boolean showInfoMessages();

    public boolean getServiceContextReturnsNull() ;

    // this method tells whether the current ORB was created from within the app server
    // This helps in performance improvement (for certain computations that donot need to be 
    //performed again and again. For e.g. getMaxStreamFormatVersion())
    public boolean isAppServerMode() ;
    
    // Get the ByteBuffer size to use when reading from a SocketChannel,
    // i.e optimized read strategy
    public int getReadByteBufferSize();
    
    // Get maximum read ByteBuffer size to re-allocate
    public int getMaxReadByteBufferSizeThreshold();

    // Get the pooled DirectByteBuffer slab size
    public int getPooledDirectByteBufferSlabSize();
    
    // Should a blocking read always be done when using the optimized read
    // strategy ?
    public boolean alwaysEnterBlockingRead();
    
    // Set whether the read optimization should always enter a blocking read
    // after doing a non-blocking read
    public void alwaysEnterBlockingRead(boolean b);

    // Should the optimized non-blocking read include in its while loop the
    // condition to check the MessageParser if it is expecting more data?
    public boolean nonBlockingReadCheckMessageParser();

    // Should the optimized blocking read include in its while loop the
    // condition to check the MessageParser if it is expecting more data?
    public boolean blockingReadCheckMessageParser();

    public boolean timingPointsEnabled() ;

    public boolean useEnumDesc() ;
}

// End of file.
