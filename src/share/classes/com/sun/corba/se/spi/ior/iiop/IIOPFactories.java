/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.se.spi.ior.iiop ;

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.se.spi.ior.Identifiable ;
import com.sun.corba.se.spi.ior.IdentifiableFactory ;
import com.sun.corba.se.spi.ior.EncapsulationFactoryBase ;
import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.folb.ClusterInstanceInfo ;

import com.sun.corba.se.impl.encoding.MarshalInputStream ;

import com.sun.corba.se.impl.ior.iiop.IIOPAddressImpl ;
import com.sun.corba.se.impl.ior.iiop.CodeSetsComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.AlternateIIOPAddressComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.JavaCodebaseComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.MaxStreamFormatVersionComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.JavaSerializationComponent;
import com.sun.corba.se.impl.ior.iiop.ORBTypeComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.IIOPProfileImpl ;
import com.sun.corba.se.impl.ior.iiop.IIOPProfileTemplateImpl ;
import com.sun.corba.se.impl.ior.iiop.RequestPartitioningComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.LoadBalancingComponentImpl ;
import com.sun.corba.se.impl.ior.iiop.ClusterInstanceInfoComponentImpl ;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.orbutil.ORBConstants;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS ;
import org.omg.IOP.TAG_CODE_SETS ;
import org.omg.IOP.TAG_JAVA_CODEBASE ;
import org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT ;
import org.omg.IOP.TAG_ORB_TYPE ;
import org.omg.IOP.TAG_INTERNET_IOP ;

/** This class provides all of the factories for the IIOP profiles and
 * components.  This includes direct construction of profiles and templates,
 * as well as constructing factories that can be registered with an
 * IdentifiableFactoryFinder.
 */
public abstract class IIOPFactories {
    private IIOPFactories() {}

    public static IdentifiableFactory makeRequestPartitioningComponentFactory()
    {
        return new EncapsulationFactoryBase(ORBConstants.TAG_REQUEST_PARTITIONING_ID) {
            public Identifiable readContents(InputStream in)
	    {
		int threadPoolToUse = in.read_ulong();
		Identifiable comp = 
		    new RequestPartitioningComponentImpl(threadPoolToUse);
		return comp;
	    }
        };
    } 

    public static RequestPartitioningComponent makeRequestPartitioningComponent(
	    int threadPoolToUse)
    {
	return new RequestPartitioningComponentImpl(threadPoolToUse);
    }

    public static IdentifiableFactory makeLoadBalancingComponentFactory()
    {
        return new EncapsulationFactoryBase(ORBConstants.TAG_LOAD_BALANCING_ID) {
            public Identifiable readContents(InputStream in)
	    {
		int loadBalancingValue = in.read_ulong();
		Identifiable comp = 
		    new LoadBalancingComponentImpl(loadBalancingValue);
		return comp;
	    }
        };
    } 

    public static LoadBalancingComponent makeLoadBalancingComponent(
	    int loadBalancingValue)
    {
	return new LoadBalancingComponentImpl(loadBalancingValue);
    }

    public static IdentifiableFactory makeClusterInstanceInfoComponentFactory()
    {
        return new EncapsulationFactoryBase(
            ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID) {

            public Identifiable readContents(InputStream in)
	    {
                final ClusterInstanceInfo cinfo = new ClusterInstanceInfo( in ) ;
		Identifiable comp = 
		    new ClusterInstanceInfoComponentImpl(cinfo);
		return comp;
	    }
        };
    } 

    public static ClusterInstanceInfoComponent makeClusterInstanceInfoComponent(
        ClusterInstanceInfo cinfo)
    {
	return new ClusterInstanceInfoComponentImpl(cinfo);
    }

    public static IdentifiableFactory makeAlternateIIOPAddressComponentFactory()
    {
	return new EncapsulationFactoryBase(TAG_ALTERNATE_IIOP_ADDRESS.value) {
	    public Identifiable readContents( InputStream in ) 
	    {
		IIOPAddress addr = new IIOPAddressImpl( in ) ;
		Identifiable comp = 
		    new AlternateIIOPAddressComponentImpl( addr ) ;
		return comp ;
	    }
	} ;
    } 

    public static AlternateIIOPAddressComponent makeAlternateIIOPAddressComponent(
	IIOPAddress addr )
    {
	return new AlternateIIOPAddressComponentImpl( addr ) ;
    }

