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

import uk.ac.ebi.rcloud.common.util.ColorUtil;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.LAFUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 27/06/2011
 * Time: 16:02
 * To change this template use File | Settings | File Templates.
 */
public class JRoundButton extends JButton implements MouseListener {

    private boolean mouseover = false;
    private Color rolloverback;

    public JRoundButton(){
        super();
        init();
    }

    public JRoundButton (String title) {
        super(title);
        init();

    }

    private void init() {
        super.setBorderPainted(false);
        super.setContentAreaFilled(false);
        super.setFocusPainted(false);

        initListeners();

        //super.setBackground(super.getBackground().darker());
        super.setBackground(ColorUtil.darker(super.getBackground(), 30));
        //super.setBackground(new Color(150,200,250));
        //super.setBackground(ColorUtil.darker(new Color(100,80,80), 20));
        rolloverback = Color.ORANGE;
    }

    public void initListeners() {
        addMouseListener(this);
    }

    // mouse listener
    public void mouseEntered(MouseEvent event) {
        if (isEnabled()) {
            fadeIn();
        }
    }
    public void mouseExited(MouseEvent event) {
        fadeOut();
    }

    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}

    public void setRolloverBackground(Color c) {
        this.rolloverback = c;
    }

    public Color getRolloverBackground() {
        return this.rolloverback;
    }

    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (mouseover) {
            g2d.setColor(getRolloverBackground());
        } else {
            g2d.setColor(getBackground());
        }

        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        super.paintComponent(g);
    }

    private void fadeIn() {
        mouseover = true;
        //System.out.println("fadein");
        repaint();
    }

    private void fadeOut() {
        mouseover = false;
        //System.out.println("fadeout");
        repaint();
    }

    private static void initUI(){
        //System.setProperty("apple.awt.draggableWindowBackground", "true");

        LAFUtil.setupLookAndFeel();

        JFrame frame0 = new JFrame();

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JRoundButton exitButton = new JRoundButton("exit");
        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        JRoundButton testButton = new JRoundButton("Test");
        testButton.setIcon(new ImageIcon(ImageLoader.load("/images/searchpanel/zoom.png")));
        testButton.setBorderPainted(false);
        testButton.setContentAreaFilled(false);


        content.add(testButton, BorderLayout.NORTH);
        content.add(exitButton, BorderLayout.SOUTH);

        frame0.setTitle("test dialog");
        frame0.add(content, BorderLayout.CENTER);
        frame0.setPreferredSize(new Dimension(240,300));
        frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame0.setLocationRelativeTo(null);
        frame0.setVisible(true);
    }

    /*
    public void setDefaultCloseOperation(int op) {
    }

    public void setResizable(boolean resizable) {
    }

    public void setModal(boolean madal) {
    }

    public void setTitle(String title) {
    }
    */

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                initUI();
            }
        });
    }



}
