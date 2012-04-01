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

import java.util.List;
import java.util.Set;

/**
 * This class represent the source tag available in the configuration tree of the maven plugin
 * 
 * @author Anders Hessellund Jensen <ahj@trifork.com>
 * @version $Id: Source.java 9341 2009-04-03 21:30:22Z aheritier $
 */
public class Source
{

    /**
     * Active the generation of java source compatibile with jdk previus 1.4
     * 
     * @parameter compatible
     */
    private Boolean compatible = Boolean.TRUE;

    /**
     * Whether the compiler should emit client stubs. Defaults to true.
     * 
     * @parameter emitStubs;
     */
    private Boolean emitStubs = Boolean.TRUE;

    /**
     * Whether the compiler should emit server skeletons. Defaults to true.
     * 
     * @parameter emitSkeletons;
     */
    private Boolean emitSkeletons = Boolean.TRUE;

    /**
     * Specifies a single, global packageprefix to use for all modules.
     * 
     * @parameter packagePrefix;
     */
    private String packagePrefix;

    /**
     * Specifies which files to include in compilation.
     * 
     * @parameter includes;
     */
    private Set includes;

    /**
     * Specifies which files to exclude from compilation.
     * 
     * @parameter excludes;
     */
    private Set excludes;

    /**
     * The list of package prefixes for certain types.
     * 
     * @parameter packagePrefixes;
     */
    private List packagePrefixes;

    /**
     * The list of preprocessor symbols to define.
     */
    private List defines;

    /**
     * The list of additional, compiler-specific arguments to use.
     */
    private List additionalArguments;

    /**
     * @return a <code>List</code> with all the defines with this source
     */
    public List getDefines()
    {
        return defines;
    }

    /**
     * @return a <code>Boolean</code> true if and only if the creation of the stubs is enabled
     */
    public Boolean emitStubs()
    {
        return emitStubs;
    }

    /**
     * @return a <code>Boolean</code> true if and only if the creation of the skeleton is enabled
     */
    public Boolean emitSkeletons()
    {
        return emitSkeletons;
    }

    /**
     * @return a <code>Boolean</code> true if and only if the creation of compatible code is enabled
     */
    public Boolean compatible()
    {
        return compatible;
    }

    /**
     * @return a <code>Set</code> with all the exclusions pattern
     */
    public Set getExcludes()
    {
        return excludes;
    }

    /**
     * @return a <code>Set</code> with all the inclusions pattern
     */
    public Set getIncludes()
    {
        return includes;
    }

    /**
     * @return the default package name that will be used as for all the generated classes
     */
    public String getPackagePrefix()
    {
        return packagePrefix;
    }

    /**
     * @return a <code>List</code> of <code>PackagePrefix</code> to use for the generated files
     */
    public List getPackagePrefixes()
    {
        return packagePrefixes;
    }

    /**
     * @return a <code>List</code> of <code>String</code> that will be added as compiler parameters
     */
    public List getAdditionalArguments()
    {
        return additionalArguments;
    }
}
