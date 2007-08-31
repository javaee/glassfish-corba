/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework;

import test.Test;
import java.io.*;
import java.util.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

/**
 * Provides a global access point for the huge amount of information
 * necessary to run the tests.  Simple
 * implementation (not a delegate or instance() style singleton) of
 * just statics. 
 * <P>
 * For some options, this will check environment variables first, before 
 * using the defaults.
 * <P>
 * Changing the output directory is disallowed.  It is set to the
 * value of the output directory given as a command line parameter
 * to the main test engine PLUS the current test's package name.
 * That command line output directory should also be in the classpath
 * before running the test framework (this is done by our makefiles).
 * Changing is disallowed because of the classpath issue, and also
 * because several other values depend on the output directory.  You
 * CAN do it, but you must take responsibility for updating things
 * that depended on it.
 *
 * <P>
 * If specifying the ioser library is required, this must be done with an
 * environment variable called LD_LIBRARY_PATH to the framework.  Otherwise, it is
 * set to null (which means it won't be passed to subprocesses).
 */
public class Options
{
    // Defaults and names
    private static final String defJavaIDLHomeDirName  
        = "JavaIDLHome";
    private static final String defActivationDbDirName 
        = "orb.db";
    private static final String defORBInitialPortString 
        = "org.omg.CORBA.ORBInitialPort";

    private static final String defActivationPortString = "ActivationPort";
    private static final String defOrbdMainClass    = 
        "com.sun.corba.se.impl.activation.ORBD";

    private static final String persistentServerIdProperty
        = ORBConstants.ORB_SERVER_ID_PROPERTY;

    // Provided as a String[] for convenience here
    private static final String[] defOrbdArgs = {"-ORBDebug", "orbd" };

    private static final String defNamingFactory = 
        "com.sun.jndi.cosnaming.CNCtxFactory";
    public static final String defORBDHandshake = "ORBD is ready.";
    public static final String defServerHandshake = "Server is ready.";

    private static final String defORBClass = "com.sun.corba.se.impl.orb.ORBImpl" ;

    private static final String defORBSingleton 
        = "com.sun.corba.se.impl.orb.ORBSingleton";

    private static long defMaximumTimeout = 120000;

    /**
     * Actual vars
     */

    // Port related:
    private static Port orbInitialPort;
    private static Port activationPort;
    private static String namingProviderURL;

    private static String javaIDLHome;
    private static String activationDbDirName;
    private static String ioserPath;
    private static String orbdMainClass;
    private static String namingFactory;
    private static String classpath;
    private static String orbClass;
    private static String orbSingleton;
    private static String javaExecutable;

    private static String giopVersion = null;
    private static String giopFragmentSize = null;
    private static String giopBufferSize = null;
    private static String giop11BufMgr = null;
    private static String giop12BufMgr = null;

    private static String persistentServerId = null;

    private static Vector orbdArgs = new Vector(10);
    private static Vector serverArgs = new Vector(10);
    private static Vector clientArgs = new Vector(10);

    // Extra execution strategy arguments
    private static Hashtable orbdExtra = new Hashtable(10);
    private static Hashtable serverExtra = new Hashtable(10);
    private static Hashtable clientExtra = new Hashtable(10);

    // Extra arguments to compilers
    private static Vector rmicArgs = new Vector(10);
    private static Vector idlCompilerArgs = new Vector(10);
    private static Vector javacArgs = new Vector(10);

    // Extra environment properties
    private static Properties extraORBDProps = new Properties();
    private static Properties extraServerProps = new Properties();
    private static Properties extraClientProps = new Properties();

    private static String packageName;
    private static String emmaFile;

    // Maximum timeout (mainly used for the handshake)
    private static long maximumTimeout;

    // Note:  Directories must have the file separator already appeneded 
    //        to the end
    private static String testDirectory;
    private static String reportDirectory;
    private static String outputDirectory;
    private static String buildDirectory;

    // Files for compilation
    private static String[] javaFiles;
    private static String[] idlFiles;
    private static String[] rmicFiles;

