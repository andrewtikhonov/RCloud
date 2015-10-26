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
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 30, 2010
 * Time: 5:41:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class FindInPathDialog extends JDialogA {

    private static Logger log = LoggerFactory.getLogger(FindInPathDialog.class);

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int dialogresult = CANCEL;

    private JComboBox  patternField;
    private JComboBox  maskField;
    private JTextField directoryField;

    private JCheckBox  caseSensitiveOption;
    private JCheckBox  wholeWordsOption;
    private JCheckBox  recursiveOption;
    private JCheckBox  useMaskOption;

    private JButton find = ButtonUtil.makeButton("Find");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    private static FindInPathDialog _dialog = null;
    private static final Integer singletonLock = new Integer(0);

    private String pattern = null;
    private String mask = null;

    public static FindInPathDialog getInstance(Component c) {

        if (_dialog != null) {
            _dialog.resetUI();
            _dialog.setPattern(_dialog.getPattern());
            return _dialog;
        }
        synchronized (singletonLock) {
            if (_dialog == null) {
                _dialog = new FindInPathDialog(c);
                _dialog.setPattern("text to find");
                _dialog.setMask("*.*");
                _dialog.resetUI();
            }
            return _dialog;
        }
    }

    private void resetUI(){
        dialogresult = CANCEL;
    }

    public void setVisible(boolean visible) {

        if (visible) {
            patternField.setSelectedIndex(patternField.getItemCount() - 1);
            maskField.setSelectedIndex(maskField.getItemCount() - 1);
        }

        super.setVisible(visible);
    }

    public FindInPathDialog(Component c) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10,10,10,10));

        JPanel fieldsPanel = new JPanel(new BorderLayout());
        fieldsPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        buttonPanel.setOpaque(false);

        buttonPanel.add(cancel);
        buttonPanel.add(find);

        // text to find
        //

        JPanel patternPanel = new JPanel(new BorderLayout());
        patternPanel.setOpaque(false);

        patternField = new JComboBox();
        patternField.setEditable(true);
        
        patternPanel.add(new JLabel("Text to find: "), BorderLayout.WEST);
        patternPanel.add(patternField, BorderLayout.CENTER);

        /*

        Useful for debugging

        ActionListener patternFieldListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                //maskField.setEnabled(useMaskOption.isSelected());

                JComboBox cb = (JComboBox)event.getSource();
                String newSelection = (String)cb.getSelectedItem();
                //setPattern(newSelection);
                log.info("patternFieldListener-actionPerformed pattern="+newSelection);
            }
        };

        patternField.addActionListener(patternFieldListener);
        */

        // options
        //

        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        JPanel optionsInnerPanel = new JPanel(new GridLayout(2,1));

        optionsPanel.add(optionsInnerPanel, BorderLayout.NORTH);

        caseSensitiveOption = new JCheckBox("Case sensitive");
        wholeWordsOption = new JCheckBox("Whole words only");

        optionsInnerPanel.add(caseSensitiveOption);
        optionsInnerPanel.add(wholeWordsOption);


        // scope
        //

        JPanel scopePanel = new JPanel(new BorderLayout());
        scopePanel.setOpaque(false);
        scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));

        JPanel scopeLabelPanel = new JPanel(new GridLayout(2,1));
        JPanel scopeFieldPanel = new JPanel(new GridLayout(2,1));
        scopeLabelPanel.setOpaque(false);
        scopeFieldPanel.setOpaque(false);

        scopePanel.add(scopeLabelPanel, BorderLayout.WEST);
        scopePanel.add(scopeFieldPanel, BorderLayout.CENTER);

        recursiveOption = new JCheckBox("Recursively");
        recursiveOption.setSelected(true);
        directoryField = new JTextField();

        scopeLabelPanel.add(new JLabel("Directory: "));
        scopeLabelPanel.add(recursiveOption);

        scopeFieldPanel.add(directoryField);
        scopeFieldPanel.add(new JLabel(" "));

        // file mask
        //

        JPanel maskPanel = new JPanel(new BorderLayout());
        maskPanel.setOpaque(false);
        maskPanel.setBorder(BorderFactory.createTitledBorder("File name filter"));

        useMaskOption = new JCheckBox("File mask");
        maskField = new JComboBox();
        maskField.setEditable(true);
        maskField.setEnabled(false);

        maskPanel.add(useMaskOption, BorderLayout.WEST);
        maskPanel.add(maskField, BorderLayout.CENTER);


        // assembly
        //

        JPanel allFieldsPanel = new JPanel(new BorderLayout());
        allFieldsPanel.setOpaque(false);

        fieldsPanel.add(optionsPanel, BorderLayout.NORTH);
        fieldsPanel.add(scopePanel, BorderLayout.CENTER);
        fieldsPanel.add(maskPanel, BorderLayout.SOUTH);

        allFieldsPanel.add(patternPanel, BorderLayout.NORTH);
        allFieldsPanel.add(fieldsPanel, BorderLayout.SOUTH);

        container.add(allFieldsPanel, BorderLayout.NORTH);
        container.add(buttonPanel, BorderLayout.SOUTH);


        ActionListener maskOptionListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                maskField.setEnabled(useMaskOption.isSelected());
            }
        };

        useMaskOption.addActionListener(maskOptionListener);

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == find) {
                    okMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        setLayout(new BorderLayout());

        add(container, BorderLayout.CENTER);
        setTitle("Find in path");

        find.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        this.getRootPane().setDefaultButton(find);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setPreferredSize(new Dimension(550, 380));

        setLocationRelativeTo(c);
        setResizable(true);
    }

    public void okMethod() {
        patternField.requestFocus();

        String mask = maskField.getSelectedItem().toString();
        String patt = patternField.getSelectedItem().toString();

        setVisible(false);

        dialogresult = OK;

        if (useMaskOption.isSelected()) {
            setMask(mask);
        }

        setPattern(patt);
    }

    public void cancelMethod() {
        dialogresult = CANCEL;
        setVisible(false);
    }


    public void setItemInternal(JComboBox box, String value) {

        int cnt = box.getItemCount();

        for (int i=0;i < cnt;i++) {
            String item = (String) box.getItemAt(i);

            if (item.equals(value)) {
                box.setSelectedIndex(i);
                return;
            }
        }

        box.addItem(value);

        //log.info("setItemInternal value="+value);

        //box.setSelectedIndex(maskField.getItemCount() - 1);
        //box.getEditor().selectAll();
    }

    // path
    //

    public String getDirectory() {
        return directoryField.getText();
    }

    public void setDirectory(final String directory) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    directoryField.setText(directory);
                }
            });
        } else {
            directoryField.setText(directory);
        }
    }


    // pattern
    //

    public String getPattern() {
        //return patternField.getSelectedItem().toString();
        return this.pattern;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;

        if (isVisible()) {
            //log.info("setPattern-visible pattern="+pattern);

            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    setItemInternal(patternField, pattern);
                }
            });
        } else {
            setItemInternal(patternField, pattern);
            //log.info("setPattern-invisible pattern="+pattern);
        }
    }

    // mask
    //

    public String getMask() {
        //return maskField.getSelectedItem().toString();
        return this.mask;
    }

    public void setMask(final String mask) {
        this.mask = mask; 

        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    setItemInternal(maskField, mask);
                }
            });
        } else {
            setItemInternal(maskField, mask);
        }
    }

    // use mask
    //

    public boolean isUseMask() {
        return useMaskOption.isSelected();
    }

    public void setUseMask(final boolean selected) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    useMaskOption.setSelected(selected);
                }
            });
        } else {
            useMaskOption.setSelected(selected);
        }
    }

    // case sensitive
    //

    public boolean isCaseSensitive() {
        return caseSensitiveOption.isSelected();
    }

    public void setCaseSensitive(final boolean selected) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    caseSensitiveOption.setSelected(selected);
                }
            });
        } else {
            caseSensitiveOption.setSelected(selected);
        }
    }

    // whole word
    //

    public boolean isWholeWords() {
        return wholeWordsOption.isSelected();
    }

    public void setWholeWords(final boolean selected) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    wholeWordsOption.setSelected(selected);
                }
            });
        } else {
            wholeWordsOption.setSelected(selected);
        }
    }

    // recursive
    //

    public boolean isRecursive() {
        return recursiveOption.isSelected();
    }

    public void setRecursive(final boolean selected) {
        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    recursiveOption.setSelected(selected);
                }
            });
        } else {
            recursiveOption.setSelected(selected);
        }
    }

    public int getResult() {
        return dialogresult;
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame();

        frame.setLayout(new BorderLayout());

        JButton button = ButtonUtil.makeButton("Find");

        final FindInPathDialog diaog = FindInPathDialog.getInstance(frame.getContentPane());

        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                diaog.setVisible(true);

                log.info("pattern = "+ diaog.getPattern()+ " mask = "+ diaog.getMask());
            }
        });

        frame.add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
