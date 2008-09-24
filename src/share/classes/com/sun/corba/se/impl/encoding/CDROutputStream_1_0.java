/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.math.BigDecimal;

import java.nio.ByteBuffer;

import java.rmi.Remote;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import javax.rmi.CORBA.EnumDesc;
import javax.rmi.CORBA.ProxyDesc;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import java.lang.reflect.Proxy;

import javax.rmi.CORBA.ValueHandler;
import javax.rmi.CORBA.ValueHandlerMultiFormat;

import org.omg.CORBA.CustomMarshal;
import org.omg.CORBA.DataOutputStream;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.VM_CUSTOM;
import org.omg.CORBA.VM_TRUNCATABLE;
import org.omg.CORBA.VM_NONE;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.CORBA.portable.CustomValue;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueBase;

import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.transport.ByteBufferPool;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.IORFactories;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.impl.encoding.ByteBufferWithInfo;
import com.sun.corba.se.impl.encoding.MarshalOutputStream;
import com.sun.corba.se.impl.encoding.CodeSetConversion;
import com.sun.corba.se.impl.corba.TypeCodeImpl;
import com.sun.corba.se.impl.orbutil.CacheTable;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.DprintUtil ;
import com.sun.corba.se.impl.orbutil.RepositoryIdStrings;
import com.sun.corba.se.impl.orbutil.RepositoryIdUtility;
import com.sun.corba.se.impl.orbutil.RepositoryIdFactory;
import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import com.sun.corba.se.impl.orbutil.ClassInfoCache ;

import com.sun.corba.se.spi.btrace.* ;

@Traceable
public class CDROutputStream_1_0 extends CDROutputStreamBase
{
    private static final int INDIRECTION_TAG = 0xffffffff;

    protected final DprintUtil dputil = new DprintUtil( this ) ;
    protected boolean littleEndian;
    protected BufferManagerWrite bufferManagerWrite;
    ByteBufferWithInfo bbwi;

    protected ORB orb;
    protected TimingPoints tp;
    protected ORBUtilSystemException wrapper ;

    // XXX These appear to always contain the same value: remove one
    protected int blockSizeIndex = -1;
    protected int blockSizePosition = 0;

    protected byte streamFormatVersion;

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String kWriteMethod = "write";

    // Enum cache
    Map<String,Map<String,EnumDesc>> enumCache = null ;

    // Codebase cache
    // Note that a CacheTable here fails badly on read.  Why?
    // This suggests that different codebase strings with the
    // same characters are being used, but that does not explain
    // the read-side failure.
    // ALTCODEBASE
    // private CacheTable<String> codebaseCache = null;
    private Map<String,Integer> codebaseCache = null;

    // Value cache
    private CacheTable<java.lang.Object> valueCache = null;

    // Repository ID cache
    private CacheTable<String> repositoryIdCache = null;

    // Write end flag
    private int end_flag = 0;

    // Beginning with the resolution to interop issue 3526,
    // only enclosing chunked valuetypes are taken into account
    // when computing the nesting level.  However, we still need
    // the old computation around for interoperability with our
    // older ORBs.
    private int chunkedValueNestingLevel = 0;

    private boolean mustChunk = false;

    // In block marker
    protected boolean inBlock = false;

    // Last end tag position
    private int end_flag_position = 0;
    private int end_flag_index = 0;

    // ValueHandler
    private ValueHandler valueHandler = null;

    // Repository ID handlers
    private RepositoryIdUtility repIdUtil;
    private RepositoryIdStrings repIdStrs;

    // Code set converters (created when first needed)
    private CodeSetConversion.CTBConverter charConverter;
    private CodeSetConversion.CTBConverter wcharConverter;
    
    // REVISIT - This should be re-factored so that including whether
    // to use pool byte buffers or not doesn't need to be known.
    public void init(org.omg.CORBA.ORB orb,
                        boolean littleEndian,
                        BufferManagerWrite bufferManager,
                        byte streamFormatVersion,
                        boolean usePooledByteBuffers)
    {
        // ORB must not be null.  See CDROutputStream constructor.
        this.orb = (ORB)orb;
	tp = this.orb.getTimerManager().points() ;
	this.wrapper = this.orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;

        this.littleEndian = littleEndian;
        this.bufferManagerWrite = bufferManager;
        this.bbwi = new ByteBufferWithInfo(orb, bufferManager, usePooledByteBuffers);
	this.streamFormatVersion = streamFormatVersion;

        createRepositoryIdHandlers();
    }

    public void init(org.omg.CORBA.ORB orb,
                        boolean littleEndian,
                        BufferManagerWrite bufferManager,
                        byte streamFormatVersion)
   {
       init(orb, littleEndian, bufferManager, streamFormatVersion, true);
   }

    private final void createRepositoryIdHandlers()
    {
        if (orb != null) {
            // Get the appropriate versions based on the ORB version.  The
            // ORB versioning info is only in the core ORB.
            repIdUtil 
                = RepositoryIdFactory.getRepIdUtility(orb);
            repIdStrs 
                = RepositoryIdFactory.getRepIdStringsFactory(orb);
        } else {
            // Get the latest versions
            repIdUtil = RepositoryIdFactory.getRepIdUtility();
            repIdStrs = RepositoryIdFactory.getRepIdStringsFactory();
        }
    }

    public BufferManagerWrite getBufferManager()
    {
	return bufferManagerWrite;
    }

