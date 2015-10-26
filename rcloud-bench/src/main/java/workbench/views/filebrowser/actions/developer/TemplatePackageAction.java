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
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.dialogs.packagetemplate.PackageTemplateDialog;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 14, 2010
 * Time: 1:32:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class TemplatePackageAction extends AbstractAction {
    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private static Logger log = LoggerFactory.getLogger(TemplatePackageAction.class);

    public TemplatePackageAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public static String join(Vector<String> values, String separator) {
        boolean first = true;

        StringBuilder result = new StringBuilder("");

        for(String item : values) {

            if (first) {
                first = false;
            } else {
                result.append(",");
            }

            result.append("'");
            result.append(item);
            result.append("'");
        }

        return result.toString();
    }

    public void actionPerformed(ActionEvent event) {
        new Thread(new Runnable(){
            public void run(){

                PackageTemplateDialog dialog = PackageTemplateDialog.getInstance(panel, rgui);

                dialog.setVisible(true);

                if (dialog.getResult() != PackageTemplateDialog.CANCEL) {
                    String name = dialog.getPackageName();
                    Vector<String> objects = dialog.getObjects();

                    if (name != null && !name.equals("")) {
                        String rootFolder = ((FileNode) panel.getModel().getRoot()).getPath();

                        //String packagePath = rootFolder + FileNode.separator + name;
                        String objectlist = "";
                        if (objects.size() > 0) {
                            objectlist = ", list = c(" + join(objects, ",") + ")";
                        }

                        String cmd = "package.skeleton(name = '" + name + "'" +
                                objectlist + ", path='" + rootFolder+ "')";

                        log.info("cmd="+cmd);

                        try {

                            rgui.obtainR().evaluate(cmd);

                        } catch (Exception ex) {
                            //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                            log.error("Problem during creation of template package " + name, ex);
                        }

                    }
                }

                /*
                String name = (String) JOptionPaneExt.showInputDialog(panel,
                                    "Package Name",
                                    "Create Package Template", JOptionPane.PLAIN_MESSAGE,
                                    null, null, "package");
                                    */
                /*
                if (name != null) {


                }
                */
            }
        }).start();
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable());
    }

}

