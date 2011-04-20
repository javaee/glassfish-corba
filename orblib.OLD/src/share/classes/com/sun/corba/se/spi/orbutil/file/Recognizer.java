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
package com.sun.corba.se.spi.orbutil.file ;

import java.io.IOException ;

import java.util.Map ;
import java.util.HashMap ;

/** Recognizes files according to patterns, and performs actions accordingly.
 */
public class Recognizer implements Scanner.Action {
    private final int verbose ;
    private final boolean dryRun ;
    private final Map<String,Scanner.Action> nameActions ;
    private final Map<String,Scanner.Action> suffixActions ;
    private Scanner.Action shellScriptAction ;
    private Scanner.Action defaultAction ;

    // Should only be constructed in ActionFactory.
    Recognizer( final int verbose, final boolean dryRun ) {
	this.verbose = verbose ;
	this.dryRun = dryRun ;

	nameActions = new HashMap<String,Scanner.Action>() ;
	suffixActions = new HashMap<String,Scanner.Action>() ;
	shellScriptAction = null ;
	defaultAction = new Scanner.Action() {
            @Override
	    public String toString() {
		return "Built-in Default Action" ;
	    }

	    public boolean evaluate( FileWrapper fw ) {
		System.out.println( "No action defined for " + fw ) ;
		return false ;
	    }
	} ;
    }

    public void dump() {
	System.out.println( "Contents of Recognizer:" ) ;
	System.out.println( "verbose = " + verbose ) ;
	System.out.println( "dryRun = " + dryRun ) ;
	System.out.println( "Name actions:" ) ;
	for (Map.Entry<String,Scanner.Action> entry : nameActions.entrySet() ) {
	    System.out.println( "\tName = " + entry.getKey() 
		+ ", Action = " + entry.getValue() ) ;
	}

	System.out.println( "Suffix actions:" ) ;
	for (Map.Entry<String,Scanner.Action> entry : suffixActions.entrySet() ) {
	    System.out.println( "\tSuffix = " + entry.getKey() 
		+ ", Action = " + entry.getValue() ) ;
	}

	System.out.println( "Shell action:" + shellScriptAction ) ;

	System.out.println( "Default action:" + defaultAction ) ;
    }

    @Override
    public String toString() {
	return "Recognizer()" ;
    }

    public void addKnownName( final String name, final Scanner.Action action ) {
	nameActions.put( name, action ) ;
    }

    public void addKnownSuffix( final String suffix, final Scanner.Action action ) {
	suffixActions.put( suffix, action ) ;
    }

    /** If set, this defines the action taken for text files that start with the 
     * patter "#!", which is the standard for all *nix shell scripts.
     * If not set, such files are handled by the default action (if not otherwise
     * handled by name or suffix match.
     * @param action The action to perform on shell scripts.
     */
    public void setShellScriptAction( final Scanner.Action action ) {
	shellScriptAction = action ;
    }

    /** This defines the default action.  The standard default action prints
     * a message identifying the File that was not processed, and returns false.
     * This allows overriding the default action.
     * @param action The default action is nothing else matches.
     */
    public void setDefaultAction( final Scanner.Action action ) {
	if (action != null) {
            defaultAction = action;
        }
    }

    /** Apply the action that matches the classification of this file.
     * Returns the result of that action.
     * @param file The file to act upon.
     * @return result of matching action.
     */
    public boolean evaluate( final FileWrapper file ) {
	final String name = file.getName() ;
	Scanner.Action action = nameActions.get( name ) ;

	if (action == null) {
	    // look for suffix action
	    final int dotIndex = name.lastIndexOf( '.' ) ;
	    if (dotIndex >= 0) {
		String suffix = name.substring( dotIndex + 1 ) ;
		action = suffixActions.get( suffix ) ;
	    }
	}

	if (action == null) {
	    try {
		// see if this is a shell script
		file.open( FileWrapper.OpenMode.READ ) ; 
		final String str = file.readLine() ;
		if ((str != null) && str.startsWith( "#!" )) {
		    action = shellScriptAction ;
		}
		file.close() ;
	    } catch (IOException exc) {
		// action is still null
		System.out.println( "Could not read file " + file + " to check for shell script" ) ;
	    }
	}

	if (action == null) {
            action = defaultAction;
        }

	if (verbose > 1) {
	    System.out.println( 
		"Recognizer: calling action " + action 
		+ " on file " + file ) ;
	}

	if (!dryRun) {
            return action.evaluate(file);
        }

	return true ;
    }
}

