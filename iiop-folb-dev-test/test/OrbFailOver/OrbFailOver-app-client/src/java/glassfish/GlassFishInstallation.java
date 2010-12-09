package glassfish;

import java.util.List;
import argparser.Pair ;
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
        boolean echoOnly ) {
        this.base = base ;
        this.installDir = installDir ;
        this.dasNodeName = dasNodeName ;
        this.availableNodes = availableNodes ;
        this.ac = new AdminCommand( base, installDir + "bin/asadmin",
            echoOnly ) ;
        start() ;
    }

    public GlassFishInstallation( Base base, String installDir,
        String dasNodeName, List<Pair<String,Integer>> availableNodes ) {
        this( base, installDir, dasNodeName, availableNodes, false ) ;
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
        checkDestroyed() ;
        checkAdminCommand( ac.startDomain() ) ;
        checkAdminCommand( ac.enableSecureAdmin() ) ;
    }

    /** Stop the domain
     *
     */
    public final void stop() {
        checkDestroyed()  ;
        checkAdminCommand( ac.stopDomain() ) ;
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
                availableNodes, true ) ;

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
