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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.graphics.effects.GaussianBlurFilter;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.LAFUtil;
import uk.ac.ebi.rcloud.common.util.WindowDragMouseAdapter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 11, 2009
 * Time: 3:22:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class JDialogExt extends JDialogBase {

    final private static Logger log = LoggerFactory.getLogger(JDialogExt.class);

    private int colorDelta = 0;

    private int borderColorGradient = 20;
    private int borderSize = 20;

    private Color backNormal;
    private Color backLighert;
    private Color backDarker;

    private Color dialogBack = new Color(230,230,230,0);

    private int border0 = 160;
    private int border1 = 200;

    private Color bordercolor0 = new Color(border0,border0,border0,255);
    private Color bordercolor1 = new Color(border1,border1,border1,255);

    private static int s = 20;

    private MyTitleBorder border;

    public JDialogExt(Frame owner, boolean modal) {
        super(owner, modal);
        setupUI();
    }

    public JDialogExt(Frame owner, String title) {
        super(owner, title);
        setupUI();
    }

    private static BufferedImage createTranslucentImage(int w, int h) {
        return new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    }

    private void initBackground() {
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

    private JPanel container;

    private void setupUI() {
        setUndecorated(true);

        //border = new MyTitleBorder(30,20,30,20);
        border = new MyTitleBorder(borderSize,borderSize,borderSize,borderSize);
        border.setBackground(getBackground());

        if (isDecorationSupported()) {

            backNormal = getBackground();

            backNormal = new Color(backNormal.getRed(),
                    backNormal.getGreen(),backNormal.getBlue(),backNormal.getAlpha());

            initBackground();

            super.setBackground(dialogBack);
            super.fixWindowDragging(this);

            initJDialogExtAnimators();

            JPanel container0 = new JPanel(new BorderLayout());
            container0.setOpaque(false);
            container0.setBorder(new CompoundBorder(new EmptyBorder(s,s,s,s), border));

            container = new JPanel() {
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

            container.setLayout(new BorderLayout());
            //container.setBorder(new MatteBorder(40,40,40,40, new Color(0,0,0,0)));
            //container.setOpaque(false);

            container0.add(container, BorderLayout.CENTER);

            super.setContentPane(container0);

        } else {
            ((JPanel)getContentPane()).setBorder(border);
        }

        WindowDragMouseAdapter handler = new WindowDragMouseAdapter();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public Container getContentPane() {
        if (isDecorationSupported()) {
            return container;
        } else {
            return super.getContentPane();
        }
    }

    public void setContentPane(Container pane) {
        super.setContentPane(pane);
        throw new RuntimeException("JDialogExt-setContentPane");
    }

    @Override
    public void setTitle(String title) {
        border.setTitle(title);
    }

    class MyTitleBorder extends EmptyBorder {

        protected String title = null;
        protected Color backgroundN = null;
        protected Color backgroundL = null;
        protected Color backgroundD = null;

        public MyTitleBorder(int top, int left, int bottom, int right, String title)   {
            super(top, left, bottom, right);
            setTitle(title);
        }

        public MyTitleBorder(int top, int left, int bottom, int right)   {
            this(top, left, bottom, right, "");
        }

        public void setBackground(Color background){

            backgroundN = background;

            this.backgroundL = new Color(Math.min(background.getRed()   + borderColorGradient, 255),
                                         Math.min(background.getGreen() + borderColorGradient, 255),
                                         Math.min(background.getBlue()  + borderColorGradient, 255),
                                         background.getAlpha());

            this.backgroundD = new Color(Math.min(background.getRed()   - borderColorGradient, 255),
                                         Math.min(background.getGreen() - borderColorGradient, 255),
                                         Math.min(background.getBlue()  - borderColorGradient, 255),
                                         background.getAlpha());
        }

        public void setTitle(String title){
            this.title = title;
            this.titleBuff = null;
        }

        private BufferedImage titleBuff;

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            int titleHeight = 60;

            if (titleBuff == null || titleBuff.getHeight() != height ||
                    titleBuff.getWidth() != width) {

                titleBuff = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = titleBuff.createGraphics();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);

                if (backgroundN != null && !isDecorationSupported()) {
                    int top = getBorderInsets().top;
                    int bot = getBorderInsets().bottom;

                    g2d.setPaint(new GradientPaint(0,0,backgroundL,0,top,backgroundN));
                    g2d.fillRect(0,0,width,top);

                    g2d.setPaint(new GradientPaint(0,height-bot,backgroundN,0,height,backgroundD));
                    g2d.fillRect(0,height-bot,width,height);

                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(0,0,width-1,height-1);

                }

                if (this.title != null && this.title.length() > 0) {
                    Font f0 = getFont();
                    Font f1 = new Font(f0.getFamily(), Font.BOLD, f0.getSize() + 1);

                    g2d.setFont(f1);

                    Rectangle2D bounds = f1.getStringBounds
                            (this.title, g2d.getFontRenderContext());

                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawString(this.title,
                            (width-(int)bounds.getWidth())/2,(titleHeight-(int)bounds.getHeight())/2);
                }

                g2d.dispose();
            }

            g.drawImage(titleBuff,x,y,null);
        }

        @Override
        public boolean isBorderOpaque(){
            return true;
        }
    }


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
        int widen = 3;
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

        GaussianBlurFilter blur = new GaussianBlurFilter(20);
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

    private static BufferedImage templateSquare = null;
    private static BufferedImage templateTall = null;
    private static BufferedImage templateWide = null;

    public static BufferedImage prepareShadowTemplate2(int w0, int h0) {

        int sh = 5;

        BufferedImage buffer = new BufferedImage(w0,h0, BufferedImage.TYPE_INT_ARGB);


        int x1 = 0 + s;//insets.left;
        int y1 = 0 + s + sh;//insets.top;
        int w1 = w0 - s*2;// - (insets.left*2) - insets.right;
        int h1 = h0 - s*2 - sh;// - (insets.top*2) - insets.bottom;

        Graphics2D g2d = buffer.createGraphics();

        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x1,y1,w1-1,h1-1,20,20);
        //Rectangle2D.Double rect = new Rectangle2D.Double(x1,y1,w1-1,h1-1);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0,0,0,70));
        g2d.fill(rect);

        GaussianBlurFilter blur = new GaussianBlurFilter(30);
        buffer = blur.filter(buffer, null);

        //ColorTintFilter tint = new ColorTintFilter(Color.BLUE, 1.0f);
        //buffer = tint.filter(buffer, null);

        g2d.dispose();

        return buffer;
    }

    private BufferedImage components;

    private Color transparent = new Color(0,0,0,0);

    public BufferedImage renderComponents() {
        int w = getWidth();
        int h = getHeight();

        if (components == null || components.getHeight() != h ||
                components.getWidth() != w) {
            components = createTranslucentImage(w,h);

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
            back = createTranslucentImage(w,h);

            Graphics2D g2d = back.createGraphics();

            Area a0 = new Area(new RoundRectangle2D.Double(x,y,w,h-2,20,20));
            //Area a0 = new Area(new Rectangle2D.Double(x,y,w,h-2));

            Component c = getContentPane();

            int x0 = c.getX() - s;
            int y0 = c.getY() - s;
            int w0 = c.getWidth() - 1;
            int h0 = c.getHeight() - 1;

            a0.subtract(new Area(new Rectangle2D.Double(x0,y0,w0,h0)));

            //g2d.setClip(rect);
            g2d.setClip(a0);

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
            g2d.drawRoundRect(x,y,w-1,h-1,20,20);
            //g2d.drawRect(x,y,w-1,h-1);

            g2d.setColor(bordercolor1);
            g2d.drawLine(x,y,w-1,y);

            g2d.dispose();
        }

        return back;
    }

    private BufferedImage shadow;

    public BufferedImage renderShadow2() {

        if (shadow == null || shadow.getHeight() != getHeight() ||
                shadow.getWidth() != getWidth()) {
            shadow = createTranslucentImage(getWidth(),getHeight());

            int sh = 5;
            int x = 0;
            int y = 0 + sh;
            int w = getWidth();
            int h = getHeight() - sh;

            Graphics2D g2d = shadow.createGraphics();

            BufferedImage template;

            if (w > h * 1.3) {
                template = templateWide;
            } else if (h > w * 1.3) {
                template = templateTall;
            } else {
                template = templateSquare;                
            }

            g2d.drawImage(template,x,y,w,h,null);
            //g2d.drawImage(template,x,y,w,h,null);
            //g2d.drawImage(template,x,y,w,h,null);

            g2d.dispose();
        }


        return shadow;
    }

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


            g2d.dispose();
        }


        return shadow;
    }

    /*
    private BufferedImage borderBuff;
    public BufferedImage renderBorder() {
        int x = 0;//insets.left;
        int y = 0;//insets.top;
        int w = getWidth() - s*2;// - (insets.left*2) - insets.right;
        int h = 50;// - (insets.top*2) - insets.bottom;

        if (borderBuff == null || borderBuff.getHeight() != h ||
                borderBuff.getWidth() != w) {
            borderBuff = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = borderBuff.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);

            border.paintBorder(this, g2d, 0, 0, getWidth(), getHeight());

            g2d.dispose();
        }

        return borderBuff;
    }
    */

    //@Override
    public void paint(Graphics g) {
        if (isDecorationSupported()) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int x = 0 + s;
            int y = 0 + s;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g2d.drawImage(renderShadow(), 0, 0, null);
            g2d.drawImage(renderBackground(), x, y, null);
            //g2d.drawImage(renderBorder(), 0, 0, null);

            border.paintBorder(this, g2d, 0, 0, getWidth(), getHeight());

            if (alpha == 1) {
                super.paintComponents(g2d);
            } else {
                g2d.drawImage(renderComponents(), 0, 0, null);
            }
        } else {
            super.paint(g);
        }
    }

    public boolean isMacOs() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }


    public void setVisible(boolean visible) {
        if (isDecorationSupported()) {
            if (visible) {
                setIgnoreRepaint(true);
                alpha = .0f;
                if (fadeOutAnimator.isRunning()) {
                    fadeOutAnimator.stop();
                }
                fadeInAnimator.start();
            } else {
                /*
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
                */


                //if (fadeInAnimator.isRunning()) {
                //    fadeInAnimator.stop();
                //}
                //fadeOutAnimator.start();
            }
        }
        super.setVisible(visible);
    }

    private float alpha = .0f;
    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;
    private int animationtime = 300;

    public void initJDialogExtAnimators(){
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

    private static void initUI2(){

        LAFUtil.setupLookAndFeel();

        JDialogExt dialog = new JDialogExt(new JFrame(), true);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

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
        dialog.setSize(new Dimension(240,300));
        dialog.setPreferredSize(new Dimension(240,300));
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
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
