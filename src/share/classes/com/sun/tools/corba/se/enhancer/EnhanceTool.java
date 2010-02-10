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

import com.sun.corba.se.spi.orbutil.file.Scanner ;
import com.sun.corba.se.spi.orbutil.file.Recognizer ;
import com.sun.corba.se.spi.orbutil.file.FileWrapper ;

public class EnhanceTool {
    private interface Arguments {
        @DefaultValue( "false" )
        @Help( "Debug flag" ) 
        boolean debug() ;

        @DefaultValue( "false" )
        @Help( "Verbose flag" ) 
        boolean verbose() ;

        @DefaultValue( "." )
        @Help( "Directory to scan for class file" ) 
        File dir() ;

        @DefaultValue( "trace" )
        @Help( "Class file enhance action to use"
        String action() ;
    }

    private static Arguments args ;

    public interface EnhanceAction extends UnaryFunction<byte[],byte[]> { }

    private static class EnhancerFileAction implements Scanner.Action {
        private EnhanceAction ea ;

        public EnhancerFileAction( EnhanceAction ea ) {
            this.ea = ea ;
        }

        public boolean evaluate( FileWrapper fw ) {
            byte[] inputData = fw.readAll() ;
            byte[] outputData = ea.evaluate( inputData ) ;
            if (outputData != null) {
                fw.writeAll() ;
            }
        }
    }

    public static void main( String[] strs ) {
        ArgParser<Arguments> ap = new ArgParser( Arguments.class ) ;
        args = ap.parse( strs ) ;
    
        // XXX Get EnhanceAction from resource file
        final EnhanceAction ea = null ;

        final Scanner.Action act = new EnhancerFileAction( ea ) ;

        final Recognizer classRecognizer = new Recognizer() ;
        classRecognizer.addKnownSuffix( "class", act ) ;

        final Scanner scanner = new Scanner( args.verbose(), args.dir() ) ;
        scanner.scan( classRecognizer ) ;
    }
}
