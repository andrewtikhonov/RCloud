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
package workbench.views.filebrowser.models;

import uk.ac.ebi.rcloud.server.file.FileNode;
import org.netbeans.swing.outline.RowModel;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 23, 2009
 * Time: 1:51:54 PM
 * To change this template use File | Settings | File Templates.
 */

public class FileRowModel implements RowModel {

    private static String[] names = { "Date", "Time", "Size" };

    //@Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Date.class;
            case 1:
                return Date.class;
            case 2:
                return Long.class;
            default:
                assert false;
        }
        return null;
    }

    //@Override
    public int getColumnCount() {
        return names.length;
    }

    //@Override
    public String getColumnName(int column) {
        return names[column];
    }

    //@Override
    public Object getValueFor(Object node, int column) {
        FileNode n = (FileNode) node;
        switch (column) {
            case 0:
                return new Date(n.lastModified());
            case 1:
                return new Date(n.lastModified());
            case 2:
                return n.length();
            default:
                assert false;
        }
        return null;
    }

    //@Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    //@Override
    public void setValueFor(Object node, int column, Object value) {
        //do nothing for now
    }

}
