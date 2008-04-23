/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1998-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;

import test.Util;
import test.Test;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import sun.rmi.rmic.iiop.Constants;
import sun.rmi.rmic.iiop.CompoundType;
import sun.rmi.rmic.iiop.ContextStack;
import sun.tools.java.ClassPath;

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

public class InnerTest extends Test implements Constants {

    private ByteArrayOutputStream out = null;
    private TestEnv env = null;
    private ContextStack stack = null;

    /*
      Outer                   Inner                   Name
      ---------------------   ---------------------   ----
      Remote interface        Remote interface        RR
      Remote interface        interface               RI
      Remote interface        Value                   RV
      Remote interface        Servant                 RS
      Remote interface        Abstract interface      RA
 
      interface               Remote interface        IR
      interface               interface               II
      interface               Value                   IV
      interface               Servant                 IS
      interface               Abstract interface      IA

      Value                   Remote interface        VR
      Value                   interface               VI
      Value                   Value                   VV
      Value                   Servant                 VS
      Value                   Abstract interface      VA

      Servant                 Remote interface        SR
      Servant                 interface               SI
      Servant                 Value                   SV
      Servant                 Servant                 SS
      Servant                 Abstract interface      SA

      Abstract interface      Remote interface        AR
      Abstract interface      interface               AI
      Abstract interface      Value                   AV
      Abstract interface      Servant                 AS
      Abstract interface      Abstract interface      AA
    */
    private static final String[] CASES = { 
	"RR","RI","RV","RS","RA",
	"IR","II","IV","IS","IA",
	"VR","VI","VV","VS","VA",//"SR",
	"SI","SV","SS","SA",
	"AR","AI","AV","AS","AA",
    };
    private int typeCode(char c) {
        switch (c) {
	case 'R': return TYPE_REMOTE;
	case 'I': return TYPE_NC_INTERFACE;
	case 'V': return TYPE_VALUE;
	case 'S': return TYPE_IMPLEMENTATION;
	case 'A': return TYPE_ABSTRACT;
	default: throw new Error("Unkown type.");
        }
    }
    
    private void checkType(String className, char typeInitial) {
        env.reset();
        int typeCode = typeCode(typeInitial);
        CompoundType type = (CompoundType) MapType.getType(className,stack);
        
        if (type == null) {
            throw new Error(type + " is null");
        }

        if (!type.isType(typeCode)) {
            throw new Error(type + " is not expected type. Found " + type.getTypeDescription());
        }
    }
    
    
    /**
     * Run the test.
     */
    public void run () {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() ) ;

        try {
    
            out = new ByteArrayOutputStream();
            env = new TestEnv(rmic.ParseTest.createClassPath(),out);
            stack = new ContextStack(env);

            // Do the tests...
            for (int i = 0; i < CASES.length; i++) {
                helper.start( "test_" + CASES[i] ) ;
                String outerClass = "rmic." + CASES[i];   
                checkType(outerClass,CASES[i].charAt(0));
                checkType(outerClass + ".Inner",CASES[i].charAt(1));
                checkType(outerClass + ".Inner",CASES[i].charAt(1));
                helper.pass() ;
            }
    
            env.shutdown();

        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            helper.fail( e ) ;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            status = new Error("Caught " + out.toString());
        } finally {
            helper.done() ;
        }
    }
}

// Remote Outer...

interface RR extends Remote {
    public String hello () throws RemoteException;
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

interface RI extends Remote {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

interface RV extends Remote {
    public String hello () throws RemoteException;
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

interface RS extends Remote {
    public String hello () throws RemoteException;
    public class Inner implements RS {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
}
public static class Nested implements RS {
    public Nested () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
}
}

interface RA extends Remote {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}

// Interface Outer...

interface IR {
    public String hello ();
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

interface II {
    public String hello ();
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

interface IV {
    public String hello ();
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

interface IS {
    public String hello ();
    public class Inner implements RS {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
}
public static class Nested implements RS {
    public Nested () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
}
}

interface IA {
    public String hello ();
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}


// Value Outer...

class VR implements Serializable {
    public String hello;
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

class VI implements Serializable {
    public String hello;
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

class VV implements Serializable {
    public String hello;
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

class VS implements Serializable {
    public String hello;
    public class Inner implements RR {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
    public static class Nested implements RR {
        public Nested () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
}

class VA implements Serializable {
    public String hello;
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}

// Servant Outer...

class SR implements SRInner {
    public SR () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

class SI implements RR {
    public SI () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

class SV implements RR {
    public SV () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    
    // Note: Changed to static to avoid error caused by
    // rmic.SV data member and constructor...
    
    static public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

class SS implements RR {
    public SS () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public class Inner implements RR {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
    public static class Nested implements RR {
        public Nested () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
}

class SA implements RR {
    public SA () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}


// Abstract Outer...

interface AR {
    public String hello () throws RemoteException;
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

interface AI {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

interface AV {
    public String hello () throws RemoteException;
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

interface AS {
    public String hello () throws RemoteException;
    public class Inner implements RS {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
    public static class Nested implements RS {
        public Nested () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
}

interface AA {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}
