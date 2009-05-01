/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.corba.ee.impl.orbutil.ORBUtility;
import com.sun.corba.ee.spi.transport.CorbaTransportManager;
import com.sun.corba.ee.spi.transport.MessageData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daraniramu
 */
public class TravelMuseJUnitTest {
     Properties p = new Properties();
     org.omg.CORBA.ORB orb;
     com.sun.corba.ee.spi.orb.ORB myOrb;

    public TravelMuseJUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

        p.put("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl");
        p.put("com.sun.corba.ee.ORBDebug","cdr,streamFormatVersion,valueHandler");
        orb=  com.sun.corba.ee.spi.orb.ORB.init(new String[0],p);
        myOrb = (com.sun.corba.ee.spi.orb.ORB)orb ;
        myOrb.cdrDebugFlag = true ;
        myOrb.streamFormatVersionDebugFlag = true ;
        myOrb.valueHandlerDebugFlag = true ;
    }

    @After
    public void tearDown() {

        orb.destroy();
        myOrb.destroy();
    }

   
     @Test
     public void travelMuse() throws FileNotFoundException,IOException, ClassNotFoundException {
              
        CorbaTransportManager ctm = (CorbaTransportManager) myOrb.getCorbaTransportManager() ;
        InputStream inputFile ;
        inputFile = new FileInputStream("C:/Users/daraniramu/Desktop/mtm.out");
        ObjectInputStream in = new ObjectInputStream(inputFile);
        Object baResult=in.readObject();
        byte[][] baResult1=(byte[][])baResult;
        MessageData md = ctm.getMessageData(baResult1);
        int bnum = 0 ;
	    for (byte[] data : baResult1) {
		ByteBuffer bb = ByteBuffer.wrap( data ) ;
		ORBUtility.printBuffer( "Dumping buffer " + bnum++, bb, System.out ) ;
	   }
        Object cdrstream1=javax.rmi.CORBA.Util.readAny( md.getStream());
     }

}