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
package uk.ac.ebi.rcloud.common.components.tooltip;

import uk.ac.ebi.rcloud.common.components.panel.JPanelAlphaBase;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.LAFUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 12, 2010
 * Time: 3:14:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class StandardToolTip extends JPanelAlphaBase {

    private Color background = new Color(255, 250, 240); //0xfffff0
    public JLabel headline = new JLabel();
    public JTextPane textline = new JTextPane();
    public JButton closebutton = new JButton();

    public StandardToolTip() {
        alpha = 1.0f;

        modifyFontFize(headline, 2);
        modifyFontFize(textline, -2);

        setBorder(new EmptyBorder(5,20,0,5));
        setLayout(new BorderLayout());

        headline.setOpaque(false);
        textline.setOpaque(false);
        textline.setEditable(false);

        closebutton.setBorderPainted(false);
        closebutton.setContentAreaFilled(false);
        closebutton.setFocusable(false);
        closebutton.setIcon(new ImageIcon(ImageLoader.load("/images/tooltippanel/clear.png")));
        closebutton.setRolloverIcon(new ImageIcon(ImageLoader.load("/images/tooltippanel/clear_rollover.png")));
        closebutton.setPressedIcon(new ImageIcon(ImageLoader.load("/images/tooltippanel/clear_pressed.png")));

        JPanel buttonbox = new JPanel();
        buttonbox.setLayout(new BorderLayout());
        buttonbox.setOpaque(false);
        buttonbox.add(closebutton, BorderLayout.EAST);
        buttonbox.add(headline, BorderLayout.CENTER);

        JPanel textbox = new JPanel();
        textbox.setBorder(new EmptyBorder(0,0,20,20));
        textbox.setLayout(new BorderLayout());
        textbox.setOpaque(false);
        textbox.add(textline, BorderLayout.CENTER);


        add(buttonbox, BorderLayout.NORTH);
        add(textbox, BorderLayout.CENTER);

        setPreferredSize(new Dimension(350, 100));
    }

    public void modifyFontFize(JComponent c, int increment) {
        Font f0 = c.getFont();
        c.setFont(new Font(f0.getFamily(), f0.getStyle(), f0.getSize() + increment));
    }

    public void setText(String text) {
        textline.setText(text);
    }

    public void setHeadline(String text) {
        headline.setText(text);
    }

    public JButton getClosebutton() {
        return closebutton;
    }

    @Override
    public void paintComponent(Graphics g) {
    //public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Rectangle2D.Double rect =
                new Rectangle2D.Double(0,0,w,h);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, alpha));

        g2d.setColor(background);
        g2d.fill(rect);

        //g2d.setColor(Color.DARK_GRAY);
        //g2d.draw(rect);

        //super.paintComponents(g2d);
    }

    private static void initUI(){
        //System.setProperty("apple.awt.draggableWindowBackground", "true");

        LAFUtil.setupLookAndFeel();

        JFrame frame0 = new JFrame();
        StandardToolTip tippanel = new StandardToolTip();

        tippanel.setHeadline("Super Headline");
        tippanel.setText("A text message to see if everything works right.");

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(tippanel, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JButton exitButton = new JButton("exit");
        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        content.add(holder, BorderLayout.NORTH);
        content.add(exitButton, BorderLayout.SOUTH);

        frame0.setLayout(new BorderLayout());
        frame0.add(content, BorderLayout.CENTER);
        frame0.setSize(new Dimension(600,500));
        frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame0.setLocationRelativeTo(null);
        frame0.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                initUI();
            }
        });
    }

    /*
    // Create a mask for this dialog. This mask has the same shape as the
    // dialog's rounded balloon tip and ensures that only the balloon tip
    // part of the dialog will be visible. All other dialog pixels will
    // disappear because they correspond to transparent mask pixels.

    // Note: The drawing code is based on the drawing code in
    // RoundedBalloonBorder.

    Rectangle bounds = getBounds ();
    BufferedImage bi = new BufferedImage (bounds.width, bounds.height,
                                          BufferedImage.TYPE_INT_ARGB);
    Graphics g = bi.createGraphics ();
    g.fillRoundRect (0, 0, bounds.width, bounds.height-VERT_OFFSET,
                     ARC_WIDTH*2, ARC_HEIGHT*2);
    g.drawRoundRect (0, 0, bounds.width-1, bounds.height-VERT_OFFSET-1,
                     ARC_WIDTH*2, ARC_HEIGHT*2);
    int [] xPoints = { HORZ_OFFSET, HORZ_OFFSET+VERT_OFFSET, HORZ_OFFSET };
    int [] yPoints = { bounds.height-VERT_OFFSET-1, bounds.height-VERT_OFFSET
                       -1, bounds.height-1 };
    g.fillPolygon (xPoints, yPoints, 3);
    g.drawLine (xPoints [0], yPoints [0], xPoints [2], yPoints [2]);
    g.drawLine (xPoints [1], yPoints [1], xPoints [2], yPoints [2]);
    g.dispose ();
    WindowUtils.setWindowMask (this, new ImageIcon (bi));

    //AWTUtilities.setWindowShape (this, gp);

    */

    /*
    class TipFrame extends JDialog
    {
        final static int BORDER_WIDTH = 10;
        final static int BORDER_HEIGHT = BORDER_WIDTH;
        final static int HORZ_OFFSET = 15;
        final static int VERT_OFFSET = 20;
        final static int ARC_WIDTH = 7;
        final static int ARC_HEIGHT = 7;

        private BalloonTip bt;

        public TipFrame (JFrame parent) {
            super (parent);

            // The balloon tip is attached to the following component.

            JLabel lblNull = new JLabel ();
            getContentPane ().add (lblNull, BorderLayout.SOUTH);

            // Remove frame border and title bar from this dialog because they look
            // unsightly.

            setUndecorated (true);

            // Create the balloon tip attached to the label.

            bt = BalloonTip.createRoundedBalloonTip (lblNull, Color.BLACK,
                                                     new Color (255, 255, 225),
                                                     BORDER_WIDTH, HORZ_OFFSET,
                                                     VERT_OFFSET, ARC_WIDTH,
                                                     ARC_HEIGHT, false);

            // Because the following text is always used, it can be hard-coded. In
            // a more flexible TipFrame, a method would be provided to specify
            // arbitrary text.

            bt.setText ("Enter 1 - 130");

            // Size this dialog to the exact size required by the balloon tip.

            setSize (bt.getPreferredSize ().width, bt.getPreferredSize ().height);

            // Create a mask for this dialog. This mask has the same shape as the
            // dialog's rounded balloon tip and ensures that only the balloon tip
            // part of the dialog will be visible. All other dialog pixels will
            // disappear because they correspond to transparent mask pixels.

            // Note: The drawing code is based on the drawing code in
            // RoundedBalloonBorder.

            Rectangle bounds = getBounds ();
            BufferedImage bi = new BufferedImage (bounds.width, bounds.height,
                                                  BufferedImage.TYPE_INT_ARGB);
            Graphics g = bi.createGraphics ();
            g.fillRoundRect (0, 0, bounds.width, bounds.height-VERT_OFFSET,
                             ARC_WIDTH*2, ARC_HEIGHT*2);
            g.drawRoundRect (0, 0, bounds.width-1, bounds.height-VERT_OFFSET-1,
                             ARC_WIDTH*2, ARC_HEIGHT*2);
            int [] xPoints = { HORZ_OFFSET, HORZ_OFFSET+VERT_OFFSET, HORZ_OFFSET };
            int [] yPoints = { bounds.height-VERT_OFFSET-1, bounds.height-VERT_OFFSET
                               -1, bounds.height-1 };
            g.fillPolygon (xPoints, yPoints, 3);
            g.drawLine (xPoints [0], yPoints [0], xPoints [2], yPoints [2]);
            g.drawLine (xPoints [1], yPoints [1], xPoints [2], yPoints [2]);
            g.dispose ();
            WindowUtils.setWindowMask (this, new ImageIcon (bi));
        }

        void hideTip () {
            setVisible (false);
        }

        void moveTip (int x, int y) {
            setLocation (x, y-getSize ().height);
        }

        void showTip (int x, int y) {
            moveTip (x, y);
            setVisible (true);
        }
    }
    */



}
