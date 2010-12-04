package orbfailover;

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

    private final String asadmin ;

    private final List<String> commandOutput = new ArrayList<String>() ;

    public AdminCommand() {
        this( DEFAULT_ASADMIN ) ;
    }

    public AdminCommand( String asadmin ) {
        this.asadmin = asadmin ;
    }

    private boolean adminCommand( Base base, String command, String instance ) {
        commandOutput.clear() ;
        base.note( "Command " + command + " for instance " + instance ) ;
        final String cmd = asadmin + " " + command + " " + instance ;

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
                    + " failed for instance " + instance + " with result "
                    + result ) ;
            }
        } catch (Exception ex) {
            base.note( "Exception " + ex + " in " + cmd ) ;
            // ex.printStackTrace();
            return false ;
        }

        return true ;
    }

    public List<String> commandOutput() {
        return commandOutput ;
    }

    public boolean startInstance( Base base, String name ) {
        return adminCommand( base, "start-instance", name) ;
    }

    public boolean stopInstance( Base base, String name ) {
        return adminCommand( base, "stop-instance --force true", name) ;
    }
}
