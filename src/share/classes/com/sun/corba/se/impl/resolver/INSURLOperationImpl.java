/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.resolver;

import java.util.List ;
import java.util.Map ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.Collections ;

import org.omg.CosNaming.NamingContextExt ;
import org.omg.CosNaming.NamingContextExtHelper ;
import org.omg.CORBA.ORBPackage.InvalidName ;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.ior.ObjectKey;
import com.sun.corba.se.spi.ior.IORFactories;
import com.sun.corba.se.spi.ior.ObjectKeyFactory ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.orb.Operation;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.resolver.Resolver;

import com.sun.corba.se.impl.encoding.EncapsInputStream;
import com.sun.corba.se.impl.logging.ORBUtilSystemException ;
import com.sun.corba.se.impl.logging.OMGSystemException ;
import com.sun.corba.se.impl.naming.namingutil.INSURLHandler;
import com.sun.corba.se.impl.naming.namingutil.IIOPEndpointInfo;
import com.sun.corba.se.impl.naming.namingutil.INSURL;
import com.sun.corba.se.impl.naming.namingutil.CorbalocURL;
import com.sun.corba.se.impl.naming.namingutil.CorbanameURL;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

/** 
 * This class provides an Operation that converts from CORBA INS URL strings into
 * CORBA object references.  It will eventually become extensible, but for now it
 * simply encapsulates the existing implementation.  Once the full extensibility
 * is in place, we want this operation to convert string to INSURL, which has mainly
 * a public resolver method that returns an object reference.
 * 
 * @author  Hemanth
 * @author  Ken
 */
public class INSURLOperationImpl implements Operation
{
    ORB orb;
    ORBUtilSystemException wrapper ;
    OMGSystemException omgWrapper ;
    Resolver bootstrapResolver ;

    // Root Naming Context for default resolution of names.
    private NamingContextExt rootNamingContextExt;
    private Object rootContextCacheLock = new Object() ;

    // The URLHandler to parse INS URL's
    private INSURLHandler insURLHandler = INSURLHandler.getINSURLHandler() ;

    public INSURLOperationImpl( ORB orb, Resolver bootstrapResolver )
    {
	this.orb = orb ;
	wrapper = orb.getLogWrapperTable().get_ORB_RESOLVER_ORBUtil() ;
	omgWrapper = orb.getLogWrapperTable().get_ORB_RESOLVER_OMG() ;
	this.bootstrapResolver = bootstrapResolver ;
    }

    private static final int NIBBLES_PER_BYTE = 2 ;
    private static final int UN_SHIFT = 4 ; // "UPPER NIBBLE" shift factor for <<
    
    /** This static method takes a Stringified IOR and converts it into IOR object.
      * It is the caller's responsibility to only pass strings that start with "IOR:".
      */
    private org.omg.CORBA.Object getIORFromString( String str )
    {
	// Length must be even for str to be valid
	if ( (str.length() & 1) == 1 )
	    throw wrapper.badStringifiedIorLen() ;

	byte[] buf = new byte[(str.length() - ORBConstants.STRINGIFY_PREFIX.length()) / NIBBLES_PER_BYTE];
	for (int i=ORBConstants.STRINGIFY_PREFIX.length(), j=0; i < str.length(); i +=NIBBLES_PER_BYTE, j++) {
	     buf[j] = (byte)((ORBUtility.hexOf(str.charAt(i)) << UN_SHIFT) & 0xF0);
	     buf[j] |= (byte)(ORBUtility.hexOf(str.charAt(i+1)) & 0x0F);
	}
	EncapsInputStream s = new EncapsInputStream(orb, buf, buf.length, 
	    orb.getORBData().getGIOPVersion());
	s.consumeEndian();
	return s.read_Object() ;
    }

