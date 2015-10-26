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
package workbench.dialogs.login.panel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.http.proxy.ProxySettings;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import workbench.RGui;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 14, 2011
 * Time: 6:32:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxyPanel extends JPanel {

    final private static Logger log = LoggerFactory.getLogger(ProxyPanel.class);

    private JRadioButton systemSettingsRadio = new JRadioButton("Use system settings");
    private JRadioButton customSettingsRadio = new JRadioButton("Use custom settings");

    private JTextField hostField = new JTextField();
    private JTextField portField = new JTextField();
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    private ImageIcon passedIcon = new ImageIcon(ImageLoader.load("/dialog/images/accept.png"));
    private ImageIcon failedIcon = new ImageIcon(ImageLoader.load("/dialog/images/error.png"));

    private RadioActionListener radioActionListener = new RadioActionListener();

    private JButton apply   = ButtonUtil.makeButton("Apply");

    private JLabel testStatusLabel = new JLabel(" ");
    private RGui rgui;

    private void resetUI() {
        testStatusLabel.setText(" ");
        testStatusLabel.setForeground(Color.BLACK);
        testStatusLabel.setIcon(null);
    }

    public ProxyPanel(RGui rgui) {

        this.rgui = rgui;

        this.setBackground(new Color(30,30,30));

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(10,20,10,20));
        setOpaque(false);

        JPanel radioPanel = new JPanel(new GridLayout(2, 1));
        radioPanel.setOpaque(false);

        systemSettingsRadio.setOpaque(false);
        customSettingsRadio.setOpaque(false);

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
        bottomPanel.setBorder(new EmptyBorder(2,2,2,2));
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
        buttonBox.add(apply);

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == apply) {
                    okMethod();
                }
            }
        };

        this.add(commonPanel, BorderLayout.CENTER);
        this.add(buttonBox, BorderLayout.SOUTH);

        apply.addActionListener(buttonListener);

        systemSettingsRadio.addActionListener(radioActionListener);
        customSettingsRadio.addActionListener(radioActionListener);

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

    public ProxySettings assembleProxySettings() {
        ProxySettings settings = new ProxySettings();

        settings.PROXY_HOST = hostField.getText();
        settings.PROXY_PORT = (portField.getText().length() > 0) ? Integer.decode(portField.getText()) : 0;
        settings.USERNAME   = usernameField.getText();
        settings.PASSWORD   = new String(passwordField.getPassword());

        return settings;
    }

    public void applySettings() {
        //log.info("applySettings");

        if (systemSettingsRadio.isSelected()) {
            ServerRuntimeImpl.detectAndApplyProxySettings();

        } else if (customSettingsRadio.isSelected()) {

            ServerRuntimeImpl.applyProxySettings(assembleProxySettings());
        }
    }

    public void okMethod() {
        //log.info("testMethod");

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

                        rgui.persistProxySettings(systemSettingsRadio.isSelected(),
                                assembleProxySettings());

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

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            initUI();
        }
        super.setVisible(visible);
    }

    public void initUI(){
        try {
            boolean proxyauto = rgui.getRuntime().getProxyAuto();
            ProxySettings settings = rgui.getRuntime().getProxySettings();

            systemSettingsRadio.setSelected(proxyauto);
            customSettingsRadio.setSelected(!proxyauto);
            radioActionListener.actionPerformed(null);

            if (!proxyauto) {
                if (settings != null) {
                    hostField.setText(settings.PROXY_HOST);
                    portField.setText(Integer.toString(settings.PROXY_PORT));
                    usernameField.setText(settings.USERNAME);
                    passwordField.setText(settings.PASSWORD);
                }
            }

            SwingUtilities.getRootPane(this).setDefaultButton(apply);

            //testStatusLabel.setText(" ");
            //testStatusLabel.setForeground(Color.BLACK);
            //testStatusLabel.setIcon(null);


        } catch (Exception ex) {}
    }

    /*
    public static void main(String[] args) {

        ProxySettingsDialog dialog = new ProxySettingsDialog(null, new JFrame().getContentPane());
        dialog.setModal(true);

        dialog.setVisible(true);
        System.exit(0);
    }
    */

}
