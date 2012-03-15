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
/* @(#)TurnTaker.java   1.3 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

// This class puts turn taking into a thread so that the UI remains
// responsive.  You take a turn by pressing the "Take Turn" button.
// The button then becomes "Waiting for other players" and may stay
// that way for a while, until all other players have pushed their
// buttons.  During this time, your turn call is blocked in the server.
// But because I didn't like being stuck during this time, I made it
// possible to do anything but take another turn while waiting for
// other players.  So you can start working on your other turn while
// waiting for other players to finish this turn.  In reality, though,
// some of the things you do will affect the current turn that is blocked
// and waiting for other players.  This obviously needs more work, but
// it's a start!


//----------------------------------------------------------------------------
// Change History:
//
// 9/98 created  rtw
//----------------------------------------------------------------------------


package ibmspace.client;

public class TurnTaker extends Thread
{
    private GameUI       fUI;

    public TurnTaker (GameUI ui)
    {
        fUI = ui;
    }

    public void run ()
    {
        fUI.beginTurn ();
        fUI.endTurn ();
    }

}
