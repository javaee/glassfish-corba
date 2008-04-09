/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework;

import java.io.*;
import java.util.*;
import test.*;

/**
 * Adapter class providing some convenient default implementations for
 * the Controller interface.
 */
public abstract class ControllerAdapter implements Controller
{
    protected String className;
    protected String processName;
    protected Properties environment;
    protected String VMArgs[];
    protected String programArgs[];
    protected OutputStream out;
    protected OutputStream err;
    protected Hashtable extra;
    protected JUnitReportHelper helper;
    
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
        this.className = className;
        this.processName = processName;
        this.environment = environment;
        this.VMArgs = VMArgs;
        this.programArgs = programArgs;
        this.out = out;
        this.err = err;
        this.extra = extra;
      
        // Make life a little easier
        if (this.environment == null)
            this.environment = new Properties();
        if (this.VMArgs == null)
            this.VMArgs = new String[0];
        if (this.programArgs == null)
            this.programArgs = new String[0];
    }
   
    public OutputStream getOutputStream()
    {
        return out;
    }
    
    public OutputStream getErrorStream()
    {
        return err;
    }

    public String getProcessName()
    {
        return processName;
    }

    public String getClassName()
    {
        return className;
    }

    public void start() throws Exception
    {
        start( null ) ;
    }
}
