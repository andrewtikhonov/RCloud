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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.textfield.JHintTextField;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 18, 2010
 * Time: 11:19:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompressFilesDialog extends JDialogA {

    final private Logger log = LoggerFactory.getLogger(getClass());

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int dialogResult = CANCEL;

    public static final String[] archName = { ".zip", ".tar.gz" };
    public static final String[] archExtension = { ".zip", ".tar.gz" };
    public static final String[] archCommand = { "zip -r", "tar -czvf" };

    private JComboBox  optionsCombo = new JComboBox(archName);
    private JTextField fileField;
    private JTextField argsField;

    private JButton compress = ButtonUtil.makeButton("Compress");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    private static CompressFilesDialog dialog = null;
    private static final Integer singletonLock = new Integer(0);

    public static CompressFilesDialog getInstance(Component c) {

        if (dialog != null) {
            return dialog;
        }
        synchronized (singletonLock) {
            if (dialog == null) {
                dialog = new CompressFilesDialog(c);
            }
            return dialog;
        }
    }

    public CompressFilesDialog(Component c) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        //log.info("super inited");

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

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
        buttonPanel.add(compress);

        fileField = new JTextField("zip filename");
        argsField = new JTextField("arguments");

        optionsPanel.add(optionsCombo, BorderLayout.NORTH);
        optionsPanel.add(fileField, BorderLayout.CENTER);
        optionsPanel.add(argsField, BorderLayout.SOUTH);


        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == compress) {
                    okMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        setLayout(new BorderLayout());

        add(container, BorderLayout.CENTER);
        setTitle("Compress Files");

        compress.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        this.getRootPane().setDefaultButton(compress);

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

        fileField.addFocusListener(selectTextListener);
        argsField.addFocusListener(selectTextListener);

        setPreferredSize(new Dimension(350, 240));
        //pack();
        setLocationRelativeTo(c);
        setResizable(false);
    }

    public void okMethod() {
        dialogResult = OK;
        setVisible(false);
    }

    public void cancelMethod() {
        dialogResult = CANCEL;
        setVisible(false);
    }

    public String getFilename() {
        return fileField.getText();
    }

    public void setFilename(final String name) {
        fileField.setText(name);
        /*
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                fileField.setText(name);
            }
        });
        */
    }

    public String getArguments() {
        return argsField.getText();
    }

    public String getArchiverCommand() {
        return archCommand[optionsCombo.getSelectedIndex()];
    }

    public String getArchiverExtension() {
        return archExtension[optionsCombo.getSelectedIndex()];
    }

    public int getResult() {
        return dialogResult;
    }

    public static void main(String[] args) {
        CompressFilesDialog dialog = new CompressFilesDialog(new JFrame().getContentPane());

        dialog.setVisible(true);

        System.exit(0);
    }

}

