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
package workbench.views.filebrowser;

import workbench.views.filebrowser.components.OutlineExtended;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 17, 2009
 * Time: 10:49:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileBrowserMouseAdapter extends MouseAdapter {

    FileBrowserPanel panel;
    FileBrowserPopupMenu menu;

    public FileBrowserMouseAdapter(FileBrowserPanel panel, FileBrowserPopupMenu menu) {
        this.panel = panel;
        this.menu = menu;
    }

    private void checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {

            OutlineExtended outline = panel.getOutline();

            int row    = outline.rowAtPoint( e.getPoint() );
            int rows[] = outline.getSelectedRows();

            if (row == -1) {

                outline.clearSelection();

            } else {

                boolean found = false;

                for(int i : rows) {
                    if (i == row) found = true;
                }

                if (!found) {
                    outline.getSelectionModel().setSelectionInterval(row, row);
                }
            }

            menu.updatePopupMenu();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }


    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            panel.getActions().get(FileBrowserActionType.OPEN).actionPerformed(null);
        }
    }

    public void mousePressed(MouseEvent event) {
        checkPopup(event);
    }

    public void mouseReleased(MouseEvent event) {
        checkPopup(event);
    }

}
