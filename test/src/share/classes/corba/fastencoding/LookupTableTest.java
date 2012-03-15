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

import java.util.Random ;
import java.util.Map ;
import java.util.HashMap ;

import org.testng.annotations.Test ;

import com.sun.corba.ee.impl.encoding.fast.LookupTable ;
import com.sun.corba.ee.impl.encoding.fast.LookupTableSimpleConcurrentImpl ;
import com.sun.corba.ee.impl.encoding.fast.LookupTableConcurrentImpl ;
import org.glassfish.pfl.basic.contain.Holder;
import org.glassfish.pfl.basic.func.UnaryFunction;

public class LookupTableTest {
    private static final int NUM_THREADS = 20 ;
    private static final int MAX_REPS = 20 ;
    private static final int MAX_DELAY = 20 ;
    private static final Object[][] DATA = new Object[][] {
        { "red", 23 },
        { "blue", 4 },
        { "green", 9 },
        { "yellow", 2 },
        { "black", 17 },
        { "orange", 83 },
        { "indigo", 29 },
        { "violet", 24 },
        { "purple", 4 },
        { "cerulean", 16 },
        { "sienna", 23 },
        { "amber", 31 },
        { "white", 35 },
        { "pink ", 14 },
        { "lavender", 21 },
        { "biege", 41 },
        { "eggshell", 7 },
        { "cerise", 11 },
        { "grey", 42 },
        { "brown", 91 },
        { "tan", 16 },
        { "chartreuse", 14 },
        { "azure", 27 },
        { "aquamarine", 33 },
        { "bisque", 82 },
        { "coral", 45 },
        { "chocolate", 71 },
        { "cyan", 7 },
        { "magenta", 19 },
        { "turquoise", 14 },
        { "fuchsia", 55 },
        { "salmon", 32 },
        { "lime", 29 },
        { "linen", 19 },
        { "maroon", 20 },
        { "navy", 34 },
        { "olive", 37 },
        { "plum", 62 },
        { "silver", 14 },
        { "gold", 8 },
        { "teal", 76 }
    } ;

    private static final UnaryFunction<String,String> FACTORY = new UnaryFunction<String,String>() {
        public String evaluate( String arg ) {
            for (int ctr=0; ctr<DATA.length; ctr++ ) {
                Object[] elem = DATA[ctr] ;
                if (((String)elem[0]).equals( arg )) {
                    int delay = (Integer)(elem[1]) ;
                    try {
                        Thread.sleep( delay ) ;
                    } catch (InterruptedException exc) {
                        // ignore
                    }

                    return arg ;
                }
            }

            return null ;
        }
    } ;

    // For each key, remember which thread(s) (and how often) were told
    // that theirs was the first lookup call on the key.
    public static class FirstTracker {
        private Map<String,Map<Thread,Integer>> fmap = new HashMap<String,Map<Thread,Integer>>() ;

        public void FirstTracker() {
        }

        public void registerFirst( String key, Thread thread ) {
            Map<Thread,Integer> imap = fmap.get( key ) ;
            if (imap == null) {
                imap = new HashMap<Thread,Integer>() ;
                fmap.put( key, imap ) ;
            }

            Integer counter = imap.get( thread ) ;
            if (counter == null) {
                counter = 0 ;
            } else {
                counter = counter.intValue() + 1 ;
            }

            imap.put( thread, counter ) ;
        }

        public boolean check() {
            boolean error = false ;
            for (String key : fmap.keySet()) {
                Map<Thread,Integer> map = fmap.get( key ) ;
                if (map.size() != 1) {
                    error = true ;
                    System.out.println( "Error: more than one thread got firstTime for key " + key ) ;
                    for (Map.Entry<Thread,Integer> entry : map.entrySet() ) {
                    }
                }
            }

            return error ;
        }
    }

    private static Random rand = new Random() ;

    public static class TestThread extends Thread {
        private int errCount = 0 ;
        private FirstTracker ft ;
        private LookupTable<String,String> table ;

        public TestThread( FirstTracker ft, LookupTable<String,String> table ) {
            this.ft = ft ;
            this.table = table ;
        }

        @Override
        public void run() {
            for (int ctr=0; ctr<MAX_REPS; ctr++) {
                int delayTime = rand.nextInt( MAX_DELAY ) ;
                try {
                    Thread.sleep( delayTime ) ;
                } catch (InterruptedException exc) {
                    // ignore
                }

                int index = rand.nextInt( DATA.length ) ;
                Object[] elem = DATA[index] ;
                String key = (String)elem[0] ;

                Holder<Boolean> firstTime = new Holder<Boolean>() ;
                String result = table.lookup( firstTime, key ) ;

                if (!result.equals( key ))
                    errCount++ ;
            }
        }
    }

    // Testing the lookup table:
    // - Have several keys that take varying lengths of time to compute
    // - Have several threads lookup different keys at various times
    // - Randomize the test
    //
    // Test thread design:
    // for some count:
    //      delay random( 1-20 ) msec
    //      lookup random key (1-100)
    //
    // create some number of threads

    @Test
    public void testSimple() {
        LookupTable<String,String> table = new LookupTableSimpleConcurrentImpl( FACTORY, String.class ) ;
        runTest( table ) ;
    }

    @Test
    public void testConcurrent() {
        LookupTable<String,String> table = new LookupTableConcurrentImpl( FACTORY, String.class ) ;
        runTest( table ) ;
    }

    private void runTest( LookupTable<String,String> table ) {
        FirstTracker ft = new FirstTracker() ;
        Thread[] threads = new Thread[NUM_THREADS] ;
        for (int ctr=0; ctr<NUM_THREADS; ctr++) {
            threads[ctr] = new TestThread( ft, table ) ;
            threads[ctr].start() ;
        }

        for (int ctr=0; ctr<NUM_THREADS; ctr++) {
            try {
                threads[ctr].join() ;
            } catch (InterruptedException exc) {
                // NO-OP
            }
        }
    }
}
