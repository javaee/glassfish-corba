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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package sun.rmi.rmic.iiop;

import sun.tools.java.CompilerError;
import sun.tools.java.ClassNotFound;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.ClassDefinition;
import sun.rmi.rmic.IndentingWriter;
import java.io.IOException;

/**
 * ClassType is an abstract base representing any non-special class
 * type.
 *
 * @version     1.0, 2/27/98
 * @author      Bryan Atsatt
 */
public abstract class ClassType extends CompoundType {

    private ClassType parent;

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________
    
    /**
     * Return the parent class of this type. Returns null if this
     * type is an interface or if there is no parent.
     */
    public ClassType getSuperclass() {
        return parent;
    }
        
   
    /**
     * Print this type.
     * @param writer The stream to print to.
     * @param useQualifiedNames If true, print qualified names; otherwise, print unqualified names.
     * @param useIDLNames If true, print IDL names; otherwise, print java names.
     * @param globalIDLNames If true and useIDLNames true, prepends "::".
     */
    public void print ( IndentingWriter writer,
                        boolean useQualifiedNames,
                        boolean useIDLNames,
                        boolean globalIDLNames) throws IOException {
                                                        
        if (isInner()) {
            writer.p("// " + getTypeDescription() + " (INNER)");
        } else {
            writer.p("// " + getTypeDescription());
        }
        writer.pln(" (" + getRepositoryID() + ")\n");
                
        printPackageOpen(writer,useIDLNames);
                
        if (!useIDLNames) {
            writer.p("public ");
        }
                
        String prefix = "";
        writer.p("class " + getTypeName(false,useIDLNames,false));
        if (printExtends(writer,useQualifiedNames,useIDLNames,globalIDLNames)) {
            prefix = ",";
        }
        printImplements(writer,prefix,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.plnI(" {");
        printMembers(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.pln();
        printMethods(writer,useQualifiedNames,useIDLNames,globalIDLNames);

        if (useIDLNames) {
            writer.pOln("};");
        } else {
            writer.pOln("}");
        }
                
        printPackageClose(writer,useIDLNames);
    }
    

    //_____________________________________________________________________
    // Subclass/Internal Interfaces
    //_____________________________________________________________________

    protected void destroy () {
        if (!destroyed) {
            super.destroy();
            if (parent != null) {
                parent.destroy();
                parent = null;
    }
    }
        }
        
    /**
     * Create a ClassType instance for the given class. NOTE: This constructor
     * is ONLY for SpecialClassType.
     */
    protected ClassType(ContextStack stack, int typeCode, ClassDefinition classDef) {
        super(stack,typeCode,classDef); // Call special parent constructor.
        if ((typeCode & TM_CLASS) == 0 && classDef.isInterface()) {
            throw new CompilerError("Not a class");
        }
        parent = null;
    }
  
    /**
     * Create a ClassType instance for the given class. NOTE: This constructor
     * is ONLY for ImplementationType. It does not walk the parent chain.
     */
    protected ClassType(int typeCode, ClassDefinition classDef,ContextStack stack) {
        super(stack,classDef,typeCode);
        
        if ((typeCode & TM_CLASS) == 0 && classDef.isInterface()) {
            throw new CompilerError("Not a class");
        }
        parent = null;
    }
   
    /**
     * Create an ClassType instance for the given class.  The resulting
     * object is not yet completely initialized. Subclasses must call
     * initialize(directInterfaces,directInterfaces,directConstants);
     */
    protected ClassType(ContextStack stack,
                        ClassDefinition classDef,
                        int typeCode) {
        super(stack,classDef,typeCode);
        if ((typeCode & TM_CLASS) == 0 && classDef.isInterface()) {
            throw new CompilerError("Not a class");
        }
        parent = null;
    }

    /**
     * Convert all invalid types to valid ones.
     */         
    protected void swapInvalidTypes () {
        super.swapInvalidTypes();
        if (parent != null && parent.getStatus() != STATUS_VALID) {
            parent = (ClassType) getValidType(parent); 
        }
    }
    
    /**
     * Modify the type description with exception info.
     */
    public String addExceptionDescription (String typeDesc) {
        if (isException) {
            if (isCheckedException) {
                typeDesc = typeDesc + " - Checked Exception";
            } else {
                typeDesc = typeDesc + " - Unchecked Exception";
            }
        }
        return typeDesc;
    }
    
    
    protected boolean initParents(ContextStack stack) {
    
        stack.setNewContextCode(ContextStack.EXTENDS);
        BatchEnvironment env = stack.getEnv();
        
        // Init parent...
        
        boolean result = true;
        
        try {
            ClassDeclaration parentDecl = getClassDefinition().getSuperClass(env);
            if (parentDecl != null) {
                ClassDefinition parentDef = parentDecl.getClassDefinition(env);
                parent = (ClassType) makeType(parentDef.getType(),parentDef,stack);
                if (parent == null) {
                    result = false;
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
            throw new CompilerError("ClassType constructor");
        }
                
        return result;
    }
}
