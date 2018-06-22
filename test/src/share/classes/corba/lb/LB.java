/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package corba.lb ;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;

import java.net.Socket ;
import java.net.ServerSocket ;
import java.net.InetAddress ;
import java.net.InetSocketAddress ;

import java.util.Arrays ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

/** This class implements a TCP load balancer.  The idea is that it has a pool of addresses
 * that it can send messages to.  Every time a TCP connection is made to this process,
 * it selects one of its pool addresses and open a corresponding TCP connection to
 * the address.  All traffic is then bidirectionally forwarded.
 */
public class LB {
    private static final boolean DEBUG = true ;
    private static int myPort ;
    private static List pool ;
    private static int poolIndex = 0 ;

    private static void dprint( String msg ) {
        System.out.println( msg ) ;
    }

    private static class ArgParser {
        List largs ;
        Iterator iter ;
        private boolean ungetFlag = false ;
        private String last = null ;

        public ArgParser( String[] args ) {
            largs = Arrays.asList( args ) ;
            iter = largs.iterator() ;
        }

        private boolean isCommand( String str ) {
            return str.charAt(0)=='-' ;
        }

        private String get() {
            if (!iter.hasNext())
                throw new RuntimeException( "No more arguments" ) ;

            if (ungetFlag) {
                ungetFlag = false ;
            } else {
                last = (String)iter.next() ;
            }

            return last ;
        }

        private void unget() {
            ungetFlag = true ;
        }

        public boolean done() {
            return !iter.hasNext() ;
        }

        public String getCommand() {
            String data = get() ;
            if (!isCommand(data))
                throw new RuntimeException( "Next argument is not a command" ) ;

            return data ;
        }

        public String getData() {
            String data = get() ;
            if (isCommand(data))
                throw new RuntimeException( "Next argument is not a command" ) ;
            
            return data ;
        }

        public List getDataList() {
            List result = new ArrayList() ;
            while (iter.hasNext()) {
                String data = get() ;
                if (isCommand(data)) {
                    unget() ;
                    break ;
                }
                result.add( data ) ;
            }

            if (result.size() == 0)
                throw new RuntimeException( "No data available" ) ;

            return result ;
        }
    }

    private static void usageAndExit(Exception exc) {
        if (exc != null) {
            System.out.println( "Caught exception " + exc ) ;
            exc.printStackTrace() ;
        } else {
            System.out.println( "Illegal arguments.  Must have one each of -listen, -pool:" ) ;
            System.out.println( "\t-listen <address>" ) ;
            System.out.println( "\t-pool <address>" ) ;
        }

        System.exit(1) ;
    }

    private static List parseInetSocketAddresses( List data ) {
        List result = new ArrayList() ;
        Iterator iter = data.iterator() ;
        while (iter.hasNext()) {
            String str = (String)iter.next() ;
            int lindex = str.lastIndexOf( ':' ) ;
            String head ;
            String tail ;

            if (lindex < 0) {
                head = "localhost" ;
                tail = str ;
            } else {
                head = str.substring( 0, lindex ) ;
                tail = str.substring( lindex+1 ) ;
            } 

            int port = Integer.valueOf( tail ).intValue() ; 
            InetSocketAddress isa = new InetSocketAddress( head, port ) ;
            result.add( isa ) ;
        }

        return result ;
    }

    /** All addresses are given in host:port form.
     * If host is omitted, we assume localhost for the host.
     * Arguments are:
     * <pre>
     * -listen <addr>
     * -pool <addr>,<addr> ...
     */
    public static void main( String[] args ) {
      try {
        ArgParser ap = new ArgParser( args ) ; 
        while (!ap.done()) {
          String command = ap.getCommand() ;
          if (command.equals( "-listen" )) {
            String data = ap.getData() ;
            myPort = Integer.valueOf( data ).intValue() ;
          } else if (command.equals( "-pool" )) {
            List data = ap.getDataList() ;
            pool = parseInetSocketAddresses( data ) ;
          } else if (command.equals( "-ORBInitialPort" )) {
            String data = ap.getData() ;
          }
          //this else block is causing it to exit since it is finding args other than -listen and -pool. 
          //-ORBInitialPort is also getting passed
          //else {
          //usageAndExit(null) ;
          //}
        }
      } catch (Exception exc) {
        usageAndExit(exc ) ;
      }
      run() ;
      
    }

    private static String makeAndSetName( String name, InetAddress addr, int port, 
        int count ) {

        String result = name + "(" + count + "):" + addr + ":" + port ;
        Thread.currentThread().setName( result ) ;
        return result ;
    }

