/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.asm;

import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;

/**
 * This represents a class for RMIC to process. It is built from a class file using ASM.
 */
class AsmClass extends ClassDefinition {

    private final AsmClassFactory factory;

    AsmClass(AsmClassFactory factory, String name, int modifiers, ClassDeclaration declaration, ClassDeclaration superClassDeclaration, ClassDeclaration[] interfaceDeclarations) {
        super(name, 0, declaration, modifiers, null, null);
        this.factory = factory;
        superClass = superClassDeclaration;
        interfaces = interfaceDeclarations;
    }

    @Override
    public void loadNested(Environment env) {
        try {
            Identifier outerClass = factory.getOuterClassName(getName());
            if (outerClass != null)
                this.outerClass = env.getClassDefinition(outerClass);
        } catch (ClassNotFound ignore) {
        }
    }

    private boolean basicCheckDone = false;
    private boolean basicChecking = false;

    // This code is copied from BinaryClass.java which ensures that inherited method 
    // information is gathered. Consider promoting this to the super class.
    protected void basicCheck(Environment env) throws ClassNotFound {
        if (tracing) env.dtEnter("AsmClass.basicCheck: " + getName());

        if (basicChecking || basicCheckDone) {
            if (tracing) env.dtExit("AsmClass.basicCheck: OK " + getName());
            return;
        }

        if (tracing) env.dtEvent("AsmClass.basicCheck: CHECKING " + getName());
        basicChecking = true;

        super.basicCheck(env);

        // Collect inheritance information.
        if (doInheritanceChecks) {
            collectInheritedMethods(env);
        }

        basicCheckDone = true;
        basicChecking = false;
        if (tracing) env.dtExit("AsmClass.basicCheck: " + getName());
    }

}
