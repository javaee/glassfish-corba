package glassfish;

import argparser.Pair;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import testtools.Base;

/** Class used to create and manage a GlassFish cluster across multiple nodes.
 *
 * @author ken
 */
public class GlassFishCluster {
    private final GlassFishInstallation gfInst ;
    private final String clusterName ;
    private final Map<String,InstanceInfo> instanceInfoMap ;
    private final Set<String> runningInstances ;

    public GlassFishCluster( GlassFishInstallation gfInst, String clusterName ) {
        this.clusterName = clusterName ;
        this.gfInst = gfInst ;
        this.instanceInfoMap = new HashMap<String,InstanceInfo>() ;
        this.runningInstances = new HashSet<String>() ;

        gfInst.ac().createCluster( clusterName ) ;
        for (Pair<String,Integer> pair : gfInst.availableNodes()) {
            String node = pair.first() ;
            gfInst.ac().createNodeSsh( node, gfInst.installDir(),
                gfInst.getNAName(node) );
        }
    }

    public InstanceInfo createInstance( String instanceName, String nodeName,
        int portBase ) {

        gfInst.ac().createInstance( gfInst.getNAName( nodeName ), clusterName,
            portBase, instanceName ) ;
        InstanceInfo info = new InstanceInfo(instanceName, nodeName) ;
        for (String str : gfInst.ac().commandOutput() ) {
            int index = str.indexOf( '=' ) ;
            if (index > 0) {
                final String pnameString = str.substring( 0, index ) ;
                final String pnumString = str.substring( index + 1 ) ;
                StandardPorts pname = StandardPorts.valueOf(pnameString) ;
                int pnum = Integer.parseInt(pnumString) ;
                info.addPort( pname, pnum) ;
            }
        }

        instanceInfoMap.put( instanceName, info) ;
        return info ;
    }


    private static final int CREATE_INSTANCES_PORT_BASE = 9000 ;
    private static final int CREATE_INSTANCES_PORT_INCREMENT = 1000 ;

    // Worst case: run out of ports because of how this is constructed
    // and how ports are allocated on a single node.  This is 56, which
    // should be more instances than I ever node for a unit test.
    private static final int MAX_INSTANCES =
        (Short.MAX_VALUE - CREATE_INSTANCES_PORT_BASE) /
            CREATE_INSTANCES_PORT_INCREMENT ;

    /** Create a number of instances spread across the available nodes in the
     * cluster.  The name of each instance is instanceBaseName + number, for
     * a number from 0 to numInstances - 1.  numInstances must not exceed
     * the total available capacity in the GF installation as indicated by
     * the elements of gfInst.availableNodes.
     *
     * @param instanceBaseName The base name to use for all instance names.
     * @param numInstances The number of instances to create.
     */
    public Map<String,InstanceInfo> createInstances( String instanceBaseName,
        int numInstances ) {

        int numAvailable = 0 ;
        for (Pair<String,Integer> pair : gfInst.availableNodes() ) {
            numAvailable += pair.second() ;
        }

        if (numInstances > MAX_INSTANCES) {
            throw new RuntimeException( "Request number of instances "
                + numInstances + " is greater than maximum instances supported"
                + MAX_INSTANCES ) ;
        }

        if (numInstances > numAvailable) {
            throw new RuntimeException( "Request number of instances "
                + numInstances + " is greater than available instances "
                + numAvailable ) ;
        }

        WeightedCircularIterator<String> iter =
            new WeightedCircularIterator<String>() ;
        for (Pair<String,Integer> pair : gfInst.availableNodes() ) {
            iter.add( pair.first(), pair.second() ) ;
        }

        final Map<String,InstanceInfo> result =
            new HashMap<String,InstanceInfo>() ;
        for (int index=0; index<numInstances; index++) {
            final String node = iter.next() ;
            final int portBase = CREATE_INSTANCES_PORT_BASE
                + index * CREATE_INSTANCES_PORT_INCREMENT ;
            final String instanceName = instanceBaseName + index ;
            InstanceInfo info = createInstance( instanceName, node, portBase ) ;
            result.put( instanceName, info ) ;
        }

        return result ;
    }

