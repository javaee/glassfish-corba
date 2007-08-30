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

package com.sun.corba.se.impl.resolver ;

import org.omg.CORBA.ORBPackage.InvalidName;

import com.sun.corba.se.spi.resolver.Resolver ;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import java.io.File;
import java.io.FileInputStream;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.orbutil.CorbaResourceUtil ;

public class FileResolverImpl implements Resolver
{
    private ORB orb ;
    private File file ;
    private Properties savedProps ;
    private long fileModified = 0 ;

    public FileResolverImpl( ORB orb, File file )
    {
	this.orb = orb ;
	this.file = file ;
	savedProps = new Properties() ;
    }

    public org.omg.CORBA.Object resolve( String name ) 
    {
	check() ;
	String stringifiedObject = savedProps.getProperty( name ) ;
	if (stringifiedObject == null) {
	    return null;
	}
	return orb.string_to_object( stringifiedObject ) ;
    }

    public java.util.Set list() 
    {
	check() ;

	Set result = new HashSet() ;

	// Obtain all the keys from the property object
	Enumeration theKeys = savedProps.propertyNames();
	while (theKeys.hasMoreElements()) {
	    result.add( theKeys.nextElement() ) ;
	}

	return result ;
    }

    /**
    * Checks the lastModified() timestamp of the file and optionally
    * re-reads the Properties object from the file if newer.
    */
    private void check() 
    {
	if (file == null)
	    return;

	long lastMod = file.lastModified();
	if (lastMod > fileModified) {
	    try {
		FileInputStream fileIS = new FileInputStream(file);
		savedProps.clear();
		savedProps.load(fileIS);
		fileIS.close();
		fileModified = lastMod;
	    } catch (java.io.FileNotFoundException e) {
		System.err.println( CorbaResourceUtil.getText(
		    "bootstrap.filenotfound", file.getAbsolutePath()));
	    } catch (java.io.IOException e) {
		System.err.println( CorbaResourceUtil.getText(
		    "bootstrap.exception",
		    file.getAbsolutePath(), e.toString()));
	    }
	}
    }
}
