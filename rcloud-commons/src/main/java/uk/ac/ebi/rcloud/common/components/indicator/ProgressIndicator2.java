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

import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 16, 2009
 * Time: 1:15:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressIndicator2 extends JLabel {

    public ProgressIndicator2 () {
        super();
        setIcon(new ImageIcon(ImageLoader.load("/images/progressindicator/ajax-loader.gif")));
    }

    public static void setupGUI(){
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        final ProgressIndicator2 indicator = new ProgressIndicator2();

        //final JSearchField search = new JSearchField();
        final JButton start = new JButton("start/stop");
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                if (indicator.isVisible()) {
                    indicator.setVisible(false);
                    //log.info("stopt");
                } else  {
                    indicator.setVisible(true);
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