    public Object operate( Object arg ) 
    {
	if (arg instanceof String) {
	    String str = (String)arg ;

	    if (str.startsWith( ORBConstants.STRINGIFY_PREFIX ))
		// XXX handle this as just another URL scheme
		return getIORFromString( str ) ;
	    else {
		INSURL insURL = insURLHandler.parseURL( str ) ;
		if (insURL == null)
		    throw omgWrapper.soBadSchemeName( str ) ;
		return resolveINSURL( insURL ) ;
	    }
	}

	throw wrapper.stringExpected() ;
    }

    private org.omg.CORBA.Object resolveINSURL( INSURL theURLObject ) {
	// XXX resolve should be a method on INSURL
        if( theURLObject.isCorbanameURL() ) {
            return resolveCorbaname( (CorbanameURL)theURLObject );
        } else {
            return resolveCorbaloc( (CorbalocURL)theURLObject );
        }
    }
      
    /**
     *  resolves a corbaloc: url that is encapsulated in a CorbalocURL object.
     * 
     *  @return the CORBA.Object if resolution is successful
     */
    private org.omg.CORBA.Object resolveCorbaloc(
        CorbalocURL theCorbaLocObject ) 
    {
        org.omg.CORBA.Object result = null;
        // If RIR flag is true use the Bootstrap protocol
	// Bug 6678177 noticed that this is incorrect: rir means use resolve_initial_references
	// on the local ORB!
        if( theCorbaLocObject.getRIRFlag( ) )  {
            // result = bootstrapResolver.resolve(theCorbaLocObject.getKeyString());
	    String keyString = theCorbaLocObject.getKeyString() ;
	    if (keyString.equals( "" ))
		keyString = "NameService" ;

	    try {
		result = orb.resolve_initial_references( keyString ) ;
	    } catch (InvalidName exc) {
		throw omgWrapper.soBadSchemaSpecific( exc, keyString ) ;
	    }
	} else {
	    result = getIORUsingCorbaloc( theCorbaLocObject );
	}

        return result;
    }

    /**
     *  resolves a corbaname: url that is encapsulated in a CorbanameURL object.
     * 
     *  @return the CORBA.Object if resolution is successful
     */
    private org.omg.CORBA.Object resolveCorbaname( CorbanameURL theCorbaName ) {
        org.omg.CORBA.Object result = null;

        try {
            NamingContextExt theNamingContext = null;

            if( theCorbaName.getRIRFlag( ) ) {
                // Case 1 of corbaname: rir#
                theNamingContext = getDefaultRootNamingContext( );
            } else {
                // Case 2 of corbaname: ::hostname#
                org.omg.CORBA.Object corbalocResult = 
                    getIORUsingCorbaloc( theCorbaName );
                if( corbalocResult == null ) {
                    return null;
                }

                theNamingContext = 
                    NamingContextExtHelper.narrow( corbalocResult );
            }

            String StringifiedName = theCorbaName.getStringifiedName( );

            if( StringifiedName == null ) {
                // This means return the Root Naming context
                return theNamingContext;
            } else {
	        return theNamingContext.resolve_str( StringifiedName );
            }
        } catch( Exception e ) {
            clearRootNamingContextCache( );
            // XXX Should this throw an exception or simply return null?
            // See bug 6475580
	    throw omgWrapper.soBadSchemaSpecific( e, theCorbaName.getStringifiedName() ) ;
        }
     }

