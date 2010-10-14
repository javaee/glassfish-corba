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
import java.util.EmptyStackException;

import sun.corba.Bridge;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.RemarshalException;

import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.spi.transport.CorbaConnection;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaContactInfoListIterator;

import com.sun.corba.se.impl.encoding.BufferManagerWrite;
import com.sun.corba.se.impl.encoding.ByteBufferWithInfo;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.CorbaInvocationInfo;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReplyMessage;

/**
 * Streaming buffer manager.
 */
public class BufferManagerWriteStream extends BufferManagerWrite
{
    private int fragmentCount = 0;

    BufferManagerWriteStream( ORB orb )
    {
	super(orb) ;
    }

    public boolean sentFragment() {
        return fragmentCount > 0;
    }

    /**
     * Returns the correct buffer size for this type of
     * buffer manager as set in the ORB.
     */
    public int getBufferSize() {
        return orb.getORBData().getGIOPFragmentSize();
    }

    public void overflow (ByteBufferWithInfo bbwi)
    {
        // Set the fragment's moreFragments field to true
        MessageBase.setFlag(bbwi.getByteBuffer(), Message.MORE_FRAGMENTS_BIT);

	try {
            sendFragment(false);
	} catch (SystemException se) {
	    // REVISIT: this part similar to 
	    // CorbaClientRequestDispatchImpl.beginRequest() 
	    // and CorbaClientRequestDelegate.request()
	    CorbaContactInfoListIterator itr;
	    try {
		itr = getContactInfoListIterator();
	    } catch (EmptyStackException ese) {
		// server side, don't reportException
		throw se;
	    }
	    
	    // bug 6382377: must not lose exception in PI
	    orb.getPIHandler().invokeClientPIEndingPoint( ReplyMessage.SYSTEM_EXCEPTION, se ) ;

	    boolean retry = itr.reportException(null, se);
	    if (retry) {
	        Bridge bridge = Bridge.get();
	        bridge.throwException(new RemarshalException());
	    } else { 
                // re-throw the SystemException
	        throw se;
	    }
	}

        // Reuse the old buffer

        // REVISIT - need to account for case when needed > available
        // even after fragmenting.  This is the large array case, so
        // the caller should retry when it runs out of space.
        bbwi.position(0);
        bbwi.setLength(bbwi.getCapacity());
        bbwi.setFragmented(true);

        // Now we must marshal in the fragment header/GIOP header

        // REVISIT - we can optimize this by not creating the fragment message
        // each time.  

        FragmentMessage header = ((CDROutputObject)outputObject).getMessageHeader().createFragmentMessage();

        header.write(((CDROutputObject)outputObject));
    }

    private void sendFragment(boolean isLastFragment)
    {
        CorbaConnection conn = ((CDROutputObject)outputObject).getMessageMediator().getConnection();

	// REVISIT: need an ORB
	//System.out.println("sendFragment: last?: " + isLastFragment);
        conn.writeLock();

        try {
            // Send the fragment
            conn.sendWithoutLock(((CDROutputObject)outputObject));

            fragmentCount++;

        } finally {

            conn.writeUnlock();
        }

    }

    // Sends the last fragment
    public void sendMessage ()
    {
        sendFragment(true);

        sentFullMessage = true;
    }

    /**
     * Close the BufferManagerWrite and do any outstanding cleanup.
     *
     * No work to do for a BufferManagerWriteStream
     */
    public void close(){};

    /**
     * Get CorbaContactInfoListIterator
     * 
     * NOTE: Requires this.orb
     */
    protected CorbaContactInfoListIterator getContactInfoListIterator()
    {
	return (CorbaContactInfoListIterator)
	    ((CorbaInvocationInfo)this.orb.getInvocationInfo())
	        .getContactInfoListIterator();
    }
}
