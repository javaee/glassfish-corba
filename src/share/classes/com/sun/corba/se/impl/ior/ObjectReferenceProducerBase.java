/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.ior ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.ior.IORFactory ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.IORTemplateList ;
import com.sun.corba.se.spi.ior.ObjectId ;

import com.sun.corba.se.impl.orbutil.ORBUtility ;
import java.io.Serializable;


// Made this serializable so that derived class ObjectReferenceFactoryImpl
// does not require a void constructor.  Instead, this class is Serializable,
// and Object is its superclass, so Object provides the void constructor.
// This change cleans up a findbugs issue.
public abstract class ObjectReferenceProducerBase implements Serializable {
    private static final long serialVersionUID = 6478965304620421549L;
    transient protected ORB orb ;

    public abstract IORFactory getIORFactory() ;

    public abstract IORTemplateList getIORTemplateList() ;

    public ObjectReferenceProducerBase( ORB orb ) 
    {
	this.orb = orb ;
    }

    public org.omg.CORBA.Object make_object (String repositoryId, 
	byte[] objectId)
    {
	ObjectId oid = IORFactories.makeObjectId( objectId ) ;
	IOR ior = getIORFactory().makeIOR( orb, repositoryId, oid ) ;

	return ORBUtility.makeObjectReference( ior ) ;
    }
}

