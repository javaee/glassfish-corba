package glassfish;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import testtools.Base;

/**
 *
 * @author ken
 */
public class AdminCommand {
    // Note that only one thread at a time may use an instance of AdminCommand.
    private static final String ASADMIN_CMD_PROP = "test.folb.asadmin.command" ;
    private static final String DEFAULT_ASADMIN = System.getProperty(
        ASADMIN_CMD_PROP ) ;

    private final Base base ;
    private final String asadmin ;
    private final boolean echoOnly ; // echo command only if true: do not exec
    private final List<String> commandOutput = new ArrayList<String>() ;

    public AdminCommand( Base base ) {
        this( base, DEFAULT_ASADMIN ) ;
    }

    public AdminCommand( Base base, String asadmin ) {
        this( base, asadmin, false ) ;
    }
    public AdminCommand( Base base, String asadmin, boolean echoOnly ) {
        this.base = base ;
        this.asadmin = asadmin ;
        this.echoOnly = echoOnly ;
    }

    private boolean adminCommand( String command ) {
        commandOutput.clear() ;
        base.note( "Command " + command ) ;
        final String cmd = asadmin + " " + command ;

        if (!echoOnly) {
            try {
                final Process proc = Runtime.getRuntime().exec( cmd ) ;
                final InputStream is = proc.getInputStream() ;
                final BufferedReader reader = new BufferedReader(
                    new InputStreamReader( is ) );
                String line = reader.readLine() ;
                while (line != null) {
                    base.note( line ) ;
                    commandOutput.add( line ) ;
                    line = reader.readLine() ;
                }

                final int result = proc.waitFor();
                if (result != 0) {
                    throw new RuntimeException( "Command " + command
                        + " failed with result " + result ) ;
                }
            } catch (Exception ex) {
                base.note( "Exception " + ex + " in " + cmd ) ;
                // ex.printStackTrace();
                return false ;
            }
        }

        return true ;
    }

    public List<String> commandOutput() {
        return commandOutput ;
    }

    public boolean startDomain() {
        return adminCommand( "start-domain" ) ;
    }

    public boolean stopDomain() {
        return adminCommand( "stop-domain" ) ;
    }

    public boolean enableSecureAdmin() {
        return adminCommand( "enable-secure-admin" ) ;
    }

    public boolean createCluster( String clusterName ) {
        return adminCommand( "create-cluster " + clusterName ) ;
    }

    public boolean deleteCluster( String clusterName ) {
        return adminCommand( "delete-cluster " + clusterName ) ;
    }

    public boolean startCluster( String clusterName ) {
        return adminCommand( "start-cluster " + clusterName ) ;
    }

    public boolean stopCluster( String clusterName ) {
        return adminCommand( "stop-cluster " + clusterName ) ;
    }

    public boolean createNodeSsh( String nodeHost, String installDir,
        String agentName ) {
        String str = String.format( 
            "--user admin create-node-ssh --nodehost %s --installdir %s %s", 
            nodeHost, installDir, agentName );
        return adminCommand( str ) ;
    }

    public boolean destroyNodeSsh( String agentName ) {
        return adminCommand( "destroy-ssh-node " + agentName ) ;
    }

    public boolean createInstance( String agentName, String clusterName, 
        int portBase, String instanceName ) {
        String command = String.format(
            "create-instance --node %s --cluster %s --portbase %d --checkports=true %s",
            agentName, clusterName, portBase, instanceName ) ;
        boolean result = adminCommand( command ) ;
        if (echoOnly) {
            // for testing only
            int current = portBase ;
            for (StandardPorts sport : StandardPorts.values()) {
                String msg = sport + "=" + current++ ;
                base.note( msg );
                commandOutput.add( msg ) ;
            }
        }
        return result ;
    }

    public boolean destroyInstance( String instanceName ) {
        return adminCommand( "delete-instance " + instanceName ) ;
    }

    public boolean startInstance( String name ) {
        return adminCommand( "start-instance " + name) ;
    }

    public boolean stopInstance( String name ) {
        return adminCommand( "stop-instance --force true " + name) ;
    }

    public boolean destroyCluster(String clusterName) {
        return adminCommand( "delete-cluster " + clusterName ) ;
    }
}
