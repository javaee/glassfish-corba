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

// GalaxyView draws the main view with all the planet gifs and
// journey arrows.  It also handles input for planet selection
// and journey arrow dragging.  It implements its own double-
// buffering and maintains a backbuffer of the parts of the map
// that don't change that often.  This is just for performance
// and smooth drawing.

package ibmspace.client;

import java.io.IOException;
import java.awt.*;
import java.awt.image.ImageProducer;
import java.util.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import ibmspace.common.*;


public class GalaxyView extends JPanel implements MouseListener, MouseMotionListener
{
    public Image           fBackBuffer = null;
    private Image           fBackBuffer2 = null;
    private int             fNumStars;
    private Point[]         fStarField;
    private GameUI          fUI;
    private GameSurrogate   fGame;

    // Selection

    private ID              fSelection;
    private int             fSelectionIndex;

    // Dragging

    private boolean         fDragging;
    private int             fDestination;
    private ID              fFleet;
    private int             fRange;
    private Point           fOriginCenter, fDestCenter;
    private ActionSource    fActionSource;
    private int             fNumberOfPlanets;
    private Rectangle[]     fPlanetBounds;

    private int             fNumberOfJournies = 0;
    private JourneyArrow[]  fJournies = null;

    private Image           fLogo = null;

    public GalaxyView (GameUI ui, GameSurrogate game)
    {
        fUI = ui;
        fGame = game;
    
        Dimension size = new Dimension (700,700);
        setSize (size);
        setPreferredSize (size);
        setMinimumSize (size);
        setMaximumSize (size);
        setBackground (Color.black);

        fSelection = game.getHome ();
        fSelectionIndex = fGame.getPlanetIndex (fSelection);

        fDragging = false;

        initStarField (700, (int)(100 * 700/500));

        addMouseListener (this);
        addMouseMotionListener (this);
        fActionSource = new ActionSource ();

        fLogo = loadImage ("logo.gif");

        //
        // Load Planet Images
        //

        PlanetViewUI.setGoodPlanetImage (loadImage("good.gif"));
        PlanetViewUI.setUnknownPlanetImage (loadImage("unknown.gif"));
        PlanetViewUI.setBadPlanetImage (loadImage("ugly.gif"));
        PlanetViewUI.setSelectionImage (loadImage("selection.gif"));


        //
        // Cache planet bounds for quick hit detection during drags
        //

        fNumberOfPlanets = fGame.getNumberOfPlanets ();
        fPlanetBounds = new Rectangle [fNumberOfPlanets];
        for (int i=0; i<fNumberOfPlanets; i++) {
            PlanetView planet = fGame.getPlanet (i);
            PlanetViewUI planetUI = new PlanetViewUI (planet);
            fPlanetBounds[i] = planetUI.getBounds ();
        }
    
    }

    private Image loadImage (String name)
    {
        try
            {
                Class refClass = Class.forName ("ibmspace.client.GameUI");
                URL url = refClass.getResource (name);
                //ImageProducer producer =(ImageProducer)url.getContent ();
                //Image image = this.getToolkit().createImage (producer);
                ImageIcon icon = new ImageIcon (url);
                Image image = icon.getImage ();
                prepareImage (image, this);
                return image;
            }
        catch (Exception e)
            {
                return null;
            }
    }


    private static Point centerOf (Rectangle r)
    {
        return new Point (r.x + r.width/2, r.y + r.height/2);
    }

    //
    // Painting
    //

    public void paint (Graphics g)
    {
        update (g);
    }

    public void update (Graphics g)
    {
        /*
          if ( fBackBuffer2 == null ) {
          fBackBuffer2 = createImage (700,700);
          prepareImage (fBackBuffer2, this);
          }
        */

        //Graphics bg = fBackBuffer2.getGraphics ();
        drawBackground (g);
        drawJournies (g);
        drawPlanets (g);
        //g.drawImage (fBackBuffer2, 0, 0, null);
    }

    private void drawBackground (Graphics g)
    {
        if ( fBackBuffer == null ) createBackBuffer ();
        g.drawImage (fBackBuffer, 0, 0, null);
    }

    private void drawJournies (Graphics g)
    {
        for (int i=0; i<fNumberOfJournies; i++) {
            fJournies[i].draw (g);
        }
    }

    private void drawPlanets (Graphics g)
    {
        for (int i=0; i<fGame.getNumberOfPlanets(); i++) {
            PlanetView planet = fGame.getPlanet (i);
            PlanetViewUI planetUI = new PlanetViewUI (planet);
            if ( fSelection.identifies (planet) )
                planetUI.draw (g, true, true, true);
            else
                planetUI.draw (g, false, false, false);
        }
    }

    //
    // Caching of data for quicker drawing
    //

    private void createBackBuffer ()
    {
        fBackBuffer = createImage (700,700);
        prepareImage (fBackBuffer, this);
        Graphics g = fBackBuffer.getGraphics ();

        // Draw background

        g.setColor (Color.black);
        g.fillRect (0,0,700,700);
        drawStarField (g);
        g.drawImage (fLogo, 5, 5, null);

        // Draw planets

        for (int i=0; i<fGame.getNumberOfPlanets(); i++) {
            PlanetView planet = fGame.getPlanet (i);
            PlanetViewUI planetUI = new PlanetViewUI (planet);

            if ( fSelection.identifies (planet) )
                planetUI.draw (g, true, true, true);
            else
                planetUI.draw (g, true, true, false);
        }

        g.dispose ();
    }

