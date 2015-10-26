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
package uk.ac.ebi.rcloud.common.components;

import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogB;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogExt;
import uk.ac.ebi.rcloud.common.util.LAFUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 24, 2009
 * Time: 9:44:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class JOptionPaneExt extends JOptionPane {

    public JOptionPaneExt() {
        super("JOptionPaneExt message", PLAIN_MESSAGE, DEFAULT_OPTION, null, null, null);
    }

    public JOptionPaneExt(Object message) {
        super(message, PLAIN_MESSAGE, DEFAULT_OPTION, null, null, null);
    }

    public JOptionPaneExt(Object message, int messageType) {
        super(message, messageType, DEFAULT_OPTION, null, null, null);
    }

    public JOptionPaneExt(Object message, int messageType, int optionType) {
        super(message, messageType, optionType, null, null, null);
    }

    public JOptionPaneExt(Object message, int messageType, int optionType, Icon icon) {
        super(message, messageType, optionType, icon, null, null);
    }

    public JOptionPaneExt(Object message, int messageType, int optionType,
                      Icon icon, Object[] options) {
        super(message, messageType, optionType, icon, options, null);
    }

    public JOptionPaneExt(Object message, int messageType, int optionType,
                      Icon icon, Object[] options, Object initialValue) {
        super(message, messageType, optionType, icon, options, initialValue);
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title,
                                        int messageType, int optionType, Icon icon, Object[] options,
                                        Object initialValue) {
        JOptionPane pane = new JOptionPaneExt(message, messageType, optionType, icon, options, initialValue);
        JDialog dialog = pane.createDialog(parentComponent, title);
        dialog.setVisible(true);

        if (pane.getValue() instanceof Integer)
            return ((Integer) pane.getValue()).intValue();

        return -1;
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title,
                                        int messageType, int optionType) {
        return showConfirmDialog(parentComponent, message, title, messageType, optionType, null, null, null);
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title,
                                        int messageType) {
        return showConfirmDialog(parentComponent, message, title, messageType, DEFAULT_OPTION, null, null, null);
    }

    public static void showMessageDialog(Component parentComponent,
                                         Object message, String title,
                                         int messageType, Icon icon)
    {
        JOptionPaneExt pane = new JOptionPaneExt(message, messageType, DEFAULT_OPTION, icon);
        JDialog dialog = pane.createDialog(parentComponent, title);
        //pane.selectInitialValue();
        dialog.setVisible(true);

    }

    public static void showMessageDialog(Component parentComponent, Object message, String title,
                                        int messageType) {
        showMessageDialog(parentComponent, message, title, messageType, null);
    }

    public static void showMessageDialog(Component parentComponent, Object message, String title) {
        showMessageDialog(parentComponent, message, title, INFORMATION_MESSAGE, null);
    }

    public static void showMessageDialog(Component parentComponent, Object message) {
        showMessageDialog(parentComponent, message, "", WARNING_MESSAGE, null);
    }

    public static void showExceptionDialog(Component parentComponent, Exception ex) {
        JTextPane text = new JTextPane();
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(800, 300));
        text.setText(getStackTraceAsString(ex) +
                "\ncalled from \n" +
                getStackTraceAsString(new Exception()));

        showMessageDialog(parentComponent, scroll, "Exception", WARNING_MESSAGE, null);
    }

    public static String getStackTraceAsString2(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }

    public static String getStackTraceAsString(Exception exception, int cnt) {
        StringBuilder        sb   = new StringBuilder();
        StackTraceElement[] trace = exception.getStackTrace();
        sb.append(exception + "\n");
        int bound = cnt < trace.length ? cnt : trace.length;
        for (int i = 0; i < bound; i++) {
            sb.append("\t " + trace[i] + "\n");
        }
        return sb.toString();
    }

    public static String getStackTraceAsString(Exception ex) {
        return getStackTraceAsString(ex, 100);
    }

    private static class ValuePropertyHandler implements PropertyChangeListener
    {
        /**
         * The dialog to close.
         */
        JDialog dialog;

        /**
         * Creates a new instance.
         *
         * @param d the dialog to be closed
         */
        ValuePropertyHandler(JDialog d)
        {
            dialog = d;
        }

        /**
         * Receives notification when any of the properties change.
         */
        public void propertyChange(PropertyChangeEvent p)
        {
            String prop = p.getPropertyName();
            Object val = p.getNewValue();
            if (prop.equals(VALUE_PROPERTY) && val != null && val != UNINITIALIZED_VALUE)
            {
                dialog.setVisible(false);
            }
        }
    }

    @Override
    public JDialog createDialog(Component parentComponent, String title) throws java.awt.HeadlessException {
        Frame toUse = getFrameForComponent(parentComponent);
        if (toUse == null)
            toUse = getRootFrame();

        JDialogExt dialog = new JDialogExt(toUse, title);
        //JDialogA dialog = new JDialogA(toUse, title);
        //JDialogB dialog = new JDialogB(toUse, title);
        inputValue = UNINITIALIZED_VALUE;
        value = UNINITIALIZED_VALUE;

        //dialog.setBackground(new Color(255,255,255,150));
        //JPanelExt container = new JPanelExt();
        //JPanel container = new JPanel();
        //container.setOpaque(false);
        //dialog.setContentPane(container);
        dialog.getContentPane().add(this);
        dialog.setModal(true);

        //dialog.setUndecorated(true);
        dialog.setResizable(true);
        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);
        addPropertyChangeListener(new ValuePropertyHandler(dialog));

        return dialog;
    }

    public static int showOptionDialog(Component component, Object message,
                                       String title, int optionType, int messageType,
                                       Icon icon, Object[] options, Object initialValue)
                                       throws HeadlessException {

        //if (message instanceof String) {
        //    message = "<html><font = \"verdana\">" + message;
        //}

        JOptionPaneExt pane = new JOptionPaneExt(message, messageType,
                optionType, icon, options, initialValue);

        pane.setInitialValue(initialValue);
        pane.setComponentOrientation(((component == null) ?
        getRootFrame() : component).getComponentOrientation());

        //int style = styleFromMessageType(messageType);
        JDialog dialog = pane.createDialog(component, title); //, style

        pane.selectInitialValue();

        dialog.setVisible(true);
        //dialog.show();
        dialog.dispose();

        Object selectedValue = pane.getValue();

        if(selectedValue == null)
            return CLOSED_OPTION;
        if(options == null) {
            if(selectedValue instanceof Integer)
                return (Integer) selectedValue;
            return CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return CLOSED_OPTION;
    }

    public static Object showInputDialog(Component parentComponent,
        Object message, String title, int messageType, Icon icon,
        Object[] selectionValues, Object initialSelectionValue)
        throws HeadlessException {
        JOptionPane    pane = new JOptionPaneExt(message, messageType,
                                              OK_CANCEL_OPTION, icon,
                                              null, null);

        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues);
        pane.setInitialSelectionValue(initialSelectionValue);
        pane.setComponentOrientation(((parentComponent == null) ?
	    getRootFrame() : parentComponent).getComponentOrientation());

        //int style = styleFromMessageType(messageType);
        //JDialog dialog = pane.createDialog(parentComponent, title, style);
        //int style = styleFromMessageType(messageType);
        JDialog dialog = pane.createDialog(parentComponent, title); //, style

        pane.selectInitialValue();

        dialog.setVisible(true);
        //dialog.show();
        dialog.dispose();

        Object value = pane.getInputValue();

        if (value == UNINITIALIZED_VALUE) {
            return null;
        }
        return value;
    }


    public static void main(String[] args) {

        LAFUtil.setupLookAndFeel();
        
        final JFrame frame1 = new JFrame();

        frame1.setLayout(new BorderLayout());
        //frame1.getRootPane().setWindowDecorationStyle(JRootPane.ERROR_DIALOG);

        JPanel panel = new JPanel(new FlowLayout());

        JButton button = new JButton("ok");

        panel.add(button);

        frame1.add(panel);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JOptionPaneExt pane = new JOptionPaneExt("a test message box", INFORMATION_MESSAGE);
                JDialog dialog = pane.createDialog(frame1, null);
                //dialog.setBackground(new Color(0,0,0,100));
                dialog.setVisible(true);

                JOptionPaneExt.showMessageDialog(frame1, "this is test message");

                Object[] objects = { "do", "stop" };
                Object   init    = "do";

                JOptionPaneExt.showOptionDialog(frame1, "this is test message", "some title",
                        DEFAULT_OPTION, WARNING_MESSAGE, null, objects, init);
            }
        });


        frame1.setPreferredSize(new Dimension(500, 400));
        frame1.pack();
        frame1.setLocationRelativeTo(null);
        frame1.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
        frame1.setVisible(true);
    }

}
