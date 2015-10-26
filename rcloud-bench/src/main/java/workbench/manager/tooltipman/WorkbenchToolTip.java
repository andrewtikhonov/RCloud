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
package workbench.manager.tooltipman;

import uk.ac.ebi.rcloud.common.components.tooltip.StandardToolTip;
import uk.ac.ebi.rcloud.common.util.LAFUtil;
import uk.ac.ebi.rcloud.common.util.WindowDragMouseAdapter;
import workbench.util.ButtonUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 27, 2010
 * Time: 6:05:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkbenchToolTip {

    private StandardToolTip tippanel;
    private JWindow tipwindow;

    public static long DEFAULT_TIMEOUT = 5000;
    
    public WorkbenchToolTip(){
        tippanel = new StandardToolTip();
        tipwindow = createWindow(tippanel);
    }

    public void setTipLocation(Point location){
        tipwindow.setLocation(location);
    }

    public void setTipLocationRelativeTo(Component c, Point rellocation){
        Point p0 = (c.isVisible()) ? c.getLocationOnScreen() : new Point(40, 40);

        if (rellocation.y < 0) {
            rellocation.y = rellocation.y - tippanel.getPreferredSize().height;
        }

        p0.translate(rellocation.x, rellocation.y);

        setTipLocation(p0);
    }

    public void setPreferredSize(Dimension preferredSize){
        tippanel.setPreferredSize(preferredSize);
        tipwindow.setPreferredSize(tippanel.getPreferredSize());
        tipwindow.pack();
    }

    public JWindow createWindow(StandardToolTip tippanel) {
        JWindow window = new JWindow();

        tippanel.getClosebutton().addActionListener(new DisposeAdapter(window));

        window.setLayout(new BorderLayout());
        window.add(tippanel, BorderLayout.CENTER);

        WindowDragMouseAdapter dragAdapter = new WindowDragMouseAdapter();
        window.addMouseListener(dragAdapter);
        window.addMouseMotionListener(dragAdapter);

        window.setSize(tippanel.getPreferredSize());
        window.setLocationRelativeTo(null);

        return window;
    }

    public long getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public void showToolTip(final String headline, final String text, final long timeout){
        new Thread(new Runnable(){
            public void run(){
                tippanel.setHeadline(headline);
                tippanel.setText(text);

                tipwindow.setVisible(true);

                if (timeout != 0) {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException ie) {
                    }

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            tipwindow.setVisible(false);
                        }
                    });
                }
            }
        }).start();
    }

    public void showToolTip(String headline, final String text){
        showToolTip(headline, text, 0);
    }

    class DisposeAdapter implements ActionListener {
        JWindow window;

        public DisposeAdapter(JWindow window) {
            this.window = window;
        }

        public void actionPerformed(ActionEvent event) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    tipwindow.setVisible(false);
                }
            });
        }
    }

    private static void initUI(){
        //System.setProperty("apple.awt.draggableWindowBackground", "true");

        LAFUtil.setupLookAndFeel();

        final JFrame frame0 = new JFrame();

        final WorkbenchToolTip tip = new WorkbenchToolTip();

        final JTextField headline = new JTextField();
        final JTextField textline = new JTextField();
        final JButton showtip = ButtonUtil.makeButton("tooltip");

        showtip.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                tip.setTipLocationRelativeTo(showtip, new Point(20, -20));
                tip.showToolTip(headline.getText(),
                        textline.getText(),
                        DEFAULT_TIMEOUT);
            }
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel textbox = new JPanel(new GridLayout(0,1));
        textbox.setOpaque(false);
        textbox.add(headline);
        textbox.add(textline);

        JButton exitButton = ButtonUtil.makeButton("exit");
        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        content.add(showtip, BorderLayout.NORTH);
        content.add(textbox, BorderLayout.CENTER);
        content.add(exitButton, BorderLayout.SOUTH);

        frame0.setLayout(new BorderLayout());
        frame0.add(content, BorderLayout.SOUTH);
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


}
