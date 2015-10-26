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
package uk.ac.ebi.rcloud.common.components.textfield;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 7, 2009
 * Time: 11:04:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class JTextFieldA extends JTextField {
    BufferedImage image;

    public JTextFieldA(String text) {
        super(text);
    }

    public JTextFieldA() {
        super();
    }

    private Color c0 = null;
    
    @Override
    public void paint(Graphics g) {

        if(image == null) {
            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        if (c0 == null) {
            Color b0 = getBackground();
            c0 = new Color(b0.getRed(), b0.getGreen(), b0.getBlue(), b0.getAlpha());
        }

        Graphics2D g2d = image.createGraphics();

        g2d.setColor(c0);

        g2d.fillRect(0,0,getWidth(),getHeight());

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        super.paint(g2d);

        g2d.dispose();

        g.drawImage(image, 0, 0, null);
    }

}

