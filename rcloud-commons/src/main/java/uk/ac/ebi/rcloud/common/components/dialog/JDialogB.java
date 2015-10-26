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
package uk.ac.ebi.rcloud.common.components.dialog;

import uk.ac.ebi.rcloud.common.animation.timing.Animator;
import uk.ac.ebi.rcloud.common.animation.timing.interpolation.PropertySetter;
import uk.ac.ebi.rcloud.common.graphics.effects.GaussianBlurFilter;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.WindowDragMouseAdapter;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 13, 2010
 * Time: 1:36:14 PM
 * To change this template use File | Settings | File Templates.
 */

public class JDialogB extends JDialog { // JWindow

    private int colorDelta = 30;

    private Color backNormal;
    private Color backLighert;
    private Color backDarker;

    private int border0 = 160;
    private int border1 = 200;

    private Color bordercolor0 = new Color(border0,border0,border0,255);
    private Color bordercolor1 = new Color(border1,border1,border1,255);

    private Color frameBack = new Color(230,230,230,0);

    private static int s = 0;

    public JDialogB() {
        //super(owner, modal);
        super();
        setupUI();
    }

    public JDialogB(Frame owner, boolean modal) {
        super(owner, modal);
        setupUI();
    }

    public JDialogB(Frame owner, String title) {
        super(owner, title);
        setupUI();
    }

    JPanel newContentPane = null;
    JPanel repainter = null;

    private void initBackgroundColors() {
        int r0 = backNormal.getRed();
        int g0 = backNormal.getGreen();
        int b0 = backNormal.getBlue();
        int a = backNormal.getAlpha(); //backAlpha;//

        int r1 = Math.min(r0 + colorDelta, 255);
        int g1 = Math.min(g0 + colorDelta, 255);
        int b1 = Math.min(b0 + colorDelta, 255);

        int r2 = Math.max(r0 - colorDelta, 0);
        int g2 = Math.max(g0 - colorDelta, 0);
        int b2 = Math.max(b0 - colorDelta, 0);


        backNormal = new Color(r0, g0, b0, a);
        backLighert = new Color(r1, g1, b1, a);
        backDarker = new Color(r2, g2, b2, a);
    }

    private static final String WDRAG = "apple.awt.draggableWindowBackground";
    private void fixWindowDragging(Window w) {
        if (w instanceof RootPaneContainer) {
            JRootPane p = ((RootPaneContainer)w).getRootPane();
            Boolean oldDraggable = (Boolean)p.getClientProperty(WDRAG);
            if (oldDraggable == null) {
                p.putClientProperty(WDRAG, Boolean.FALSE);
                if (w.isDisplayable()) {
                    String context = "-context-";
                    System.err.println(context + "(): To avoid content dragging, " + context + "() must be called before the window is realized, or " + WDRAG + " must be set to Boolean.FALSE before the window is realized.  If you really want content dragging, set " + WDRAG + " on the window's root pane to Boolean.TRUE before calling " + context + "() to hide this message.");
                }
            }
        }
    }

    private void setupUI() {
        setUndecorated(true);

        setDefaultLookAndFeelDecorated(true);

        backNormal = getBackground(); // Color.GRAY;
        //backNormal = Color.LIGHT_GRAY;

        super.setBackground(new Color(0,0,0,0));

        fixWindowDragging(this);

        initBackgroundColors();
        initJDialogAAnimators();

        //super.setBackground(frameBack);

        //JDialog dialog = new JDialog();
        //setDefaultLookAndFeelDecorated(false);
        //getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
        //UIManager.getUI()

        WindowDragMouseAdapter handler = new WindowDragMouseAdapter();

        addMouseListener(handler);
        addMouseMotionListener(handler);

        MyContainer dialogPainter = new MyContainer();
        dialogPainter.setLayout(new BorderLayout());
        dialogPainter.setBorder(new MatteBorder(s+20,s+20,s+20,s+20, new Color(0,0,0,0)));

        /*
        repainter = new JPanel() {
            Color c0 = null;
            @Override
            public void paint (Graphics g) {
                //log.info("Container");

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Composite c = g2d.getComposite();
                if (c instanceof AlphaComposite && ((AlphaComposite)c).getAlpha() == 1) {
                    if (c0 == null) {
                        Color b0 = getBackground();
                        c0 = new Color (b0.getRed(), b0.getGreen(), b0.getBlue(), b0.getAlpha());
                    }
                    g2d.setColor(c0);
                    g2d.fillRect(0,0,getWidth()-1,getHeight()-1);
                    super.paint(g2d);
                }
            }
        };

        //newContentPane = new JPanel();
        //newContentPane.setOpaque(false);

        repainter.setLayout(new BorderLayout());
        //repainter.add(newContentPane, BorderLayout.CENTER);

        dialogPainter.add(repainter, BorderLayout.CENTER);

        super.setContentPane(dialogPainter);
        */

        setContentPane(dialogPainter);
    }

