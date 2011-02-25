/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package orb.folb;

import javax.ejb.Stateful;

/**
 *
 * @author ken
 */
@Stateful
public class StatefullLocationBean implements StatefullLocationBeanRemote {
    @Override
    public String getLocation() {
        try {
            String instanceName = System.getProperty("com.sun.aas.instanceName");
            return instanceName;
        } catch (Exception e) {
            return null;
        }
    }

}
