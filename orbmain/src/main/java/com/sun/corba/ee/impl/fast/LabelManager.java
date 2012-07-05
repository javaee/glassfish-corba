/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.ee.impl.fast;

import java.util.concurrent.atomic.AtomicLong ;

import com.sun.corba.ee.impl.fast.bytebuffer.Reader ;
import com.sun.corba.ee.impl.fast.bytebuffer.Writer ;
import org.glassfish.pfl.basic.contain.Holder;
import org.glassfish.pfl.basic.contain.Pair;
import org.glassfish.pfl.basic.func.UnaryFunction;

/** Maintains the assignment of Labels to Objects for an OutputStream
 * New labels are allocated as needed, and all context determinations
 * are handled in this class.
 */
public class LabelManager {    
    public final static class Label extends Pair<Long,Long> {
        public Label( Long first, Long second ) {
            super( first, second ) ;
        }

        public Label( Reader reader ) {
            super( VarOctetUtility.get( reader ), VarOctetUtility.get( reader ) ) ;
        }

        public void put( Writer writer ) {
            VarOctetUtility.put( writer, first() ) ;
            VarOctetUtility.put( writer, second() ) ; 
        }

        public String toString() {
            return "Label[ctx=" + first() + ",val=" + second() + "]" ;
        }
    }

    private final AtomicLong idCounter = new AtomicLong() ;

    private UnaryFunction<Object,LabelManager.Label> allocateLabel =
        new UnaryFunction<Object,Label>() {
            public Label evaluate( final Object arg ) {
                long contextId = 0 ;
                long id = idCounter.getAndIncrement() ;
                return new Label( contextId, id ) ;
            }
        } ;

    // If the contextId is 0, use the msgTable, otherwise
    // use the extTable.
    private final LookupTable<Object,LabelManager.Label> msgTable ;
    private final LookupTable<Object,LabelManager.Label> extTable ;

    public LabelManager( LookupTable<Object,Label> extTable ) {
        this.msgTable = new LookupTableSimpleConcurrentImpl<Object,Label>(
            allocateLabel, Label.class ) ;
        this.extTable = extTable ;
    }

    /** Exact function TBD, but here are some general principles:
     * <ul>
     * <li>If obj is not immutable, result is 0.
     * <li>If obj is immutable, result is &gt;= 0.
     * <li>Certain objects may be reserved for result &gt;= FIRST_GLOBAL_CONTEXT_ID. 
     * </ul>
     */
    public long getContextId( Object obj ) {
        /** Implementation to use for now.
         */
        return EmergeCodeFactory.MESSAGE_CONTEXT_ID ;
    }

    public Label lookup( Holder<Boolean> firstTime, Object data ) {
        long contextId = getContextId( data ) ;
        Label label ;
        if (contextId == 0) {
            label = msgTable.lookup( firstTime, data ) ;
        }  else {
            // XXX assume for now that this case must do another 
            // getContextId call.  How can we improve this?
            label = extTable.lookup( firstTime, data ) ;
        }
        return label ;
    }
}
