/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework ;

import java.lang.reflect.Method ;
import java.util.EventObject ;

/** An event representing entering or exiting a particular method
 * on a particular thread, as recorded by an interceptor with a
 * particular id.
 */
public class MethodEvent 
{
    private String threadId ;
    private String id ;
    private Method method ;

    public static MethodEvent make( String id, Method method ) 
    {
	return new MethodEvent( id, method ) ;
    }

    private MethodEvent( String id, Method method ) 
    {
	this.threadId = Thread.currentThread().toString() ;
	this.id = id ;
	this.method = method ;
    }

    public String getThreadId() { return threadId ; }

    public String getId() { return id ; } 

    public Method getMethod() { return method ; }

    public boolean equals( Object obj ) 
    {
	if (!(obj instanceof MethodEvent))
	    return false ;

	MethodEvent other = (MethodEvent)obj ;

	return (id.equals( other.id ) &&
		method.equals( other.method )) ;
    }

    public int hashCode()
    {
	return id.hashCode() ^ method.hashCode() ;
    }

    public String toString() 
    {
	return "MethodEvent[threadId=" + threadId + " id=" + id + 
	    " method=" + method + "]" ;
    }
}
