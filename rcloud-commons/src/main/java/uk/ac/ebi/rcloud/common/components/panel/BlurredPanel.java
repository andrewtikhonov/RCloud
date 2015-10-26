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

import uk.ac.ebi.rcloud.common.animation.timing.TimingTargetAdapter;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import uk.ac.ebi.rcloud.common.graphics.effects.GaussianBlurFilter;
import uk.ac.ebi.rcloud.common.components.panel.JPanelA;
import uk.ac.ebi.rcloud.common.animation.timing.interpolation.PropertySetter;
import uk.ac.ebi.rcloud.common.animation.timing.Animator;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 25, 2009
 * Time: 3:05:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlurredPanel extends JPanelAlphaBase {

    private BufferedImage backBuffer;
    private BufferedImage blurBuffer;
    private JComponent container;
    private int animationtime = 400;

    private boolean createblur = true;

    boolean painted = false;

    public BlurredPanel(JComponent container){

        this.container  = container;

        /*
        MouseListener listener = new MouseListener() {
            public void mouseClicked(MouseEvent e)  { if (panel.isShowing()) e.consume(); }
            public void mouseEntered(MouseEvent e)  { }
            public void mouseExited(MouseEvent e)   { }
            public void mousePressed(MouseEvent e)  { if (panel.isShowing()) e.consume(); }
            public void mouseReleased(MouseEvent e) { if (panel.isShowing()) e.consume(); }
        };

        this.addMouseListener(listener);
        */
    }

    /*
    cbo.setLightWeightPopupEnabled(false);
    try {
	    Class cls = Class.forName("javax.swing.PopupFactory");
	    Field field = cls.getDeclaredField("forceHeavyWeightPopupKey");
	    field.setAccessible(true);
	    cbo.putClientProperty(field.get(null), Boolean.TRUE);
    } catch (Exception e1) {e1.printStackTrace();}
    */

    public BlurredPanel(){
        this(null);
    }

    public void setContainer(JComponent container) {
        this.container = container;
    }

    public void createBlur() {

        backBuffer = GraphicsUtilities.createCompatibleImage(getWidth(), getHeight());
        blurBuffer = GraphicsUtilities.createCompatibleImage(getWidth(), getHeight());

        Graphics2D backG2D = backBuffer.createGraphics();
        Graphics2D blurG2D = blurBuffer.createGraphics();

        JRootPane root = SwingUtilities.getRootPane(this);

        root.paint(backG2D);

        blurG2D.drawImage(backBuffer, 0, 0, null);

        GaussianBlurFilter blur2 = new GaussianBlurFilter(4);

        blurBuffer = blur2.filter(blurBuffer, null);

        /*
        float[] matrix = {
                0.111f, 0.111f, 0.111f,
                0.111f, 0.111f, 0.111f,
                0.111f, 0.111f, 0.111f
        };

        BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, matrix) );
        blurBuffer = op.filter(blurBuffer, null);
        */


        backG2D.dispose();
        blurG2D.dispose();
    }

    public void setCreateblur(boolean createblur) {
        this.createblur = createblur;
    }

    public void paintComponent(Graphics g) {

        if (createblur == false) return;

        if (isVisible() && blurBuffer != null)
        {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                 RenderingHints.VALUE_INTERPOLATION_BILINEAR);


            g2d.drawImage(backBuffer, 0, 0, null);

            AlphaComposite aComposite = AlphaComposite.
                    getInstance(AlphaComposite.SRC_OVER, alpha);

            g2d.setComposite(aComposite);

            g2d.drawImage(blurBuffer, 0, 0, null);

            g2d.dispose();
        }
    }

    public void fadeIn() {
        createBlur();

        setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Animator animator = PropertySetter.createAnimator(
                    animationtime, BlurredPanel.this, "alpha", 1.0f);
                animator.setAcceleration(0.2f);
                animator.setDeceleration(0.3f);
                animator.addTarget(
                    new PropertySetter(container, "alpha", 1.0f));
                animator.start();
            }
        });

        painted = true;
    }

    public boolean isPainted() {
        return false;
    }


    public void fadeOut() {
        painted = false;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Animator animator = PropertySetter.createAnimator(
                    animationtime - 100, container, "alpha", 1.0f, .0f);
                animator.setAcceleration(0.2f);
                animator.setDeceleration(0.3f);
                animator.addTarget(
                    new PropertySetter(BlurredPanel.this, "alpha", 1.0f, .0f));
                animator.addTarget(new TimingTargetAdapter() {
                    @Override
                    public void end() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                setVisible(false);
                            }
                        });
                    }
                });

                animator.start();
            }
        });
    }

}