    // Initiator (used to get output directory)
    private CORBATest parent;

    /**
     * Initialize the options.  This should be called by the 
     * test framework, not individual tests.  It should be called
     * before each new test runs to reset everything.
     *
     *@param  parent   The current test
     */
    public static void init(CORBATest parent) throws IOException
    {
        javaFiles = null;
        idlFiles = null;
        rmicFiles = null;

        parent = parent;

        activationDbDirName = defActivationDbDirName;

	orbdExtra.clear();
	orbdExtra.put(ExternalExec.HANDSHAKE_KEY, defORBDHandshake);

	serverExtra.clear();
	serverExtra.put(ExternalExec.HANDSHAKE_KEY, defServerHandshake);

        clientExtra.clear();

        orbdArgs.clear();
        serverArgs.clear();
        clientArgs.clear();

        maximumTimeout = defMaximumTimeout;

        idlCompilerArgs.clear();
        javacArgs.clear();
        rmicArgs.clear();

        /**
         * If an environment variable called LD_LIBRARY_PATH isn't set, it is set to
         * null here.  No value will be passed to subprocesses.
         */
        ioserPath = 
            Options.mostRecentValue("LD_LIBRARY_PATH", null);

	emmaFile = Options.mostRecentValue("emma.coverage.out.file", "") ;
        extraORBDProps.clear();
        extraServerProps.clear();
        extraClientProps.clear();

        giopVersion = Options.mostRecentValue(ORBConstants.GIOP_VERSION, null);
        giopFragmentSize = Options.mostRecentValue(ORBConstants.GIOP_FRAGMENT_SIZE, null);
        giopBufferSize = Options.mostRecentValue(ORBConstants.GIOP_BUFFER_SIZE, null);
        giop11BufMgr = Options.mostRecentValue(ORBConstants.GIOP_11_BUFFMGR, null);
        giop12BufMgr = Options.mostRecentValue(ORBConstants.GIOP_12_BUFFMGR, null);

        Properties giopProps = new Properties();
        if (giopVersion != null)
            giopProps.setProperty(ORBConstants.GIOP_VERSION,
                                  giopVersion);
        if (giopFragmentSize != null)
            giopProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE,
                                  giopFragmentSize);
        if (giopBufferSize != null)
            giopProps.setProperty(ORBConstants.GIOP_BUFFER_SIZE,
                                  giopBufferSize);
        if (giop11BufMgr != null)
            giopProps.setProperty(ORBConstants.GIOP_11_BUFFMGR,
                                  giop11BufMgr);
        if (giop12BufMgr != null)
            giopProps.setProperty(ORBConstants.GIOP_12_BUFFMGR,
                                  giop12BufMgr);

        if (giopProps.size() != 0) {
            extraORBDProps.putAll(giopProps);
            extraServerProps.putAll(giopProps);
            extraClientProps.putAll(giopProps);
        }

        persistentServerId = Options.mostRecentValue(persistentServerIdProperty, "1");

        setPortRelatedOptions();

        orbdMainClass = defOrbdMainClass;
    
        for(int i = 0; i < defOrbdArgs.length; i++)
            orbdArgs.add(defOrbdArgs[i]);

        namingFactory = defNamingFactory;

        // If someone has specified which ORB to use for the tests, use that,
        // otherwise use the default.
        orbClass =
            Options.mostRecentValue("org.omg.CORBA.ORBClass",
                                    defORBClass);

        orbSingleton = defORBSingleton;
        packageName = CORBAUtil.getPackageName(parent);

        String pkg = 
            packageName.replace('.', File.separatorChar);

	// This appears to be unused outside of this class.
        buildDirectory = System.getProperty("user.dir")
            + File.separator
            + "classes"
            + File.separator
            + pkg
            + File.separator;

	String testBase = (String)(parent.getArgs().get( "-testbase" )) ;
	String testRoot = "" ;
	
