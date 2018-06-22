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

package sun.rmi.rmic.iiop;

import java.util.Vector;
import sun.tools.java.CompilerError;
import sun.tools.java.ClassNotFound;
import sun.tools.java.ClassDefinition;

/**
 * AbstractType represents any non-special interface which does not 
 * inherit from java.rmi.Remote, for which all methods throw RemoteException.
 * <p>
 * The static forAbstract(...) method must be used to obtain an instance, and will
 * return null if the ClassDefinition is non-conforming.
 * @version     1.0, 2/25/98
 * @author      Bryan Atsatt
 */
public class AbstractType extends RemoteType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an AbstractType for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static AbstractType forAbstract(ClassDefinition classDef,
                                           ContextStack stack,
                                           boolean quiet)
    {
        boolean doPop = false;
        AbstractType result = null;
            
        try {
        
            // Do we already have it?
                        
            sun.tools.java.Type theType = classDef.getType();           
            Type existing = getType(theType,stack);
                        
            if (existing != null) {
                                
                if (!(existing instanceof AbstractType)) return null; // False hit.
                                
                                // Yep, so return it...
                                
                return (AbstractType) existing;
                                
            }
                
            // Could this be an abstract?
                
            if (couldBeAbstract(stack,classDef,quiet)) {
                
                // Yes, so try it...
                
                AbstractType it = new AbstractType(stack, classDef);
                putType(theType,it,stack);
                stack.push(it);
                doPop = true;
                
                if (it.initialize(quiet,stack)) {
                    stack.pop(true);
                    result = it;
                } else {
                    removeType(theType,stack);
                    stack.pop(false);
                }
            }
        } catch (CompilerError e) {
            if (doPop) stack.pop(false);
        }
        
        return result;
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return "Abstract interface";
    }

    //_____________________________________________________________________
    // Internal/Subclass Interfaces
    //_____________________________________________________________________

    /**
     * Create a AbstractType instance for the given class.  The resulting
     * object is not yet completely initialized.
     */
    private AbstractType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_ABSTRACT | TM_INTERFACE | TM_COMPOUND);
    }

    //_____________________________________________________________________
    // Internal Interfaces
    //_____________________________________________________________________

    
    private static boolean couldBeAbstract(ContextStack stack, ClassDefinition classDef,
                                           boolean quiet) {
                        
        // Return true if interface and not remote...

        boolean result = false;
        
        if (classDef.isInterface()) {
            BatchEnvironment env = stack.getEnv();
            
            try {
                result = ! env.defRemote.implementedBy(env, classDef.getClassDeclaration());
                if (!result) failedConstraint(15,quiet,stack,classDef.getName());
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
        } else {
            failedConstraint(14,quiet,stack,classDef.getName());
        }
        
        
        return result;
    }
    
    
    /**
     * Initialize this instance.
     */
    private boolean initialize (boolean quiet,ContextStack stack) {

        boolean result = false;
        ClassDefinition self = getClassDefinition();
                
        try {
                        
            // Get methods...
                
            Vector directMethods = new Vector();

            if (addAllMethods(self,directMethods,true,quiet,stack) != null) {

                // Do we have any methods?

                boolean validMethods = true;
                
                if (directMethods.size() > 0) {
                    
                                // Yes. Walk 'em, ensuring each is a valid remote method...
                                
                    for (int i = 0; i < directMethods.size(); i++) {
                                        
                        if (! isConformingRemoteMethod((Method) directMethods.elementAt(i),true)) {
                            validMethods = false;
                        }
                    }
                }
                                
                if (validMethods) {

                    // We're ok, so pass 'em up...

                    result = initialize(null,directMethods,null,stack,quiet);
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }

        return result;
    }
}
