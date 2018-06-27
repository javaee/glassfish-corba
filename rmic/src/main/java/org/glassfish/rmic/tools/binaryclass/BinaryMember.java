/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1994-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.binaryclass;

import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;
import org.glassfish.rmic.tools.tree.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * This class represents a binary member
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class BinaryMember extends MemberDefinition {
    Expression value;
    BinaryAttribute atts;

    /**
     * Constructor
     */
    public BinaryMember(ClassDefinition clazz, int modifiers, Type type,
                        Identifier name, BinaryAttribute atts) {
        super(0, clazz, modifiers, type, name, null, null);
        this.atts = atts;

        // Was it compiled as deprecated?
        if (getAttribute(idDeprecated) != null) {
            this.modifiers |= M_DEPRECATED;
        }

        // Was it synthesized by the compiler?
        if (getAttribute(idSynthetic) != null) {
            this.modifiers |= M_SYNTHETIC;
        }
    }

    /**
     * Constructor for an inner class.
     */
    public BinaryMember(ClassDefinition innerClass) {
        super(innerClass);
    }

    /**
     * Inline allowed (currently only allowed for the constructor of Object).
     */
    public boolean isInlineable(Environment env, boolean fromFinal) {
        // It is possible for 'getSuperClass()' to return null due to error
        // recovery from cyclic inheritace.  Can this cause a problem here?
        return isConstructor() && (getClassDefinition().getSuperClass() == null);
    }

    /**
     * Get arguments
     */
    public Vector<MemberDefinition> getArguments() {
        if (isConstructor() && (getClassDefinition().getSuperClass() == null)) {
            Vector<MemberDefinition> v = new Vector<>();
            v.addElement(new LocalMember(0, getClassDefinition(), 0,
                                        getClassDefinition().getType(), idThis));
            return v;
        }
        return null;
    }

    /**
     * Get exceptions
     */
    public ClassDeclaration[] getExceptions(Environment env) {
        if ((!isMethod()) || (exp != null)) {
            return exp;
        }
        byte data[] = getAttribute(idExceptions);
        if (data == null) {
            return new ClassDeclaration[0];
        }

        try {
            BinaryConstantPool cpool = ((BinaryClass)getClassDefinition()).getConstants();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            // JVM 4.7.5 Exceptions_attribute.number_of_exceptions
            int n = in.readUnsignedShort();
            exp = new ClassDeclaration[n];
            for (int i = 0 ; i < n ; i++) {
                // JVM 4.7.5 Exceptions_attribute.exception_index_table[]
                exp[i] = cpool.getDeclaration(env, in.readUnsignedShort());
            }
            return exp;
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }

    /**
     * Get documentation
     */
    public String getDocumentation() {
        if (documentation != null) {
            return documentation;
        }
        byte data[] = getAttribute(idDocumentation);
        if (data == null) {
            return null;
        }
        try {
            return documentation = new DataInputStream(new ByteArrayInputStream(data)).readUTF();
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }

    /**
     * Check if constant:  Will it inline away to a constant?
     * This override is needed to solve bug 4128266.  It is also
     * integral to the solution of 4119776.
     */
    private boolean isConstantCache = false;
    private boolean isConstantCached = false;
    public boolean isConstant() {
        if (!isConstantCached) {
            isConstantCache = isFinal()
                              && isVariable()
                              && getAttribute(idConstantValue) != null;
            isConstantCached = true;
        }
        return isConstantCache;
    }

    @Override
    public String getMemberValueString(Environment env) throws ClassNotFound {
        String value = null;

        // Prod it to setValue if it is a constant...

        getValue(env);

        // Get the value, if any...

        Node node = getValue();

        if (node != null) {
            // We don't want to change the code in CharExpression,
            // which is shared among tools, to return the right string
            // in case the type is char, so we treat it special here.
            if (getType().getTypeCode() == TC_CHAR) {
                Integer intValue = (Integer)((IntegerExpression)node).getValue();
                value = "L'" + String.valueOf((char)intValue.intValue()) + "'";
            } else {
                value = node.toString();
            }
        }
        return value;
    }


    /**
     * Get the value
     */
    public Node getValue(Environment env) {
        if (isMethod()) {
            return null;
        }
        if (!isFinal()) {
            return null;
        }
        if (getValue() != null) {
            return (Expression)getValue();
        }
        byte data[] = getAttribute(idConstantValue);
        if (data == null) {
            return null;
        }

        try {
            BinaryConstantPool cpool = ((BinaryClass)getClassDefinition()).getConstants();
            // JVM 4.7.3 ConstantValue.constantvalue_index
            Object obj = cpool.getValue(new DataInputStream(new ByteArrayInputStream(data)).readUnsignedShort());
            switch (getType().getTypeCode()) {
              case TC_BOOLEAN:
                setValue(new BooleanExpression(0, ((Number)obj).intValue() != 0));
                break;
              case TC_BYTE:
              case TC_SHORT:
              case TC_CHAR:
              case TC_INT:
                setValue(new IntExpression(0, ((Number)obj).intValue()));
                break;
              case TC_LONG:
                setValue(new LongExpression(0, ((Number)obj).longValue()));
                break;
              case TC_FLOAT:
                setValue(new FloatExpression(0, ((Number)obj).floatValue()));
                break;
              case TC_DOUBLE:
                setValue(new DoubleExpression(0, ((Number)obj).doubleValue()));
                break;
              case TC_CLASS:
                setValue(new StringExpression(0, (String)cpool.getValue(((Number)obj).intValue())));
                break;
            }
            return (Expression)getValue();
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }

    /**
     * Get a field attribute
     */
    public byte[] getAttribute(Identifier name) {
        for (BinaryAttribute att = atts ; att != null ; att = att.next) {
            if (att.name.equals(name)) {
                return att.data;
            }
        }
        return null;
    }

    public boolean deleteAttribute(Identifier name) {
        BinaryAttribute walker = null, next = null;

        boolean succeed = false;

        while (atts.name.equals(name)) {
            atts = atts.next;
            succeed = true;
        }
        for (walker = atts; walker != null; walker = next) {
            next = walker.next;
            if (next != null) {
                if (next.name.equals(name)) {
                    walker.next = next.next;
                    next = next.next;
                    succeed = true;
                }
            }
        }
        for (walker = atts; walker != null; walker = walker.next) {
            if (walker.name.equals(name)) {
                throw new InternalError("Found attribute " + name);
            }
        }

        return succeed;
    }



    /*
     * Add an attribute to a field
     */
    public void addAttribute(Identifier name, byte data[], Environment env) {
        this.atts = new BinaryAttribute(name, data, this.atts);
        // Make sure that the new attribute is in the constant pool
        ((BinaryClass)(this.clazz)).cpool.indexString(name.toString(), env);
    }

}
