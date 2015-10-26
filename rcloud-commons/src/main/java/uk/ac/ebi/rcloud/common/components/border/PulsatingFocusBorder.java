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
package uk.ac.ebi.rcloud.common.components.border;

import uk.ac.ebi.rcloud.common.animation.timing.Animator;
import uk.ac.ebi.rcloud.common.animation.timing.interpolation.PropertySetter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 12, 2009
 * Time: 11:55:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class PulsatingFocusBorder implements Border {
    private float thickness = 0.0f;
    private JComponent c;

    public PulsatingFocusBorder(JComponent c) {
        this.c = c;

        PropertySetter setter = new PropertySetter(this, "thickness", 0.0f, 1.0f);
        Animator animator = new Animator(900,
                Animator.INFINITE,Animator.RepeatBehavior.REVERSE, setter);

        this.c.putClientProperty("animator", animator);
    }

    public void paintBorder(Component c, Graphics g,
            int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle2D r = new Rectangle2D.Double(x, y, width - 1, height - 1);
        g2.setStroke(new BasicStroke(2.0f * getThickness()));

        g2.setComposite(AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, getThickness()));

        g2.setColor(new Color(0x54A4DE));
        g2.draw(r);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public float getThickness() {
        return thickness;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
        c.repaint();
    }
}
