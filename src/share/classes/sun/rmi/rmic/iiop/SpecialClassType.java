/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

package sun.rmi.rmic.iiop;

import sun.tools.java.ClassNotFound;
import sun.tools.java.CompilerError;
import sun.tools.java.Identifier;
import sun.tools.java.ClassDefinition;

/**
 * SpecialClassType represents any one of the following types:
 * <pre>
 *    java.lang.Object
 *    java.lang.String
 * </pre>
 * all of which are treated as special cases.
 * <p>
 * The static forSpecial(...) method must be used to obtain an instance, and
 * will return null if the type is non-conforming.
 *
 * @version     1.0, 2/27/98
 * @author      Bryan Atsatt
 */
public class SpecialClassType extends ClassType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create a SpecialClassType object for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static SpecialClassType forSpecial (ClassDefinition theClass,
                                               ContextStack stack) {
        if (stack.anyErrors()) return null;
                
        sun.tools.java.Type type = theClass.getType();
        
        // Do we already have it?
        
        String typeKey = type.toString() + stack.getContextCodeString();
                
        Type existing = getType(typeKey,stack);
                
        if (existing != null) {
                        
            if (!(existing instanceof SpecialClassType)) return null; // False hit.
                        
            // Yep, so return it...
                        
            return (SpecialClassType) existing;
        }
        
        // Is it a special type?
        
        int typeCode = getTypeCode(type,theClass,stack);
        
        if (typeCode != TYPE_NONE) {
            
            // Yes...
            
            SpecialClassType result = new SpecialClassType(stack,typeCode,theClass);
            putType(typeKey,result,stack);
            stack.push(result);
            stack.pop(true);
            return result;
                
        } else {

            return null;
        }
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return "Special class";
    }

    //_____________________________________________________________________
    // Subclass/Internal Interfaces
    //_____________________________________________________________________

    /**
     * Create an SpecialClassType instance for the given class.
     */
    private SpecialClassType(ContextStack stack, int typeCode,
                             ClassDefinition theClass) {
        super(stack,typeCode | TM_SPECIAL_CLASS | TM_CLASS | TM_COMPOUND, theClass);
        Identifier id = theClass.getName();
        String idlName = null;
        String[] idlModuleName = null;
        boolean constant = stack.size() > 0 && stack.getContext().isConstant();
        
        // Set names...
        
        switch (typeCode) {
        case TYPE_STRING:   {
            idlName = IDLNames.getTypeName(typeCode,constant);
            if (!constant) {
                idlModuleName = IDL_CORBA_MODULE;
            }
            break;
        }
            
        case TYPE_ANY:   {
            idlName = IDL_JAVA_LANG_OBJECT;
            idlModuleName = IDL_JAVA_LANG_MODULE;
            break;
        }
        }
        
        setNames(id,idlModuleName,idlName);

        // Init parents...
        
        if (!initParents(stack)) {
        
            // Should not be possible!
            
            throw new CompilerError("SpecialClassType found invalid parent.");
        }
        
        // Initialize CompoundType...
        
        initialize(null,null,null,stack,false);
    }
    
    private static int getTypeCode(sun.tools.java.Type type, ClassDefinition theClass, ContextStack stack) {
        if (type.isType(TC_CLASS)) {
            Identifier id = type.getClassName();
            if (id == idJavaLangString) return TYPE_STRING;
            if (id == idJavaLangObject) return TYPE_ANY;
        }
        return TYPE_NONE;
    }
}
