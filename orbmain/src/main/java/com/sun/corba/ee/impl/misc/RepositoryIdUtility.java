/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.misc;

import org.omg.CORBA.ORB;
import com.sun.corba.ee.impl.util.RepositoryId;

/**
 * Utility methods for working with repository IDs.
 */
public interface RepositoryIdUtility
{
    boolean isChunkedEncoding(int valueTag);
    boolean isCodeBasePresent(int valueTag);

    // These are currently the same in both RepositoryId and
    // RepositoryId_1_3, but provide the constants again here
    // to eliminate awkardness when using this interface.
    int NO_TYPE_INFO = RepositoryId.kNoTypeInfo;
    int SINGLE_REP_TYPE_INFO = RepositoryId.kSingleRepTypeInfo;
    int PARTIAL_LIST_TYPE_INFO = RepositoryId.kPartialListTypeInfo;

    // Determine how many (if any) repository IDs follow the value
    // tag.
    int getTypeInfo(int valueTag);

    // Accessors for precomputed value tags
    int getStandardRMIChunkedNoRepStrId();
    int getCodeBaseRMIChunkedNoRepStrId();
    int getStandardRMIChunkedId();
    int getCodeBaseRMIChunkedId();
    int getStandardRMIUnchunkedId();
    int getCodeBaseRMIUnchunkedId();
    int getStandardRMIUnchunkedNoRepStrId();
    int getCodeBaseRMIUnchunkedNoRepStrId();
}
