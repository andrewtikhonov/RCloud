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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 04/07/2012
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
public class PackageTemplateDialog extends JDialogA {

    final private Logger log = LoggerFactory.getLogger(getClass());

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int dialogResult    = CANCEL;

    private JTextField packageNameField;

    private JButton create  = ButtonUtil.makeButton("Create");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    private static PackageTemplateDialog dialog  = null;
    private static final Integer singletonLock   = new Integer(0);

    private JTable objectsTable;
    private JScrollPane objectsTableScroll;
    private PackageTemplateTableModel objectsTableModel;

    public static PackageTemplateDialog getInstance(Component c, RGui rgui) {

        if (dialog != null) {
            dialog.loadObjects();
            return dialog;
        }
        synchronized (singletonLock) {
            if (dialog == null) {
                dialog = new PackageTemplateDialog(c, rgui);
            }
            return dialog;
        }
    }

    public PackageTemplateDialog(Component c, RGui rgui) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

        JPanel fieldsPanel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));

        fieldsPanel.setOpaque(false);
        tablePanel.setOpaque(false);
        buttonPanel.setOpaque(false);

        container.add(fieldsPanel, BorderLayout.NORTH);
        container.add(tablePanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        buttonPanel.add(cancel);
        buttonPanel.add(create);

        packageNameField = new JTextField("package");

        fieldsPanel.add(packageNameField, BorderLayout.NORTH);
        //fieldsPanel.add(new JLabel("Include Objects Into Package"), BorderLayout.SOUTH);

        objectsTable       = new JTable();
        objectsTableScroll = new JScrollPane(objectsTable);
        objectsTableModel  = new PackageTemplateTableModel(rgui);

        // center
        tablePanel.add(objectsTableScroll, BorderLayout.CENTER);
        tablePanel.setBorder(new TitledBorder(new EmptyBorder(10,0,10,0), "Include Objects Into Package"));

        objectsTableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //PackageTemplateRenderer renderer = new PackageTemplateRenderer();

        objectsTable.setGridColor(Color.LIGHT_GRAY);
        objectsTable.setModel(objectsTableModel);
        //objectsTable.setDefaultRenderer(String.class, renderer);
        //objectsTable.setRowHeight(20);
        objectsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        objectsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        objectsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        objectsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        objectsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        objectsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        //objectsTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(typeCombo));

        objectsTable.getTableHeader().setPreferredSize(new Dimension(2, 16));

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == create) {
                    okMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        setLayout(new BorderLayout());

        add(container, BorderLayout.CENTER);
        setTitle("Package Template");

        create.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        this.getRootPane().setDefaultButton(create);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        FocusListener selectTextListener = new FocusListener() {
            public void focusGained(FocusEvent event) {
                Object obj = event.getSource();
                if (obj instanceof JTextComponent) {
                    JTextComponent source = (JTextComponent) obj;

                    source.setSelectionStart(0);
                    source.setSelectionEnd(source.getText().length());
                }
            }
            public void focusLost(FocusEvent event) {
            }
        };

        packageNameField.addFocusListener(selectTextListener);

        setPreferredSize(new Dimension(550, 440));
        //pack();
        setLocationRelativeTo(c);
        setResizable(false);

        loadObjects();
    }

    public void okMethod() {
        dialogResult = OK;
        setVisible(false);
    }

    public void cancelMethod() {
        dialogResult = CANCEL;
        setVisible(false);
    }

    public String getPackageName() {
        return packageNameField.getText();
    }

    public Vector<String> getObjects() {
        return objectsTableModel.getSelectedObjects();
    }

    public void loadObjects() {
        objectsTableModel.loadGlabalObjects();
    }

    public int getResult() {
        return dialogResult;
    }

    public static void main(String[] args) {
        //PackageTemplateDialog dialog = new PackageTemplateDialog(new JFrame().getContentPane());
        //dialog.setVisible(true);
        //System.exit(0);
    }

}
