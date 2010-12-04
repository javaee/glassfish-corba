package orbfailover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.naming.NamingException;
import orb.folb.LocationBeanRemote;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.InitialContext;

import testtools.Test ;
import testtools.Base ;
import testtools.Post;

/**
 * @author hv51393
 * @author kcavanaugh
 */
public class Main extends Base {
    private static final String INSTANCE_NAMES_PROP = "test.folb.instances" ;
    private static final Set<String> instanceNames ;
    private static final AdminCommand ac = new AdminCommand() ;

    // @EJB
    // private static LocationBeanRemote locBean;
    private static final String beanJNDIname = "orb.folb.LocationBeanRemote";


    public Main( String[] args ) {
        super( args ) ;
    }

    static {
        Set<String> temp = new HashSet<String>() ;
        final String instances = System.getProperty( INSTANCE_NAMES_PROP ) ;
        if (instances != null) {
            temp.addAll(Arrays.asList(instances.split(",")));
        }
        instanceNames = Collections.unmodifiableSet(temp) ;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main( args );
        m.run() ;
    }

    @Test( "failover" )
    public void failOverTest() {
        final int numCalls = 150 ;
        final int stopAt = numCalls/3 ;
        final int startAt = 2*numCalls/3 ;
        String stoppedInstance = null ;

        boolean failOverDetected = false;
        try {
            InitialContext ctx = new InitialContext();
            LocationBeanRemote locBean =
                    (LocationBeanRemote) ctx.lookup(beanJNDIname);
            String origLocation = locBean.getLocation();
            if (origLocation == null) {
                fail( "couldn't get the instance name!");
            }
            for (int i = 1; i <= numCalls; i++) {
                String newLocation = null ;
                try {
                    newLocation = locBean.getLocation();
                } catch (Exception e) {
                    fail( "caught invocation exception " + e );
                }

                note( "result[" + i + "]= " + newLocation);
                if (!origLocation.equals(newLocation)) {
                    failOverDetected = true;
                }

                if (i == stopAt) {
                    stoppedInstance = newLocation ;
                    ac.stopInstance( this, stoppedInstance );
                }

                if (i == startAt) {
                    ac.startInstance( this, stoppedInstance ) ;
                    stoppedInstance = null ;
                }
            } 
        } catch (Exception exc) {
            fail( "caught naming exception " + exc ) ;
        } finally {
            if (stoppedInstance != null) {
                ac.startInstance( this, stoppedInstance ) ;
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
        doLoadBalance( instanceNames, 100 )  ;
    }

    private <T> T pick( Set<T> set ) {
        for (T elem : set) {
            return elem ;
        }

        return null ;
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
        for (String in : instanceNames) {
            ac.stopInstance(this, in);
        }
        final String firstInstance = pick( instanceNames ) ;
        ac.startInstance(this, firstInstance) ;
        final List<InitialContext> ics = new ArrayList<InitialContext>() ;
        final List<LocationBeanRemote> lbs = new ArrayList<LocationBeanRemote>() ;
        for (int ctr=0; ctr<10; ctr++) {
            final InitialContext ic = new InitialContext() ;
            ics.add( ic ) ;
            final LocationBeanRemote lb = (LocationBeanRemote)ic.lookup( beanJNDIname) ;
            lbs.add( lb ) ;
        }
        ensure() ;
        ac.stopInstance(this, firstInstance );
        for (LocationBeanRemote lb : lbs ) {
            String loc = lb.getLocation() ;
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
        InitialContext ic = new InitialContext() ;
        LocationBeanRemote lb = (LocationBeanRemote)ic.lookup( beanJNDIname ) ;
        String first = lb.getLocation() ;
        ac.stopInstance(this, first);
        String second = lb.getLocation() ;
        check( !first.equals( second ),
            "Method executed on instance that was supposed to be down " + second ) ;
        ac.startInstance( this, first ) ;
        String result = lb.getLocation() ;
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
        InitialContext ctx = new InitialContext();
        LocationBeanRemote locBean = (LocationBeanRemote) ctx.lookup(
            beanJNDIname);

        String loc1 = locBean.getLocation();
        ac.stopInstance(this, loc1);

        String loc2 = locBean.getLocation() ;
        check( !loc1.equals(loc2), "Failover did not happen") ;
        ac.stopInstance(this, loc2);

        String loc3 = locBean.getLocation() ;
        check( !loc3.equals(loc2), "Failover did not happen") ;
        ac.startInstance(this, loc1);

        String loc4 = locBean.getLocation() ;
        check( loc4.equals(loc3), "No failover expected" ) ;
        ac.stopInstance(this, loc3);

        String loc5 = locBean.getLocation() ;
        check( !loc5.equals(loc4), "Failover did not happen") ;
    }

    @Test( "lbfail" )
    public void testLBFail() throws NamingException {
        final int numCalls = 20 ;
        Set<String> runningInstances = instanceNames ;
        doLoadBalance( runningInstances, numCalls )  ;

        final String inst1 = pick( runningInstances ) ;
        ac.stopInstance(this, inst1) ;
        runningInstances.remove( inst1 ) ;
        doLoadBalance( runningInstances, numCalls )  ;

        final String inst2 = pick( runningInstances ) ;
        ac.stopInstance(this, inst2) ;
        runningInstances.remove( inst2 ) ;
        final String inst3 = pick( runningInstances ) ;

        ac.startInstance(this, inst1);
        runningInstances.add( inst1 ) ;
        doLoadBalance( runningInstances, numCalls )  ;

        ac.stopInstance(this, inst3);
        runningInstances.remove( inst3 ) ;
        doLoadBalance( runningInstances, numCalls )  ;
    }

    public void doLoadBalance( Set<String> expected, int numCalls ) 
        throws NamingException {
        // XXX add checking for approximate distribution
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        String newLocation = "" ;
        for (int i = 1; i <= numCalls ; i++) {
            InitialContext ctx = new InitialContext();
            LocationBeanRemote locBean =
                    (LocationBeanRemote) ctx.lookup(beanJNDIname);
            newLocation = locBean.getLocation();
            note( "result[" + i + "]= " + newLocation);
            increment( counts, newLocation ) ;
        }
        note( "Call distribution:" ) ;
        for (Map.Entry<String,Integer> entry : counts.entrySet()) {
            int count = entry.getValue().intValue() ;
            note( String.format( "\tName = %20s Count = %10d",
                entry.getKey(), count ) ) ;
        }
        check( !expected.equals(counts.keySet()),
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
        // Make sure only inst is running
        boolean first = true ;
        String running = "" ;
        for (String inst : instanceNames) {
            if (first) {
                running = inst ;
                note( "Running instance is " + inst ) ;
                first = false ;
            } else {
                ac.stopInstance(this, inst);
            }
        }

        InitialContext ctx = new InitialContext();
        note( "got new initial context") ;

        LocationBeanRemote locBean =
                (LocationBeanRemote) ctx.lookup(beanJNDIname);
        note( "located EJB" ) ;

        String current = locBean.getLocation();
        note( "EJB invocation returned " + current ) ;
        check( current.equals( running ),
            "Current location " + current + " is not the same as the"
                + " running location " + running ) ;

        ac.stopInstance( this, running ) ;
        for (String inst : instanceNames) {
            if (!inst.equals(running)) {
                ac.startInstance( this, inst ) ;
            }
        }

        locBean = (LocationBeanRemote) ctx.lookup(beanJNDIname);
        note( "Locating EJB second time") ;

        current = locBean.getLocation();
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
    public void test14867() {
    }

    @Post
    private void ensure() {
        // make sure all instances are running
        for (String inst : instanceNames) {
            ac.startInstance(this, inst);
        }
    }
}
