/*
 * Copyright (c) 1997, 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*****************************************************************************/
/*                    Copyright (c) IBM Corporation 1998                     */
/*                                                                           */
/* (C) Copyright IBM Corp. 1998                                              */
/*                                                                           */
/*****************************************************************************/

package org.glassfish.rmic;

import org.glassfish.rmic.tools.java.ClassDefinition;

import java.io.File;

/**
 * A placeholder to handle the no-longer supported JRMP flags. Any attempt to use them will now result in an error.
 *
 * @author      Peter Jones,  Bryan Atsatt
 */
public class RMIGenerator implements RMIConstants, Generator {

   /**
     * Examine and consume command line arguments.
     * @param argv The command line arguments. Ignore null
     * and unknown arguments. Set each consumed argument to null.
     * @param main Report any errors using the main.error() methods.
     * @return true if no errors, false otherwise.
     */
    public boolean parseArgs(String argv[], Main main) {
        main.error("rmic.jrmp.not.supported", main.program);
        return false;
    }

    /**
     * Generate the source files for the stub and/or skeleton classes
     * needed by RMI for the given remote implementation class.
     *
     * @param env       compiler environment
     * @param cdef      definition of remote implementation class
     *                  to generate stubs and/or skeletons for
     * @param destDir   directory for the root of the package hierarchy
     *                  for generated files
     */
    public void generate(BatchEnvironment env, ClassDefinition cdef, File destDir) {
    }

}
