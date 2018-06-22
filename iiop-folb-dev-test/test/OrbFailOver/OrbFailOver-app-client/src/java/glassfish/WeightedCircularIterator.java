/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package glassfish;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** An iterator that iterates over the elements of a list.
 * Each element will be returned the number of times of its weight.
 * hasNext() is true until each element has been returned at many
 * times as its weight.
 *
 * @author ken_admin
 */
public class WeightedCircularIterator<T> implements Iterator<T> {
    private final List<T> elements = new ArrayList<T>() ;
    private final Map<T,Integer> weights = new HashMap<T,Integer>() ;
    private final Map<T,Integer> counts = new HashMap<T,Integer>() ;
    private int current = 0 ;

    public void add( T elem, int weight ) {
        elements.add( elem ) ;
        weights.put( elem, weight ) ;
        counts.put( elem, 0 ) ;
    }

    @Override
    public boolean hasNext() {
        return !elements.isEmpty() ;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException(
                "Collection is empty, so no next element") ;
        }

        if (current > (elements.size()-1)) {
            current = 0 ;
        }

        T result = elements.get( current ) ;

        int count = counts.get( result ) ;
        count++ ;
        counts.put( result, count ) ;
        if (count == weights.get( result )) {
            elements.remove( current ) ;
        } else {
            current++ ;
        }

        return result ;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main( String[] args ) {
        WeightedCircularIterator<String> iter =
            new WeightedCircularIterator<String>() ;

        iter.add( "A", 5 ) ;
        iter.add( "B", 3 ) ;
        iter.add( "C", 1 ) ;
        iter.add( "D", 4 ) ;

        String[] strs = { "A", "B", "C", "D", "A", "B", "D", "A", "B", "D",
            "A", "D", "A" } ;

        for (String str : strs) {
            if (!iter.hasNext()) {
                throw new RuntimeException( "Unexpected failure of hasNext") ;
            }

            String next = iter.next() ;
            System.out.println( next ) ;
            if (!str.equals( next )) {
                throw new RuntimeException(
                    "Expected " + str + ", got " + next ) ;
            }
        }

        if (iter.hasNext()) {
            throw new RuntimeException(
                "hasNext true after all elements exhausted" ) ;
        }
    }
}
