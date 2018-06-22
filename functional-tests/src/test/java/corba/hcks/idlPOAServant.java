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

//
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2003 Dec 19 (Fri) 11:02:11 by Harold Carr.
//

package corba.hcks;

import org.omg.PortableServer.*;
import java.util.Iterator;
import java.util.Properties;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactory ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.TaggedComponent ;
import com.sun.corba.ee.spi.ior.TaggedComponentFactoryFinder ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactory ;
import com.sun.corba.ee.spi.ior.ObjectAdapterId ;
import com.sun.corba.ee.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.IORTemplateList ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;

import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory ;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.transport.Acceptor;

import com.sun.corba.ee.spi.ior.iiop.ORBTypeComponent ;

import com.sun.corba.ee.impl.ior.GenericTaggedProfile ;
import com.sun.corba.ee.impl.ior.GenericTaggedComponent ;
import com.sun.corba.ee.impl.ior.FreezableList ;
import com.sun.corba.ee.impl.ior.OldJIDLObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.OldPOAObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.JIDLObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.POAObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.WireObjectKeyTemplate ;
import com.sun.corba.ee.impl.ior.EncapsulationUtility ;
import com.sun.corba.ee.impl.ior.TaggedComponentFactoryFinderImpl ;
import com.sun.corba.ee.impl.ior.TaggedProfileFactoryFinderImpl ;
import com.sun.corba.ee.impl.ior.ObjectAdapterIdArray ;
import com.sun.corba.ee.impl.ior.ObjectAdapterIdNumber ;
import com.sun.corba.ee.impl.ior.ObjectReferenceTemplateImpl ;
import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl ;

import com.sun.corba.ee.impl.orb.ORBVersionImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

class idlPOAServant
    extends 
        idlIPOA
{
    public static final String baseMsg = idlPOAServant.class.getName();
    public static final String from_idlPOAServant = "from idlPOAServant";

    public ORB orb;

    public org.omg.CORBA.Object savedObject;

    public idlPOAServant(org.omg.CORBA.ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public String syncOK(String arg1)
    {
        return baseMsg + " " + arg1;
    }

    public synchronized void asyncOK(byte[] data)
    {
        try {
            U.sop(new String(data, C.UTF8));
        } catch ( Exception e ) {
            U.sopUnexpectedException(baseMsg + C.asyncOK, e);
        }
    }

    public void throwUserException()
        throws idlExampleException
    {
        C.throwUserException(from_idlPOAServant);
    }

    public void throwSystemException()
    {
        C.throwSystemException(from_idlPOAServant);
    }

    public void throwUnknownException()
    {
        C.throwUnknownException(from_idlPOAServant);
    }

    public void throwUNKNOWN()
    {
        C.throwUNKNOWN(from_idlPOAServant);
    }

    public void raiseSystemExceptionInSendReply()
    {
    }

    public void testEffectiveTarget1()
    {
    }

    public void testEffectiveTarget2()
    {
    }

    public idlValueTypeA sendValue (idlValueTypeA a, 
                                    idlValueTypeB b, 
                                    idlValueTypeC c,
                                    idlValueTypeD d,
                                    idlValueTypeE e,
                                    int[]         f,
                                    byte[]        g)
    {
        U.sop(d);
        return b;
    }

    public org.omg.CORBA.Object getAndSaveUnknownORBVersionIOR()
    {
        // This is modified from corba.ior.Client test - around line 754.

        String[] ss = { "foo", "bar" } ;
        ObjectAdapterId poaid = new ObjectAdapterIdArray( ss ) ;
        POAObjectKeyTemplate poktemp1 = makePOAObjectKeyTemplate( poaid ) ;

        // KEY POINT: set an "unknown" ORB version.
        poktemp1.setORBVersion(new ORBVersionImpl((byte)99));

        IIOPProfileTemplate ptemp1 = makeIIOPProfileTemplate( 45671 ) ;
        ORBTypeComponent comp1 = IIOPFactories.makeORBTypeComponent( 0x34567ABF ) ;

        ptemp1.add( comp1) ;
        IORTemplate iortemp = IORFactories.makeIORTemplate( poktemp1 ) ;
        iortemp.add( ptemp1 ) ;

        byte[] id = { 0x00, 0x00, 0x33, 0x44, 0x21, 0x23, 0x00 } ;
        ObjectId oid = IORFactories.makeObjectId( id ) ;

        String typeid = "foo:bar" ;
        IOR ior3 = iortemp.makeIOR( orb, typeid, oid ) ;
        savedObject = ORBUtility.makeObjectReference(ior3);
        return savedObject;
    }

    public boolean isIdenticalWithSavedIOR(org.omg.CORBA.Object o)
    {
        return savedObject._is_equivalent(o);
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    public POAObjectKeyTemplate makePOAObjectKeyTemplate( ObjectAdapterId poaid )
    {
        int scid = ORBConstants.FIRST_POA_SCID ;
        String orbid = "AVeryLongORBIdName" ;
        int serverid = -123 ;

        POAObjectKeyTemplate temp = new POAObjectKeyTemplate( orb, scid, 
            serverid, orbid, poaid ) ;

        return temp ;
    }

    public IIOPProfileTemplate makeIIOPProfileTemplate( int port )
    {
        String host = "FOO" ;
        IIOPAddress primary = IIOPFactories.makeIIOPAddress( host, port ) ;
        
        IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate( orb,
            GIOPVersion.V1_2, primary ) ;

        return ptemp ;
    }
}

// End of file.

