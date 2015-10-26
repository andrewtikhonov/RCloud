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
package uk.ac.ebi.rcloud.common.components.border;

import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 11, 2009
 * Time: 6:20:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShadowedLineBorder extends LineBorder {

    private Color col0 = new Color(210,210,210);
    private Color col1 = new Color(170,170,170);
    private Color col2 = new Color(110,110,110);
    private Color col3 = new Color(200,200,200);
    private Color col4 = new Color(245,245,245);

    private Insets borderInsets = new Insets(2,3,2,3);

    public ShadowedLineBorder(Color color) {
        super(color);
    }

    public Insets getBorderInsets(Component component) {
        return borderInsets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width,int height) {
        int h = height - 1;
        int w = width - 1;

        g.setColor(col0);
        g.drawLine(0,h,w,h); // bottom

        g.setColor(col1);
        g.drawLine(0,0,0,h); // left side
        g.drawLine(w,0,w,h); // right side

        g.setColor(col3);
        g.drawLine(1,1,w-1,1); // top shadow

        g.setColor(col2);
        g.drawLine(0,0,w,0); // top

        g.setColor(col4);
        g.drawLine(1,2,1,h-1);
        g.drawLine(w-1,2,w-1,h-1);
        g.drawLine(2,h-1,w-2,h-1);
    }

}
