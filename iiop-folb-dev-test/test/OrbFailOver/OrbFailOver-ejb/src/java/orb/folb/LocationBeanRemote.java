/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package orb.folb;

import javax.ejb.Remote;

/**
 *
 * @author hv51393
 */
@Remote
public interface LocationBeanRemote extends Location {
   public String getHostName();
   public void printSystemProperties();

}
