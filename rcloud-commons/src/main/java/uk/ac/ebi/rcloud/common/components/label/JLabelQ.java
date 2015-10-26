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
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 11, 2009
 * Time: 2:05:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class JLabelQ extends JLabel {

    private BufferedImage image = null;

    public JLabelQ () {
        super();
    }

    public JLabelQ (String text) {
        super(text);
    }

    public JLabelQ (Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);
    }

    public JLabelQ (String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }


    public void paint(Graphics g){

        image = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();

        super.paint(g2d);

        g2d.dispose();

        g.drawImage(image,0,0,null);
    }


}
