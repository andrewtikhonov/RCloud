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
 * Date: Jun 12, 2009
 * Time: 1:12:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class OptionsDialog extends JDialogA {

    private static OptionsDialog _dialog = null;
    private static final Integer singletonLock
                                = new Integer(0);

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    private int    _result  = CANCEL;

    private JButton ok      = ButtonUtil.makeButton("Apply");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    public String options_cb_app []  = {"above", "below"};
    public String options_cb_act []  = {"select", "close"};
    public JComboBox options_c_cb_app  = null;
    public JComboBox options_h_cb_app  = null;

    public static OptionsDialog getInstance(Component c) {

        if (_dialog != null)
        {
            return _dialog;
        }
        synchronized (singletonLock) {
            if (_dialog == null) {
                _dialog = new OptionsDialog(c);
            }
            return _dialog;
        }
    }

    public OptionsDialog(Component c) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        JPanel global = new JPanel(new BorderLayout());
        global.setOpaque(false);

        JPanel options = new JPanel(new GridLayout(2,2));
        options.setOpaque(false);

        options_c_cb_app = new JComboBox(options_cb_app);
        options_h_cb_app = new JComboBox(options_cb_app);

        options_c_cb_app.setOpaque(false);
        options_h_cb_app.setOpaque(false);

        options.add(new JLabel(" completion box appears"));     options.add(options_c_cb_app);
        options.add(new JLabel(" history box appears"));        options.add(options_h_cb_app);

        //options.add(new JLabel(" completion box TAB action"));  options.add(options_c_cb_act);
        //options.add(new JLabel(" history box TAB action"));     options.add(options_h_cb_act);

        options.setBackground(new Color(0xfffff0));

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.setOpaque(false);

        buttons.add(ok);
        buttons.add(cancel);

        KeyListener keyListener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okMethod();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelMethod();
                }
            }
            public void keyReleased(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
        };

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == ok)
                {
                    okMethod();
                }
                if (event.getSource() == cancel)
                {
                    cancelMethod();
                }
            }
        };

        global.add(options, BorderLayout.CENTER);
        global.add(buttons, BorderLayout.SOUTH);

        JTabbedPane tabbed = new JTabbedPane();
        tabbed.addTab("Console options", global);
        tabbed.setOpaque(false);

        setLayout(new BorderLayout());
        add(tabbed, BorderLayout.CENTER);
        setTitle("Options box");

        ok.addKeyListener(keyListener);
        cancel.addKeyListener(keyListener);

        ok.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        setSize(new Dimension(380, 240));
        setLocationRelativeTo(c);
        setResizable(false);
    }

    public void okMethod()
    {
        _result = OK;
        setVisible(false);
    }

    public void cancelMethod()
    {
        setVisible(false);
        _result = CANCEL;
    }

    public String getCompletionLocation()
    {
        return (String) options_c_cb_app.getSelectedItem();
    }

    public String getHistoryLocation()
    {
        return (String) options_h_cb_app.getSelectedItem();
    }

    public int getResult()
    {
        return _result;
    }

    public static void main(String[] args) {
        OptionsDialog dialog = new OptionsDialog(new JFrame().getContentPane());
        dialog.setModal(true);

        dialog.setVisible(true);
        System.exit(0);
    }
    
}

