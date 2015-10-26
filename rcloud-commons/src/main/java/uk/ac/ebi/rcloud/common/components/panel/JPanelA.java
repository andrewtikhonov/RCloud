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

import uk.ac.ebi.rcloud.common.graphics.effects.GaussianBlurFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 25, 2009
 * Time: 3:45:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class JPanelA extends JPanelAlphaBase {

    int     shadow = 20;
    int     borderArcW   = 10;
    int     borderArcH   = 10;
    int     panelArcW    = 8;
    int     panelArcH    = 8;
    int     borderStroke = 4;
    Color background     = new Color(10, 10, 0, 200);
    Color bordercolor    = Color.GRAY;
    boolean gradient     = false;

    BufferedImage compBuffer;

    public void setGradient(boolean g){
        gradient = g;

        if (isShowing()) { repaint(); }
    }

    public void setBackground(Color c){
        background = c;

        if (isShowing()) { repaint(); }
    }

    public Color getBackground(){
        return background;
    }

    public void setShadowSize(int size){
        shadow = size;

        if (isShowing()) { repaint(); }
    }

    public void setBorderColor(Color c){
        bordercolor = c;

        if (isShowing()) { repaint(); }
    }

    public void setBorderArc(int w, int h){
        borderArcW = w;
        borderArcH = h;

        if (isShowing()) { repaint(); }
    }

    public void setBorderStroke(int s){
        borderStroke = s;

        if (isShowing()) { repaint(); }
    }

    public void setPanelArc(int w, int h){
        panelArcW = w;
        panelArcH = h;

        if (isShowing()) { repaint(); }
    }

    public BufferedImage renderComponent(BufferedImage buffer) {

        int indent, smalloverlap, x, y, w, h;

        Graphics2D g2d = buffer.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        indent = borderStroke/2 + shadow;
        x = indent;
        y = indent - shadow/2;
        w = getWidth() - indent * 2 - 2 ;
        h = getHeight() - indent * 2 - 2 ;
        g2d.setColor(bordercolor);
        g2d.setStroke(new BasicStroke(borderStroke));
        g2d.drawRoundRect(x, y, w, h, borderArcW, borderArcH);
        
        if (gradient) {
            GradientPaint bgGradient = new GradientPaint(
                    0, 0, getBackground(),
                    0, this.getHeight(), getBackground().darker(), true);


            g2d.setPaint(bgGradient);
        } else {
            g2d.setColor(background);
        }

        indent = borderStroke + shadow;
        smalloverlap = 1;
        x = indent - smalloverlap;
        y = indent - smalloverlap - shadow/2;;
        w = getWidth() - (indent - smalloverlap)* 2 - 3 ;
        h = getHeight() - (indent - smalloverlap) * 2 - 3;


        g2d.fillRoundRect(x, y, w, h, panelArcW, panelArcH);

        g2d.dispose();
        
        return buffer;

    }

    public BufferedImage renderShadow(BufferedImage buffer) {

        int indent, x, y, w, h;

        if (shadow == 0) return buffer;

        Graphics2D g2d = buffer.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        indent = borderStroke/2 + shadow;
        x = indent;
        y = indent - shadow/2 + 8;
        w = getWidth() - indent * 2 ;
        h = getHeight() - indent * 2;

        g2d.setColor(new Color(0,0,0,170));
        //g2d.setStroke(new BasicStroke(5));

        g2d.fillRoundRect(x, y, w, h, borderArcW, borderArcH);

        GaussianBlurFilter blur = new GaussianBlurFilter(20);
        buffer = blur.filter(buffer, null);

        g2d.dispose();

        return buffer;

        /*
        ColorTintFilter tint = new ColorTintFilter(Color.GRAY, 1.0f);

        compBuffer = tint.filter(compBuffer, null);
        */
    }

    public void paintComponent(Graphics g) {

        if (compBuffer == null ||
                compBuffer.getWidth() != getWidth() ||
                compBuffer.getHeight() != getHeight()) {

            compBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            compBuffer = renderShadow(compBuffer);
            compBuffer = renderComponent(compBuffer);
        }

        ((Graphics2D)g).setComposite(AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, alpha));

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(compBuffer, 0, 0, null);

    }
}