    public static IdentifiableFactory makeCodeSetsComponentFactory()
    {
	return new EncapsulationFactoryBase(TAG_CODE_SETS.value) {
	    public Identifiable readContents( InputStream in ) 
	    {
		return new CodeSetsComponentImpl( in ) ;
	    }
	} ;
    }
    
    public static CodeSetsComponent makeCodeSetsComponent( ORB orb )
    {
	return new CodeSetsComponentImpl( orb ) ;
    }
	
    public static IdentifiableFactory makeJavaCodebaseComponentFactory()
    {
	return new EncapsulationFactoryBase(TAG_JAVA_CODEBASE.value) {
	    public Identifiable readContents( InputStream in ) 
	    {
		String url = in.read_string() ;
		Identifiable comp = new JavaCodebaseComponentImpl( url ) ;
		return comp ;
	    }
	} ;
    }

    public static JavaCodebaseComponent makeJavaCodebaseComponent( 
	String codebase ) 
    {
	return new JavaCodebaseComponentImpl( codebase ) ;
    }

    public static IdentifiableFactory makeORBTypeComponentFactory()
    {
	return new EncapsulationFactoryBase(TAG_ORB_TYPE.value) {
	    public Identifiable readContents( InputStream in ) 
	    {
		int type = in.read_ulong() ;
		Identifiable comp = new ORBTypeComponentImpl( type ) ;
		return comp ;
	    }
	} ;
    }

    public static ORBTypeComponent makeORBTypeComponent( int type ) 
    {
	return new ORBTypeComponentImpl( type ) ;
    }

    public static IdentifiableFactory makeMaxStreamFormatVersionComponentFactory()
    {
        return new EncapsulationFactoryBase(TAG_RMI_CUSTOM_MAX_STREAM_FORMAT.value) {
            public Identifiable readContents(InputStream in)
	    {
		byte version = in.read_octet() ;
		Identifiable comp = new MaxStreamFormatVersionComponentImpl(version);
		return comp ;
	    }
        };
    } 

    public static MaxStreamFormatVersionComponent makeMaxStreamFormatVersionComponent()
    {
	return new MaxStreamFormatVersionComponentImpl() ;
    }

    public static IdentifiableFactory makeJavaSerializationComponentFactory() {
	return new EncapsulationFactoryBase(
				ORBConstants.TAG_JAVA_SERIALIZATION_ID) {
	    public Identifiable readContents(InputStream in) {
		byte version = in.read_octet();
		Identifiable cmp = new JavaSerializationComponent(version);
		return cmp;
	    }
	};
    }

    public static JavaSerializationComponent makeJavaSerializationComponent() {
        return JavaSerializationComponent.singleton();
    }

    public static IdentifiableFactory makeIIOPProfileFactory()
    {
	return new EncapsulationFactoryBase(TAG_INTERNET_IOP.value) {
	    public Identifiable readContents( InputStream in ) 
	    {
		Identifiable result = new IIOPProfileImpl( in ) ;
		return result ;
	    }
	} ;
    }

    public static IIOPProfile makeIIOPProfile( ORB orb, ObjectKeyTemplate oktemp,
	ObjectId oid, IIOPProfileTemplate ptemp )
    {
	return new IIOPProfileImpl( orb, oktemp, oid, ptemp ) ;
    }

    public static IIOPProfile makeIIOPProfile( ORB orb, 
	org.omg.IOP.TaggedProfile profile )
    {
	return new IIOPProfileImpl( orb, profile ) ;
    }

    public static IdentifiableFactory makeIIOPProfileTemplateFactory()
    {
	return new EncapsulationFactoryBase(TAG_INTERNET_IOP.value) {
	    public Identifiable readContents( InputStream in ) 
	    {
		Identifiable result = new IIOPProfileTemplateImpl( in ) ;
		return result ;
	    }
	} ;
    }

    public static IIOPProfileTemplate makeIIOPProfileTemplate( ORB orb, 
	GIOPVersion version, IIOPAddress primary ) 
    {
	return new IIOPProfileTemplateImpl( orb, version, primary ) ;
    }

    public static IIOPAddress makeIIOPAddress( ORB orb, String host, int port ) 
    {
	return new IIOPAddressImpl( orb, host, port ) ;
    }

    public static IIOPAddress makeIIOPAddress( InputStream is ) 
    {
	return new IIOPAddressImpl( is ) ;
    }
}
