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

import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogExt;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.util.ButtonUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 27, 2009
 * Time: 4:36:53 PM
 * To change this template use File | Settings | File Templates.
 */

public class SaveDialog extends JDialogA { //

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  static int FILE     = 0;
    public  static int FUNCTION = 1;

    //private boolean init0 = true;
    private String _name    = "";
    private int    _option  = FILE;
    private int    _result  = CANCEL;

    private String[]  items = {"save into a file", "source as a function"};
    private JComboBox  cb   = new JComboBox(items);
    private JTextField text = new JTextField();
    private JButton ok      = ButtonUtil.makeButton("OK");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    private static SaveDialog _dialog = null;
    private static final Integer singletonLock = new Integer(0);

    public static SaveDialog getInstance(Component c) {

        if (_dialog != null) {
            _dialog.resetUI();
            return _dialog;
        }
        synchronized (singletonLock) {
            if (_dialog == null) {
                _dialog = new SaveDialog(c);
                _dialog.resetUI();
            }
            return _dialog;
        }
    }

    private void resetUI(){
        _result = CANCEL;
    }
    
    public SaveDialog(Component c) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        JPanel g = new JPanel(new BorderLayout());
        g.setOpaque(false);

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.setOpaque(false);
        p.add(new JLabel("Select the way to save it"));
        p.add(cb);
        p.add(new JLabel("Give it a name"));
        p.add(text);

        JPanel b = new JPanel(new FlowLayout());
        b.setOpaque(false);
        b.add(ok);
        b.add(cancel);

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == ok) {
                    okMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        g.add(p, BorderLayout.CENTER);
        g.add(b, BorderLayout.SOUTH);

        JTabbedPane t = new JTabbedPane();
        t.addTab("Choose the way to save the code", g);

        setLayout(new BorderLayout());
        add(t, BorderLayout.CENTER);
        setTitle("Save As..");

        ok.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        setSize(new Dimension(450, 280));
        setLocationRelativeTo(c);
        setResizable(false);

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                switch(_option)
                {
                    case 0: cb.setSelectedIndex(0); break;
                    case 1: cb.setSelectedIndex(1); break;
                }

                text.requestFocus();
            }
        });
    }

    public void okMethod() {
        _name   = text.getText();
        switch (cb.getSelectedIndex()) {
            case 0: _option = FILE;     break;
            case 1: _option = FUNCTION; break;
            default: _option = FILE;     break;
        }

        if (_name == null || _name.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Name must be provided", "alert", JOptionPane.ERROR_MESSAGE);

            _result = CANCEL;
            return;
        }

        //log.info("n="+_name+" o="+_option);

        _result = OK;

        setVisible(false);
    }

    public void cancelMethod() {
        setVisible(false);
        _result = CANCEL;
    }

    public String getName() {
        return _name;
    }

    public void setFName(String name) {
        _dialog.text.setText(name);
        _dialog._name = name;
    }

    public int getOption() {
        return _option;
    }

    public void setOption(int option) {
        _dialog.cb.setSelectedIndex(option);
        _dialog._option = option;
    }

    public int getResult() {
        return _result;
    }

    public void visialize() {
        _dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SaveDialog dialog = new SaveDialog(new JFrame().getContentPane());
        dialog.setModal(true);

        dialog.setVisible(true);
        System.exit(0);
    }
    
}

