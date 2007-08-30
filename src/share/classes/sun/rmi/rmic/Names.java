/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package sun.rmi.rmic;

import sun.tools.java.Identifier;

/**
 * Names provides static utility methods used by other rmic classes
 * for dealing with identifiers.
 */
public class Names {

    /**
     * Return stub class name for impl class name.
     */
    static final public Identifier stubFor(Identifier name) {
	return Identifier.lookup(name + "_Stub");
    }

    /**
     * Return skeleton class name for impl class name.
     */
    static final public Identifier skeletonFor(Identifier name) {
	return Identifier.lookup(name + "_Skel");
    }

    /**
     * If necessary, convert a class name to its mangled form, i.e. the
     * non-inner class name used in the binary representation of
     * inner classes.  This is necessary to be able to name inner
     * classes in the generated source code in places where the language
     * does not permit it, such as when synthetically defining an inner
     * class outside of its outer class, and for generating file names
     * corresponding to inner classes.
     *
     * Currently this mangling involves modifying the internal names of
     * inner classes by converting occurrences of ". " into "$".
     *
     * This code is taken from the "mangleInnerType" method of
     * the "sun.tools.java.Type" class; this method cannot be accessed
     * itself because it is package protected.
     */
    static final public Identifier mangleClass(Identifier className) {
	if (!className.isInner())
	    return className;

	/*
	 * Get '.' qualified inner class name (with outer class
	 * qualification and no package qualification) and replace
	 * each '.' with '$'.
	 */
	Identifier mangled = Identifier.lookup(
					       className.getFlatName().toString()
					       .replace('.', sun.tools.java.Constants.SIGC_INNERCLASS));
	if (mangled.isInner())
	    throw new Error("failed to mangle inner class name");

	// prepend package qualifier back for returned identifier
	return Identifier.lookup(className.getQualifier(), mangled);
    }
}
