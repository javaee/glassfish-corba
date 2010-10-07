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

package corba.fastencoding  ;

import java.util.List ;
import java.util.ArrayList ;

import java.io.Closeable ;
import java.nio.ByteBuffer ;

import org.testng.annotations.Test ;
import org.testng.Assert ;

import com.sun.corba.se.impl.encoding.fast.bytebuffer.Allocator ;
import com.sun.corba.se.impl.encoding.fast.bytebuffer.AllocatorFactory ;
import com.sun.corba.se.impl.encoding.fast.bytebuffer.BufferWrapper ;
import com.sun.corba.se.impl.encoding.fast.bytebuffer.Reader ;
import com.sun.corba.se.impl.encoding.fast.bytebuffer.Writer ;
import com.sun.corba.se.impl.encoding.fast.bytebuffer.StreamFactory ;

import com.sun.corba.se.impl.encoding.fast.EmergeCodeFactory ;
import com.sun.corba.se.impl.encoding.fast.VarOctetUtility ;

public class ByteBufferTest {
    // Basic idea:
    // 1. Create a Writer and write everything to it.
    // 2. Create a Reader out of the buffers generated by the writer.
    // 3. Check that the data that was written is what's received.
    @Test 
    public void testBufferWrapper() {
        byte[] data1 = new byte[100] ;
        byte[] data2 = new byte[500] ;
        byte[] data3 = new byte[data1.length + data2.length] ;

        for (int ctr=0; ctr<data1.length; ctr++)
            data1[ctr] = (byte)(ctr & 255) ;
        for (int ctr=0; ctr<data2.length; ctr++)
            data2[ctr] = (byte)((ctr + data1.length) & 255) ;
        for (int ctr=0; ctr<data3.length; ctr++)
            data3[ctr] = (byte)(ctr & 255) ;
        
        Allocator alloc1 = AllocatorFactory.makeAllocator( data1.length, 10000,
            Allocator.BufferType.HEAP ) ;
        BufferWrapper eb1 = alloc1.allocate( data2.length ) ;
        eb1.buffer().put( data2 ) ;
        eb1.reset() ;
        byte[] result1 = new byte[ data2.length ] ;
        eb1.buffer().get( result1 ) ;
        eb1.reset() ;
        // System.out.println( "eb1.buffer().position(()         = " 
        // + eb1.buffer().position() ) ;
        // System.out.println( "eb1.buffer().limit()             = " 
        // + eb1.buffer().limit() ) ;
        // System.out.println( "eb1.buffer().capacity()          = " 
        // + eb1.buffer().capacity() ) ;

        Assert.assertEquals( result1, data2 ) ;
        
        ByteBuffer bb2 = ByteBuffer.allocate( data1.length ) ;
        bb2.put( data1 ) ;
        bb2.position(0) ;
        // System.out.println( "bb2.position(()         = " + bb2.position() ) ;
        // System.out.println( "bb2.limit()             = " + bb2.limit() ) ;
        // System.out.println( "bb2.capacity()          = " + bb2.capacity() ) ;

        eb1.reset() ;
        ByteBuffer buff2 = eb1.prepend( bb2 ) ;
        Assert.assertEquals( buff2, eb1.buffer() ) ;

        byte[] result2 = new byte[ data1.length + data2.length ] ;
        // System.out.println( "buff2.position(()         = " 
        // + buff2.position() ) ;
        // System.out.println( "buff2.limit()             = " 
        // + buff2.limit() ) ;
        // System.out.println( "buff2.capacity()          = " 
        // + buff2.capacity() ) ;

        buff2.get( result2 ) ;

        Assert.assertEquals( result2, data3 ) ;
    }

    /** Set up a pipe between a writer and a reader for testing.
     * Anything written to the writer() will be available in the reader() 
     * after either writer().flush() or writer().close() is called.
     */
    static class Pipe implements Closeable {
        private Allocator allocator ;
        private int bufferSize ;
        private Reader reader ;
        private Writer writer ;

        private Writer.BufferHandler handler = new Writer.BufferHandler() {
            public BufferWrapper overflow( BufferWrapper current ) {
                BufferWrapper eb = allocator.allocate( bufferSize ) ;
                if (current != null)
                    reader.receiveData( current ) ;
                return eb ;
            }

            public void close( BufferWrapper current ) {
                reader.receiveData( current ) ;
            }
        } ;

