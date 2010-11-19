package orbfailover;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import testtools.Base;

/**
 *
 * @author ken
 */
public class AdminCommand {
    private AdminCommand() {}

    private static final String ASADMIN_CMD_PROP = "test.folb.asadmin.command" ;

    private static boolean adminCommand( Base base, String command, String instance ) {
        base.note( "Command " + command + " for instance " + instance ) ;
        final String asadmin = System.getProperty(ASADMIN_CMD_PROP) ;
        if (asadmin == null) {
            base.note( "Could not find property " + ASADMIN_CMD_PROP ) ;
            return false ;
        }

        final String cmd = asadmin + " " + command + " " + instance ;

        try {
            final Process proc = Runtime.getRuntime().exec( cmd ) ;
            final InputStream is = proc.getInputStream() ;
            final BufferedReader reader = new BufferedReader(
                new InputStreamReader( is ) );
            String line = reader.readLine() ;
            while (line != null) {
                base.note( line ) ;
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

    public static boolean startInstance( Base base, String name ) {
        return adminCommand( base, "start-instance", name) ;
    }

    public static boolean stopInstance( Base base, String name ) {
        return adminCommand( base, "stop-instance --force true", name) ;
    }
}
