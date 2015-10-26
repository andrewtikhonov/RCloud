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
package uk.ac.ebi.rcloud.common.components.indicator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 15, 2009
 * Time: 6:54:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressIndicator extends JComponent {

    private int NUM = 8;
    private double RFRACTION = 0.15;
    private double DFRACTION = 0.3;

    private Color[] colors = new Color[NUM];
    private double[] radius = new double[NUM];

    public ProgressIndicator (){
        super();
        setColor(Color.DARK_GRAY.darker());
    }

    public ProgressIndicator (Color c){
        super();
        setColor(c);
    }

    private boolean stopanimator = false;

    class AnimatorRunnable implements Runnable {
        public void run(){
            while (Thread.currentThread() == animator && !stopanimator) {

                try {
                    Thread.sleep(100);
                } catch(InterruptedException ex) {
                }

                rotateColorTable();
                ProgressIndicator.this.repaint();
            }
        }
    }

    AnimatorRunnable animatorRunnable = new AnimatorRunnable();


    Thread animator = null;

    public boolean isRunning() {
        return animator != null && animator.isAlive();
    }

    public void start() {
        if (!isVisible()) {
            setVisible(true);
        }

        stopanimator = false;
        animator = new Thread(animatorRunnable);
        animator.start();
    }

    public void stop() {
        stopanimator = true;
        if (animator != null) {
            animator.interrupt();
            animator = null;
        }

        if (isVisible()) {
            setVisible(false);
        }
    }

    public void setColor(Color c) {
        initColorTable(c);
    }

    private void initColorTable(Color c) {

        double alphafraction = 400/NUM;
        double colorfraction = 0.7;

        for(int i=0;i<NUM;i++) {
            colors[i] = c;
            radius[i] = i * DFRACTION;

            //c = c.brighter();

            c = new Color((int)Math.min(c.getRed()/colorfraction, 255),
                    (int)Math.min(c.getGreen()/colorfraction, 255),
                    (int)Math.min(c.getBlue()/colorfraction, 255),
                    (int)Math.max(255 - i * alphafraction, 0));
        }
    }

    private void rotateColorTable() {
        Color c = colors[0];
        double r = radius[0];
        for(int i=0;i<NUM-1;i++) {
            colors[i] = colors[i+1];
            radius[i] = radius[i+1];
        }
        colors[NUM-1] = c;
        radius[NUM-1] = r;
    }

    private BufferedImage buff = null;

    public void paint(Graphics g) {

        if (buff == null || buff.getHeight() != getHeight() ||
                buff.getWidth() != getWidth()) {
            buff = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2d = buff.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);

        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setComposite(AlphaComposite.SrcOver);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        double twoPI = 2 * Math.PI;

        int h = getHeight();
        int w = getWidth();

        int centerx = w/2;
        int centery = h/2;

        int dim = Math.min(w, h);

        double r = dim * RFRACTION;
        double d = dim * DFRACTION;
        double anglefraction = twoPI/NUM;


        for (int i = 0;i < NUM;i++) {
            double angle = anglefraction * (-i);
            double yd = Math.sin(angle) * d;
            double xd = Math.cos(angle) * d;

            int cy = centery + (int) yd;
            int cx = centerx + (int) xd;

            int r0 = (int) r;//(int) (r - radius[i]);

            g2d.setColor(colors[i]);
            g2d.fillOval(cx, cy, r0, r0);
        }

        g.drawImage(buff,0,0,null);

    }

    public static void setupGUI(){
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        final ProgressIndicator indicator = new ProgressIndicator();

        //final JSearchField search = new JSearchField();
        final JButton start = new JButton("start/stop");
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                if (indicator.isRunning()) {
                    indicator.stop();
                    //log.info("stopt");
                } else  {
                    indicator.start();
                    //log.info("start");
                }
            }
        });

        //frame.add(search, BorderLayout.NORTH);

        indicator.setPreferredSize(new Dimension(20,20));

        frame.add(indicator, BorderLayout.CENTER);
        frame.add(start, BorderLayout.SOUTH);

        frame.setPreferredSize(new Dimension(500, 200));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable (){
            public void run(){
                setupGUI();
            }
        });
    }


}
