/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package corba.framework;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set ;
import java.util.HashSet ;
import java.lang.reflect.Method ;
import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.argparser.Help ;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;
import com.sun.corba.se.spi.orbutil.argparser.Separator ;
import java.util.Arrays;
import java.util.Collection;

/** A VERY quick-and-dirty test framework.
 *
 * @author ken
 */
public class TestBase {
    private final List<Method> testMethods ;
    private final List<String> currentResults ;
    // private final List<String> currentNotes ;
    private final Arguments argvals ;
    private final Set<String> includes ;
    private final Set<String> excludes ;
    private final List<Method> preMethods ;
    private final List<Method> postMethods ;

    private String current ;
    private Set<String> pass = new HashSet<String>() ;
    private Set<String> fail = new HashSet<String>() ;
    private Set<String> skip = new HashSet<String>() ;
    private final Object testObject ;

    private interface Arguments {
        @DefaultValue( "false" )
        @Help( "Control debugging mode")
        boolean debug() ;

        @DefaultValue( "false" ) 
        @Help( "Displays the valid test case identifiers" ) 
        boolean cases() ;

        @DefaultValue( "" ) 
        @Help( "A list of test cases to include: includes everything if empty" ) 
        @Separator( "," )
        List<String> include() ;

        @DefaultValue( "" ) 
        @Help( "A list of test cases to excelude: include everything if empty" ) 
        @Separator( "," )
        List<String> exclude()  ;
    }

    private void execute( Collection<Method> methods )
        throws IllegalAccessException, IllegalArgumentException,
        InvocationTargetException {

        for (Method m : methods) {
            m.invoke( this ) ;
        }
    }

    public TestBase( String[] args ) {
        this( args, null ) ;
    }

    public TestBase(String[] args, Class<?> parserInterface) {
        this( args, parserInterface, null ) ;
    }

    public TestBase(String[] args, Class<?> parserInterface, Object testObject ) {
        testMethods = new ArrayList<Method>() ;
        preMethods = new ArrayList<Method>() ;
        postMethods = new ArrayList<Method>() ;

        this.testObject = (testObject == null)
            ? this
            : testObject ;

        final Class<?> cls = (testObject == null)
            ? this.getClass()
            : testObject.getClass() ;

        for (Method m : cls.getMethods()) {
            if (m.getDeclaringClass().equals( TestBase.class )
                && !this.getClass().equals( TestBase.class )) {
                // Skip test methods defined on this class for self test
                // unless we are actually running the self test.
                continue ;
            }

            TestCase anno = m.getAnnotation( TestCase.class ) ;
            if (anno != null) {
                if (m.getParameterTypes().length == 0) {
                    if (m.getReturnType().equals( void.class )) {
                        testMethods.add( m ) ;
                    } else {
                        msg( "Method " + m + " is annotated @Test, "
                            + "but has a non-void return type").nl() ;
                    }
                } else {
                    msg( "Method " + m + " is annotated @Test, "
                        + "but has parameters").nl() ;
                }
            }

            Pre pre = m.getAnnotation( Pre.class ) ;
            if (pre != null) {
                preMethods.add( m ) ;
            }

            Post post = m.getAnnotation( Post.class ) ;
            if (post != null) {
                postMethods.add( m ) ;
            }
        }


        Class<?>[] interfaces = (parserInterface == null)
            ? new Class<?>[]{ Arguments.class } 
            : new Class<?>[]{ Arguments.class, parserInterface } ;

        ArgParser parser = new ArgParser( Arrays.asList(interfaces)) ;
        argvals = (Arguments)parser.parse( args ) ;
        if (argvals.debug()) {
            msg( "Arguments are:\n" + argvals ).nl() ;
        }

        if (argvals.include().isEmpty()) {
            includes = new HashSet<String>() ;
            for (Method m : testMethods) {
                includes.add( getTestId( m ) ) ;
            }
        } else {
            List<String> incs = argvals.include() ;
            includes = new HashSet<String>( incs ) ;
        }

        excludes = new HashSet<String>( argvals.exclude() ) ;

        if (argvals.cases()) {
            msg( "Valid test case identifiers are:" ).nl() ;
            for (Method m : testMethods) {
                msg( "    " + getTestId( m ) ).nl() ;
            }
        }
        
        currentResults = new ArrayList<String>() ;
        // currentNotes = new ArrayList<String>() ;
    }

