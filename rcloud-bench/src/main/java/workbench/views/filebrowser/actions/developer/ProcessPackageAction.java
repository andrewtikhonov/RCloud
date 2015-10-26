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
 * Date: Jan 19, 2010
 * Time: 3:58:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessPackageAction extends AbstractAction {
    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private String operation  = null;
    private String command  = null;
    private String title  = null;

    private static Logger log = LoggerFactory.getLogger(ProcessPackageAction.class);
    
    public ProcessPackageAction(RGui rgui, FileBrowserPanel panel, String name,
                                String command, String title, String operation){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
        this.command = command;
        this.title = title;
        this.operation = operation;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toProcess = panel.getSelectedNodes();
        if (toProcess.size() > 0) {
            new Thread(new Runnable(){
                public void run(){
                    String defaultString = "arguments";

                    String args = (String) JOptionPaneExt.showInputDialog(panel,
                                        command,
                                        title, JOptionPane.PLAIN_MESSAGE,
                                        null, null, defaultString);

                    if (args == null) return;
                    if (args.equals(defaultString)) {
                        args = "";
                    }
                    
                    Operation buildOp =
                            rgui.getOpManager().createOperation(operation, true);
                    try {
                        buildOp.startOperation("...");

                        for (FileNode node : toProcess) {
                            buildOp.setProgress(50, node.getName());

                            rgui.obtainR().exec(command + args + " '" + node.getPath() + "'");
                        }

                    } catch (Exception ex) {
                        //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                        log.error("Problem during " + command, ex);
                    } finally {
                        buildOp.completeOperation();
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

