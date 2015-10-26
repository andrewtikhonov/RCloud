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
package workbench.dialogs;

import sun.awt.VerticalBagLayout;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 23, 2009
 * Time: 2:00:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstallPackageDialog extends JDialogA {

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int dialogResult = CANCEL;

    private static String binaryStr = "Binary";
    private static String sourceStr = "Source";

    private JTable      packageTable;
    private JTextField  optionsField;
    private JTextField  libraryField;

    private JButton install = ButtonUtil.makeButton("Install");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    public static class PackageItem {
        public String name;
        public String type;
        public PackageItem(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
    private Vector<PackageItem> packageList = new Vector<PackageItem>(); 

    public InstallPackageDialog(Component c) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10,10,10,10));

        //container.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel optionsPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));


        tablePanel.setOpaque(false);
        optionsPanel.setOpaque(false);
        buttonPanel.setOpaque(false);

        tablePanel.add(optionsPanel, BorderLayout.SOUTH);
        container.add(tablePanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        buttonPanel.add(cancel);
        buttonPanel.add(install);

        JPanel optionsPanelCont1 = new JPanel(new GridLayout(2,1));
        JPanel optionsPanelCont2 = new JPanel(new GridLayout(2,1));

        optionsPanel.add(optionsPanelCont1, BorderLayout.WEST);
        optionsPanel.add(optionsPanelCont2, BorderLayout.CENTER);

        JLabel libraryLabel = new JLabel("Library: ");
        libraryField = new JTextField();
        optionsPanelCont1.add(libraryLabel);
        optionsPanelCont2.add(libraryField);

        JLabel optionsLabel = new JLabel("Options: ");
        optionsField = new JTextField();
        optionsPanelCont1.add(optionsLabel);
        optionsPanelCont2.add(optionsField);

        JComboBox typeCombo = new JComboBox(new String[]{ sourceStr, binaryStr});

        //Object[][] d = {{ "sf", "asdf"}};
        //Object[] h = { "Package", "Type"};

        //packageTable = new JTable(d, h);
        packageTable = new JTable();

        packageTable.setModel(new AbstractTableModel() {

            String[] columns = { "Package", "Type" };

            public int getColumnCount() {
                return columns.length;
            }

            public int getRowCount() {
                return packageList.size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                PackageItem item = packageList.elementAt(rowIndex);
                switch(columnIndex) {
                    case 0: return item.name;
                    case 1: return item.type;
                }
                return null;
            }

            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                PackageItem item = packageList.elementAt(rowIndex);
                switch(columnIndex) {
                    case 0: item.name = (String) aValue;
                    case 1: item.type = (String) aValue;
                }
            } 

            public String getColumnName(int column) {
                return columns[column];
            }

            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 1) return true;
                return false;
            }
        });

        packageTable.getColumnModel().getColumn(0).setPreferredWidth(230);
        packageTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        packageTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCombo));

        packageTable.setRowHeight(20);
        packageTable.getTableHeader().setPreferredSize(new Dimension(2, 16));

        tablePanel.add(new JScrollPane(packageTable), BorderLayout.CENTER);
        
        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == install) {
                    okMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        setLayout(new BorderLayout());

        add(container, BorderLayout.CENTER);
        setTitle("Install A Custom Package");

        install.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        this.getRootPane().setDefaultButton(install);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setPreferredSize(new Dimension(550, 320));
        //pack();
        setLocationRelativeTo(c);
        setResizable(true);
    }

    public void okMethod() {
        dialogResult = OK;
        setVisible(false);
    }

    public void cancelMethod() {
        dialogResult = CANCEL;
        setVisible(false);
    }

    public String getPackage() {
        return null;
        //return packageList.get packageField.getText();
    }

    public String getLibrary() {
        return libraryField.getText();
    }

    public String getOptions() {
        return optionsField.getText();
    }

    public int getSelectedType() {
        return 0;
        //return typeCombo.getSelectedIndex();
    }

    public void setLibrary(final String libraryPath) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    libraryField.setText(libraryPath);
                }
            });
        } else {
            libraryField.setText(libraryPath);
        }
    }

    public void setOptions(final String options) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    optionsField.setText(options);
                }
            });
        } else {
            optionsField.setText(options);
        }
    }

    public void addPackage(String packageName) {
        packageList.add(new PackageItem(packageName, sourceStr));
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    ((AbstractTableModel) packageTable.getModel()).fireTableDataChanged();
                }
            });
        }
    }

    public int getPackageType(String packageName) {

        for (PackageItem item : packageList) {
            if (item.name.equals(packageName)) {
                if (item.type.equals(sourceStr)) return 1;
                return 0;
            }
        }
        return 0;
    }
    
    public int getResult() {
        return dialogResult;
    }

    public static void main(String[] args) {
        InstallPackageDialog dialog = new InstallPackageDialog(new JFrame().getContentPane());

        dialog.setLibrary("/library/path");

        dialog.addPackage("AER");
        dialog.addPackage("AGFIU");
        dialog.addPackage("OIUPOIO4");
        dialog.addPackage("sfg44d");
        dialog.addPackage("KJ778");
        dialog.addPackage("ASKK5");
        dialog.addPackage("ZXff4");

        dialog.setVisible(true);

        System.exit(0);
    }

}
