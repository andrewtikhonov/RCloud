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
import uk.ac.ebi.rcloud.server.search.SearchRequest;
import workbench.RGui;
import workbench.dialogs.CompressFilesDialog;
import workbench.dialogs.FindInPathDialog;
import workbench.manager.opman.Operation;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 1, 2010
 * Time: 4:11:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class FindInPathAction extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(CompressFilesAction.class);

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;

    public FindInPathAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent event) {

        //log.info("actionPerformed");

        final Vector<FileNode> toProcess = panel.getSelectedNodes();

        if (toProcess.size() > 0) {

            FindInPathDialog findDialog = FindInPathDialog.getInstance(panel);

            String filename = toProcess.get(0).getPath();

            findDialog.setDirectory(filename);

            findDialog.setVisible(true);

            if (findDialog.getResult() == CompressFilesDialog.CANCEL) {
                return;
            }

            SearchRequest request = new SearchRequest(
                    findDialog.getDirectory(),  // this.path = path;
                    findDialog.getPattern(),    // this.pattern = pattern;
                    findDialog.getMask(),       // this.mask = mask;
                    findDialog.isUseMask(),     // this.usemask = usemask;
                    findDialog.isCaseSensitive(), // this.casesensitive = casesensitive;
                    findDialog.isWholeWords(),  // this.wholewords = wholewords;
                    findDialog.isRecursive()    // this.recursive = recursive;
                    );

            final Operation compressOp =
                    rgui.getOpManager().createOperation(
                            "Searching ", true);

            try {
                compressOp.startOperation();

                rgui.obtainR().searchAsync(request);

            } catch (Exception ex) {
                //JOptionPaneExt.showExceptionDialog(panel.getParent(), ex);
                log.error("Problem during search", ex);
            } finally {
                compressOp.completeOperation();
            }

            /*
            new Thread(new Runnable(){
                public void run(){

                }
            }).start();
            */
        }
    }

    @Override
    public boolean isEnabled() {
        boolean isEnabled = (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length > 0);
        return (isEnabled);
    }

}

