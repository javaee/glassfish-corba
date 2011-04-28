/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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
package com.sun.corba.se.tool ;

import org.omg.CORBA.portable.ObjectImpl ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.protocol.ClientDelegate ;
import com.sun.corba.se.spi.transport.ContactInfoList ;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.impl.ior.GenericIdentifiable ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.TaggedProfile ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.TaggedComponent ;
import org.glassfish.pfl.basic.algorithm.Printer;

public class IORDump {
    private static Printer pr = new Printer( System.out ) ;

    private static final String[] STANDARD_TAGGED_COMPONENT_NAMES = new String[]{
        "ORB_TYPE",                     // 0
        "CODE_SETS",
        "POLICIES",
        "ALTERNATE_IIOP_ADDRESS",
        null,
        "COMPLETE_OBJECT_KEY",
        "ENDPOINT_ID_POSITION",
        null,
        null,
        null,                           
        null,                           // 10
        null,
        "LOCATION_POLICY",
        "ASSOCIATION_OPTIONS",
        "SEC_NAME",
        "SPKM_1_SEC_MECH",
        "SPKM_2_SEC_MECH",
        "KergerosV5_SEC_MECH",
        "CSI_ECMA_Secret_SEC_MECH",
        "CSI_ECMA_Hybrid_SEC_MECH",
        "SSL_SEC_TRANS",                // 20
        "CSI_ECMA_Public_SEC_MECH",
        "GENERIC_SEC_MECH",
        "FIREWALL_TRANS",
        "SCCP_CONTACT_INFO",
        "JAVA_CODEBASE",
        "TRANSACTION_POLICY",
        "FT_GROUP",
        "FT_PRIMARY",
        "FT_HEARTBEAT_ENABLED",
        "MESSAGE_ROUTERS",              // 30
        "OTS_POLICY",
        "INV_POLICY",
        "CSI_SEC_MECH_LIST",
        "NULL_TAG",
        "SECIOP_SEC_TRANS",
        "TLS_SEC_TRANS",
        "ACTIVITY_POLICY",
        "RMI_CUSTOM_MAX_STREAM_FORMAT",
        null,
        null,                           // 40
        null, null, null, null, null,
        null, null, null, null, null,   // 50
        null, null, null, null, null,
        null, null, null, null, null,   // 60
        null, null, null, null, null,
        null, null, null, null, null,   // 70
        null, null, null, null, null,
        null, null, null, null, null,   // 80
        null, null, null, null, null,
        null, null, null, null, null,   // 90
        null, null, null, null, null,
        null, null, null, null, 
        "DCE_STRING_BINDING",            // 100
        "DCE_BINDING_NAME",
        "DCE_NO_PIPES",
        "DCE_SEC_MECH",
        null, null,
        null, null, null, null, null,   // 110
        null, null, null, null, null,   
        null, null, null, null, null,   // 120
        null, null, 
        "INET_SEC_TRANS"
    } ;

    private static String getTaggedComponentName( int id ) {
        String entry = null ;
        if ((id >= 0) && (id < STANDARD_TAGGED_COMPONENT_NAMES.length))
            entry = STANDARD_TAGGED_COMPONENT_NAMES[id] ;

        if (entry == null)
            entry = "UNASSIGNED_" + id ;

        return entry ;
    }

    public static void main( String[] args ) {
        if (args.length != 1) {
            System.out.println( "Syntax: iordump <stringified IOR or URL>" ) ;
        } else {
            try {
                String iorString = args[0] ;
                String[] initArgs = null ;
                ORB orb = (ORB)ORB.init( initArgs, null ) ;
                org.omg.CORBA.Object obj = orb.string_to_object( iorString ) ;
                ObjectImpl oimpl = (ObjectImpl)obj ;
                ClientDelegate delegate = (ClientDelegate)(oimpl._get_delegate()) ;
                ContactInfoList cilist = (ContactInfoList)(delegate.getContactInfoList()) ;
                IOR ior = cilist.getTargetIOR() ;
                dumpIOR( ior ) ;
            } catch (Exception exc) {
                System.out.println( "Caught exception: " + exc ) ;
                exc.printStackTrace() ;
            }
        }
    }

    public static void dumpIOR( IOR ior ) {
        pr.nl().p( "Dump of IOR:" ).in() ;
        pr.nl().p( "typeId = " + ior.getTypeId() ) ;
        pr.nl().p( "tagged profiles:" ).in() ;
        for (TaggedProfile tprof : ior) {
            dumpTaggedProfile( tprof ) ;
        }
        pr.out().out().nl() ; 
    }

    public static void dumpTaggedProfile( TaggedProfile tprof ) {
        pr.nl().p( "Id = ", tprof.getId(), ":" ).in() ;

        TaggedProfileTemplate tpt = tprof.getTaggedProfileTemplate() ;
        if (tpt instanceof IIOPProfileTemplate) {
            IIOPProfileTemplate iptemp = (IIOPProfileTemplate)tpt ;
            pr.nl().p( "GIOPVersion    = ", iptemp.getGIOPVersion() ) ;
            pr.nl().p( "PrimaryAddress = ", iptemp.getPrimaryAddress() ) ;
        }

        pr.nl().p( "ObjectId:" ).in() ;
        pr.printBuffer( tprof.getObjectId().getId() ).out() ;

        pr.nl().p( "ObjectKeyTemplate:" ).in() ;
        dumpObjectKeyTemplate( tprof.getObjectKeyTemplate() ) ;
        pr.out() ;

        pr.nl().p( "Tagged components:" ).in() ;
        for (TaggedComponent tcomp : tpt ) {
            int id = tcomp.getId() ;
            pr.nl().p( "id = ", tcomp.getId(), 
                " (", getTaggedComponentName( id ), ")" ).in() ;
            if (tcomp instanceof GenericIdentifiable) {
                GenericIdentifiable gid = (GenericIdentifiable)tcomp ;
                pr.printBuffer( gid.getData() ) ;
            } else {
                pr.nl().p( tcomp ) ;
            }
            pr.out() ;
        }
        pr.out() ;

        pr.out() ;
    }

    public static void dumpObjectKeyTemplate( ObjectKeyTemplate oktemp ) {
        pr.nl().p( "ORBVersion      = " ).p( oktemp.getORBVersion() ) ;
        pr.nl().p( "SubcontractId   = " ).p( oktemp.getSubcontractId() ) ;
        pr.nl().p( "ServerId        = " ).p( oktemp.getServerId() ) ;
        pr.nl().p( "ORBId           = " ).p( oktemp.getORBId() ) ;
        pr.nl().p( "ObjectAdapterId = " ).p( oktemp.getObjectAdapterId() ) ;
    }
}
