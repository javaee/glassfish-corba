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

package corba.dynamicrmiiiop  ;

import java.util.Iterator ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA.portable.ServantObject ;
import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA.portable.RemarshalException ;
import org.omg.CORBA.portable.ResponseHandler ;
import org.omg.CORBA.Request ;

import org.omg.CORBA.Context ;
import org.omg.CORBA.NVList ;
import org.omg.CORBA.NamedValue ;
import org.omg.CORBA.ExceptionList ;
import org.omg.CORBA.ContextList ;

import org.omg.CORBA_2_3.portable.Delegate ;

import com.sun.corba.ee.spi.transport.ContactInfoList ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcher ;
import com.sun.corba.ee.spi.protocol.ClientDelegate ;

import corba.dynamicrmiiiop.testclasses.TieTestImpl ;

public class TestClientDelegate extends ClientDelegate
{
    private final TestTransport transport ;
    private final TieTestImpl impl ;
    private final Tie tie ;
    private final ResponseHandler rhandler ;
    private final ORB orb ;

    private boolean isLocal = false ;
    private final LocalClientRequestDispatcher lcrd ;

    private int invocationState = INVALID ;

    private static final int INVALID            = 0 ;
    private static final int START              = 1 ;
    private static final int USE_LOCAL_TRUE     = 2 ;
    private static final int USE_LOCAL_FALSE    = 3 ;
    private static final int PREINVOKE_CALLED   = 3 ;
    private static final int POSTINVOKE_CALLED  = 4 ;
    private static final int REQUEST_CALLED     = 5 ;
    private static final int INVOKE_CALLED      = 6 ;
    private static final int RELEASE_CALLED     = 7 ;

    private void checkState( int expected, int next )
    {
        if (invocationState != expected)
            throw new RuntimeException( "Expected state " + expected +
                " but was in state " + invocationState ) ;

        invocationState = next ;
    }

    public TestClientDelegate( ORB orb, TestTransport transport, 
        final TieTestImpl impl, Tie tie, ResponseHandler rhandler )
    {
        this.transport = transport ;
        this.impl = impl ;
        this.tie = tie ;
        this.rhandler = rhandler ;
        this.orb = orb ;

        lcrd = new LocalClientRequestDispatcher() {
            public boolean useLocalInvocation( org.omg.CORBA.Object self )
            {
                if (isLocal)
                    checkState( START, USE_LOCAL_TRUE ) ;
                else
                    checkState( START, USE_LOCAL_FALSE ) ;

                return isLocal ;
            }

            public boolean is_local( org.omg.CORBA.Object self ) 
            {
                return isLocal ;
            }

            public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
                String operation, Class expectedType ) 
            {
                checkState( USE_LOCAL_TRUE, PREINVOKE_CALLED ) ;

                ServantObject result = new ServantObject() ;
                result.servant = impl ;
                return result ;
            }

            public void servant_postinvoke( org.omg.CORBA.Object self,
                ServantObject servant )
            {
                checkState( PREINVOKE_CALLED, POSTINVOKE_CALLED ) ;
            }
        } ;
    }

    public org.omg.CORBA.Object get_interface_def(
        org.omg.CORBA.Object self)
    {
        return null ;
    }

    public org.omg.CORBA.Object duplicate(org.omg.CORBA.Object obj)
    {
        return obj ;
    }

    public void release(org.omg.CORBA.Object obj)
    {
        // NO-OP
    }

    public boolean is_a(org.omg.CORBA.Object obj, String repository_id)
    {
        // Not needed for test
        return false ;
    }

    public boolean non_existent(org.omg.CORBA.Object obj)
    {
        // Always exists for test
        return false ;
    }

    public boolean is_equivalent(org.omg.CORBA.Object obj,
                                          org.omg.CORBA.Object other)
    {
        return obj == other ;
    }

    public int hash(org.omg.CORBA.Object obj, int max)
    {
        return hashCode( obj ) % max ;
    }

    public Request request(org.omg.CORBA.Object obj, String operation)
    {
        return null ;
    }

    public Request create_request(org.omg.CORBA.Object obj,
                                           Context ctx,
                                           String operation,
                                           NVList arg_list,
                                           NamedValue result)
    {
        return null ;
    }

    public Request create_request(org.omg.CORBA.Object obj,
                                           Context ctx,
                                           String operation,
                                           NVList arg_list,
                                           NamedValue result,
                                           ExceptionList exclist,
                                           ContextList ctxlist)
    {
        return null ;
    }

    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object obj) {
        return orb ;
    }

    public boolean is_local(org.omg.CORBA.Object self) 
    {
        return lcrd.is_local( self ) ;
    }

    public ServantObject servant_preinvoke( org.omg.CORBA.Object self, 
        String operation, Class expectedType) 
    {
        return lcrd.servant_preinvoke( self, operation, expectedType ) ;
    }

    public void servant_postinvoke( org.omg.CORBA.Object self, 
        ServantObject servant) 
    {
        lcrd.servant_postinvoke( self, servant ) ;
    }

    public OutputStream request(org.omg.CORBA.Object self,
                                String operation,
                                boolean responseExpected) 
    {
        checkState( USE_LOCAL_FALSE, REQUEST_CALLED ) ;
        
        return transport.makeRequest( operation ) ;
    }

    public InputStream invoke(org.omg.CORBA.Object self, 
        OutputStream output)
        throws ApplicationException, RemarshalException 
    {
        checkState( REQUEST_CALLED, INVOKE_CALLED ) ;
        InputStream is = transport.getInputStream( (org.omg.CORBA_2_3.portable.OutputStream)output ) ; 
        String mname = transport.readRequestHeader( (org.omg.CORBA_2_3.portable.InputStream)is ) ;
        OutputStream os = (OutputStream)tie._invoke( mname, is, rhandler ) ;
        InputStream result = transport.getInputStream( (org.omg.CORBA_2_3.portable.OutputStream)os ) ;
        transport.readReplyHeader( (org.omg.CORBA_2_3.portable.InputStream)result ) ; 
        // readReplyHeader throws ApplicationException on exceptions
        return result ;
    }

    public void releaseReply( org.omg.CORBA.Object self, 
        InputStream input) 
    {
        checkState( INVOKE_CALLED, RELEASE_CALLED ) ;
    }

    // From ClientDelegate:

    public ORB getBroker()
    {
        return null ;
    }

    public ContactInfoList getContactInfoList()
    {
        return new ContactInfoList() {
            public Iterator iterator()
            {
                return null ;
            }

            
            public void setTargetIOR(IOR ior)
            {
                // NO-OP
            }

            public IOR getTargetIOR()
            {
                return null ;
            }


            public void setEffectiveTargetIOR(IOR locatedIor)
            {
                // NO-OP
            }

            public IOR getEffectiveTargetIOR()
            {
                return null ;
            }


            public long getEffectiveTargetIORTimestamp()
            {
                return 0 ;
            }


            public LocalClientRequestDispatcher 
                getLocalClientRequestDispatcher()
            {
                return lcrd ;
            }


            public int hashCode()
            {
                return 0 ;
            }
        } ;
    }

    // Test methods:

    public void startLocalTest()
    {
        isLocal = true ;
        invocationState = START ;
    }

    public void startRemoteTest() 
    {
        isLocal = false ;
        invocationState = START ;
    }

    public void checkForError()
    {
        if (isLocal)
            checkState( POSTINVOKE_CALLED, INVALID ) ;
        else
            checkState( RELEASE_CALLED, INVALID ) ;
    }
}
