/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.presentation.rmi ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA.portable.ApplicationException ;

import java.lang.reflect.Method ;

import java.rmi.RemoteException ;

/** Used to read and write arguments and results for a particular method.
*
*/
public interface DynamicMethodMarshaller
{
    /** Returns the method used to create this DynamicMethodMarshaller.
     */
    Method getMethod() ;

    /** Copy the arguments as needed for this particular method.
     * Can be optimized so that as little copying as possible is
     * performed.
     */
    Object[] copyArguments( Object[] args, ORB orb ) throws RemoteException ;

    /** Read the arguments for this method from the InputStream.
     * Returns null if there are no arguments.
     */
    Object[] readArguments( InputStream is ) ;

    /** Write arguments for this method to the OutputStream.
     * Does nothing if there are no arguments.
     */
    void writeArguments( OutputStream os, Object[] args ) ;

    /** Copy the result as needed for this particular method.
     * Can be optimized so that as little copying as possible is
     * performed.
     */
    Object copyResult( Object result, ORB orb ) throws RemoteException ;

    /** Read the result from the InputStream.  Returns null 
     * if the result type is null.
     */
    Object readResult( InputStream is ) ;

    /** Write the result to the OutputStream.  Does nothing if
     * the result type is null.
     */
    void writeResult( OutputStream os, Object result ) ;

    /** Returns true iff thr's class is a declared exception (or a subclass of
     * a declared exception) for this DynamicMethodMarshaller's method.
     */
    boolean isDeclaredException( Throwable thr ) ;

    /** Write the repository ID of the exception and the value of the
     * exception to the OutputStream.  ex should be a declared exception
     * for this DynamicMethodMarshaller's method.
     */
    void writeException( OutputStream os, Exception ex ) ;

    /** Reads an exception ID and the corresponding exception from 
     * the input stream.  This should be an exception declared in
     * this method.
     */
    Exception readException( ApplicationException ae ) ;
}
