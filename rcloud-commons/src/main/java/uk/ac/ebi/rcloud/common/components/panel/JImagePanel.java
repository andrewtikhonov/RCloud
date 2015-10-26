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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.net.URL;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 19, 2009
 * Time: 3:49:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class JImagePanel extends JPanel {
    final private Logger log = LoggerFactory.getLogger(getClass());

    Image back = null;

    private Image loadImage(String name) {
        Image img = null;
        URL url = getClass().getResource(name);
        try {
            img = ImageIO.read(url);
        } catch (IOException ex) {
            log.error("Error!", ex);
        }

        return img;
    }

    public JImagePanel(){
        super();
    }

    public JImagePanel(LayoutManager layout){
        super(layout);
    }

    public void setImage(String file){
        back = loadImage(file);
    }

    public void paintComponent(Graphics g) {
        //super.paintComponent(g);

        if (back != null)
        {
            g.drawImage(back, 0, 0, this);
        }
    }
}