    /**
     *  This is an internal method to get the IOR from the CorbalocURL object.
     * 
     *  @return the CORBA.Object if resolution is successful
     */
    private org.omg.CORBA.Object getIORUsingCorbaloc( INSURL corbalocObject ) 
    {
	Map	profileMap = new HashMap();
	List	profileList1_0 = new ArrayList();

        // corbalocObject cannot be null, because it's validated during
        // parsing. So no null check is required.
        java.util.List theEndpointInfo = corbalocObject.getEndpointInfo();
        String theKeyString = corbalocObject.getKeyString();
        // If there is no KeyString then it's invalid
        if( theKeyString == null ) {
            return null;
        }

	ObjectKey key = orb.getObjectKeyFactory().create( 
	    theKeyString.getBytes() );
	IORTemplate iortemp = IORFactories.makeIORTemplate( key.getTemplate() );

        java.util.Iterator iterator = theEndpointInfo.iterator( );
        while( iterator.hasNext( ) ) {
	    IIOPEndpointInfo element = 
                (IIOPEndpointInfo) iterator.next( );
            IIOPAddress addr = IIOPFactories.makeIIOPAddress( orb, element.getHost(), 
                element.getPort() );
	    GIOPVersion giopVersion = GIOPVersion.getInstance( (byte)element.getMajor(), 
					     (byte)element.getMinor());
	    IIOPProfileTemplate profileTemplate = null;
	    if (giopVersion.equals(GIOPVersion.V1_0)) {
		profileTemplate = IIOPFactories.makeIIOPProfileTemplate(
		    orb, giopVersion, addr);
		profileList1_0.add(profileTemplate);
	    } else {
		if (profileMap.get(giopVersion) == null) {
		    profileTemplate = IIOPFactories.makeIIOPProfileTemplate(
		        orb, giopVersion, addr);
		    profileMap.put(giopVersion, profileTemplate);
		} else {
		    profileTemplate = (IIOPProfileTemplate)profileMap.get(giopVersion);
		    AlternateIIOPAddressComponent iiopAddressComponent =
				IIOPFactories.makeAlternateIIOPAddressComponent(addr);
		    profileTemplate.add(iiopAddressComponent);
		}
	    }
	}

	GIOPVersion giopVersion = orb.getORBData().getGIOPVersion();
	IIOPProfileTemplate pTemplate = (IIOPProfileTemplate)profileMap.get(giopVersion);
	if (pTemplate != null) {
	    iortemp.add(pTemplate); // Add profile for GIOP version used by this ORB
	    profileMap.remove(giopVersion); // Now remove this value from the map
	}

	// Create a comparator that can sort in decending order (1.2, 1.1, ...)
	Comparator comp = new Comparator() {
	    public int compare(Object o1, Object o2) {
		GIOPVersion gv1 = (GIOPVersion)o1;
		GIOPVersion gv2 = (GIOPVersion)o2;
		return (gv1.lessThan(gv2) ? 1 : (gv1.equals(gv2) ? 0 : -1));
	    };
	};

	// Now sort using the above comparator
	List list = new ArrayList(profileMap.keySet());
	Collections.sort(list, comp);

	// Add the profiles in the sorted order
	Iterator iter = list.iterator();
	while (iter.hasNext()) {
	    IIOPProfileTemplate pt = (IIOPProfileTemplate)profileMap.get(iter.next());
	    iortemp.add(pt);
	}

	// Finally add the 1.0 profiles
	iortemp.addAll(profileList1_0);

	IOR ior = iortemp.makeIOR( orb, "", key.getId() ) ;
	return ORBUtility.makeObjectReference( ior ) ;
    }

    /**
     *  This is required for corbaname: resolution. Currently we
     *  are not caching RootNamingContext as the reference to rootNamingContext
     *  may not be Persistent in all the implementations. 
     *  _REVISIT_ to clear the rootNamingContext in case of COMM_FAILURE.
     * 
     *  @return the org.omg.COSNaming.NamingContextExt if resolution is 
     *   successful
     *  
     */
    private NamingContextExt getDefaultRootNamingContext( ) {
        synchronized( rootContextCacheLock ) {
	    if( rootNamingContextExt == null ) {
	        try {
	            rootNamingContextExt =
	  	        NamingContextExtHelper.narrow(
		        orb.getLocalResolver().resolve( "NameService" ) );
	        } catch( Exception e ) {
	            rootNamingContextExt = null;
	        }
            }
        }
	return rootNamingContextExt;
    }

    /**
     *  A utility method to clear the RootNamingContext, if there is an
     *  exception in resolving CosNaming:Name from the RootNamingContext,
     */
    private void clearRootNamingContextCache( ) {
        synchronized( rootContextCacheLock ) {
            rootNamingContextExt = null;
        }
    }
}
