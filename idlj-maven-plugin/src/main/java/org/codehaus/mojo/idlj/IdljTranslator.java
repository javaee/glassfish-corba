package org.codehaus.mojo.idlj;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringOutputStream;
import org.codehaus.plexus.util.StringUtils;

/**
 * This class implement the <code>CompilerTranslator</code> for the Sun idlj IDL compiler
 *
 * @author Anders Hessellund Jensen <ahj@trifork.com>
 * @version $Id: IdljTranslator.java 9344 2009-04-03 22:25:02Z aheritier $
 */
public class IdljTranslator
    extends AbstractTranslator
    implements CompilerTranslator
{

    /**
     * Default constructor
     */
    public IdljTranslator()
    {
        super();
    }

    /**
     * This method it's used to invoke the compiler
     *
     * @param sourceDirectory the path to the sources
     * @param includeDirs the <code>File[]</code> of directories where to find the includes
     * @param targetDirectory the path to the destination of the compilation
     * @param idlFile the path to the file to compile
     * @param source the source tag available in the configuration tree of the maven plugin
     * @throws MojoExecutionException the exception is thrown whenever the compilation fails or crashes
     * @see CompilerTranslator#invokeCompiler(String, List, String, String, Source)
     */
    public void invokeCompiler( String sourceDirectory, File[] includeDirs, String targetDirectory, String idlFile,
                                Source source )
        throws MojoExecutionException
    {
        List args = new ArrayList();
        args.add( "-i" );
        args.add( sourceDirectory );

        // add idl files from other directories as well
        if ( includeDirs != null && includeDirs.length > 0 )
        {
            for ( int i = 0; i < includeDirs.length; i++ )
            {
                args.add( "-i" );
                args.add( includeDirs[i].toString() );
            }
        }

        args.add( "-td" );
        args.add( toRelativeAndFixSeparator( new File( System.getProperty( "user.dir" ) ), new File( targetDirectory ),
                                             false ) );

        if ( source.getPackagePrefix() != null )
        {
            throw new MojoExecutionException( "idlj compiler does not support packagePrefix" );
        }

        if ( source.getPackagePrefixes() != null )
        {
            for ( Iterator prefixes = source.getPackagePrefixes().iterator(); prefixes.hasNext(); )
            {
                PackagePrefix prefix = (PackagePrefix) prefixes.next();
                args.add( "-pkgPrefix" );
                args.add( prefix.getType() );
                args.add( prefix.getPrefix() );
            }
        }

        if ( source.getDefines() != null )
        {
            for ( Iterator defs = source.getDefines().iterator(); defs.hasNext(); )
            {
                Define define = (Define) defs.next();
                if ( define.getValue() != null )
                {
                    throw new MojoExecutionException( "idlj compiler unable to define symbol values" );
                }
                args.add( "-d" );
                args.add( define.getSymbol() );
            }
        }

        if ( source.emitStubs() != null && source.emitStubs().booleanValue() )
        {
            if ( source.emitSkeletons().booleanValue() )
            {
                args.add( "-fall" );
            }
            else
            {
                args.add( "-fclient" );
            }
        }
        else
        {
            if ( source.emitSkeletons() != null && source.emitSkeletons().booleanValue() )
            {
                args.add( "-fserver" );
            }
            else
            {
                args.add( "-fserverTIE" );
            }
        }

        if ( source.compatible() != null && source.compatible().booleanValue() )
        {
            String version = System.getProperty( "java.specification.version" );
            getLog().debug( "JDK Version:" + version );
            // TODO A compiled REGEX should be used instead of the matches()
            // method
            if ( version.matches( "^[0-1]\\.[0-3]" ) )
            {
                getLog().debug( "OPTION IGNORED: compatible" );
            }
            else
            {
                args.add( "-oldImplBase" );
            }
        }

        if ( source.getAdditionalArguments() != null )
        {
            for ( Iterator it = source.getAdditionalArguments().iterator(); it.hasNext(); )
            {
                args.add( it.next() );
            }
        }

        args.add( idlFile );

        Class compilerClass = getCompilerClass();
        invokeCompiler( compilerClass, args );
    }

    /**
     * @return the <code>Class</code> that implements the idlj compiler
     * @throws MojoExecutionException if the search for the class fails
     */
    private Class getCompilerClass()
        throws MojoExecutionException
    {
        ClassLoader cl = this.getClass().getClassLoader();
        Class idljCompiler;
        try
        {
            idljCompiler = Class.forName( getIDLCompilerClass() );
        }
        catch ( ClassNotFoundException e )
        {
            try
            {
                File javaHome = new File( System.getProperty( "java.home" ) );
                File toolsJar = new File( javaHome, "../lib/tools.jar" );
                URL toolsJarUrl = toolsJar.toURL();
                URLClassLoader urlLoader = new URLClassLoader( new URL[] { toolsJarUrl }, cl );

                // Unfortunately the idlj compiler reads messages using the
                // system class path.
                // Therefore this really nasty hack is required.
                System.setProperty( "java.class.path", System.getProperty( "java.class.path" )
                    + System.getProperty( "path.separator" ) + toolsJar.getAbsolutePath() );
                if ( System.getProperty( "java.vm.name" ).indexOf( "HotSpot" ) != -1 )
                {
                    urlLoader.loadClass( "com.sun.tools.corba.se.idl.som.cff.FileLocator" );
                }
                idljCompiler = urlLoader.loadClass( getIDLCompilerClass() );
            }
            catch ( Exception notUsed )
            {
                throw new MojoExecutionException( " IDL compiler not available", e );
            }
        }
        return idljCompiler;
    }

    /**
     * @return the class name of the clas that implements the compiler
     */
    private String getIDLCompilerClass()
    {
        String vendor = System.getProperty( "java.vm.vendor" );

        if ( vendor.indexOf( "IBM" ) != -1 )
        {
            return "com.ibm.idl.toJavaPortable.Compile";
        }
        return "com.sun.tools.corba.se.idl.toJavaPortable.Compile";
    }

    /**
     * Invoke the specified compiler with a set of arguments
     *
     * @param compilerClass the <code>Class</code> that implements the compiler
     * @param args a <code>List</code> that contains the arguments to use for the compiler
     * @throws MojoExecutionException if the compilation fail or the compiler crashes
     */
    private void invokeCompiler( Class compilerClass, List args )
        throws MojoExecutionException
    {
        getLog().debug( "Current dir : " + System.getProperty( "user.dir" ) );
        Method compilerMainMethod;
        String arguments[];

        if ( isDebug() )
        {
            args.add( 0, "-verbose" );
            arguments = (String[]) args.toArray( new String[args.size()] );
            String command = compilerClass.getName();
            for ( int i = 0; i < arguments.length; i++ )
            {
                command += " " + arguments[i];
            }
            getLog().info( command );
        }
        else
        {
            arguments = (String[]) args.toArray( new String[args.size()] );
        }

        try
        {
            compilerMainMethod = compilerClass.getMethod( "main", new Class[] { String[].class } );
        }
        catch ( NoSuchMethodException e1 )
        {
            throw new MojoExecutionException( "Error: Compiler had no main method" );
        }

        int exitCode = 0;
        // Backup std channels
        PrintStream stdErr = System.err;
        PrintStream stdOut = System.out;
        // Local channels
        StringOutputStream err = new StringOutputStream();
        StringOutputStream out = new StringOutputStream();
        System.setErr( new PrintStream( err ) );
        System.setOut( new PrintStream( out ) );
        try
        {
            Object retVal = (Object) compilerMainMethod.invoke( compilerClass, new Object[] { arguments } );
            if ( retVal != null && retVal instanceof Integer )
                exitCode = ( (Integer) retVal ).intValue();

        }
        catch ( InvocationTargetException e )
        {
            throw new MojoExecutionException( "IDL compilation failed", e.getTargetException() );
        }
        catch ( Throwable e )
        {
            throw new MojoExecutionException( "IDL compilation failed", e );
        }
        finally
        {
            if ( !"".equals( out.toString() ) )
                getLog().info( out.toString() );
            if ( !"".equals( err.toString() ) )
                getLog().error( err.toString() );
            // Restore std channels
            System.setErr( stdErr );
            System.setOut( stdOut );
        }
        if ( !"".equals( out.toString() ) )
            getLog().info( out.toString() );
        if ( !"".equals( err.toString() ) )
            getLog().error( err.toString() );
        // Restore std channels
        System.setErr( stdErr );
        System.setOut( stdOut );

        if ( isFailOnError() && ( exitCode != 0 || err.toString().indexOf( "Invalid argument" ) != -1 ) )
        {
            throw new MojoExecutionException( "IDL compilation failed" );
        }
    }

    /**
     * Convert the provided filename from a Windows separator \\ to a unix/java separator /
     *
     * @param filename file name to fix separator
     * @return filename with all \\ replaced with /
     */
    public static String fixSeparator( String filename )
    {
        return StringUtils.replace( filename, '\\', '/' );
    }

    public static String getCanonicalPath( File file )
        throws MojoExecutionException
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Can't canonicalize system path: " + file.getAbsolutePath(), e );
        }
    }

    /**
     * Taken from maven-eclipse-plugin
     *
     * @param fromdir
     * @param todir
     * @param replaceSlashesWithDashes
     * @return the relative path between fromdir to todir
     * @throws MojoExecutionException
     */
    public static String toRelativeAndFixSeparator( File fromdir, File todir, boolean replaceSlashesWithDashes )
        throws MojoExecutionException
    {
        if ( !todir.isAbsolute() )
        {
            todir = new File( fromdir, todir.getPath() );
        }

        String basedirPath = getCanonicalPath( fromdir );
        String absolutePath = getCanonicalPath( todir );

        String relative = null;

        if ( absolutePath.equals( basedirPath ) )
        {
            relative = "."; //$NON-NLS-1$
        }
        else if ( absolutePath.startsWith( basedirPath ) )
        {
            // MECLIPSE-261
            // The canonical form of a windows root dir ends in a slash, whereas
            // the canonical form of any other file
            // does not.
            // The absolutePath is assumed to be: basedirPath + Separator +
            // fileToAdd
            // In the case of a windows root directory the Separator is missing
            // since it is contained within
            // basedirPath.
            int length = basedirPath.length() + 1;
            if ( basedirPath.endsWith( "\\" ) )
            {
                length--;
            }
            relative = absolutePath.substring( length );
        }
        else
        {
            relative = absolutePath;
        }

        relative = fixSeparator( relative );

        if ( replaceSlashesWithDashes )
        {
            relative = StringUtils.replace( relative, '/', '-' );
            relative = StringUtils.replace( relative, ':', '-' ); // remove ":"
            // for absolute
            // paths in
            // windows
        }

        return relative;
    }

}
