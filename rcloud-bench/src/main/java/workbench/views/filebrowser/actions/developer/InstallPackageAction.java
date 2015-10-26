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
import workbench.manager.opman.Operation;
import workbench.manager.opman.OperationCancelledException;
import workbench.dialogs.InstallPackageDialog;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.util.Vector;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 21, 2009
 * Time: 1:37:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstallPackageAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private static Logger log = LoggerFactory.getLogger(InstallPackageAction.class);

    public InstallPackageAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toInstall = panel.getSelectedNodes();
        final InstallPackageDialog packageDialog = new InstallPackageDialog(panel);

        packageDialog.setLibrary(rgui.getUser().getUserLibFolder());

        if (toInstall.size() > 0) {
            new Thread(new Runnable(){
                public void run(){


                    for (FileNode node : toInstall) {
                        packageDialog.addPackage(node.getName());
                    }

                    packageDialog.setVisible(true);

                    if (packageDialog.getResult() == InstallPackageDialog.CANCEL) return;

                    Operation installPackageOp =
                            rgui.getOpManager().createOperation("Installing packages..");

                    int cnt = 0;
                    int tot = toInstall.size();
                    String cmd0 = "R CMD INSTALL --library="+packageDialog.getLibrary()+
                            " "+packageDialog.getOptions()+" ";

                    if (rgui.isRAvailable()) {

                        installPackageOp.startOperation();

                        try {
                            for (FileNode node : toInstall) {
                                try {

                                    installPackageOp.startOperation("...");

                                    try {
                                        installPackageOp.setProgress(100 * cnt++ / tot,
                                                "installing " + node.getName());

                                    } catch (OperationCancelledException oce) {
                                        break;
                                    }

                                    String cmd1 = cmd0 + " '" + node.getPath() + "'";

                                    log.info("InstallPackageAction-cmd1=" + cmd1);

                                    rgui.obtainR().exec(cmd1);

                                } catch (Exception ex) {
                                    //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                                    log.error("Problem during installation of " + node.getName(), ex);
                                    break;
                                } finally {
                                    rgui.getRLock().unlock();
                                }
                            }

                        } finally {
                            installPackageOp.completeOperation();
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
