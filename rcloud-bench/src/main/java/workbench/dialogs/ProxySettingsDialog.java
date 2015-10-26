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
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.http.proxy.ProxySettings;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import workbench.RGui;
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
 * Date: Feb 11, 2011
 * Time: 5:33:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxySettingsDialog extends JDialogA {
    final private static Logger log = LoggerFactory.getLogger(ProxySettingsDialog.class);

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    private int    _result  = CANCEL;

    private JRadioButton systemSettingsRadio = new JRadioButton("Use system proxy settings");
    private JRadioButton customSettingsRadio = new JRadioButton("Use custom proxy settings");

    private JTextField hostField = new JTextField();
    private JTextField portField = new JTextField();
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    private ImageIcon passedIcon = new ImageIcon(ImageLoader.load("/dialog/images/accept.png"));
    private ImageIcon failedIcon = new ImageIcon(ImageLoader.load("/dialog/images/error.png"));

    private JButton ok      = ButtonUtil.makeButton("OK");
    private JButton test    = ButtonUtil.makeButton("Test");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    private JLabel testStatusLabel = new JLabel(" ");
    private RGui rgui;

    private static ProxySettingsDialog _dialog = null;
    private static final Integer singletonLock = new Integer(0);

    public static ProxySettingsDialog getInstance(RGui rgui, Component c) {

        if (_dialog != null) {
            _dialog.resetUI();
            return _dialog;
        }
        synchronized (singletonLock) {
            if (_dialog == null) {
                _dialog = new ProxySettingsDialog(rgui, c);
                _dialog.resetUI();
            }
            return _dialog;
        }
    }

    private void resetUI(){
        backupSettings();
        testStatusLabel.setText(" ");
        testStatusLabel.setForeground(Color.BLACK);
        testStatusLabel.setIcon(null);
        _result = CANCEL;
    }

    private ProxySettingsDialog(RGui rgui, Component c) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        this.rgui = rgui;

        JPanel radioPanel = new JPanel(new GridLayout(2, 1));
        radioPanel.setOpaque(false);

        radioPanel.add(systemSettingsRadio);
        radioPanel.add(customSettingsRadio);

		ButtonGroup proxyButtonGroup = new ButtonGroup();
		proxyButtonGroup.add(systemSettingsRadio);
		proxyButtonGroup.add(customSettingsRadio);

        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setOpaque(false);
        configPanel.setBorder(new EmptyBorder(0,10,0,10));

        JPanel leftPanel = new JPanel(new GridLayout(4, 1));
        JPanel rightPanel = new JPanel(new GridLayout(4, 1));
        leftPanel.setOpaque(false);
        rightPanel.setOpaque(false);

        leftPanel.add(new JLabel("Proxy host:"));
        leftPanel.add(new JLabel("Proxy port:"));
        leftPanel.add(new JLabel("Proxy username:"));
        leftPanel.add(new JLabel("Proxy password:"));

        rightPanel.add(hostField);
        rightPanel.add(portField);
        rightPanel.add(usernameField);
        rightPanel.add(passwordField);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(2,10,2,10));
        bottomPanel.add(testStatusLabel);


        configPanel.add(leftPanel, BorderLayout.WEST);
        configPanel.add(rightPanel, BorderLayout.CENTER);
        configPanel.add(bottomPanel, BorderLayout.SOUTH);

        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setOpaque(false);
        settingsPanel.add(configPanel, BorderLayout.NORTH);

        JPanel commonPanel = new JPanel(new BorderLayout());
        commonPanel.setOpaque(false);
        commonPanel.add(radioPanel, BorderLayout.NORTH);
        commonPanel.add(settingsPanel, BorderLayout.SOUTH);

        JPanel buttonBox = new JPanel(new FlowLayout());
        buttonBox.setOpaque(false);
        buttonBox.add(ok);
        buttonBox.add(test);
        buttonBox.add(cancel);

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == ok) {
                    okMethod();
                }
                if (event.getSource() == test) {
                    testMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        JPanel globalPanel = new JPanel(new BorderLayout());
        globalPanel.setOpaque(false);

        globalPanel.add(commonPanel, BorderLayout.CENTER);
        globalPanel.add(buttonBox, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Proxy Settings", globalPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        setTitle("Proxy Configuration");

        ok.addActionListener(buttonListener);
        test.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);


        this.getRootPane().setDefaultButton(ok);
        
        setSize(new Dimension(450, 340));
        setLocationRelativeTo(c);
        setResizable(false);

        RadioActionListener radioActionListener = new RadioActionListener();

        systemSettingsRadio.addActionListener(radioActionListener);
        customSettingsRadio.addActionListener(radioActionListener);

        systemSettingsRadio.setSelected(true);
        radioActionListener.actionPerformed(null);

    }

    class RadioActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            if (systemSettingsRadio.isSelected()) {
                hostField.setEnabled(false);
                portField.setEnabled(false);
                usernameField.setEnabled(false);
                passwordField.setEnabled(false);
            }
            if (customSettingsRadio.isSelected()) {
                hostField.setEnabled(true);
                portField.setEnabled(true);
                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
            }
        }
    }

    private ProxySettings backupProxySettings
            = ServerRuntimeImpl.getProxySettings();

    public void backupSettings() {
        log.info("backupSettings");

        backupProxySettings = ServerRuntimeImpl.getProxySettings();
    }

    public void restoreSettings() {
        log.info("restoreSettings");

        ServerRuntimeImpl.applyProxySettings(backupProxySettings);
    }

    public ProxySettings assembleProxySettings() {
        ProxySettings settings = new ProxySettings();

        settings.PROXY_HOST = hostField.getText();
        settings.PROXY_PORT = (portField.getText().length() > 0) ? Integer.decode(portField.getText()) : 0;
        settings.USERNAME   = usernameField.getText();
        settings.PASSWORD   = new String(passwordField.getPassword());

        return settings;
    }

    public void applySettings() {
        log.info("applySettings");

        if (systemSettingsRadio.isSelected()) {
            ServerRuntimeImpl.detectAndApplyProxySettings();

        } else if (customSettingsRadio.isSelected()) {

            ServerRuntimeImpl.applyProxySettings(assembleProxySettings());
        }
    }

    public void testMethod() {
        log.info("testMethod");

        applySettings();

        EventQueue.invokeLater(new Runnable(){
            public void run (){
                testStatusLabel.setForeground(Color.BLACK);
                testStatusLabel.setIcon(null);
                testStatusLabel.setText("Testing..");
            }
        });

        new Thread(new Runnable(){
            public void run(){
                try {
                    if (ServerRuntimeImpl.isConnectionAlive(rgui.getRuntime().getSession())) {

                        EventQueue.invokeLater(new Runnable(){
                            public void run (){
                                testStatusLabel.setForeground(Color.GREEN.darker().darker());
                                testStatusLabel.setIcon(passedIcon);
                                testStatusLabel.setText("OK");
                            }
                        });

                    }
                } catch (Exception ex) {

                    EventQueue.invokeLater(new Runnable(){
                        public void run (){
                            testStatusLabel.setForeground(Color.RED.darker().darker());
                            testStatusLabel.setIcon(failedIcon);
                            testStatusLabel.setText("Failed");
                        }
                    });
                }

            }
        }).start();
    }

    public void okMethod() {
        log.info("okMethod");

        applySettings();

        rgui.persistProxySettings(systemSettingsRadio.isSelected(), 
                assembleProxySettings());

        _result = OK;
        setVisible(false);
    }

    public void cancelMethod() {
        log.info("cancelMethod");

        restoreSettings();

        _result = CANCEL;
        setVisible(false);
    }

    public int getResutl() {
        return _result;
    }

    public static void main(String[] args) {

        ProxySettingsDialog dialog = new ProxySettingsDialog(null, new JFrame().getContentPane());
        dialog.setModal(true);

        dialog.setVisible(true);
        System.exit(0);
    }

}
