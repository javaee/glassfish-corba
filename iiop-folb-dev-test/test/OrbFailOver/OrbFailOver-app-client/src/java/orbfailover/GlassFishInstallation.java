package orbfailover;

import java.util.List;
import argparser.Pair ;
import java.util.ArrayList;

/**
 *
 * @author ken
 */
public class GlassFishInstallation {
    private final String installDir ;
    private final String dasNodeName ;
    private final List<Pair<String,Integer>> availableNodes ;
    private final AdminCommand ac ;
    private boolean isDestroyed = false ;

    public String dasNodeName() {
        return dasNodeName ;
    }

    public List<Pair<String,Integer>> availableNodes() {
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
    public GlassFishInstallation( String installDir, String dasNodeName,
        List<Pair<String,Integer>> availableNodes ) {
        this.installDir = installDir ;
        this.dasNodeName = dasNodeName ;
        this.availableNodes = new ArrayList<Pair<String,Integer>>() ;
        this.ac = new AdminCommand( installDir + "bin/asadmin" ) ;
    }

    private void checkDestroyed() {
        if (isDestroyed) {
            throw new RuntimeException( 
                "GlassFish installation has been destroyed") ;
        }
    }
    /** Start the domain and enable secure mode
     *
     */
    public void start() {
        checkDestroyed()  ;
    }

    /** Stop the domain
     *
     */
    public void stop() {
        checkDestroyed()  ;
    }

    public void destroy() {
        isDestroyed = true ;
    }

}
