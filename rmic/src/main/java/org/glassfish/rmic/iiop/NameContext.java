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

import java.util.Hashtable;

/**
 * A NameContext enables detection of strings which differ only
 * in case.
 *
 * @author      Bryan Atsatt
 */
class NameContext {

    private Hashtable table;
    private boolean allowCollisions;

    /**
     * Get a context for the given name. Name may be null, in
     * which case this method will return the default context.
     */
    public static synchronized NameContext forName (String name,
                                                    boolean allowCollisions,
                                                    BatchEnvironment env) {

        NameContext result = null;

        // Do we need to use the default context?

        if (name == null) {

            // Yes.

            name = "null";
        }

        // Have we initialized our hashtable?

        if (env.nameContexts == null) {

            // Nope, so do it...

            env.nameContexts = new Hashtable();

        } else {

            // Yes, see if we already have the requested
            // context...

            result = (NameContext) env.nameContexts.get(name);
        }

        // Do we have the requested context?

        if (result == null) {

            // Nope, so create and add it...

            result = new NameContext(allowCollisions);

            env.nameContexts.put(name,result);
        }

        return result;
    }

    /**
     * Construct a context.
     * @param allowCollisions true if case-sensitive name collisions
     * are allowed, false if not.
     */
    public NameContext (boolean allowCollisions) {
        this.allowCollisions = allowCollisions;
        table = new Hashtable();
    }

    /**
     * Add a name to this context. If constructed with allowCollisions
     * false and a collision occurs, this method will throw an exception
     * in which the message contains the string: "name" and "collision".
     */
    public void assertPut (String name) throws Exception {

        String message = add(name);

        if (message != null) {
            throw new Exception(message);
        }
    }

    /**
     * Add a name to this context..
     */
    public void put (String name) {

        if (allowCollisions == false) {
            throw new Error("Must use assertPut(name)");
        }

        add(name);
    }

    /**
     * Add a name to this context. If constructed with allowCollisions
     * false and a collision occurs, this method will return a message
     * string, otherwise returns null.
     */
    private String add (String name) {

        // First, create a key by converting name to lowercase...

        String key = name.toLowerCase();

        // Does this key exist in the context?

        Name value = (Name) table.get(key);

        if (value != null) {

            // Yes, so they match if we ignore case. Do they match if
            // we don't ignore case?

            if (!name.equals(value.name)) {

                // No, so this is a case-sensitive match. Are we
                // supposed to allow this?

                if (allowCollisions) {

                    // Yes, make sure it knows that it collides...

                    value.collisions = true;

                } else {

                    // No, so return a message string...

                    return new String("\"" + name + "\" and \"" + value.name + "\"");
                }
            }
        } else {

            // No, so add it...

            table.put(key,new Name(name,false));
        }

        return null;
    }

    /**
     * Get a name from the context. If it has collisions, the name
     * will be converted as specified in section 5.2.7.
     */
    public String get (String name) {

        Name it = (Name) table.get(name.toLowerCase());
        String result = name;

        // Do we need to mangle it?

        if (it.collisions) {

            // Yep, so do it...

            int length = name.length();
            boolean allLower = true;

            for (int i = 0; i < length; i++) {

                if (Character.isUpperCase(name.charAt(i))) {
                    result += "_";
                    result += i;
                    allLower = false;
                }
            }

            if (allLower) {
                result += "_";
            }
        }

        return result;
    }

    /**
     * Remove all entries.
     */
    public void clear () {
        table.clear();
    }

    public class Name {
        public String name;
        public boolean collisions;

        public Name (String name, boolean collisions) {
            this.name = name;
            this.collisions = collisions;
        }
    }
}
