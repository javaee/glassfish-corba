/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.java;

import java.util.*;

/**
 * The MethodSet structure is used to store methods for a class.
 * It maintains the invariant that it never stores two methods
 * with the same signature.  MethodSets are able to lookup
 * all methods with a given name and the unique method with a given
 * signature (name, args).
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */

public
class MethodSet {

    /**
     * A Map containing Lists of MemberDefinitions.  The Lists
     * contain methods which share the same name.
     */
    private final Map<Identifier,List<MemberDefinition>> lookupMap;

    /**
     * The number of methods stored in the MethodSet.
     */
    private int count;

    /**
     * Is this MethodSet currently frozen?  See freeze() for more details.
     */
    private boolean frozen;

    /**
     * Creates a brand new MethodSet
     */
    public MethodSet() {
        frozen = false;
        lookupMap = new HashMap<>();
        count = 0;
    }

    /**
     * Returns the number of distinct methods stored in the MethodSet.
     */
    public int size() {
        return count;
    }

    /**
     * Adds `method' to the MethodSet.  No method of the same signature
     * should be already defined.
     */
    public void add(MemberDefinition method) {
            // Check for late additions.
            if (frozen) {
                throw new CompilerError("add()");
            }

            // todo: Check for method??

            Identifier name = method.getName();

            // Get a List containing all methods of this name.
            List<MemberDefinition> methodList = lookupMap.get(name);

            if (methodList == null) {
                // There is no method with this name already.
                // Create a List, and insert it into the hash.
                methodList = new ArrayList<>();
                lookupMap.put(name, methodList);
            }

            // Make sure that no method with the same signature has already
            // been added to the MethodSet.
            int size = methodList.size();
            for (int i = 0; i < size; i++) {
                if ((methodList.get(i))
                    .getType().equalArguments(method.getType())) {
                    throw new CompilerError("duplicate addition");
                }
            }

            // We add the method to the appropriate list.
            methodList.add(method);
            count++;
    }

    /**
     * Adds `method' to the MethodSet, replacing any previous definition
     * with the same signature.
     */
    public void replace(MemberDefinition method) {
            // Check for late additions.
            if (frozen) {
                throw new CompilerError("replace()");
            }

            // todo: Check for method??

            Identifier name = method.getName();

            // Get a List containing all methods of this name.
            List<MemberDefinition> methodList = lookupMap.get(name);

            if (methodList == null) {
                // There is no method with this name already.
                // Create a List, and insert it into the hash.
                methodList = new ArrayList<>();
                lookupMap.put(name, methodList);
            }

            // Replace the element which has the same signature as
            // `method'.
            int size = methodList.size();
            for (int i = 0; i < size; i++) {
                if ((methodList.get(i))
                    .getType().equalArguments(method.getType())) {
                    methodList.set(i, method);
                    return;
                }
            }

            // We add the method to the appropriate list.
            methodList.add(method);
            count++;
    }

    /**
     * If the MethodSet contains a method with the same signature
     * then lookup() returns it.  Otherwise, this method returns null.
     */
    public MemberDefinition lookupSig(Identifier name, Type type) {
        // Go through all methods of the same name and see if any
        // have the right signature.
        Iterator<MemberDefinition> matches = lookupName(name);
        MemberDefinition candidate;

        while (matches.hasNext()) {
            candidate = matches.next();
            if (candidate.getType().equalArguments(type)) {
                return candidate;
            }
        }

        // No match.
        return null;
    }

    /**
     * Returns an Iterator of all methods contained in the
     * MethodSet which have a given name.
     */
    public Iterator<MemberDefinition> lookupName(Identifier name) {
        // Find the List containing all methods of this name, and
        // return that List's Iterator.
        List<MemberDefinition> methodList = lookupMap.get(name);
        if (methodList == null) {
            // If there is no method of this name, return a bogus, empty
            // Iterator.
            return Collections.emptyIterator();
        }
        return methodList.iterator();
    }

    /**
     * Returns an Iterator of all methods in the MethodSet
     */
    public Iterator<MemberDefinition> iterator() {

        //----------------------------------------------------------
        // The inner class MethodIterator is used to create our
        // Iterator of all methods in the MethodSet.
        class MethodIterator implements Iterator<MemberDefinition> {
            Iterator<List<MemberDefinition>> hashIter = lookupMap.values().iterator();
            Iterator<MemberDefinition> listIter = Collections.emptyIterator();

            public boolean hasNext() {
                if (listIter.hasNext()) {
                    return true;
                } else {
                    if (hashIter.hasNext()) {
                        listIter = hashIter.next().iterator();

                        // The following should be always true.
                        if (listIter.hasNext()) {
                            return true;
                        } else {
                            throw new
                                CompilerError("iterator() in MethodSet");
                        }
                    }
                }

                // We've run out of Lists.
                return false;
            }

            public MemberDefinition next() {
                return listIter.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
        // end MethodIterator
        //----------------------------------------------------------

        // A one-liner.
        return new MethodIterator();
    }

    /**
     * After freeze() is called, the MethodSet becomes (mostly)
     * immutable.  Any calls to add() or addMeet() lead to
     * CompilerErrors.  Note that the entries themselves are still
     * (unfortunately) open for mischievous and wanton modification.
     */
    public void freeze() {
        frozen = true;
    }

    /**
     * Tells whether freeze() has been called on this MethodSet.
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Returns a (big) string representation of this MethodSet
     */
    public String toString() {
        int len = size();
        StringBuilder sb = new StringBuilder();
        Iterator<MemberDefinition> all = iterator();
        sb.append("{");

        while (all.hasNext()) {
            sb.append(all.next().toString());
            len--;
            if (len > 0) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
