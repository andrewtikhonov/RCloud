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
package uk.ac.ebi.rcloud.common.components.button;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 27, 2009
 * Time: 6:03:09 PM
 * To change this template use File | Settings | File Templates.
 */

public class JButtonA extends JButton {

    private BufferedImage image = null;

    public JButtonA (String title) {
        super(title);
        this.setIgnoreRepaint(true);
    }

    private Color c0 = null;


    /*
    public void paint(Graphics g){

        //Graphics2D g2d = (Graphics2D) g;

        image = getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight());

        Graphics2D g2d = image.createGraphics();

        g2d.setColor(new Color(0,0,0,0));
        g2d.fillRect(0,0,getWidth(),getHeight());

        //g2d.clearRect(0,0,getWidth(),getHeight());

        super.paint(g2d);

        g.drawImage(image,0,0,null);
    }
    */

    public void paint(Graphics g){

        //log.info("JButtonA");

        //Graphics2D g2d = (Graphics2D) g;

        image = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();

        super.paint(g2d);

        g2d.dispose();

        g.drawImage(image,0,0,null);
    }


}