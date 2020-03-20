/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.ior.iiop;

import java.util.Iterator ;

import com.sun.corba.ee.spi.ior.iiop.JavaCodebaseComponent;
import com.sun.corba.ee.spi.transport.SocketInfo;
import org.omg.IOP.TAG_INTERNET_IOP ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.TaggedComponent ;
import com.sun.corba.ee.spi.ior.TaggedProfile ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplateBase ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;

import com.sun.corba.ee.impl.ior.EncapsulationUtility ;

import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;

import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.orb.ORB ;

/**
 * @author 
 * If getMinorVersion==0, this does not contain any tagged components
 */
public class IIOPProfileTemplateImpl extends TaggedProfileTemplateBase 
    implements IIOPProfileTemplate, SocketInfo
{
    private ORB orb ;
    private GIOPVersion giopVersion ;
    private IIOPAddress primary ;
   
    public Iterator<TaggedComponent> getTaggedComponents() {
        return iterator() ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "IIOPProfileTemplateImpl[giopVersion=") ;
        sb.append(giopVersion.getMajor()).append('.').append(giopVersion.getMinor()) ;
        sb.append( " primary=" ) ;
        sb.append(primary.getHost()).append(':').append(primary.getPort()) ;
        sb.append( ']' ) ;
        return sb.toString() ;
    }

    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPProfileTemplateImpl))
            return false ;

        IIOPProfileTemplateImpl other = (IIOPProfileTemplateImpl)obj ;

        return super.equals( obj ) && giopVersion.equals( other.giopVersion ) &&
            primary.equals( other.primary ) ;
    }

    public int hashCode()
    {
        return super.hashCode() ^ giopVersion.hashCode() ^ primary.hashCode() ;
    }

    public TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id ) 
    {
        return IIOPFactories.makeIIOPProfile( orb, oktemp, id, this ) ;
    }

    public GIOPVersion getGIOPVersion()
    {
        return giopVersion ;
    }

    public IIOPAddress getPrimaryAddress() 
    {
        return primary ;
    }

    @Override
    public SocketInfo getPrimarySocketInfo() {
        return this;
    }

    @Override
    public String getType() {
        return stream().anyMatch(this::isSslTaggedComponent) ? SocketInfo.SSL_PREFIX : SocketInfo.IIOP_CLEAR_TEXT;
    }

    private boolean isSslTaggedComponent(TaggedComponent component) {
        return component instanceof JavaCodebaseComponent && ((JavaCodebaseComponent) component).getURLs().contains("https:");
    }

    @Override
    public String getHost() {
        return primary.getHost();
    }

    @Override
    public int getPort() {
        return primary.getPort();
    }

    public IIOPProfileTemplateImpl( ORB orb, GIOPVersion version, IIOPAddress primary ) 
    {
        this.orb = orb ;
        this.giopVersion = version ;
        this.primary = primary ;
        if (giopVersion.getMinor() == 0)
            // Adding tagged components is not allowed for IIOP 1.0,
            // so this template is complete and should be made immutable.
            makeImmutable() ;
    }

    public IIOPProfileTemplateImpl( InputStream istr )
    {
        byte major = istr.read_octet() ;
        byte minor = istr.read_octet() ;
        giopVersion = GIOPVersion.getInstance( major, minor ) ;
        primary = new IIOPAddressImpl( istr ) ;
        orb = (ORB)(istr.orb()) ;
        // Handle any tagged components (if applicable)
        if (minor > 0) 
            EncapsulationUtility.readIdentifiableSequence(      
                this, orb.getTaggedComponentFactoryFinder(), istr ) ;

        makeImmutable() ;
    }
    
    public void write( ObjectKeyTemplate okeyTemplate, ObjectId id, OutputStream os) 
    {
        giopVersion.write( os ) ;
        primary.write( os ) ;

        // Note that this is NOT an encapsulation: do not marshal
        // the endianness flag.  However, the length is required.
        // Note that this cannot be accomplished with a codec!

        // Use the byte order of the given stream
        OutputStream encapsulatedOS = OutputStreamFactory.newEncapsOutputStream( (ORB)os.orb()
        ) ;

        okeyTemplate.write( id, encapsulatedOS ) ;
        EncapsulationUtility.writeOutputStream( encapsulatedOS, os ) ;

        if (giopVersion.getMinor() > 0) 
            EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    
    /** Write out this IIOPProfileTemplateImpl only.
    */
    public void writeContents( OutputStream os) 
    {
        giopVersion.write( os ) ;
        primary.write( os ) ;

        if (giopVersion.getMinor() > 0) 
            EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }
    
    public int getId() 
    {
        return TAG_INTERNET_IOP.value ;
    }

    public boolean isEquivalent( TaggedProfileTemplate temp )
    {
        if (!(temp instanceof IIOPProfileTemplateImpl))
            return false ;

        IIOPProfileTemplateImpl tempimp = (IIOPProfileTemplateImpl)temp ;

        return primary.equals( tempimp.primary )  ;
    }

}
