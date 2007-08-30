/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.codegen ;

import java.io.File ;
import java.io.IOException ;
import java.io.FileInputStream ;
import java.io.PrintStream ;

import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;

import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;

import com.sun.corba.se.impl.codegen.CodeGeneratorUtil ;

import static com.sun.corba.se.spi.codegen.Wrapper.* ;

public class JavaCodeGenerator extends CodeGeneratorBase {
    private File directory ;
    private long sourceGenerationTime ;
    private long compilationTime ;
    private static final boolean CLEANUP_DIRECTORY = false ;
    public static String dirName = "work." + System.currentTimeMillis() ;

    private void deleteFiles( File f ) {
	if (f.isDirectory()) {
	    for (File file : f.listFiles())
		deleteFiles( file ) ;
	}

	if (!f.delete())
	    throw new RuntimeException( "Could not delete " + f ) ;
    }

    private String pathName() {
	return className().replace( '.', File.separatorChar ) ;
    }

    private String containerName() {
	String pn = pathName() ;

	int len = pn.lastIndexOf( File.separatorChar ) ;
	if (len == -1)
	    return "" ;

	String name = pn.substring( 0, len ) ;
	return name ;
    }

    private String sourceName() {
	return pathName() + ".java" ;
    }

    private String classFileName() {
	return pathName() + ".class" ;
    }

    public JavaCodeGenerator( ClassGeneratorFactory cgf ) {
	super( cgf ) ;

	File baseDir = new File( "gen" ) ;
	directory = new File( baseDir, dirName ) ;
	directory.mkdir() ; // may fail because it already exists, which is OK.

	// Make sure the directory for the generated source 
	// and compiled class file exists.
	String cn = containerName() ;
	File classDir ;
	if (cn.equals( "" )) 
	    classDir = directory ;
	else
	    classDir = new File( directory, containerName() ) ;

	classDir.mkdirs() ; // may fail because it already exists, which is OK.
    }

    /** Compile the java source file for the named class in the 
     * directory.  File are compiled in the same directory as
     * their source.
     */
    private int compileClass( File dir, String name ) {
	// Classpath also needs the gen directory!
	String classpath = directory + System.getProperty( "path.separator" ) 
	    + System.getProperty( "java.class.path" ) ;

	String[] args = new String[] {
	    "-g",
	    "-classpath",
	    classpath,
	    (new File( dir, name )).toString() 
	} ;

	// This peculiar construction is done deliberately
	// to defeat the ORB renaming mechanism.
	try {
	    // Non-portable, but works on all Sun JDKs >= 5.
	    String compilerMainClass = "com.sun." + "tools.javac." +
		"main.Main" ;
	    Class compilerMain = Class.forName( compilerMainClass ) ;
	    Constructor cons = compilerMain.getConstructor(
		String.class ) ;
	    Object main = cons.newInstance( "TestCompiler" ) ;
	    Method compile = compilerMain.getMethod( "compile",
		String[].class ) ;
	    int result = Integer.class.cast(
		compile.invoke( main, (Object)args ) ) ;
	    if (result != 0)
		System.out.println( "Compilation of class " + name +
		    " failed with result " + result ) ;
	    return result ;
	} catch (Exception exc) {
	    System.out.println( "Compilation of class " + name +
		" failed with exception " + exc ) ;
	    exc.printStackTrace() ;
	    return 1 ;
	}
    }

    /** Load the class file for the named class from the file.
     */
    private Class<?> loadClass( ClassLoader cl, File cfile, 
	String className ) {
	FileInputStream fis = null ;

	try {
	    fis = new FileInputStream( cfile ) ;
	    int fileSize = fis.available() ; 
	    byte[] cdata = new byte[fileSize] ;
	    fis.read( cdata ) ;
	    return CodeGeneratorUtil.makeClass( className, cdata, null, cl ) ;
	} catch (IOException exc) {
	    throw new RuntimeException( "Caught exception in loadClass", exc ) ;
	} finally {
	    if (fis != null)
		try {
		    fis.close() ;
		} catch (IOException exc) {
		    // IGNORE IOException on close
		}
	}
    }

    public Class generate( ClassLoader loader ) {
	// System.out.println( System.getProperty( "user.dir" )) ;
	PrintStream ps = null ;
	try {
	    String str = sourceName() ;
	    File out = new File( directory, str ) ;
	    ps = new PrintStream( out ) ;

	    long start = System.nanoTime() ;
	    try {
		_sourceCode( ps, null ) ;
	    } finally {
		sourceGenerationTime = (System.nanoTime() - start)/1000 ;
	    }
	} catch (IOException exc) {
	    exc.printStackTrace() ;
	    throw new RuntimeException( "Caught exception in generate", exc ) ;
	} finally {
	    if (ps != null)
		ps.close() ;
	}

	long start = System.nanoTime() ;
	try {
	    if (compileClass( directory, sourceName()) != 0)
		throw new RuntimeException( "Compilation of " + sourceName() 
		    + " failed." ) ;

	    return loadClass( loader, 
		new File( directory, classFileName()), className() ) ;
	} finally {
	    compilationTime = (System.nanoTime() - start)/1000 ;

	    if (CLEANUP_DIRECTORY)
		deleteFiles( directory ) ;
	}
    }

    public long sourceGenerationTime() {
	return sourceGenerationTime ;
    }

    public long compilationTime() {
	return compilationTime ;
    }
}
