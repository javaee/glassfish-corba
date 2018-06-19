/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

public class PlanetNames implements java.io.Serializable
{
    private Vector      fNames;

    public PlanetNames ()
    {
        fNames = new Vector ();
        fNames.addElement (new String ("Deneb"));
        fNames.addElement (new String ("Proxima"));
        fNames.addElement (new String ("Enif"));
        fNames.addElement (new String ("Altair"));
        fNames.addElement (new String ("Ursa"));
        fNames.addElement (new String ("Sauron"));
        fNames.addElement (new String ("Sol"));
        fNames.addElement (new String ("Propus"));
        fNames.addElement (new String ("Hobbes"));
        fNames.addElement (new String ("Spica"));
        fNames.addElement (new String ("Yavin"));
        fNames.addElement (new String ("Virgo"));
        fNames.addElement (new String ("Tiber"));
        fNames.addElement (new String ("Quark"));
        fNames.addElement (new String ("Coxa"));
        fNames.addElement (new String ("Libra"));
        fNames.addElement (new String ("Atlas"));
        fNames.addElement (new String ("Alkaid"));
        fNames.addElement (new String ("Antares"));
        fNames.addElement (new String ("Rigel"));
        fNames.addElement (new String ("Murzim"));
        fNames.addElement (new String ("Barsoon"));
        fNames.addElement (new String ("Atria"));
        fNames.addElement (new String ("Thune"));
        fNames.addElement (new String ("Regor"));
        fNames.addElement (new String ("Remulak"));
        fNames.addElement (new String ("Sooltar"));
        fNames.addElement (new String ("Klah"));
        fNames.addElement (new String ("Dabih"));
        fNames.addElement (new String ("Basil"));
        fNames.addElement (new String ("Hope"));
        fNames.addElement (new String ("Torino"));
        fNames.addElement (new String ("Scorpio"));
        fNames.addElement (new String ("Procyon"));
        fNames.addElement (new String ("Beid"));
        fNames.addElement (new String ("Denali"));
        fNames.addElement (new String ("Ain"));
    }

    public String getName ()
    {
        int index = (int)(Math.random() * (fNames.size()-1));
        String name = (String)fNames.elementAt (index);
        fNames.removeElementAt (index);
        return name;
    }
  
}