        public Pipe( int bufferSize ) {
            this( bufferSize, 30000 ) ;
        }

        public Pipe( int bufferSize, long timeout ) {
            allocator = AllocatorFactory.makeAllocator( 8, 
                bufferSize, Allocator.BufferType.HEAP ) ;
            reader = StreamFactory.makeReader( timeout ) ;
            writer = StreamFactory.makeWriter( handler ) ;
            this.bufferSize = bufferSize ;
        }

        public Reader reader() {
            return reader ;
        }

        public Writer writer() {
            return writer ;
        }

        public void flush() {
            writer.flush() ;
        }

        public void close() {
            try {
                reader.close() ;
                writer.close() ;
            } catch (Exception exc) {
                // ignore
            }
        }
    }

    private static final boolean DEBUG_CHECKER_WRITE = false ;
    private static final boolean DEBUG_CHECKER_READ = false ;

    interface Checker {
        void write( Writer writer ) ;

        void readAndCheck( Reader reader ) ;

        long operations() ;

        long byteSize() ;
    }

    abstract static class CheckerBase implements Checker {
        private String name ;

        public CheckerBase() {
            String className = this.getClass().getName() ;
            int index = className.indexOf( '$' ) ;
            name = className.substring( index + 1 ) ;
        }

        public abstract String value() ;

        public String toString() {
            return name + "[" + value() + "]" ;
        }

        protected void werrMsg( Object obj, Throwable thr ) {
            System.out.println( "###Checker(" + toString() + ").write: Caught " 
                + thr + " at parameter " + obj ) ;
        }

        protected void rerrMsg( Object obj, Throwable thr ) {
            System.out.println( "###Checker(" + toString() 
                + ").readAndCheck: Caught " 
                + thr + " at parameter " + obj ) ;
        }

        public void wmsg() {
            if (DEBUG_CHECKER_WRITE) 
                System.out.println( "Write:" + toString() ) ;
        }

        public void rmsg() {
            if (DEBUG_CHECKER_READ) 
                System.out.println( "ReadAndCheck:" + toString() ) ;
        }

        public long operations() {
            return 1 ;
        }
    }

    static class CompositeChecker extends CheckerBase {
        private List<Checker> checkers ;

        public String value() {
            StringBuilder sb = new StringBuilder() ;
            boolean first = true ;
            for (Checker ch : checkers) {
                if (first) {
                    first = false ;
                } else {
                    sb.append( ' ' ) ;
                }

                sb.append( ch.toString() ) ;
            }

            return sb.toString() ;
        }

        public CompositeChecker( Checker... args ) {
            checkers = new ArrayList<Checker>() ;
            for (Checker ch : args)
                checkers.add( ch ) ;
        }

        public void add( Checker ch ) {
            checkers.add( ch ) ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            Checker ch = null ;

            try {
                for (Checker checker : checkers) {
                    ch = checker ;
                    ch.write( writer ) ;
                }
            } catch (Error err) {
                werrMsg( ch, err ) ;
                throw err ;
            } catch (RuntimeException exc) {
                werrMsg( ch, exc ) ;
                throw exc ;
            }
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            Checker ch = null ;

            try {
                for (Checker checker : checkers) {
                    ch = checker ;
                    ch.readAndCheck( reader ) ;
                }
            } catch (Error err) {
                rerrMsg( ch, err ) ;
                throw err ;
            } catch (RuntimeException exc) {
                rerrMsg( ch, exc ) ;
                throw exc ;
            }
        }

        public long operations() {
            long sum = 0 ;
            for (Checker ch : checkers) 
                sum += ch.operations() ;

            return sum ;
        }

        public long byteSize() {
            long sum = 0 ;
            for (Checker ch : checkers) 
                sum += ch.byteSize() ;

            return sum ;
        }
    }

    static class RepeatedChecker extends CheckerBase {
        private int count ;
        private Checker checker ;

        public String value() {
            return count + " " + checker.toString() ;
        }

