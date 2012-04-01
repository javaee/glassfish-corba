// HelloStringifiedServer.java, stringified object reference version

import java.io.*;
import org.omg.CORBA.*;
import HelloApp.*;


public class HelloStringifiedServer {

    public static void main(String args[])
    {
	try{
	    // create and initialize the ORB
	    ORB orb = ORB.init(args, null);

	    // create servant and register it with the ORB
	    HelloServant helloRef = new HelloServant();
	    orb.connect(helloRef);

	    // stringify the helloRef and dump it in a file
	    String str = orb.object_to_string(helloRef);
	    String filename = System.getProperty("user.home")+
	        System.getProperty("file.separator")+"HelloIOR";
	    FileOutputStream fos = new FileOutputStream(filename);
	    PrintStream ps = new PrintStream(fos);
	    ps.print(str);
	    ps.close();

	    // wait for invocations from clients
            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) {
                sync.wait();
            }

	} catch (Exception e) {
	    System.err.println("ERROR: " + e);
	    e.printStackTrace(System.out);
	}
    }
}
