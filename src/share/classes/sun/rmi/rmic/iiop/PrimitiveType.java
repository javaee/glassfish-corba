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
import sun.tools.java.Identifier;
import sun.tools.java.ClassDefinition;

/**
 * PrimitiveType wraps primitive types and void.
 * <p>
 * The static forPrimitive(...) method must be used to obtain an instance, and
 * will return null if the type is non-conforming.
 *
 * @version     1.0, 2/27/98
 * @author      Bryan Atsatt
 */
public class PrimitiveType extends Type {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create a PrimitiveType object for the given type.
     *
     * If the type is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static PrimitiveType forPrimitive(sun.tools.java.Type type,
                                             ContextStack stack) {
        
        if (stack.anyErrors()) return null;
        
        // Do we already have it?
                
        Type existing = getType(type,stack);
                
        if (existing != null) {
                        
            if (!(existing instanceof PrimitiveType)) return null; // False hit.
                        
            // Yep, so return it...
                        
            return (PrimitiveType) existing;
        }
                
        int typeCode;
        
        switch (type.getTypeCode()) {
        case TC_VOID:           typeCode = TYPE_VOID; break;
        case TC_BOOLEAN:        typeCode = TYPE_BOOLEAN; break;
        case TC_BYTE:           typeCode = TYPE_BYTE; break;
        case TC_CHAR:           typeCode = TYPE_CHAR; break;
        case TC_SHORT:          typeCode = TYPE_SHORT; break;
        case TC_INT:            typeCode = TYPE_INT; break;
        case TC_LONG:           typeCode = TYPE_LONG; break;
        case TC_FLOAT:          typeCode = TYPE_FLOAT; break;
        case TC_DOUBLE:         typeCode = TYPE_DOUBLE; break;
        default: return null;
        }
                
        PrimitiveType it = new PrimitiveType(stack,typeCode);
                
        // Add it...
                
        putType(type,it,stack);
                
        // Do the stack thing in case tracing on...
            
        stack.push(it);
        stack.pop(true);
            
        return it;
    }

    /**
     * Return signature for this type  (e.g. com.acme.Dynamite
     * would return "com.acme.Dynamite", byte = "B")
     */
    public String getSignature() {
        switch (getTypeCode()) {
        case TYPE_VOID:         return SIG_VOID;
        case TYPE_BOOLEAN:      return SIG_BOOLEAN;
        case TYPE_BYTE:         return SIG_BYTE;
        case TYPE_CHAR:         return SIG_CHAR;
        case TYPE_SHORT:    return SIG_SHORT;
        case TYPE_INT:          return SIG_INT;
        case TYPE_LONG:         return SIG_LONG;
        case TYPE_FLOAT:        return SIG_FLOAT;
        case TYPE_DOUBLE:       return SIG_DOUBLE;
        default:            return null;
        }
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return "Primitive";
    }
    
    /**
     * IDL_Naming
     * Return the fully qualified IDL name for this type (e.g. com.acme.Dynamite would
     * return "com::acme::Dynamite").
     * @param global If true, prepends "::".
     */
    public String getQualifiedIDLName(boolean global) {
        return super.getQualifiedIDLName(false);
    }
        
    //_____________________________________________________________________
    // Subclass/Internal Interfaces
    //_____________________________________________________________________
        
    /*
     * Load a Class instance. Return null if fail.
     */
    protected Class loadClass() {
        switch (getTypeCode()) {
        case TYPE_VOID:         return Null.class;
        case TYPE_BOOLEAN:      return boolean.class;
        case TYPE_BYTE:         return byte.class;
        case TYPE_CHAR:         return char.class;
        case TYPE_SHORT:        return short.class;
        case TYPE_INT:          return int.class;
        case TYPE_LONG:         return long.class;
        case TYPE_FLOAT:        return float.class;
        case TYPE_DOUBLE:       return double.class;
        default:            throw new CompilerError("Not a primitive type");          
        }
    }
    
    /**
     * IDL_Naming
     * Create an PrimitiveType instance for the given class.
     */
    private PrimitiveType(ContextStack stack, int typeCode) {
        super(stack,typeCode | TM_PRIMITIVE);
        
        // Validate type and set names...
        
        String idlName = IDLNames.getTypeName(typeCode,false);
        Identifier id = null;
        
        switch (typeCode) {
        case TYPE_VOID:         id = idVoid; break;
        case TYPE_BOOLEAN:      id = idBoolean; break;
        case TYPE_BYTE:         id = idByte; break;
        case TYPE_CHAR:         id = idChar; break;
        case TYPE_SHORT:        id = idShort; break;
        case TYPE_INT:          id = idInt; break;
        case TYPE_LONG:         id = idLong; break;
        case TYPE_FLOAT:        id = idFloat; break;
        case TYPE_DOUBLE:       id = idDouble; break;
        default:            throw new CompilerError("Not a primitive type");          
        }
                
        setNames(id,null,idlName);
        setRepositoryID();
    }
}

class Null {}