        public RepeatedChecker( int count, Checker checker ) {
            this.count = count ;
            this.checker = checker ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            int ctr = 0 ;

            try {
                for (ctr=0; ctr<count; ctr++)
                    checker.write( writer ) ;
            } catch (Error err) {
                werrMsg( ctr, err ) ;
                throw err ;
            } catch (RuntimeException exc) {
                werrMsg( ctr, exc ) ;
                throw exc ;
            }
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            int ctr = 0 ;

            try {
                for (ctr=0; ctr<count; ctr++)
                    checker.readAndCheck( reader ) ;
            } catch (Error err) {
                rerrMsg( ctr, err ) ;
                throw err ;
            } catch (RuntimeException exc) {
                rerrMsg( ctr, exc ) ;
                throw exc ;
            }
        }

        public long operations() {
            return count * checker.operations() ;
        }

        public long byteSize() {
            return count * checker.byteSize() ;
        }
    }

    static class VarOctetChecker extends CheckerBase {
        private long data ;

        public String value() {
            return "" + data ;
        }

        public VarOctetChecker( long arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            VarOctetUtility.put( writer, data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            long value = VarOctetUtility.get( reader ) ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return EmergeCodeFactory.varOctetSize( data ) ;
        }
    }

    static class VarOctetRangeChecker extends CheckerBase {
        private long min ;
        private long max ;

        public String value() {
            return "" + min + ":" + max ;
        }

        public VarOctetRangeChecker( long min, long max ) {
            this.min = min ;
            this.max = max ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            for (long ctr=min; ctr<max; ctr++)
                VarOctetUtility.put( writer, ctr ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            for (long ctr=min; ctr<max; ctr++) {
                long value = VarOctetUtility.get( reader ) ;
                Assert.assertEquals( value, ctr ) ;
            }
        }

        public long operations() {
            return max - min + 1 ;
        }

        public long byteSize() {
            long sum = 0 ;
            for (long ctr=min; ctr<max; ctr++) {
                sum += EmergeCodeFactory.varOctetSize( ctr ) ;
            }

            return sum ;
        }
    }

    static class ByteChecker extends CheckerBase {
        private byte data ;

        public String value() {
            return "" + data ;
        }

        public ByteChecker( byte arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putByte( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            byte value = reader.getByte() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 1 ;
        }
    }

    static class BooleanChecker extends CheckerBase {
        private boolean data ;

        public String value() {
            return "" + data ;
        }

        public BooleanChecker( boolean arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putBoolean( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            boolean value = reader.getBoolean() ;
            Assert.assertTrue( value == data ) ;
        }

        public long byteSize() {
            return 1 ;
        }
    }


    static class CharChecker extends CheckerBase {
        private char data ;

        public String value() {
            return "" + data ;
        }

        public CharChecker( char arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putChar( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            char value = reader.getChar() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 2 ;
        }
    }


    static class ShortChecker extends CheckerBase {
        private short data ;

        public String value() {
            return "" + data ;
        }

        public ShortChecker( short arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putShort( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            short value = reader.getShort() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 2 ;
        }
    }


    static class IntChecker extends CheckerBase {
        private int data ;

        public String value() {
            return "" + data ;
        }

        public IntChecker( int arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putInt( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            int value = reader.getInt() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 4 ;
        }
    }


    static class LongChecker extends CheckerBase {
        private long data ;

        public String value() {
            return "" + data ;
        }

        public LongChecker( long arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putLong( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            long value = reader.getLong() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 8 ;
        }
    }


    static class FloatChecker extends CheckerBase {
        private float data ;

        public String value() {
            return "" + data ;
        }

        public FloatChecker( float arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putFloat( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            float value = reader.getFloat() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 4 ;
        }
    }

    static class DoubleChecker extends CheckerBase {
        private double data ;

        public String value() {
            return "" + data ;
        }

        public DoubleChecker( double arg ) {
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            writer.putDouble( data ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            double value = reader.getDouble() ;
            Assert.assertEquals( value, data ) ;
        }

        public long byteSize() {
            return 8 ;
        }
    }

    static class BooleanArrayChecker extends CheckerBase {
        private boolean data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public BooleanArrayChecker( int size, boolean arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            boolean[] value = new boolean[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putBooleanArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            boolean[] value = new boolean[size] ;
            reader.getBooleanArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertTrue( value[ctr] == data ) ;
        }

        public long byteSize() {
            return size ;
        }
    }

