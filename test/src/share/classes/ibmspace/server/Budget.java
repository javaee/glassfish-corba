/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package ibmspace.server;

import java.util.Vector;
import java.lang.reflect.Array;
import ibmspace.common.BudgetSummary;
import java.io.Serializable;

public class Budget implements Investment, Serializable
{
    String fName;
    Vector fBudgetItems;




    public Budget (String name)
    {
        fName = name;
        fBudgetItems = new Vector ();
    }

    public String getName ()
    {
        return fName;
    }

    public void invest (long dollars)
    {
        balance ();

        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            Investment investment = item.getInvestment ();
            long investmentDollars = dollars * item.getPercentage() / 100;
            if ( investmentDollars > dollars ) {
                investmentDollars = dollars;
            }
            investment.invest (investmentDollars);
            dollars -= investmentDollars;
        }
    }

    public int totalAllocations ()
    {
        int total = 0;

        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt(i);
            total += item.getPercentage ();
        }

        return total;
    }

    public int numberOfBudgetItems ()
    {
        return fBudgetItems.size ();
    }

    public void balance ()
    {
        int difference = totalAllocations() - 100;
        int change = 0;

        if ( difference != 0 ) {
            if ( difference > 0 ) {
                change = -1;
            } else {
                change = 1;
            }

            int i = 0;
            while ( difference != 0 ) {
                BudgetItem item = (BudgetItem)fBudgetItems.elementAt(i);
                item.setPercentage (item.getPercentage()+change);
                difference += change;

                i++;
                if ( i == numberOfBudgetItems() ) i = 0;
            }
        }

    }

    public void addBudgetItem (BudgetItem item)
    {
        fBudgetItems.addElement (item);
    }

    public void removeBudgetItem (BudgetItem item)
    {
        fBudgetItems.removeElement (item);
    }

    public BudgetItem findBudgetItem (Investment investment)
    {
        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            if ( item.getInvestment() == investment ) {
                return item;
            }
        }
        return null;
    }

    public BudgetItem findBudgetItem (String name)
    {
        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            if ( item.getName().equals(name) ) {
                return item;
            }
        }
        return null;
    }

    public BudgetSummary createSummary ()
    {
        int numItems = fBudgetItems.size ();
        String[] names = new String [numItems];
        double[] percentages = new double [numItems];

        for (int i=0; i<numItems; i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt(i);
            names[i] = item.getName ();
            percentages[i] = ((double)item.getPercentage())/100.0;
        }

        return new BudgetSummary (numItems, names, percentages);
    }


    public void update (BudgetSummary summary)
    {
        String[] names = summary.getNames ();

        for (int i=0; i<Array.getLength(names); i++) {
            BudgetItem item = findBudgetItem (names[i]);
            if ( item != null ) {
                item.setPercentage ( (int)(summary.getPercentage(names[i])*100));
            }
        }

    }

    public String toString ()
    {
        String s = getName() + " (";
        for (int i=0; i<fBudgetItems.size(); i++) {
            BudgetItem item = (BudgetItem)fBudgetItems.elementAt (i);
            s += item.toString () + ",";
        }
        s += ")";
        return s;
    }

}
