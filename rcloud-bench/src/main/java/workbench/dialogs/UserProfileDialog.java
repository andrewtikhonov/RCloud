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
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.textfield.JHintPasswordField;
import uk.ac.ebi.rcloud.common.components.textfield.JHintTextField;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.http.exception.BadLoginPasswordException;
import uk.ac.ebi.rcloud.http.exception.ConnectionFailedException;
import uk.ac.ebi.rcloud.http.proxy.DAOHttpProxy;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import workbench.report.BugReport;
import workbench.runtime.RuntimeEnvironment;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 25/07/2011
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */

public class UserProfileDialog extends JDialogA {

    private static Logger log = LoggerFactory.getLogger(UserProfileDialog.class);


    // register
    private JHintTextField fullnameField;
    private JHintTextField emailField;
    private JHintTextField contactField;
    private JHintPasswordField passwordField;

    private JLabel fullnameIcon;
    private JLabel emailIcon;
    private JLabel passwordIcon;
    private JLabel contactIcon;

    private JButton updateButton;

    private String labelProperty   = "LABEL";
    private String tooltipProperty = "TOOLTIP";

    private ImageIcon okayIcon;
    private ImageIcon errorIcon;

    private Runnable    updateUser;
    private FocusListener regListener;

    private String ENTERNAME = "Full name (e.g. Bill Clinton)";
    private String ENTEREMAIL = "Email address is used for notifications";
    private String ENTERPASSWORD = "Please enter your current password";
    private String WRONGPASSWORD = "Please carefully enter your current password";
    private String ENTERPROFILE = "Profile and contacts, it will help us contact you should you require any assistance.";

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int dialogresult = CANCEL;

    private static UserProfileDialog _dialog = null;
    private static final Integer singletonLock = new Integer(0);
    private static RuntimeEnvironment runtime = null;

    public static UserProfileDialog getInstance(Component c, RuntimeEnvironment runtime) {

        if (_dialog != null) {
            _dialog.resetUI();
            return _dialog;
        }
        synchronized (singletonLock) {
            if (_dialog == null) {
                _dialog = new UserProfileDialog(c, runtime);
                _dialog.resetUI();
            }
            return _dialog;
        }
    }


    private UserProfileDialog(Component c, RuntimeEnvironment runtime0) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        //super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        this.runtime = runtime0;

        setupUI();

        updateUser = new Runnable(){
            public void run(){

                String password   = new String( passwordField.getPassword() );
                String email      = emailField.getText().trim();
                String fullname   = fullnameField.getText().trim();
                String profile    = contactField.getText().trim();

                // validate fields
                //
                if ("".equals(fullname)) {
                    warningMessage(ENTERNAME);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            fullnameField.requestFocus();
                        }
                    });

