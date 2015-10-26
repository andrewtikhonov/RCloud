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
import uk.ac.ebi.rcloud.common.components.textfield.JHintTextField;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.http.exception.ConnectionFailedException;
import uk.ac.ebi.rcloud.http.exception.TunnelingException;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

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
 * Time: 5:37:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RestorePanel extends JPanel {
    final private Logger log = LoggerFactory.getLogger(getClass());

    // restore
    private JButton restoreButton;
    private JHintTextField restoreEmail;

    private Runnable        restoreUser;
    private LoginInternalActions actions;
    private RuntimeEnvironment runtime;

    public RestorePanel(RuntimeEnvironment runtime0, LoginInternalActions internalActions){

        this.runtime = runtime0;
        this.actions   = internalActions;

        setupUI();

        restoreUser = new Runnable(){
            public void run(){

                final String email  = restoreEmail.getText().trim();

                // validate fields
                //
                if (email.equals("")) {
                    warningMessage("Please enter your email address..");

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            restoreEmail.requestFocus();
                        }
                    });
                    
                    return;
                }

                try {
                    HashMap<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("email", email);

                    ServerRuntimeImpl.restoreUser(runtime.getSession(), parameters);

                    warningMessage("We've sent your registration details to you");

                    restoreEmail.setText("");



                } catch (ConnectionFailedException cfe) {

                    boolean report = BugReport.reportAProblem(RestorePanel.this,
                            "Sorry, we could not connect to the Registration Server.\n");

                    if (report) {
                        BugReport.showReportDialog(RestorePanel.this, runtime,
                                "cannot connect to the registration server",
                                BugReport.stackTraceToString(cfe));
                    }

                } catch (TunnelingException ex) {
                    boolean report = BugReport.reportAProblem(RestorePanel.this,
                            "Sorry, we could not restore your details.\n");

                    if (report) {
                        BugReport.showReportDialog(RestorePanel.this, runtime,
                                "problem during password restoration",
                                BugReport.stackTraceToString(ex));
                    }

                }
            }
        };


        restoreButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {

                new Thread(restoreUser).start();

            }
        });
    }


    private void setupUI(){

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(40,40,20,40));
        setOpaque(false);

        final JPanel container = new JPanel(new GridLayout(0, 1));
        container.setOpaque(false);
        add(container, BorderLayout.NORTH);

        final JLabel messageText0 = new JLabel("We will send you an email that");
        final JLabel messageText1 = new JLabel("will contain your account details.");

        Font font0 = messageText0.getFont();
        Font font1 =new Font(font0.getName(), font0.getStyle(), 14);

        messageText0.setFont(font1);
        messageText0.setHorizontalAlignment(0);

        messageText1.setFont(font1);
        messageText1.setHorizontalAlignment(0);

        restoreEmail = new JHintTextField("enter your e-mail address");
        restoreButton = ButtonUtil.makeButton("Restore");

        final JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(restoreButton);

        container.add(messageText0);
        container.add(messageText1);
        container.add(restoreEmail);
        container.add(new JLabel(" "));

        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            initUI();
        }
        super.setVisible(visible);
    }

    public void initUI(){
        SwingUtilities.getRootPane(this).setDefaultButton(restoreButton);
    }

    public void resetUI(){
    }

    public void errorMessage(String message) {
        JOptionPaneExt.showMessageDialog(RestorePanel.this,
                message, "",
                JOptionPaneExt.ERROR_MESSAGE);
    }

    public void warningMessage(final String message) {
        JOptionPaneExt.showMessageDialog(RestorePanel.this,
                message, "",
                JOptionPaneExt.WARNING_MESSAGE);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();

        JButton run = ButtonUtil.makeButton("run");

        final JDialogExt dialog = new JDialogExt(frame, "dialog");
        //final JDialog dialog = new JDialog(frame, "dialog");

        RestorePanel restorePanel = new RestorePanel(null,null);


        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        };

        restorePanel.registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        dialog.setLayout(new BorderLayout());
        dialog.add(restorePanel, BorderLayout.CENTER);
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