	if (testBase == null) {
	    testRoot = "/../src/share/classes/";
	    testBase = System.getProperty( "user.dir" ) ;
	} else {
	    testRoot = "/src/share/classes/";
	}

	testDirectory = testBase + testRoot.replace('/', File.separatorChar) + 
	    pkg + File.separator;

        outputDirectory = parent.getArgs().get(test.Test.OUTPUT_DIRECTORY)
            + File.separator 
            + pkg
            + File.separator;

        reportDirectory = outputDirectory;

        StringBuffer newPath = new StringBuffer(outputDirectory 
                                                + File.pathSeparator);
        newPath.append(System.getProperty("java.class.path"));
        classpath = newPath.toString();

        javaIDLHome = Options.mostRecentValue(defJavaIDLHomeDirName, 
                                              outputDirectory 
                                              + defJavaIDLHomeDirName);
        javaExecutable = System.getProperty("java.home")
            + File.separator + "bin" + File.separator + "java";

    }

    private Options()
    {
        // Prevent normal instantiation
    }

    /**
     * Return the system property with the given name, or return the
     * provided default value if not found.
     */
    private static String mostRecentValue(String propertyName,
					  String defaultValue)
    {
        String result = System.getProperty(propertyName);

        if (result != null)
            return result;
        else
            return defaultValue;
    }
  
    /**
     * Returns the JavaIDLHome directory path (defaults to
     * output directory/JavaIDLHome).
     * 
     */
    public static String getJavaIDLHome()
    {
        return javaIDLHome;
    }

    /**
     * Set the JavaIDLHome directory path.
     */
    public static void setJavaIDLHome(String value)
    {
        javaIDLHome = value;
    }

    /**
     * Returns the activation database directory name (defaults to
     * orb.db)
     */
    public static String getActivationDbDirName()
    {
        return activationDbDirName;
    }

    /**
     * Set the activation database directory name.
     */
    public static void setActivationDbDirName(String value)
    {
        activationDbDirName = value;
    }

    /**
     * Get the path for the ioser library (defaults to null unless
     * an LD_LIBRARY_PATH environment variable was set before running the
     * test).
     */
    public static String getioserPath()
    {
        return ioserPath;
    }

    /**
     * Set the path for the ioser library.
     */
    public static void setioserPath(String value)
    {
        ioserPath = value;
    }

    /**
     * Get the ORB initial port value (defaults to 1050 unless an
     * environment variable called "org.omg.CORBA.ORBInitialPort"
     * was found).
     */
    public static String getORBInitialPort()
    {
        return orbInitialPort.toString();
    }

    /**
     * Set the ORB initial port value.
     */
    public static void setORBInitialPort(String value)
    {
        orbInitialPort = new Port(Integer.parseInt(value));
    }

    /**
     * Get the activation port value (defaults to 1049 unless an
     * environment variable called "ActivationPort" was found).
     */
    public static String getActivationPort()
    {
        return activationPort.toString();
    }

    /**
     * Set the activation port value.
     */
    public static void setActivationPort(String value)
    {
        activationPort = new Port(Integer.parseInt(value));
    }

    /**
     * Get the class used for ORBD (defaults to 
     * com.sun.corba.se.impl.activation.ORB).
     */
    public static String getORBDMainClass()
    {
        return orbdMainClass;
    }

    /**
     * Set the class used for ORBD.
     */
    public static void setORBDMainClass(String value)
    {
        orbdMainClass = value;
    }

    /**
     * Get argument vector passed to ORBD (initially -ORBDebug orbd).
     */
    public static Vector getORBDArgs()
    {
        return orbdArgs;
    }

    /**
     * Add one command line argument for the ORBD.
     */
    public static void addORBDArg(String value)
    {
        orbdArgs.add(value);
    }

    /**
     * Add a number of command line arguments for the
     * ORBD (space separated).
     */
    public static void addORBDArgs(String values)
    {
        Options.addArgsFromString(values, orbdArgs);
    }

    /**
     * Get the naming factory class (defaults to
     * com.sun.jndi.cosnaming.CNCtxFactory).
     */
    public static String getNamingFactory()
    {
        return namingFactory;
    }

    /**
     * Set the value of the naming factory class.
     */
    public static void setNamingFactory(String value)
    {
        namingFactory = value;
    }

    /**
     * Get the naming provider URL (defaults to
     * iiop://localhost:{ORBInitialPort}).
     */
    public static String getNamingProviderURL()
    {
        if (namingProviderURL == null) {
            namingProviderURL = "iiop://localhost:" + orbInitialPort;
        }

        return namingProviderURL;
    }

    /**
     * Set the naming provider URL.
     */
    public static void setNamingProviderURL(String value)
    {
        namingProviderURL = value;
    }

    /**
     * Get argument vector passed to a server (default is
     * no arguments).
     */
    public static Vector getServerArgs()
    {
        return serverArgs;
    }

    /**
     * Add one command line argument for the server.
     */
    public static void addServerArg(String value)
    {
        serverArgs.add(value);
    }

    /**
     * Add a number of command line arguments for the
     * server (space separated).
     */
    public static void addServerArgs(String values)
    {
        Options.addArgsFromString(values, serverArgs);
    }

    /**
     * Get argument vector passed to a client (default is
     * no arguments).
     */
    public static Vector getClientArgs()
    {
        return clientArgs;
    }

    /**
     * Add one command line argument for the client.
     */
    public static void addClientArg(String value)
    {
        clientArgs.add(value);
    }

    /**
     * Add a number of command line arguments for the
     * client (space separated).
     */
    public static void addClientArgs(String values)
    {
        Options.addArgsFromString(values, clientArgs);
    }

    /**
     * Return the test's home directory (where it's source
     * files are located.  Defaults to
     * ../src/share/classes/{package}/ since the current
     * directory is assumed to be ../test/make).
     */
    public static String getTestDirectory()
    {
        return testDirectory;
    }

    /**
     * Set the test's home directory (where it's source
     * files are located).  
     */
    public static void setTestDirectory(String value)
    {
        testDirectory = value;
    }

    /**
     * Return the test's report directory (defaults to
     * the output directory).  This is where output files such
     * as javac.err.txt and javac.out.txt will go.
     */
    public static String getReportDirectory()
    {
        return reportDirectory;
    }

    /**
     * Set the test's report directory.
     */
    public static void setReportDirectory(String value)
    {
        reportDirectory = value;
    }

    /**
     * Return the test's output directory (defaults to
     * {output dir from command line}/{package}/).
     */
    public static String getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Set the output directory -- disallowed.
     */
    public static void setOutputDirectory(String value)
    {
        outputDirectory = value;
        // Disallow this because of dependencies and
	// class path issues.  The top output directory
	// should be set on the command line as well as
	// defined on the real classpath at runtime.
    }

    /**
     * Return the class path to pass to subprocesses 
     * (defaults to the system property java.class.path
     * plus the output directory).
     */
    public static String getClasspath()
    {
        return classpath;
    }

    /**
     * Set the class path which will be given to
     * subprocesses.  (Note:  This does not affect the
     * classpath of the current process)
     *
     */
    public static void setClasspath(String value)
    {
        classpath = value;
    }

    /**
     * Get the ORB class name (defaults to the IIOP ORB).
     */
    public static String getORBClass()
    {
        return orbClass;
    }

    /**
     * Set the ORB class name.
     */
    public static void setORBClass(String value)
    {
        orbClass = value;
    }

    /**
     * Get the ORB singleton class name (defaults to
     * com.sun.corba.se.impl.orb.ORBSingleton).
     */
    public static String getORBSingleton()
    {
        return orbSingleton;
    }

    /**
     * Set the ORB singleton class name.
     */
    public static void setORBSingelton(String value)
    {
        orbSingleton = value;
    }  

    /**
     * Get the maximum timeout for handshakes as well as
     * compiles (defaults to 1 minute).  This may be too
     * short for some things.  Value is in milliseconds.
     */
    public static long getMaximumTimeout()
    {
        return maximumTimeout;
    }

    /**
     * Set the maximum timeout (in milliseconds) for handshakes and
     * compiles.  Setting to zero may have undefined behavior (could
     * wait forever, could throw a timeout exception immediately, etc).
     */
    public static void setMaximumTimeout(long value)
    {
        maximumTimeout = value;
    }

    /**
     * Get a hashtable of extra flags to give to the ORBD
     * execution strategy.
     */
    public static Hashtable getORBDExtra()
    {
        return orbdExtra;
    }

    /**
     * Get a hashtable of extra flags to give to server
     * execution strategies.
     */
    public static Hashtable getServerExtra()
    {
        return serverExtra;
    }

    /**
     * Get a hashtable of extra flags to give to client
     * execution strategies.
     */
    public static Hashtable getClientExtra()
    {
        return clientExtra;
    }

    /**
     * Return the package name of the current test.
     */
    public static String getPackageName()
    {
        return packageName;
    }

    /**
     * Get the string for the java executable to use
     * when creating subprocesses (defaults to
     * {java.home}/bin/java).
     */
    public static String getJavaExec()
    {
        return javaExecutable;
    }

    /**
     * Set the string for the java executable to use when
     * creating subprocesses.
     */
    public static void setJavaExec(String value)
    {
        javaExecutable = value;
    }

    /**
     * Returns a vector of arguments to the Java compiler.
     */
    public static Vector getJavacArgs()
    {
        return javacArgs;
    }

    /**
     * Add one argument to the vector of Java compiler args.
     */
    public static void addJavacArg(String value)
    {
        javacArgs.add(value);
    }

    /**
     * Add a number of arguments to the vector of Java compiler
     * args (space separated).
     */
    public static void addJavacArgs(String values)
    {
        Options.addArgsFromString(values, javacArgs);
    }

    /**
     * Returns a vector of arguments to RMIC.
     */
    public static Vector getRMICArgs()
    {
        return rmicArgs;
    }

    /**
     * Add one argument to the vector of RMIC args.
     */
    public static void addRMICArg(String value)
    {
        rmicArgs.add(value);
    }

    /**
     * Add a number of arguments to the vector of RMIC args
     * (space separated).
     */
    public static void addRMICArgs(String values)
    {
        Options.addArgsFromString(values, rmicArgs);
    }

    /**
     * Returns a vector of arguments to the IDL compiler.
     */
    public static Vector getIDLCompilerArgs()
    {
        return idlCompilerArgs;
    }

    /**
     * Add one argument to the vector of IDL compiler args.
     */
    public static void addIDLCompilerArg(String value)
    {
        idlCompilerArgs.add(value);
    }

    /**
     * Add a number of arguments to the vector of IDL compiler
     * args(space separated).
     */
    public static void addIDLCompilerArgs(String values)
    {
        Options.addArgsFromString(values, idlCompilerArgs);
    }

    /**
     * Utility method for adding space separated arguments to 
     * a given vector.
     */
    private static void addArgsFromString(String args, Vector container)
    {
        StringTokenizer st = new StringTokenizer(args);

        while (st.hasMoreTokens()) {
            String arg = st.nextToken();
            container.add(arg);
        }
    }

    /**
     * Set the array of fully qualified class names to
     * give to RMIC.
     */
    public static void setRMICClasses(String[] value)
    {
        rmicFiles = value;
    }

    /**
     * Get the array of fully qualified class names that
     * will be given to RMIC.
     */
    public static String[] getRMICClasses()
    {
        return rmicFiles;
    }

    /**
     * Set the array of .java file names to give to javac
     * (note: any one of these can be either a full path
     * or a file name in the test directory, but must
     * end in .java).
     */
    public static void setJavaFiles(String[] value)
    {
        javaFiles = value;
    }

    /**
     * Get the array of .java file names that will be
     * given to javac.
     */
    public static String[] getJavaFiles()
    {
        return javaFiles;
    }

    /**
     * Set the array of .idl file names to give to idlj
     * (note: any one of these can be either a full path
     * or a file name in the test directory).
     * 
     */
    public static void setIDLFiles(String[] value)
    {
        idlFiles = value;
    }

    /**
     * Get the array of IDL file names that will be
     * given to idlj.
     */
    public static String[] getIDLFiles()
    {
        return idlFiles;
    }

    /**
     * Returns the build directory of the currently
     * executing test (set to {current dir}/classes/{pkg}
     * since the currently directory is test/build/{OS}).
     */
    public static String getBuildDirectory()
    {
        return buildDirectory;
    }

    /**
     * Set the build directory of the currently
     * executing test.
     */
    public static void setBuildDirectory(String value)
    {
        buildDirectory = value;
    }

    /**
     * The framework already provides many properties to
     * ORBD, but this allows the test author to override
     * or augment them.
     */
    public static Properties getExtraORBDProperties()
    {
        return extraORBDProps;
    }

    /**
     * The framework already provides many properties to
     * a server, but this allows the test author to override
     * or augment them.
     */
    public static Properties getExtraServerProperties()
    {
        return extraServerProps;
    }

    /**
     * The framework already provides many properties to
     * a client, but this allows the test author to override
     * or augment them.
     */
    public static Properties getExtraClientProperties()
    {
        return extraClientProps;
    }

    public static String getEmmaFile() {
	return emmaFile ;
    }

    public static String getPersistentServerID()
    {
        return persistentServerId;
    }

    public static void setPersistentServerID(String value)
    {
        persistentServerId = value;
    }

    private static void copySystemProperty( String name, Properties result )
    {
	String value = System.getProperty( name ) ;
	if (value != null)
	    result.setProperty( name, value ) ;
    }

    /**
     * Add special io and util delegate properties if they're
     * available in System.getProperties().  The following
     * properties are added:
     * <PRE>
     *   javax.rmi.CORBA.UtilClass (if available)
     *   javax.rmi.CORBA.StubClass (if available)
     *   javax.rmi.CORBA.PortableRemoteObjectClass (if available)
     *   com.sun.corba.se.ORBUseDynamicStub (if available)
     * </PRE>
     */
    public static void addSpecialProperties(Properties props)
    {
        copySystemProperty("javax.rmi.CORBA.UtilClass", props );
        copySystemProperty("javax.rmi.CORBA.StubClass", props );
        copySystemProperty("javax.rmi.CORBA.PortableRemoteObjectClass", props );

	// The following properties are used for setting up the ORB with
	// a SecurityManager.  This is harmless when running without
	// a SecurityManager (the normal practise).
	copySystemProperty("com.sun.corba.se.ORBBase", props );
	copySystemProperty("java.security.policy", props ) ;
	copySystemProperty("java.security.debug", props ) ;
	copySystemProperty("java.security.manager", props ) ;

	copySystemProperty(ORBConstants.USE_CODEGEN_REFLECTIVE_COPYOBJECT, props ) ;   
	copySystemProperty(ORBConstants.USE_DYNAMIC_STUB_PROPERTY, props ) ;   
	copySystemProperty(ORBConstants.DYNAMIC_STUB_FACTORY_FACTORY_CLASS, 
	    props ) ;   
	copySystemProperty(ORBConstants.ENABLE_JAVA_SERIALIZATION_PROPERTY,
			   props);
    }

    private static void setPortRelatedOptions() throws IOException {
        String prop = System.getProperty(ORBConstants.INITIAL_PORT_PROPERTY);
        if (prop == null)
            orbInitialPort = new Port();
        else
            orbInitialPort = new Port(Integer.parseInt(prop));

        prop = System.getProperty(ORBConstants.ORBD_PORT_PROPERTY);
        if (prop == null)
            activationPort = new Port();
        else
            activationPort = new Port(Integer.parseInt(prop));

        namingProviderURL = null;
    }

    /**
     * Create and return an abstraction for an unused port.
     */
    public static Port getUnusedPort() {
        return new Port();
    }
}
