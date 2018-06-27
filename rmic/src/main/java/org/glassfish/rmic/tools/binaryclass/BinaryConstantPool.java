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
import org.glassfish.rmic.tools.java.Constants;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;
import java.util.Hashtable;

/**
 * This class is used to represent a constant table once
 * it is read from a class file.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class BinaryConstantPool implements Constants {
    private byte types[];
    private Object cpool[];

    /**
     * Constructor
     */
    BinaryConstantPool(DataInputStream in) throws IOException {
        // JVM 4.1 ClassFile.constant_pool_count
        types = new byte[in.readUnsignedShort()];
        cpool = new Object[types.length];
        for (int i = 1 ; i < cpool.length ; i++) {
            int j = i;
            // JVM 4.4 cp_info.tag
            switch(types[i] = in.readByte()) {
              case CONSTANT_UTF8:
                cpool[i] = in.readUTF();
                break;

              case CONSTANT_INTEGER:
                cpool[i] = in.readInt();
                break;
              case CONSTANT_FLOAT:
                cpool[i] = new Float(in.readFloat());
                break;
              case CONSTANT_LONG:
                cpool[i++] = in.readLong();
                break;
              case CONSTANT_DOUBLE:
                cpool[i++] = new Double(in.readDouble());
                break;

              case CONSTANT_CLASS:
              case CONSTANT_STRING:
                // JVM 4.4.3 CONSTANT_String_info.string_index
                // or JVM 4.4.1 CONSTANT_Class_info.name_index
                cpool[i] =in.readUnsignedShort();
                break;

              case CONSTANT_FIELD:
              case CONSTANT_METHOD:
              case CONSTANT_INTERFACEMETHOD:
              case CONSTANT_NAMEANDTYPE:
                // JVM 4.4.2 CONSTANT_*ref_info.class_index & name_and_type_index
                cpool[i] = (in.readUnsignedShort() << 16) | in.readUnsignedShort();
                break;

              case CONSTANT_METHODHANDLE:
                cpool[i] = readBytes(in, 3);
                break;
              case CONSTANT_METHODTYPE:
                cpool[i] = readBytes(in, 2);
                break;
              case CONSTANT_INVOKEDYNAMIC:
                cpool[i] = readBytes(in, 4);
                break;

              case 0:
              default:
                throw new ClassFormatError("invalid constant type: " + (int)types[i]);
            }
        }
    }

    private byte[] readBytes(DataInputStream in, int cnt) throws IOException {
        byte[] b = new byte[cnt];
        in.readFully(b);
        return b;
    }

    /**
     * get a integer
     */
    public int getInteger(int n) {
        return (n == 0) ? 0 : ((Number)cpool[n]).intValue();
    }

    /**
     * get a value
     */
    public Object getValue(int n) {
        return (n == 0) ? null : cpool[n];
    }

    /**
     * get a string
     */
    public String getString(int n) {
        return (n == 0) ? null : (String)cpool[n];
    }

    /**
     * get an identifier
     */
    public Identifier getIdentifier(int n) {
        return (n == 0) ? null : Identifier.lookup(getString(n));
    }

    /**
     * get class declaration
     */
    public ClassDeclaration getDeclarationFromName(Environment env, int n) {
        return (n == 0) ? null : env.getClassDeclaration(Identifier.lookup(getString(n).replace('/','.')));
    }

    /**
     * get class declaration
     */
    public ClassDeclaration getDeclaration(Environment env, int n) {
        return (n == 0) ? null : getDeclarationFromName(env, getInteger(n));
    }

    /**
     * get a type from a type signature
     */
    public Type getType(int n) {
        return Type.tType(getString(n));
    }

    /**
     * get the type of constant given an index
     */
    public int getConstantType(int n) {
        return types[n];
    }

    /**
     * get the n-th constant from the constant pool
     */
    public Object getConstant(int n, Environment env) {
        int constant_type = getConstantType(n);
        switch (constant_type) {
            case CONSTANT_INTEGER:
            case CONSTANT_FLOAT:
            case CONSTANT_LONG:
            case CONSTANT_DOUBLE:
            case CONSTANT_METHODHANDLE:
            case CONSTANT_METHODTYPE:
            case CONSTANT_INVOKEDYNAMIC:
                return getValue(n);

            case CONSTANT_CLASS:
                return getDeclaration(env, n);

            case CONSTANT_STRING:
                return getString(getInteger(n));

            case CONSTANT_FIELD:
            case CONSTANT_METHOD:
            case CONSTANT_INTERFACEMETHOD:
                try {
                    int key = getInteger(n);
                    ClassDefinition clazz =
                        getDeclaration(env, key >> 16).getClassDefinition(env);
                    int name_and_type = getInteger(key & 0xFFFF);
                    Identifier id = getIdentifier(name_and_type >> 16);
                    Type type = getType(name_and_type & 0xFFFF);

                    for (MemberDefinition field = clazz.getFirstMatch(id);
                         field != null;
                         field = field.getNextMatch()) {
                        Type field_type = field.getType();
                        if ((constant_type == CONSTANT_FIELD)
                            ? (field_type == type)
                            : (field_type.equalArguments(type)))
                            return field;
                    }
                } catch (ClassNotFound e) {
                }
                return null;

            default:
                throw new ClassFormatError("invalid constant type: " +
                                              constant_type);
        }
    }


    /**
     * Get a list of dependencies, ie: all the classes referenced in this
     * constant pool.
     */
    public Vector<ClassDeclaration> getDependencies(Environment env) {
        Vector<ClassDeclaration> v = new Vector<>();
        for (int i = 1 ; i < cpool.length ; i++) {
            switch(types[i]) {
              case CONSTANT_CLASS:
                v.addElement(getDeclarationFromName(env, getInteger(i)));
                break;
            }
        }
        return v;
    }

    Hashtable<Object, Integer> indexHashObject;
    Hashtable<Object, Integer> indexHashAscii;
    Vector<String> MoreStuff;

    /**
     * Find the index of an Object in the constant pool
     */
    public int indexObject(Object obj, Environment env) {
        if (indexHashObject == null)
            createIndexHash(env);
        Integer result = indexHashObject.get(obj);
        if (result == null)
            throw new IndexOutOfBoundsException("Cannot find object " + obj + " of type " +
                                obj.getClass() + " in constant pool");
        return result.intValue();
    }

    /**
     * Find the index of an ascii string in the constant pool.  If it's not in
     * the constant pool, then add it at the end.
     */
    public int indexString(String string, Environment env) {
        if (indexHashObject == null)
            createIndexHash(env);
        Integer result = indexHashAscii.get(string);
        if (result == null) {
            if (MoreStuff == null) MoreStuff = new Vector<>();
            result = cpool.length + MoreStuff.size();
            MoreStuff.addElement(string);
            indexHashAscii.put(string, result);
        }
        return result.intValue();
    }

    /**
     * Create a hash table of all the items in the constant pool that could
     * possibly be referenced from the outside.
     */

    public void createIndexHash(Environment env) {
        indexHashObject = new Hashtable<>();
        indexHashAscii = new Hashtable<>();
        for (int i = 1; i < cpool.length; i++) {
            if (types[i] == CONSTANT_UTF8) {
                indexHashAscii.put(cpool[i], i);
            } else {
                try {
                    indexHashObject.put(getConstant(i, env), i);
                } catch (ClassFormatError e) { }
            }
        }
    }


    /**
     * Write out the contents of the constant pool, including any additions
     * that have been added.
     */
    public void write(DataOutputStream out, Environment env) throws IOException {
        int length = cpool.length;
        if (MoreStuff != null)
            length += MoreStuff.size();
        out.writeShort(length);
        for (int i = 1 ; i < cpool.length; i++) {
            int type = types[i];
            Object x = cpool[i];
            out.writeByte(type);
            switch (type) {
                case CONSTANT_UTF8:
                    out.writeUTF((String) x);
                    break;
                case CONSTANT_INTEGER:
                    out.writeInt(((Number)x).intValue());
                    break;
                case CONSTANT_FLOAT:
                    out.writeFloat(((Number)x).floatValue());
                    break;
                case CONSTANT_LONG:
                    out.writeLong(((Number)x).longValue());
                    i++;
                    break;
                case CONSTANT_DOUBLE:
                    out.writeDouble(((Number)x).doubleValue());
                    i++;
                    break;
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                    out.writeShort(((Number)x).intValue());
                    break;
                case CONSTANT_FIELD:
                case CONSTANT_METHOD:
                case CONSTANT_INTERFACEMETHOD:
                case CONSTANT_NAMEANDTYPE: {
                    int value = ((Number)x).intValue();
                    out.writeShort(value >> 16);
                    out.writeShort(value & 0xFFFF);
                    break;
                }
                case CONSTANT_METHODHANDLE:
                case CONSTANT_METHODTYPE:
                case CONSTANT_INVOKEDYNAMIC:
                    out.write((byte[])x, 0, ((byte[])x).length);
                    break;
                default:
                     throw new ClassFormatError("invalid constant type: "
                                                   + (int)types[i]);
            }
        }
        for (int i = cpool.length; i < length; i++) {
            String string = MoreStuff.elementAt(i - cpool.length);
            out.writeByte(CONSTANT_UTF8);
            out.writeUTF(string);
        }
    }

}
