/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.pept.protocol.MessageMediator;

import com.sun.corba.se.spi.encoding.CorbaOutputObject ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator ;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.transport.CorbaConnection;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.impl.transport.MessageTraceManagerImpl;

/**
 * @author Harold Carr
 */
public class CDROutputObject extends CorbaOutputObject
{
    private Message header;
    private ORB orb;
    private ORBUtilSystemException wrapper;
    private OMGSystemException omgWrapper;

    // REVISIT - only used on sendCancelRequest.
    private CorbaConnection connection;

    private CDROutputObject(
        ORB orb, GIOPVersion giopVersion, Message header,
	BufferManagerWrite manager, byte streamFormatVersion,
	CorbaMessageMediator mediator)
    {
	super(orb, giopVersion, header.getEncodingVersion(),
	      false, manager, streamFormatVersion,
	      ((mediator != null && mediator.getConnection() != null) ?
	       ((CorbaConnection)mediator.getConnection()).
	             shouldUseDirectByteBuffers() : false));

	this.header = header;
        this.orb = orb;
	this.wrapper = orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
	this.omgWrapper = orb.getLogWrapperTable().get_RPC_ENCODING_OMG() ;

        getBufferManager().setOutputObject(this);
	this.corbaMessageMediator = mediator;
    }

    public CDROutputObject(ORB orb,
			   MessageMediator messageMediator,
			   Message header,
			   byte streamFormatVersion) 
    {
        this(
	    orb, 
	    ((CorbaMessageMediator)messageMediator).getGIOPVersion(), 
	    header, 
	    BufferManagerFactory.newBufferManagerWrite(
		((CorbaMessageMediator)messageMediator).getGIOPVersion(),
		header.getEncodingVersion(),
		orb),
	    streamFormatVersion,
	    (CorbaMessageMediator)messageMediator);
    }

    // NOTE: 
    // Used in SharedCDR (i.e., must be grow).
    // Used in msgtypes test.
    public CDROutputObject(ORB orb,
			   MessageMediator messageMediator,
			   Message header,
			   byte streamFormatVersion,
			   int strategy) 
    {
        this( 
	    orb, 
	    ((CorbaMessageMediator)messageMediator).getGIOPVersion(), 
	    header, 
            BufferManagerFactory.
	        newBufferManagerWrite(strategy,
				      header.getEncodingVersion(),
				      orb),
	    streamFormatVersion,
	    (CorbaMessageMediator)messageMediator);
    }

    // REVISIT 
    // Used on sendCancelRequest.
    // Used for needs addressing mode.
    public CDROutputObject(ORB orb, CorbaMessageMediator mediator,
			   GIOPVersion giopVersion,
			   CorbaConnection connection, Message header,
			   byte streamFormatVersion)
    {
        this(
	    orb, 
	    giopVersion, 
	    header, 
            BufferManagerFactory.
	    newBufferManagerWrite(giopVersion,
				  header.getEncodingVersion(),
				  orb),
	    streamFormatVersion,
	    mediator);
	this.connection = connection ;
    }

    // XREVISIT
    // Header should only be in message mediator.
    // Another possibility: merge header and message mediator.
    // REVISIT - make protected once all encoding together
    public Message getMessageHeader() {
        return header;
    }

    public final void finishSendingMessage() {
        getBufferManager().sendMessage();
    }

    /**
     * Write the contents of the CDROutputStream to the specified
     * output stream.  Has the side-effect of pushing any current
     * Message onto the Message list.
     * @param s The output stream to write to.
     */
    public void writeTo(CorbaConnection connection)
	throws java.io.IOException 
    {

        //
        // Update the GIOP MessageHeader size field.
        //

        ByteBufferWithInfo bbwi = getByteBufferWithInfo();

        getMessageHeader().setSize(bbwi.getByteBuffer(), bbwi.getSize());

	ORB orb = (ORB)orb() ;
        if (orb != null) {
	    if (orb.transportDebugFlag) {
		dprint(".writeTo: " + connection);
	    }
	    if (orb.giopDebugFlag) {
		ORBUtility.printBuffer( "CDROutputObject Buffer", 
                                bbwi.getByteBuffer(), System.out ) ;
	    }
	
	    CorbaTransportManager ctm = 
		(CorbaTransportManager)orb.getTransportManager() ;
	    MessageTraceManagerImpl mtm = 
		(MessageTraceManagerImpl)ctm.getMessageTraceManager() ;
	    if (mtm.isEnabled()) {
		mtm.recordDataSent( bbwi.getByteBuffer()) ;
	    }
        }

	bbwi.flip();
	connection.write(bbwi.getByteBuffer());
    }

