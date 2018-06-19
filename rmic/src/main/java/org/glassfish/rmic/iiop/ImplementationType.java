/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1998-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;
import org.glassfish.rmic.tools.java.MemberDefinition;

import java.util.Vector;

/**
 * ImplementationType represents any non-special class which implements
 * one or more interfaces which inherit from java.rmi.Remote.
 * <p>
 * The static forImplementation(...) method must be used to obtain an instance,
 * and will return null if the ClassDefinition is non-conforming.
 *
 * @author      Bryan Atsatt
 */
public class ImplementationType extends ClassType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an ImplementationType for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static ImplementationType forImplementation(ClassDefinition classDef,
                                                       ContextStack stack,
                                                       boolean quiet) {
        if (stack.anyErrors()) return null;

        boolean doPop = false;
        ImplementationType result = null;

        try {
            // Do we already have it?

            org.glassfish.rmic.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);

            if (existing != null) {

                if (!(existing instanceof ImplementationType)) return null; // False hit.

                                // Yep, so return it...

                return (ImplementationType) existing;

            }

            // Could this be an implementation?

            if (couldBeImplementation(quiet,stack,classDef)) {

                // Yes, so check it...

                ImplementationType it = new ImplementationType(stack, classDef);
                putType(theType,it,stack);
                stack.push(it);
                doPop = true;

                if (it.initialize(stack,quiet)) {
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
        return "Implementation";
    }


    //_____________________________________________________________________
    // Internal Interfaces
    //_____________________________________________________________________

    /**
     * Create a ImplementationType instance for the given class.  The resulting
     * object is not yet completely initialized.
     */
    private ImplementationType(ContextStack stack, ClassDefinition classDef) {
        super(TYPE_IMPLEMENTATION | TM_CLASS | TM_COMPOUND,classDef,stack); // Use special constructor.
    }


    private static boolean couldBeImplementation(boolean quiet, ContextStack stack,
                                                 ClassDefinition classDef) {
        boolean result = false;
        BatchEnvironment env = stack.getEnv();

        try {
            if (!classDef.isClass()) {
                failedConstraint(17,quiet,stack,classDef.getName());
            } else {
                result = env.defRemote.implementedBy(env, classDef.getClassDeclaration());
                if (!result) failedConstraint(8,quiet,stack,classDef.getName());
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }

        return result;
    }


    /**
     * Initialize this instance.
     */
    private boolean initialize (ContextStack stack, boolean quiet) {

        boolean result = false;
        ClassDefinition theClass = getClassDefinition();

        if (initParents(stack)) {

            // Make up our collections...

            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();

            // Check interfaces...

            try {
                if (addRemoteInterfaces(directInterfaces,true,stack) != null) {

                    boolean haveRemote = false;

                    // Get methods from all interfaces...

                    for (int i = 0; i < directInterfaces.size(); i++) {
                        InterfaceType theInt = (InterfaceType) directInterfaces.elementAt(i);
                        if (theInt.isType(TYPE_REMOTE) ||
                            theInt.isType(TYPE_JAVA_RMI_REMOTE)) {
                            haveRemote = true;
                        }

                        copyRemoteMethods(theInt,directMethods);
                    }

                    // Make sure we have at least one remote interface...

                    if (!haveRemote) {
                        failedConstraint(8,quiet,stack,getQualifiedName());
                        return false;
                    }

                    // Now check the methods to ensure we have the
                    // correct throws clauses...

                    if (checkMethods(theClass,directMethods,stack,quiet)) {

                        // We're ok, so pass 'em up...

                        result = initialize(directInterfaces,directMethods,null,stack,quiet);
                    }
                }
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
        }

        return result;
    }

    private static void copyRemoteMethods(InterfaceType type, Vector list) {

        if (type.isType(TYPE_REMOTE)) {

            // Copy all the unique methods from type...

            Method[] allMethods = type.getMethods();

            for (int i = 0; i < allMethods.length; i++) {
                Method theMethod = allMethods[i];

                if (!list.contains(theMethod)) {
                    list.addElement(theMethod);
                }
            }

            // Now recurse thru all inherited interfaces...

            InterfaceType[] allInterfaces = type.getInterfaces();

            for (int i = 0; i < allInterfaces.length; i++) {
                copyRemoteMethods(allInterfaces[i],list);
            }
        }
    }

    // Walk all methods of the class, and for each that is already in
    // the list, call setImplExceptions()...

    private boolean checkMethods(ClassDefinition theClass, Vector list,
                                 ContextStack stack, boolean quiet) {

        // Convert vector to array...

        Method[] methods = new Method[list.size()];
        list.copyInto(methods);

        for (MemberDefinition member = theClass.getFirstMember();
             member != null;
             member = member.getNextMember()) {

            if (member.isMethod() && !member.isConstructor()
                && !member.isInitializer()) {

                // It's a method...

                if (!updateExceptions(member,methods,stack,quiet)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean updateExceptions (MemberDefinition implMethod, Method[] list,
                                      ContextStack stack, boolean quiet) {
        int length = list.length;
        String implMethodSig = implMethod.toString();

        for (int i = 0; i < length; i++) {
            Method existingMethod = list[i];
            MemberDefinition existing = existingMethod.getMemberDefinition();

            // Do we have a matching method?

            if (implMethodSig.equals(existing.toString())) {

                // Yes, so create exception list...

                try {
                    ValueType[] implExcept = getMethodExceptions(implMethod,quiet,stack);
                    existingMethod.setImplExceptions(implExcept);
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }
}
