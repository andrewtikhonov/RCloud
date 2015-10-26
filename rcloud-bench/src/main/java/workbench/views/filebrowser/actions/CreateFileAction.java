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
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 14, 2010
 * Time: 2:04:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateFileAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private static Logger log = LoggerFactory.getLogger(CreateFileAction.class);

    public CreateFileAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toCreateIn = panel.getSelectedNodes();

        new Thread(new Runnable(){
            public void run(){

                String fileName = (String) JOptionPaneExt.showInputDialog(panel,
                                    "Create a new file:",
                                    "New File", JOptionPane.PLAIN_MESSAGE,
                                    null, null, "untitled");

                if (fileName == null) return;

                if (toCreateIn.size() == 0) {
                    toCreateIn.add((FileNode) panel.getModel().getRoot());
                }

                for (FileNode node : toCreateIn) {
                    try {
                        if (node.isDirectory()) {
                            String filePath = node.getPath() + FileNode.separator + fileName;

                            rgui.obtainR().createRandomAccessFile(filePath);


                        } else  {
                            String destpath = node.getPath();
                            destpath = destpath.substring(0, destpath.lastIndexOf(FileNode.separator));
                            destpath = destpath + FileNode.separator + fileName;

                            rgui.obtainR().createRandomAccessFile(destpath);
                        }
                    } catch (Exception ex) {
                        //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                        log.error("Problem during file creation", ex);

                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable());
    }

}
