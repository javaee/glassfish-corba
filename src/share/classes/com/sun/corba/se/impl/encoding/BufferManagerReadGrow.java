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

package com.sun.corba.se.impl.encoding;

import java.nio.ByteBuffer;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;

public class BufferManagerReadGrow
    implements BufferManagerRead, MarkAndResetHandler
{
    // REVISIT - This should go in an abstract class called
    //           BufferManagerReadBase which should implement
    //           BufferManagerRead. Then, this class should extend
    //           BufferManagerReadBase.
    private ORB orb ;

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    BufferManagerReadGrow( ORB orb ) 
    {
	this.orb = orb ;
    }

    public void processFragment (ByteBuffer byteBuffer, FragmentMessage header)
    {
        // REVISIT - should we consider throwing an exception similar to what's
        //           done for underflow()???
    }

    public void init(Message msg) {}

    public ByteBufferWithInfo underflow (ByteBufferWithInfo bbwi)
    {
	throw wrapper.unexpectedEof() ;
    }

    public void cancelProcessing(int requestId) {}
    
    // Mark and reset handler -------------------------

    private Object streamMemento;
    private RestorableInputStream inputStream;
    private boolean markEngaged = false;

    public MarkAndResetHandler getMarkAndResetHandler() {
        return this;
    }

    public void mark(RestorableInputStream is) {
        markEngaged = true;
        inputStream = is;
        streamMemento = inputStream.createStreamMemento();
    }

    // This will never happen
    public void fragmentationOccured(ByteBufferWithInfo newFragment) {}

    public void reset() {

        if (!markEngaged)
            return;

        markEngaged = false;
        inputStream.restoreInternalState(streamMemento);
        streamMemento = null;
    }

    // Nothing to close and cleanup.
    public void close(ByteBufferWithInfo bbwi) {}
}
