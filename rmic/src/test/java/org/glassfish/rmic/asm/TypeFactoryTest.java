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

import org.glassfish.rmic.classes.nestedClasses.TwoLevelNested;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.Type;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;

/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
public class TypeFactoryTest {
    private TypeFactory factory = new TypeFactory();

    @Test
    public void constructNoArgVoidMethodType() throws Exception {
        Type methodType = TypeFactory.createMethodType("()V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), emptyArray());
    }

    @Test
    public void constructByteArrayToIntType() throws Exception {
        Type methodType = TypeFactory.createMethodType("([B)I");

        assertThat(methodType.getReturnType(), equalTo(Type.tInt));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tByte)));
    }

    @Test
    public void constructAllNumericArgsToBooleanMethod() throws Exception {
        Type methodType = TypeFactory.createMethodType("(SIJFD)Z");

        assertThat(methodType.getReturnType(), equalTo(Type.tBoolean));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tShort, Type.tInt, Type.tLong, Type.tFloat, Type.tDouble));
    }

    @Test
    public void constructAllObjectArguments() throws Exception {
        Type methodType = TypeFactory.createMethodType("(Ljava/lang/String;Lorg/glassfish/rmic/classes/nestedClasses/TwoLevelNested$Level1;)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tString, Type.tClass(Identifier.lookup(TwoLevelNested.Level1.class.getName()))));
    }

    @Test
    public void constructObjectArrayArgument() throws Exception {
        Type methodType = TypeFactory.createMethodType("([Ljava/lang/Object;)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tObject)));
    }

    @Test
    public void constructCharArrayArgument() throws Exception {
        Type methodType = TypeFactory.createMethodType("([C)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tChar)));
    }

    @Test
    public void constructMultiDimensionalArrayType() throws Exception {
        assertThat(TypeFactory.createType("[[I"), equalTo(Type.tArray(Type.tArray(Type.tInt))));
    }
}
