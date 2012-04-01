
import HelloApp.*;           // The package containing our stubs.
import org.omg.CosNaming.*;  // HelloClient will use the naming service.
import org.omg.CORBA.*;      // All CORBA applications need these classes.


public class HelloClient
{
  public static void main(String args[])
  {
    try{
      
      // Create and initialize the ORB
      ORB orb = ORB.init(args, null);
      
      // Get the root naming context
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
      // Resolve the object reference in naming
      NameComponent nc = new NameComponent("Hello", "");
      NameComponent path[] = {nc};
      Hello helloRef = HelloHelper.narrow(ncRef.resolve(path));
      
      // Call the Hello server object and print results
      String hello = helloRef.sayHello();
      System.out.println(hello);
          
    } catch(Exception e) {
        System.out.println("ERROR : " + e);
        e.printStackTrace(System.out);
      }  
  }
}


