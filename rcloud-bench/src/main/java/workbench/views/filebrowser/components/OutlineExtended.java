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
package workbench.views.filebrowser.components;

import uk.ac.ebi.rcloud.server.file.FileNode;
import org.netbeans.swing.outline.Outline;

import java.util.EventObject;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 17, 2009
 * Time: 10:38:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class OutlineExtended extends Outline {

    private OutlineExpandAction expandAction;

    public OutlineExtended(OutlineExpandAction expandAction) {
        this.expandAction = expandAction;
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject event) {
        if (event instanceof MouseEvent && ((MouseEvent) event).getClickCount() == 2) {
            int[] rows = getSelectedRows();

            if (rows.length == 1) {
                FileNode node = (FileNode) getValueAt(rows[0],
                        getColumnIndex("Files"));

                if (node.isDirectory()) {
                    expandAction.nodeExpanded(node);
                    return false;
                }
            }
        }
        return super.editCellAt(row, column, event);
    }

    public int getColumnIndex(String columnName) {
        return getColumnModel().getColumnIndex(columnName);
    }


}
