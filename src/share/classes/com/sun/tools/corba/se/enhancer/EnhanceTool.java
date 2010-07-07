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

package com.sun.tools.corba.se.enhancer ;

import com.sun.corba.se.impl.orbutil.newtimer.TimerPointSourceGenerator;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;
import com.sun.corba.se.spi.orbutil.argparser.Help ;
import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.file.ActionFactory;
import com.sun.corba.se.spi.orbutil.file.Scanner ;
import com.sun.corba.se.spi.orbutil.file.Recognizer ;
import com.sun.corba.se.spi.orbutil.file.FileWrapper ;
import com.sun.corba.se.spi.orbutil.generic.Pair;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory;
import com.sun.corba.se.spi.orbutil.tf.Util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

/** Tool for enhancing classes annotated with tracing facility annotations.
 * The processing is divided into two phases:
 * <ol>
 * <li>The first phase adds code to the static initialized of the class to 
 * register the class with the TF framework.  This phase also modifies all 
 * calls to @InfoMethod annotated methods to pass extra arguments needed for
 * tracing.  Note that this phase <b>must</b> be done at build time, because it
 * modifies the schema of the class.
 * <li>The second phase adds the actual trace code to methods annotated with
 * tracing annotations.  This does not change the schema, so this phase 
 * could be done either at build time, or at runtime.  Tracing the code only
 * at runtime may reduce the overhead, since untraced code need not be modified.
 * </ol>
 * This tool can do either phase 1, or phase 1 and 2, against ALL classes 
 * reachable from a starting directory.
 * @author ken
 */
public class EnhanceTool {
    private static int errorCount = 0 ;
    private Util util ;

    public enum ProcessingMode {
        /** Only generate the timing points file: no changes to monitored class
         * bytecode.
         */
        TimingPoints,

        /** Update the class schemas for monitored classes, but don't insert
         * the tracing code.  Generate the TimingPoints file if the
         * timingPointDir and timingPointClass options are set.
         */
        UpdateSchemas,

        /** Update the class schema and insert tracing code.  Generate the
         * TimingPoints file if the timingPointDir and timingPointClass
         * options are set.
         */
        TraceEnhance
    }

    public interface Arguments {
        @DefaultValue( "tfannotations.properties" )
        @Help( "Name of resource file containing information "
           + "about tf annotations")
        File rf() ;

        @DefaultValue( "false" )
        @Help( "Debug flag" ) 
        boolean debug() ;

        @DefaultValue( "0" )
        @Help( "Verbose flag" ) 
        int verbose() ;

        @DefaultValue( "false" )
        @Help( "Indicates a run that only prints out actions, "
            + "but does not perform them")
        boolean dryrun() ;

        @DefaultValue( "." )
        @Help( "Directory to scan for class file" ) 
        File dir() ;

        @DefaultValue( "false")
        @Help( "If true, write output to a .class.new file")
        boolean newout() ;

        @DefaultValue( "TimingPoints" )
        @Help( "Control the mode of operation: TimingPoints, UpdateSchema, or TraceEnhance")
        ProcessingMode mode() ;

        @DefaultValue( "" ) 
        @Help( "The timing point class name")
        String timingPointClass() ;

        @DefaultValue( "" ) 
        @Help( "The directory in which to write the TimingPoint file")
        String timingPointDir() ;
    }

    private Arguments args ;

    private TimerPointSourceGenerator.TimingInfoProcessor tip ;

    private class EnhancerFileAction implements Scanner.Action {
        private UnaryFunction<byte[],byte[]> ea ;

        public EnhancerFileAction( UnaryFunction<byte[],byte[]> ea ) {
            this.ea = ea ;
        }

        public boolean evaluate( FileWrapper fw ) {
            try {
                util.info( 2, "Processing class " + fw.getName() ) ;
                byte[] inputData = fw.readAll() ;
                byte[] outputData = ea.evaluate( inputData ) ;
                if (outputData != null) {
                    if (args.newout()) {
                        String fname = fw.getName() + ".new" ;
                        util.info( 1, "Writing to class file " + fname ) ;
                        FileWrapper fwo = new FileWrapper( fname ) ;
                        fwo.writeAll( outputData ) ;
                    } else {
                        util.info( 1, "Writing to class file " + fw.getName() ) ;
                        fw.writeAll( outputData ) ;
                    }
                }
            } catch (Exception exc) {
                util.info( 1, "Exception " + exc + " while processing class "
                    + fw.getName() ) ;
                errorCount++ ;
            }

            // Always succeed, so we keep processing files after the first
            // error.
            return true ;
        }
    }