    private static void makeCleaner( final InetAddress addr,
        final int port, final int count, final Thread t1, final Thread t2, 
        final Socket ss, final Socket cs ) {

        new Thread() {
            public void run() {
                String myName = makeAndSetName( "Cleaner", addr, port, count ) ;
                try {
                    System.out.println( myName + " waiting for " + t1 ) ;
                    t1.join() ;
                    System.out.println( myName + " waiting for " + t2 ) ;
                    t2.join() ;
                    ss.close() ;
                    cs.close() ;
                } catch (Exception exc) {
                    // ignore this: we are just cleaning up here!
                    System.out.println( myName + " caught exception " + exc ) ;
                    exc.printStackTrace() ;
                }

                System.out.println( myName + " is exiting" ) ;
            }
        }.start() ;
    }

    private static class DataCopier extends Thread {
        private byte[] buffer = new byte[4096] ;
        private String name ;
        private Socket inputSocket ;
        private InputStream is ;
        private OutputStream os ;
        private DataCopier peer ;
        private volatile boolean running = true ;

        public DataCopier( String name, Socket inputSocket, OutputStream os ) {
            this.name = name ;
            this.inputSocket = inputSocket ;
            try {
                this.is = inputSocket.getInputStream() ;
            } catch (Exception exc) {
                throw new RuntimeException( exc ) ;
            }
            this.os = os ;
        }

        public void run() {
            try {
                this.setName( name ) ;
                while (running) {
                    int size = is.read( buffer ) ;
                    dprint( name + ": read " + size + " bytes" ) ;
                    if (size < 0)
                        break ;
                    os.write( buffer, 0, size ) ;
                    dprint( name + ": wrote " + size + " bytes" ) ;
                }
            } catch (IOException exc) {
                System.out.println( name + " terminated with exception " + exc ) ;
                exc.printStackTrace() ;
            }

            close() ;
            System.out.println( name + " is exiting" ) ;
        }

        // stop this thread by closing the inputSocket, which should
        // terminate the loop.  Then close the peer copier if.
        // This method does nothing after running == false, since otherwise
        // the two peers would cause infinite recursion.
        public synchronized void close() {
            dprint( name + ": close called, running = " + running ) ;
            if (running) {
                running = false ; 
                try {
                    inputSocket.close() ;
                } catch (Exception exc) {
                    // NO-OP
                    dprint( name + ": exception on DataCopier.close: " + exc ) ;
                }

                peer.close() ;
            } 
        }

        public void setPeer( DataCopier peer ) {
            this.peer = peer ;
        }
    }

    private static DataCopier makeDataCopier( final String name, 
        final InetAddress addr, final int port, final int count,
        final Socket s, final OutputStream os ) {
        
        String myName = makeAndSetName( name, addr, port, count ) ;
        DataCopier result = new DataCopier( myName, s, os ) ;

        return result ;
    }

    private static boolean createSocketCopier( Socket socket, int count ) {
        int retryCount = 0 ;
        while (retryCount++ < pool.size()) {
            // Grab the next pool address
            poolIndex++ ;
            if (poolIndex == pool.size())
                poolIndex = 0 ;

            dprint( "Creating new Socket copier(" + count + ") for socket " + socket ) ;
            // Open a new connection to the pool address
            InetSocketAddress isa = (InetSocketAddress)pool.get(poolIndex) ;
            InetAddress addr = isa.getAddress() ;
            int port = isa.getPort() ;
            
            Socket client = null ;

            try {
                client = new Socket( addr, port ) ;
                
                // Create two data copiers:
                DataCopier t1 = makeDataCopier( "S->C", addr, port, count, socket,
                    client.getOutputStream() ) ;
                DataCopier t2 = makeDataCopier( "C->S", addr, port, count, client,
                    socket.getOutputStream() ) ;

                // set each copier as a peer of the other
                t1.setPeer( t2 ) ;
                t2.setPeer( t1 ) ;

                // Now start the data copying
                t1.start() ;
                t2.start() ;

                // Cleaner is no longer used
                // makeCleaner( addr, port, count, t1, t2, socket, client ) ;
                return true ;
            } catch (IOException exc) {
                System.out.println( "Exception while creating socket to port " + port + ": " + exc ) ;
                exc.printStackTrace() ;
                // run around the retry loop
            }
        }

        // It was not possible to find a valid instance in the pool.
        return false ;
    }

    private static int acceptCount = 0 ;

    private static void run() {  
        try {
            ServerSocket ss = new ServerSocket( myPort ) ;
            System.out.println("Server is ready." ) ;
            System.out.println("Server listening at port " + myPort);
            while (true) {
                try {
                    Socket socket = ss.accept() ;
                    if (!createSocketCopier( socket, acceptCount++ ))
                        // It is very important to close the socket if we cannot
                        // find a valid pool address, as otherwise the client will hang!
                        socket.close() ;
                } catch (Exception exc) {
                    System.out.println( "Exception in accept loop: " + exc ) ;
                    exc.printStackTrace() ;
                }
            }
        } catch (IOException exc) {
            usageAndExit( exc ) ;
        } 
    }
}
    
