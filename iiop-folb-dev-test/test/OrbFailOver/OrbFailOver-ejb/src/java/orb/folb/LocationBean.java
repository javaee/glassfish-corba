/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package orb.folb;

import javax.ejb.Stateless;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Enumeration;

/**
 *
 * @author hv51393
 */
@Stateless
public class LocationBean implements LocationBeanRemote {

    public String getLocation() {

        try {
            String instanceName = System.getProperty("instance_name");
            return instanceName;
        } catch (Exception e) {
            return null;
        }
    }

    public String getHostName() {

        try {
            String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            return hostName ;
        } catch (Exception e) {
            return null;
        }
    }

    public void printSystemProperties() {
        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) p.get(key);
            System.out.println(key + ": " + value);
        }
    }
}