    static class ByteArrayChecker extends CheckerBase {
        private byte data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public ByteArrayChecker( int size, byte arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            byte[] value = new byte[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putByteArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            byte[] value = new byte[size] ;
            reader.getByteArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertEquals( value[ctr], data ) ;
        }

        public long byteSize() {
            return size ;
        }
    }

    static class CharArrayChecker extends CheckerBase {
        private char data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public CharArrayChecker( int size, char arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            char[] value = new char[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putCharArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            char[] value = new char[size] ;
            reader.getCharArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertEquals( value[ctr], data ) ;
        }

        public long byteSize() {
            return 2 * size ;
        }
    }

    static class ShortArrayChecker extends CheckerBase {
        private short data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public ShortArrayChecker( int size, short arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            short[] value = new short[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putShortArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            short[] value = new short[size] ;
            reader.getShortArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertEquals( value[ctr], data ) ;
        }

        public long byteSize() {
            return 2 * size ;
        }
    }

    static class IntArrayChecker extends CheckerBase {
        private int data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public IntArrayChecker( int size, int arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            int[] value = new int[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putIntArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            int[] value = new int[size] ;
            reader.getIntArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertEquals( value[ctr], data ) ;
        }

        public long byteSize() {
            return 4 * size ;
        }
    }

    static class LongArrayChecker extends CheckerBase {
        private long data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public LongArrayChecker( int size, long arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            long[] value = new long[size] ;

            try {
                for (int ctr=0; ctr<size; ctr++)
                    value[ctr] = data ;
                writer.putLongArray( value ) ;
            } catch (Error err) {
                werrMsg( size, err ) ;
                throw err ;
            } catch (RuntimeException exc) {
                werrMsg( size, exc ) ;
                throw exc ;
            }
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            long[] value = new long[size] ;
            reader.getLongArray( value ) ;

            try {
                for (int ctr=0; ctr<size; ctr++)
                    Assert.assertEquals( value[ctr], data ) ;
            } catch (Error err) {
                rerrMsg( size, err ) ;
                throw err ;
            } catch (RuntimeException exc) {
                rerrMsg( size, exc ) ;
                throw exc ;
            }
        }

        public long byteSize() {
            return 8 * size ;
        }
    }

    static class FloatArrayChecker extends CheckerBase {
        private float data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public FloatArrayChecker( int size, float arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            float[] value = new float[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putFloatArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            float[] value = new float[size] ;
            reader.getFloatArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertEquals( value[ctr], data ) ;
        }

        public long byteSize() {
            return 4 * size ;
        }
    }

    static class DoubleArrayChecker extends CheckerBase {
        private double data ;
        private int size ;

        public String value() {
            return size + ":" + data ;
        }

        public DoubleArrayChecker( int size, double arg ) {
            this.size = size ;
            data = arg ;
        }

        public void write( Writer writer ) {
            wmsg() ;
            double[] value = new double[size] ;
            for (int ctr=0; ctr<size; ctr++)
                value[ctr] = data ;
            writer.putDoubleArray( value ) ;
        }

        public void readAndCheck( Reader reader ) {
            rmsg() ;
            double[] value = new double[size] ;
            reader.getDoubleArray( value ) ;
            for (int ctr=0; ctr<size; ctr++)
                Assert.assertEquals( value[ctr], data ) ;
        }

        public long byteSize() {
            return 8 * size ;
        }
    }

    // How best to construct checkers for running tests?
    //
    // use a series of private methods, each of which returns a Checker
    // comp( list )
    // rep( count, checker )
    // boolean      z
    // byte         b
    // char         c
    // short        s   
    // int          i
    // long         l
    // float        f
    // double       d
    // var octet    v
    // a for array

    private static Checker comp( Checker... arg ) {
        return new CompositeChecker( arg ) ;
    }

    private static Checker rep( int count, Checker ch ) {
        return new RepeatedChecker( count, ch ) ;
    }

    private static Checker v( long arg ) {
        return new VarOctetChecker( arg ) ;
    }

    private static Checker vr( long min, long max ) {
        return new VarOctetRangeChecker( min, max ) ;
    }

