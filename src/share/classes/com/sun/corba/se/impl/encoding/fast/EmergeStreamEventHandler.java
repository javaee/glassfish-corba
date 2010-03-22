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
package com.sun.corba.se.impl.encoding.fast ;

public interface EmergeStreamEventHandler {
    /** Primitive event representing a null value.
     */
    public void nullEvent() ;

    /** Primitive event representing a label .
     */
    public void indirEvent( LabelManager.Label label ) ;

    /** Primitive event representing a boolean.
     */
    public void boolEvent( boolean value ) ;

    /** Primitive event representing a byte.
     */
    public void byteEvent( byte value ) ;

    /** Primitive event representing a character.
     */
    public void charEvent( char value ) ;

    /** Primitive event representing a short.
     */
    public void shortEvent( short value ) ;

    /** Primitive event representing an integer.
     */
    public void intEvent( int value ) ;

    /** Primitive event representing a long.
     */
    public void longEvent( long value ) ;

    /** Primitive event representing a float.
     */
    public void floatEvent( float value ) ;

    /** Primitive event representing a double.
     */
    public void doubleEvent( double value ) ;

    public void boolArrEvent(
	LabelManager.Label selfLabel,
	long offset,
	long length,
	boolean[] value ) ;

    public void charArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	char[] value ) ;

    public void byteArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	byte[] value ) ;

    public void shortArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	short[] value ) ;

    public void intArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	int[] value ) ;

    public void longArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	long[] value ) ;

    public void floatArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	float[] value ) ;

    public void doubleArrEvent( 
	LabelManager.Label selfLabel,
	long offset,
	long length,
	double[] value ) ;

    public void refArrEvent( 
	LabelManager.Label selfLabel,
	LabelManager.Label typeLabel,
	long offset,
	long length,
	LabelManager.Label[] value ) ;

    /** A refEvent is followed by numParts simplePartEvents.
     */
    public void refEvent(
	LabelManager.Label selfLabel,
	long numParts ) ;

    /** A simplePartEvent is followed by length primitiveEvents.
     */
    public void simplePartEvent(
	LabelManager.Label typeLabel,
	long offset,
	long length ) ;

    /** A customPartEvent is followed by length primitiveEvents (as
     * in the simplePartEvent), then a tupleEvent (start),
     * a sequence of primitiveEvents, and a tupleEvent (finish).
     */
    public void customPartEvent(
	LabelManager.Label typeLabel,
	long offset,
	long length ) ;

    /** Represents the beginning of a tuple sequence.
     */
    public void tupleStartEvent() ;

    /** Represents the end of a tuple sequence.
     */
    public void tupleEndEvent() ;

    /** Base interface for all Label message requests and replies.
     * The following events depend on the label value as follows:
     * <UL>
     * <LI>REQUEST: no events
     * <LI>REPLY GOOD: a ref-data event
     * <LI>REPLY BAD: no events
     * </UL>
     */
    /** A label request message.
     */
    public void labelMessageRequestEvent(
	LabelManager.Label label ) ;

    /** A successful reply to a label request message.
     * Followed by a refData event.
     */
    public void labelMessageReplyGoodEvent(
	LabelManager.Label label ) ;

    /** A reply with error to a label request message.
     */
    public void labelMessageReplyBadEvent(
	LabelManager.Label label,
	long reasonCodeCategory,
	long reasonCodeMinorCode ) ;

    public void closeSessionMessageEvent(
	long sessionId ) ;

    public void rejectRequestMessageEvent(
	long reasonCodeCategory,
	long reasonCodeMinorCode ) ;

    public void fiberListMessageEvent(
	long[] fibers ) ;

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
    public void messageStartEvent(
	long requestId,
	long sessionId,
	long fiberId,
	long numArgs ) ;

    public void messageEndEvent(
	long requestId,
	long sessionId,
	long fiberId,
	long numArgs ) ;
}


