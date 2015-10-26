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
package uk.ac.ebi.rcloud.common.components.label;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 12, 2009
 * Time: 3:44:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class JActionLabel extends JLabel implements MouseListener {

    private Vector<ActionListener> listenerList = new Vector<ActionListener>();

    private Color cbackup;
    private Color color;

    public JActionLabel(String text) {
        super(text);

        setForeground(new Color(150, 50, 50));
        //setForeground(new Color(50, 80, 100));

        cbackup = getForeground();
        color = getForeground().brighter();

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(this);

        //setOpaque(true);
    }

    public JActionLabel() {
        this("");
    }

    /*
    private BufferedImage image;

    public void paint(Graphics g) {


        //BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        //Graphics2D g2d = image.createGraphics();
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        Color b0 = getBackground();
        g2d.setColor(new Color(b0.getRed(), b0.getGreen(), b0.getBlue(), b0.getAlpha()));
        g2d.clearRect(0,0,getWidth(),getHeight());
        g2d.fillRect(0,0,getWidth(),getHeight());

        super.paint(g2d);
    }
    */

    public void mouseClicked(MouseEvent event) {
        invokeListeners(event);
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}

    public void mouseEntered(MouseEvent event) {
        setForeground(color);
        repaint();
    }

    public void mouseExited(MouseEvent event) {
        setForeground(cbackup);
        repaint();
    }

    private void invokeListeners(MouseEvent event) {
        ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, "action");

        for (ActionListener l : listenerList) {
            l.actionPerformed(actionEvent);
        }
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listenerList.remove(l);
    }

}
