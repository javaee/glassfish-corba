/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package corba.dynamicrmiiiop.testclasses ; 

import java.rmi.RemoteException ;

import java.util.Map ;
import java.util.HashMap ;

import javax.rmi.CORBA.Util ;

import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.CompletionStatus ;
import org.omg.CORBA.SystemException ;

import org.omg.CORBA.portable.UnknownException ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import org.glassfish.pfl.test.ObjectUtility;

public class TieTestImpl implements TieTest 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static Object wrapException( Throwable thr ) 
    {
        try {
            return Util.wrapException( thr ) ;
        } catch (RuntimeException exc) {
            // The return type of wrapException is RemoteException,
            // but the stupid spec (see section 1.4.8.1) requires
            // that a RuntimeException simply result in the same
            // RuntimeException, which is not a subtype of RemoteException.
            // Therefore the result is thrown in this case.
            // That works OK in stubs, but throwing an exception in a 
            // static initializer blows up the class loading, which we 
            // cannot tolerate here in this test.
            return exc ;
        }
    }

    // Data format: ( name, tieOnly?, tieResult, stubResult, args, argTypes ) *
    // The Tie result is the result used here, while the stub result is often
    // wrapped for exception handling.
    private static Object[][] data = {
        // name, not used,
        // tieResult, 
        // stubResult,
        // args,
        // types
        {   "hasAByteArray", Boolean.FALSE, 
            null,
            null,
            new Object[] { new byte[]{ 0, 1, 2, 3 } },
            new Class[] { byte[].class } },
        {   "foo_bar_baz", Boolean.TRUE,
            new BAD_OPERATION( 
                wrapper.METHOD_NOT_FOUND_IN_TIE,
                CompletionStatus.COMPLETED_NO ),
            null,
            new Object[] { new Integer(37) }, 
            new Class[] { int.class } },
        {   "throwsDeclaredException", Boolean.FALSE,
            new MyApplicationException("Foo"), 
            new MyApplicationException("Foo"), 
            new Object[] { new Integer(36) },
            new Class[] { int.class } },
        {   "throwsException", Boolean.FALSE,
            new Exception("Foo"), 
            new Exception("Foo"), 
            new Object[] { new Integer(38) },
            new Class[] { int.class } },
        {   "throwsSystemException", Boolean.FALSE,
            new BAD_PARAM(), 
            Util.mapSystemException( new BAD_PARAM() ), 
            new Object[] { new Integer(35) },
            new Class[] { int.class } },
        {   "throwsJavaException", Boolean.FALSE,
            new UnknownException( new IllegalStateException() ),   
            wrapException( new IllegalStateException() ),   
            new Object[] { new Integer(31) },
            new Class[] { int.class } },
        {   "m0", Boolean.FALSE,
            "m0 result", 
            "m0 result", 
            null,
            new Class[0] },
        {   "m1", Boolean.FALSE,
            "m1 result", 
            "m1 result", 
            new Object[] { "m1 arg" },
            new Class[] { String.class } },
        {   "m2", Boolean.FALSE,
            "m2 result", 
            "m2 result", 
            new Object[] { new HashMap(), "m2 arg" },
            new Class[] { Map.class, String.class } },
        {   "vm0", Boolean.FALSE,
            null, 
            null, 
            null,
            new Class[0] },
        {   "vm1", Boolean.FALSE,
            null, 
            null, 
            new Object[] { "vm1 arg" },
            new Class[0] },
        {   "vm2", Boolean.FALSE,
            null, 
            null, 
            new Object[] { null, "vm2 arg" },
            new Class[0] } } ;

    private static Map mnameToExpectedTieResult = new HashMap() ;
    private static Map mnameToExpectedStubResult = new HashMap() ;
    private static Map mnameToExpectedArguments = new HashMap() ;
    private static Map mnameToArgumentTypes = new HashMap() ;
    private static String lastError ;

    static {
        for (int ctr=0; ctr<data.length; ctr++) {
            Object[] elem = data[ctr] ;
            String key = (String)elem[0] ;
            Object tieResult = elem[2] ;
            Object stubResult = elem[3] ;
            Object[] args = (Object[])elem[4] ;
            Class[] types = (Class[])elem[5] ;
            mnameToExpectedTieResult.put( key, tieResult ) ;
            mnameToExpectedStubResult.put( key, stubResult ) ;
            mnameToExpectedArguments.put( key, args ) ;
            mnameToArgumentTypes.put( key, types ) ;
        }
    }

    // Note that this clears the last error
    public String getLastError() 
    {
        String result = lastError ;
        lastError = null ;
        return result ;
    }

    private void checkArgs( String mname, Object[] args )
    {
        Object[] expected = getExpectedArguments( mname ) ;
        if (expected.length != args.length)
            lastError = "Expected and actual lengths do not match" ;

        for (int ctr=0; ctr<expected.length; ctr++ ) {
            if (!ObjectUtility.equals( expected[ctr], args[ctr] )) {
                lastError = "Argument " + ctr + 
                    " does not match expected result" ;
                break ;
            }
        }
    }

    public static Object getExpectedTieResult( String mname )
    {
        return mnameToExpectedTieResult.get( mname ) ;
    }

    public static Object getExpectedStubResult( String mname )
    {
        return mnameToExpectedStubResult.get( mname ) ;
    }

    public static Object[] getExpectedArguments( String mname )
    {
        return (Object[])mnameToExpectedArguments.get( mname ) ;
    }

    public static Class[] getArgumentTypes( String mname ) 
    {
        return (Class[])mnameToArgumentTypes.get( mname ) ;
    }

    public int throwsException( int arg ) 
        throws Exception, RemoteException
    {
        checkArgs( "throwsException", 
            new Object[] { new Integer(arg) } ) ;
        throw (Exception)getExpectedTieResult( 
            "throwsException" ) ;
    }

    public int throwsDeclaredException( int arg ) 
        throws MyApplicationExceptionBase, RemoteException
    {
        checkArgs( "throwsDeclaredException", 
            new Object[] { new Integer(arg) } ) ;
        throw (MyApplicationException)getExpectedTieResult( 
            "throwsDeclaredException" ) ;
    }

    public int throwsSystemException( int arg ) throws RemoteException 
    {
        checkArgs( "throwsSystemException", new Object[] { new Integer(arg) } ) ;
        throw (SystemException)getExpectedTieResult( "throwsSystemException" ) ;
    }

    public int throwsJavaException( int arg ) throws RemoteException 
    {
        checkArgs( "throwsJavaException", new Object[] { new Integer(arg) } ) ;
        throw (RuntimeException)getExpectedTieResult( "throwsJavaException" ) ;
    }

    public String m0() throws RemoteException 
    {
        return (String)getExpectedTieResult( "m0" ) ;
    }

    public String m1( String another ) throws RemoteException 
    {
        checkArgs( "m1", new Object[] { another } ) ;
        return (String)getExpectedTieResult( "m1" ) ;
    }

    public String m2( Map map, String key ) throws RemoteException 
    {
        checkArgs( "m2", new Object[] { map, key } ) ;
        return (String)getExpectedTieResult( "m2" ) ;
    }

    public void vm0() throws RemoteException 
    {
    }

    public void vm1( String another ) throws RemoteException 
    {
        checkArgs( "vm1", new Object[] { another } ) ;
    }

    public void hasAByteArray( byte[] arg ) throws RemoteException 
    {
        checkArgs( "hasAByteArray", new Object[] { arg } ) ;
    }

    public void vm2( Map map, String key ) throws RemoteException 
    {
        checkArgs( "vm2", new Object[] { map, key } ) ;
    }
}