    public void destroyInstance( String instanceName ) {
        gfInst.ac().destroyInstance(instanceName);
        instanceInfoMap.remove( instanceName ) ;
    }

    public void startInstance( String instanceName ) {
        if (instanceInfoMap.keySet().contains(instanceName)) {
            if (gfInst.ac().startInstance( instanceName )) {
                runningInstances.add( instanceName ) ;
            }
        }
    }

    public void stopInstance( String instanceName ) {
        if (instanceInfoMap.keySet().contains(instanceName)) {
            if (gfInst.ac().stopInstance( instanceName )) {
                runningInstances.remove( instanceName ) ;
            }
        }
    }

    public Set<String> runningInstances() {
        return runningInstances ;
    }

    public void startCluster() {
        if (gfInst.ac().startCluster(clusterName)) {
            runningInstances.clear() ;
            runningInstances.addAll( instanceInfoMap.keySet() ) ;
        }
    }

    public void stopCluster() {
        if (gfInst.ac().stopCluster(clusterName)) {
            runningInstances.clear() ;
        }
    }

    public void destroyCluster() {
        stopCluster() ;

        Set<String> instances = new HashSet<String>( instanceInfoMap.keySet() ) ;
        for (String instName : instances) {
            destroyInstance(instName) ;
        }

        for (Pair<String,Integer> pair : gfInst.availableNodes()) {
            String node = pair.first() ;
            gfInst.ac().destroyNodeSsh( gfInst.getNAName(node) );
        }

        gfInst.ac().destroyCluster( clusterName ) ;
    }

    public static class InstanceInfo {
        private final String name ;
        private final String node ;
        private final Map<StandardPorts,Integer> portMap ;

        public InstanceInfo( String name, String node ) {
            this.name = name ;
            this.node = node ;
            this.portMap = new EnumMap<StandardPorts,Integer>(
                StandardPorts.class) ;
        }

        String name() {
            return name ;
        }

        String node() {
            return node ;
        }

        Map<StandardPorts,Integer> ports() {
            return portMap ;
        }

        void addPort( StandardPorts pname, int pnum ) {
            portMap.put( pname, pnum ) ;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder() ;
            sb.append( "InstanceInfo[" ) ;
            sb.append( name ).append( ',' ) ;
            sb.append( node ).append( ',' ) ;
            sb.append( portMap.toString() ) ;
            sb.append( ']' ) ;
            return sb.toString() ;
        }
    }

    public Map<String,InstanceInfo> instanceInfo() {
        return null ;
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

        private static final String clusterName = "c1" ;

        private GlassFishCluster gfCluster =
            new GlassFishCluster( gfInst, clusterName ) ;

        @testtools.Test
        public void testCreateInstance() {
            InstanceInfo info = gfCluster.createInstance( "in1", "minas", 2000 ) ;
            note( "createInstance returned " + info ) ;
            gfCluster.destroyInstance( "in1" ) ;
        }

        @testtools.Test
        public void testCreateInstances() {
            Map<String,InstanceInfo> infos = gfCluster.createInstances(
                 "in", 7 ) ;
            note( "createInstances returned " + infos ) ;
            gfCluster.destroyCluster();
        }

        @testtools.Test
        public void testStartStop() {
            Map<String,InstanceInfo> infos = gfCluster.createInstances(
                 "in", 7 ) ;
            Set<String> instances = new HashSet<String>(
                infos.keySet() )  ;
            gfCluster.startCluster() ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;

            gfCluster.stopInstance( "in1" ) ;
            instances.remove( "in1" ) ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;

            gfCluster.startInstance( "in1" ) ;
            instances.add( "in1" ) ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;

            gfCluster.stopCluster() ;
            instances.clear() ;
            check( instances.equals( gfCluster.runningInstances() ),
                "Expected running instances does not match instances") ;
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
