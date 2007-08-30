/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import java.util.HashMap;
import java.util.Map;

import com.sun.interview.Checklist;
import com.sun.interview.ChoiceArrayQuestion;
import com.sun.interview.ChoiceQuestion;
import com.sun.interview.ErrorQuestion;
import com.sun.interview.FileQuestion;
import com.sun.interview.FinalQuestion;
import com.sun.interview.FloatQuestion;
import com.sun.interview.InetAddressQuestion;
import com.sun.interview.IntQuestion;
import com.sun.interview.Interview;
import com.sun.interview.NullQuestion;
import com.sun.interview.Question;
import com.sun.interview.StringQuestion;
import com.sun.interview.YesNoQuestion;

import com.sun.javatest.Parameters.EnvParameters;
import com.sun.javatest.TestEnvironment;
import com.sun.javatest.interview.DefaultInterviewParameters;

public class CORBATestInterview
    extends DefaultInterviewParameters
    implements EnvParameters
{
    public CORBATestInterview() throws Fault {
	super("CORBA Tests");
	// setHelpSet("moreInfo/idemo");
	// setResourceBundle("i18n");
	// setFirstQuestion(qIntro);
    }

    public TestEnvironment getEnv() {
	HashMap envEntries = new HashMap();
	export(envEntries);
	try {
	    String name = "CORBA";
	    return new TestEnvironment(name, envEntries, "configuration interview");
	}
	catch (TestEnvironment.Fault e) {
	    throw new Error("should not happen");
	}
    }

    public EnvParameters getEnvParameters() {
	return this;
    }

    public Question getEnvFirstQuestion() {
	// this method is not used in this interview because we don't use the
	// standard prolog; instead we go straight to our own introduction (qIntro), 
	// but the method needs to be defined, so...
	return null;
    }
}