    public <T> T getArguments( Class<T> cls ) {
        return cls.cast( argvals ) ;
    }

    private TestBase msg( String str ) {
        System.out.print( str ) ;
        return this ;
    }

    private TestBase nl() {
        System.out.println() ;
        return this ;
    }

    private String getTestId( Method m ) {
        TestCase anno = m.getAnnotation( TestCase.class ) ;
        if (!anno.value().equals("")) {
            return anno.value() ;
        }

        String mname = m.getName() ;
        if (mname.startsWith( "test" )) {
            return mname.substring( 4 ) ;
        } else {
            return mname ;
        }
    }

    private void display( String title, List<String> strs ) {
        if (!strs.isEmpty()) {
            msg( title + ":" ).nl() ;
            for (String str : strs ) {
                msg( "\t" + str ).nl() ;
            }
        }
    }

    public int run() {
        for (Method m : testMethods) {
            currentResults.clear() ;
            // currentNotes.clear() ;

            current = getTestId( m ) ;
            if (includes.contains(current) && !excludes.contains(current)) {
                msg( "Test " + current + ": " ).nl() ;
                msg( "    Notes:" ).nl() ;
                try {
                    execute( preMethods ) ;
                    m.invoke( testObject ) ;
                } catch (Exception exc) {
                    fail( "Caught exception : " + exc )  ;
                    exc.printStackTrace();
                } finally {
                    try {
                        execute(postMethods);
                    } catch (Exception exc) {
                        fail( "Exception in post methods : " + exc ) ;
                        exc.printStackTrace();
                    }
                }

                if (currentResults.isEmpty()) {
                    pass.add( current ) ;
                    msg( "Test " + current + " PASSED." ).nl() ;
                } else {
                    fail.add( current )  ;
                    msg( "Test " + current + " FAILED." ).nl() ;
                }

                // display( "    Notes", currentNotes ) ;
                display( "    Results", currentResults ) ;
            } else {
                msg( "Test " + current + " SKIPPED" ).nl() ;
                skip.add( current ) ;
            }
        }

        msg( "-------------------------------------------------").nl() ;
        msg( "Results:" ).nl() ;
        msg( "-------------------------------------------------").nl() ;

        msg( "\tFAILED:").nl() ; displaySet( fail ) ;
        msg( "\tSKIPPED:").nl() ; displaySet( skip ) ;
        msg( "\tPASSED:").nl() ; displaySet( pass ) ;

        nl() ;
        msg( pass.size() + " test(s) passed; "
            + fail.size() + " test(s) failed; "
            + skip.size() + " test(s) skipped." ).nl() ;
        msg( "-------------------------------------------------").nl() ;

        return fail.size() ;
    }

    private void displaySet( Set<String> set ) {
        for (String str : set ) {
            msg( "\t\t" ).msg( str ).nl() ;
        }
    }

    public void fail( String failMessage ) {
        check( false, failMessage ) ;
    }

    public void check( boolean result, String failMessage ) {
        if (!result) {
            currentResults.add( failMessage ) ;
        }
    }

    public void note( String msg ) {
        // currentNotes.add( msg ) ;
        msg( "\t" + msg ).nl() ;
    }

    @TestCase
    public void testSimple() {}

    @TestCase
    public void testGood( ) {
        note( "this is a good test" ) ;
        note( "A second note") ;
    }

    @TestCase( "Bad" )
    public void badTest() {
        note( "this is a bad test" ) ;
        fail( "this test failed once" ) ;
        fail( "this test failed twice" ) ;
    }

    @TestCase
    public void exception() {
        throw new RuntimeException( "This test throws an exception") ;
    }

    @TestCase
    public boolean badReturnType() {
        return true ;
    }

    @TestCase
    public void hasParameters( String name ) {
    }

    public static void main( String[] args ) {
        TestBase base = new TestBase( args ) ;
        base.run() ;
    }
}
