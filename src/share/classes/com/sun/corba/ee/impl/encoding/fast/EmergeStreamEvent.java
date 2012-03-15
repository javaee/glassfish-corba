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
package com.sun.corba.ee.impl.encoding.fast ;

// XXX This seems like the wrong approach: do we even need this API?

/** The basic event type, which always has an EmergeKind.
 */
public interface EmergeStreamEvent {
    EmergeCode.EmergeKind kind() ;

    /** Event type used for all primitives.
     */
    interface PrimitiveEvent extends EmergeStreamEvent {
    }

    /** The primitive representing a null value.
     */
    public interface NullEvent extends PrimitiveEvent {
    }

    /** The primitive representing an indirection.
     */
    public interface IndirEvent extends PrimitiveEvent {
        LabelManager.Label label() ;
    }

    /** The primitive representing a boolean value.
     */
    public interface BoolEvent extends PrimitiveEvent {
        boolean value() ;
    }

    /** The primitive representing a byte value.
     */
    public interface ByteEvent extends PrimitiveEvent {
        byte value() ;
    }

    /** The primitive representing a char value.
     */
    public interface CharEvent extends PrimitiveEvent {
        char value() ;
    }

    /** The primitive representing a short value.
     */
    public interface ShortEvent extends PrimitiveEvent {
        short value() ;
    }

    /** The primitive representing a int value.
     */
    public interface IntEvent extends PrimitiveEvent {
        int value() ;
    }

    /** The primitive representing a long value.
     */
    public interface LongEvent extends PrimitiveEvent {
        long value() ;
    }

    /** The primitive representing a float value.
     */
    public interface FloatEvent extends PrimitiveEvent {
        float value() ;
    }

    /** The primitive representing a double value.
     */
    public interface DoubleEvent extends PrimitiveEvent {
        double value() ;
    }

    /** Event type common to all arrays.
     */
    public interface ArrayEvent extends EmergeStreamEvent {
        LabelManager.Label selfLabel() ;
        long offset() ;
        long length() ;
    }

    public interface BoolArrEvent extends ArrayEvent {
        boolean[] value() ;
    }

    public interface CharArrEvent extends ArrayEvent {
        char[] value() ;
    }

    public interface ByteArrEvent extends ArrayEvent {
        byte[] value() ;
    }

    public interface ShortArrEvent extends ArrayEvent {
        short[] value() ;
    }

    public interface IntArrEvent extends ArrayEvent {
        int[] value() ;
    }

    public interface LongArrEvent extends ArrayEvent {
        long[] value() ;
    }

    public interface FloatArrEvent extends ArrayEvent {
        float[] value() ;
    }

    public interface DoubleArrEvent extends ArrayEvent {
        double[] value() ;
    }

    public interface RefArrEvent extends ArrayEvent {
        LabelManager.Label[] value() ;
    }

    /** A RefEvent is followed by numParts() SimplePartEvents.
     */
    public interface RefEvent extends EmergeStreamEvent {
        LabelManager.Label selfLabel() ;
        long numParts() ;
    }

    /** A SimplePartEvent is followed by length() PrimitiveEvents.
     */
    public interface SimplePartEvent extends EmergeStreamEvent {
        LabelManager.Label typeLabel() ;
        long offset() ;
        long length() ;
    }

    /** A CustomPartEvent is followed by length() PrimitiveEvents (as
     * in the SimplePartEvent), then a TupleEvent (start),
     * a sequence of PrimitiveEvents, and a TupleEvent (finish).
     */
    public interface CustomPartEvent extends SimplePartEvent {
    }

    /** Represents the beginning or end of a tuple sequence.
     */
    public interface TupleEvent extends EmergeStreamEvent {
        EmergeCode.TupleCode code() ;
    }

    /** Base interface for all Label message requests and replies.
     * The following events depend on the label value as follows:
     * <UL>
     * <LI>REQUEST: no events
     * <LI>REPLY GOOD: a ref-data event
     * <LI>REPLY BAD: no events
     * </UL>
     */
    public interface LabelMessageEvent extends EmergeStreamEvent {
        EmergeCode.LabelMsg labelMsg() ;
        LabelManager.Label label() ;
    }

    public interface LabelMessageReplyGoodEvent extends LabelMessageEvent {
    }

    public interface LabelMessageReplyBadEvent extends LabelMessageEvent {
        long reasonCodeCategory() ;
        long reasonCodeMinorCode() ;
    }

    public interface CloseSessionMessageEvent extends EmergeStreamEvent {
        long sessionId() ;
    }

    public interface RejectRequestMessageEvent extends EmergeStreamEvent {
        long reasonCodeCategory() ;
        long reasonCodeMinorCode() ;
    }

    public interface FiberListMessageEvent extends EmergeStreamEvent {
        long[] fibers() ;
    }

    /** Represents a data message transferring some number
     * of arguments.  Sequence:
     * <UL>
     * <LI>MessageEvent (start)
     * <LI>TupleEvent (start)
     * <LI>numArgs() primitives
     * <LI>TupleEvent (end)
     * <LI>Any number of arrays or RefEvent (with its own events)
     * <LI>MessageEvent (end)
     */
    public interface MessageEvent extends EmergeStreamEvent {
        EmergeCode.MsgCode msgCode() ;
        long requestId() ;
        long sessionId() ;
        long fiberId() ;
        long numArgs() ;
    }
}

