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
package workbench.views.filebrowser.actions;

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.actions.WorkbenchActionType;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 19, 2009
 * Time: 4:26:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpenFileAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
                                                                          
    public OpenFileAction(RGui rgui, FileBrowserPanel panel, String name) {
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    class OpenFileRunnable implements Runnable {
        Vector<FileNode> toOpen;

        public OpenFileRunnable(Vector<FileNode> toOpen){
            this.toOpen = toOpen;
        }

        public void run(){
            AbstractAction openFileAction = rgui.getActions().get(WorkbenchActionType.OPENFILE);
            for (FileNode node : toOpen) {
                if (!node.isDirectory()) {

                    long threashold = 512 * 1024;

                    if (node.length() > threashold) {
                        Object[] options = { "Open", "Cancel" };

                        int reply = JOptionPaneExt.showOptionDialog(panel,
                                node.getName() + " seems weighty ( ~" +
                                        (node.length() / 1024 / 1024) + " MB )", "",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null, options, options[0]);

                        if (reply == -1 || reply == 1) continue;
                    }

                    openFileAction.actionPerformed(new ActionEvent(this, 0, node.getPath()));
                }
            }
        }
    }

    public void actionPerformed(ActionEvent ae) {
        Vector<FileNode> toOpen = panel.getSelectedNodes();

        if (toOpen.size() > 0 && rgui.isRAvailable()) {
            new Thread(new OpenFileRunnable(toOpen)).start();
        }
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length > 0);
    }
}
