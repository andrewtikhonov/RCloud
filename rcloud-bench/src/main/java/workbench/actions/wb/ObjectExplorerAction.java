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

import net.infonode.docking.DockingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.TabWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.Workbench;
import workbench.actions.WorkbenchActionType;
import workbench.util.AbstractDockingWindowListener;
import workbench.views.filebrowser.FileBrowserPanel;
import workbench.views.objectexplorer.ObjectExplorerView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 3, 2010
 * Time: 1:36:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectExplorerAction extends AbstractAction {
    final private static Logger log = LoggerFactory.getLogger(ObjectExplorerAction.class);

    private RGui rgui;

    public ObjectExplorerAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(final ActionEvent event) {
        if (getOpenedObjectExplorerView() == null) {
            int id = rgui.getViewManager().getDynamicViewId();

            ObjectExplorerView explorerview = new ObjectExplorerView("Object Explorer", null, id, rgui);

            rgui.getMainTabWindow().addTab(explorerview);

            explorerview.addListener(new ObjectExplorerDockingWindowListener(explorerview));
        }
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && !rgui.getOpManager().isLocked());
    }

    private ObjectExplorerView getOpenedObjectExplorerView() {
        return (ObjectExplorerView) rgui.getViewManager().getFirstViewOfClass(ObjectExplorerView.class);
    }

    class ObjectExplorerDockingWindowListener extends AbstractDockingWindowListener {
        ObjectExplorerView view;

        public ObjectExplorerDockingWindowListener(ObjectExplorerView view){
            this.view = view;
        }

        public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
            //view.dispose();
        }
    }

}
