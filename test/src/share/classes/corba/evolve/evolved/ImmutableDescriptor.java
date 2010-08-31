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
import java.io.InvalidObjectException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ImmutableDescriptor implements Descriptor {
    private static final long serialVersionUID = 8853308591080540165L;

    private final String[] names;
    private final Object[] values;
    
    private transient int hashCode = -1;

    public static final ImmutableDescriptor EMPTY_DESCRIPTOR =
            new ImmutableDescriptor();
    
    public ImmutableDescriptor(String[] fieldNames, Object[] fieldValues) {
        this(makeMap(fieldNames, fieldValues));
    }
    
    public ImmutableDescriptor(String... fields) {
        this(makeMap(fields));
    }

    public ImmutableDescriptor(Map<String, ?> fields) {
        if (fields == null)
            throw new IllegalArgumentException("Null Map");
        SortedMap<String, Object> map =
                new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            String name = entry.getKey();
            if (name == null || name.equals(""))
                throw new IllegalArgumentException("Empty or null field name");
            if (map.containsKey(name))
                throw new IllegalArgumentException("Duplicate name: " + name);
            map.put(name, entry.getValue());
        }
        int size = map.size();
        this.names = map.keySet().toArray(new String[size]);
        this.values = map.values().toArray(new Object[size]);
    }
    
    private Object readResolve() throws InvalidObjectException {
        if (names.length == 0 && getClass() == ImmutableDescriptor.class)
            return EMPTY_DESCRIPTOR;

        boolean bad = false;
        if (names == null || values == null || names.length != values.length)
            bad = true;
        if (!bad) {
            final Comparator<String> compare = String.CASE_INSENSITIVE_ORDER;
            String lastName = ""; // also catches illegal null name
            for (int i = 0; i < names.length; i++) {
                if (names[i] == null ||
                        compare.compare(lastName, names[i]) >= 0) {
                    bad = true;
                    break;
                }
                lastName = names[i];
            }
        }
        if (bad)
            throw new InvalidObjectException("Bad names or values");

        return this;
    }
    
    private static SortedMap<String, ?> makeMap(String[] fieldNames,
                                                Object[] fieldValues) {
        if (fieldNames == null || fieldValues == null)
            throw new IllegalArgumentException("Null array parameter");
        if (fieldNames.length != fieldValues.length)
            throw new IllegalArgumentException("Different size arrays");
        SortedMap<String, Object> map =
                new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < fieldNames.length; i++) {
            String name = fieldNames[i];
            if (name == null || name.equals(""))
                throw new IllegalArgumentException("Empty or null field name");
            Object old = map.put(name, fieldValues[i]);
            if (old != null) {
                throw new IllegalArgumentException("Duplicate field name: " +
                                                   name);
            }
        }
        return map;
    }

    private static SortedMap<String, ?> makeMap(String[] fields) {
        if (fields == null)
            throw new IllegalArgumentException("Null fields parameter");
        String[] fieldNames = new String[fields.length];
        String[] fieldValues = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            int eq = field.indexOf('=');
            if (eq < 0) {
                throw new IllegalArgumentException("Missing = character: " +
                                                   field);
            }
            fieldNames[i] = field.substring(0, eq);
            // makeMap will catch the case where the name is empty
            fieldValues[i] = field.substring(eq + 1);
        }
        return makeMap(fieldNames, fieldValues);
    }
    
    public static ImmutableDescriptor union(Descriptor... descriptors) {
        // Optimize the case where exactly one Descriptor is non-Empty
        // and it is immutable - we can just return it.
        int index = findNonEmpty(descriptors, 0);
        if (index < 0)
            return EMPTY_DESCRIPTOR;
        if (descriptors[index] instanceof ImmutableDescriptor
                && findNonEmpty(descriptors, index + 1) < 0)
            return (ImmutableDescriptor) descriptors[index];

        Map<String, Object> map =
            new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        ImmutableDescriptor biggestImmutable = EMPTY_DESCRIPTOR;
        for (Descriptor d : descriptors) {
            if (d != null) {
                String[] names;
                if (d instanceof ImmutableDescriptor) {
                    ImmutableDescriptor id = (ImmutableDescriptor) d;
                    names = id.names;
                    if (id.getClass() == ImmutableDescriptor.class
                            && names.length > biggestImmutable.names.length)
                        biggestImmutable = id;
                } else
                    names = d.getFieldNames();
                for (String n : names) {
                    Object v = d.getFieldValue(n);
                    Object old = map.put(n, v);
                    if (old != null) {
                        boolean equal;
                        if (old.getClass().isArray()) {
                            equal = Arrays.deepEquals(new Object[] {old},
                                                      new Object[] {v});
                        } else
                            equal = old.equals(v);
                        if (!equal) {
                            final String msg =
                                "Inconsistent values for descriptor field " +
                                n + ": " + old + " :: " + v;
                            throw new IllegalArgumentException(msg);
                        }
                    }
                }
            }
        }
        if (biggestImmutable.names.length == map.size())
            return biggestImmutable;
        return new ImmutableDescriptor(map);
    }
    
    private static boolean isEmpty(Descriptor d) {
        if (d == null)
            return true;
        else if (d instanceof ImmutableDescriptor)
            return ((ImmutableDescriptor) d).names.length == 0;
        else
            return (d.getFieldNames().length == 0);
    }
    
    private static int findNonEmpty(Descriptor[] ds, int start) {
        for (int i = start; i < ds.length; i++) {
            if (!isEmpty(ds[i]))
                return i;
        }
        return -1;
    }

    private int fieldIndex(String name) {
        return Arrays.binarySearch(names, name, String.CASE_INSENSITIVE_ORDER);
    }
    
    public final Object getFieldValue(String fieldName) {
        checkIllegalFieldName(fieldName);
        int i = fieldIndex(fieldName);
        if (i < 0)
            return null;
        Object v = values[i];
        if (v == null || !v.getClass().isArray())
            return v;
        if (v instanceof Object[])
            return ((Object[]) v).clone();
        // clone the primitive array, could use an 8-way if/else here
        int len = Array.getLength(v);
        Object a = Array.newInstance(v.getClass().getComponentType(), len);
        System.arraycopy(v, 0, a, 0, len);
        return a;
    }

    public final String[] getFields() {
        String[] result = new String[names.length];
        for (int i = 0; i < result.length; i++) {
            Object value = values[i];
            if (value == null)
                value = "";
            else if (!(value instanceof String))
                value = "(" + value + ")";
            result[i] = names[i] + "=" + value;
        }
        return result;
    }

    public final Object[] getFieldValues(String... fieldNames) {
        if (fieldNames == null)
            return values.clone();
        Object[] result = new Object[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            String name = fieldNames[i];
            if (name != null && !name.equals(""))
                result[i] = getFieldValue(name);
        }
        return result;
    }

    public final String[] getFieldNames() {
        return names.clone();
    }

     public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Descriptor))
            return false;
        String[] onames;
        if (o instanceof ImmutableDescriptor) {
            onames = ((ImmutableDescriptor) o).names;
        } else {
            onames = ((Descriptor) o).getFieldNames();
            Arrays.sort(onames, String.CASE_INSENSITIVE_ORDER);
        }
        if (names.length != onames.length)
            return false;
        for (int i = 0; i < names.length; i++) {
            if (!names[i].equalsIgnoreCase(onames[i]))
                return false;
        }
        Object[] ovalues;
        if (o instanceof ImmutableDescriptor)
            ovalues = ((ImmutableDescriptor) o).values;
        else
            ovalues = ((Descriptor) o).getFieldValues(onames);
        return Arrays.deepEquals(values, ovalues);
    }
    
     public int hashCode() {
        if (hashCode == -1) {
            int hash = 0;
            for (int i = 0; i < names.length; i++) {
                Object v = values[i];
                int h;
                if (v == null)
                    h = 0;
                else if (v instanceof Object[])
                    h = Arrays.deepHashCode((Object[]) v);
                else if (v.getClass().isArray()) {
                    h = Arrays.deepHashCode(new Object[] {v}) - 31;
                    // hashcode of a list containing just v is
                    // v.hashCode() + 31, see List.hashCode()
                } else
                    h = v.hashCode();
                hash += names[i].toLowerCase().hashCode() ^ h;
            }
            hashCode = hash;
        }
        return hashCode;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < names.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(names[i]).append("=");
            Object v = values[i];
            if (v != null && v.getClass().isArray()) {
                String s = Arrays.deepToString(new Object[] {v});
                s = s.substring(1, s.length() - 1); // remove [...]
                v = s;
            }
            sb.append(String.valueOf(v));
        }
        return sb.append("}").toString();
    }

    public boolean isValid() {
        return true;
    }
    
    public Descriptor clone() {
        return this;
    }

    public final void setFields(String[] fieldNames, Object[] fieldValues) {
        if (fieldNames == null || fieldValues == null)
            illegal("Null argument");
        if (fieldNames.length != fieldValues.length)
            illegal("Different array sizes");
        for (int i = 0; i < fieldNames.length; i++)
            checkIllegalFieldName(fieldNames[i]);
        for (int i = 0; i < fieldNames.length; i++)
            setField(fieldNames[i], fieldValues[i]);
    }

    public final void setField(String fieldName, Object fieldValue){
        checkIllegalFieldName(fieldName);
        int i = fieldIndex(fieldName);
        if (i < 0)
            unsupported();
        Object value = values[i];
        if ((value == null) ?
                (fieldValue != null) :
                !value.equals(fieldValue))
            unsupported();
    }

    public final void removeField(String fieldName) {
        if (fieldName != null && fieldIndex(fieldName) >= 0)
            unsupported();
    }
    
    static Descriptor nonNullDescriptor(Descriptor d) {
        if (d == null)
            return EMPTY_DESCRIPTOR;
        else
            return d;
    }

    private static void checkIllegalFieldName(String name) {
        if (name == null || name.equals(""))
            illegal("Null or empty field name");
    }
    
    private static void unsupported() {
        UnsupportedOperationException uoe =
            new UnsupportedOperationException("Descriptor is read-only");
        throw new RuntimeException(uoe);
    }

    private static void illegal(String message) {
        IllegalArgumentException iae = new IllegalArgumentException(message);
        throw new RuntimeException(iae);
    }
}
