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
package uk.ac.ebi.rcloud.common.components.panel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 19, 2009
 * Time: 4:07:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class JGradientPanel extends JPanel {

    Color color1 = new Color(0xEFEFEF);
    Color color2 = new Color(0xCFCFCF);
    Color color3 = new Color(0xC8C8C8);

    public JGradientPanel(){
        super();
    }

    public JGradientPanel(LayoutManager layout){
        super(layout);
    }

    public void paintComponent(Graphics g) {

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                            RenderingHints.VALUE_ANTIALIAS_ON);

        //g.setColor(Color.LIGHT_GRAY);
        //g.drawRoundRect(0, 0, getWidth(), getHeight() - 1, 8, 8);


        GradientPaint bgGradient1 = new GradientPaint(
                0, 0, color1,
                0, (int)(getHeight()/1.8f), color2, true);

        ((Graphics2D)g).setPaint(bgGradient1);

        //g.fillRect(0, 0, getWidth(), (int) (getHeight()/1.5f));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);

        GradientPaint bgGradient2 = new GradientPaint(
                0, (int) (getHeight()/1.8f), color3,
                0, getHeight(), color1);

        ((Graphics2D)g).setPaint(bgGradient2);

        g.fillRoundRect(0, (int) (getHeight()/1.8f), getWidth(), getHeight() - (int) (getHeight()/1.8f), 4, 4);

        //g.fillRect(0, getHeight()/2, getWidth(), getHeight());

        //g.setColor(Color.DARK_GRAY);
        //g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 8, 8);
    }
}
