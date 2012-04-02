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
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

/**
 * This is abstarct class used to decrease the code needed to the creation of the compiler MOJO.
 *
 * @author Anders Hessellund Jensen <ahj@trifork.com>
 * @version $Id: AbstractIDLJMojo.java 9344 2009-04-03 22:25:02Z aheritier $
 */
public abstract class AbstractIDLJMojo
    extends AbstractMojo
{
    /**
     * A <code>List</code> of <code>Source</code> configurations to compile.
     *
     * @parameter
     */
    private List sources;

    /**
     * Activate more detailed debug messages.
     *
     * @parameter debug
     */
    private boolean debug;

    /**
     * Should the plugin fail the build if there's an error while generating sources from IDLs.
     *
     * @parameter expression="${failOnError}" default-value="true"
     */
    private boolean failOnError;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The granularity in milliseconds of the last modification date for testing whether a source needs recompilation.
     *
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * The maven project helper class for adding resources.
     *
     * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}"
     */
    private MavenProjectHelper projectHelper;

    /**
     * The directory to store the processed grammars. Used so that grammars are not constantly regenerated.
     *
     * @parameter default-value="${project.build.directory}/idlj-timestamp"
     */
    private File timestampDirectory;

    /**
     * The compiler to use. Current options are Suns idlj compiler and JacORB. Should be either "idlj" or "jacorb".
     *
     * @parameter default-value="idlj"
     */
    private String compiler;

    /**
     * The interface between this class and the rest of the world - unit tests replace the default implementation.
     */
    private DependenciesFacade dependenciesFacade;

    /**
     * The default implementation of the dependencies.
     */
    private static final DependenciesFacade DEPENDENCIES_FACADE = new DependenciesFacadeImpl();


    protected AbstractIDLJMojo() {
        this( DEPENDENCIES_FACADE );

    }

    AbstractIDLJMojo( DependenciesFacade dependenciesFacade ) {
        this.dependenciesFacade = dependenciesFacade;
    }

    /**
     * @return the source directory that contains the IDL files
     * @throws MojoExecutionException
     */
    protected abstract File getSourceDirectory()
        throws MojoExecutionException;

    /**
     * @return the <code>File[]</code> of the directories to use as include directories for the compilation
     */
    protected abstract File[] getIncludeDirs();

    /**
     * @return the path of the directory that will contains the results of the compilation
     * @throws MojoExecutionException
     */
    protected abstract File getOutputDirectory()
        throws MojoExecutionException;

    /**
     * Execute the goal of the MOJO that is: compiling the IDL files
     *
     * @throws MojoExecutionException if the compilation fails or the compiler crashes
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( !dependenciesFacade.exists(getOutputDirectory()))
        {
            dependenciesFacade.createDirectory(getOutputDirectory());
        }
        if ( !dependenciesFacade.isWriteable(getOutputDirectory()))
            throw new MojoExecutionException( "Cannot write in : " + getOutputDirectory());

        addCompileSourceRoot();

        if ( !dependenciesFacade.exists(timestampDirectory))
        {
            dependenciesFacade.createDirectory(timestampDirectory);
        }

        CompilerTranslator translator;
        if ( compiler == null )
        {
            translator = new IdljTranslator();
        }
        else if ( compiler.equals( "idlj" ) )
        {
            translator = new IdljTranslator();
        }
        else if ( compiler.equals( "jacorb" ) )
        {
            translator = new JacorbTranslator();
        }
        else
        {
            throw new MojoExecutionException( "Compiler not supported: " + compiler );
        }

        translator.setDebug( debug );
        translator.setFailOnError( failOnError );
        translator.setLog( getLog() );

        if ( sources != null )
        {
            for (Object source : sources) {
                processSource((Source)source, translator);
            }
        }
        else
        {
            Source defaultSourceConfiguration = new Source();
            processSource( defaultSourceConfiguration, translator );
        }
    }

    /**
     * Compile the IDL files located in the given source path.
     *
     * @param source the <code>Source</code> that specify which file compile with arguments to use for the source
     * @param translator the <code>CompilerTranslator</code> that raprresents idl compiler backend that will be used
     * @throws MojoExecutionException if the compilation fails or the compiler crashes
     */
    private void processSource( Source source, CompilerTranslator translator )
        throws MojoExecutionException
    {
        Set staleGrammars = computeStaleGrammars( source );
        if ( staleGrammars.size() > 0 )
        {
            getLog().info( "Processing " + staleGrammars.size() + " grammar files to " + getOutputDirectory() );
        }
        else
        {
            getLog().info( "Nothing to compile - all idl files are up to date" );
        }

        for ( Iterator it = staleGrammars.iterator(); it.hasNext(); )
        {
            File idlFile = (File) it.next();
            getLog().debug( "Processing: " + idlFile.toString() );
            translator.invokeCompiler( getSourceDirectory().getAbsolutePath(), getIncludeDirs(),
                                       getOutputDirectory().getAbsolutePath(), idlFile.toString(), source );
            try
            {
                URI relativeURI = getSourceDirectory().toURI().relativize( idlFile.toURI() );
                File timestampFile = new File( timestampDirectory.toURI().resolve( relativeURI ) );
                dependenciesFacade.copyFile(idlFile, timestampFile);
            }
            catch ( IOException e )
            {
                getLog().warn( "Failed to copy IDL file to output directory: " + e );
            }
        }
    }

    /**
     * Determine which idl files need to be compiled.
     *
     * @param source the <code>Source</code> that represents which files to compile
     * @return a set of files that need to be compiled
     * @throws MojoExecutionException if the selection of the files to compile fails
     */
    private Set computeStaleGrammars( Source source )
        throws MojoExecutionException
    {
        Set includes = source.getIncludes();
        getLog().debug( "includes : " + includes );
        if ( includes == null )
        {
            includes = new HashSet();
            includes.add( "**/*.idl" );
        }
        Set excludes = source.getExcludes();
        getLog().debug( "excludes : " + excludes );
        if ( excludes == null )
        {
            excludes = new HashSet();
        }
        SourceInclusionScanner scanner = dependenciesFacade.createSourceInclusionScanner(staleMillis, includes, excludes);
        scanner.addSourceMapping( new SuffixMapping( ".idl", ".idl" ) );

        Set staleSources = new HashSet();

        File sourceDir = getSourceDirectory();
        getLog().debug( "sourceDir : " + sourceDir );
        try
        {
            if ( dependenciesFacade.exists(sourceDir) && dependenciesFacade.isDirectory(sourceDir))
            {
                staleSources.addAll( scanner.getIncludedSources( sourceDir, timestampDirectory ) );
            }
            else
            {
                getLog().debug( "sourceDir isn't a directory" );
            }
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDir
                + "\' for stale CORBA IDL files to reprocess.", e );
        }

        return staleSources;
    }

    /**
     * Add generated sources in compile source root
     *
     * @throws MojoExecutionException
     */
    protected abstract void addCompileSourceRoot()
        throws MojoExecutionException;

    /**
     * @return the current <code>MavenProject</code> instance
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * @return the current <code>MavenProjectHelper</code> instance
     */
    protected MavenProjectHelper getProjectHelper()
    {
        return projectHelper;
    }

    interface DependenciesFacade {

        SourceInclusionScanner createSourceInclusionScanner(int updatedWithinMsecs, Set includes, Set excludes);

        void copyFile(File sourceFile, File targetFile) throws IOException;

        boolean exists(File outputDirectory);

        void createDirectory(File directory);

        boolean isWriteable(File directory);

        boolean isDirectory(File file);
    }

    static class DependenciesFacadeImpl implements DependenciesFacade {

        public void copyFile(File sourceFile, File targetFile) throws IOException {
            FileUtils.copyFile(sourceFile, targetFile);
        }

        public SourceInclusionScanner createSourceInclusionScanner(int updatedWithinMsecs, Set includes, Set excludes) {
            return new StaleSourceScanner(updatedWithinMsecs, includes, excludes );
        }

        public boolean exists(File file) {
            return file.exists();
        }

        public void createDirectory(File directory) {
            directory.mkdirs();
        }

        public boolean isWriteable(File directory) {
            return directory.canWrite();
        }

        public boolean isDirectory(File file) {
            return file.isDirectory();
        }
    }

}
