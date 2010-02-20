package corba.tf;

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitor;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Hand-written example of trace enhancer modifications to an annotated trace-enabled class.
 *
 * @author ken
 */
@A @B @C
public class TestClassImpl_tf implements TestClass {
    // Set-up for tracing facility (generated)
    private static final SynchronizedHolder<MethodMonitor> __mmA =
        new SynchronizedHolder<MethodMonitor>() ;
    private static final SynchronizedHolder<MethodMonitor> __mmB =
        new SynchronizedHolder<MethodMonitor>() ;
    private static final SynchronizedHolder<MethodMonitor> __mmC =
        new SynchronizedHolder<MethodMonitor>() ;

    static {
        Class<?> thisClass = TestClassImpl_tf.class ;

        final List<String> __mnames = new ArrayList<String>() ;
        __mnames.add( "increment" ) ;
        __mnames.add( "decrement" ) ;
        __mnames.add( "if0" ) ;
        __mnames.add( "if1" ) ;
        __mnames.add( "add" ) ;
        __mnames.add( "mult" ) ;
        __mnames.add( "bigAddValue" ) ;
        __mnames.add( "bigMultValue" ) ;

        final Map<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>> __classToMMH =
            new HashMap<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>>() ;

        __classToMMH.put( A.class, __mmA ) ;
        __classToMMH.put( B.class, __mmB ) ;
        __classToMMH.put( C.class, __mmC ) ;

        MethodMonitorRegistry.registerClass( thisClass, __mnames, __classToMMH );
    }
    // end of set-up

    @A
    long increment( long x ) {
        long __result = 0 ;
        Object __ident = null ;
        final MethodMonitor __mm = __mmA.content() ;
        final boolean enabled = __mm != null ;

        if (enabled) {
            __ident = 0 ;
            __mm.enter( __ident, x ) ;
        }

        try {
            __result = x + 1 ;
	} catch (RuntimeException exc) { // Should be Throwable
	    if (enabled) {
		__mm.exception( __ident, exc ) ; 
	    }
	    throw exc ;
        } finally {
            if (enabled) {
                __mm.exit( __ident, __result ) ;
            }
        }

        return __result ;
    }

    @A
    long decrement( long x ) {
        long __result = 0 ;
        Object __ident = null ;
        final MethodMonitor __mm = __mmA.content() ;
        final boolean enabled = __mm != null ;

        if (enabled) {
            __ident = 1 ;
            __mm.enter( __ident, x ) ;
        }

        try {
            __result = x - 1 ;
	} catch (RuntimeException exc) {
	    if (enabled) {
		__mm.exception( __ident, exc ) ;
	    }
	    throw exc ;
        } finally {
            if (enabled) {
                __mm.exit( __ident, __result ) ;
            }
        }

        return __result ;
    }

    @A
    boolean is0( long x ) {
        boolean __result = false ;
        Object __ident = null ;
        final MethodMonitor __mm = __mmA.content() ;
        final boolean enabled = __mm != null ;

        if (enabled) {
            __ident = 2 ;
            __mm.enter( __ident, x ) ;
        }

        try {
            __result = x==0 ;
	} catch (RuntimeException exc) {
	    if (enabled) {
		__mm.exception( __ident, exc ) ;
	    }
	    throw exc ;
        } finally {
            if (enabled) {
                __mm.exit( __ident, __result ) ;
            }
        }

        return __result ;
    }

    @A
    boolean is1( long x ) {
        boolean __result = false ;
        Object __ident = null ;
        final MethodMonitor __mm = __mmA.content() ;
        final boolean enabled = __mm != null ;

        if (enabled) {
            __ident = 3 ;
            __mm.enter( __ident, x ) ;
        }

        try {
            __result = x==1 ;
	} catch (RuntimeException exc) {
	    if (enabled) {
		__mm.exception( __ident, exc ) ;
	    }
	    throw exc ;
        } finally {
            if (enabled) {
                __mm.exit( __ident, __result ) ;
            }
        }

        return __result ;
    }

    @InfoMethod
    private void bigAddValue( String msg, long value, 
        MethodMonitor __mm, Object __callerId ) {
        if (__mm != null) {
            Object[] args = new Object[2] ;
            args[0] = msg ;
            args[1] = value ;
            __mm.info( args, __callerId, 6 ) ;
        }
    }

    @InfoMethod
    private void bigMultValue( String msg, long value,
        MethodMonitor __mm, Object __callerId ) {
        if (__mm != null) {
            Object[] args = new Object[2] ;
            args[0] = msg ;
            args[1] = value ;
            __mm.info( args, __callerId, 7 ) ;
        }
    }

    @B
    public long add( long a, long b ) {
        long __result = 0 ;  // just for codegen check purposes: enhanced code picks up
                             // return value directly from stack.
        Object __ident = null ;
        final MethodMonitor __mm = __mmB.content() ;
        final boolean enabled = __mm != null ;

        if (enabled) {
            __ident = 4 ;
            __mm.enter( __ident, a, b ) ;
        }

        try {
            if ((a<0) || (b<0)) {
                RuntimeException exc = new RuntimeException( "Negative not supported" ) ;
                if (enabled) {
                    __mm.exception( __ident, exc ) ;
                }
                throw exc ;
            }

            if (is0(b)) {
                return __result = a ;
            } else {
                if (b > 100) {
                    bigAddValue( "Large argument for add", b, 
                        __mm, __ident ) ;
                }

                return __result = add( increment(a), decrement(b) ) ;
            }
        } finally {
            if (enabled) {
                __mm.exit( __ident, __result ) ;
            }
        }
    }

    @C
    public long mult( long a, long b ) {
        long __result = 0 ;
        Object __ident = null ;
        final MethodMonitor __mm = __mmC.content() ;
        final boolean enabled = __mm != null ;

        if (enabled) {
            __ident = 5 ;
            __mm.enter( __ident, a, b ) ;
        }

        try {
            if ((a<0) || (b<0)) {
                RuntimeException exc = new RuntimeException( "Negative not supported" ) ;
                if (enabled) {
                    __mm.exception( __ident, exc ) ;
                }
                throw exc ;
            }

            if (is0(b)) {
                __result = 0 ;
            } else if (is1(b)) {
                __result = a ;
            } else {
                if (b > 10) {
                    bigMultValue( "Large argument for mult", b,
                        __mm, __ident ) ;
                }

                __result = add( a, mult( a, decrement(b) )) ;
            }
        } finally {
            if (enabled) {
                __mm.exit( __ident, __result ) ;
            }
        }

        return __result ;
    }
}