                    return;
                }

                if ("".equals(email)) {
                    warningMessage(ENTEREMAIL);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            emailField.requestFocus();
                        }
                    });

                    return;
                }

                if ("".equals(password)) {
                    warningMessage(ENTERPASSWORD);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            passwordField.requestFocus();
                        }
                    });
                    return;
                }

                // authenticate the change
                //
                UserDataDAO user = null;
                HashMap<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("username", runtime.getUser().getUsername());
                parameters.put("password", password);

                try {
                    boolean user_authenticated = ServerRuntimeImpl.
                            authenticateUser(runtime.getSession(), parameters);

                    if (user_authenticated) {
                        user = runtime.getDAOLayer().getUserByUsername(runtime.getUser().getUsername());
                    }


                } catch (BadLoginPasswordException ex) {
                    warningMessage(WRONGPASSWORD);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            passwordField.requestFocus();
                        }
                    });

                    return;
                } catch (ConnectionFailedException ex) {
                    boolean report = BugReport.reportAProblem(UserProfileDialog.this,
                            "Sorry, we could not connect to the Registration Server.\n");

                    if (report) {
                        BugReport.showReportDialog(UserProfileDialog.this, runtime,
                                "problem connecting to the registration server",
                                BugReport.stackTraceToString(ex));
                    }

                    return;
                } catch (Exception ex) {
                    boolean report = BugReport.reportAProblem(UserProfileDialog.this,
                            "Sorry, we could not update your profile. \n" +
                            "The registration server responded unexpectedly.\n");

                    if (report) {
                        BugReport.showReportDialog(UserProfileDialog.this, runtime,
                                "Unexpected response from the registration server ",
                                BugReport.stackTraceToString(ex));
                    }

                    return;
                }

                if (user == null) {
                    warningMessage(WRONGPASSWORD);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            passwordField.requestFocus();
                        }
                    });

                    return;
                }


                // check if there are users in DB
                // that have same login or email.
                //
                if (runtime.getDAOLayer() == null) {
                    warningMessage("Sorry, we could not update your profile.\n" +
                                   "Database component is not available.\n"+
                                   "Please contact us for help.");
                    return;
                }

                runtime.getUser().getMap().put(UserDataDAO.fullName, fullnameField.getText());
                runtime.getUser().getMap().put(UserDataDAO.email,    email);
                runtime.getUser().getMap().put(UserDataDAO.profile,  profile);

                try {
                    runtime.getDAOLayer().updateUser(runtime.getUser());

                    warningMessage("Profile has been updated.");

                    setVisible(false);

                    return;
                } catch (Exception ex) {

                    boolean report = BugReport.reportAProblem(getOwner(),
                            "Sorry, we could not update your profile\n"+
                            "Would you like to report it to the R Cloud support ?");

                    if (report) {
                        BugReport.showReportDialog(getOwner(), runtime,
                                "problem updating user profile",
                                BugReport.stackTraceToString(ex));
                    }

                    return;
                }
            }
        };


        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                new Thread(updateUser).start();

            }
        });

        regListener = new FocusListener(){

            public void focusGained(final FocusEvent event) {
            }
            public void focusLost(FocusEvent event) {

                boolean result = false;

                JTextField source   = (JTextField) event.getSource();
                String text         = source.getText();

                result = !text.equals("");

                if (source == emailField) {
                    result = text.contains("@");
                }

                if (source == passwordField) {
                    String p1 = new String (passwordField.getPassword());
                    result = !p1.equals("");
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
        contactField.addFocusListener(regListener);
        passwordField.addFocusListener(regListener);

        setPreferredSize(new Dimension(500, 300));

        setLocationRelativeTo(c);
        setResizable(true);

    }

    private void setupUI(){
        setLayout(new BorderLayout());

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(10,10,10,10));

        final JPanel panel7 = new JPanel(new BorderLayout(5, 0));
        panel7.setOpaque(false);
        container.add(panel7, BorderLayout.NORTH);

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

        passwordIcon    = new JLabel(okayIcon);
        passwordIcon.setVisible(false);
        passwordField   = new JHintPasswordField("Password");
        passwordField.putClientProperty(labelProperty, passwordIcon);
        passwordField.putClientProperty(tooltipProperty, ENTERPASSWORD);
        regFields.add(passwordField);
        regIcons.add(passwordIcon);

        updateButton = ButtonUtil.makeButton("Update");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(updateButton);

        container.add(buttonPanel, BorderLayout.SOUTH);

        add(container, BorderLayout.CENTER);

        setTitle("User Profile");

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

    public void resetUI(){
        dialogresult = CANCEL;

        fullnameField.setText("");
        emailField.setText("");
        contactField.setText("");
        passwordField.setText("");

        fullnameIcon.setVisible(false);
        emailIcon.setVisible(false);
        contactIcon.setVisible(false);
        passwordIcon.setVisible(false);

        if (runtime.getUser() != null) {
            setUser(runtime.getUser());
        }

    }

    public void errorMessage(String message) {
        JOptionPaneExt.showMessageDialog(UserProfileDialog.this,
                message, "",
                JOptionPaneExt.ERROR_MESSAGE);
    }

    public void warningMessage(final String message) {
        JOptionPaneExt.showMessageDialog(UserProfileDialog.this,
                message, "",
                JOptionPaneExt.WARNING_MESSAGE);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            initUI();
        }
        super.setVisible(visible);
    }

    public void initUI(){
        SwingUtilities.getRootPane(this).setDefaultButton(updateButton);
    }

    // user
    //
    public void setUserInternal(final UserDataDAO user) {
        fullnameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        contactField.setText(user.getProfile());
    }


    public void setUser(final UserDataDAO user) {

        if (isVisible()) {
            //log.info("setPattern-visible pattern="+pattern);

            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    setUserInternal(user);
                }
            });
        } else {
            setUserInternal(user);
        }
    }

    public static void main(String[] args) {

        try {
            HashMap<String, Object> options = new HashMap<String, Object>();
            options.put(UserDataDAO.username, "login");
            options.put(UserDataDAO.email,    "tom@mail.com");
            options.put(UserDataDAO.fullName, "Tom Marvolo Riddle");
            options.put(UserDataDAO.profile,  "");
            options.put(UserDataDAO.password, "1234");
            options.put(UserDataDAO.userFolder, System.getProperty("userfolder"));

            UserDataDAO user = new UserDataDAO(options);

            RuntimeEnvironment runtime = new RuntimeEnvironment();

            runtime.setBaseUrl(System.getProperty("baseurl"));

            runtime.setDAOLayer(DAOHttpProxy.getDAOLayer(null, runtime.getSession()));

            runtime.setUser(user);

            UserProfileDialog dialog = UserProfileDialog.getInstance(new JFrame(), runtime);

            dialog.setModal(true);
            dialog.setVisible(true);
            System.exit(0);

        } catch (Exception ex) {
            log.error("Error! ", ex);
        }
    }

}