    /*
    public Container getContentPane() {
        return repainter;
    }

    public void setContentPane(Container pane) {
        super.setContentPane(pane);
        //repainter.removeAll();
        //repainter.add(pane, BorderLayout.CENTER);
    }
    */

    private static BufferedImage[] imageMap = new BufferedImage[9];

    private static final int LEFTTOP = 0;
    private static final int TOP = 1;
    private static final int RIGHTTOP = 2;
    private static final int LEFT = 3;
    private static final int CENTER = 4;
    private static final int RIGHT = 5;
    private static final int LEFTBOTTOM = 6;
    private static final int BOTTOM = 7;
    private static final int RIGHTBOTTOM = 8;

    static {
        prepareShadowTemplate();
    }

    private static final int PART1 = 50;  // <-----

    public static void prepareShadowTemplate() {

        int h0 = PART1 * 3;
        int w0 = PART1 * 3;

        int shiftdown = 10;
        int widen = 5;
        int x1 = s - widen;
        int y1 = s;
        int w1 = w0 - s*2 + widen*2;
        int h1 = h0 - s*2;

        BufferedImage buffer = new BufferedImage(w0,h0, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = buffer.createGraphics();

        g2d.translate(0, shiftdown);

        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x1,y1,w1-1,h1-1,20,20);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0,0,0,70));
        g2d.fill(rect);

        GaussianBlurFilter blur = new GaussianBlurFilter(10);
        buffer = blur.filter(buffer, null);

        //ColorTintFilter tint = new ColorTintFilter(Color.BLUE, 1.0f);
        //buffer = tint.filter(buffer, null);

        int rows = 3;
        int cols = 3;

        g2d.dispose();

        int w = PART1;
        int h = PART1;
        int i = 0;

        for(int y = 0; y < rows; y++) {
            for(int x = 0; x < cols; x++) {
                imageMap[i] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                Graphics2D g = imageMap[i].createGraphics();

                g.drawImage(buffer, 0, 0, w, h, w*x, h*y, w*x+w, h*y+h, null);
                g.dispose();
                i++;
            }
        }
    }

    class MyContainer extends JPanel {

        private BufferedImage components;

        public BufferedImage renderComponents() {
            int w = getWidth();
            int h = getHeight();

            if (components == null || components.getHeight() != h ||
                    components.getWidth() != w) {
                components = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = components.createGraphics();
                super.paintComponents(g2d);
                g2d.dispose();
            }

            //g2d.setColor(transparent);
            //g2d.clearRect(0,0,w,h);

            return components;
        }

        private BufferedImage back;
        private BasicStroke borderstroke = new BasicStroke(4);

        public BufferedImage renderBackground() {

            int x = 0;//insets.left;
            int y = 0;//insets.top;
            int w = getWidth() - s*2;// - (insets.left*2) - insets.right;
            int h = getHeight() - s*2;// - (insets.top*2) - insets.bottom;

            if (back == null || back.getHeight() != h ||
                    back.getWidth() != w) {
                back = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = back.createGraphics();

                /*
                Area a0 = new Area(new Rectangle2D.Double(x,y,w,h-2));

                Component c = getContentPane();

                int x0 = c.getX();// - s;
                int y0 = c.getY();// - s;
                int w0 = c.getWidth() - 1;
                int h0 = c.getHeight() - 1;

                a0.subtract(new Area(new Rectangle2D.Double(x0,y0,w0,h0)));

                //g2d.setClip(rect);
                g2d.setClip(a0);
                */

                RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x,y,w-1,h-1,20,20);

                g2d.setClip(rect);

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);

                int bord = 20;

                //fill
                g2d.setColor(backNormal);
                g2d.fillRect(0,bord,w-1,h-bord*2);

                g2d.setPaint(new GradientPaint(0,0,backLighert,0,bord,backNormal));
                g2d.fillRect(0,1,w-1,bord-1);

                g2d.setPaint(new GradientPaint(0,h-bord,backNormal,0,h,backDarker));
                g2d.fillRect(0,h-bord,w-1,bord-2);

                // paint border
                g2d.setColor(bordercolor0);
                g2d.setStroke(borderstroke);
                g2d.draw(rect);
                //g2d.drawRoundRect(x,y,w-1,h-1,20,20);

                //g2d.setColor(bordercolor1);
                //g2d.drawLine(x,y,w-1,y);

                g2d.dispose();
            }

            return back;
        }

        private BufferedImage shadow;

        public BufferedImage renderShadow() {

            if (shadow == null || shadow.getHeight() != getHeight() ||
                    shadow.getWidth() != getWidth()) {
                shadow = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);

                int sh = 5;
                int x = 0;
                int y = 0 + sh;
                int w = getWidth();
                int h = getHeight() - sh;

                Graphics2D g2d = shadow.createGraphics();

                // left-top
                g2d.drawImage(imageMap[LEFTTOP],
                        0,0,PART1,PART1, // dest
                        0,0,PART1,PART1, // source
                        null);

                g2d.translate(0, h-PART1);


                // left-bottom
                g2d.drawImage(imageMap[LEFTBOTTOM],
                        0,0,PART1,PART1, // dest
                        0,0,PART1,PART1, // source
                        null);

                g2d.translate(w-PART1, 0);

                // right-bottom
                g2d.drawImage(imageMap[RIGHTBOTTOM],
                        0,0,PART1,PART1, // dest
                        0,0,PART1,PART1, // source
                        null);

                g2d.translate(0, -(h-PART1));

                // right-top
                g2d.drawImage(imageMap[RIGHTTOP],
                        0,0,PART1,PART1, // dest
                        0,0,PART1,PART1, // source
                        null);

                Rectangle r = new Rectangle(0, 0, PART1, PART1);

                int centerW = w-PART1*2;
                int centerH = h-PART1*2;



                // top
                g2d.translate(-centerW, 0);

                Rectangle rect0 = new Rectangle(0,0,centerW,PART1);
                g2d.setPaint(new TexturePaint(imageMap[TOP], r));
                g2d.fill(rect0);

                // center
                g2d.translate(0, PART1);

                Rectangle rect1 = new Rectangle(0,0,centerW,centerH);
                g2d.setPaint(new TexturePaint(imageMap[CENTER], r));
                g2d.fill(rect1);

                // bottom
                g2d.translate(0, centerH);

                g2d.setPaint(new TexturePaint(imageMap[BOTTOM], r));
                g2d.fill(rect0);

                // left
                g2d.translate(-PART1, -centerH);

                Rectangle rect3 = new Rectangle(0,0,PART1,centerH);
                g2d.setPaint(new TexturePaint(imageMap[LEFT], r));
                g2d.fill(rect3);

                // right
                g2d.translate(centerW+PART1, 0);

                g2d.setPaint(new TexturePaint(imageMap[RIGHT], r));
                g2d.fill(rect3);



                /*

                Rectangle rect = new Rectangle(5,5,200,200);

                g2.fill(rect);
                */


                g2d.dispose();
            }


            return shadow;
        }

        //@Override
        public void paint(Graphics g) {

            Graphics2D g2d = (Graphics2D) g;// getMyGraphics();

            //g2d.setColor(new Color(0,0,0,0));
            //g2d.fillRect(0,0,getWidth(),getHeight());

            int x = 0 + s;
            int y = 0 + s;

            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            //g2d.drawImage(renderShadow(), 0, 0, null);
            g2d.drawImage(renderBackground(), x, y, null);

            if (alpha == 1) {
                super.paintComponents(g2d);
            } else {
                g2d.drawImage(renderComponents(), 0, 0, null);
            }
        }

    }

    public void setSize(Dimension size) {
        setPreferredSize(size);
    }

    public void  setPreferredSize(Dimension preferredSize) {
        //WindowUtils.setWindowMask(this, new RoundRectangle2D.Double(0, 0,
        //        preferredSize.width, preferredSize.height, 20, 20));
        super.setSize(preferredSize);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setIgnoreRepaint(true);
            alpha = .0f;
            repaint();
            if (fadeOutAnimator.isRunning()) {
                fadeOutAnimator.stop();
            }
            fadeInAnimator.start();
        } else {
            //if (fadeInAnimator.isRunning()) {
            //    fadeInAnimator.stop();
            //}
            //fadeOutAnimator.start();
        }

        super.setVisible(visible);

        //WindowUtils.setWindowAlpha(this, 1.0f);

    }


    private float alpha = .0f;
    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;
    private int animationtime = 300;

    public void initJDialogAAnimators(){
        fadeInAnimator = PropertySetter.createAnimator(
                animationtime, this, "alpha", .0f, 1.0f);
        fadeInAnimator.setAcceleration(0.2f);
        fadeInAnimator.setDeceleration(0.3f);

        fadeOutAnimator = PropertySetter.createAnimator(
                animationtime, this, "alpha", 1.0f, .0f);
        fadeOutAnimator.setAcceleration(0.2f);
        fadeOutAnimator.setDeceleration(0.3f);
    }

    // alpha setter
    public void setAlpha(float alpha){
        this.alpha = alpha;
        if (isShowing()) repaint();
    }

    private static void initUI(){
        System.setProperty("apple.awt.draggableWindowBackground", "true");

        JDialogB dialog = new JDialogB();

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(true);

        JButton exitButton = new JButton("exit");
        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        JTextField textField = new JTextField("dsfg");
        textField.setEditable(true);
        textField.setFocusable(true);
        textField.setEnabled(true);

        content.add(new JButton("test"), BorderLayout.NORTH);
        content.add(textField, BorderLayout.CENTER);
        content.add(exitButton, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setPreferredSize(new Dimension(240,300));
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //WindowUtils.setWindowMask(dialog, new RoundRectangle2D.Double(0, 0, 250, 320, 20, 20)); //createShape() //new ImageIcon(buff)
        dialog.setLocationRelativeTo(null);

        dialog.setVisible(true);
        //dialog.setWindowAlpha(0.5f);
        //WindowUtils.setWindowAlpha(dialog, 1.0f);

    }

    private static void initUI2(){
        //System.setProperty("apple.awt.draggableWindowBackground", "true");

        JDialogB dialog = new JDialogB(new JFrame(), "");

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(true);

        JButton exitButton = new JButton("exit");
        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        JTextField textField = new JTextField("dsfg");
        textField.setEditable(true);
        textField.setFocusable(true);
        textField.setEnabled(true);

        JButton testButton = new JButton("Test");
        testButton.setIcon(new ImageIcon(ImageLoader.load("/images/searchpanel/zoom.png")));
        testButton.setBorderPainted(false);
        testButton.setContentAreaFilled(false);


        content.add(testButton, BorderLayout.NORTH);
        content.add(textField, BorderLayout.CENTER);
        content.add(exitButton, BorderLayout.SOUTH);

        dialog.add(content, BorderLayout.CENTER);
        //dialog.setSize(new Dimension(240,300));
        //dialog.setPreferredSize(new Dimension(240,300));
        //dialog.pack();
        dialog.setPreferredSize(new Dimension(240,300));
        //dialog.setSize();

        /*
        249         final Window me = this;
        250         SwingUtilities.invokeSync(new Runnable JavaDoc() {
        251             public void run() {
        252                 Dimension d = rootPane.getContentPane().getPreferredSize();
        253                 d.width += FRAME_DECORATION_WIDTH;
        254                 d.height += FRAME_DECORATION_HEIGHT;
        255                 peer.setSize( d.width, d.height );
        256             }
        257         });
        */



        //dialog.setPreferredSize(new Dimension(240,300));
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                initUI2();
            }
        });
    }
}
