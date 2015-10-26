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
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 7, 2009
 * Time: 2:00:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SendBugReportDialog extends JDialogA { //JDialogB

    final private static Logger log = LoggerFactory.getLogger(SendFeedbackDialog.class);

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int dialogResult = CANCEL;

    private JTextField  senderName;
    private JTextField  senderEmail;
    private JTextField  subject;
    private JEditorPane message;
    private JComboBox   categoryCombo;
    private JTextField  othercatField;

    private JButton report  = ButtonUtil.makeButton("Report");
    private JButton cancel  = ButtonUtil.makeButton("Cancel");

    private String categoryItems [] = {
            "Bench Component",
            "Bench Menus",
            "Command Console",
            "Command Completion",
            "Connectivity",
            "Bench Editor",
            "File Browser",
            "Login Dialog",
            "Object Viewer",
            "Project Dialog",
            "Robustness",
            "Other..",
    };

    private static SendBugReportDialog dialog = null;
    private static final Integer singletonLock = new Integer(0);

    public static SendBugReportDialog getInstance(Component c) {

        if (dialog != null) {
            dialog.resetUI();
            return dialog;
        }
        synchronized (singletonLock) {
            if (dialog == null) {
                dialog = new SendBugReportDialog(c);
                dialog.resetUI();
            }
            return dialog;
        }
    }

    private void resetUI(){
        dialogResult = CANCEL;
    }

    private void addItem(JPanel p, JComponent c, int x, int y, int width, int height, int align) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.gridheight = height;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.anchor = align;
        gc.fill = GridBagConstraints.NONE;
        p.add(c, gc);
    }

    public SendBugReportDialog(Component c) {
        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

        JPanel gridbagPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));

        gridbagPanel.setOpaque(false);
        buttonPanel.setOpaque(false);

        container.add(gridbagPanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        senderName  = new JTextField();
        senderEmail  = new JTextField();
        subject = new JTextField();
        message = new JEditorPane();
        categoryCombo   = new JComboBox(categoryItems);
        othercatField   = new JTextField();

        JScrollPane messageScroll = new JScrollPane(message);
        messageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        senderName.setPreferredSize(new Dimension(410, 28));
        senderEmail.setPreferredSize(new Dimension(410, 28));
        subject.setPreferredSize(new Dimension(410, 28));
        //description.setPreferredSize(new Dimension(400, 180));
        messageScroll.setPreferredSize(new Dimension(410, 200));
        categoryCombo.setPreferredSize(new Dimension(410, 28));
        othercatField.setPreferredSize(new Dimension(410, 28));

        final JLabel othercatLabel = new JLabel("Other Area ");

        addItem(gridbagPanel, new JLabel("Your Name "),  0, 0, 1, 1, GridBagConstraints.EAST);
        addItem(gridbagPanel, new JLabel("Your Email "), 0, 1, 1, 1, GridBagConstraints.EAST);
        addItem(gridbagPanel, new JLabel("Subject "),    0, 2, 1, 1, GridBagConstraints.EAST);
        addItem(gridbagPanel, new JLabel("Message "),    0, 3, 1, 1, GridBagConstraints.EAST);
        addItem(gridbagPanel, new JLabel("Area "),       0, 4, 1, 1, GridBagConstraints.EAST);
        addItem(gridbagPanel, othercatLabel,             0, 5, 1, 1, GridBagConstraints.EAST);

        addItem(gridbagPanel, senderName,    1, 0, 1, 1, GridBagConstraints.WEST);
        addItem(gridbagPanel, senderEmail,   1, 1, 1, 1, GridBagConstraints.WEST);
        addItem(gridbagPanel, subject,       1, 2, 1, 1, GridBagConstraints.WEST);
        addItem(gridbagPanel, messageScroll, 1, 3, 1, 1, GridBagConstraints.WEST);
        addItem(gridbagPanel, categoryCombo, 1, 4, 1, 1, GridBagConstraints.WEST);
        addItem(gridbagPanel, othercatField, 1, 5, 1, 1, GridBagConstraints.WEST);

        othercatField.setEnabled(false);
        othercatLabel.setEnabled(false);

        buttonPanel.add(cancel);
        buttonPanel.add(report);

        KeyListener keyListener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    categoryCombo.requestFocus();
                    e.consume();
                }
            }
            public void keyReleased(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}
        };

        message.addKeyListener(keyListener);

        ActionListener buttonListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (event.getSource() == report) {
                    okMethod();
                }
                if (event.getSource() == cancel) {
                    cancelMethod();
                }
            }
        };

        ActionListener categoryListener = new ActionListener(){
            public void actionPerformed(ActionEvent event){
                JComboBox combo = (JComboBox) event.getSource();

                boolean lastItemSelected =
                        categoryItems[categoryItems.length-1].equals(combo.getSelectedItem());

                if (othercatField.isEnabled() != lastItemSelected) {
                    othercatField.setEnabled(lastItemSelected);
                    othercatLabel.setEnabled(lastItemSelected);
                }

                if (lastItemSelected) {
                    othercatField.requestFocus();
                }
            }
        };

        categoryCombo.addActionListener(categoryListener);

        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);
        setTitle("Report A Bug");

        report.addActionListener(buttonListener);
        cancel.addActionListener(buttonListener);

        this.getRootPane().setDefaultButton(report);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        this.getRootPane().registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setPreferredSize(new Dimension(640, 500));
        pack();
        setLocationRelativeTo(c);
        setResizable(false);
    }

    public void okMethod() {
        dialogResult = OK;
        setVisible(false);
    }

    public void cancelMethod() {
        dialogResult = CANCEL;
        setVisible(false);
    }

    public String getBugTitle() {
        return subject.getText();
    }

    public void setBugTitle(String title) {
        subject.setText(title);
    }

    public String getBugSenderName() {
        return senderName.getText();
    }

    public void setBugSenderName(String info) {
        senderName.setText(info);
    }

    public String getBugSenderEmail() {
        return senderEmail.getText();
    }

    public void setBugSenderEmail(String info) {
        senderEmail.setText(info);
    }

    public String getBugMessage() {
        return message.getText();
    }

    public void setBugMessage(String descr) {
        message.setText(descr);
    }

    public String getBugCategory() {
        if (othercatField.isEnabled()) {
            return othercatField.getText();
        }
        return (String) categoryCombo.getSelectedItem();
    }

    public int getResult() {
        return dialogResult;
    }

    public static void main(String[] args) {

        SendBugReportDialog dialog = new SendBugReportDialog(new JFrame());
        dialog.setModal(true);
        //dialog.setUndecorated(false);
        //dialog.setResizable(true);
        dialog.setVisible(true);
        Dimension dim = dialog.getSize();

        log.info("dim.getHeight()="+dim.getHeight()+" dim.getWidth()="+dim.getWidth());

        dialog.setVisible(true);
        System.exit(0);
    }

}
