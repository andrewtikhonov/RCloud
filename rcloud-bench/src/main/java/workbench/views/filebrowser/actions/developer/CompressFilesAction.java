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
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.dialogs.CompressFilesDialog;
import workbench.manager.opman.Operation;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 18, 2010
 * Time: 10:59:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompressFilesAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private CompressFilesDialog compressDialog;
    private static Logger log = LoggerFactory.getLogger(CompressFilesAction.class);

    public CompressFilesAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {

        //log.info("actionPerformed");

        final Vector<FileNode> toProcess = panel.getSelectedNodes();
        if (toProcess.size() > 0) {

            new Thread(new Runnable(){
                public void run(){

                    compressDialog = CompressFilesDialog.getInstance(panel);

                    String name = toProcess.size() > 1 ?
                            ((FileNode)panel.getModel().getRoot()).getName() : toProcess.get(0).getName();

                    compressDialog.setFilename(name);

                    compressDialog.setVisible(true);

                    if (compressDialog.getResult() == CompressFilesDialog.CANCEL) {
                        return;
                    }

                    String zipname = compressDialog.getFilename();
                    String command = compressDialog.getArchiverCommand();
                    String extension = compressDialog.getArchiverExtension();
                    String arguments = compressDialog.getArguments();

                    if (zipname != null) {

                        if (!zipname.endsWith(extension)) {
                            zipname = zipname + extension;
                        }

                        StringBuilder sb = new StringBuilder("");

                        for (FileNode node : toProcess) {
                            sb.append("'"+node.getName()+"' ");
                        }

                        String command0 = command + " " + arguments + " " + zipname + " " + sb.toString();

                        final Operation compressOp =
                                rgui.getOpManager().createOperation(
                                        "Compressing ", true);

                        try {
                            compressOp.startOperation();

                            rgui.obtainR().exec(command0);

                        } catch (Exception ex) {
                            //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                            log.error("Problem during compressing of " + zipname, ex);
                        } finally {
                            compressOp.completeOperation();
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean isEnabled() {
        boolean isEnabled = (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length > 0);
        //log.info("isEnabled="+Boolean.toString(isEnabled));

        return (isEnabled);
    }

}

