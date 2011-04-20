/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package com.sun.corba.se.spi.orbutil.generic ;

/** Type safe holder that can hold any non-primitive type.
 * Useful for out parameters and passing arguments that need
 * to be set later.
 */
public class Holder<T> 
{
    private T _content ;

    public Holder( T content ) 
    {
	this._content = content ;
    }

    public Holder()
    {
	this( null ) ;
    }

    /** Issue 15392: It is EXTEMELY important to make this operation
     * as fast as possible, because it is called a lot, even when
     * tracing is not enabled.  This method is called at the start
     * of every method that has a tracing annotation.
     * In particular, synchronized is too slow, and it is suspected that
     * volatile may be too slow as well.
     * <p>
     * The consequences of not synchronizing here are that a content(T) call
     * may NEVER become visible in some threads.  This means that turning on
     * tracing while the system is running may not capture all methods
     * that should be traced.
     * <P>
     * Note that in GF 3.1, we NORMALLY only enabled ORB tracing using
     * -Dcom.sun.corba.ee.Debug, which only happens at GF startup time,
     * so this is not too big a problem.  However, it is possible to change
     * tracing flags using a ORB MBean, which could definitely cause
     * the problem mentioned above.
     * <P>
     * Another aid in fixing this is to make SynchronizedHolder subclass Holder,
     * and then do all of the tracing facility in terms of Holder.  Then
     * registering a class can decide which kind of Holder to use.
     * In particular, we can make the default be unsychronized, and override
     * this if necessary for testing.
     */
    public T content()
    {
	return _content ;
    }

    public void content( T content ) 
    {
	this._content = content ;
    }

    public boolean equals( Object obj )
    {
	if (!(obj instanceof Holder))
	    return false ;

	Holder other = Holder.class.cast( obj ) ;

	return _content.equals( other.content() ) ;
    }

    public int hashCode()
    {
	return _content.hashCode() ;
    }

    public String toString() 
    {
        return this.getClass().getSimpleName() + "[" + _content + "]" ;
    }
}

