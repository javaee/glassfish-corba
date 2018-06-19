/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1995-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Abstract class to represent a class file.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
abstract class ClassFile {
    /**
     * Factory method to create a ClassFile backed by a File.
     *
     * @param file a File object
     * @return a new ClassFile
     */
    public static ClassFile newClassFile(File file) {
        return new FileClassFile(file);
    }

    /**
     * Factory method to create a ClassFile backed by a ZipEntry.
     *
     * @param zf a ZipFile
     * @param ze a ZipEntry within the zip file
     * @return a new ClassFile
     */
    public static ClassFile newClassFile(ZipFile zf, ZipEntry ze) {
        return new ZipClassFile(zf, ze);
    }

    /**
     * Factory method to create a ClassFile backed by a nio Path.
     *
     * @param path nio Path object
     * @return a new ClassFile
     */
    public static ClassFile newClassFile(Path path) {
        return Files.exists(path)? new PathClassFile(path) : null;
    }

    /**
     * Returns true if this is zip file entry
     */
    public abstract boolean isZipped();

    /**
     * Returns input stream to either regular file or zip file entry
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns true if file exists.
     */
    public abstract boolean exists();

    /**
     * Returns true if this is a directory.
     */
    public abstract boolean isDirectory();

    /**
     * Return last modification time
     */
    public abstract long lastModified();

    /**
     * Get file path. The path for a zip file entry will also include
     * the zip file name.
     */
    public abstract String getPath();

    /**
     * Get name of file entry excluding directory name
     */
    public abstract String getName();

    /**
     * Get absolute name of file entry
     */
    public abstract String getAbsoluteName();

    /**
     * Get length of file
     */
    public abstract long length();
}
