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

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 21, 2009
 * Time: 3:11:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class JPanelExt extends JPanel {

    float   alpha = 1.0f;

    int     drop         = 0;
    int     borderArcW   = 10;
    int     borderArcH   = 10;
    int     panelArcW    = 20;
    int     panelArcH    = 20;
    int     borderStroke = 0;
    Color background1    = new Color(60, 60, 60, 180);
    Color background0    = new Color(255,255,255,200);
    //Color background0    = new Color(120, 120, 120, 120);

    Color background2    = new Color(100,100,100,100);

    Color bordercolor    = Color.DARK_GRAY;
    boolean gradient     = false;

    BufferedImage compBuffer;

    public JPanelExt(){
        super();
    }

    public JPanelExt(LayoutManager manager){
        super(manager);
    }

    public void setGradient(boolean g){
        gradient = g;

        if (isShowing()) { repaint(); }
    }

    public void setBackground(Color c){
        background0 = c;

        if (isShowing()) { repaint(); }
    }

    public Color getBackground(){
        return background0;
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


    public void setAlpha(float alpha){
        this.alpha = alpha;
        if (isShowing()) {
            repaint();
        }
    }

    public float getAlpha(){
        return alpha;
    }

    public BufferedImage renderComponent(BufferedImage buffer) {

        int indent, x, y, w, h;

        Graphics2D g2d = buffer.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (gradient) {
            GradientPaint bgGradient = new GradientPaint(
                    0, 0, background0,
                    0, this.getHeight(), background1, true);


            g2d.setPaint(bgGradient);
        } else {
            g2d.setColor(background0);
        }

        indent = borderStroke;
        x = indent;
        y = indent + drop;
        w = getWidth() - (indent)* 2 - 1;
        h = getHeight() - (indent) * 2 - drop - 2;


        g2d.fillRoundRect(x, y, w, h, panelArcW, panelArcH);

        g2d.dispose();

        return buffer;
    }

    public void renderComponent2(Graphics2D g2d) {

        int indent, x, y, w, h;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (gradient) {
            GradientPaint bgGradient = new GradientPaint(
                    0, 0, background0,
                    0, this.getHeight(), background1, true);


            g2d.setPaint(bgGradient);
        } else {
            g2d.setColor(background0);
        }

        indent = borderStroke;
        x = indent;
        y = indent + drop;
        w = getWidth() - (indent)* 2 - 1;
        h = getHeight() - (indent) * 2 - drop - 2;

        g2d.fillRoundRect(x, y, w, h, panelArcW, panelArcH);

        g2d.setColor(background2);
        g2d.drawRoundRect(x, x, w, h, panelArcW, panelArcH);
    }

    @Override
    public void paintComponent(Graphics g) {

        /*
        if (true || compBuffer == null ||
                compBuffer.getWidth() != getWidth() ||
                compBuffer.getHeight() != getHeight()) {

            compBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            compBuffer = renderComponent(compBuffer);
        }
        */

        Graphics2D g2d = (Graphics2D) g;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        renderComponent2(g2d);

        /*
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
                */

        //g.drawImage(compBuffer, 0, 0, null);

        super.paintComponents(g);
    }

    public static class JFrameExt extends JFrame {
        public JFrameExt() {
        }

        public void initFrame() {
            this.frameInit();
        }

        public void updateSuper() {
            super.update(super.getGraphics());
        }
    }

    public static void main(String[] args) {
        final JFrameExt frame = new JFrameExt();

        final JPanelExt panel = new JPanelExt();
        panel.setLayout(new BorderLayout());

        JPanel p0 = new JPanel(new FlowLayout());
        p0.add(new JButton("OK"));

        panel.add(p0, BorderLayout.NORTH);
        panel.setPreferredSize(new Dimension(500, 400));

        frame.setLayout(null);
        frame.setBackground(new Color(255,255,255,0));
        frame.setContentPane(panel);

        frame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

}
