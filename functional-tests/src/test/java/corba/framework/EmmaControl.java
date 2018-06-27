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

package corba.framework;

import java.util.Properties ;

// import com.vladium.emma.ctl.ctlCommand ;

/** Programmatic API for use in controlling emma.  This is
 * specific to emma version 2.1.  This is intended for use 
 * in a test harness that execs test programs.  Note that 
 * the execed Java program must be started with the following
 * properties:
 * <ul>
 * <li>emma.rt.control true (the default)
 * <li>emma.rt.control.host localhost (the default)
 * <li>emma.rt.control.port somePort (defaults to 47653)
 * </ul>
 * The port should be one obtained from allocatePort, but that is
 * not too important.
 * Also note that if several programs are execed, each must 
 * have a unique port.  This is yet another CORBA test framework
 * feature that prevents more than one concurrent test run per
 * machine.
 */
public class EmmaControl {
    private EmmaControl() {}

    private static final int FIRST_PORT = 47000 ;

    private static int nextPort = FIRST_PORT ;
    private static boolean DEBUG = true ;

    /** Allocate a port to be used for emma, in the writeCoverageData
     * method.
     */
    private synchronized static int allocatePort() {
        return nextPort++ ;
    }

    public synchronized static void resetPortAllocator() {
        nextPort = FIRST_PORT ;
    }

    public static int setCoverageProperties( Properties props ) {
        // Allow for both automatic and controlled output of coverage data.
        props.setProperty( "emma.coverage.out.file", Options.getEmmaFile() ) ;
        props.setProperty( "emma.coverage.out.merge", "true" ) ;
        props.setProperty( "emma.rt.control", "true" ) ;
        props.setProperty( "emma.rt.control.host", "localhost" ) ;
        int result = allocatePort() ;
        props.setProperty( "emma.rt.control.port", "" + result ) ;
        return result ;
    }

    /** Tell emma to dump the coverage data for the process listening
     * for emma command on port to the given fileName.  fileName is
     * interpreted on the client side and the new coverage data is 
     * merged into the existing file.  Emma will not dump on process
     * exit after this method is used.
     */
    public static void writeCoverageData( int port, String fileName ) {
        String[] args = new String[] {
                "-connect",
                "localhost:" + port,
                "-command", 
                "coverage.dump," + fileName + ",true,true"
            } ;

        /*
        if (DEBUG) {
            System.out.print( "Executing emma ctl command with args:" ) ;
            for (String arg : args) 
                System.out.print( " " + arg ) ;
            System.out.println() ;
        }

        ctlCommand cmd = new ctlCommand( "ctl", args ) ;
        cmd.run() ;
        */

        String command = "java emma ctl" ;
        for (String arg : args)
            command += " " + arg ;
        try {
            Runtime.getRuntime().exec( command ) ;
        } catch (Exception exc) {
            System.out.println( "Error in executing emma ctl command" + exc ) ;
            exc.printStackTrace() ;
        }
    }
}
