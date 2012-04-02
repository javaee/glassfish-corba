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

import org.apache.maven.plugin.logging.Log;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Shared capabilities for translators.
 * 
 * @author Arnaud Heritier <aheritier AT apache DOT org>
 * @version $Id: AbstractIDLJMojo.java 9189 2009-03-10 21:47:46Z aheritier $
 */
public abstract class AbstractTranslator
    implements CompilerTranslator
{

    /**
     * enable/disable debug messages
     */
    private boolean debug;

    /**
     * Set to true to fail the build if an error occur while compiling the IDL.
     */
    private boolean failOnError;

    /**
     * the <code>Log</code> that will used for the messages
     */
    private Log log;

    /* A facade to enable unit testing to control compiler access. */
    private static ClassLoaderFacade classLoaderFacade = new ClassLoaderFacadeImpl();

    /**
     * @return the debug
     */
    public boolean isDebug()
    {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug( boolean debug )
    {
        this.debug = debug;
    }

    /**
     * @return the log
     */
    public Log getLog()
    {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog( Log log )
    {
        this.log = log;
    }

    /**
     * @return the failOnError
     */
    public boolean isFailOnError()
    {
        return failOnError;
    }

    /**
     * @param failOnError the failOnError to set
     */
    public void setFailOnError( boolean failOnError )
    {
        this.failOnError = failOnError;
    }

    static void setClassLoaderFacade(ClassLoaderFacade classLoaderFacade) {
        AbstractTranslator.classLoaderFacade = classLoaderFacade;
    }

    protected ClassLoaderFacade getClassLoaderFacade() {
        return classLoaderFacade;
    }

    /**
     An interface for loading the proper IDL compiler class.
     */
    public static interface ClassLoaderFacade {
        void prependUrls(URL... urls);

        Class loadClass(String idlCompilerClass) throws ClassNotFoundException;

        URL getResource(String resourceName);
    }

    /**
     * The implementation of ClassLoaderFacade used at runtime.
     */
    static class ClassLoaderFacadeImpl implements ClassLoaderFacade {
        ClassLoader classLoader = getClass().getClassLoader();

        public void prependUrls(URL... urls) {
            classLoader = new URLClassLoader(urls, classLoader);
        }

        public Class loadClass(String idlCompilerClass) throws ClassNotFoundException {
            return classLoader.loadClass(idlCompilerClass);
        }

        public URL getResource(String resourceName) {
            return classLoader.getResource( resourceName );
        }

    }
}
