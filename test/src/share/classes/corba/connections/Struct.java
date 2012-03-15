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
//
// Created       : 2003 Sep 27 (Sat) 15:39:01 by Harold Carr.
// Last Modified : 2003 Sep 27 (Sat) 15:39:39 by Harold Carr.
//

package corba.connections;

import java.io.Serializable;

public class Struct implements Serializable {
    private static int INSTANCE_SIZE = 20;

    public static Struct[] getSampleInstance() {
        Struct[] instance = new Struct[INSTANCE_SIZE];
        for (int i = 0; i < instance.length; i++) {
            instance[i] = new Struct("This is a string", 12345678, true);
        }

        return instance;
    }

    private String _stringT;
    private int _intT;
    private boolean _booleanT;

    Struct(String s, int i, boolean b) {
        _stringT = s;
        _intT = i;
        _booleanT = b;
    }

    public void setStringT(String s) {
        _stringT = s;
    }

    public String getStringT() {
        return _stringT;
    }

    public void setIntT(int i) {
        _intT = i;
    }

    public int getIntT() {
        return _intT;
    }

    public void setBooleanT(boolean b) {
        _booleanT = b;
    }

    public boolean getBooleanT() {
        return _booleanT;
    }
}

// End of file.
