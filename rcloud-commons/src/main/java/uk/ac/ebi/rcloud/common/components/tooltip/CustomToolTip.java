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
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 19, 2009
 * Time: 1:03:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomToolTip extends JToolTip {

    int back = 255;
    int border = 100;

    Color background0 = new Color(back,back,back,40);
    Color background1 = new Color(back,back,back,200);
    Color bordercolor = new Color(border,border,border,200);

    public CustomToolTip() {
        //setUI(new CustomToolTipUI());
        //setBackground(new Color(255,255,255,0));
    }

    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(new Color(255,255,255,0));

        g2d.clearRect(0,0,w,h);

        g2d.setColor(bordercolor);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(0,0,w-1,h-1,20,20);

        g2d.setColor(background1);
        g2d.fillRoundRect(1,1,w-3,h-3,20,20);

        ///super.paint(g);

    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        frame.setLayout(new BorderLayout(0,0));

        JButton butt = new JButton("button"){
            public JToolTip createToolTip(){
                JToolTip tooltip = new CustomToolTip();
                tooltip.setComponent(this);
                return tooltip;
            }
        };

        butt.setToolTipText("<html>some button dflg sdfg sdlfkg<br>some button dflg sdfg sdlfkg<br></html>");

        frame.add(butt, BorderLayout.NORTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