    private static Checker z( boolean arg ) {
        return new BooleanChecker( arg ) ;
    }

    private static Checker b( byte arg ) {
        return new ByteChecker( arg ) ;
    }

    private static Checker c( char arg ) {
        return new CharChecker( arg ) ;
    }

    private static Checker s( short arg ) {
        return new ShortChecker( arg ) ;
    }

    private static Checker i( int arg ) {
        return new IntChecker( arg ) ;
    }

    private static Checker l( long arg ) {
        return new LongChecker( arg ) ;
    }

    private static Checker f( float arg ) {
        return new FloatChecker( arg ) ;
    }

    private static Checker d( double arg ) {
        return new DoubleChecker( arg ) ;
    }

    private static Checker za( int size, boolean arg ) {
        return new BooleanArrayChecker( size, arg ) ;
    }

    private static Checker ba( int size, byte arg ) {
        return new ByteArrayChecker( size, arg ) ;
    }

    private static Checker ca( int size, char arg ) {
        return new CharArrayChecker( size, arg ) ;
    }

    private static Checker sa( int size, short arg ) {
        return new ShortArrayChecker( size, arg ) ;
    }

    private static Checker ia( int size, int arg ) {
        return new IntArrayChecker( size, arg ) ;
    }

    private static Checker la( int size, long arg ) {
        return new LongArrayChecker( size, arg ) ;
    }

    private static Checker fa( int size, float arg ) {
        return new FloatArrayChecker( size, arg ) ;
    }

    private static Checker da( int size, double arg ) {
        return new DoubleArrayChecker( size, arg ) ;
    }

    private void doTest( Checker ch ) {
        long start = 0 ;
        System.out.println( "Testing and timing " + ch 
            + "\n(operations:" + ch.operations() 
            + " byteSize:" + ch.byteSize() + ")" ) ;
        Pipe p = new Pipe( 25739 ) ;

        try {
            start = System.currentTimeMillis() ;
            ch.write( p.writer() ) ;
            p.flush() ;
            System.out.println( "\tWriting took " + (System.currentTimeMillis() - start) 
                + " milliseconds" ) ;

            start = System.currentTimeMillis() ;
            ch.readAndCheck( p.reader() ) ;
            System.out.println( "\tReading and checking took " + (System.currentTimeMillis() - start) 
                + " milliseconds" ) ;
        } finally {
            p.close() ;
        }
    }

    @Test
    public void testPrimitives() {
        Checker ch = rep( 1000, comp( z(true), b((byte)23), c('A'), s((short)2343), i(231231), l(-789789789789L), 
            f((float)123.23), d(2321.3232213) ) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testReaderWriterVarOctet() {
        Checker ch = rep( 5, comp( vr( 0L, 100000L ), vr( 99999L, 999999L ),
            vr( 123456789L, 123512345L ), vr( 123123456456L, 123123600600L ) ) ) ;

        doTest( ch ) ;
    }

    @Test
    public void testReaderWriterLong() {
        Checker ch = rep( 5, comp( rep(100001, l(23)), rep( 900001, l(32123)),
            rep(55557, l(7483848374L)), rep(144145, l(0)))) ;

        doTest( ch ) ;
    }

    @Test
    public void testTimeVarOctet() {
        Checker ch = rep( 10000000, v(23) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testTimeByte() {
        Checker ch = rep( 10000000, b((byte)23) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testTimeShort() {
        Checker ch = rep( 10000000, s((short)23) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testTimeInt() {
        Checker ch = rep( 10000000, i(23) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testTimeLong() {
        Checker ch = rep( 10000000, l(23) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testBooleanArrayTrue() {
        Checker ch = rep( 10000000, z(true) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testBooleanArrayFalse() {
        Checker ch = rep( 10000000, z(false) ) ;
        doTest( ch ) ;
    }

    @Test
    public void testMultipleArrays() {
        Checker ch = rep( 10, comp( 
            la( 37913, 1234567879123456789L ),
            ba( 5531, (byte)39 ),
            ca( 57932, 'A' ),
            sa( 123324, (short)27456 ),
            ia( 479, 356789 ),
            la( 44123, 49384892348923489L ),
            fa( 574321, (float)12.7 ),
            da( 234435, 45921.45891 )
        )) ;

        doTest( ch ) ;
    }
}
