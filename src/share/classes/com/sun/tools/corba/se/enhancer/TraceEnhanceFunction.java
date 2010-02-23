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

package com.sun.tools.corba.se.enhancer;

import java.util.Set;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;

import org.objectweb.asm.ClassAdapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/** ClassFile enhancer for the tracing facility.  This modifies the bytecode
 * for an applicable class, then returns the updated bytecode.
 * Makes extensive use of the ASM library.
 *
 * This is split into two parts.  The first part modifies the schema
 * of the class as follows:
 * <ul>
 * <li>Adds static fields as required for the SynchronizedHolder<MethodMonitor> 
 * instances.
 * <li>Modifies the static initializer to set up the new fields, and register
 * the class with the MethodMonitorRegistry.  This also constructs the list
 * of method names, which is needed by the second part.
 * <li>Re-writes all @InfoMethod methods to take two extra parameters at the
 * end of their argument lists.
 * <li>Re-writes all calls to @InfoMethod methods to supply the two extra 
 * parameters to all calls.
 * <li>Checks that @InfoMethod methods (which must be private) are only called
 * from MM annotated methods.
 * </ul>
 * <p>
 * The second part modifies the MM annotated methods as follows:
 * <li>Adds a preamble to set up some local variables, and to call 
 * the MethodMonitor.enter method when active.
 * <li>Adds a finally block at the end of the method that handles calling
 * MethodMonitor.exit whenever an exception is thrown or propagated from the
 * body of the method.
 * <li>Modifies all exit point in the method as follows:
 * <ul>
 * <li>If the exit point is a return, call MethodMonitor.exit before the return.
 * <li>If the exit point is a throw, call MethodMonitor.exception before the throw.
 * </ul>
 * </ul>
 * <p>
 * Note that the second part could be run in a ClassFileTransformer or ClassLoader
 * if desired, since this design enhances the class files in place for the first
 * part.
 *
 * @author ken
 */
public class TraceEnhanceFunction implements EnhanceTool.EnhanceFunction {
    private boolean dryrun ;
    private Set<String> annotationNames = null ;

    private Util util = new Util();

    // These fields are initialized in the evaluate method.
    private ClassNode currentClass = null ;
    private EnhancedClassData ecd = null ;

    public TraceEnhanceFunction() {
    }

    public void setMMGAnnotations(Set<String> mmgAnnotations) {
        annotationNames = mmgAnnotations ;
    }

    public void setDebug(boolean flag) {
        util.setDebug( flag ) ;
    }

    public void setVerbose(int level) {
        util.setVerbose(level);
    }

    public void setDryrun(boolean flag) {
        dryrun = flag ;
    }

    private boolean hasAccess( int access, int flag ) {
        return (access & flag) == flag ;
    }

    public byte[] evaluate( final byte[] arg) {
        final ClassNode cn = new ClassNode() ;
        final ClassReader cr = new ClassReader( arg ) ;
        cr.accept( cn, 0 ) ;

        // Ignore annotations and interfaces.
        if (hasAccess(cn.access, Opcodes.ACC_ANNOTATION) ||
            hasAccess(cn.access, Opcodes.ACC_INTERFACE)) {
            return null ;
        }

        // We need EnhancedClassData to hold the results of scanning the class
        // for various details about annotations.  This makes it easy to write
        // a one-pass visitor in part 2 to actually add the tracing code.
        // Note that the ECD can easily be computed either at build time
        // from the classfile byte[] (using ASM), or at runtime, directly
        // from a Class object using reflection.
        ecd = new EnhancedClassDataASMImpl( util, annotationNames, cn ) ;

        // If this class is not annotated as a traced class, ignore it.
        if (!ecd.isTracedClass()) {
            return null ;
        }

        final byte[] phase1 = util.transform( arg,
            new UnaryFunction<ClassWriter, ClassAdapter>() {
                public ClassAdapter evaluate(ClassWriter arg) {
                    return new ClassEnhancer( util, ecd, arg ) ;
                }
            }
        ) ;

        // Only communication from part 1 to part2 is a byte[] and
        // the EnhancedClassData.  A runtime version can be regenerated
        // as above from the byte[] from the class file as it is presented
        // to a ClassFileTransformer.
        //     Implementation note: runtime would keep byte[] stored of
        //     original version whenever tracing is enabled, so that
        //     disabling tracing simply means using a ClassFileTransformer
        //     to get back to the original code.
        //
        // Then add tracing code (part 2).
        //     This is a pure visitor using the AdviceAdapter.
        //     It must NOT modify its input visitor (or you get an
        //     infinite series of calls to onMethodExit...)

        final byte[] phase2 = util.transform( phase1,
            new UnaryFunction<ClassWriter, ClassAdapter>() {

            public ClassAdapter evaluate(ClassWriter arg) {
                return new ClassTracer( util, ecd, arg ) ;
            }
        }) ;

        return phase2 ;
    }
}
