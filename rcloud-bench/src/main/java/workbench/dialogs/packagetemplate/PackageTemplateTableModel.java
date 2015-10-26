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
package workbench.dialogs.packagetemplate;

import uk.ac.ebi.rcloud.server.RType.RChar;
import uk.ac.ebi.rcloud.server.RType.RDataFrame;
import uk.ac.ebi.rcloud.server.RType.RObject;
import workbench.RGui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 04/07/2012
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
public class PackageTemplateTableModel extends AbstractTableModel {

    private String[] columnNames     = { "Include",      "",              "Name",         "Dims",      "Group" } ;
    private Class[] columnClasses    = {Boolean.class, ImageIcon.class, String.class, String.class, String.class };
    private Boolean[] columnEditable = {   true,         false,           false,          false,       false };


    private Vector<PackageTemplateTableItem> data = null ;
    private RGui rgui;

    public PackageTemplateTableModel(RGui rgui) {
        this.rgui = rgui;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data == null ? 0 : data.size() ;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Class<?> getColumnClass(int c) {
        return columnClasses[c];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnEditable[columnIndex];
    }

    public Object getValueAt(int row, int col) {
        if( data == null ) return null ;
        return data.get(row).getValueAt(col);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        PackageTemplateTableItem item = data.elementAt(rowIndex);
        switch(columnIndex) {
            case 0: item.setIncluded((Boolean)aValue);
        }
    }

    public Vector<String> getSelectedObjects() {
        Vector<String> result = new Vector<String>();

        for(PackageTemplateTableItem item : data) {
            if (item.isIncluded()) {
                result.add(item.getName());
            }
        }

        return result;
    }

    public void loadGlabalObjects( ) {
        loadObjectsFrom(".GlobalEnv");
    }

    public void loadObjectsFrom( final String text ) {

        new Thread(new Runnable() {
            public void run() {

                int n ;
                String[] names = null ;
                String[] classes =  null ;
                String[] groups = null ;
                String[] dims = null ;
                RDataFrame out = null ;

                try{
                    rgui.getRLock().lock() ;
                    out =  ( RDataFrame ) rgui.obtainR().getObject(
                            "as.data.frame( objList( path=NULL, all.info=TRUE, compare = FALSE, env = '" + text +
                                    "' )[, c('Name','Class','Group','Dims')] ) " ) ;
                } catch( Exception e){
                } finally{
                    rgui.getRLock().unlock( ) ;
                }

                try {

                    n = out.getRowNames().length ;
                    if( n == 0 ){
                        data = null ;
                    } else{
                        data = new Vector<PackageTemplateTableItem>();//[n] ;
                        RObject[] list = out.getData().getValue() ;
                        names = ( (RChar) list[0] ).getValue() ;
                        classes = ( (RChar) list[1] ).getValue() ;
                        groups = ( (RChar) list[2] ).getValue() ;
                        dims = ( (RChar) list[3] ).getValue() ;

                        for( int i=0; i<n; i++) {
                            data.add( new PackageTemplateTableItem( names[i], classes[i], groups[i], dims[i] ) ) ;
                            //data[i] = new RObjectExplorerObject( names[i], classes[i], groups[i], dims[i] ) ;
                        }
                    }

                    fireTableDataChanged() ;
                } catch (Exception ex) {
                }
            }
        }).start();
    }

}
