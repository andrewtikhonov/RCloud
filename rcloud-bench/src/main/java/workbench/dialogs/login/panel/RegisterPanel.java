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

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogExt;
import uk.ac.ebi.rcloud.common.components.textfield.JHintPasswordField;
import uk.ac.ebi.rcloud.common.components.textfield.JHintTextField;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.http.exception.ConnectionFailedException;
import uk.ac.ebi.rcloud.http.exception.TunnelingException;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.HashMap;
import java.rmi.AlreadyBoundException;
import java.awt.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import workbench.dialogs.login.LoginTabbedContainer;
import workbench.dialogs.login.LoginInternalActions;
import workbench.report.BugReport;
import workbench.runtime.RuntimeEnvironment;
import workbench.util.ButtonUtil;
import workbench.util.OptionsConst;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 28, 2009
 * Time: 5:36:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterPanel extends JPanel {
    final private Logger log = LoggerFactory.getLogger(getClass());


    // register
    private JHintTextField fullnameField;
    private JHintTextField emailField;
    private JHintTextField loginField;
    private JHintTextField contactField;
    private JHintPasswordField passwordField1;
    private JHintPasswordField passwordField2;

    private JLabel fullnameIcon;
    private JLabel emailIcon;
    private JLabel loginIcon;
    private JLabel passwordIcon1;
    private JLabel passwordIcon2;
    private JLabel contactIcon;

    private JButton registerButton;

    private String labelProperty   = "LABEL";
    private String tooltipProperty = "TOOLTIP";

    private ImageIcon okayIcon;
    private ImageIcon errorIcon;

    private Runnable    addUser;
    private FocusListener regListener;

    private LoginTabbedContainer loginTabbedContainer;
    private RuntimeEnvironment runtime;

    private String ENTERNAME = "Please enter your full name (e.g. Bill Clinton)";
    private String ENTEREMAIL = "We use emails for notifications and in communications should you require any help";
    private String ENTERUSERNAME = "Please choose your username";
    private String ENTERPASSWORD = "Please choose password";
    private String RETYPEPASSWORD = "Please retype password";
    private String WRONGPASSWORD = "Passwords do not match";
    private String ENTERPROFILE = "We'd appreciate if you provide your profile and contact details. "+
                                  "It will help us contact you should you need any help.";
    private String CHECKPASSWORD = "Passwords do not match";

    public RegisterPanel(RuntimeEnvironment runtime0, LoginInternalActions internalActions,
                         LoginTabbedContainer container){

        this.runtime = runtime0;
        this.loginTabbedContainer = container;

        setupUI();

        addUser = new Runnable(){
            public void run(){

                final String username   = loginField.getText().trim();
                final String password1  = new String( passwordField1.getPassword() );
                String password2        = new String( passwordField2.getPassword() );
                String email            = emailField.getText().trim();
                String fullname         = fullnameField.getText().trim();
                String profile          = contactField.getText().trim();

                // validate fields
                //
                if ("".equals(fullname))
                {
                    warningMessage(ENTERNAME);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            fullnameField.requestFocus();
                        }
                    });

                    return;
                }

                if ("".equals(email))
                {
                    warningMessage(ENTEREMAIL);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            emailField.requestFocus();
                        }
                    });

                    return;
                }

                if ("".equals(username))
                {
                    warningMessage(ENTERUSERNAME);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            loginField.requestFocus();
                        }
                    });

                    return;
                }

                if ("".equals(password1)) {
                    warningMessage(ENTERPASSWORD);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            passwordField1.requestFocus();
                        }
                    });
                    return;
                }

                if ("".equals(password2)) {
                    warningMessage(RETYPEPASSWORD);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            passwordField2.requestFocus();
                        }
                    });
                    return;
                }

                if (!password2.equals(password2)) {
                    warningMessage(WRONGPASSWORD);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            passwordField2.requestFocus();
                        }
                    });
                    return;
                }

                // check if there are users in DB
                // that have same login or email.
                //
                if (runtime.getDAOLayer() == null) {
                    warningMessage("Sorry, we could not register you in.\n" +
                                   "Workbench database component is not available.\n"+
                                   "Please contact us for help.");
                    return;
                }

                try {
                    UserDataDAO userToCheck = runtime.getDAOLayer().getUserByUsername(username);

                    if (userToCheck != null) {
                        warningMessage("Sorry, the login you provided is in use.\n" +
                                       "Please try another one.");

                        EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                loginField.requestFocus();
                            }
                        });
                        return;
                    }

                    String defaultpool = runtime.getDAOLayer().getOption(OptionsConst.USER_DEFAULT_POOL).getOptionValue();
                    String defaultfolder = runtime.getDAOLayer().getOption(OptionsConst.USER_DEFAULT_FOLDER).getOptionValue();

                    HashMap<String, Object> map = new HashMap<String, Object>();

                    map.put(UserDataDAO.username,    username);
                    map.put(UserDataDAO.password,    password1);
                    map.put(UserDataDAO.status,      UserDataDAO.USER_OFFLINE);
                    map.put(UserDataDAO.userFolder,  defaultfolder + "/" + username);
                    map.put(UserDataDAO.fullName,    fullnameField.getText());
                    map.put(UserDataDAO.email,       email);
                    map.put(UserDataDAO.profile,     profile);
                    map.put(UserDataDAO.poolName,    defaultpool);

                    UserDataDAO user = new UserDataDAO( map );

                    try {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("user", user);

                        ServerRuntimeImpl.registerUser(runtime.getSession(), parameters);

                        warningMessage("Great, you've been successfully registered.\n" +
                                "We've sent an e-mail with registration details to you.");

                        EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                loginTabbedContainer.tabbedPane1.setSelectedIndex(0);
                                loginTabbedContainer.loginPanel.userLogin.setText(username);
                                loginTabbedContainer.loginPanel.userPassword.setText(password1);
                            }
                        });

                    } catch (TunnelingException ex) {
                        if (ex.getCause() instanceof AlreadyBoundException) {
                            warningMessage("User with the specified login already exists.");
                        } else if (ex.getCause() instanceof ConnectionFailedException) {

                            boolean report = BugReport.reportAProblem(RegisterPanel.this,
                                    "Sorry, we could not connect to the registration server.\n");

                            if (report) {
                                BugReport.showReportDialog(RegisterPanel.this, runtime,
                                        "cannot connect to the registration server",
                                        BugReport.stackTraceToString(ex));
                            }

                        } else {

                            boolean report = BugReport.reportAProblem(RegisterPanel.this,
                                    "Sorry, we could not register you.\n");

                            if (report) {
                                BugReport.showReportDialog(RegisterPanel.this, runtime,
                                        "general registration problem",
                                        BugReport.stackTraceToString(ex));
                            }
                        }
                    }

                } catch (ConnectionFailedException re ) {

                    boolean report = BugReport.reportAProblem(RegisterPanel.this,
                            "Sorry, we could not connect to the Registration server\n");

                    if (report) {
                        BugReport.showReportDialog(RegisterPanel.this, runtime,
                                "problem connecting to the registration server",
                                BugReport.stackTraceToString(re));
                    }


                } catch (RemoteException re ) {

                    boolean report = BugReport.reportAProblem(RegisterPanel.this,
                            "Sorry, we could not log you");

                    if (report) {
                        BugReport.showReportDialog(RegisterPanel.this, runtime,
                                "general registration problem",
                                BugReport.stackTraceToString(re));
                    }
                }
            }
        };


        registerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {

                new Thread(addUser).start();

            }
        });

        regListener = new FocusListener(){
            public void focusGained(final FocusEvent event) {
            }
            public void focusLost(FocusEvent event) {

                boolean result = false;

                JTextField source   = (JTextField) event.getSource();
                String text         = source.getText();

                if (!text.equals("")) {
                     result = true;
                }

                if (source == emailField) {
                    if (text.indexOf("@") == -1) {
                        result = false;
                    }
                }

                if (source == passwordField1) {

                    String p1 = new String (passwordField1.getPassword());

                    if (p1.equals("")) {
                        result = false;
                    } else {
                        result = true;
                    }
                }

                if (source == passwordField2) {

                    String p1 = new String (passwordField1.getPassword());
                    String p2 = new String (passwordField2.getPassword());

                    if (!p2.equals("") && p1.equals(p2)) {
                        result = true;
                    } else {
                        result = false;
                    }
                }

                JLabel iconLabel = (JLabel)source.getClientProperty(labelProperty);
                String tipText   = (String)source.getClientProperty(tooltipProperty);

                if (result == true) {
                    iconLabel.setIcon(okayIcon);
                    iconLabel.setToolTipText("");
                    iconLabel.setVisible(true);
                } else {
                    iconLabel.setIcon(errorIcon);
                    iconLabel.setToolTipText(tipText);
                    iconLabel.setVisible(true);
                }
            }
        };

        fullnameField.addFocusListener(regListener);
        emailField.addFocusListener(regListener);
        loginField.addFocusListener(regListener);
        contactField.addFocusListener(regListener);
        passwordField1.addFocusListener(regListener);
        passwordField2.addFocusListener(regListener);
        

    }

    
    private void setupUI(){
        // register panel
        //
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(40,40,20,40));
        setOpaque(false);

        final JPanel panel7 = new JPanel(new BorderLayout(5, 0));
        panel7.setOpaque(false);
        add(panel7, BorderLayout.NORTH);

        //final JPanel regLables = new JPanel(new GridLayout(6, 1));
        //regLables.setOpaque(false);
        //panel7.add(regLables, BorderLayout.WEST);

        final JPanel regFields = new JPanel(new GridLayout(0, 1));
        regFields.setOpaque(false);
        panel7.add(regFields, BorderLayout.CENTER);

        final JPanel regIcons = new JPanel(new GridLayout(0, 1));
        regIcons.setOpaque(false);
        panel7.add(regIcons, BorderLayout.EAST);


        okayIcon = new ImageIcon(ImageLoader.load("/views/images/registerdialog/accept.png"));
        errorIcon = new ImageIcon(ImageLoader.load("/views/images/registerdialog/exclamation.png"));

        fullnameIcon  = new JLabel(okayIcon);
        fullnameIcon.setVisible(false);
        fullnameField = new JHintTextField("Full Name");
        fullnameField.putClientProperty(labelProperty, fullnameIcon);
        fullnameField.putClientProperty(tooltipProperty, ENTERNAME);
        regFields.add(fullnameField);
        regIcons.add(fullnameIcon);

        emailIcon   = new JLabel(okayIcon);
        emailIcon.setVisible(false);
        emailField  = new JHintTextField("Email");
        emailField.putClientProperty(labelProperty, emailIcon);
        emailField.putClientProperty(tooltipProperty, ENTEREMAIL);
        regFields.add(emailField);
        regIcons.add(emailIcon);

        contactIcon   = new JLabel(okayIcon);
        contactIcon.setVisible(false);
        contactField  = new JHintTextField("Profile & Contacts");
        contactField.putClientProperty(labelProperty, contactIcon);
        contactField.putClientProperty(tooltipProperty, ENTERPROFILE);
        regFields.add(contactField);
        regIcons.add(contactIcon);

        loginIcon   = new JLabel(okayIcon);
        loginIcon.setHorizontalAlignment(JLabel.CENTER);
        loginIcon.setVisible(false);
        loginField  = new JHintTextField("Username");
        loginField.putClientProperty(labelProperty, loginIcon);
        loginField.putClientProperty(tooltipProperty, ENTERUSERNAME);
        regFields.add(loginField);
        regIcons.add(loginIcon);

        passwordIcon1    = new JLabel(okayIcon);
        passwordIcon1.setVisible(false);
        passwordField1   = new JHintPasswordField("Choose password");
        passwordField1.putClientProperty(labelProperty, passwordIcon1);
        passwordField1.putClientProperty(tooltipProperty, ENTERPASSWORD);
        regFields.add(passwordField1);
        regIcons.add(passwordIcon1);

        passwordIcon2 = new JLabel(okayIcon);
        passwordIcon2.setVisible(false);
        passwordField2 = new JHintPasswordField("Confirm password");
        passwordField2.putClientProperty(labelProperty, passwordIcon2);
        passwordField2.putClientProperty(tooltipProperty, CHECKPASSWORD);
        regFields.add(passwordField2);
        regIcons.add(passwordIcon2);

        registerButton = ButtonUtil.makeButton("Register");

        //JPanel buttonPanel = new JPanel(new BorderLayout(0, 0));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        //buttonPanel.add(registerButton, BorderLayout.EAST);
        buttonPanel.add(registerButton);

        //regFields.add(buttonPanel);
        //regIcons.add(new JLabel(" "));

        add(buttonPanel, BorderLayout.SOUTH);

    }

    public void resetUI(){
        fullnameField.setText("");
        emailField.setText("");
        loginField.setText("");
        contactField.setText("");
        passwordField1.setText("");
        passwordField2.setText("");

        fullnameIcon.setVisible(false);
        emailIcon.setVisible(false);
        contactIcon.setVisible(false);
        loginIcon.setVisible(false);
        passwordIcon1.setVisible(false);
        passwordIcon2.setVisible(false);
    }

    public void errorMessage(String message) {
        JOptionPaneExt.showMessageDialog(RegisterPanel.this,
                message, "",
                JOptionPaneExt.ERROR_MESSAGE);
    }

    public void warningMessage(final String message) {
        JOptionPaneExt.showMessageDialog(RegisterPanel.this,
                message, "",
                JOptionPaneExt.WARNING_MESSAGE);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            initUI();
            resetUI();
        }
        super.setVisible(visible);
    }

    public void initUI(){
        SwingUtilities.getRootPane(this).setDefaultButton(registerButton);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        JButton run = ButtonUtil.makeButton("run");

        final JDialogExt dialog = new JDialogExt(frame, "dialog");
        //final JDialog dialog = new JDialog(frame, "dialog");

        RegisterPanel registerPanel = new RegisterPanel(null,null,null);


        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        };

        registerPanel.registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        dialog.setLayout(new BorderLayout());
        dialog.add(registerPanel, BorderLayout.CENTER);
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
