/*
 * R Cloud - R-based Cloud Platform for Computational Research
 * at EMBL-EBI (European Bioinformatics Institute)
 *
 * Copyright (C) 2007-2015 European Bioinformatics Institute
 * Copyright (C) 2009-2015 Andrew Tikhonov - andrew.tikhonov@gmail.com
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package workbench.dialogs.projectbrowser;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by andrew on 08/06/15.
 */
public class ViewVerticalLayout implements LayoutManager2, Serializable {

    public ViewVerticalLayout () { }

    public Dimension minimumLayoutSize (Container target) {
        return (preferredLayoutSize (target));
    }

    public Dimension preferredLayoutSize (Container target) {
        int count;
        Component component;
        Dimension dimension;
        Insets insets;
        Dimension ret;

        synchronized (target.getTreeLock ())
        {
            // get the the total height and maximum width component
            ret = new Dimension (0, 0);
            count = target.getComponentCount ();
            for (int i = 0 ; i < count ; i++)
            {
                component = target.getComponent (i);
                if (component.isVisible ())
                {
                    dimension = component.getPreferredSize ();
                    ret.width = Math.max (ret.width, dimension.width);
                    ret.height += dimension.height;
                }
            }
            insets = target.getInsets ();
            ret.width += insets.left + insets.right;
            ret.height += insets.top + insets.bottom;
        }

        return (ret);
    }

    public Dimension maximumLayoutSize (Container target) {
        return (preferredLayoutSize (target));
    }

    public void addLayoutComponent (String name, Component comp) {
    }

    public void removeLayoutComponent (Component comp) {
    }

    public void layoutContainer (Container target) {
        Insets insets;
        int x;
        int y;
        int count;
        int width;
        Component component;
        Dimension dimension;

        synchronized (target.getTreeLock ()) {
            insets = target.getInsets ();
            x = insets.left;
            y = insets.top;
            count = target.getComponentCount ();
            width = 0;
            for (int i = 0 ; i < count ; i++) {
                component = target.getComponent (i);
                if (component.isVisible ()) {
                    dimension = component.getPreferredSize ();
                    width = Math.max (width, dimension.width);
                    component.setSize (dimension.width, dimension.height);
                    component.setLocation (x, y);
                    y += dimension.height;
                }
            }
            // now set them all to the same width
            for (int i = 0 ; i < count ; i++) {
                component = target.getComponent (i);
                if (component.isVisible ()) {
                    dimension = component.getSize ();
                    dimension.width = width;
                    component.setSize (dimension.width, dimension.height);
                }
            }
        }
    }

    public void addLayoutComponent (Component comp, Object constraints) {

    }

    public float getLayoutAlignmentX (Container target) {
        return (0.0f);
    }

    public float getLayoutAlignmentY (Container target) {
        return (0.0f);
    }

    public void invalidateLayout (Container target) {
    }
}