    /** overrides create_input_stream from CDROutputStream */
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        // XREVISIT
	return null;
        //return new XIIOPInputStream(orb(), getByteBuffer(), getIndex(), 
	    //isLittleEndian(), getMessageHeader(), conn);
    }

    public CorbaConnection getConnection() 
    {
	// REVISIT - only set when doing sendCancelRequest.
	if (connection != null) {
	    return connection;
	}
	return (CorbaConnection) corbaMessageMediator.getConnection();
    }

    // XREVISIT - If CDROutputObject doesn't live in the iiop
    // package, it will need this, here, to give package access
    // to xgiop.
    // REVISIT - make protected once all encoding together
    public final ByteBufferWithInfo getByteBufferWithInfo() {
        return super.getByteBufferWithInfo();
    }

    // REVISIT - make protected once all encoding together
    public final void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        super.setByteBufferWithInfo(bbwi);
    }

    /**
     * Override the default CDR factory behavior to get the
     * negotiated code sets from the connection.
     *
     * These are only called once per message, the first time needed.
     *
     * In the local case, there is no Connection, so use the
     * local code sets.
     */
    protected CodeSetConversion.CTBConverter createCharCTBConverter() {
        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, fall back on the defaults defined
        // in CDRInputStream.
        if (codesets == null)
            return super.createCharCTBConverter();
        
        OSFCodeSetRegistry.Entry charSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getCharCodeSet());

        if (charSet == null)
	    throw wrapper.unknownCodeset( charSet ) ;

        return CodeSetConversion.impl().getCTBConverter(charSet, 
                                                        isLittleEndian(), 
                                                        false);
    }

    protected CodeSetConversion.CTBConverter createWCharCTBConverter() {

        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, we have to throw an exception.
        // See CORBA formal 00-11-03 13.9.2.6.
        if (codesets == null) {
            if (getConnection().isServer())
		throw omgWrapper.noClientWcharCodesetCtx() ;
            else
		throw omgWrapper.noServerWcharCodesetCmp() ;
        }

        OSFCodeSetRegistry.Entry wcharSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getWCharCodeSet());

        if (wcharSet == null)
	    throw wrapper.unknownCodeset( wcharSet ) ;

        boolean useByteOrderMarkers
            = ((ORB)orb()).getORBData().useByteOrderMarkers();

        // With UTF-16:
        //
        // For GIOP 1.2, we can put byte order markers if we want to, and
        // use the default of big endian otherwise.  (See issue 3405b)
        //
        // For GIOP 1.1, we don't use BOMs and use the endianness of
        // the stream.
        if (wcharSet == OSFCodeSetRegistry.UTF_16) {
            if (getGIOPVersion().equals(GIOPVersion.V1_2)) {
                return CodeSetConversion.impl().getCTBConverter(wcharSet, 
                                                                false, 
                                                                useByteOrderMarkers);
            }

            if (getGIOPVersion().equals(GIOPVersion.V1_1)) {
                return CodeSetConversion.impl().getCTBConverter(wcharSet,
                                                                isLittleEndian(),
                                                                false);
            }
        }

        // In the normal case, let the converter system handle it
        return CodeSetConversion.impl().getCTBConverter(wcharSet, 
                                                        isLittleEndian(),
                                                        useByteOrderMarkers);
    }

    // If we're local and don't have a Connection, use the
    // local code sets, otherwise get them from the connection.
    // If the connection doesn't have negotiated code sets
    // yet, then we use ISO8859-1 for char/string and wchar/wstring
    // are illegal.
    private CodeSetComponentInfo.CodeSetContext getCodeSets() {
        if (getConnection() == null)
            return CodeSetComponentInfo.LOCAL_CODE_SETS;
        else
            return getConnection().getCodeSetContext();
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("CDROutputObject", msg);
    }
}

// End of file.
