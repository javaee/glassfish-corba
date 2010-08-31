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
/* @(#)SpaceFrame.java	1.3 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

// This is the main window.   It creates and lays out all the controls
// and views that are its contents.


//----------------------------------------------------------------------------
// Change History:
//
// 9/98 created  rtw
//----------------------------------------------------------------------------


package ibmspace.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SpaceFrame extends JFrame
{
    GameUI fGameUI = null;


    public SpaceFrame(String username)
    {
	fGameUI = new GameUI (this, new GameSurrogate(username));

	getContentPane().setLayout (new PercentLayout(PercentLayout.HORZ));
	setSize(new Dimension(762, 591));
	setTitle("IBM Space Conquest! RMI-IIOP Demo (" + username + ")");

	// Menu Bar

	MenuBar menuBar = new MenuBar ();
	menuBar.add (fGameUI.createBuildMenu());
	menuBar.add (fGameUI.createDoMenu());
	setMenuBar(menuBar);


	//
	// Create UI Components
	//

	JPanel controlPanel = new JPanel ();
	controlPanel.setLayout (new PercentLayout(PercentLayout.VERT));
	getContentPane().add (controlPanel, new Float(.35f));


	// Planet Stats Pane

	JPanel PlanetStatsPanel = new JPanel();
	PlanetStatsPanel.setLayout(new BorderLayout());
	JComponent planetStatsUI = fGameUI.createPlanetStatsUI ();
	PlanetStatsPanel.add(planetStatsUI, BorderLayout.CENTER);


	// Planet Resource Control Pane

	JPanel PlanetResourceControl = new JPanel();
	PlanetResourceControl.setBackground(Color.black);
	PlanetResourceControl.setLayout(new BorderLayout());
	PieControl pie = fGameUI.createPlanetSpendingUI ();
	pie.setSecondPercentage(0.3);
	pie.setFirstPercentage(20.0);
	pie.setBorder (new EmptyBorder(5,5,5,5));
	PlanetResourceControl.add(pie, BorderLayout.CENTER);


	// Planet Group

	JPanel planetGroup = new JPanel();
	planetGroup.setLayout (new PercentLayout(PercentLayout.HORZ));
	planetGroup.setBorder (new EtchedBorder());
	planetGroup.add(PlanetStatsPanel, new Float(.5f));
	planetGroup.add(PlanetResourceControl, new Float(.5f));
	controlPanel.add(planetGroup, new Float(.2f));


	// Turn Button

	JButton turnButton = fGameUI.createTurnButton ();
	turnButton.addActionListener(new SpaceFrame_miTakeTurn_actionAdapter(this));
	controlPanel.add(turnButton, new Float(.07f));


	// Ship List Pane

	JComponent shipListUI = fGameUI.createShipListUI ();
	controlPanel.add(shipListUI, new Float(.13f));


	// Resources Pane

	JComponent resourceLevelsUI = fGameUI.createResourceLevelsUI ();
	resourceLevelsUI.setBorder (new EtchedBorder());
	controlPanel.add (resourceLevelsUI, new Float(.1f));


	// Budget Pane

	JComponent budgetUI = fGameUI.createBudgetUI ();
	JScrollPane budgetScroller = new JScrollPane ();
	budgetScroller.getViewport().add(budgetUI, null);
	controlPanel.add(budgetScroller, new Float(.3f));

    
	// Tech Spending Pane

	JComponent techSpendingUI = fGameUI.createTechSpendingUI ();
	JScrollPane techSpendingScroller = new JScrollPane ();
	techSpendingScroller.getViewport().add (techSpendingUI, null);
	controlPanel.add (techSpendingScroller, new Float(.2f));


	// Space Pane

	JScrollPane galaxyScroller = new JScrollPane();
	galaxyScroller.setPreferredSize(new Dimension(300, 300));
	galaxyScroller.setMinimumSize(new Dimension(300, 300));
	GalaxyView galaxyView = fGameUI.createGalaxyUI ();
	galaxyScroller.getViewport().add(galaxyView, null);
	getContentPane().add (galaxyScroller, new Float(.65f));

	fGameUI.init ();

    }

    public void fileExit_actionPerformed(ActionEvent e)
    {
	System.exit(0);
    }


    void miTakeTurn_actionPerformed(ActionEvent e)
    {
	System.out.println ("Taking turn");
	fGameUI.takeTurn ();
    }

}



class SpaceFrame_miTakeTurn_actionAdapter implements java.awt.event.ActionListener
{
    SpaceFrame adaptee;

  
    SpaceFrame_miTakeTurn_actionAdapter(SpaceFrame adaptee)
    {
	this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
	adaptee.miTakeTurn_actionPerformed(e);
    }
}
