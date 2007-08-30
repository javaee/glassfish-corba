/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework ;

import com.sun.javatest.TestFinder ;
import com.sun.javatest.TestDescription ;

import java.io.File ;
import java.io.FileFilter ;
import java.io.FileReader ;
import java.io.BufferedReader ;

import java.util.HashMap ;
import java.util.Hashtable ;
import java.util.Map ;
import java.util.StringTokenizer ;

public class CORBATestFinder extends TestFinder {
    public void scan( File file ) 
    {
	if (file.isDirectory())
	    scanDirectory( file ) ;
	else
	    scanFile( file ) ;
    }

    private void scanDirectory( File file )
    {
	FileFilter filter = new FileFilter() {
	    public boolean accept( File file ) 
	    {
		String name = file.getName() ;
		if (file.isDirectory())
		    return !(name.equals("make") ||
			name.equals( "build" ) ||
			name.equals( "SCCS" )) ;
		else
		    return name.endsWith( "Tests.txt" ) ;
	    }
	} ;

	File[] files = file.listFiles( filter ) ;
	for( int ctr=0; ctr<files.length; ctr++ )
	    foundFile( files[ctr] ) ;
    }

    private void scanFile( File file )
    {
	BufferedReader reader ;
	try {
	    reader = new BufferedReader( new FileReader( file ) ) ;
	} catch (java.io.FileNotFoundException exc) {
	    return ;
	}

	String line ;
	try {
	    line = reader.readLine() ;
	    while (line != null) {
		StringTokenizer st = new StringTokenizer( line ) ;
		// ignore all lines whose first token is not -test
		if (st.hasMoreTokens())
		    if (st.nextToken().equals( "-test" )) {
			// Create a map for the TestDescription args
			Map map = new HashMap() ;

			String testClass = st.nextToken() ;
			map.put( "title", testClass ) ;
			map.put( "executeClass", testClass ) ;
			
			// Put any remaining args under the "testArgs" name
			int remaining = st.countTokens() ;
			map.put( "numArgs", Integer.toString(remaining) ) ;
			int ctr = 0 ;
			while (st.hasMoreTokens()) 
			    map.put( "testArg." + ctr++, st.nextToken() ) ;

			// create the TestDescription
			TestDescription td = new TestDescription( getRoot(), file, map ) ;
			foundTestDescription( td ) ;
		    }

		line = reader.readLine() ;
	    }
	} catch (java.io.IOException exc) {
	    // Just exit; any IO errors send us out of here.
	    // XXX May want to report errors through the JavaTest framework.
	}
    }
}
