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
package uk.ac.ebi.rcloud.common.components.tooltip;

import javax.swing.*;
import javax.swing.plaf.metal.MetalToolTipUI;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 19, 2009
 * Time: 1:03:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomToolTipUI extends MetalToolTipUI {

    private int maxWidth = 0;

    int back = 255;
    int border = 100;

    Color background0 = new Color(back,back,back,40);
    Color background1 = new Color(back,back,back,200);
    Color bordercolor = new Color(border,border,border,200);

    public void paint(Graphics g, JComponent c) {

        int w = c.getWidth();
        int h = c.getHeight();

        Graphics2D g2d = (Graphics2D) g;

        g2d.clearRect(0,0,w,h);

        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
        new float[] { 3, 1 }, 0)); 

        g2d.setColor(bordercolor);
        g2d.drawRoundRect(0,0,w-1,h-1,20,20);

        g2d.setColor(background1);
        g2d.fillRoundRect(1,1,w-3,h-3,20,20);

        //super.paint(g, c);

        /*
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(
            g.getFont());
        Dimension size = c.getSize();
        g.setColor(c.getBackground());
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(c.getForeground());
        if (strs != null) {
            for (int i = 0; i < strs.length; i++) {
                g.drawString(strs[i], 3, (metrics.getHeight()) * (i + 1));
            }
        }
        */
    }

    public Dimension getPreferredSize(JComponent c) {
        return super.getPreferredSize(c);

        /*
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(c.getFont());
        String tipText = ((JToolTip) c).getTipText();

        if (tipText == null) {
            tipText = "";
        }

        BufferedReader br = new BufferedReader(new StringReader(tipText));
        String line;
        int maxWidth = 0;
        Vector v = new Vector();
        try {
            while ((line = br.readLine()) != null) {
                int width = SwingUtilities.computeStringWidth(metrics, line);
                maxWidth = (maxWidth < width) ? width : maxWidth;
                v.addElement(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int lines = v.size();
        if (lines < 1) {
            strs = null;
            lines = 1;
        } else {
            strs = new String[lines];
            int i = 0;
            for (Enumeration e = v.elements(); e.hasMoreElements(); i++) {
                strs[i] = (String) e.nextElement();
            }
        }
        int height = metrics.getHeight() * lines;
        this.maxWidth = maxWidth;
        return new Dimension(maxWidth + 6, height + 4);
        */
    }
}
