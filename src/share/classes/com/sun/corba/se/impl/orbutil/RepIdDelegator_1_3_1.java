/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.corba.se.impl.orbutil;

import org.omg.CORBA.ORB;
import java.io.Serializable;
import java.util.Hashtable;
import java.net.MalformedURLException;
import com.sun.corba.se.impl.io.TypeMismatchException;
import com.sun.corba.se.impl.util.RepositoryId;

/**
 * Delegates to the RepositoryId_1_3_1 implementation in
 * com.sun.corba.se.impl.orbutil.  This is necessary to
 * overcome the fact that many of RepositoryId's methods
 * are static.
 */
public final class RepIdDelegator_1_3_1
    implements RepositoryIdStrings, 
               RepositoryIdUtility,
               RepositoryIdInterface
{
    // RepositoryIdFactory methods

    public String createForAnyType(Class type) {
        return RepositoryId_1_3_1.createForAnyType(type);
    }

    public String createForAnyType(Class type, ClassInfoCache.ClassInfo cinfo ) {
        return RepositoryId_1_3_1.createForAnyType(type);
    }

    public String createForJavaType(Serializable ser)
        throws TypeMismatchException
    {
        return RepositoryId_1_3_1.createForJavaType(ser);
    }
               
    public String createForJavaType(Class clz)
        throws TypeMismatchException
    {
        return RepositoryId_1_3_1.createForJavaType(clz);
    }

    public String createForJavaType(Class clz, ClassInfoCache.ClassInfo cinfo )
        throws TypeMismatchException
    {
        return RepositoryId_1_3_1.createForJavaType(clz);
    }

    public String createSequenceRepID(java.lang.Object ser) {
        return RepositoryId_1_3_1.createSequenceRepID(ser);
    }

    public String createSequenceRepID(Class clazz) {
        return RepositoryId_1_3_1.createSequenceRepID(clazz);
    }

    public RepositoryIdInterface getFromString(String repIdString) {
        return new RepIdDelegator_1_3_1(RepositoryId_1_3_1.cache.getId(repIdString));
    }

    // RepositoryIdUtility methods
    
    public boolean isChunkedEncoding(int valueTag) {
        return RepositoryId.isChunkedEncoding(valueTag);
    }

    public boolean isCodeBasePresent(int valueTag) {
        return RepositoryId.isCodeBasePresent(valueTag);
    }

    public String getClassDescValueRepId() {
        return RepositoryId_1_3_1.kClassDescValueRepID;
    }

    public String getWStringValueRepId() {
        return RepositoryId_1_3_1.kWStringValueRepID;
    }

    public int getTypeInfo(int valueTag) {
        return RepositoryId.getTypeInfo(valueTag);
    }

    public int getStandardRMIChunkedNoRepStrId() {
        return RepositoryId.kPreComputed_StandardRMIChunked_NoRep;
    }

    public int getCodeBaseRMIChunkedNoRepStrId() {
        return RepositoryId.kPreComputed_CodeBaseRMIChunked_NoRep;
    }

    public int getStandardRMIChunkedId() {
        return RepositoryId.kPreComputed_StandardRMIChunked;
    }

    public int getCodeBaseRMIChunkedId() {
        return RepositoryId.kPreComputed_CodeBaseRMIChunked;
    }

    public int getStandardRMIUnchunkedId() {
        return RepositoryId.kPreComputed_StandardRMIUnchunked;
    }

    public int getCodeBaseRMIUnchunkedId() {
        return RepositoryId.kPreComputed_CodeBaseRMIUnchunked;
    }

    public int getStandardRMIUnchunkedNoRepStrId() {
	return RepositoryId.kPreComputed_StandardRMIUnchunked_NoRep;
    }

    public int getCodeBaseRMIUnchunkedNoRepStrId() {
        return RepositoryId.kPreComputed_CodeBaseRMIUnchunked_NoRep;
    }

    // RepositoryIdInterface methods

    public Class getClassFromType() throws ClassNotFoundException {
        return delegate.getClassFromType();
    }

    public Class getClassFromType(String codebaseURL) 
        throws ClassNotFoundException, MalformedURLException
    {
        return delegate.getClassFromType(codebaseURL);
    }

    public Class getClassFromType(Class expectedType,
                                  String codebaseURL) 
        throws ClassNotFoundException, MalformedURLException
    {
        return delegate.getClassFromType(expectedType, codebaseURL);
    }

    public String getClassName() {
        return delegate.getClassName();
    }

    // Constructor used for factory/utility cases
    public RepIdDelegator_1_3_1() {}

    // Constructor used by getIdFromString.  All non-static
    // RepositoryId methods will use the provided delegate.
    private RepIdDelegator_1_3_1(RepositoryId_1_3_1 _delegate) {
        this.delegate = _delegate;
    }

    private RepositoryId_1_3_1 delegate = null;

    public String toString() {
        if (delegate != null)
            return delegate.toString();
        else
            return this.getClass().getName();
    }

    public boolean equals(Object obj) {
        if (delegate != null)
            return delegate.equals(obj);
        else
            return super.equals(obj);
    }

    public int hashCode() {
	return delegate.hashCode() ;
    }
}
