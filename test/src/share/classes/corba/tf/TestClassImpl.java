package corba.tf;

import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;

/**
 *
 * @author ken
 */
@A @B @C
public class TestClassImpl implements TestClass {
    /* This is not needed here, if we instrument the files directly.
    // Set-up for tracing facility
    @A private static MethodMonitor mmA ;
    @B private static MethodMonitor mmB ;
    @C private static MethodMonitor mmC ;

    @MethodNames private static List<String> mnames ;
    // end of set-up
    */

    @A
    private long increment( long x ) {
        return x + 1 ;
    }

    @A
    private long decrement( long x ) {
        return x - 1 ;
    }

    @A
    private boolean is0( long x ) {
        return x==0 ;
    }

    @A
    private boolean is1( long x ) {
        return x==1 ;
    }

    @InfoMethod
    @B
    private void bigAddValue( String msg, long value ) { }

    @InfoMethod
    @C
    private void bigMultValue( String msg, long value ) { }

    @B
    public long add( long a, long b ) {
        if ((a<0) || (b<0)) {
            throw new RuntimeException( "Negative not supported" ) ;
        }

        if (is0(b)) {
            return a ;
        } else {
            if (b > 100) {
                bigAddValue( "Large argument for add", b ) ;
            }

            return add( increment(a), decrement(b) ) ;
        }
    }

    @C
    public long mult( long a, long b ) {
        if ((a<0) || (b<0)) {
            throw new RuntimeException( "Negative not supported" ) ;
        }

        if (is0(b)) {
            return 0 ;
        } else if (is1(b)) {
            return a ;
        } else {
            if (b > 10) {
                bigMultValue( "Large argument for mult", b ) ;
            }

            return add( a, mult( a, decrement(b) )) ;
        }
    }
}
