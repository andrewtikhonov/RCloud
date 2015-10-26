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
package workbench.dialogs.packageinstaller;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 1, 2009
 * Time: 12:49:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstallerTableModel extends AbstractTableModel {

    private Vector<InstallerTableItem> packageList  = new Vector<InstallerTableItem>();
    private Vector<InstallerTableItem> filteredList = new Vector<InstallerTableItem>();
    private Vector<InstallerTableRowFilter>       filters  = new Vector<InstallerTableRowFilter>();

    private String[] columnNames = {"Package", "Installed Version", "Repository Version" };
    private Class[] columnClasses = {String.class, String.class, String.class };

    public int getColumnCount() {
        return columnNames.length;
    }

    public Vector<InstallerTableItem> getList() {
        if (filters.size() > 0) {
            return filteredList;
        } else {
            return packageList;
        }
    }

    public int getRowCount() {

        //log.info("InstallerTableModel-getRowCount-getList().size()="+getList().size());

        return getList().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        InstallerTableItem pkg = getList().elementAt(rowIndex);

        //log.info("InstallerTableModel-getValueAt-pkg="+pkg.getName());

        if (columnIndex == 0) {
            return pkg.getName();
        } else if (columnIndex == 1) {
            return pkg.getInstalledVersion();
        } else if (columnIndex == 2) {
            return pkg.getRepositoryVersion();
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

    public Vector<InstallerTableItem> getPackageList() {
        return packageList;
    }

    public void setPackageList(Vector<InstallerTableItem> list) {
        packageList = list;
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void addRowFilter(InstallerTableRowFilter filter) {
        filters.add(filter);
        fireTableDataChanged();
    }

    public void removeRowFilter(InstallerTableRowFilter filter) {
        filters.remove(filter);
        fireTableDataChanged();
    }

    @Override
    public void fireTableDataChanged() {
        if (filters.size() > 0) {
            Vector<InstallerTableItem> filteredlist = packageList;

            for (InstallerTableRowFilter f : filters) {
                filteredlist = filterList(filteredlist, f.getMask());
            }
            filteredList = filteredlist;
        }

        super.fireTableDataChanged();
    }

    public Vector<InstallerTableItem> filterList(Vector<InstallerTableItem> fullList, String mask) {

        Vector<InstallerTableItem> list = new Vector<InstallerTableItem>();

        for (InstallerTableItem item : fullList) {
            if (item.getName().regionMatches(true, 0, mask, 0, mask.length())) {
                list.add(item);
            }
        }
    
        return list;
    }
}
