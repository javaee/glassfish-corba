
// The package containing our stubs.
import HelloApp.*;

// HelloClient will use the naming service.
import org.omg.CosNaming.*;

// The package containing special exceptions thrown by the name service.
import org.omg.CosNaming.NamingContextPackage.*;

// All CORBA applications need these classes.
import org.omg.CORBA.*;

// Needed for the applet.
import java.awt.Graphics;


public class HelloApplet extends java.applet.Applet
{
  public void init()
  {
    try{

      // Create and initialize the ORB
      ORB orb = ORB.init(this, null);
      
      // Get the root naming context
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
      NamingContext ncRef = NamingContextHelper.narrow(objRef);

      // Resolve the object reference in naming
      NameComponent nc = new NameComponent("Hello", "");
      NameComponent path[] = {nc};
      Hello helloRef = HelloHelper.narrow(ncRef.resolve(path));
      
      // Call the Hello server object and print the results
      message = helloRef.sayHello();

    
    } catch(Exception e) {
        System.out.println("HelloApplet exception: " + e);
        e.printStackTrace(System.out);
      }  
  }
  String message = "";

  public void paint(Graphics g)
  {
    g.drawString(message, 25, 50);
  }

}

