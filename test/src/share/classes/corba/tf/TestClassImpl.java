package corba.tf;

import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;

/**
 *
 * @author ken
 */
@A @B @C
public class TestClassImpl implements TestClass {
    static {
        System.out.println( "Start of <clinit>" ) ;
    }

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
    private void bigAddValue( String msg, long value ) { }

    @InfoMethod
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

            long decRes = decrement(b) ;
            long multRes = mult( a, decRes ) ;
            return add( a, multRes ) ;
        }
    }
}
