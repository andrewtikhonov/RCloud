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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.manager.opman.Operation;
import workbench.manager.opman.OperationCancelledException;
import workbench.views.filebrowser.FileBrowserPanel;
import static workbench.util.CloudDefaults.BASE_FOLDER;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 19, 2010
 * Time: 9:37:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileLinkImportAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private static Logger log = LoggerFactory.getLogger(FileLinkImportAction.class);

    public FileLinkImportAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toCreateIn = panel.getSelectedNodes();

        new Thread(new Runnable(){
            public void run(){

                if (toCreateIn.size() > 1) return;

                String fileLink = (String) JOptionPaneExt.showInputDialog(panel,
                                    "Create a new file:",
                                    "New File", JOptionPane.PLAIN_MESSAGE,
                                    null, null, "untitled");

                if (fileLink == null) return;

                if (toCreateIn.size() == 0) {
                    toCreateIn.add((FileNode) panel.getModel().getRoot());
                }

                String srcFileName = fileLink.substring(fileLink.lastIndexOf(FileNode.separator) + 1);
                String srcFilePath = BASE_FOLDER + FileNode.separator + fileLink;

                for (FileNode nodeToCreateIn : toCreateIn) {

                    final Operation exportOp =
                            rgui.getOpManager().createOperation("Importing " + fileLink, true);

                    String destFilePath;
                    String nodePath = nodeToCreateIn.getPath();

                    if (nodeToCreateIn.isDirectory()) {
                        destFilePath = nodePath + FileNode.separator + srcFileName;
                    } else {
                        destFilePath = nodePath.substring(0, nodePath.length() - nodeToCreateIn.getName().length()) +
                                FileNode.separator + srcFileName;
                    }

                    try {
                        exportOp.startOperation();

                        //log.info("srcFilePath="+srcFilePath+" destFilePath="+destFilePath);

                        rgui.obtainR().copyRandomAccessDir(srcFilePath, destFilePath);

                    } catch (Exception ex) {
                        if (ex instanceof OperationCancelledException) {
                            exportOp.abortOperation();
                        } else {
                            //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                            log.error("Problem during link import", ex);

                        }
                    } finally {
                        exportOp.completeOperation();
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length <= 1);
    }

}

