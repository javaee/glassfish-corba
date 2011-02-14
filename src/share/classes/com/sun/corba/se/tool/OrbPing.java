/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.se.tool;

import com.sun.corba.se.spi.orbutil.argparser.DefaultValue;
import com.sun.corba.se.spi.orbutil.argparser.Help;
import com.sun.corba.se.spi.orbutil.argparser.ArgParser;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

/** This tool checks to see if an ORB is listening at the given host and port.
 * It can print out the round trip time, and do a series of pings, or a single ping.
 * <p>
 * A ping consists of constructing a corbaname URL for the NameService, and
 * narrowing the corresponding object reference to the name service.
 *
 * @author ken
 */
public class OrbPing {
    public static class IntervalTimer {
        long lastTime ;

        public void start() {
            lastTime = System.nanoTime() ;
        }

        /** Returns interval since last start() or interval() call in
         * microseconds.
         * @return Elapsed time in microseconds
         */
        public long interval() {
            final long current = System.nanoTime() ;
            final long diff = current - lastTime ;
            start() ;
            return diff/1000 ;
        }
    }

    private interface Args {
        @DefaultValue( "1" )
        @Help( "The number of times to repeat the ORB ping")
        int count() ;

        @DefaultValue( "localhost" )
        @Help( "The host running the ORB")
        String host() ;

        @DefaultValue( "3037")
        @Help( "The port on which the ORB listens for clear text requests")
        int port() ;

        @DefaultValue( "false" )
        @Help( "Display extra information, including timing information" )
        boolean verbose() ;
    }

    private static Args args ;
    private static ORB orb ;
    private static IntervalTimer timer = new IntervalTimer() ;

    private static void ping( String host, int port ) {
        final String url = String.format( "corbaname:iiop:1.2@%s:%d/NameService",
            host, port ) ;

        org.omg.CORBA.Object cobject = null ;
        try {
            timer.start() ;
            cobject = orb.string_to_object( url ) ;
        } catch (Exception exc) {
            msg( "Exception in string_to_object call: %s\n", exc ) ;
        } finally {
            if (args.verbose()) {
                msg( "string_to_object call took %d microseconds\n",
                    timer.interval() ) ;
            }
        }

        NamingContext nctx ;

        try {
            timer.start() ;
            nctx = NamingContextHelper.narrow(cobject);
        } catch (Exception exc) {
            msg( "Exception in naming narrow call: %s\n", exc ) ;
        } finally {
            if (args.verbose()) {
                msg( "naming narrow call took %d microseconds\n",
                    timer.interval() ) ;
            }
        }
    }

    private static void msg( String str, Object... args ) {
        System.out.printf( str, args ) ;
    }

    public static void main( String[] params ) {
        args = (new ArgParser( Args.class )).parse( params, Args.class ) ;

        try {
            timer.start() ;
            orb = ORB.init( params, null ) ;
        } catch (Exception exc) {
            msg( "Exception in ORB.init: %s\n", exc ) ;
        } finally {
            if (args.verbose()) {
                msg( "ORB.init call took %d microseconds\n", timer.interval() ) ;
            }
        }

        for (int ctr=0; ctr<args.count(); ctr++ ) {
            ping( args.host(), args.port() ) ;
        }
    }
}
