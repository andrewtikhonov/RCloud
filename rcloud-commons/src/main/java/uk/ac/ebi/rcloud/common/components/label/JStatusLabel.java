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
package uk.ac.ebi.rcloud.common.components.label;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 15, 2009
 * Time: 12:31:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class JStatusLabel extends JLabel {

    private Color c1;// = new Color(0xE6E6E6);
    private Color c2;// = new Color(0xB0B0B0);

    public JStatusLabel(String text) {
        super(text);
        init();
    }

    public JStatusLabel() {
        super();
        init();
    }

    public void init() {
        Color c = getBackground();
        c1 = c.brighter();
        c2 = c.darker();
    }

    public void setBackground(Color background) {
        super.setBackground(background);
        init();
    }

    public void paintComponent(Graphics g){

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                            RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bgGradient1 = new GradientPaint(
                0, 0, c1,
                0, getHeight(), c2, false);

        ((Graphics2D)g).setPaint(bgGradient1);

        g.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(g);

        int x = getWidth() - 2;
        int y1 = 4;
        int y2 = getHeight() - 4;

        g.setColor(c2);
        g.drawLine(0, 0, getWidth(), 0);

        g.setColor(Color.LIGHT_GRAY.brighter());
        g.drawLine(0, 1, getWidth(), 1);

        g.setColor(c2);
        g.drawLine(x, y1, x, y2);

        g.setColor(c1);
        g.drawLine(x+1, y1, x+1, y2);

    }

}
