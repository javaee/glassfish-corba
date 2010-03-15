/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package corba.tf;

import com.sun.corba.se.spi.orbutil.tf.MethodMonitorBase;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ken
 */
public class MethodMonitorTracingImpl extends MethodMonitorBase
    implements Iterable<TraceNode>  {

    public Iterator<TraceNode> iterator() {
        return state.iterator() ;
    }

    public enum EntryType { ENTER, INFO, EXIT, EXIT_RESULT, EXCEPTION }

    private final List<TraceNode> state = new ArrayList<TraceNode>() ;

    public MethodMonitorTracingImpl( Class<?> cls ) {
        super( cls ) ;
    }

    public void enter(int ident, Object... args) {
        state.add( new TraceNode( ident, args ) ) ;
    }

    public void info(Object[] args, int callerIdent, int selfIdent) {
        state.add( new TraceNode( selfIdent, callerIdent, args ) ) ;
    }

    public void exit(int ident) {
        state.add( new TraceNode( ident ) ) ;
    }

    public void exit(int ident, Object result) {
        state.add( new TraceNode( ident, result ) ) ;
    }

    public void exception(int ident, Throwable thr) {
        state.add( new TraceNode( ident, thr ) ) ;
    }

    public void clear() {
        state.clear() ;
    }

    @Override
    public int hashCode() {
        return state.hashCode() ;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true ;
        }

        if (!(obj instanceof MethodMonitorTracingImpl)) {
            return false ;
        }

        MethodMonitorTracingImpl other = (MethodMonitorTracingImpl)obj ;

        return state.equals( other.state ) ;
    }
}