    public byte[] toByteArray() {
    	byte[] it;

    	it = new byte[bbwi.position()];

        bbwi.getByteBuffer().position(0);
        bbwi.getByteBuffer().get(it);

    	return it;
    }

    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_0;
    }

    // Called by Request and Reply message. Valid for GIOP versions >= 1.2 only.
    // Illegal for GIOP versions < 1.2.
    void setHeaderPadding(boolean headerPadding) {
        throw wrapper.giopVersionError();
    }

    @CDR
    protected void handleSpecialChunkBegin(int requiredSize)
    {
        // No-op for GIOP 1.0
    }

    @CDR
    protected void handleSpecialChunkEnd()
    {
        // No-op for GIOP 1.0
    }

    protected final int computeAlignment4() {
	int incr = bbwi.position() & 3 ;
	if (incr != 0)
	    return 4-incr ;
	return 0 ;
    }

    protected final int computeAlignment(int align) {
        if (align > 1) {
            int incr = bbwi.position() & (align - 1);
            if (incr != 0)
                return align - incr;
        }

        return 0;
    }

    protected void alignAndReserve44() {
        bbwi.position(bbwi.position() + computeAlignment4());

        if (bbwi.position() + 4  > bbwi.getLength())
            grow44();
    }

    protected void alignAndReserve(int align, int n) {

        bbwi.position(bbwi.position() + computeAlignment(align));

        if (bbwi.position() + n  > bbwi.getLength())
            grow(align, n);
    }

    protected void grow44() {
        bbwi.setNumberOfBytesNeeded(4);

        bufferManagerWrite.overflow(bbwi);
    }

    //
    // Default implementation of grow.  Subclassers may override this.
    // Always grow the single buffer. This needs to delegate
    // fragmentation policy for IIOP 1.1.
    //
    protected void grow(int align, int n) 
    {
        bbwi.setNumberOfBytesNeeded(n);

        bufferManagerWrite.overflow(bbwi);
    }

    public final void putEndian() throws SystemException {
    	write_boolean(littleEndian);
    }

    public final boolean littleEndian() {
    	return littleEndian;
    }

    void freeInternalCaches() {
	if (codebaseCache != null) {
	    // ALTCODEBASE
	    // codebaseCache.done() ;
	    codebaseCache.clear();
	}

	freeValueCache() ;
		
	if (repositoryIdCache != null)
	    repositoryIdCache.done();
    }

    // No such type in java
    public final void write_longdouble(double x) 
    {
	throw wrapper.longDoubleNotImplemented(
	    CompletionStatus.COMPLETED_MAYBE ) ;
    }

    @PrimitiveWrite
    public void write_octet(byte x) 
    {
        if (orb.cdrDebugFlag)
            dputil.enter( "write_octet", x ) ;

        try {
            // The 'if' stmt is commented out since we need the alignAndReserve to
            // be called, particularly when the first body byte is written,
            // to induce header padding to align the body on a 8-octet boundary,
            // for GIOP versions 1.2 and above. Refer to internalWriteOctetArray()
            // method that also has a similar change.
            alignAndReserve(1, 1);

            bbwi.getByteBuffer().put(x);
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    public final void write_boolean(boolean x)
    {
	write_octet(x? (byte)1:(byte)0);
    }

    public void write_char(char x) 
    {
        CodeSetConversion.CTBConverter converter = getCharConverter();

        converter.convert(x);

        // CORBA formal 99-10-07 15.3.1.6: "In the case of multi-byte encodings
        // of characters, a single instance of the char type may only
        // hold one octet of any multi-byte character encoding."
        if (converter.getNumBytes() > 1)
	    throw wrapper.invalidSingleCharCtb(CompletionStatus.COMPLETED_MAYBE);

        write_octet(converter.getBytes()[0]);
    }

    private final void writeLittleEndianWchar(char x) {
        bbwi.getByteBuffer().put((byte)(x & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    }

    private final void writeBigEndianWchar(char x) {
        bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    	bbwi.getByteBuffer().put((byte)(x & 0xFF));
    }

    private final void writeLittleEndianShort(short x) {
        bbwi.getByteBuffer().put((byte)(x & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    }

    private final void writeBigEndianShort(short x) {
        bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    	bbwi.getByteBuffer().put((byte)(x & 0xFF));
    }

    private final void writeLittleEndianLong(int x) {
     	bbwi.getByteBuffer().put((byte)(x & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 16) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 24) & 0xFF));
    }

    private final void writeBigEndianLong(int x) {
        bbwi.getByteBuffer().put((byte)((x >>> 24) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 16) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    	bbwi.getByteBuffer().put((byte)(x & 0xFF));
    }

    private final void writeLittleEndianLongLong(long x) {
        bbwi.getByteBuffer().put((byte)(x & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 16) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 24) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 32) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 40) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 48) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 56) & 0xFF));
    }

    private final void writeBigEndianLongLong(long x) {
        bbwi.getByteBuffer().put((byte)((x >>> 56) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 48) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 40) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 32) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 24) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 16) & 0xFF));
    	bbwi.getByteBuffer().put((byte)((x >>> 8) & 0xFF));
    	bbwi.getByteBuffer().put((byte)(x & 0xFF));
    }

    @PrimitiveWrite
    public void write_wchar(char x)
    {
        if (orb.cdrDebugFlag)
            dputil.enter( "write_wchar", x ) ;

        try {
            // Don't allow transmission of wchar/wstring data with
            // foreign ORBs since it's against the spec.
            if (ORBUtility.isForeignORB(orb)) {
                throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);
            }

            // If it's one of our legacy ORBs, do what they did:
            alignAndReserve(2, 2);
            
            if (littleEndian) {
                writeLittleEndianWchar(x);
            } else {
                writeBigEndianWchar(x);
            }
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    @PrimitiveWrite
    public void write_short(short x) 
    {
        if (orb.cdrDebugFlag)
            dputil.enter( "write_short", x ) ;

        try {
            alignAndReserve(2, 2);
            
            if (littleEndian) {
                writeLittleEndianShort(x);
            } else {
                writeBigEndianShort(x);
            }
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    public final void write_ushort(short x)
    {
	write_short(x);
    }

    @PrimitiveWrite
    public void write_long(int x) 
    {
        if (orb.cdrDebugFlag)
            dputil.enter( "write_long", x ) ;

        try {
            alignAndReserve44() ;

            if (littleEndian) {
                writeLittleEndianLong(x);
            } else {
                writeBigEndianLong(x);
            }
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    public final void write_ulong(int x)
    {
	write_long(x);
    }

    @PrimitiveWrite
    public void write_longlong(long x) 
    {
        if (orb.cdrDebugFlag)
            dputil.enter( "write_longlong", x ) ;

        try {
            alignAndReserve(8, 8);

            if (littleEndian) {
                writeLittleEndianLongLong(x);
            } else {
                writeBigEndianLongLong(x);
            }
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    public final void write_ulonglong(long x)
    {
	write_longlong(x);
    }

    public final void write_float(float x) 
    {
	write_long(Float.floatToIntBits(x));
    }

    public final void write_double(double x) 
    {
	write_longlong(Double.doubleToLongBits(x));
    }

    public void write_string(String value)
    {
      writeString(value);
    }

    @PrimitiveWrite
    protected int writeString(String value)
    {
        if (orb.cdrDebugFlag)
            dputil.enter( "writeString", value ) ;

        try {
            if (value == null) {
                throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
            }

            CodeSetConversion.CTBConverter converter = getCharConverter();

            converter.convert(value);

            // A string is encoded as an unsigned CORBA long for the
            // number of bytes to follow (including a terminating null).
            // There is only one octet per character in the string.
            int len = converter.getNumBytes() + 1;

            handleSpecialChunkBegin(computeAlignment(4) + 4 + len);

            write_long(len);
            int indirection = get_offset() - 4;

            internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());

            // Write the null ending
            write_octet((byte)0);

            handleSpecialChunkEnd();
            return indirection;
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    public void write_wstring(String value)
    {
        if (value == null)
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // Don't allow transmission of wchar/wstring data with
        // foreign ORBs since it's against the spec.
        if (ORBUtility.isForeignORB(orb)) {
	    throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);
        }
            
        // When talking to our legacy ORBs, do what they did:
    	int len = value.length() + 1;

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(4 + (len * 2) + computeAlignment(4));

        write_long(len);

        for (int i = 0; i < len - 1; i++)
            write_wchar(value.charAt(i));

        // Write the null ending
        write_short((short)0);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    // Performs no checks and doesn't tamper with chunking
    void internalWriteOctetArray(byte[] value, int offset, int length)
    {
    	int n = offset;

	// This flag forces the alignAndReserve method to be called the
	// first time an octet is written. This is necessary to ensure
	// that the body is aligned on an 8-octet boundary. Note the 'if'
	// condition inside the 'while' loop below. Also, refer to the
	// write_octet() method that has a similar change.
	boolean align = true;
            
    	while (n < length+offset) {
    	    int avail;
    	    int bytes;
    	    int wanted;

            if ((bbwi.position() + 1 > bbwi.getLength()) || align) {
		align = false;
        	alignAndReserve(1, 1);
	    }
    	    avail = bbwi.getLength() - bbwi.position();
    	    wanted = (length + offset) - n;
    	    bytes = (wanted < avail) ? wanted : avail;
	    bbwi.getByteBuffer().put(value, n, bytes); 
	    bbwi.position(bbwi.getByteBuffer().position()); 
    	    n += bytes;
    	}
    }

    public final void write_octet_array(byte b[], int offset, int length)
    {
        if ( b == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(length);

        internalWriteOctetArray(b, offset, length);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    @SuppressWarnings({"deprecation"})
    public void write_Principal(org.omg.CORBA.Principal p)
    {
    	write_long(p.name().length);
    	write_octet_array(p.name(), 0, p.name().length);
    }

    @CDR
    @CDRWrite
    public void write_any(Any any) 
    {
        tp.enter_writeAny() ;
        if (orb.cdrDebugFlag) {
            dputil.enter("write_Any", "any", any );
	}

        try {
            if ( any == null )
                throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

            write_TypeCode(any.type());
            any.write_value(parent);
        } finally {
            tp.exit_writeAny() ;
            if (orb.cdrDebugFlag) {
                dputil.exit();
            }
        }
    }

    @CDR
    @CDRWrite
    public void write_TypeCode(TypeCode tc)
    {
        if (orb.cdrDebugFlag) {
            dputil.enter("write_TypeCode", "tc", tc );
	}

        try {
            if ( tc == null ) {
                throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
            }
            TypeCodeImpl tci;
            if (tc instanceof TypeCodeImpl) {
                tci = (TypeCodeImpl)tc;
            } else {
                tci = new TypeCodeImpl(orb, tc);
            }

            tci.write_value((org.omg.CORBA_2_3.portable.OutputStream)parent);
        } finally {
            if (orb.cdrDebugFlag) {
                dputil.exit();
            }
        }
    }
 
    @CDR
    @CDRWrite
    public void write_Object(org.omg.CORBA.Object ref)
    {
        if (ref == null) {
	    IOR nullIOR = IORFactories.makeIOR( orb ) ;
            nullIOR.write(parent);
            return;
        }
		
        // IDL to Java formal 01-06-06 1.21.4.2
        if (ref instanceof org.omg.CORBA.LocalObject)
	    throw wrapper.writeLocalObject(CompletionStatus.COMPLETED_MAYBE);
    
	IOR ior = orb.getIOR( ref, true ) ;
	ior.write(parent);
	return;
    }

    // ------------ RMI related methods --------------------------

    @CDR
    @CDRWrite
    public void write_abstract_interface(java.lang.Object obj) {
	boolean corbaObject = false; // Assume value type.
	org.omg.CORBA.Object theObject = null;
	    
	// Is it a CORBA.Object?
	    
	if (obj != null && obj instanceof org.omg.CORBA.Object) {
	        
	    // Yes.
	        
	    theObject = (org.omg.CORBA.Object)obj;
	    corbaObject = true;	        
	}
	    
	// Write our flag...
	    
	write_boolean(corbaObject);
	    
	// Now write out the object...
	    
	if (corbaObject) {
	    write_Object(theObject);
	} else {
	    try {
		write_value((java.io.Serializable)obj);
	    } catch(ClassCastException cce) {
		if (obj instanceof java.io.Serializable)
		    throw cce;
		else
                    ORBUtility.throwNotSerializableForCorba(obj.getClass().getName());
	    }
	}
    }

    @CDR
    @CDRWrite
    public void write_value(Serializable object, Class clz) {

	write_value(object); 
    }

    @CDR
    @CDRWrite
    private void startValueChunk( boolean useChunking ) {
        if (useChunking) {
            start_block();
            chunkedValueNestingLevel--;
        } 

	end_flag--;
    }

    @CDR
    @CDRWrite
    private void endValueChunk( boolean useChunking ) {
        if (useChunking) {
            end_block();
	}

	writeEndTag(useChunking);
    }

    private void writeWStringValue(String string) {

        int indirection = writeValueTag(mustChunk, true, null);
            
        write_repositoryId(repIdStrs.getWStringValueRepId());
            
        updateIndirectionTable(indirection, string);

	startValueChunk(mustChunk) ;
        write_wstring(string);
	endValueChunk(mustChunk) ; 
    }

    @CDR
    @CDRWrite
    private void writeArray(Serializable array, Class clazz) {
        if (valueHandler == null)
            valueHandler = ORBUtility.createValueHandler(orb); 

        // Write value_tag
        int indirection = writeValueTag(mustChunk, true, 
	    Util.getInstance().getCodebase(clazz));
				
        // Write repository ID
        write_repositoryId(repIdStrs.createSequenceRepID(clazz));
				
        // Add indirection for object to indirection table
        updateIndirectionTable(indirection, array);
				
        callWriteValue( parent, array, streamFormatVersion ) ;
    }

    @CDR
    @CDRWrite
    private void writeValueBase(org.omg.CORBA.portable.ValueBase object,
                                Class clazz) {
        // _REVISIT_ could check to see whether chunking really needed 
        mustChunk = true;
			
        int indirection = writeValueTag(true, true, 
	    Util.getInstance().getCodebase(clazz));
			
        String repId = ((ValueBase)object)._truncatable_ids()[0];
        write_repositoryId(repId);
			
        updateIndirectionTable(indirection, object);
        
	startValueChunk(true) ;
        writeIDLValue(object, repId);
	endValueChunk(true) ;
    }

    // We know that object is not null, because that was checked in 
    // write_value( Serializable, String )
    @CDR
    @CDRWrite
    private void writeRMIIIOPValueType(Serializable object, Class clazz, 
	ClassInfoCache.ClassInfo cinfo) {

        if (valueHandler == null)
            valueHandler = ORBUtility.createValueHandler(orb); 

        Serializable key = object;

        // Allow the ValueHandler to call writeReplace on
        // the Serializable (if the method is present)
        tp.enter_callValueHandlerWriteReplaceFromCDRStream() ;
        try {
            object = valueHandler.writeReplace(key);
        } finally {
            tp.exit_callValueHandlerWriteReplaceFromCDRStream() ;
        }
		
        if (object != key) {
	    if (object == null) {
		// If replaced value is null, write null tag and return
		write_long(0);
		return;
	    }
		
	    // write replace changed something
	    if (writeIndirectionIfPossible( object )) {
		return ;
	    }
            
            clazz = object.getClass();
        }

        tp.enter_callValueHandlerIsCustomMarshaledFromCDRStream() ;
        try {
            mustChunk = valueHandler.isCustomMarshaled(clazz) ;
        } finally {
            tp.exit_callValueHandlerIsCustomMarshaledFromCDRStream() ;
        }
				
        // Write value_tag
        int indirection = writeValueTag(mustChunk, true, 
	    Util.getInstance().getCodebase(clazz));
				
        // Write rep. id
        write_repositoryId(repIdStrs.createForJavaType(clazz, cinfo ));
				
        // Add indirection for object to indirection table.
	// If writeReplace nominated a replacement object,
	// store both the replacement and the original object in the
	// table.
        updateIndirectionTable(indirection, key);
	if (object != key)
	    updateIndirectionTable(indirection, object);

        callWriteValue( parent, object, streamFormatVersion ) ;
    }
    
    @CDR
    @CDRWrite
    private void callWriteValue( org.omg.CORBA.portable.OutputStream parent, 
        java.io.Serializable object, byte streamFormatVersion ) {

	boolean currentMustChunk = mustChunk ;
	startValueChunk(currentMustChunk) ;

        tp.enter_callValueHandlerWriteValueFromCDRStream() ;
        try {
            if (valueHandler instanceof ValueHandlerMultiFormat) {
                ValueHandlerMultiFormat vh = (ValueHandlerMultiFormat)valueHandler;
                vh.writeValue(parent, object, streamFormatVersion);
            } else {
                valueHandler.writeValue(parent, object);
            }
        } finally {
            tp.exit_callValueHandlerWriteValueFromCDRStream() ;
        }

	endValueChunk(currentMustChunk) ;
    }

    private EnumDesc getEnumDesc( String className, String enumValue ) {
	EnumDesc result = null ;
	Map<String,EnumDesc> map = null ;

	if (enumCache == null) {
	    enumCache = new HashMap<String,Map<String,EnumDesc>>() ;
	} else {
	    map = enumCache.get( className ) ;
	}

	if (map == null) {
	    map = new HashMap<String,EnumDesc>() ;
	    enumCache.put( className, map ) ;
	} else {
	    result = map.get( enumValue ) ;
	}

	if (result == null) {
	    result = new EnumDesc() ;
	    result.className = className ;
	    result.value = enumValue ;

	    map.put( enumValue, result ) ;
	}

	return result ;
    }

    @CDR
    @CDRWrite
    public void write_value(Serializable object, String repository_id) {
	// Handle null references
	if (object == null) {
	    // Write null tag and return
	    write_long(0);
	    return;
	}

	Class clazz = object.getClass();
	ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( clazz ) ;
	if (cinfo.isEnum()) {
	    String enumValue = ((Enum)object).name() ;
	    EnumDesc desc = getEnumDesc( clazz.getName(), enumValue ) ;
	    write_value( desc, (String)null ) ;
	    return ;
	}

	if (cinfo.isProxyClass()) {
            Class[] ifaces = clazz.getInterfaces();
            ProxyDesc pd = new ProxyDesc();

            pd.interfaces = new String[ifaces.length];
            for (int i=0; i <ifaces.length; ++i) {
                 pd.interfaces[i] = ifaces[i].getName();
            }
            pd.handler = Proxy.getInvocationHandler(object);
            pd.codebase = Util.getInstance().getCodebase(object.getClass());
            write_value(pd, (String)null);
	    return ;
        }

	// Handle shared references
	if (writeIndirectionIfPossible( object )) {
	    return ;
	}

	// Save mustChunk in case a recurisive call from the ValueHandler or IDL
	// generated code calls write_value with a possibly different value of mustChunk
	boolean oldMustChunk = mustChunk ;

	if (inBlock)
	    end_block();

	// XXX Should we classify once, then use a switch to dispatch?
	if (cinfo.isArray()) {
            // Handle arrays
            writeArray(object, clazz);
	} else if (cinfo.isAValueBase( clazz )) {
            // Handle IDL Value types
            writeValueBase((org.omg.CORBA.portable.ValueBase)object, clazz);
	} else if (cinfo.isAIDLEntity( clazz ) && !cinfo.isACORBAObject( clazz )) {
            writeIDLEntity((IDLEntity)object);
	} else if (cinfo.isAString( clazz )) {
            writeWStringValue((String)object);
	} else if (cinfo.isAClass( clazz )) {
            writeClass(repository_id, (Class)object, cinfo );
	} else {
            // RMI-IIOP value type
            writeRMIIIOPValueType( object, clazz, cinfo );
        }
		
	mustChunk = oldMustChunk;

	// Check to see if we need to start another block for a
	// possible outer value
	if (mustChunk)
	    start_block();
    }

    public void write_value(Serializable object)
    {
        write_value(object, (String)null);
    }

    @SuppressWarnings({"deprecation"})
    @CDR
    @CDRWrite
    public void write_value(Serializable object, 
	org.omg.CORBA.portable.BoxedValueHelper factory)
    {
        if (object == null) {
            // Write null tag and return
            write_long(0);
            return;
        }
        
        // Handle shared references
	if (writeIndirectionIfPossible( object )) {
	    return;
	} 

	// Save mustChunk in case a recurisive call from the ValueHandler or IDL
	// generated code calls write_value with a possibly different value of mustChunk
	boolean oldMustChunk = mustChunk;

	boolean isCustom = false;
	if (factory instanceof com.sun.org.omg.CORBA.portable.ValueHelper) {
	    short modifier;
	    try {
		modifier = ((com.sun.org.omg.CORBA.portable.ValueHelper)factory)
		    .get_type().type_modifier();
	    } catch(BadKind ex) {  // tk_value_box
		modifier = VM_NONE.value;
	    }  

	    if (object instanceof CustomMarshal &&
	        modifier == VM_CUSTOM.value) {
		isCustom = true;
		mustChunk = true;
	    }

	    if (modifier == VM_TRUNCATABLE.value)
		mustChunk = true;
	}

	if (mustChunk && inBlock)
	    end_block() ;

	int indirection = writeValueTag(mustChunk, 
	    orb.getORBData().useRepId(), 
	    Util.getInstance().getCodebase(object.getClass()));
		    
	if (orb.getORBData().useRepId()) {
	    write_repositoryId(factory.get_id());
	}
			
	updateIndirectionTable(indirection, object);
		    
	boolean currentMustChunk = mustChunk ;
	startValueChunk(currentMustChunk) ; 
	if (mustChunk && isCustom) {
	    ((CustomMarshal)object).marshal(parent);
	} else {
	    factory.write_value(parent, object);
	}
	endValueChunk(currentMustChunk) ;

	mustChunk = oldMustChunk;

	// Check to see if we need to start another block for a
	// possible outer value
	if (mustChunk)
	    start_block();

    }
	
    public int get_offset() {
	return bbwi.position();
    }

    @CDR
    @CDRWrite
    public void start_block() {
        if (orb.cdrDebugFlag) {
            dputil.enter("start_block", "position", bbwi.position());
	}

        try {
            // Save space in the buffer for block size
            write_long(0);

            // Has to happen after write_long since write_long could
            // trigger grow which is overridden by subclasses to 
            // depend on inBlock.
            inBlock = true; 

            // Note that get_offset is overridden in subclasses to handle fragmentation!
            // Thus blockSizePosition and blockSizeIndex are not always the same!
            blockSizePosition = get_offset();
            blockSizeIndex = bbwi.position() ;
        } finally {
            if (orb.cdrDebugFlag) {
                dputil.info( "blockSizeIndex", blockSizeIndex ) ; 
                dputil.exit() ;
            }
	}
    }

    // Utility method which will hopefully decrease chunking complexity
    // by allowing us to end_block and update chunk lengths without
    // calling alignAndReserve.  Otherwise, it's possible to get into
    // recursive scenarios which lose the chunking state.
    protected void writeLongWithoutAlign(int x) {
        if (orb.cdrDebugFlag) 
            dputil.enter( "writeLongWithoutAlign", x ) ;

        try {
            if (littleEndian) {
                writeLittleEndianLong(x);
            } else {
                writeBigEndianLong(x);
            }
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    @CDR
    @CDRWrite
    public void end_block() {
        if (orb.cdrDebugFlag) {
            dputil.enter("end_block");
	}

        try {
            if (!inBlock)
                return;

            if (orb.cdrDebugFlag) {
                dputil.info("in a block");
            }

            inBlock = false;

            // Test to see if the block was of zero length
            // If so, remove the block instead of ending it
            // (This can happen if the last field written 
            //  in a value was another value)
            if (orb.cdrDebugFlag)
                dputil.info( "blockSizePosition=" + blockSizePosition ) ;

            if (get_offset() == blockSizePosition) {
                if (orb.cdrDebugFlag)
                    dputil.info( "removing 0 length block" ) ;
                // Need to assert that blockSizeIndex == bbwi.position()?  REVISIT

                bbwi.position(bbwi.position() - 4);
                blockSizeIndex = -1;
                blockSizePosition = -1;
                return;
            }

            int oldSize = bbwi.position();
            bbwi.position(blockSizeIndex - 4);

            writeLongWithoutAlign(oldSize - blockSizeIndex);

            bbwi.position(oldSize);
            blockSizeIndex = -1;
            blockSizePosition = -1;
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }
    
    public org.omg.CORBA.ORB orb() {
        return orb;    
    }

    // ------------ End RMI related methods --------------------------
    
    public final void write_boolean_array(boolean[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(length);

        for (int i = 0; i < length; i++)
            write_boolean(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_char_array(char[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(length);

        for (int i = 0; i < length; i++)
            write_char(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public void write_wchar_array(char[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(2) + (length * 2));

        for (int i = 0; i < length; i++)
            write_wchar(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_short_array(short[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(2) + (length * 2));

        for (int i = 0; i < length; i++)
            write_short(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_ushort_array(short[]value, int offset, int length) {
    	write_short_array(value, offset, length);
    }

    public final void write_long_array(int[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(4) + (length * 4));

        for (int i = 0; i < length; i++)
            write_long(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_ulong_array(int[]value, int offset, int length) {
    	write_long_array(value, offset, length);
    }

    public final void write_longlong_array(long[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(8) + (length * 8));

        for (int i = 0; i < length; i++)
            write_longlong(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_ulonglong_array(long[]value, int offset, int length) {
    	write_longlong_array(value, offset, length);
    }

    public final void write_float_array(float[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(4) + (length * 4));

        for (int i = 0; i < length; i++)
            write_float(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public final void write_double_array(double[]value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);

        // This will only have an effect if we're already chunking
        handleSpecialChunkBegin(computeAlignment(8) + (length * 8));

        for (int i = 0; i < length; i++)
            write_double(value[offset + i]);

        // This will only have an effect if we're already chunking
        handleSpecialChunkEnd();
    }

    public void write_string_array(String[] value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
	    
    	for(int i = 0; i < length; i++)
    	    write_string(value[offset + i]);
    }
    
    public void write_wstring_array(String[] value, int offset, int length) {
        if ( value == null )
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
	    
    	for(int i = 0; i < length; i++)
    	    write_wstring(value[offset + i]);
    }

    public final void write_any_array(org.omg.CORBA.Any value[], int offset, int length)
    {
    	for(int i = 0; i < length; i++) 
    	    write_any(value[offset + i]);
    }

    //--------------------------------------------------------------------//
    // CDROutputStream state management.
    //

    public void writeTo(java.io.OutputStream s) 
	throws java.io.IOException 
    {
        byte[] tmpBuf = ORBUtility.getByteBufferArray(bbwi.getByteBuffer());
	s.write(tmpBuf, 0, bbwi.position());	
    }

    public void writeOctetSequenceTo(org.omg.CORBA.portable.OutputStream s) {

        byte[] buf = ORBUtility.getByteBufferArray(bbwi.getByteBuffer());
    	s.write_long(bbwi.position());
    	s.write_octet_array(buf, 0, bbwi.position());

    }

    public final int getSize() {
    	return bbwi.position();
    }

    public int getIndex() {
    	return bbwi.position();
    }

    public boolean isLittleEndian() {
        return littleEndian;
    }

    public void setIndex(int value) {
        bbwi.position(value);
    }

    public ByteBufferWithInfo getByteBufferWithInfo() {
        return bbwi;
    }

    public void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        this.bbwi = bbwi;
    }

    public ByteBuffer getByteBuffer() {
        ByteBuffer result = null;;
        if (bbwi != null) {
            result = bbwi.getByteBuffer();
        }
        return result;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        bbwi.setByteBuffer(byteBuffer);
    }

    private final void freeValueCache() {
	if (valueCache != null)
	    valueCache.done();
    }

    private final void updateIndirectionTable(int indirection, 
        java.lang.Object key) {

	if (valueCache == null)
	    valueCache = new CacheTable<java.lang.Object>(
		"Output valueCache",orb,true);
	valueCache.put(key, indirection);
    }

    private final boolean writeIndirectionIfPossible( final Serializable object ) {
	if (valueCache != null) {
	    final int indir = valueCache.getVal( object ) ;
	    if (indir != -1) {
		writeIndirection(INDIRECTION_TAG, indir );
		return true ;
	    }
	}

	return false ;
    }

    @CDR
    @CDRWrite
    private final void write_repositoryId(String id) {
        // Use an indirection if available
	if (repositoryIdCache != null) {
	    int indir = repositoryIdCache.getVal( id ) ;
	    if (indir != -1) {
		writeIndirection(INDIRECTION_TAG, indir );
		return;
	    }
	}

        // Write it as a string.  Note that we have already done the
        // special case conversion of non-Latin-1 characters to escaped
        // Latin-1 sequences in RepositoryId.

        // It's not a good idea to cache them now that we can have
        // multiple code sets.
        int indirection = writeString(id);

        // Add indirection for id to indirection table
        if (repositoryIdCache == null)
	    repositoryIdCache = new CacheTable<String>("Output repositoryIdCache",orb,true);
        repositoryIdCache.put(id, indirection);
    }

    @CDR
    @CDRWrite
    private void write_codebase(String str, int pos) {
	Integer value = null ;
	if (codebaseCache != null) {
	    // ALTCODEBASE
	    // value = codebaseCache.getVal(str) ;
	    value = codebaseCache.get(str) ;
	}

	if (value != null) {
	    writeIndirection(INDIRECTION_TAG, value);
        } else {
	    write_string(str);
            if (codebaseCache == null) {
		// ALTCODEBASE
        	// codebaseCache = new CacheTable<String>("Output codebaseCache",orb,true);
        	codebaseCache = new HashMap<String,Integer>() ;
	    }

            codebaseCache.put(str, pos );
        }
    }

    @CDR
    @CDRWrite
    private final int writeValueTag(boolean chunkIt, boolean useRepId, 
				    String codebase) {
	int indirection = 0;
	if (chunkIt && !useRepId){
	    if (codebase == null) {
		write_long(repIdUtil.getStandardRMIChunkedNoRepStrId());
		indirection = get_offset() - 4;
	    } else {			
		write_long(repIdUtil.getCodeBaseRMIChunkedNoRepStrId());
		indirection = get_offset() - 4;
		write_codebase(codebase, get_offset());
	    }
	} else if (chunkIt && useRepId){
	    if (codebase == null) {
		write_long(repIdUtil.getStandardRMIChunkedId());
		indirection = get_offset() - 4;
	    } else {			
		write_long(repIdUtil.getCodeBaseRMIChunkedId());
		indirection = get_offset() - 4;
		write_codebase(codebase, get_offset());
	    }
	} else if (!chunkIt && !useRepId) {
	    if (codebase == null) {
		write_long(repIdUtil.getStandardRMIUnchunkedNoRepStrId());
		indirection = get_offset() - 4;
	    } else {			
		write_long(repIdUtil.getCodeBaseRMIUnchunkedNoRepStrId());
		indirection = get_offset() - 4;
		write_codebase(codebase, get_offset());
	    }
	} else if (!chunkIt && useRepId) {
	    if (codebase == null) {
		write_long(repIdUtil.getStandardRMIUnchunkedId());
		indirection = get_offset() - 4;
	    } else {			
		write_long(repIdUtil.getCodeBaseRMIUnchunkedId());
		indirection = get_offset() - 4;
		write_codebase(codebase, get_offset());
	    }
	}
        return indirection;
    }

    @SuppressWarnings({"deprecation"})
    @CDR
    @CDRWrite
    private void writeIDLValue(Serializable object, String repID) {
    	if (object instanceof StreamableValue) {
	    ((StreamableValue)object)._write(parent);
	} else if (object instanceof CustomValue) {
	    ((CustomValue)object).marshal(parent);
	} else {
	    BoxedValueHelper helper = Utility.getHelper(object.getClass(), null, repID);
	    boolean isCustom = false;

	    if (helper instanceof com.sun.org.omg.CORBA.portable.ValueHelper && 
		object instanceof CustomMarshal) {
		try {
		    if (((com.sun.org.omg.CORBA.portable.ValueHelper)helper)
			.get_type().type_modifier() == VM_CUSTOM.value)
		        isCustom = true;
	        } catch(BadKind ex) {
		    throw wrapper.badTypecodeForCustomValue( CompletionStatus.COMPLETED_MAYBE,
			ex ) ;
		}  
	    }

	    if (isCustom)
		((CustomMarshal)object).marshal(parent);
	    else
		helper.write_value(parent, object);
	}
    }

    // Handles end tag compaction...
    @CDR
    @CDRWrite
    private void writeEndTag(boolean chunked){
	if (orb.cdrDebugFlag) 
            dputil.enter( "writeEndTag", "chunked", chunked ) ;

        try {
            if (chunked) {
                if (get_offset() == end_flag_position) {
                    if (bbwi.position() == end_flag_index) {
                        // We are exactly at the same position and index as the
                        // end of the last end tag.  Thus, we can back up over it
                        // and compact the tags.
                        bbwi.position(bbwi.position() - 4);
                    } else {
                        // Special case in which we're at the beginning of a new
                        // fragment, but the position is the same.  We can't back up,
                        // so we just write the new end tag without compaction.  This
                        // occurs when a value ends and calls start_block to open a
                        // continuation chunk, but it's called at the very end of
                        // a fragment.
                    }
                }

                writeNestingLevel();

                // Remember the last index and position.  These are only used when chunking.
                end_flag_index = bbwi.position();
                end_flag_position = get_offset();

                chunkedValueNestingLevel++;
            }

            // Increment the nesting level
            end_flag++;
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    /**
     * Handles ORB versioning of the end tag.  Should only
     * be called if chunking.
     *
     * If talking to our older ORBs (Standard Extension,
     * Kestrel, and Ladybird), write the end flag that takes
     * into account all enclosing valuetypes.
     *
     * If talking a newer or foreign ORB, or if the orb
     * instance is null, write the end flag that only takes
     * into account the enclosing chunked valuetypes.
     */
    @CDR
    private void writeNestingLevel() {
        if (orb == null ||
            ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
            ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {

            write_long(chunkedValueNestingLevel);
        } else {
            write_long(end_flag);
        }
    }

    @CDR
    @CDRWrite
    private void writeClass(String repository_id, Class clz, 
	ClassInfoCache.ClassInfo cinfo ) {

        if (repository_id == null)
            repository_id = repIdStrs.getClassDescValueRepId();

        // Write value_tag
        int indirection = writeValueTag(mustChunk, true, null);
        updateIndirectionTable(indirection, clz);
            			
        write_repositoryId(repository_id);

	startValueChunk(mustChunk) ;
        writeClassBody(clz, cinfo);
	endValueChunk(mustChunk) ;
    }

    // Pre-Merlin/J2EE 1.3 ORBs wrote the repository ID
    // and codebase strings in the wrong order.  This handles
    // backwards compatibility.
    @CDR
    @CDRWrite
    private void writeClassBody(Class clz, ClassInfoCache.ClassInfo cinfo ) {
        if (orb == null ||
            ORBVersionFactory.getFOREIGN().equals(orb.getORBVersion()) ||
            ORBVersionFactory.getNEWER().compareTo(orb.getORBVersion()) <= 0) {

	    write_value(Util.getInstance().getCodebase(clz));
	    write_value(repIdStrs.createForAnyType(clz, cinfo ));
        } else {
	    write_value(repIdStrs.createForAnyType(clz, cinfo ));
	    write_value(Util.getInstance().getCodebase(clz));
        }
    }

    @CDR
    @CDRWrite
    private void writeIDLEntity(IDLEntity object) {
	// _REVISIT_ could check to see whether chunking really needed 
	mustChunk = true;

	String repository_id = repIdStrs.createForJavaType(object);
	final Class clazz = object.getClass();
	String codebase = Util.getInstance().getCodebase(clazz); 
		
	int indirection = writeValueTag(true, true, codebase);
	updateIndirectionTable(indirection, object);
	write_repositoryId(repository_id);
		
	// Write Value chunk
	startValueChunk(true) ;

	// Write the IDLEntity using reflection 
	try {
            ClassLoader clazzLoader = (clazz == null ? null : clazz.getClassLoader());
	    final Class helperClass = Utility.loadClassForClass(clazz.getName()+"Helper", codebase,
                                                   clazzLoader, clazz, clazzLoader);
	    
            // getDeclaredMethod requires RuntimePermission accessDeclaredMembers
            // if a different class loader is used (even though the javadoc says otherwise)
            Method writeMethod = null;
            try {
                writeMethod = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Method>() {
                        public Method run() throws NoSuchMethodException {
                            return helperClass.getDeclaredMethod(kWriteMethod, 
				org.omg.CORBA.portable.OutputStream.class, clazz);
                        }
                    }
                );
            } catch (PrivilegedActionException pae) {
                // this gets caught below
                throw (NoSuchMethodException)pae.getException();
            }
	    writeMethod.invoke(null, parent, object );
	} catch (Exception exc) {
	    throw wrapper.errorInvokingHelperWrite( CompletionStatus.COMPLETED_MAYBE, exc ) ;
	}

	endValueChunk(true) ;
    }
    
    /* DataOutputStream methods */

    @CDR
    @CDRWrite
    public void write_Abstract (java.lang.Object value) {
        write_abstract_interface(value);
    }

    @CDR
    @CDRWrite
    public void write_Value (java.io.Serializable value) {
        write_value(value);
    }

    // This will stay a custom add-on until the java-rtf issue is resolved.
    // Then it should be declared in org.omg.CORBA.portable.OutputStream.
    //
    // Pads the string representation of bigDecimal with zeros to fit the given
    // digits and scale before it gets written to the stream.
    public void write_fixed(java.math.BigDecimal bigDecimal, short digits, short scale) {
        String string = bigDecimal.toString();
        String integerPart;
        String fractionPart;
        StringBuffer stringBuffer;

        // Get rid of the sign
        if (string.charAt(0) == '-' || string.charAt(0) == '+') {
            string = string.substring(1);
        }

        // Determine integer and fraction parts
        int dotIndex = string.indexOf('.');
        if (dotIndex == -1) {
            integerPart = string;
            fractionPart = null;
        } else if (dotIndex == 0 ) {
            integerPart = null;
            fractionPart = string;
        } else {
            integerPart = string.substring(0, dotIndex);
            fractionPart = string.substring(dotIndex + 1);
        }

        // Pad both parts with zeros as necessary
        stringBuffer = new StringBuffer(digits);
        if (fractionPart != null) {
            stringBuffer.append(fractionPart);
        }
        while (stringBuffer.length() < scale) {
            stringBuffer.append('0');
        }
        if (integerPart != null) {
            stringBuffer.insert(0, integerPart);
        }
        while (stringBuffer.length() < digits) {
            stringBuffer.insert(0, '0');
        }

        // This string contains no sign or dot
        this.write_fixed(stringBuffer.toString(), bigDecimal.signum());
    }

    // This method should be remove by the java-rtf issue.
    // Right now the scale and digits information of the type code is lost.
    public void write_fixed(java.math.BigDecimal bigDecimal) {
        // This string might contain sign and/or dot
        this.write_fixed(bigDecimal.toString(), bigDecimal.signum());
    }

    // The string may contain a sign and dot
    public void write_fixed(String string, int signum) {
        int stringLength = string.length();
        // Each octet contains (up to) two decimal digits
        byte doubleDigit = 0;
        char ch;
        byte digit;

        // First calculate the length of the string without optional sign and dot
        int numDigits = 0;
        for (int i=0; i<stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.')
                continue;
            numDigits++;
        }
        for (int i=0; i<stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.')
                continue;
            digit = (byte)Character.digit(ch, 10);
            if (digit == -1) {
		throw wrapper.badDigitInFixed( CompletionStatus.COMPLETED_MAYBE ) ;
            }
            // If the fixed type has an odd number of decimal digits,
            // then the representation begins with the first (most significant) digit.
            // Otherwise, this first half-octet is all zero, and the first digit
            // is in the second half-octet.
            if (numDigits % 2 == 0) {
                doubleDigit |= digit;
                this.write_octet(doubleDigit);
                doubleDigit = 0;
            } else {
                doubleDigit |= (digit << 4);
            }
            numDigits--;
        }
        // The sign configuration, in the last half-octet of the representation,
        // is 0xD for negative numbers and 0xC for positive and zero values
        if (signum == -1) {
            doubleDigit |= 0xd;
        } else {
            doubleDigit |= 0xc;
        }
        this.write_octet(doubleDigit);
    }

    private final static String _id = "IDL:omg.org/CORBA/DataOutputStream:1.0";
    private final static String[] _ids = { _id };

    public String[] _truncatable_ids() {
        if (_ids == null)
            return null;

        return (String[])_ids.clone();
    }

    public void writeIndirection(int tag, int posIndirectedTo)
    {
        // Must ensure that there are no chunks between the tag
        // and the actual indirection value.  This isn't talked about
        // in the spec, but seems to cause headaches in our code.
        // At the very least, this method isolates the indirection code
        // that was duplicated so often.

        handleSpecialChunkBegin(computeAlignment(4) + 8);

        // write indirection tag
        write_long(tag);

        // write indirection
        // Use parent.getRealIndex() so that it can be overridden by TypeCodeOutputStreams
/*
        System.out.println("CDROutputStream_1_0 writing indirection pos " + posIndirectedTo +
                           " - real index " + parent.getRealIndex(get_offset()) + " = " +
                           (posIndirectedTo - parent.getRealIndex(get_offset())));
*/
        write_long(posIndirectedTo - parent.getRealIndex(get_offset()));

        handleSpecialChunkEnd();
    }

    protected CodeSetConversion.CTBConverter getCharConverter() {
        if (charConverter == null)
            charConverter = parent.createCharCTBConverter();
        
        return charConverter;
    }

    protected CodeSetConversion.CTBConverter getWCharConverter() {
        if (wcharConverter == null)
            wcharConverter = parent.createWCharCTBConverter();
    
        return wcharConverter;
    }

    protected void dprint(String msg) {
        if (orb.cdrDebugFlag)
            ORBUtility.dprint(this, msg);
    }

    void alignOnBoundary(int octetBoundary) {
        alignAndReserve(octetBoundary, 0);
    }

    @CDR
    @CDRWrite
    public void start_value(String rep_id) {
        if (orb.cdrDebugFlag) 
            dputil.enter( "start_value", "rep_id", rep_id,
                "called at position", get_offset(), "position", bbwi.position() ) ;

        try {
            if (inBlock)
                end_block();
            
            // Write value_tag
            writeValueTag(true, true, null);
                                    
            // Write rep. id
            write_repositoryId(rep_id);
                                    
            // Write Value chunk
            end_flag--;
            chunkedValueNestingLevel--;

            // Make sure to chunk the custom data
            start_block();
        } finally {
            if (orb.cdrDebugFlag)
                dputil.exit() ;
        }
    }

    @CDR
    @CDRWrite
    public void end_value() {
        if (orb.cdrDebugFlag) {
            dputil.enter("end_value", "called as position", get_offset(),
                "position", bbwi.position() ) ;
	}

        try {
            end_block();

            writeEndTag(true);

            // Check to see if we need to start another block for a
            // possible outer value.  Since we're in the stream
            // format 2 custom type contained by another custom
            // type, mustChunk should always be true.
            //
            // Here's why we need to open a continuation chunk:
            //
            // We need to enclose the default data of the
            // next subclass down in chunks.  There won't be
            // an end tag separating the superclass optional
            // data and the subclass's default data.

            if (orb.cdrDebugFlag) {
                dprint("mustChunk is " + mustChunk);
            }

            if (mustChunk) {
                start_block();
            }
        } finally {
            if (orb.cdrDebugFlag) 
                dputil.exit() ;
        }
    }

    public void close() throws IOException
    {
        // tell BufferManagerWrite to release any ByteBuffers
        getBufferManager().close();

        // It's possible bbwi.byteBuffer is shared between
        // this OutputStream and an InputStream. Thus, we check
        // if the Input/Output streams are using the same ByteBuffer.
        // If they sharing the same ByteBuffer we need to ensure only
        // one of those ByteBuffers are released to the ByteBufferPool.

        if (getByteBufferWithInfo() != null && getByteBuffer() != null)
        {
            MessageMediator messageMediator = parent.getMessageMediator();
            if (messageMediator != null)
            {
                CDRInputObject inputObj = 
                               (CDRInputObject)messageMediator.getInputObject();
                if (inputObj != null)
                {
		    // shared byteBuffers?
		    if (bbwi.getByteBuffer() == inputObj.getByteBuffer())
		    {
                        // Set InputStream's ByteBuffer and bbwi to null
                        // so its ByteBuffer cannot be released to the pool
                        inputObj.setByteBuffer(null);
                        inputObj.setByteBufferWithInfo(null);
		    }
                }
            }

            // release this stream's ByteBuffer to the pool
            ByteBufferPool byteBufferPool = orb.getByteBufferPool();
            if (orb.cdrDebugFlag)
            {
                // print address of ByteBuffer being released
                int bbAddress = System.identityHashCode(bbwi.getByteBuffer());
                StringBuffer sb = new StringBuffer(80);
                sb.append(".close - releasing ByteBuffer id (");
                sb.append(bbAddress).append(") to ByteBufferPool.");
                String msg = sb.toString();
                dprint(msg);
             }
             byteBufferPool.releaseByteBuffer(getByteBuffer());
             bbwi.setByteBuffer(null);
             bbwi = null;
        }
    }
}
