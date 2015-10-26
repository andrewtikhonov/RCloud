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

import uk.ac.ebi.rcloud.common.components.dialog.JDialogExt;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.http.exception.BadLoginPasswordException;
import uk.ac.ebi.rcloud.http.exception.ConnectionFailedException;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.dialogs.login.LoginInternalActions;
import workbench.report.BugReport;
import workbench.runtime.RuntimeEnvironment;
import workbench.util.ButtonUtil;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 28, 2009
 * Time: 5:36:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginPanel extends JPanel {
    private static Logger log = LoggerFactory.getLogger(LoginPanel.class);
    
    // login
    public JButton loginButton;
    public JTextField  userLogin;
    public JPasswordField userPassword;

    private LoginInternalActions actions;
    private RuntimeEnvironment runtime;

    private Runnable loginUser;
    private Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private Cursor defcur = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    public LoginPanel(RuntimeEnvironment runtime, LoginInternalActions internalActions) {

        this.actions = internalActions;
        this.runtime = runtime;

        setupUI();
        initActions();
    }

    private void initActions(){

        // login user
        //
        loginUser = new Runnable(){
            public void run(){

                final String login    = userLogin.getText().trim();
                final String password = new String( userPassword.getPassword() );

                LoginPanel.this.setCursor(wait);

                try {
                    if (runtime.getUser() != null) {
                        //logoffUser.run();
                    }

                    // reset user
                    runtime.setUser(null);

                    // validate fields
                    if (login.equals("")) {
                        warningMessage("Please enter your username.");

                        EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                userLogin.requestFocus();
                            }
                        });

                        return;
                    }

                    // validate fields
                    //
                    if (password.equals("")) {
                        warningMessage("Please enter your password.");


                        EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                userPassword.requestFocus();
                            }
                        });

                        return;
                    }

                    HashMap<String, Object> parameters = new HashMap<String, Object>();

                    parameters.put("username", login);
                    parameters.put("password", password);

                    try {
                        boolean user_logged_in = ServerRuntimeImpl.loginUser(
                                runtime.getSession(), parameters);

                        if (!user_logged_in) {
                            warningMessage("Please check username and password are correct.");
                            return;
                        }

                        runtime.setUser(runtime.getDAOLayer().getUserByUsername(login));
                        actions.okAction();
                        
                    } catch (BadLoginPasswordException ex) {
                        warningMessage("Please check username and password are correct.\n" +
                                       "Please register, if you are not registered.");
                    } catch (ConnectionFailedException ex) {

                        boolean report = BugReport.reportAProblem(LoginPanel.this,
                                "Sorry, we could not connect to the Registration Server.\n");

                        if (report) {
                            BugReport.showReportDialog(LoginPanel.this, runtime,
                                    "problem connecting to the registration server",
                                    BugReport.stackTraceToString(ex));
                        }

                    } catch (Exception ex) {

                        boolean report = BugReport.reportAProblem(LoginPanel.this,
                                "Sorry, we could not log you in. Registration server " +
                                "responded unexpectedly.\n");

                        if (report) {
                            BugReport.showReportDialog(LoginPanel.this, runtime,
                                    "registration server unexpected response",
                                    BugReport.stackTraceToString(ex));
                        }
                    }

                } finally {
                    LoginPanel.this.setCursor(defcur);
                }
            }
        };
    }

    private void setupUI(){
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(40,40,40,40));
        setOpaque(false);

        JPanel leftPanel = new JPanel(new GridLayout(0, 1));
        leftPanel.setOpaque(false);

        JLabel label1 = new JLabel("Username: ");
        JLabel label2 = new JLabel("Password: ");

        leftPanel.add(label1);
        leftPanel.add(label2);
        leftPanel.add(new JLabel(" "));
        leftPanel.add(new JLabel(" "));
        leftPanel.add(new JLabel(" "));
        leftPanel.add(new JLabel(" "));

        loginButton = ButtonUtil.makeButton("Sign In");

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(loginUser).start();
            }
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton, BorderLayout.WEST);

        userLogin = new JTextField();
        userPassword = new JPasswordField();

        final JPanel rightPanel = new JPanel(new GridLayout(0, 1));
        rightPanel.setOpaque(false);

        rightPanel.add(userLogin);
        rightPanel.add(userPassword);
        rightPanel.add(buttonPanel);
        rightPanel.add(new JLabel(" "));
        rightPanel.add(new JLabel(" "));
        rightPanel.add(new JLabel(" "));

        final JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.NORTH);
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
            SwingUtilities.getRootPane(this).setDefaultButton(loginButton);
        } catch (Exception ex) {}
    }

    public void resetUI(){
    }

    public void warningMessage(String message) {
        JOptionPaneExt.showMessageDialog(LoginPanel.this,
                message, "",
                JOptionPaneExt.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        JButton run = ButtonUtil.makeButton("run");

        final JDialogExt dialog = new JDialogExt(frame, "dialog");
        //final JDialog dialog = new JDialog(frame, "dialog");

        LoginPanel loginPanel = new LoginPanel(null,null);


        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        };

        loginPanel.registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        dialog.setLayout(new BorderLayout());
        dialog.add(loginPanel, BorderLayout.CENTER);
        dialog.setSize(new Dimension(450,350));
        dialog.setLocationRelativeTo(frame);

        run.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                dialog.setVisible(true);
            }
        });

        frame.add(run);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);

        //System.exit(0);
    }


}
