package orbfailover;

import java.util.Arrays;
import java.util.Collections;
import orb.folb.LocationBeanRemote;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.naming.InitialContext;

import testtools.Test ;
import testtools.Base ;

/**
 * @author hv51393
 * @author kcavanaugh
 */
public class Main extends Base {
    private static final String INSTANCE_NAMES_PROP = "test.folb.instances" ;
    private static final Set<String> instanceNames ;

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
                    AdminCommand.stopInstance( this, stoppedInstance );
                }

                if (i == startAt) {
                    AdminCommand.startInstance( this, stoppedInstance ) ;
                    stoppedInstance = null ;
                }
            } 
        } catch (Exception exc) {
            fail( "caught naming exception " + exc ) ;
        } finally {
            if (stoppedInstance != null) {
                AdminCommand.startInstance( this, stoppedInstance ) ;
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
    public void testLoadBalance( ) {
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
    public void test14762() {
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
    public void test14755() {
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
    @Test( "14766" )
    public void test14766() {
        try {
            final int numCalls = 20 ;
            Set<String> runningInstances = instanceNames ;
            doLoadBalance( runningInstances, numCalls )  ;

            final String inst1 = pick( runningInstances ) ;
            AdminCommand.stopInstance(this, inst1) ;
            runningInstances.remove( inst1 ) ;
            doLoadBalance( runningInstances, numCalls )  ;

            final String inst2 = pick( runningInstances ) ;
            AdminCommand.stopInstance(this, inst2) ;
            runningInstances.remove( inst2 ) ;
            final String inst3 = pick( runningInstances ) ;

            AdminCommand.startInstance(this, inst1);
            runningInstances.add( inst1 ) ;
            doLoadBalance( runningInstances, numCalls )  ;

            AdminCommand.stopInstance(this, inst3);
            runningInstances.remove( inst3 ) ;
            doLoadBalance( runningInstances, numCalls )  ;
        } finally {
            ensure() ;
        }
    }

    public void doLoadBalance( Set<String> expected, int numCalls ) {
        // XXX add checking for approximate distribution
        Map<String,Integer> counts =
            new HashMap<String,Integer>() ;

        try {
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
            int prod = 1 ;
            for (Map.Entry<String,Integer> entry : counts.entrySet()) {
                int count = entry.getValue().intValue() ;
                prod *= count ;
                note( String.format( "\tName = %20s Count = %10d",
                    entry.getKey(), count ) ) ;
            }
            check( !expected.equals(counts.keySet()),
                "Requests not loadbalanced across expected instances: "
                + " expected " + expected + ", actual " + counts.keySet() ) ;
        } catch (Exception e) {
            fail( "Exception " + e ) ;
        }
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
    public void test14732() {
        // Make sure only inst is running
        boolean first = true ;
        String running = "" ;
        for (String inst : instanceNames) {
            if (first) {
                running = inst ;
                note( "Running instance is " + inst ) ;
                first = false ;
            } else {
                AdminCommand.stopInstance(this, inst);
            }
        }

        try {
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

            AdminCommand.stopInstance( this, running ) ;
            for (String inst : instanceNames) {
                if (!inst.equals(running)) {
                    AdminCommand.startInstance( this, inst ) ;
                }
            }

            locBean = (LocationBeanRemote) ctx.lookup(beanJNDIname);
            note( "Locating EJB second time") ;

            current = locBean.getLocation();
            note( "EJB invocation returned " + current ) ;
            check( !current.equals( running ),
                "Apparent location " + current
                + " is the same as a stoppped instance " + running ) ;
        } catch (Exception exc) {
            fail( "Exception " + exc ) ;
        } finally {
            ensure() ;
        }
    }

    private void ensure() {
        // make sure all instances are running
        for (String inst : instanceNames) {
            AdminCommand.startInstance(this, inst);
        }
    }
}
