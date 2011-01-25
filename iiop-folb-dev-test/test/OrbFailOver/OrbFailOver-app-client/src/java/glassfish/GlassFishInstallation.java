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
package glassfish;

import java.util.List;
import argparser.Pair ;
import java.io.File;
import java.util.ArrayList;
import testtools.Base;

/**
 *
 * @author ken
 */
public class GlassFishInstallation {
    private final Base base ;
    private final String installDir ;
    private final String dasNodeName ;
    private final List<? extends Pair<String,Integer>> availableNodes ;
    private final AdminCommand ac ;
    private boolean isDestroyed = false ;
    private boolean skipSetup ;

    public String installDir() {
        return installDir ;
    }

    public String dasNodeName() {
        return dasNodeName ;
    }

    public List<? extends Pair<String,Integer>> availableNodes() {
        return availableNodes ;
    }

    public String getNAName( String name ) {
        return name + "NA" ;
    }

    public AdminCommand ac() {
        return ac ;
    }

    /** Create a GlassFish installation and all of the node agents needed.
     * After the construction completes, the DAS and node agents are running,
     * but no instances have been created.
     *
     * @param installDir Installation directory for GF (contains bin).
     * @param dasNodeName Name of node used for DAS.
     * @param availableNodes List of node name, max supported instances for each
     * available node (may include DAS node).
     */
    public GlassFishInstallation( Base base, String installDir,
        String dasNodeName, List<? extends Pair<String,Integer>> availableNodes,
        boolean echoOnly, boolean skipSetup ) {
        this.base = base ;
        this.installDir = installDir ;
        if (!installDir.endsWith( File.separator )) {
             installDir += File.separator ;
        }
        this.dasNodeName = dasNodeName ;
        this.availableNodes = availableNodes ;
        this.ac = new AdminCommand( base, installDir + "glassfish/bin/asadmin",
            echoOnly ) ;
        this.skipSetup = skipSetup ;
        start() ;
    }

    public GlassFishInstallation( Base base, String installDir,
        String dasNodeName, List<Pair<String,Integer>> availableNodes ) {
        this( base, installDir, dasNodeName, availableNodes, false, false ) ;
    }

    private void checkDestroyed() {
        if (isDestroyed) {
            throw new RuntimeException( 
                "GlassFish installation has been destroyed") ;
        }
    }

    private void checkAdminCommand( boolean result ) {
        if (!result) {
            throw new RuntimeException( "Admin command failed" ) ;
        }
    }

    /** Start the domain and enable secure mode
     *
     */
    public final void start() {
        if (!skipSetup) {
            checkDestroyed() ;
            checkAdminCommand( ac.startDomain() ) ;
        }
    }

    /** Stop the domain
     *
     */
    public final void stop() {
        if (!skipSetup) {
            checkDestroyed()  ;
            checkAdminCommand( ac.stopDomain() ) ;
        }
    }

    public final void destroy() {
        stop() ;
        isDestroyed = true ;
    }

    public static class Test extends Base {
        private static final String installDir = 
            "/volumes/work/GlassFish/v3/glassfishv3/glassfish" ;
        private static final String dasNodeName = "minas" ;
        private static final List<Pair<String,Integer>> availableNodes =
            new ArrayList<Pair<String,Integer>>() ;

        static {
            availableNodes.add( new Pair<String,Integer>( "minas", 3 ) ) ;
            availableNodes.add( new Pair<String,Integer>( "hermes", 2 ) ) ;
            availableNodes.add( new Pair<String,Integer>( "apollo", 4 ) ) ;
        }

        private GlassFishInstallation gfInst = 
            new GlassFishInstallation( this, installDir, dasNodeName, 
                availableNodes, true, false ) ;

        @testtools.Test
        public void test() {
            gfInst.stop() ;
            gfInst.start() ;
            gfInst.destroy() ;
        }

        public Test( String[] args ) {
            super( args ) ;
        }
    }

    public static void main( String[] args ) {
        Test test = new Test( args ) ;
        test.run() ;
    }
}
