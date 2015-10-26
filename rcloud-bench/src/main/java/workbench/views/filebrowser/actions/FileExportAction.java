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
import workbench.util.FileLoad;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.security.AccessControlException;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 16, 2009
 * Time: 4:17:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileExportAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private JFileChooser     chooser = null;
    private static Logger log = LoggerFactory.getLogger(FileExportAction.class);

    public FileExportAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toExport = panel.getSelectedNodes();
        if (toExport.size() > 0) {

            new Thread(new Runnable(){
                public void run(){

                    if (chooser == null) {
                        try {
                            chooser = new JFileChooser();
                            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            chooser.setMultiSelectionEnabled(false);
                        } catch (AccessControlException e) {
                            JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                                    "No permissions to access your local disk", "Permissions Required",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    chooser.setSelectedFile(new File(toExport.get(0).getName()));

                    int returnVal = chooser.showSaveDialog(rgui.getRootFrame());

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        String path     = chooser.getSelectedFile().getPath();
                        String basepath = path.substring(0, path.lastIndexOf(File.separator));

                        Object[] arrayToExport = toExport.toArray();

                        final int cnt = fetchNumberOfNodes(arrayToExport, 0);

                        final Operation exportOp =
                                rgui.getOpManager().createOperation("Exporting "+cnt+" files and folders..");

                        Notifier notifier = new Notifier() {
                            int i = 0;
                            public void call(Object obj) throws Exception {
                                int p = (100 * i++ / cnt);
                                exportOp.setProgress(p, obj.toString());
                            }
                        };

                        exportOp.startOperation();

                        try {
                            doRecursiveExport(arrayToExport, basepath, notifier);
                        } catch (Exception ex) {
                            if (ex instanceof OperationCancelledException) {
                                exportOp.abortOperation();
                            } else {
                                //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                                log.error("Problem during file export",ex);
                            }
                        }

                        exportOp.completeOperation();
                    }
                }
            }).start();
        }
    }

    public int fetchNumberOfNodes(Object[] toCount, int cnt) {
        if (toCount != null) {
            cnt += toCount.length;
            for (int i = 0; i < toCount.length; i++) {
                FileNode node = (FileNode) toCount[i];
                if (node.isDirectory()) {
                    cnt = fetchNumberOfNodes(node.getChildren(), cnt);
                }
            }
        }
        return cnt;
    }

    private void doRecursiveExport(Object[] toExport, String destpath, Notifier notifier) throws Exception {
        if (toExport != null) {
            for (int i = 0; i < toExport.length; i++) {
                FileNode fromFile = (FileNode) toExport[i];

                if (fromFile.isDirectory()) {

                    if (notifier != null) notifier.call("creating " + fromFile.getName());

                    File newdest = new File(destpath, fromFile.getName());

                    newdest.mkdirs();

                    doRecursiveExport(fromFile.getChildren(), newdest.getPath(), notifier);
                } else {
                    if (notifier != null) notifier.call("Exporting "+fromFile.getName());

                    FileLoad.download(fromFile.getPath(),
                            new File(destpath, fromFile.getName()), rgui.obtainR());
                }
            }
        }
    }


    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length > 0);
    }
    
}
