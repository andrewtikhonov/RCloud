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

import uk.ac.ebi.rcloud.common.animation.timing.Animator;
import uk.ac.ebi.rcloud.common.animation.timing.interpolation.PropertySetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 5, 2009
 * Time: 3:30:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class JButtonEx extends JButton implements MouseListener {

    float alpha = .6f;

    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;
    private int animationtime = 300;

    public JButtonEx(){
        super();
        initAnimators();
        initListeners();
    }

    public JButtonEx (String title) {
        super(title);
        initAnimators();
        initListeners();
    }

    public void initAnimators(){
        fadeInAnimator = PropertySetter.createAnimator(
                animationtime, this, "alpha", .6f, 1.0f);
        fadeInAnimator.setAcceleration(0.2f);
        fadeInAnimator.setDeceleration(0.3f);

        fadeOutAnimator = PropertySetter.createAnimator(
                animationtime, this, "alpha", 1.0f, .6f);
        fadeOutAnimator.setAcceleration(0.2f);
        fadeOutAnimator.setDeceleration(0.3f);
    }

    public void initListeners() {
        addMouseListener(this);
    }


    // alpha setter
    public void setAlpha(float alpha){
        this.alpha = alpha;
        if (isShowing()) repaint();
    }

    // mouse listener
    public void mouseEntered(MouseEvent event) {
        fadeIn();
    }
    public void mouseExited(MouseEvent event) {
        fadeOut();
    }

    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}

    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Composite composite = g2d.getComposite();

        float tmp_alpha = 1;
        float new_alpha = this.alpha;

        if (composite instanceof AlphaComposite) {
            AlphaComposite alpha = (AlphaComposite) composite;
            tmp_alpha = alpha.getAlpha();
            new_alpha = tmp_alpha * this.alpha;
        }

        g2d.setComposite(AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, new_alpha));

        g.setColor(Color.LIGHT_GRAY);
        g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,4,4);

        g.setColor(Color.GRAY);
        g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,4,4);

        g2d.setComposite(AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, tmp_alpha));

        super.paintComponent(g);
    }


    private void fadeIn() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (fadeOutAnimator.isRunning()){
                    fadeOutAnimator.stop();
                }
                if (!fadeInAnimator.isRunning()){
                    fadeInAnimator.start();
                }
            }
        });
    }

    private void fadeOut() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (fadeInAnimator.isRunning()){
                    fadeInAnimator.stop();
                }
                if (!fadeOutAnimator.isRunning()){
                    fadeOutAnimator.start();
                }
            }
        });
    }

}
