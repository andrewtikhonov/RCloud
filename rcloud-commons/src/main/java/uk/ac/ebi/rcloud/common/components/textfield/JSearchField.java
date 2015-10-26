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
package uk.ac.ebi.rcloud.common.components.textfield;

import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 2, 2009
 * Time: 11:02:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class JSearchField extends JTextField {

    private ImageIcon findIcon;

    public JSearchField(){
        super();
        initComponent();
    }

    private int leftGap = 20;
    private int rightGap = 5;

    public void initComponent() {
        findIcon = new ImageIcon(ImageLoader.load("/images/searchpanel/zoom.png"));
        this.setBorder(new CompoundBorder(this.getBorder(), new EmptyBorder(0,leftGap,0,rightGap)));

    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int x = 8;
        final int y = (getHeight() - findIcon.getIconHeight() + 1)/2;

        g2d.drawImage(findIcon.getImage(), x, y, null);

        if (super.getText().length() == 0 && !super.hasFocus()) {
            final int h = g2d.getFontMetrics().getHeight();
            final int x1 = leftGap + 7;
            final int y1 = (getHeight() + h)/2 - 3; //19


            g2d.setColor(Color.GRAY);
            g2d.drawString("Search..", x1, y1);
        }
    }

    public static void setupGUI(){
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        final JSearchField search = new JSearchField();
        final JTextField text = new JTextField();

        frame.add(search, BorderLayout.NORTH);
        frame.add(text, BorderLayout.SOUTH);

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

