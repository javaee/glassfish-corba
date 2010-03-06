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

import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;
import com.sun.corba.se.spi.orbutil.argparser.Help ;
import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.file.ActionFactory;

import com.sun.corba.se.spi.orbutil.file.Scanner ;
import com.sun.corba.se.spi.orbutil.file.Recognizer ;
import com.sun.corba.se.spi.orbutil.file.FileWrapper ;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class EnhanceTool {
    private interface Arguments {
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

        @DefaultValue( "trace" )
        @Help( "Class file enhance action to use" )
        String action() ;

        @DefaultValue( "false")
        @Help( "If true, write output to a .class.new file")
        boolean newout() ;

        @DefaultValue( "true" )
        @Help( "If true, do second phase (add tracing code) processing")
        boolean tracegen() ;
    }

    private Arguments args ;

    public interface StandardSupport {
        void setDebug( boolean flag ) ;

        void setVerbose( int level ) ;

        void setDryrun( boolean flag ) ;
    }

    public interface ScanAction extends Scanner.Action, StandardSupport {}

    public interface EnhanceFunction extends UnaryFunction<byte[],byte[]>,
        StandardSupport {
    }

    private class EnhancerFileAction implements Scanner.Action {
        private EnhanceFunction ea ;

        public EnhancerFileAction( EnhanceFunction ea ) {
            this.ea = ea ;
        }

        public boolean evaluate( FileWrapper fw ) {
            try {
                byte[] inputData = fw.readAll() ;
                byte[] outputData = ea.evaluate( inputData ) ;
                if (outputData != null) {
                    if (args.newout()) {
                        String fname = fw.getName() + ".new" ;
                        FileWrapper fwo = new FileWrapper( fname ) ;
                        fwo.writeAll( outputData ) ;
                    } else {
                        fw.writeAll( outputData ) ;
                    }
                }
                return true ;
            } catch (IOException exc) {
                return false ;
            }
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
        fw.open( FileWrapper.OpenMode.WRITE )  ;

        try {
            fw.writeLine( "# Trace Facility Annotations" ) ;
            fw.writeLine( "# generated by EnhanceTool on " + new Date() ) ;
            fw.writeLine( "com.sun.corba.tf.annotations.size="
                + anames.size() ) ;
            int ctr=1 ;
            for (String str : anames) {
                fw.writeLine( "com.sun.corba.tf.annotation."
                    + ctr + "=" + str ) ;
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
                    System.out.println( "Skipping " + arg ) ;
                }

                return true ;
            }
        } ;
    }

    private void setArgs( Arguments args, StandardSupport ss ) {
        ss.setDebug( args.debug() );
        ss.setVerbose( args.verbose() );
        ss.setDryrun( args.dryrun() );
    }

    private void doScan( Arguments args, ActionFactory af,
        Scanner scanner, Scanner.Action classAct ) throws IOException {

        final Recognizer classRecognizer = af.getRecognizerAction() ;
        final Scanner.Action ignoreAction = makeIgnoreAction(
            args.debug() || args.verbose() > 0 ) ;
        classRecognizer.setDefaultAction( ignoreAction ) ;
        classRecognizer.addKnownSuffix( "class", classAct ) ;
        scanner.scan( classRecognizer ) ;
    }

    public void run( String[] strs ) {
        try {
            final ArgParser<Arguments> ap = new ArgParser<Arguments>(
                Arguments.class ) ;
            args = ap.parse( strs ) ;

            final ActionFactory af = new ActionFactory( args.verbose(),
                args.dryrun() ) ;
            final Scanner scanner = new Scanner( args.verbose(), args.dir() ) ;

            AnnotationScannerAction annoAct = new AnnotationScannerAction() ;
            setArgs( args, annoAct ) ;

            doScan( args, af, scanner, annoAct ) ;

            Set<String> anames = annoAct.getAnnotationNames() ;

            if (args.debug()) {
                System.out.println( "MM Annotations: " + anames ) ;
            }

            generatePropertiesFile( args, anames ) ;

            Transformer ea = new Transformer( args.tracegen() ) ;
            setArgs( args, ea ) ;
            ea.setMMGAnnotations( anames );

            final Scanner.Action act = new EnhancerFileAction( ea ) ;

            doScan( args, af, scanner, act ) ;
        } catch (Exception exc) {
            System.out.println( "Exception: " + exc ) ;
            exc.printStackTrace() ;
        }
    }

    public static void main( String[] strs ) {
        (new EnhanceTool()).run( strs ) ;
    }
}
