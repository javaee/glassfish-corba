/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.tf;

import com.sun.corba.se.spi.orbutil.generic.Algorithms;
import com.sun.corba.se.spi.orbutil.generic.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ken
 */
public class MethodMonitorFactoryDefaults {
    private MethodMonitorFactoryDefaults() {}

    private static Map<String,String> prefixTable =
        new HashMap<String,String>() ;

    /** Add a new Package prefix symbol to the table.  This reduces the
     * size of the generated messages in the dprintImpl output.
     * 
     * @param pkg A Java package name. Should end in '.' (which will be added
     * if not present). 
     * @param symbol The symbol to substitute for the package.  Typically this
     * is 3-8 upper case characters.
     */
    public static void addPrefix( String pkg, String symbol ) {
        String str ;
        if (pkg.charAt( pkg.length() - 1) == '.') {
            str = pkg ;
        } else {
            str = pkg + '.' ;
        }

        prefixTable.put( pkg, symbol ) ;
    }

    private static String compressClassName( String name ) {
        for (Map.Entry<String,String> entry : prefixTable.entrySet()) {
            if (name.startsWith( entry.getKey() )) {
                return "(" + entry.getValue() + ")." +
                    name.substring( entry.getKey().length() ) ;
            }
        }

        return name ;
    }

    // XXX define me
    private static MethodMonitorFactory operationTracerImpl = null ;

    public static class OperationTracer {
        private static boolean enabled = true ;

        public static void enable() {
            enabled = true ;
        }

        public static void disable() {
            enabled = false ;
        }

        private OperationTracer() {}

        private static ThreadLocal<List<Pair<String,Object[]>>> state =
            new ThreadLocal<List<Pair<String,Object[]>>>() {
            @Override
            public List<Pair<String,Object[]>> initialValue() {
                return new ArrayList<Pair<String,Object[]>>() ;
            }
        } ;

        private static String format( final Pair<String,Object[]> arg ) {
            String name = arg.first() ;
            Object[] args = arg.second() ;
            StringBuilder sb = new StringBuilder() ;
            if (name == null) {
                sb.append( "!NULL_NAME!" ) ;
            } else {
                sb.append( name ) ;
            }

            sb.append( '(' ) ;
            boolean first = true ;
            for (Object obj : args ) {
                if (first) {
                    first = false ;
                } else {
                    sb.append( ',' ) ;
                }

                sb.append( Algorithms.convertToString(obj)) ;
            }
            sb.append( ')' ) ;
            return sb.toString() ;
        }

        /** Return the current contents of the OperationTracer state
         * for the current thread.
         * @return State of the OperationTracer.
         */
        public static String getAsString() {
            final StringBuilder sb = new StringBuilder() ;
            final Formatter fmt = new Formatter( sb ) ;
            final List<Pair<String,Object[]>> elements = state.get() ;
            int ctr = 0 ;
            for (Pair<String,Object[]> elem : elements) {
                fmt.format( "\n\t(%3d): %s", ctr++, format( elem ) ) ;
            }

            return sb.toString() ;
        }

        public static void clear() {
            if (enabled) {
                state.get().clear() ;
            }
        }

        public static void enter( final String name, final Object... args ) {
            if (enabled) {
                state.get().add( new Pair<String,Object[]>( name, args ) ) ;
            }
        }

        public static void exit() {
            if (enabled) {
                final List<Pair<String,Object[]>> elements = state.get() ;
                int size = elements.size() ;
                if (size > 0) {
                    elements.remove( size - 1 ) ;
                }
            }
        }
    }

