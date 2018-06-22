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

//
// Created       : 2000 Nov 24 (Fri) 11:12:04 by Harold Carr.
// Last Modified : 2000 Nov 26 (Sun) 09:30:15 by Harold Carr.
//

package corba.hcks;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.glassfish.pfl.basic.contain.Pair;

public class ErrorAccumulator
{
    public int numberOfErrors;
    public ArrayList<MessageAndException> errors;

    private int numberOfErrorsInTest ;
    public ArrayList<MessageAndException> errorsInTest ;

    public void startTest() {
        numberOfErrorsInTest = 0 ;
        errorsInTest = new ArrayList<MessageAndException>() ;
    }

    public List<MessageAndException> getTestErrors() {
        return errorsInTest ;
    }

    public ErrorAccumulator() 
    {
        numberOfErrors = 0; 
        errors = new ArrayList<MessageAndException>();
        startTest() ;
    }

    public void add(String errorMessage, Throwable t)
    {
        MessageAndException mae = new MessageAndException(errorMessage, t);
        numberOfErrors++;
        errors.add( mae );
        numberOfErrorsInTest++ ;
        errorsInTest.add( mae ) ;
    }

    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    public Collection getErrors()
    {
        return errors;
    }

    public void reportErrors(boolean printErrors, boolean printStackTrace)
    {
        U.lf();
        U.sop("==================================================");
        U.sop("Number of errors: " + numberOfErrors);
        U.sop("==================================================");
        U.lf();

        if (printErrors) {
            Iterator iterator = errors.iterator();
            while (iterator.hasNext()) {
                MessageAndException messageAndException = 
                    (MessageAndException) iterator.next();
                U.reportError(printStackTrace,
                              messageAndException.getMessage(),
                              messageAndException.getException());
            }
        }
    }

    public class MessageAndException extends Pair<String,Throwable> {
        public MessageAndException(String message, Throwable exception) {
            super( message, exception ) ;
        }

        public String getMessage() {
            return first() ;
        }

        public Throwable getException() { 
            return second() ; 
        }
    }
}

// End of file.
