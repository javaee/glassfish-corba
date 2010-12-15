package orb.folb;

/**
 *
 * @author ken
 */
public interface LocationBeanRemote {
   String INSTANCE_NAME_PROPERTY = "instance_name" ;

   public String getLocation();
   public String getHostName();
   public void printSystemProperties();
}
