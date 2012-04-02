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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * This class implement the <code>CompilerTranslator</code> for the JacORB IDL compiler
 *
 * @author Anders Hessellund Jensen <ahj@trifork.com>
 * @version $Id: JacorbTranslator.java 9344 2009-04-03 22:25:02Z aheritier $
 */
public class JacorbTranslator
    extends AbstractTranslator
    implements CompilerTranslator
{

    /**
     * Default constructor
     */
    public JacorbTranslator()
    {
        super();
    }

    /**
     * Invoke the specified compiler with a set of arguments
     *
     * @param compilerClass the <code>Class</code> that implemtns the compiler
     * @param args a <code>List</code> that contains the arguments to use for the compiler
     * @throws MojoExecutionException if the compilation fail or the compiler crashes
     */
    private void invokeCompiler( Class compilerClass, List args )
        throws MojoExecutionException
    {
        // It would be great to use some 3rd party library for this stuff
        boolean fork = true;
        if ( !fork )
        {
            try
            {
                String arguments[] = (String[]) args.toArray( new String[args.size()] );

                if ( isDebug() )
                {
                    String command = "compile";
                    for ( int i = 0; i < arguments.length; i++ )
                    {
                        command += " " + arguments[i];
                    }
                    getLog().info( command );
                }

                Method compileMethod = compilerClass.getMethod( "compile", new Class[] { String[].class } );
                compileMethod.invoke( compilerClass, arguments );
            }
            catch ( InvocationTargetException e )
            {
                throw new MojoExecutionException( "Compilation failed", e.getTargetException() );
            }
            catch ( Throwable t )
            {
                throw new MojoExecutionException( "Compilation failed", t );
            }
        }
        else
        {

            // Forks a new java process.
            // Get path to java binary
            File javaHome = new File( System.getProperty( "java.home" ) );
            File javaBin = new File( new File( javaHome, "bin" ), "java" );

            // Get current class path
            URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();
            URL[] classPathUrls = cl.getURLs();

            // Construct list of arguments
            List binArgs = new ArrayList();

            // First argument is the java binary to run
            binArgs.add( javaBin.getPath() );

            // Add the classpath to argument list
            binArgs.add( "-classpath" );
            String classPath = "" + new File( classPathUrls[0].getPath().replaceAll( "%20", " " ) );
            for ( int i = 1; i < classPathUrls.length; i++ )
            {
                classPath += File.pathSeparator + new File( classPathUrls[i].getPath().replaceAll( "%20", " " ) );
            }
            classPath += "";
            binArgs.add( classPath );

            // Add class containing main method to arg list
            binArgs.add( compilerClass.getName() );

            // Add java arguments
            for ( Iterator it = args.iterator(); it.hasNext(); )
            {
                Object o = it.next();
                binArgs.add( o.toString() );
            }

            // Convert arg list to array
            String[] argArray = new String[binArgs.size()];
            for ( int i = 0; i < argArray.length; i++ )
            {
                argArray[i] = binArgs.get( i ).toString();
            }

            if ( isDebug() )
            {
                String command = "";
                for ( int i = 0; i < argArray.length; i++ )
                {
                    command += " " + argArray[i];
                }
                getLog().info( command );
            }

            try
            {
                Process p = Runtime.getRuntime().exec( argArray );
                redirectStream( p.getErrorStream(), System.err, "" );
                redirectStream( p.getInputStream(), System.out, "" );

                p.waitFor();

                if ( isFailOnError() && p.exitValue() != 0 )
                {
                    throw new MojoExecutionException( "IDL Compilation failure" );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error forking compiler", e );
            }
            catch ( InterruptedException e )
            {
                throw new MojoExecutionException( "Thread interrupted unexpectedly", e );
            }
        }
    }

    /**
     * This method it's used to invoke the compiler
     *
     * @param sourceDirectory the path to the sources
     * @param includeDirs the <code>File[]</code> of directories where to find the includes
     * @param targetDirectory the path to the destination of the compilation
     * @param idlFile the path to the file to compile
     * @param source //TODO ???
     * @throws MojoExecutionException the exeception is thrown whenever the compilation fails or crashes
     */
    public void invokeCompiler( String sourceDirectory, File[] includeDirs, String targetDirectory, String idlFile,
                                Source source )
        throws MojoExecutionException
    {
        List args = new ArrayList();

        // TODO: This should be configurable
        args.add( "-sloppy_names" );

        args.add( "-I" + sourceDirectory );

        // add idl files from other directories as well
        if ( includeDirs != null && includeDirs.length > 0 )
        {
            for ( int i = 0; i < includeDirs.length; i++ )
            {
                args.add( "-I" + includeDirs[i] );
            }
        }

        args.add( "-d" );
        args.add( targetDirectory );

        if ( source.emitSkeletons() != null && !source.emitSkeletons().booleanValue() )
        {
            args.add( "-noskel" );
        }
        if ( source.emitStubs() != null && !source.emitStubs().booleanValue() )
        {
            args.add( "-nostub" );
        }

        if ( source.getPackagePrefix() != null )
        {
            args.add( "-i2jpackage" );
            args.add( ":" + source.getPackagePrefix() );
        }

        if ( source.getPackagePrefixes() != null )
        {
            for ( Iterator prefixes = source.getPackagePrefixes().iterator(); prefixes.hasNext(); )
            {
                PackagePrefix prefix = (PackagePrefix) prefixes.next();
                args.add( "-i2jpackage" );
                args.add( prefix.getType() + ":" + prefix.getPrefix() + "." + prefix.getType() );
            }
        }

        if ( source.getDefines() != null )
        {
            for ( Iterator defs = source.getDefines().iterator(); defs.hasNext(); )
            {
                Define define = (Define) defs.next();
                String arg = "-D" + define.getSymbol();
                if ( define.getValue() != null )
                {
                    arg += "=" + define.getValue();
                }
                args.add( arg );
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

        Class compilerClass;
        try
        {
            compilerClass = getClassLoaderFacade().loadClass("org.jacorb.idl.parser");
        }
        catch ( ClassNotFoundException e )
        {
            throw new MojoExecutionException( "JacORB IDL compiler not found", e );
        }

        invokeCompiler( compilerClass, args );
    }

    /**
     * This methos it's used to redirect an <code>InputeStream</code> to a <code>OutputStream</code>
     *
     * @param in the <code>InputStream</code> to read from
     * @param out the <code>OutputStream</code> to write into
     * @param streamName the name of Stream
     */
    public static void redirectStream( final InputStream in, final OutputStream out, final String streamName )
    {
        Thread stdoutTransferThread = new Thread()
        {
            public void run()
            {
                PrintWriter pw = new PrintWriter( new OutputStreamWriter( out ), true );
                try
                {
                    BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
                    String line;
                    while ( ( line = reader.readLine() ) != null )
                    {
                        pw.println( line );
                    }
                }
                catch ( Throwable e )
                {
                    e.printStackTrace();
                }
            }
        };
        stdoutTransferThread.start();
    }
}
