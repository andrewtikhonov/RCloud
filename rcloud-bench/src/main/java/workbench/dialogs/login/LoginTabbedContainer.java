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
package workbench.dialogs.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;
import workbench.dialogs.ProxySettingsDialog;
import workbench.dialogs.login.panel.LoginPanel;
import workbench.dialogs.login.panel.ProxyPanel;
import workbench.dialogs.login.panel.RegisterPanel;
import workbench.dialogs.login.panel.RestorePanel;
import workbench.runtime.RuntimeEnvironment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 7, 2009
 * Time: 5:47:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginTabbedContainer extends JDialogA {// JDialogExt {
    final private static Logger log = LoggerFactory.getLogger(LoginTabbedContainer.class);

    // dialog instance
    private static LoginTabbedContainer dialog = null;
    private static final Integer singletonLock = new Integer(0);

    // persistance properties
    private static String userLoginProp  = "USER_LOGIN";
    private static String userPasswordProp = "USER_PASSWORD";

    // rgui
    private RGui rgui = null;

    // result
    public static int OK = 0;
    public static int CANCEL = 1;
    private int result = CANCEL;

    // actions and providers
    public static LoginInternalActions actions = null;
    private static RuntimeEnvironment runtime = null;


    // components
    private JPanel     contentPane;
    public JTabbedPane tabbedPane1;

    // panels
    public LoginPanel loginPanel;
    public RestorePanel restorePanel;
    public RegisterPanel registerPanel;
    public ProxyPanel proxyPanel;

    // buttons
    //public JButton settingsButton = new JButton("Proxy Settings");

    public static LoginTabbedContainer getInstance(RGui rgui, RuntimeEnvironment runtime) {

        if (dialog != null) {
            dialog.resetUI();
            return dialog;
        }
        synchronized (singletonLock) {

            if (dialog == null) {
                dialog = new LoginTabbedContainer(rgui, runtime);
                dialog.resetUI();
            }
            return dialog;
        }
    }

    public LoginTabbedContainer(RGui rgui, RuntimeEnvironment runtime) {
        super(rgui != null ? rgui.getRootFrame() : new JFrame(), true);

        //super();

        this.rgui = rgui;
        this.runtime = runtime;

        setupUI();

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                actions.cancelAction();
            }
        });

        Dimension windowSize = new Dimension(500, 480);
        setSize(windowSize);
        setPreferredSize(windowSize);

        setLocationRelativeTo(rgui != null ? rgui.getRootFrame() : null);
        setResizable(true);
    }

    private void setupFields() {
        if (rgui != null) {
            String login = rgui.getProperty(userLoginProp);
            if (login == null) login = "";
            loginPanel.userLogin.setText(login);
        }
    }

    /*
    class SettingsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            ProxySettingsDialog dialog = ProxySettingsDialog.getInstance(rgui, LoginTabbedContainer.this);
            dialog.setVisible(true);
        }
    }
    */

    private void setupUI() {

        actions = new LoginInternalActions(){
            public void okAction() { okMethod(); }
            public void cancelAction() { cancelMethod(); }
        };

        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setOpaque(false);
        contentPane.setPreferredSize(new Dimension(550, 462));

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        contentPane.registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);


        //JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //buttonPanel.setOpaque(false);
        //buttonPanel.add(settingsButton);
        //settingsButton.addActionListener(new SettingsActionListener());

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(550, 100));
        imageLabel.setIcon(new ImageIcon(ImageLoader.load("/images/Plasma_1.jpg")));
        contentPane.add(imageLabel, BorderLayout.NORTH);
        //contentPane.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane1 = new JTabbedPane();/* {
            @Override
            public void paint(Graphics g) {
                g.clearRect(0,0,getWidth(),getHeight());
                super.paint(g);
            }
        };*/

        tabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane1.setOpaque(false);

        contentPane.add(tabbedPane1, BorderLayout.CENTER);

        loginPanel      = new LoginPanel(runtime, actions);
        registerPanel   = new RegisterPanel(runtime, actions, this);
        restorePanel    = new RestorePanel(runtime, actions);
        proxyPanel      = new ProxyPanel(rgui);

        tabbedPane1.addTab("Login", loginPanel);
        tabbedPane1.addTab("Register", registerPanel);
        tabbedPane1.addTab("Restore Password", restorePanel);
        tabbedPane1.addTab("Proxy", proxyPanel);
        tabbedPane1.setOpaque(true);

        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        loginPanel.initUI();
    }
    
    private void resetUI(){
        result = CANCEL;
    }

    public int getResult() {
        return result;
    }

    private void persistState() {
        /*
        providers.getRuntime().setMode(Workbench.HTTP_MODE);
        if (providers.getRuntime().getUser() != null) {
            rgui.setProperty(userLoginProp, providers.getRuntime().getUser().getLogin());
        }
        */
    }

    public void okMethod() {
        result = OK;
        persistState();
        setVisible(false);
    }

    public void cancelMethod() {
        result = CANCEL;
        persistState();
        setVisible(false);
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();

        LoginTabbedContainer loginContainer = LoginTabbedContainer.getInstance(null, null);

        dialog.setVisible(true);
        System.exit(0);
    }

}
