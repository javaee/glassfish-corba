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

package ibmspace.spacetest;

import ibmspace.common.BudgetSummary;
import ibmspace.common.GameView;
import ibmspace.common.Planet;
import ibmspace.common.PlanetView;
import ibmspace.common.SpaceConquest;
import java.rmi.Remote;
import test.ServantContext;
import test.RemoteTest;
import javax.rmi.PortableRemoteObject;

import java.lang.reflect.Array;
import org.glassfish.pfl.test.JUnitReportHelper;

public class SpaceTest extends RemoteTest {

    private static final String servantClass = "ibmspace.server.SpaceConquestServer";
    private static final String[] compileEm =
    {
        "ibmspace.server.SpaceConquestServer",
        "ibmspace.server.GameViewServer"
    };

    private static final int TIMING_ITERATIONS = 100;
 
    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
     
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */

    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-always", "-keep"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
     
    public void doTest (ServantContext context) throws Throwable {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() 
            + ( iiop ? "_iiop" : "_jrmp" ) ) ;
        
        helper.start( "spaceTest" ) ;

        try {
            // First ensure that the caches are cleared out so
            // that we can switch between IIOP and JRMP...
            
            //Utility.clearCaches();

            Remote remote = context.startServant(servantClass,"SpaceConquest",true,iiop);

            if (remote == null) {
                throw new Exception ("startServant() failed");
            }

            // Try narrow...

            SpaceConquest game = (SpaceConquest)PortableRemoteObject.narrow(remote,SpaceConquest.class);

            if (game == null) {
                throw new Exception ("narrow() failed for remote");
            }

            GameView gameView = game.joinGame ("Test");

            Planet[] planets = game.getGalaxyMap ();

            int numPlanets = Array.getLength (planets);
            PlanetView[] planetViews = new PlanetView [numPlanets];
            BudgetSummary[] planetBudgets = new BudgetSummary [numPlanets];

            for (int i=0; i<numPlanets; i++) {
                planetViews[i] = gameView.getPlanet (planets[i].getID());
                String name = planetViews[i].getName ();
                planetBudgets[i] = gameView.getPlanetBudget (planets[i].getID());
            }

            helper.pass() ;
        } catch (Throwable thr) {
            helper.fail( thr ) ;
            throw thr ;
        } finally {
            helper.done() ;
        }
    }
}
