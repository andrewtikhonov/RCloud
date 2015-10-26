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
package workbench.dialogs.propertyviewer;

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 11, 2010
 * Time: 1:38:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyBrowser extends JDialogA {

    public  static int OK       = 0;
    public  static int CANCEL   = 1;

    public  int result = CANCEL;

    private JComboBox combo;
    private JTable propertytable;
    private PropertyBrowserModel propertymodel;
    private JButton addnew = new JButton();

    private Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private static Integer singletonLock = new Integer(0);
    private static PropertyBrowser browser = null;

    private RGui rgui;

    public static PropertyBrowser getInstance(Component c, RGui rgui) {
        if (browser == null) {
            synchronized (singletonLock) {
                browser = new PropertyBrowser(c, rgui);
                return browser;
            }
        } else {
            return browser;
        }
    }

    public PropertyBrowser(Component c, RGui rgui) {

        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        this.rgui = rgui;

        JPanel container  = new JPanel(new BorderLayout(0,0));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));

        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        JPanel optionsPanel = new JPanel(new BorderLayout());

        tablePanel.setOpaque(false);
        buttonPanel.setOpaque(false);
        optionsPanel.setOpaque(false);

        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        container.add(optionsPanel, BorderLayout.NORTH);
        container.add(tablePanel, BorderLayout.CENTER);


        ActionListener listener = new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                refresh();
            }
        };

        combo = new JComboBox(PropertyBrowserConfig.proprtyOptions);
        combo.addActionListener(listener);

        optionsPanel.add(combo);

        addnew = makeButton("/views/images/propertybrowser/list-add.png", "Add property");

        ActionListener addlistener = new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                String propertyName = (String) JOptionPaneExt.showInputDialog(PropertyBrowser.this,
                                    "Define new property\n",
                                    "Add Property", JOptionPane.PLAIN_MESSAGE,
                                    null, null, "proprty.name = value");

                if (propertyName != null) {
                    String delimiter = "=";

                    if (propertyName.contains(delimiter)) {
                        String[] str = propertyName.split(delimiter);
                        propertymodel.add(new PropertyListItem(str[0].trim(), str[1].trim()));
                    }
                }

            }
        };

        addnew.addActionListener(addlistener);

        buttonPanel.add(addnew);

        propertytable = new JTable();
        propertytable.getTableHeader().addMouseListener(
                new PropertyBrowserHeaderAdapter(propertytable));

        propertymodel = new PropertyBrowserModel(rgui);
        propertytable.setModel(propertymodel);
        propertytable.getColumnModel().getColumn(0).setPreferredWidth(100);
        propertytable.getColumnModel().getColumn(1).setPreferredWidth(200);
        propertytable.setRowHeight(20);
        propertytable.getTableHeader().setPreferredSize(new Dimension(2, 16));

        tablePanel.add(new JScrollPane(propertytable), BorderLayout.CENTER);

        setLayout(new BorderLayout());

        add(container, BorderLayout.CENTER);
        setTitle("Property Browser (debugging)");

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

        setPreferredSize(new Dimension(550, 720));
        //pack();
        setLocationRelativeTo(c);
        setResizable(false);

        refresh();
    }

    public void refresh() {
        propertymodel.refresh((String)combo.getSelectedItem());
    }

    private JButton makeButton(String imagePath, String toolTip) {
        JButton b = new JButton();

        b.setIcon(new ImageIcon(ImageLoader.load(imagePath)));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusable(false);
        b.setPreferredSize(new Dimension(24, 24));
        b.setToolTipText(toolTip);
        b.setCursor(hand);

        return(b);
    }

    public static void main(String[] args) {
        PropertyBrowser browser = 
                PropertyBrowser.getInstance(new JFrame(), null);

        browser.setModal(true);
        browser.setVisible(true);

        System.exit(0);
    }

}
