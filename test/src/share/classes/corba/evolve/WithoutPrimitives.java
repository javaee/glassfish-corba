
package corba.evolve;

import java.io.Serializable;
import mymath.BigDecimal;

/**
 *
 * @author AlexisMP
 */
public class WithoutPrimitives implements Serializable {
    public String string1;
    public BigDecimal biggy;

    public WithoutPrimitives (String s, BigDecimal bi) {
        string1 = s;
        biggy = bi;
    }

    public WithoutPrimitives() {
        this("not initialized", new BigDecimal("-1.23E-12"));
    }

    public boolean equals( Object obj ) {
        if (obj == this)
            return true ;

        if (!(obj instanceof WithoutPrimitives))
            return false ;

        WithoutPrimitives other = (WithoutPrimitives)obj ;

        return string1.equals( other.string1 ) &&
            biggy.equals( other.biggy ) ;
    }

    public int hashCode() {
        return string1.hashCode() ^ biggy.hashCode() ;
    }

    private void readObject( java.io.ObjectInputStream s ) 
        throws java.io.IOException, ClassNotFoundException {

        s.defaultReadObject() ;
    }

    private void writeObject( java.io.ObjectOutputStream s ) 
        throws java.io.IOException {

        s.defaultWriteObject() ;
    }
}
