/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package rmic;

import sun.rmi.rmic.Names;
import sun.rmi.rmic.iiop.Type;
import sun.rmi.rmic.iiop.CompoundType;
import sun.rmi.rmic.iiop.ContextStack;
import sun.tools.java.ClassPath;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.Identifier;

public class MapType extends CompoundType {

    public static boolean resetTypesForEach = false;
        
    private MapType() {
        super(null,0,null);
    }

    public String getTypeDescription () {
        return null;
    }


    public int getCount () {
        return countTypes();
    }
    
    public static Type getType (String className,
                                ContextStack stack) {
            
        if (MapType.resetTypesForEach) {
            stack.getEnv().reset();
        }
            
        Identifier classID = Identifier.lookup(className);
        classID = stack.getEnv().resolvePackageQualifiedName(classID);
        classID = Names.mangleClass(classID);
        ClassDeclaration decl = stack.getEnv().getClassDeclaration(classID);
        return makeType(decl.getType(), null, stack);
    }

    public static void main (String[] args) {

        int status = 0;
        try {
            resetTypesForEach = Boolean.valueOf(args[0]).booleanValue();
            int offset = Integer.parseInt(args[1]);
            TestEnv env = new TestEnv(new ClassPath(args[2]));
            ContextStack stack = new ContextStack(env);

            for (int i = 3; i < args.length; i++)  {
                String className = args[i];
                try {
                    env.reset();
                    int line = offset + i - 2;
                    String num = Integer.toString(line);
                    if (line < 10) num = "    " + num;
                    else if (line < 100) num = "   " + num;
                    else if (line < 1000) num = "  " + num;
                    else if (line < 10000) num = " " + num;
                    System.out.print(num + " - " + className);

                    Type result = getType(className,stack);

                    if (result != null) {
                        if (env.nerrors > 0) {
                            status = 1;
                            System.out.println("!!!Failure: result = " + result.getTypeDescription());   
                        } else {
                            System.out.println(" = " + result.getTypeDescription());
                        }
                    }
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) throw (ThreadDeath) e;
                    status = 1;
                    System.out.println("!!!Exception: " + className + " caught " + e);
                }
            }
        } catch (Throwable e) {
            System.out.println("!!!Exception: caught " + e);
            status = 1;
        }
                
        System.exit(status);
    }
}
