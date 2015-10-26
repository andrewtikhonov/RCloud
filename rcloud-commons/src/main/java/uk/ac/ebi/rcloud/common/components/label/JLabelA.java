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
 * Date: Jun 13, 2009
 * Time: 3:20:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class JLabelA extends JLabel {

    private float alpha = 1.0f;
    private String text;

    public JLabelA (String text) {
        super(text);
        this.text = text;
        super.setForeground(new Color(50, 100, 150));
    }

    public JLabelA () {
        this("");
    }

    public void setText (String text) {
        this.text   = text;
    }

    public void setBackground(Color c){
        super.setBackground(c);
        if (isShowing()) { repaint(); }
    }

    public void setForeground(Color c){
        super.setForeground(c);
        if (isShowing()) { repaint(); }
    }

    public void setAlpha(float alpha){
        this.alpha = alpha;
        repaint();
    }

    public float getAlpha(){
        return alpha;
    }

    public void paintComponent(Graphics g){

        AlphaComposite aComposite = AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, alpha);

        ((Graphics2D)g).setComposite(aComposite);

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);


        int x = 0;
        int y = 0;

        g.setColor(getForeground());
        g.drawString(text, x, y);
    }


}