    private void generatePropertiesFile( Arguments args,
        Set<String> anames ) throws IOException {

        // Resource file that lists all MM annotations:
        // com.sun.corba.tf.annotations.size=n
        // com.sun.corba.tf.annotation.1=...
        // ...
        // com.sun.corba.tf.annotation.n=...
        final FileWrapper fw = new FileWrapper( args.rf() ) ;
        fw.open( FileWrapper.OpenMode.WRITE_EMPTY )  ;

        try {
            fw.writeLine( "# Trace Facility Annotations" ) ;
            fw.writeLine( "# generated by EnhanceTool on " + new Date() ) ;
            fw.writeLine( "com.sun.corba.tf.annotations.size="
                + anames.size() ) ;
            int ctr=1 ;
            for (String str : anames) {
                String cname = str.replace( '/', '.' ) ;
                fw.writeLine( "com.sun.corba.tf.annotation."
                    + ctr + "=" + cname ) ;
                ctr++ ;
            }
        } finally {
            fw.close() ;
        }
    }

    private final Scanner.Action makeIgnoreAction(
        final boolean trace ) {

        return new Scanner.Action() {
            @Override
            public String toString() {
                return "ignore action (ignore files that don't match)" ;
            }

            public boolean evaluate(FileWrapper arg) {
                if (trace) {
                    util.info( 1, "Skipping " + arg ) ;
                }

                return true ;
            }
        } ;
    }

    private void doScan( Arguments args, ActionFactory af,
        Scanner scanner, Scanner.Action classAct ) throws IOException {

        final Recognizer classRecognizer = af.getRecognizerAction() ;
        final Scanner.Action ignoreAction = makeIgnoreAction(
            args.debug() || args.verbose() > 2 ) ;
        classRecognizer.setDefaultAction( ignoreAction ) ;
        classRecognizer.addKnownSuffix( "class", classAct ) ;
        scanner.scan( classRecognizer ) ;
    }

    public void run( String[] strs ) {
        try {
            final ArgParser<Arguments> ap = new ArgParser<Arguments>(
                Arguments.class ) ;
            args = ap.parse( strs ) ;
            util = new Util( args.debug(), args.verbose() ) ;

            String fullTimingPointName = args.timingPointClass() ;
            String pkg = "" ;
            String cname = fullTimingPointName ;
            int index = fullTimingPointName.lastIndexOf('.') ;
            if (index > 0) {
                cname = fullTimingPointName.substring( index + 1 ) ;
                pkg = fullTimingPointName.substring( 0, index ) ;
            }

            tip = new TimerPointSourceGenerator.TimingInfoProcessor( 
                cname, pkg ) ;

            final ActionFactory af = new ActionFactory( 0, args.dryrun() ) ;
            final Scanner scanner = new Scanner( 0, args.dir() ) ;

            AnnotationScannerAction annoAct = new AnnotationScannerAction( util,
                tip ) ;

            doScan( args, af, scanner, annoAct ) ;

            Set<String> anames = annoAct.getAnnotationNames() ;

            if (args.debug()) {
                util.info( 1, "MM Annotations: " + anames ) ;
            }

            generatePropertiesFile( args, anames ) ;

            Transformer ea = new Transformer( util, 
                args.mode(), tip, anames ) ;

            final Scanner.Action act = new EnhancerFileAction( ea ) ;

            doScan( args, af, scanner, act ) ;

            Pair<String,TimerFactory> res = tip.getResult() ;

            if (!args.timingPointDir().equals( "" ) ) {
                TimerPointSourceGenerator.generateSingleFile(
                    args.timingPointDir(), res );
            }
        } catch (Exception exc) {
            if (util == null) {
                util = new Util( true, 1 ) ;
            }

            util.info( 1, "Exception: " + exc ) ;
            if (args.debug()) {
                exc.printStackTrace() ;
            }
        }
    }

    public static void main( String[] strs ) {
        (new EnhanceTool()).run( strs ) ;
        if (errorCount > 0) {
            System.exit(errorCount);
        }
    }
}
