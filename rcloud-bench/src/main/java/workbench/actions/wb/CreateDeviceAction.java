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
package workbench.actions.wb;

import net.infonode.docking.FloatingWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.http.exception.TunnelingException;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import workbench.RGui;
import workbench.graphics.GDContainerPanel;
import workbench.graphics.GDControlPanel;
import workbench.views.graphicdevice.DeviceView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 3, 2010
 * Time: 1:54:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateDeviceAction extends AbstractAction {

    final private static Logger log = LoggerFactory.getLogger(CreateDeviceAction.class);

    private RGui rgui;

    public static int DEFAULT_WIDTH = 600;
    public static int DEFAULT_HEIGHT = 400;

    public CreateDeviceAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(final ActionEvent e) {

        JPanel rootGraphicPanel = new JPanel(new BorderLayout());

        JPanel graphicPanel = new JPanel();

        rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);

        int id = rgui.getViewManager().getDynamicViewId();

        DeviceView deviceView = new DeviceView("Graphic Device", null, rootGraphicPanel, id);

        try {


            GDControlPanel controlPanel = null;
            GDDevice newDevice = null;

            try {
                rgui.getRLock().lock();

                newDevice = rgui.obtainR().newDevice(DEFAULT_WIDTH, DEFAULT_HEIGHT);

            } finally {
                rgui.getRLock().unlock();
            }

            graphicPanel = new GDContainerPanel(newDevice, null);
            controlPanel = new GDControlPanel((GDContainerPanel) graphicPanel);

            ((GDContainerPanel) graphicPanel).setConsoleProvider(rgui.getBenchRConsoleProvider());

            rootGraphicPanel.removeAll();
            rootGraphicPanel.setLayout(new BorderLayout());
            rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);
            rootGraphicPanel.add(controlPanel,  BorderLayout.SOUTH);


            deviceView.setPanel((GDContainerPanel) graphicPanel);

            rgui.setCurrentDevice(newDevice);

            FloatingWindow window = rgui.getBenchRootWindow().
                    createFloatingWindow(new Point(200, 200), new Dimension(DEFAULT_WIDTH,
                            DEFAULT_HEIGHT + controlPanel.getPreferredSize().height), deviceView);

            window.getTopLevelAncestor().setVisible(true);


        } catch (TunnelingException te) {
            log.error("Error!", te);
        } catch (RemoteException re) {
            log.error("Error!", re);
        }

    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && !rgui.getOpManager().isLocked());
    }


}