    private static MethodMonitorFactory dprintImpl = new MethodMonitorFactory() {
        private static final boolean USE_LOGGER = false ;
        public MethodMonitor create( final Class<?> cls ) {
            return new MethodMonitorBase( cls ) {
                final String loggerName =
                    USE_LOGGER ? cls.getPackage().getName()
                               : null ;

                final String sourceClassName = compressClassName(
                    cls.getName() ) ;

                public synchronized void dprint(String mname, String msg) {
                    String prefix = "(" + Thread.currentThread().getName() 
                        + "): " ;

                    if (USE_LOGGER) {
                        Logger.getLogger( loggerName ).
                            logp( Level.INFO, prefix + msg, sourceClassName,
                                mname ) ;
                    } else {
                        System.out.println( prefix + sourceClassName
                            + "." + mname + msg ) ;
                    }
                }

                private String makeString( Object... args ) {
                    if (args.length == 0) {
                        return "";
                    }

                    StringBuilder sb = new StringBuilder() ;
                    sb.append( '(' ) ;
                    boolean first = true ;
                    for (Object obj : args) {
                        if (first) {
                            first = false ;
                        } else {
                            sb.append( ' ' ) ;
                        }

                        sb.append( Algorithms.convertToString(obj)) ;
                    }
                    sb.append( ')' ) ;

                    return sb.toString() ;
                }

                @Override
                public void enter( Object ident, Object... args ) {
                    String mname = MethodMonitorRegistry.getMethodName( cls,
                        ident ) ;
                    String str = makeString( args ) ;
                    dprint( mname, "->" + str ) ;
                }

                @Override
                public void exception( Object ident, Throwable thr ) {
                    String mname = MethodMonitorRegistry.getMethodName( cls,
                        ident ) ;
                    dprint( mname, ":throw:" + thr ) ;
                }

                @Override
                public void info( Object[] args, Object callerId,
                    Object selfId ) {

                    String mname = MethodMonitorRegistry.getMethodName( cls,
                        callerId ) ;
                    String infoName = MethodMonitorRegistry.getMethodName( cls, 
                        selfId) ;
                    String str = makeString( args ) ;
                    dprint( mname, "::(" + infoName + ")" + str ) ;
                }

                @Override
                public void exit( Object ident ) {
                    String mname = MethodMonitorRegistry.getMethodName( cls,
                        ident ) ;
                    dprint( mname, "<-" ) ;
                }

                @Override
                public void exit( Object ident, Object retVal ) {
                    String mname = MethodMonitorRegistry.getMethodName( cls,
                        ident ) ;
                    dprint( mname, "<-(" + retVal + ")" ) ;
                }

                @Override
                public void clear() {
                    // NO-OP
                }
            } ;
        }
    } ;

    private static MethodMonitorFactory noOpImpl =
        new MethodMonitorFactory() {
            public MethodMonitor create(final Class<?> cls) {
                return new MethodMonitorBase( cls ) {
                    public void enter(Object ident, Object... args) { }

                    public void info(Object[] args, Object callerId,
                        Object selfId ) { }

                    public void exit(Object ident) { }

                    public void exit(Object ident, Object result) { }

                    public void exception(Object ident, Throwable thr) { }

                    public void clear() { }
                } ;
            }
        } ;

    public static MethodMonitorFactory operationTracer() {
        return operationTracerImpl ;
    }

    public static MethodMonitorFactory noOp() {
        return noOpImpl ;
    }

    public static MethodMonitorFactory dprint() {
        return dprintImpl ;
    }

    public static MethodMonitorFactory compose(
        final Collection<MethodMonitorFactory> factories ) {

        return new MethodMonitorFactory() {
            public MethodMonitor create(final Class<?> cls) {
                if (factories.size() == 0) {
                    // null is a very efficient no-op indicator
                    return null ;
                } else if (factories.size() == 1) {
                    MethodMonitorFactory mmf = factories.toArray( 
                        new MethodMonitorFactory[1])[0] ;
                    return mmf.create( cls ) ;
                } else {
                    final List<MethodMonitor> mms = new ArrayList<MethodMonitor>() ;
                    for (MethodMonitorFactory f : factories) {
                        mms.add( f.create( cls ) ) ;
                    }

                    return new MethodMonitorBase( cls ) {
                        public void enter(Object ident, Object... args) {
                            for (MethodMonitor mm : mms) {
                                mm.enter( ident, args ) ;
                            }
                        }

                        public void info( Object[] args, Object callerId,
                            Object selfId ) {

                            for (MethodMonitor mm : mms) {
                                mm.info( args, callerId, selfId ) ;
                            }
                        }

                        public void exit(Object ident) {
                            for (MethodMonitor mm : mms) {
                                mm.exit( ident ) ;
                            }
                        }

                        public void exit(Object ident, Object result) {
                            for (MethodMonitor mm : mms) {
                                mm.exit( ident, result ) ;
                            }
                        }

                        public void exception(Object ident, Throwable thr) {
                            for (MethodMonitor mm : mms) {
                                mm.exception( ident, thr ) ;
                            }
                        }

                        public void clear() {
                            for (MethodMonitor mm : mms) {
                                mm.clear() ;
                            }
                        }
                    } ;
                }
            }
        } ;
    }
}
