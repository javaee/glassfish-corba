/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1996-2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.rmic.tools.tree.*;

/**
 * This is the protocol by which a Parser makes callbacks
 * to the later phases of the compiler.
 * <p>
 * (As a backwards compatibility trick, Parser implements
 * this protocol, so that an instance of a Parser subclass
 * can handle its own actions.  The preferred way to use a
 * Parser, however, is to instantiate it directly with a
 * reference to your own ParserActions implementation.)
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author      John R. Rose
 */
public interface ParserActions {
    /**
     * package declaration
     */
    void packageDeclaration(long off, IdentifierToken nm);

    /**
     * import class
     */
    void importClass(long off, IdentifierToken nm);

    /**
     * import package
     */
    void importPackage(long off, IdentifierToken nm);

    /**
     * Define class
     * @return a cookie for the class
     * This cookie is used by the parser when calling defineField
     * and endClass, and is not examined otherwise.
     */
    ClassDefinition beginClass(long off, String doc,
                               int mod, IdentifierToken nm,
                               IdentifierToken sup, IdentifierToken impl[]);


    /**
     * End class
     * @param c a cookie returned by the corresponding beginClass call
     */
    void endClass(long off, ClassDefinition c);

    /**
     * Define a field
     * @param c a cookie returned by the corresponding beginClass call
     */
    void defineField(long where, ClassDefinition c,
                     String doc, int mod, Type t,
                     IdentifierToken nm, IdentifierToken args[],
                     IdentifierToken exp[], Node val);
}
