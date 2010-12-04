package orbfailover;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Class used to create and manage a GlassFish cluster across multiple nodes.
 *
 * @author ken
 */
public class GlassFishCluster {
    private final GlassFishInstallation gfInst ;
    private final String clusterName ;
    private final Map<String,InstanceInfo> instanceInfoMap ;

    public GlassFishCluster( GlassFishInstallation gfInst, String clusterName ) {
        this.clusterName = clusterName ;
        this.gfInst = gfInst ;
        this.instanceInfoMap = new HashMap<String,InstanceInfo>() ;
    }

    public void createInstance( String instanceName, String naName ) {
    }

    public void destroyInstance( String instanceName ) {
    }

    public void startInstance( String instanceName ) {
    }

    public void stopInstance( String instanceName ) {
    }

    public Set<String> runningInstances() {
        return null ;
    }

    public void stopCluster() {
    }

    public void destroyCluster() {
    }

    // The standard ports GlassFish uses.  The create-instance command returns
    // these in the form (NAME)=nnnn.
    public enum StandardPorts {
        HTTP_LISTENER_PORT,
        HTTP_SSL_LISTENER_PORT,
        IIOP_LISTENER_PORT,
        IIOP_SSL_LISTENER_PORT,
        IIOP_SSL_MUTUALAUTH_LISTENER_PORT,
        JMX_SYSTEM_CONNECTOR_PORT,
        JMS_PROVIDER_PORT,
        ASADMIN_LISTENER_PORT,
        GMS_LISTENER_PORT ;
    } ;

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
    }

    public Map<String,InstanceInfo> instanceInfo() {
        return null ;
    }
}
