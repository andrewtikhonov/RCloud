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
package workbench.dialogs.propertyviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.RGui;

import javax.swing.table.AbstractTableModel;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 11, 2010
 * Time: 2:04:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyBrowserModel extends AbstractTableModel {

    private Logger log = LoggerFactory.getLogger(getClass());

    private Vector<PropertyListItem> proprtylistbackup = new Vector<PropertyListItem>();
    private Vector<PropertyListItem> proprtylistvisual = new Vector<PropertyListItem>();

    private String[] columns = { "Proprty", "Value" };
    private Boolean[] editable = { false, true };
    private RGui rgui;

    private PropertyInterface currentInterface = null;

    public PropertyBrowserModel(RGui rgui){
        this.rgui = rgui;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getRowCount() {
        return proprtylistvisual.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        PropertyListItem item = proprtylistvisual.elementAt(rowIndex);
        switch(columnIndex) {
            case 0: return item.name;
            case 1: return item.value;
        }
        return null;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        PropertyListItem item = proprtylistvisual.elementAt(rowIndex);
        switch(columnIndex) {
            case 0: item.name = (String) aValue; break;
            case 1: item.value = (String) aValue; break;
        }

        log.info("setting " + item.name + " = " + item.value);
        currentInterface.setProperty(item.name, item.value);
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable[columnIndex];
    }

    public void add(PropertyListItem item) {
        currentInterface.setProperty(item.getName(), item.getValue());

        log.info("setting " + item.getName() + " = " + item.getValue());

        refreshContent();
    }

    PropertyInterface workbenchPropsInterface = new PropertyInterface() {
        public Hashtable<Object, Object> getProperties() {
            return System.getProperties();
        }
        public String getProperty(String name) {
            return System.getProperty(name);
        }
        public void setProperty(String name, String value) {
            System.setProperty(name, value);
        }
    };

    PropertyInterface serverPropsInterface = new PropertyInterface() {
        public Hashtable<Object, Object> getProperties() {
            try {
                return rgui.obtainR().getProperties();
            } catch (RemoteException re) {
                log.error("Error!", re);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
            return new Hashtable<Object, Object>();
        }
        public String getProperty(String name) {
            try {
                return rgui.obtainR().getProperty(name);
            } catch (RemoteException re) {
                log.error("Error!", re);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
            return "";
        }
        public void setProperty(String name, String value) {
            try {
                rgui.obtainR().setProperty(name, value);
            } catch (RemoteException re) {
                log.error("Error!", re);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }
    };

    public void refreshContent() {

        proprtylistbackup = new Vector<PropertyListItem>();

        Hashtable<Object, Object> table = currentInterface.getProperties();
        Iterator<Map.Entry<Object, Object>> iterator = table.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<Object, Object> entry = iterator.next();

            proprtylistbackup.add(new PropertyListItem(
                    entry.getKey().toString(),
                    entry.getValue().toString()));

        }

        proprtylistvisual = (Vector) proprtylistbackup.clone();

        fireTableDataChanged() ;
    }

    public void refresh(String option) {

        if (option.equals(PropertyBrowserConfig.WORKBENCH)) {
            currentInterface = workbenchPropsInterface;
        } else if (option.equals(PropertyBrowserConfig.SERVER)) {
            currentInterface = serverPropsInterface;
        } else {
            log.error("interface not supported for option = " + option);
            return;
        }

        refreshContent();
    }

    public void sortByColumn(int colIndex, int option) {
        if (option == 0) {
            proprtylistvisual = (Vector) proprtylistbackup.clone();
        } else if (option == 1) {
            Collections.sort(proprtylistvisual,
                    new PropertyBrowserColumnSorter(colIndex, true));

        } else if (option == 2) {
            Collections.sort(proprtylistvisual,
                    new PropertyBrowserColumnSorter(colIndex, false));

        } else {
            throw new RuntimeException("Unsupported Sorting option " + option);
        }

        fireTableStructureChanged();
    }
}
