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

// A layout manager that lays out components in either a vertical
// or horizontal arrangement giving each component a percentage
// of the arrangement.  I did this quickly at the end and it can
// certainly be improved.  For one, it doesn't check to ensure that
// the percentages total 100!  It's still pretty useful.

package ibmspace.client;

import java.lang.*;
import java.awt.*;
import java.util.*;


public class PercentLayout implements LayoutManager2
{
    static int VERT = 0;
    static int HORZ = 1;

    private int fAlign;

    private Vector fComponents = null;

    public PercentLayout (int align)
    {
        fAlign = align;
        fComponents = new Vector ();
    }

    public void addLayoutComponent (Component c, Object constraints)
    {
        fComponents.addElement (new ComponentInfo(c,(Float)constraints));
    }

    public void addLayoutComponent(String name, Component c)
    {
        // Not supported
    }
  
    public void removeLayoutComponent (Component c)
    {
        for (int i=0; i<fComponents.size(); i++) {
            ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
            if ( ci.fComponent == c) {
                fComponents.removeElement (ci);
            }
        }
    }

    public float getLayoutAlignmentX (Container target)
    {
        return target.getAlignmentX ();
    }

    public float getLayoutAlignmentY (Container target)
    {
        return target.getAlignmentY ();
    }

    public void invalidateLayout (Container target)
    {
    }

    public Dimension preferredLayoutSize(Container target)
    {
        Dimension size = new Dimension (0,0);

        if ( fAlign == VERT ) {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                Dimension cSize = c.getPreferredSize ();
                size.setSize (Math.max(size.width,cSize.width), size.height+cSize.height);
            }
        } else {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                Dimension cSize = c.getPreferredSize ();
                size.setSize (size.width+cSize.width, Math.max(size.height,cSize.height));
            }
        }

        return size;
    }

    public Dimension minimumLayoutSize(Container target)
    {
        Dimension size = new Dimension (0,0);

        if ( fAlign == VERT ) {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMinimumSize ();
                size.setSize (Math.max(size.width,cSize.width), size.height+cSize.height);
            }
        } else {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMinimumSize ();
                size.setSize (size.width+cSize.width, Math.max(size.height,cSize.height));
            }
        }

        return size;
    }

    public Dimension maximumLayoutSize (Container target)
    {
        Dimension size = new Dimension (0,0);

        if ( fAlign == VERT ) {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMaximumSize ();
                size.setSize (Math.max(size.width,cSize.width), size.height+cSize.height);
            }
        } else {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                Dimension cSize = c.getMaximumSize ();
                size.setSize (size.width+cSize.width, Math.max(size.height,cSize.height));
            }
        }

        return size;
    }
  
    public void layoutContainer (Container target)
    {
        Insets insets = target.getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = target.getSize().width - (insets.left + insets.right);
        int height = target.getSize().height - (insets.top + insets.bottom);

        if ( fAlign == VERT ) {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                float p = ci.fPercentage;
                int ch = (int)(height*p);
                c.setLocation (x, y);
                c.setSize (width, ch);
                y += ch;
            }
        } else {
            for (int i=0; i<fComponents.size(); i++) {
                ComponentInfo ci = (ComponentInfo)fComponents.elementAt (i);
                Component c = ci.fComponent;
                float p = ci.fPercentage;
                int cw = (int)(width*p);
                c.setLocation(x, y);
                c.setSize (cw, height);
                x += cw;
            }
        }

    }

}


class ComponentInfo
{
    public Component  fComponent = null;
    public float      fPercentage;

    public ComponentInfo (Component c, Float p)
    {
        fComponent = c;
        fPercentage = p.floatValue ();
    }
}
