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
package workbench.dialogs.packagemanager;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 12, 2009
 * Time: 4:44:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManagerTableModel extends AbstractTableModel {

    private Vector<ManagerTableItem> packageList  = new Vector<ManagerTableItem>();
    private Vector<ManagerTableItem> filteredList = new Vector<ManagerTableItem>();
    private Vector<ManagerTableRowFilter>       filters  = new Vector<ManagerTableRowFilter>();

    private String[] columnNames = {"Loaded", "Package", "Title" };
    private Class[] columnClasses = {Boolean.class, String.class, String.class };
    private Boolean[] columnEditable = {true, false, false};

    public int getColumnCount() {
        return columnNames.length;
    }

    public Vector<ManagerTableItem> getList() {
        return filteredList;
    }

    public int getRowCount() {
        return getList().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ManagerTableItem item = getList().elementAt(rowIndex);

        if (columnIndex == 0) {
            return item.isLoaded();
        } else if (columnIndex == 1) {
            return item.getName();
        } else if (columnIndex == 2) {
            return item.getDescription();
        } else {
            throw new RuntimeException(columnIndex + " : bad column index");
        }
    }

    public void setValueAt(Object obj, int rowIndex, int columnIndex) {
        ManagerTableItem item = getList().elementAt(rowIndex);

        if (columnIndex == 0) {
            item.setLoaded((Boolean)obj);
        } else if (columnIndex == 1) {
        } else if (columnIndex == 2) {
        } else {
            throw new RuntimeException(columnIndex + " : bad column index");
        }
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Class<?> getColumnClass(int column) {
        return columnClasses[column];
    }

    public Vector<ManagerTableItem> getPackageList() {
        return packageList;
    }

    public void setPackageList(Vector<ManagerTableItem> list) {
        packageList = list;
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnEditable[columnIndex];
    }

    public void addRowFilter(ManagerTableRowFilter filter) {
        filters.add(filter);
        fireTableDataChanged();
    }

    public void removeRowFilter(ManagerTableRowFilter filter) {
        filters.remove(filter);
        fireTableDataChanged();
    }

    @Override
    public void fireTableDataChanged() {
        if (filters.size() > 0) {
            Vector<ManagerTableItem> filteredlist = packageList;

            for (ManagerTableRowFilter f : filters) {
                filteredlist = filterList(filteredlist, f.getMask());
            }
            filteredList = filteredlist;
        } else {
            filteredList = packageList;
        }

        super.fireTableDataChanged();
    }

    public Vector<ManagerTableItem> filterList(Vector<ManagerTableItem> fullList, String mask) {

        Vector<ManagerTableItem> list = new Vector<ManagerTableItem>();

        for (ManagerTableItem item : fullList) {
            if (item.getName().regionMatches(true, 0, mask, 0, mask.length())) {
                list.add(item);
            }
        }

        return list;
    }
}
