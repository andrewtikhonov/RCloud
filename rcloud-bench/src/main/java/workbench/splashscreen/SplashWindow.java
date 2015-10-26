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
package workbench.splashscreen;

import uk.ac.ebi.rcloud.common.graphics.effects.GaussianBlurFilter;
import uk.ac.ebi.rcloud.common.graphics.effects.ColorTintFilter;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import uk.ac.ebi.rcloud.common.animation.timing.Animator;
import uk.ac.ebi.rcloud.common.animation.timing.interpolation.PropertySetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.version.SoftwareVersion;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.event.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 13, 2009
 * Time: 2:55:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SplashWindow extends JFrame {
    final static private Logger log = LoggerFactory.getLogger(SplashWindow.class);
                                           
    private String label1 = "R Cloud";
    private String label2 = "Workbench";
    private String label3 = "Your Research Assistant\n" +
                            "\n";

    private String versionlabel = SoftwareVersion.getVersion();


    private int distanceduration = 4000;
    private int   xstart = 8;
    private int   xstop  = -8;
    private double xstep  = ((double) (xstop - xstart)) / distanceduration;
    private double xvalue = xstart;
    private double minvelocity = 10;
    private double distance = computeDistance(0, xstart);

    private int altduration   = 2000;
    private int   altrange = 30;
    private double altstart = 1.0f;
    private double altstop  = 0.0f;
    private double altstep  = ((double) (altstop - altstart)) / altduration;
    private double alt = altstart;

    private float tint =  1.0f;
    private float alpha1 = .0f;
    private float alpha2 = .0f;


    private int fps   = 30;
    private int delay = ((fps > 0) ? (1000 / fps) : 100);

    int width   = 600;
    int height  = 400;

    BufferedImage image;
    BufferedImage imageL1;
    BufferedImage imageL2;
    BufferedImage imageL3;
    BufferedImage imageVL;
    BufferedImage imageL1b;
    BufferedImage imageL2b;
    BufferedImage back;
    BufferedImage blackBack;

    WritableRaster blackBackRaster;

    Animator backFadeInAnimator;
    Animator backFadeOutAnimator;
    Animator labelFadeInAnimator1;
    Animator labelFadeInAnimator2;
    Animator labelFadeoutAnimator1;
    Animator labelFadeoutAnimator2;
    //Animator label3Animator11;

    Thread mainAnimator;
    Thread currentThread;

    private volatile boolean shutDownCommenced = false;

    public BufferedImage loadImage(String name) {
        try {
            return ImageIO.read(getClass().getResource(name));
        } catch (IOException ex) {
            log.error("Error!", ex);
            return null;
        }
    }

    public void init(long duration) {
        distanceduration = (int)(duration + (duration / 8));
        xstep  = ((double) (xstop - xstart)) / distanceduration;
    }

    private void fadeIn() {
        paintScreen();

        backFadeInAnimator.start();
        labelFadeInAnimator1.start();
        labelFadeInAnimator2.start();
        //label3Animator11.start();
        mainAnimator.start();
    }

    public void setTint(float tint) {
        this.tint = tint;
    }

    public void setAlpha1(float alpha1) {
        this.alpha1 = alpha1;
        //System.out.println("alpha1 = " + alpha1);
    }

    public void setAlpha2(float alpha2) {
        this.alpha2 = alpha2;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setAlt(double alt) {
        //log.info("setAlt-alt="+alt);
        this.alt = alt;
    }

    public double getSpeed(double x) {
        return x * x ;
    }

    public double computeDistance(int start, int stop) {
        double distance = 0;
        for(int i = start;i< stop;i++) {
            distance += getSpeed(i);
        }

        return distance;
    }

    private void fadeOut() {
        if (!shutDownCommenced){
            shutDownCommenced = true;

            backFadeInAnimator.stop();
            labelFadeInAnimator1.stop();
            labelFadeInAnimator2.stop();

            backFadeOutAnimator.setStartFraction(this.tint);
            labelFadeoutAnimator1.start();
            labelFadeoutAnimator2.start();
            backFadeOutAnimator.start();

            try { Thread.sleep(1000); } catch (Exception ex) {}

            setVisible(false);

            backFadeOutAnimator.stop();
            labelFadeoutAnimator1.stop();
            labelFadeoutAnimator2.stop();

            mainAnimator = null;

            try { Thread.sleep(100); } catch (Exception ex) {}

            dispose();

        }
    }

    private void initImages() {

        int border = 40;

        image   = loadImage("/images/DNA_by_padey89.jpg");

        int realImgWidth    = image.getWidth(null);
        int realImgHeight   = image.getHeight(null);
        int idealImgWidth   = width;
        int idealImgHeight  = idealImgWidth * realImgHeight / realImgWidth;

        int imgWidth        = idealImgWidth;
        int imgHeight       = idealImgHeight;

        image               = GraphicsUtilities.getScaledInstance(image, imgWidth, imgHeight,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR, true );

        back       = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Font font1 = new Font("Arial Black", Font.BOLD, 46);
        Color c1   = Color.RED;
        Color c2   = Color.RED.darker().darker().darker();
        int h1     = this.getFontMetrics(font1).getHeight() + border;
        GradientPaint paint1 = new GradientPaint(0, 0, c1, 0, h1, c2, false);

        imageL1     = renderLabel(label1, paint1, font1);

        Font font2 = new Font("Arial Black", Font.BOLD, 50);
        Color c3   = new Color(250, 250, 220, 200);
        Color c4   = new Color(250, 250, 220, 220);
        int h2 = this.getFontMetrics(font2).getHeight() + border;
        GradientPaint paint2    = new GradientPaint(0, 0, c3, 0, h2, c4, false);

        imageL2 = renderLabel(label2, paint2, font2);

        Font font4 = new Font("Arial Black", Font.BOLD, 24);
        Color c41   = new Color(250, 250, 220, 200);
        Color c42   = new Color(250, 250, 220, 220);
        int h4 = this.getFontMetrics(font2).getHeight() + border;
        GradientPaint paint4    = new GradientPaint(0, 0, c41, 0, h4, c42, false);

        imageL3 = renderLabel2(label3, paint4, font4);

        Font font3              = new Font("Arial Black", Font.BOLD, 20);
        Color c5                = Color.ORANGE;
        Color c6                = Color.WHITE;
        int h3 = this.getFontMetrics(font3).getHeight() + border;
        GradientPaint paint3    = new GradientPaint(0, 0, c5, 0, h3, c6, false);

        imageVL     = renderLabel(versionlabel, paint3, font3);

        GaussianBlurFilter blur = new GaussianBlurFilter(20);

        imageL1b = blur.filter(imageL1, null);
        imageL2b = blur.filter(imageL2, null);

        ColorTintFilter colorTint = new ColorTintFilter(new Color(250, 220, 220), 1.0f);
        //ColorTintFilter colorTint = new ColorTintFilter(Color.WHITE, 1.0f);

        imageL1b = colorTint.filter(imageL1b, null);
        imageL2b = colorTint.filter(imageL2b, null);

        blackBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = blackBack.createGraphics();

        g2d.setColor(Color.BLACK);

        g2d.fillRect(0, 0, blackBack.getWidth(), blackBack.getHeight());

        blackBackRaster = blackBack.copyData(null);

        g2d.dispose();
    }

    private void initAnimators() {

        backFadeInAnimator = PropertySetter.createAnimator(500, SplashWindow.this, "tint", 1.0f, .0f);

        backFadeOutAnimator = PropertySetter.createAnimator(400, SplashWindow.this, "tint", .0f, 1.0f);
        //backFadeOutAnimator.setDeceleration(0.1f);
        backFadeOutAnimator.setStartDelay(600);

        labelFadeInAnimator1 = PropertySetter.createAnimator(400, SplashWindow.this, "alpha1", .0f, 1.0f);
        labelFadeInAnimator1.setStartDelay(500);

        labelFadeInAnimator2 = PropertySetter.createAnimator(400, SplashWindow.this, "alpha2", .0f, 1.0f);
        labelFadeInAnimator2.setStartDelay(600);

        labelFadeoutAnimator1 = PropertySetter.createAnimator(600, SplashWindow.this, "alpha1", 1.0f, .0f);
        labelFadeoutAnimator2 = PropertySetter.createAnimator(300, SplashWindow.this, "alpha2", 1.0f, .0f);
        //labelFadeoutAnimator1.setDeceleration(0.1f);

        //label3Animator11 = PropertySetter.createAnimator(altduration, SplashWindow.this, "alt", altstart, altstop);
        //label3Animator11.setDeceleration(0.1f);


        mainAnimator = new Thread(new Runnable(){
            private double distance_b = 0;
            private double alt_b = 0;
            private double tint_b = 0;
            private double alpha1_b = 0;
            private double alpha2_b = 0;

            public void run(){

                long tm = System.currentTimeMillis();
                while (Thread.currentThread() == mainAnimator) {

                    if ((int)distance_b != (int)distance || (int)alt_b != (int) alt ||
                            tint_b != tint || alpha1_b != alpha1 || alpha2_b != alpha2) {

                        renderScene(back);
                        paintScreen();

                        distance_b = distance;
                        alt_b = alt;
                        tint_b = tint;
                        alpha1_b = alpha1;
                        alpha2_b = alpha2;

                    } else {
                        //log.info("NOTHING...");
                    } 

                    // Delay depending on how far we are behind.
                    try {
                        tm += delay;

                        long td = tm - System.currentTimeMillis();

                        if (td > 0){
                            Thread.sleep(td);
                        } else {
                            Thread.yield();
                        }

                        if (xvalue > xstop) {

                            //double v1 = getSpeed(xvalue);

                            xvalue += xstep * delay;

                            double velocity = Math.max(minvelocity, getSpeed(xvalue));

                            //double dv = v2 - v1;

                            //if (distance > distancestop) {
                            //distance += (distancestep * delay);



                            distance += velocity * xstep * delay;

                            //double distance_speed = getSpeed(xvalue);
                            //distance += distance_speed * xstep;

                            //log.info("xvalue="+xvalue+"xvalue="+xvalue+"velocity="+velocity+"distance="+distance);
                        }

                        //if (alt > altstop) {
                        //    alt += (altstep * delay);
                        //}

                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
    }


    public SplashWindow() {
        super();

        setLayout(null);
        setUndecorated(true);
        setSize(width, height);
        setLocationRelativeTo(null);

        WindowListener windowListener = new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                if (currentThread != null) {
                    currentThread.interrupt();
                }
            }
        };

        addWindowListener(windowListener);

        MouseListener mouseListener = new MouseAdapter(){
            public void mouseClicked(MouseEvent event){
                if (currentThread != null) {
                    currentThread.interrupt();
                }
            }
        };

        addMouseListener(mouseListener);

        initImages();
        initAnimators();

        setIgnoreRepaint(true);
        setVisible(true);
    }

    public void start(long showFor) {

        currentThread = Thread.currentThread();

        init(showFor);

        fadeIn();

        toFront();

        try { Thread.sleep(showFor); } catch (Exception ex) {}

        currentThread = null;

        fadeOut();
    }

    private BufferedImage renderLabel(String text, Paint p, Font f) {

        int b = 40;
        int w0 = this.getFontMetrics(f).stringWidth(text);
        int h0 = this.getFontMetrics(f).getHeight();
        int w1 = w0 + b;
        int h1 = h0 + b;

        BufferedImage image = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d      = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        //g2d.setColor(Color.DARK_GRAY);
        //g2d.fillRect(0,0,w,h);
        //f.getS


        g2d.setFont(f);
        g2d.setPaint(p);

        g2d.drawString(text, b/2, h0 + b/2 );
        g2d.dispose();

        return image;
    }

    private BufferedImage renderLabel2(String text, Paint p, Font f) {

        String[] arr = text.split("\n");

        int w0 = 0;

        for (String s : arr) {
            w0 = Math.max(w0, this.getFontMetrics(f).stringWidth(s));
        }


        int b = 20;
        int h0 = this.getFontMetrics(f).getHeight();
        int w1 = w0 + b;
        int h1 = h0 * arr.length + b;

        BufferedImage image = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d      = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(f);
        g2d.setPaint(p);

        int y = h0 + b/2;

        for (String s : arr) {
            if(s.length() > 0) {
                g2d.drawString(s, b/2, y );
            }

            y = y + h0;
        }

        g2d.dispose();

        return image;
    }


    private void renderScene(BufferedImage scene) {

        Graphics2D g2d = scene.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.drawImage(image, 0, 0, null);

        if (tint > 0) {
          g2d.setComposite(AlphaComposite.
                  getInstance(AlphaComposite.SRC_OVER, this.tint));

          g2d.drawImage(blackBack, 0, 0, null);

          //log.info("renderScene-tint="+tint);
        }

        //distance
        int x1 = 80 - (int) distance;
        int x2 = 220 + (int) distance;
        int x3 = 20;//(int) alt + 40;

        int y1 = getHeight()/2;
        int y2 = y1 + 60;
        int y3 = 80;
        //int y3 = (int) alt + 60;
        //int y3 = (int) distance/3 + 40;

        g2d.setComposite(AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, alpha1));

        g2d.drawImage(imageL1b, x1, y1, null);
        g2d.drawImage(imageL2b, x2, y2, null);
        g2d.drawImage(imageVL, 0, 0, null);

        g2d.setComposite(AlphaComposite.
                getInstance(AlphaComposite.SRC_OVER, alpha2));
                                                          
        g2d.drawImage(imageL1, x1, y1, null);
        g2d.drawImage(imageL2, x2, y2, null);
        //g2d.drawImage(imageL3, x3, y3, null);

        double v1 = alt * altrange / 5;
        double v2 = alt * altrange;
        double sinv = Math.sin(v2);
        double cosv = Math.cos(v2);

        g2d.drawImage(imageL3, x3, y3,
                imageL3.getWidth(), imageL3.getHeight(), null);

        //g2d.drawImage(imageL3, x3 + (int)(cosv * v1), y3 + (int)(sinv * v1),
        //        (int)(imageL3.getWidth() + sinv * v2), (int)(imageL3.getHeight() + cosv * v2), null);

        g2d.dispose();
    }

    private void paintScreen() {

        if (back != null && isShowing()){
            Graphics g = getGraphics();
            g.drawImage(back, 0,0, null);
            g.dispose();
        }
    }

    public static void main (String args[]) {
        new SplashWindow().start(5000);
    }

}
