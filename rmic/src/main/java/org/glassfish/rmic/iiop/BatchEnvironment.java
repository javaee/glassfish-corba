/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1998-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.ClassPath;

import java.io.File;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * BatchEnvironment for iiop extends rmic's version to add
 * parse state.
 */
public class BatchEnvironment extends org.glassfish.rmic.BatchEnvironment implements Constants {

    /*
     * If the following flag is true, then the IDL generator can map
     * the methods and constants of non-conforming types. However,
     * this is very expensive, so the default should be false.
     */
    private boolean parseNonConforming = false;

    /**
     * This flag indicates that the stubs and ties need to be generated without
     * the package prefix (org.omg.stub).
     */
    private boolean standardPackage;

    /* Common objects used within package */

    HashSet alreadyChecked = new HashSet();
    Hashtable allTypes = new Hashtable(3001, 0.5f);
    Hashtable invalidTypes = new Hashtable(256, 0.5f);
    DirectoryLoader loader = null;
    ClassPathLoader classPathLoader = null;
    Hashtable nameContexts = null;
    Hashtable namesCache = new Hashtable();
    NameContext modulesContext = new NameContext(false);

    ClassDefinition defRemote = null;
    ClassDefinition defError = null;
    ClassDefinition defException = null;
    ClassDefinition defRemoteException = null;
    ClassDefinition defCorbaObject = null;
    ClassDefinition defSerializable = null;
    ClassDefinition defExternalizable = null;
    ClassDefinition defThrowable = null;
    ClassDefinition defRuntimeException = null;
    ClassDefinition defIDLEntity = null;
    ClassDefinition defValueBase = null;

    org.glassfish.rmic.tools.java.Type typeRemoteException = null;
    org.glassfish.rmic.tools.java.Type typeIOException = null;
    org.glassfish.rmic.tools.java.Type typeException = null;
    org.glassfish.rmic.tools.java.Type typeThrowable = null;

    ContextStack contextStack = null;

    /**
     * Create a BatchEnvironment for rmic with the given class path,
     * stream for messages and the destination directory.
     */
    public BatchEnvironment(OutputStream out, ClassPath path, File destinationDir) {

        super(out, path, destinationDir);

        // Make sure we have our definitions...

        try {
            defRemote =
                getClassDeclaration(idRemote).getClassDefinition(this);
            defError =
                getClassDeclaration(idJavaLangError).getClassDefinition(this);
            defException =
                getClassDeclaration(idJavaLangException).getClassDefinition(this);
            defRemoteException =
                getClassDeclaration(idRemoteException).getClassDefinition(this);
            defCorbaObject =
                getClassDeclaration(idCorbaObject).getClassDefinition(this);
            defSerializable =
                getClassDeclaration(idJavaIoSerializable).getClassDefinition(this);
            defRuntimeException =
                getClassDeclaration(idJavaLangRuntimeException).getClassDefinition(this);
            defExternalizable =
                getClassDeclaration(idJavaIoExternalizable).getClassDefinition(this);
            defThrowable=
                getClassDeclaration(idJavaLangThrowable).getClassDefinition(this);
            defIDLEntity=
                getClassDeclaration(idIDLEntity).getClassDefinition(this);
            defValueBase=
                getClassDeclaration(idValueBase).getClassDefinition(this);
            typeRemoteException = defRemoteException.getClassDeclaration().getType();
            typeException = defException.getClassDeclaration().getType();
            typeIOException = getClassDeclaration(idJavaIoIOException).getType();
            typeThrowable = getClassDeclaration(idJavaLangThrowable).getType();

            classPathLoader = new ClassPathLoader(path);

        } catch (ClassNotFound e) {
            error(0, "rmic.class.not.found", e.name);
            throw new Error();
        }
    }

    /**
     * Return whether or not to parse non-conforming types.
     */
    public boolean getParseNonConforming () {
        return parseNonConforming;
    }

    /**
     * Set whether or not to parse non-conforming types.
     */
    public void setParseNonConforming (boolean parseEm) {

        // If we are transitioning from not parsing to
        // parsing, we need to throw out any previously
        // parsed types...

        if (parseEm && !parseNonConforming) {
            reset();
        }

        parseNonConforming = parseEm;
    }

    void setStandardPackage(boolean standardPackage) {
        this.standardPackage = standardPackage;
    }

    boolean getStandardPackage() {
        return standardPackage;
    }

    /**
     * Clear out any data from previous executions.
     */
    public void reset () {

        // First, find all Type instances and call destroy()
        // on them...

        for (Enumeration e = allTypes.elements() ; e.hasMoreElements() ;) {
            Type type = (Type) e.nextElement();
            type.destroy();
        }

        for (Enumeration e = invalidTypes.keys() ; e.hasMoreElements() ;) {
            Type type = (Type) e.nextElement();
            type.destroy();
        }

        for (Iterator e = alreadyChecked.iterator() ; e.hasNext() ;) {
            Type type = (Type) e.next();
            type.destroy();
        }

        if (contextStack != null) contextStack.clear();

        // Remove and clear all NameContexts in the
        // nameContexts cache...

        if (nameContexts != null) {
            for (Enumeration e = nameContexts.elements() ; e.hasMoreElements() ;) {
                NameContext context = (NameContext) e.nextElement();
                context.clear();
            }
            nameContexts.clear();
        }

        // Now remove all table entries...

        allTypes.clear();
        invalidTypes.clear();
        alreadyChecked.clear();
        namesCache.clear();
        modulesContext.clear();

        // Clean up remaining...
        loader = null;
        parseNonConforming = false;

        // REVISIT - can't clean up classPathLoader here
    }

    /**
     * Release resources, if any.
     */
    public void shutdown() {
        if (alreadyChecked != null) {
            //System.out.println();
            //System.out.println("allTypes.size() = "+ allTypes.size());
            //System.out.println("    InstanceCount before reset = "+Type.instanceCount);
            reset();
            //System.out.println("    InstanceCount AFTER reset = "+Type.instanceCount);

            alreadyChecked = null;
            allTypes = null;
            invalidTypes = null;
            nameContexts = null;
            namesCache = null;
            modulesContext = null;
            defRemote = null;
            defError = null;
            defException = null;
            defRemoteException = null;
            defCorbaObject = null;
            defSerializable = null;
            defExternalizable = null;
            defThrowable = null;
            defRuntimeException = null;
            defIDLEntity = null;
            defValueBase = null;
            typeRemoteException = null;
            typeIOException = null;
            typeException = null;
            typeThrowable = null;

            super.shutdown();
        }
    }
}
