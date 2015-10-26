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
package workbench.graphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 7, 2010
 * Time: 2:38:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class GDControlPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(GDContainerPanel.class);

    private GDContainerPanel container;

    private JLabel zoomLabel;
    private JButton zoomIn;
    private JButton zoomOut;
    private JButton zoomNorm;

    private Cursor  hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public GDControlPanel(GDContainerPanel container) {
        this.container = container;


        zoomIn = makeButton("/views/images/gdevices/zoom in.png",
                "zoom in");

        zoomOut = makeButton("/views/images/gdevices/zoom out.png",
                "zoom out");

        zoomNorm = makeButton("/views/images/gdevices/search.png",
                "1:1");

        zoomLabel = new JLabel(" Zoom: " +
                Double.toString(normalize(container.getZoom())));

        setLayout(new FlowLayout());

        ZoomActionListener listener = new ZoomActionListener();

        zoomIn.addActionListener(listener);
        zoomOut.addActionListener(listener);
        zoomNorm.addActionListener(listener);

        add(zoomIn);
        add(zoomOut);
        add(zoomNorm);
        add(zoomLabel);

        container.addPropertyChangeListener(GDContainerPanel.ZOOM_PROPERY,
                new ContainerPropertyChangeListener());

    }

    private double normalize(double value) {
        return ((double)((int) (value * 100)))/100;
    }

    class ContainerPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent propertychangeevent) {

            double value = (Double) propertychangeevent.getNewValue();

            zoomLabel.setText(" Zoom: " + Double.toString(normalize(value)));
        }
    }

    private JButton makeButton(String iconPath, String tooltip) {
        JButton button = new JButton();

        ImageIcon icon = new ImageIcon(ImageLoader.load(iconPath));
        icon.setImage(GraphicsUtilities.scaleImage(icon.getImage(), 20));
        button.setIcon(icon);
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(26, 26));
        button.setBorderPainted(false);
        button.setCursor(hand);

        return button;
    }

    class ZoomActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();

            if (source == zoomIn) {
                container.setZoom(Math.min(container.getZoom() + 0.5, 4));

            } else if (source == zoomOut) {
                container.setZoom(Math.max(container.getZoom() - 0.5, 0.5));

            } else if (source == zoomNorm) {
                container.setZoom(1);

            }
        }
    }

}