    public void refreshCache ()
    {
        fBackBuffer = null;

        fNumberOfJournies = fGame.getNumberOfJournies ();
        fJournies = new JourneyArrow [fNumberOfJournies];
        for (int i=0; i<fNumberOfJournies; i++) {
            Journey journey = fGame.getJourney (i);
            JourneyArrow a = new JourneyArrow ();
            int origin = fGame.getPlanetIndex (journey.getOrigin().getID());
            int destination = fGame.getPlanetIndex (journey.getDestination().getID());
            a.setAvailableRange (100);
            a.setTail (centerOf(fPlanetBounds[origin]));
            a.setHead (centerOf(fPlanetBounds[destination]));
            a.setHeadPadding (30);
            a.setTailPadding ((int)(a.getLength()*journey.getPercentComplete()));
            fJournies[i] = a;
        }
    }


    //
    // Starfield
    //

    private void drawStarField (Graphics g)
    {
        g.setColor (Color.white);
        for (int i = 0; i<fNumStars; i++) {
            g.drawRect (fStarField[i].x, fStarField[i].y, 1, 1);
        }
    }

    private void initStarField (int size, int numStars)
    {
        fStarField = new Point [numStars];
        fNumStars = numStars;

        for (int i = 0; i<numStars; i++) {
            int x = (int)(Math.random() * (double)size);
            int y = (int)(Math.random() * (double)size);
            fStarField[i] = new Point (x,y);
        }
    }

    //
    // Mouse Handling
    //

    private int hitPlanet (Point p)
    {
        for (int i=0; i<fNumberOfPlanets; i++) {
            if ( i != fSelectionIndex && fPlanetBounds[i].contains (p) )
                return i;
        }
        return -1;
    }

    public void mousePressed (MouseEvent e)
    {

        Point p = e.getPoint ();

        for (int i=0; i<fGame.getNumberOfPlanets(); i++) {
            PlanetView planet = fGame.getPlanet (i);
            PlanetViewUI planetUI = new PlanetViewUI (planet);
            Rectangle bounds = planetUI.getBounds ();
            if ( bounds.contains (p) ) {
                fDragging = true;
                fFleet = null;
                fDestination = -1;
                int cx = bounds.x + bounds.width / 2;
                int cy = bounds.y + bounds.height / 2;
                fOriginCenter = new Point (cx,cy);
                fDestCenter = new Point (cx,cy);
                if ( !planet.getID().equals (fSelection) ) {
                    fSelection = planet.getID();
                    fSelectionIndex = fGame.getPlanetIndex (fSelection);
                    fActionSource.notifyListeners (planet,"Selection Changed");
                    repaint ();
                }
                return;
            }
        }
    }

    public void mouseReleased (MouseEvent e)
    {
        if ( fDragging = true ) {

            ID fleet = fUI.getSelectedFleet ();
            ID destPlanet = null;

            fDragging = false;
            Point p = e.getPoint ();
            Graphics g = getGraphics ();
            drawBackground (g);
            int fDestination = hitPlanet (p);
            if ( fDestination != -1 ) {
                PlanetView planet = fGame.getPlanet (fDestination);
                if ( !planet.getID().equals (fSelection) ) {
                    Rectangle bounds = fPlanetBounds[fDestination];
                    int cx = bounds.x + bounds.width / 2;
                    int cy = bounds.y + bounds.height / 2;
                    p = new Point (cx, cy);

                    JourneyArrow a = new JourneyArrow ();
                    a.setTail (fOriginCenter);
                    a.setHead (p);

                    if ( a.getDistance() <= fUI.getSelectedFleetRange() ) {
                        if ( fleet != null ) {
                            destPlanet = fGame.getPlanet(fDestination).getID ();
                        }
                    }
                }
            }

            if ( fleet != null ) {
                fGame.sendFleet (fleet, destPlanet);
                fGame.updateJourneys ();
                fUI.updateShipListView ();
                refreshCache ();
            }
      
            repaint ();
        }
    }

    public void mouseDragged (MouseEvent e)
    {
        if ( fBackBuffer2 == null ) {
            fBackBuffer2 = createImage (700,700);
            prepareImage (fBackBuffer2, this);
        }

        if ( fDragging = true ) {
            Point p = e.getPoint ();
            Graphics g = getGraphics ();
            Graphics bg = fBackBuffer2.getGraphics ();
            drawBackground (bg);
            drawJournies (bg);
            int fDestination = hitPlanet (p);
            if ( fDestination == -1 ) {
                bg.setColor (Color.red);
                bg.drawLine (fOriginCenter.x, fOriginCenter.y, p.x, p.y);
            } else {
                Rectangle bounds = fPlanetBounds[fDestination];
                int cx = bounds.x + bounds.width / 2;
                int cy = bounds.y + bounds.height / 2;
                p = new Point (cx, cy);
                JourneyArrow a = new JourneyArrow ();
                fFleet = fUI.getSelectedFleet ();
                a.setAvailableRange (fUI.getSelectedFleetRange());
                a.setTail (fOriginCenter);
                a.setHead (p);
                a.setHeadPadding (30);
                a.setShowDistance (true);
                a.draw (bg);
            }

            drawPlanets (bg);
            g.drawImage (fBackBuffer2, 0, 0, null);

        }

    }

    public void mouseClicked (MouseEvent e) {}
    public void mouseEntered (MouseEvent e) {}
    public void mouseExited (MouseEvent e) {}
    public void mouseMoved (MouseEvent e) {}


    //
    // Action Management
    //

    public void addActionListener (ActionListener listener)
    {
        fActionSource.addActionListener (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fActionSource.removeActionListener (listener);
    }

}
