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

/**
 * Process CORBA IDL test files in IDLJ.
 *
 * @author maguro <adc@apache.org>
 * @version $Id: TestIDLJMojo.java 9344 2009-04-03 22:25:02Z aheritier $
 * @goal generate-test
 * @phase generate-test-sources
 */
public class TestIDLJMojo
    extends AbstractIDLJMojo
{
    /**
     * The source directory containing *.idl files.
     *
     * @parameter default-value="${basedir}/src/test/idl"
     */
    private File sourceDirectory;

    /**
     * Additional include directories containing additional *.idl files required for compilation.
     *
     * @parameter
     */
    private File[] includeDirs;

    /**
     * The directory to output the generated sources to.
     *
     * @parameter default-value="${project.build.directory}/generated-test-sources/idl"
     */
    private File outputDirectory;

    /**
     * @return the directory that contains the source
     */
    protected File getSourceDirectory()
    {
        return sourceDirectory;
    }

    /**
     * @return the directory that will contain the generated code
     */
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * @return a <code>List</code> of directory to use as <i>include</i>
     */
    protected File[] getIncludeDirs()
    {
        return includeDirs;
    }

    /**
     * Adds the generated source path to the test source directories list so that maven can find the new sources to
     * compile tests.
     */
    protected void addCompileSourceRoot()
    {
        getProject().addTestCompileSourceRoot( getOutputDirectory().getAbsolutePath() );
    }
}
