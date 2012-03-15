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

package pi.clientrequestinfo;

import org.omg.CORBA.*;

import java.io.*;
import javax.rmi.*;
import javax.naming.*;
import java.util.*;

import ClientRequestInfo.*; // hello interface

/**
 * Contains the actual implementations of hello operations.
 */
public class helloDelegate 
    extends Observable
    implements helloIF 
{
    private boolean invoked = false;
    private PrintStream out = null;
    
    public helloDelegate( PrintStream out ) {
        super();
        this.out = out;
    }

    public String sayHello() {
        out.println( "helloServant: sayHello() invoked" );
        invoked = true;
        return "Hello, world!";
    }
    
    public String saySystemException() {
        out.println( "helloServant: saySystemException() invoked" );
        invoked = true;
        throw new UNKNOWN( "Valid Test Result" );
    }

    public void sayUserException() 
        throws ExampleException 
    {
        out.println( "helloServant: sayUserException() invoked" );
        invoked = true;
        throw new ExampleException( "valid" );
    }
    
    // This will cause a receive_reply to be invoked since this
    // is a one-way method.
    public void sayOneway() {
        out.println( "helloServant: sayOneway() invoked" );
        invoked = true;
    }

    public String sayArguments( String arg1, int arg2, boolean arg3 ) {
        out.println( "helloServant: sayArguments() invoked" );
        invoked = true;

        return "return value";
    }
    
    public void clearInvoked() {
        invoked = false;
    }
    
    public boolean wasInvoked() {
        return invoked;
    }

    public void resetServant() {
        setChanged();
        notifyObservers();
    }
}
