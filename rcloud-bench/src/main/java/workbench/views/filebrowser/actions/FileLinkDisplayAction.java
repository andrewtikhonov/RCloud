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
import workbench.views.filebrowser.FileBrowserPanel;
import static workbench.util.CloudDefaults.BASE_FOLDER;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 18, 2010
 * Time: 10:57:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileLinkDisplayAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;

    public FileLinkDisplayAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toShow = panel.getSelectedNodes();
        if (toShow.size() > 0) {
            new Thread(new Runnable(){
                public void run(){
                    for (FileNode node : toShow) {

                        String filePath = node.getPath();

                        String idFull = filePath.substring(BASE_FOLDER.length() + 1); 

                        String filename = (String) JOptionPaneExt.showInputDialog(panel,
                                            "File Link:",
                                            "File Link box", JOptionPane.PLAIN_MESSAGE,
                                            null, null, idFull);
                    }
                }
            }).start();
        }

    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length == 1);
    }
}


