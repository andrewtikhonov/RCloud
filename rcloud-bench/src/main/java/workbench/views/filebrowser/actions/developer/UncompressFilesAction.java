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
package workbench.views.filebrowser.actions.developer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.manager.opman.Operation;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 18, 2010
 * Time: 11:00:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class UncompressFilesAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private static Logger log = LoggerFactory.getLogger(UncompressFilesAction.class);

    public UncompressFilesAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toProcess = panel.getSelectedNodes();
        if (toProcess.size() > 0) {
            new Thread(new Runnable(){
                public void run(){

                    for (FileNode node : toProcess) {

                        String name = node.getName();
                        String command = "";

                        if (name.endsWith(".zip")) {
                            command = "unzip";

                        } else if (name.endsWith(".tar.gz")) {
                            command = "tar -zxvf";

                        } else {
                            JOptionPaneExt.showMessageDialog(panel.getParent(),
                                    "Unsupported archive " + name);
                            continue;
                        }


                        final Operation uncompressOp =
                                rgui.getOpManager().createOperation(
                                        "Uncompressing ", true);

                        try {
                            uncompressOp.startOperation();

                            rgui.obtainR().exec(command + " '" + node.getName() + "'");
                            
                        } catch (Exception ex) {
                            //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                            log.error("Problem during un-compressing of "+node.getName(), ex);

                            break;
                        } finally {
                            uncompressOp.completeOperation();
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length > 0);
    }

}

