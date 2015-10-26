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

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 7, 2009
 * Time: 4:28:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class JHintPasswordField extends JPasswordField {

    private String hint = "";

    public JHintPasswordField(String text, String hint){
        super(text);
        setHint(hint);
        initComponent();
    }

    public JHintPasswordField(String hint){
        super();
        setHint(hint);
        initComponent();
    }

    public JHintPasswordField(){
        super();
        setHint("");
        initComponent();
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    int leftGap = 0;

    public void initComponent() {
        this.setBorder(new CompoundBorder(this.getBorder(), new EmptyBorder(0,0,0,0)));
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (super.getPassword().length == 0) { //&& !super.hasFocus()
            final int h = g2d.getFontMetrics().getHeight();
            final int x1 = leftGap + 7;
            final int y1 = (getHeight() + h)/2 - 3; //19


            g2d.setColor(Color.GRAY);
            g2d.drawString(hint, x1, y1);
        }
    }

    public static void setupGUI(){
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        final JHintPasswordField hintpassword = new JHintPasswordField("Choose password");
        final JTextField text = new JTextField();

        frame.add(hintpassword, BorderLayout.NORTH);
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
