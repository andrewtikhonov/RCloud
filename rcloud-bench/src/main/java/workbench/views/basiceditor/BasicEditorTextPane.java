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
package workbench.views.basiceditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.views.basiceditor.highlighting.NonWrappingTextPane;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.Graphics;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 19, 2010
 * Time: 4:51:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicEditorTextPane extends NonWrappingTextPane {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private Rectangle region = new Rectangle(0,0,0,0);
    private Color highlighter = new Color(10,10,40,20);

    public BasicEditorTextPane(){
        super();
        //JLayeredPane.
    }

    public void paintComponent(Graphics g) {

        Rectangle clip0 = g.getClip().getBounds();
        Rectangle pos0 = new Rectangle(0,0,0,0);

        try {
            pos0 = modelToView(getCaretPosition());
        } catch (BadLocationException ble) {
        }

        super.paintComponent(g);

        if (region.y != pos0.y) {

            Rectangle clip1 = getVisibleRect();

            //log.info(" ");
            //log.info("pos0="+pos0);
            //log.info("clip0="+clip0);
            //log.info("clip1="+clip1);
            //log.info("1 region="+region);

            // redraw old marker
            g.setClip(region); // new Rectangle(clip1.x, region.y, clip1.width, region.height)

            super.paintComponent(g);

            // draw new marker
            region = new Rectangle(clip1.x, pos0.y, clip1.width, pos0.height);

            //log.info("2 region="+region);

            g.setClip(region);

            g.setColor(highlighter);

            ((Graphics2D) g).fill(region);

        } else {

            super.paintComponent(g);

            g.setColor(highlighter);

            Rectangle r0 = new Rectangle(clip0.x, pos0.y, clip0.width, pos0.height);

            //log.info("pos0="+pos0);
            //log.info("clip0="+clip0);
            //log.info("r0="+r0);

            ((Graphics2D) g).fill(r0);


        }


        /*
        Rectangle clip0 = g.getClip().getBounds();
        Rectangle pos0 = new Rectangle(0,0,0,0);

        try {
            pos0 = modelToView(getCaretPosition());
        } catch (BadLocationException ble) {
        }

        Rectangle clip1 = getVisibleRect();
        clip0.x = clip1.x;
        clip0.width = clip1.width;

        g.setClip(clip0);

        super.paintComponent(g);

        Rectangle draw = new Rectangle(clip0.x, pos0.y, clip1.width, pos0.height);

        g.setColor(highlighter);

        ((Graphics2D) g).fill(draw);

        if (region != null && region.y != draw.y) {
            g.setClip(region);
            super.paintComponent(g);
        }

        region = draw;
        */
    }
}

