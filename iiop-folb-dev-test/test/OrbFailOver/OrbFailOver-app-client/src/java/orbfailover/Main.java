package orbfailover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import argparser.DefaultValue;
import argparser.Help;
import argparser.Pair;

import glassfish.AdminCommand;
import glassfish.GlassFishCluster;
import glassfish.GlassFishInstallation;
import glassfish.StandardPorts;
import java.util.HashSet;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import orb.folb.LocationBeanRemote;

import testtools.Test ;
import testtools.Base ;
import testtools.Post;

/**
 * @author hv51393
 * @author kcavanaugh
 */
public class Main extends Base {
    // @EJB
    // private static LocationBeanRemote locationBean;
    private static final String CLUSTER_NAME = "c1" ;
    private static final String EJB_NAME = "TestEJB" ;
    private static final String INSTANCE_BASE_NAME = "in" ;

    private GlassFishInstallation  gfInstallation ;
    private AdminCommand ac ;
    private GlassFishCluster gfCluster ;
    private Map<String,GlassFishCluster.InstanceInfo> clusterInfo ;

    private static final String beanJNDIname = "orb.folb.LocationBeanRemote";

    private static Pair<String,Integer> split(String str) {
        int index = str.indexOf( ':' ) ;
        final String first = str.substring( 0, index ) ;
        final String second = str.substring( index + 1 ) ;
        final int value = Integer.valueOf( second ) ;
        return new Pair<String,Integer>( first, value ) ;
    }

    private static List<Pair<String,Integer>> parseStringIntPair(
        List<String> args ) {

        List<Pair<String,Integer>> result =
            new ArrayList<Pair<String, Integer>>() ;
        for (String str : args) {
            result.add( split( str ) ) ;
        }
        return result ;
    }

    // Returns string of <host>:<clear text port> separated by commas
    // for all running instances.
    private String getIIOPEndpointList() {
        final StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for (String str : gfCluster.runningInstances()) {
            if (first) {
                first = false ;
            } else {
                sb.append( ',' ) ;
            }

            final GlassFishCluster.InstanceInfo info =
                gfCluster.instanceInfo().get( str ) ;
            final String host = info.node() ;
            final int port = info.ports().get( StandardPorts.IIOP_LISTENER_PORT ) ;
            sb.append( host ).append( ':' ).append( port ) ;
        }

        return sb.toString() ;
    }

    private InitialContext makeIC() throws NamingException {
        return makeIC( getIIOPEndpointList() ) ;
    }

    private InitialContext makeIC( String eplist ) throws NamingException {
        InitialContext result ;
        if (eplist != null) {
            final Hashtable table = new Hashtable() ;
            table.put( "com.sun.appserv.iiop.endpoints", eplist ) ;
            result = new InitialContext( table ) ;
        } else {
            result = new InitialContext() ;
        }

        note( "Created new InitialContext with endpoints " + eplist ) ;

        return result ;
    }

    private String invokeMethod( LocationBeanRemote locBean ) {
        String result = locBean.getLocation() ;
        note( "Invocation returned " + result ) ;
        return result ;
    }

    private LocationBeanRemote lookup( InitialContext ic ) throws NamingException {
        LocationBeanRemote lb = (LocationBeanRemote)ic.lookup( beanJNDIname ) ;
        note( "Looked up bean in context") ;
        return lb ;
    }

    public interface Installation {
        @DefaultValue("")
        @Help("Name of directory where GlassFish is installed")
        String installDir() ;

        @DefaultValue("")
        @Help("Name of node for GlassFish DAS")
        String dasNode() ;

        @DefaultValue("")
        @Help("A list of available nodes for cluster setup: comma separed list of name:number")
        List<String> availableNodes() ;

        @DefaultValue("")
        @Help("The test EJB needed for this test")
        String testEjb() ;

        @DefaultValue( "true" )
        @Help("Set if we want to shutdown and destroy the cluster after the test")
        boolean doCleanup() ;

        @DefaultValue( "false" )
        @Help( "Set if the cluster is already setup and running in order to avoid"
        + "extra processing")
        boolean skipSetup() ;

        @DefaultValue( "5" )
        @Help( "Set the number of instances to create in the cluster.  "
            + "There must be sufficient availability declared in availableNodes "
            + "for this many instances")
        int numInstances() ;
    }

