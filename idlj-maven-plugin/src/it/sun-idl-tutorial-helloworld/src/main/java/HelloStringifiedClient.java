// HelloStringifiedClient.java, stringified object reference version

import java.io.*;
import org.omg.CORBA.*;
import HelloApp.*;

public class HelloStringifiedClient 
{
    public static void main(String args[])
    {
	try{
	    // create and initialize the ORB
	    ORB orb = ORB.init(args, null);

	    // Get the stringified object reference and destringify it.
	    String filename = System.getProperty("user.home")+
	        System.getProperty("file.separator")+"HelloIOR";
	    BufferedReader br = new BufferedReader(new FileReader(filename));
            String ior = br.readLine();
            org.omg.CORBA.Object obj = orb.string_to_object(ior);

            Hello helloRef = HelloHelper.narrow(obj);

	    // call the Hello server object and print results
	    String hello = helloRef.sayHello();
	    System.out.println(hello);

	} catch (Exception e) {
	    System.out.println("ERROR : " + e) ;
	    e.printStackTrace(System.out);
	}
    }
}
