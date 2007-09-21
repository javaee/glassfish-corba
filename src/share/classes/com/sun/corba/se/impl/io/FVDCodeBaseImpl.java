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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.io;

import org.omg.CORBA.ORB;
import org.omg.CORBA.CompletionStatus;

import javax.rmi.CORBA.ValueHandler;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;

import com.sun.org.omg.SendingContext._CodeBaseImplBase;
import com.sun.org.omg.SendingContext.CodeBase;
import com.sun.org.omg.SendingContext.CodeBaseHelper;

import com.sun.corba.se.impl.logging.OMGSystemException;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

/**
 * This class acts as the remote interface to receivers wishing to retrieve
 * the information of a remote Class.
 */
public class FVDCodeBaseImpl extends _CodeBaseImplBase
{
    // Contains rep. ids as keys to FullValueDescriptions
    private static Map<String,FullValueDescription> fvds = 
	new HashMap<String,FullValueDescription>();

    // Private ORBSingleton used when we need an ORB while not 
    // having a delegate set.  
    private transient ORB orb = null;

    private transient OMGSystemException wrapper = 
	com.sun.corba.se.spi.orb.ORB
	    .getStaticLogWrapperTable().get_RPC_ENCODING_OMG() ;

    // backward compatability so that appropriate rep-id calculations
    // can take place
    // this needs to be transient to prevent serialization during
    // marshalling/unmarshalling
    private transient ValueHandlerImpl vhandler = null;

    public FVDCodeBaseImpl( ValueHandler vh ) {
	// vhandler will never be null
	this.vhandler = (com.sun.corba.se.impl.io.ValueHandlerImpl)vh ;  
    }

    // Operation to obtain the IR from the sending context
    public com.sun.org.omg.CORBA.Repository get_ir (){
	return null;
    }

    // Operations to obtain a URL to the implementation code
    public String implementation (String x){
	try{
            // Util.getCodebase may return null which would
            // cause a BAD_PARAM exception.
	    String result = Util.getInstance().getCodebase(vhandler.getClassFromType(x));
            if (result == null)
                return "";
            else
                return result;
	} catch(ClassNotFoundException cnfe){
	    throw wrapper.missingLocalValueImpl( CompletionStatus.COMPLETED_MAYBE,
		cnfe ) ;
	}
    }

    public String[] implementations (String[] x){
	String result[] = new String[x.length];

	for (int i = 0; i < x.length; i++)
	    result[i] = implementation(x[i]);

	return result;
    }

    // the same information
    public FullValueDescription meta (String x){
	try{
	    FullValueDescription result = fvds.get(x);

	    if (result == null) {
		try{
		    result = ValueUtility.translate(_orb(), 
			ObjectStreamClass.lookup(vhandler.getAnyClassFromType(x)), vhandler);
		} catch(Throwable t){
		    if (orb == null)
			orb = ORB.init(); //d11638
		    result = ValueUtility.translate(orb, 
			ObjectStreamClass.lookup(vhandler.getAnyClassFromType(x)), vhandler);		
		}

		if (result != null){
		    fvds.put(x, result);
		} else {
		    throw wrapper.missingLocalValueImpl( CompletionStatus.COMPLETED_MAYBE);
		}
	    }
				
	    return result;
	} catch(Throwable t){
	    throw wrapper.incompatibleValueImpl(CompletionStatus.COMPLETED_MAYBE,t);
	}
    }

    public FullValueDescription[] metas (String[] x){
	FullValueDescription descriptions[] = new FullValueDescription[x.length];

	for (int i = 0; i < x.length; i++)
	    descriptions[i] = meta(x[i]);

	return descriptions;
    }

    // information
    public String[] bases (String x){
	try {
	    Stack<String> repIds = new Stack<String>();
	    Class parent = ObjectStreamClass.lookup(vhandler.getClassFromType(x)).forClass().getSuperclass();

	    while (!parent.equals(java.lang.Object.class)) {
		repIds.push(vhandler.createForAnyType(parent));
		parent = parent.getSuperclass();
	    }

	    String result[] = new String[repIds.size()];
	    for (int i = result.length - 1; i >= 0; i++)
		result[i] = repIds.pop();

	    return result;
	} catch (Throwable t) {
	    throw wrapper.missingLocalValueImpl( CompletionStatus.COMPLETED_MAYBE, t );
	}
    }
}
