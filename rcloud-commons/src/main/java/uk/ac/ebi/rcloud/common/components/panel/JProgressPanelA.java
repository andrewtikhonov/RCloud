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

import uk.ac.ebi.rcloud.common.components.dialog.JDialogExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 10, 2009
 * Time: 2:40:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class JProgressPanelA extends JPanel { //JPanelA {

    private JLabel       progressLabel;
    private JLabel       tooltipLabel;
    private JProgressBar progressBar;
    private JButton      cancelButton;
    private boolean      eventUnsafe = false;

    private Runnable     cancelRunnable = new Runnable(){
        public void run(){
        }
    };

    public JProgressPanelA() {
        initUI();
    }

    public void setCancelAction(Runnable action) {
        cancelRunnable = action;
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        tooltipLabel  = new JLabel(" ");
        progressLabel = new JLabel(" ");
        progressBar  = new JProgressBar();
        cancelButton = new JButton("Cancel");

        progressBar.setOpaque(false);
        cancelButton.setOpaque(false);
        progressLabel.setOpaque(false);
        tooltipLabel.setOpaque(false);


        //progressLabel.setForeground(Color.BLACK);

        JPanel bottomSide = new JPanel(new BorderLayout(0,0));

        bottomSide.setOpaque(false);
        bottomSide.add(progressBar, BorderLayout.CENTER);
        bottomSide.add(cancelButton, BorderLayout.EAST);
        bottomSide.add(tooltipLabel, BorderLayout.SOUTH); 

        add(progressLabel, BorderLayout.CENTER);
        add(bottomSide, BorderLayout.SOUTH);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new Thread(cancelRunnable).start();
            }
        });

    }

    public void setEventUnsafe(boolean eventUnsafe) {
        this.eventUnsafe = eventUnsafe;
    }

    public boolean isEventUnsafe() {
        return eventUnsafe;
    }

    public void setLabel(final String text) {
        if (this.eventUnsafe) {
            progressLabel.setText(text);
        } else {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    progressLabel.setText(text);
                }
            });
        }
    }

    public void setToolTip(final String text) {
        if (this.eventUnsafe) {
            tooltipLabel.setText(text);
        } else {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    tooltipLabel.setText(text);
                }
            });
        }
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }

    public int getProgress() {
        return progressBar.getValue();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void paintComponent(Graphics g) {
        //g.clearRect(0,0,getWidth(), getHeight());
        super.paintComponent(g);
    }

    public static void main(String[] args) {
        JDialogExt dialog = new JDialogExt(null, "progress bar");

        final JProgressPanelA progress = new JProgressPanelA();

        progress.setLabel("Loading project...");

        new Thread(new Runnable(){
            public void run(){
                int i = 0;
                while(i < 100) {
                    try { Thread.sleep(200 + (int)(Math.random() * 200) ); } catch (Exception ex) {}
                    i+=5;
                    progress.setProgress(i);
                }
                System.exit(0);
            }
        }).start();

        dialog.setLayout(new BorderLayout(0,0));
        dialog.add(progress, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dialog.setVisible(true);
    }    
}
