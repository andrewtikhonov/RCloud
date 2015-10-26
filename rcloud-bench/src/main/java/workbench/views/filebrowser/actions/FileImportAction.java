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
import workbench.views.filebrowser.actions.Notifier;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.security.AccessControlException;
import java.io.File;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 16, 2009
 * Time: 4:17:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileImportAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private JFileChooser     chooser = null;
    private static Logger log = LoggerFactory.getLogger(FileImportAction.class);

    public FileImportAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> importTo = panel.getSelectedNodes();

        new Thread(new Runnable(){
            public void run(){

                if (chooser == null) {
                    try {
                        chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                        chooser.setMultiSelectionEnabled(true);
                    } catch (AccessControlException e) {
                        JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                                "No permissions to access your local disk", "Permissions Required",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                String destpath = null;

                if (importTo.size() == 0) {
                    importTo.add((FileNode) panel.getModel().getRoot());
                }

                FileNode node = importTo.get(0);
                if (node.isDirectory()) {
                    destpath = node.getPath();
                } else {
                    destpath = node.getPath();
                    destpath = destpath.substring(0, destpath.lastIndexOf(FileNode.separator));
                }

                chooser.setSelectedFile(new File(""));

                int returnVal = chooser.showOpenDialog(rgui.getRootFrame());

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    //final Stopwatch sw = new Stopwatch();
                    //sw.start();

                    File[] selectedFiles = chooser.getSelectedFiles();

                    final int cnt = fetchNumberOfFiles(selectedFiles, 0);

                    final Operation exprtOp =
                            rgui.getOpManager().createOperation("Importing "+cnt+" files and folders..");

                    Notifier notifier = new Notifier() {
                        int i = 0;
                        public void call(Object obj) throws Exception {
                            int p = (100 * i++ / cnt);
                            exprtOp.setProgress(p, obj.toString());
                        }
                    };

                    exprtOp.startOperation();

                    try {
                        doRecursiveImport(selectedFiles, destpath, notifier);
                    } catch (Exception ex) {
                        if (ex instanceof OperationCancelledException) {
                            exprtOp.abortOperation();
                        } else {
                            //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                            log.error("Problem during file import", ex);
                        }
                    }

                    exprtOp.completeOperation();

                    //sw.stop();
                    //log.info("FileImportAction-sw="+sw.toString());

                }
            }
        }).start();
    }

    public int fetchNumberOfFiles(Object[] toCount, int cnt) {
        if (toCount != null) {
            cnt += toCount.length;
            for (int i = 0; i < toCount.length; i++) {
                File file = (File) toCount[i];
                if (file.isDirectory()) {
                    cnt = fetchNumberOfFiles(file.listFiles(), cnt);
                }
            }
        }
        return cnt;
    }

    private void doRecursiveImport(Object[] toImport, String destpath, Notifier notifier) throws Exception {
        if (toImport != null) {
            for (int i = 0; i < toImport.length; i++) {
                File fromFile = (File) toImport[i];

                if (fromFile.isDirectory()) {
                    String newbasepath = destpath + FileNode.separator + fromFile.getName();

                    if (notifier != null) notifier.call("creating "+newbasepath);

                    rgui.obtainR().createRandomAccessDir(newbasepath);

                    doRecursiveImport(fromFile.listFiles(), newbasepath, notifier);
                } else {
                    String toFile = destpath + FileNode.separator + fromFile.getName();

                    if (notifier != null) notifier.call("Importing "+fromFile.getName());

                    FileLoad.upload(fromFile, toFile, rgui.obtainR());
                }
            }
        }
    }


    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable());
    }


}
