/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.se.spi.orbutil.file;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Given a pathname of a Mercurial repository, analyze the contents to
 * determine the first and last years in which a file in the repository was
 * modified.
 *
 * @author ken
 */
public class ModificationAnalyzer implements Iterable<String> {
    private String _firstYear = null ;
    private String _lastYear = null ;
    private final Map<String,String> fileFirstYear =
        new HashMap<String, String>();
    private final Map<String,String> fileLastYear =
        new HashMap<String, String>();

    /** Initialize the ModificationAnalyzer for all files in the
     * Mercurial repository at pathname root.
     * This analyzes the output of hg log to determine the first and
     * last modification years of every file in the repository.
     * The earliest and latest modification years for each file are
     * also available.
     * @param root Absolute pathname of the repository
     * @throws IllegalArgumentException if root is not a mercurial repository.
     */
    public ModificationAnalyzer( String root ) {
        String prefix = root ;
        if (root.charAt(root.length()-1) != '/') {
            prefix += '/' ;
        }

        try {
            final Block block =
                new Block("hg log " + root);
            final String error = block.find("abort:");
            if (error != null) {
                throw new IllegalArgumentException( "Error in hg log command: "
                    + error ) ;
            }

            // Format: first "^date:", then "^files:" is the next line.
            // date:     Mon Feb 08 11:32:56  2011 -0800
            // files:    blank-separated list of file names relative to the root
            String year = "" ;
            for (String str : block) {
                if (str.startsWith( "date:" )) {
                    String[] tokens = str.split( " +" ) ;
                    year = tokens[5] ;
                    if (_firstYear == null) {
                        _firstYear = year ;
                    }
                    _lastYear = year ;
                }

                if (str.startsWith( "files:" )) {
                    String[] fnames = str.split( " +" ) ;
                    boolean first = true ;
                    for (String fname : fnames) {
                        if (first) {
                            first = false ;
                        } else {
                            final String fullName = prefix + fname ;
                            if (!fileLastYear.containsKey(fullName)) {
                                fileLastYear.put( fullName, year ) ;
                            }
                            fileFirstYear.put( fullName, year ) ;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException( "IOException on root " + root, ex ) ;
        }
    }

    /** Year of the first changeset in the log.
     *
     * @return Year
     */
    public String firstYear() {
        return _firstYear ;
    }

    /** Year of the last changeset in the log.
     *
     * @return Year
     */
    public String lastYear() {
        return _lastYear ;
    }

    /** Return the first modification year of the file given
     * by the absolute pathname fname.
     * @param fname absolute pathname of file in repository.
     * @return Year
     * @throws IllegalArgumentException if the file is not in the
     * repository.
     */
    public String firstModification( String fname ) {
        return fileFirstYear.get(fname) ;
    }

    /** Return the last modification year of the file given
     * by the absolute pathname fname.
     * @param fname absolute pathname of file in repository.
     * @return Year
     * @throws IllegalArgumentException if the file is not in the
     * repository.
     */
    public String lastModification( String fname ) {
        return fileLastYear.get(fname) ;
    }

    public Iterator<String> iterator() {
        return fileFirstYear.keySet().iterator() ;
    }

    private static void msg( String str ) {
        System.out.println( str ) ;
    }

    public static void main( String[] args ) {
        ModificationAnalyzer ma = new ModificationAnalyzer( args[0] ) ;
        msg( "firstYear() = " + ma.firstYear() ) ;
        msg( "lastYear() = " + ma.lastYear() ) ;
        for (String fname : ma) {
            final String first = ma.firstModification( fname ) ;
            final String last = ma.lastModification( fname ) ;
            msg( "file: " + fname ) ;
            msg( "\tfirst year: " + first ) ;
            msg( "\tlast year : " + last ) ;
        }
    }
}
