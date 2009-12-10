/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.misc ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Arrays ;

public class OperationTracer {
    private static boolean enabled = false ;

    public static String convertToString( Object arg ) {
        if (arg == null)
            return "<NULL>" ;

        Class cls = arg.getClass() ;
        if (cls.isArray()) {
            Class cclass = cls.getComponentType() ;
            if (cclass.equals( int.class ))
                        return Arrays.toString( (int[])arg ) ;
            if (cclass.equals( byte.class ))
                        return Arrays.toString( (byte[])arg ) ;
            if (cclass.equals( boolean.class ))
                        return Arrays.toString( (boolean[])arg ) ;
            if (cclass.equals( char.class ))
                        return Arrays.toString( (char[])arg ) ;
            if (cclass.equals( short.class ))
                        return Arrays.toString( (short[])arg ) ;
            if (cclass.equals( long.class ))
                        return Arrays.toString( (long[])arg ) ;
            if (cclass.equals( float.class ))
                        return Arrays.toString( (float[])arg ) ;
            if (cclass.equals( double.class ))
                        return Arrays.toString( (double[])arg ) ;
            return Arrays.toString( (Object[])arg ) ;
        } else {
            return arg.toString() ;
        }
    }

    public static void enable() {
        enabled = true ;
    }

    public static void disable() {
        enabled = false ;
    }

    private OperationTracer() {}

    interface Element {
        String getAsString() ;
    }

    private static ThreadLocal<List<Element>> state = new ThreadLocal() {
        public List<Element> initialValue() {
            return new ArrayList<Element>() ;
        }
    } ;

    private static class ArrayElement implements Element {
        private String componentName ;
        private int size ;
        private int index ;

        public ArrayElement( final String componentName, final int size ) {
            this.componentName = componentName ;
            this.size = size ;
            this.index = -1 ;
        }

        public void setIndex( final int index ) {
            this.index = index ;
        }

        public String getAsString() {
            if (index < 0) {
                return componentName + '<' + size + '>' ;
            } else {
                return componentName + '<' + size + ">["  + index + ']' ;
            }
        }
    }

    private static class ValueElement implements Element {
        private String valueName ;
        private String fieldName ;

        public ValueElement( final String valueName ) {
            this.valueName = valueName ;
            this.fieldName = null ;
        }

        public void setFieldName( final String fieldName ) {
            this.fieldName = fieldName ;
        }

        public String getAsString() {
            if (fieldName == null) {
                return valueName ;
            } else {
                return valueName + '.' + fieldName ;
            }
        }
    }

    private static class GenericElement implements Element {
        private String name ;
        private Object[] data ;

        public GenericElement( final String name, final Object[] data ) {
            this.name = name ;
            this.data = data ;
        }

        public String getAsString() {
            StringBuilder sb = new StringBuilder() ;
            if (name == null) {
                sb.append( "!NULL_NAME!" ) ;
            } else {
                sb.append( name ) ;
            }

            sb.append( '(' ) ;
            boolean first = true ;
            for (Object obj : data) {
                if (first) {
                    first = false ;
                } else {
                    sb.append( ',' ) ;
                }

                sb.append( convertToString(obj)) ;
            }
            sb.append( ')' ) ;
            return sb.toString() ;
        }
    }

    /** Return the current contents of the OperationTracer state
     * for the current thread.
     */
    public static String getAsString() {
        final StringBuilder sb = new StringBuilder() ;
        final List<Element> elements = state.get() ;
        int count = 0 ;
        for (Element elem : elements) {
            if (count == 0) {
                sb.append( elem.getAsString() ) ;
                sb.append( ':' ) ;
            } else if (count == 1) {
                sb.append( elem.getAsString() ) ;
            } else {
                sb.append( ',' ) ;
                sb.append( elem.getAsString() ) ;
            }

            count++ ;
        }

        return sb.toString() ;
    }

    /** Initialize operation tracing on the caller's thread.
     * The OperationTracer is initially empty.
     */
    public static void begin( final String label ) {
        if (enabled) {
            final List<Element> elements = state.get() ;
            elements.clear() ;
            elements.add( 
                new Element() {
                    public String getAsString() {
                        return label ;
                    }
                }
            ) ;
        }
    }

    /** Terminate operation tracing on the caller's thread.
     * After this call, toString will return the empty string.
     */
    public static void finish() {
        if (enabled) {
            state.get().clear() ;
        }
    }

    /** Push a record into the trace of the start of reading a value of the
     * given type name.
     */
    public static void startReadValue( final String name ) {
        if (enabled) {
            state.get().add( new ValueElement( name ) ) ;
        }
    }

    /** Update the current Value record to indicate the field currently being
     * read.
     */
    public static void readingField( final String fieldName ) {
        if (enabled) {
            final List<Element> elements = state.get() ;
            final int lastIndex = elements.size() - 1 ;

            if (lastIndex >= 0) {
                final Element elem = elements.get( lastIndex ) ;
                if (elem instanceof ValueElement) {
                    ValueElement ve = (ValueElement)elem ;
                    ve.setFieldName( fieldName ) ;
                }
            }
        }
    }

    /** Pop the record of the current value that was just read.
     */
    public static void endReadValue() {
        if (enabled) {
            end() ;
        }
    }

    public static void startReadArray( final String name, final int size ) {
        if (enabled) {
            state.get().add( new ArrayElement( name, size ) ) ;
        }
    }

    public static void readingIndex( final int index ) {
        if (enabled) {
            final List<Element> elements = state.get() ;
            final int lastIndex = elements.size() - 1 ;

            if (lastIndex >= 0) {
                final Element elem = elements.get( lastIndex ) ;

                if (elem instanceof ArrayElement) {
                    ArrayElement ae = (ArrayElement)elem ;
                    ae.setIndex( index ) ;
                }
            }
        }
    }

    public static void endReadArray() {
        if (enabled) {
            end() ;
        }
    }

    private static void end() {
        final List<Element> elements = state.get() ;
        final int lastIndex = elements.size() - 1 ;
        if (lastIndex >= 0) {
            elements.remove( lastIndex ) ;
        }
    }

    public static void clear() {
        if (enabled) {
            state.get().clear() ;
        }
    }

    public static void enter( final String name, final Object... args ) {
        if (enabled) {
            state.get().add( new GenericElement( name, args ) ) ;    
        }
    }

    public static void exit() {
        if (enabled) {
            end() ;
        }
    }
}