    private final Installation inst ;

    public Main( String[] args ) {
        super( args, Installation.class ) ;
        inst = getArguments( Installation.class ) ;
        try {
            List<Pair<String,Integer>> avnodes = parseStringIntPair(
                inst.availableNodes() ) ;
            gfInstallation = new GlassFishInstallation( this,
                inst.installDir(), inst.dasNode(), avnodes, false,
                inst.skipSetup() ) ;
            ac = gfInstallation.ac() ;

            // Create default 3 instance cluster
            gfCluster = new GlassFishCluster( gfInstallation, CLUSTER_NAME,
                inst.skipSetup()) ;
            if (!inst.skipSetup()) {
                clusterInfo = gfCluster.createInstances( INSTANCE_BASE_NAME,
                    inst.numInstances() ) ;
                gfCluster.startCluster() ;
                ac.deploy( CLUSTER_NAME, EJB_NAME, inst.testEjb() ) ;
            } else {
                clusterInfo = gfCluster.instanceInfo() ;
            }
        } catch (Exception exc) {
            System.out.println( "Exception in constructor: " + exc ) ;
            exc.printStackTrace() ;
            cleanup() ;
        }
    }

    private void cleanup() {
        if (inst.doCleanup()) {
            if (ac != null) {
                ac.undeploy( CLUSTER_NAME, EJB_NAME ) ;
                ac = null ;
            }

            if (gfCluster != null) {
                gfCluster.destroyCluster() ;
                gfCluster = null ;
            }

            if (gfInstallation != null) {
                gfInstallation.destroy() ;
                gfInstallation = null ;
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main( args );
        int result = 1 ;
        if (m.gfCluster != null) {
            try {
                result = m.run() ;
            } finally {
                m.cleanup() ;
            }
        }
        System.exit( result ) ;
    }

    @Test( "listContextTest" )
    public void listContextTest() throws NamingException {
        InitialContext ic = makeIC() ;
        listContext( ic, 0 ) ;
    }

    private String indent( String str, int size ) {
        String fmt = String.format( "%%%d%%s", size ) ;
        return String.format( fmt, str ) ;
    }

    private void listContext( Context ctx, int level ) throws NamingException {
        final NamingEnumeration<NameClassPair> ne = ctx.list( "" ) ;
        final int size = 4*level ;
        while (ne.hasMore()) {
             final NameClassPair pair = ne.next() ;
             final String name = pair.getName() ;

             Object val = null ;
             try {
                val = ctx.lookup( name ) ;
             } catch (Exception exc) {
                val = "*UNAVAILABLE* (" + pair.getClassName() + ")" ;
             }

             final StringBuilder sb = new StringBuilder() ;
             if (val instanceof Context) {
                 final Context ctx2 = (Context)val ;
                 sb.append( name ).append( ':' ) ;
                 note( indent( sb.toString(), size ) ) ;
                 listContext( ctx2, level+1 ) ;
             } else {
                 sb.append( name ).append( "=>" ).append( val.toString() ) ;
                 note( indent( sb.toString(), size ) ) ;
             }
        }
    }

    @Test( "failover" )
    public void failOverTest() {
        final int numCalls = 150 ;
        final int stopAt = numCalls/3 ;
        final int startAt = 2*numCalls/3 ;
        String stoppedInstance = null ;

        boolean failOverDetected = false;
        try {
            InitialContext ctx = makeIC(null);
            LocationBeanRemote locBean = lookup( ctx ) ;
            String origLocation = invokeMethod( locBean );
            if (origLocation == null) {
                fail( "couldn't get the instance name!");
            }
            for (int i = 1; i <= numCalls; i++) {
                String newLocation = null ;
                try {
                    newLocation = invokeMethod( locBean );
                } catch (Exception e) {
                    fail( "caught invocation exception " + e );
                }

                note( "result[" + i + "]= " + newLocation);
                if (!origLocation.equals(newLocation)) {
                    failOverDetected = true;
                }

                if (i == stopAt) {
                    stoppedInstance = newLocation ;
                    ac.stopInstance( stoppedInstance );
                }

                if (i == startAt) {
                    ac.startInstance( stoppedInstance ) ;
                    stoppedInstance = null ;
                }
            } 
        } catch (Exception exc) {
            fail( "caught naming exception " + exc ) ;
        } finally {
            if (stoppedInstance != null) {
                ac.startInstance( stoppedInstance ) ;
            }
        }
    }

    private void increment( Map<String,Integer> map, String name ) {
        Integer val = map.get( name ) ;
        if (val == null) {
            val = Integer.valueOf(1) ;
        } else {
            val = Integer.valueOf( val.intValue() + 1 ) ;
        }

        map.put( name, val ) ;
    }

    @Test( "loadbalance" )
    public void testLoadBalance( ) throws NamingException {
        doLoadBalance( clusterInfo.keySet(), 100 )  ;
    }

    private <T> T pick( Set<T> set ) {
        return pick( set, false ) ;
    }

    private <T> T pick( Set<T> set, boolean remove ) {
        T result = null ;
        for (T elem : set) {
            result = elem ;
            break ;
        }

        if (remove) {
            set.remove( result ) ;
        }

        return result ;
    }

    // Test scenario for issue 14762:
    // 1. Deploy the app
    // 2. Bring down the cluster
    // 3. Start one instance (firstinstance)
    // 4. Create 10 new IC, and do ejb lookups (appclient is run with
    //    endpoints having all instances host: port info)
    // 5. Bring up all remaining instances
    // 6. Kill Instance where we created ic and did lookups
    // 7. Access business methods for the ejb's (created in step 4)
    @Test( "14762" )
    public void test14762() throws NamingException {
        for (String in : clusterInfo.keySet()) {
            ac.stopInstance(in);
        }
        final String firstInstance = pick( clusterInfo.keySet() ) ;
        ac.startInstance(firstInstance) ;
        final List<InitialContext> ics = new ArrayList<InitialContext>() ;
        final List<LocationBeanRemote> lbs = new ArrayList<LocationBeanRemote>() ;
        for (int ctr=0; ctr<10; ctr++) {
            final InitialContext ic = makeIC(null) ;
            ics.add( ic ) ;
            final LocationBeanRemote lb = lookup( ic ) ;
            lbs.add( lb ) ;
        }
        ensure() ;
        ac.stopInstance(firstInstance );
        for (LocationBeanRemote lb : lbs ) {
            String loc = invokeMethod( lb ) ;
            check( !loc.equals( firstInstance ),
                "Location returned was stopped instance " + firstInstance ) ;
        }
    }

    // Test scenario for issue 14755:
    // 1. New Initial Context
    // 2. Lookup EJB
    // 3. Call a Business method
    // 4. Find the instance that served the business method - firstinstance
    // 5. Kill that instance
    // 6. Call business method
    // 7. Now the request will be served by another instance - secondinstance
    // 8. Bring back the first instance (which we killed before)
    //    Call business method
    // 9. The request goes to firstinstance (it should go to secondinstance - since it
    //    should be sticky)
    @Test( "14755" )
    public void test14755() throws NamingException {
        InitialContext ic = makeIC(null) ;
        LocationBeanRemote lb = lookup( ic ) ;
        String first = invokeMethod( lb ) ;
        ac.stopInstance(first);
        String second = invokeMethod( lb ) ;
        check( !first.equals( second ),
            "Method executed on instance that was supposed to be down " + second ) ;
        ac.startInstance( first ) ;
        String result = invokeMethod( lb ) ;
        check( result.equals( second ),
            "Request did not stick to instance " + second +
            " after original instance " + first + " restarted" ) ;
    }

    // Test scenario for issue 14766:
    // 1. start up all instances
    // 2. start up client with target-server+ A:B:C
    // 3. do new IntialContext/lookup
    // 4. do one request - ensure it works
    // 5. shutdown A
    // 6. do one request - ensure it works
    // 7. shutdown B
    // 8. do one request - ensure it works
    // 9. restart A
    // 10 do one request - ensure it works
    // 11 shutdown C
    // 12 do one request - ensure it works
    @Test( "14766")
    public void test14766() throws NamingException {
        InitialContext ctx = makeIC(null) ;
        LocationBeanRemote locBean = lookup( ctx ) ;

        String loc1 = invokeMethod( locBean );
        ac.stopInstance(loc1);

        String loc2 = invokeMethod( locBean ) ;
        check( !loc1.equals(loc2), "Failover did not happen") ;
        ac.stopInstance(loc2);

        String loc3 = invokeMethod( locBean ) ;
        check( !loc3.equals(loc2), "Failover did not happen") ;
        ac.startInstance(loc1);

        String loc4 = invokeMethod( locBean ) ;
        check( loc4.equals(loc3), "No failover expected" ) ;
        ac.stopInstance(loc3);

        String loc5 = invokeMethod( locBean ) ;
        check( !loc5.equals(loc4), "Failover did not happen") ;
    }

    @Test( "lbfail" )
    public void testLBFail() throws NamingException {
        final int numCalls = 20 ;
        Set<String> runningInstances = clusterInfo.keySet() ;
        doLoadBalance( runningInstances, numCalls )  ;

        final String inst1 = pick( runningInstances ) ;
        ac.stopInstance(inst1) ;
        runningInstances.remove( inst1 ) ;
        doLoadBalance( runningInstances, numCalls )  ;

        final String inst2 = pick( runningInstances ) ;
        ac.stopInstance(inst2) ;
        runningInstances.remove( inst2 ) ;
        final String inst3 = pick( runningInstances ) ;

        ac.startInstance(inst1);
        runningInstances.add( inst1 ) ;
        doLoadBalance( runningInstances, numCalls )  ;

        ac.stopInstance(inst3);
        runningInstances.remove( inst3 ) ;
        doLoadBalance( runningInstances, numCalls )  ;
    }

    public void doLoadBalance( Set<String> expected,
        int numCalls ) throws NamingException  {
        doLoadBalance( expected, numCalls, getIIOPEndpointList() ) ;
    }

    public void doLoadBalance( Set<String> expected, int numCalls,
        String endpoints ) throws NamingException {
        // XXX add checking for approximate distribution
        // according to weights
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        String newLocation = "" ;
        for (int i = 1; i <= numCalls ; i++) {
            InitialContext ctx ;
            if (i == 1) {
                ctx = makeIC( endpoints ) ;
            } else {
                ctx = makeIC(null) ;
            }
            LocationBeanRemote locBean = lookup( ctx ) ;
            newLocation = invokeMethod( locBean );
            note( "result[" + i + "]= " + newLocation);
            increment( counts, newLocation ) ;
        }

        note( "Call distribution:" ) ;
        for (Map.Entry<String,Integer> entry : counts.entrySet()) {
            int count = entry.getValue().intValue() ;
            note( String.format( "\tName = %20s Count = %10d",
                entry.getKey(), count ) ) ;
        }

        check( expected.equals(counts.keySet()),
            "Requests not loadbalanced across expected instances: "
            + " expected " + expected + ", actual " + counts.keySet() ) ;
    }

    public void doLoadBalance( Set<String> expected, Set<InitialContext> ics
        ) throws NamingException {
        // XXX add checking for approximate distribution
        // according to weights
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        String newLocation = "" ;
        int i = 0 ;
        for (InitialContext ctx : ics ) {
            LocationBeanRemote locBean = lookup( ctx ) ;
            newLocation = invokeMethod( locBean );
            note( "result[" + (i++) + "]= " + newLocation);
            increment( counts, newLocation ) ;
        }

        note( "Call distribution:" ) ;
        for (Map.Entry<String,Integer> entry : counts.entrySet()) {
            int count = entry.getValue().intValue() ;
            note( String.format( "\tName = %20s Count = %10d",
                entry.getKey(), count ) ) ;
        }

        check( expected.equals(counts.keySet()),
            "Requests not loadbalanced across expected instances: "
            + " expected " + expected + ", actual " + counts.keySet() ) ;
    }

    // Test scenario for issue 14732:
    // 1. One instance of cluster is running (all remaining are stopped)
    // 2. Create Initial Context (with endpoints having all instance host and ports)
    // 3. Do EJB Lookup
    // 4. Call Business method
    // 5. Create InitialContext
    // 6. Stop Instance (the only instance)
    // 7. Start remaining instances
    // 8. Do lookup (the expectation is it will failover to other instance)
    // 9. Call Business Method
    @Test( "14732" )
    public void test14732() throws NamingException {
        // Capture full endpoint list for later use in makeIC.
        String endpoints = getIIOPEndpointList() ;

        // Make sure only inst is running
        boolean first = true ;
        String running = "" ;
        for (String inst : clusterInfo.keySet()) {
            if (first) {
                running = inst ;
                note( "Running instance is " + inst ) ;
                first = false ;
            } else {
                ac.stopInstance(inst);
            }
        }

        InitialContext ctx = makeIC( endpoints ) ;
        note( "got new initial context") ;

        LocationBeanRemote locBean = lookup( ctx ) ;
        note( "located EJB" ) ;

        String current = invokeMethod( locBean );
        note( "EJB invocation returned " + current ) ;
        check( current.equals( running ),
            "Current location " + current + " is not the same as the"
                + " running location " + running ) ;

        ac.stopInstance( running ) ;
        for (String inst : clusterInfo.keySet()) {
            if (!inst.equals(running)) {
                ac.startInstance( inst ) ;
            }
        }

        locBean = lookup( ctx ) ;
        note( "Locating EJB second time") ;

        current = invokeMethod( locBean );
        note( "EJB invocation returned " + current ) ;
        check( !current.equals( running ),
            "Apparent location " + current
            + " is the same as a stoppped instance " + running ) ;
}

    // Test scenario for issue 14867:
    // 1. Deploy an application to a cluster with three instances
    // 2. Start up one client with target-server+ specifying the three instances.
    // 3. In a loop (100 iterations):
    //    1. Create a new InitialContext
    //    2. Do a lookup
    //    3. Remember which instance the lookup went to
    // 4. Add two new instances
    // 5. Then do 100 new IntialContext/lookup.
    // New instances should process some of new requests
    @Test( "14867" )
    public void test14867() throws NamingException {
        Set<String> instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        check( instances.size() >= 5, "Cluster must contain at least 5 instances") ;

        final String gfInstance1 = pick( instances, true ) ;
        final String gfInstance2 = pick( instances, true ) ;
        final String gfInstance3 = pick( instances, true ) ;
        final String gfInstance4 = pick( instances, true ) ;
        final String gfInstance5 = pick( instances, true ) ;
        gfCluster.stopCluster() ;

        gfCluster.startInstance( gfInstance1 );
        gfCluster.startInstance( gfInstance2 );
        gfCluster.startInstance( gfInstance3 );
        final String endpoints = getIIOPEndpointList() ;

        final int COUNT = 100 ;
        instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        doLoadBalance( instances, COUNT, endpoints ) ;

        gfCluster.startInstance( gfInstance4 );
        gfCluster.startInstance( gfInstance5 );

        instances = gfCluster.runningInstances() ;
        note( "Running instances = " + instances ) ;
        doLoadBalance( instances, COUNT, endpoints ) ;
    }


    // Test scenario for issue 15100:
    // 1. App is deployed on a 3 instance cluster
    // 2. Send 100 New initial Contexts, approx 30 are created on instance 1
    // 3. Bring down instance 1
    // 4. Call business methods using the IC , the 30 requests are failed
    //    over to only one instance
    // 5. The expectation is, the failoved requests should be distributed
    //    among healthy instances (15 each approx)
    @Test( "15100")
    public void test15100() throws NamingException {
        final int CLUSTER_SIZE = 3 ;
        final int NUM_IC = 100 ;
        Set<String> clusterInstances = new HashSet<String>() ;
        Set<String> instances = gfCluster.runningInstances() ;
        for (int ctr=0; ctr<CLUSTER_SIZE; ctr++) {
            clusterInstances.add( pick(instances, true) ) ;
        }
        check( clusterInstances.size() == 3, "Not enough instances to run test" ) ;

        gfCluster.stopCluster() ;
        for (String inst : clusterInstances) {
            gfCluster.startInstance(inst);
        }

        String shutdownInst = pick( clusterInstances ) ;
        Set<InitialContext> chosen = new HashSet<InitialContext>() ;

        for (int ctr=0; ctr<NUM_IC; ctr++ ) {
            InitialContext ic = makeIC(null) ;
            LocationBeanRemote locBean = lookup(ic) ;
            String runningInstance = invokeMethod( locBean ) ;
            if (runningInstance.equals( shutdownInst )) {
                chosen.add( ic ) ;
            }
        }

        ac.stopInstance( shutdownInst ) ;
        doLoadBalance( gfCluster.runningInstances(), chosen ) ;
    }

    @Post
    private void ensure() {
        // make sure all instances are running
        gfCluster.startCluster();
    }
}
