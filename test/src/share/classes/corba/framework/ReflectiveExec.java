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
package corba.framework;

import java.util.Enumeration;

import test.Test;

public class ReflectiveExec extends ExternalExec {
    protected String[] buildCommand()
    {
        // Command line array:

        // [java executable]
        // [arguments to the java executable]
        // [-D environment variables]
	// [debug arguments to the java executable]
        // corba.framwork.ReflectiveWrapper
        // [class name]
        // [arguments to the program]

	String[] debugArgs = getDebugVMArgs() ;

        int size = 2 + debugArgs.length + VMArgs.length +
	    environment.size() + programArgs.length + 1;

        String cmd [] = new String [size];

        int idx = 0;
        // Java executable
        cmd[idx++] = Options.getJavaExec();

        // Arguments to the java executable
        for(int i = 0; i < VMArgs.length; i++)
            cmd[idx++] = VMArgs[i];

        // -D environment variables
        Enumeration names = environment.propertyNames();
        while(names.hasMoreElements()) {
            String name =(String) names.nextElement();
            cmd[idx++] = "-D" + name + "="
                + environment.getProperty(name);
        }

	// Debugging arguments, if any
	for(int i = 0; i < debugArgs.length; i++ )
	    cmd[idx++] = debugArgs[i];


        cmd[idx++] = "corba.framework.ReflectiveWrapper";

        // Class name
        cmd[idx++] = className;

        // Arguments to the program
        for(int i = 0; i < programArgs.length; i++)
            cmd[idx++] = programArgs[i];

        Test.dprint("--------");
        for(int i = 0; i < cmd.length; i++)
            Test.dprint("" + i + ": " + cmd[i]);
        Test.dprint("--------");

        return cmd;
    }

}
