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
package workbench.dialogs.packagemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.textfield.JSearchField;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.server.RType.RChar;
import workbench.RGui;
import workbench.manager.runner.RActionAsyncHelper;
import workbench.util.ButtonUtil;
import workbench.util.RUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 12, 2009
 * Time: 4:30:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageManagerDialog extends JDialogA {// JDialogExt {

    final private static Logger log = LoggerFactory.getLogger(PackageManagerDialog.class);
    final private static String component = "Package Manager";

    private RGui rgui;

    private JSearchField searchField = new JSearchField();
    private JButton refresh = ButtonUtil.makeButton("Refresh");

    //private ETable packageTable  = new ETable();
    private JTable packageTable  = new JTable();
    private JScrollPane tableScroll  = new JScrollPane(packageTable);


    private ManagerTableModel managerTableModel = new ManagerTableModel();

    private Integer updateListLock    = new Integer(0);

    private static Integer singletonLock = new Integer(0);
    private static PackageManagerDialog dialog = null;

    private PackageManagerConfig config = PackageManagerConfig.getInstance();

    public static PackageManagerDialog getInstance(Component c, RGui rgui) {
        if (dialog == null) {
            synchronized (singletonLock) {
                dialog = new PackageManagerDialog(c, rgui);
                dialog.loadAvailablePackagesVisual();
                return dialog;
            }
        } else {
            return dialog;
        }
    }

    private PackageManagerDialog(Component c, RGui rg) {

        //super((Frame) c, false);
        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), false);

        this.rgui = rg;

        //System.setProperty("packagetest", "true");

        setTitle("Package Manager");

        setLayout(new BorderLayout());
        //setBackground(new Color(0,0,0,80));

        JPanel topContainer  = new JPanel(new BorderLayout(0,0));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(10,10,10,10));
        //topContainer.setBorder(BorderFactory.createTitledBorder("repositories"));

        JPanel centerContainer  = new JPanel(new BorderLayout(0,0));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(10,10,10,10));

        JPanel bottomContainer  = new JPanel(new BorderLayout(0,0));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(10,10,10,10));

        add(topContainer, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        // top
        topContainer.setLayout(new BorderLayout(0,0));
        JPanel topLeft  = new JPanel(new BorderLayout());
        JPanel topRight = new JPanel(new BorderLayout());
        topLeft.setOpaque(false);
        topRight.setOpaque(false);

        topLeft.add(refresh, BorderLayout.CENTER);

        topRight.add(searchField, BorderLayout.CENTER);

        topContainer.add(topLeft, BorderLayout.WEST);
        topContainer.add(topRight, BorderLayout.CENTER);

        // center
        centerContainer.setLayout(new BorderLayout(0,0));
        centerContainer.add(tableScroll, BorderLayout.CENTER);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        ManagerTableRenderer renderer = new ManagerTableRenderer();

        packageTable.setGridColor(Color.LIGHT_GRAY);
        packageTable.setModel(managerTableModel);
        packageTable.setDefaultRenderer(String.class, renderer);
        packageTable.setRowHeight(20);
        packageTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        packageTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        packageTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        packageTable.getColumnModel().getColumn(2).setPreferredWidth(380);

        packageTable.getTableHeader().setPreferredSize(new Dimension(2, 16));

        // bottom
        bottomContainer.setLayout(new BorderLayout(0,0));

        ManagerTableRowFilter rowFilter = new ManagerTableRowFilter(){
            public String getMask(){
                return searchField.getText();
            }
        };

        managerTableModel.addRowFilter(rowFilter);

        DocumentListener searchListener = new DocumentListener(){
            public void insertUpdate(DocumentEvent event) {
                synchronized (updateListLock) {
                    managerTableModel.fireTableDataChanged();
                }
            }
            public void removeUpdate(DocumentEvent event) {
                synchronized (updateListLock) {
                    managerTableModel.fireTableDataChanged();
                }
            }
            public void changedUpdate(DocumentEvent event) {}
        };

        searchField.getDocument().addDocumentListener(searchListener);

        ActionListener refreshListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadAvailablePackagesVisual();
            }
        };

        refresh.addActionListener(refreshListener);

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

        Dimension windowSize = new Dimension(580, 620);
        //setSize(windowSize);
        setPreferredSize(windowSize);
        //pack();
        setLocationRelativeTo(c);
        setResizable(true);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    private Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    
    private void lockPackageManager() {
        refresh.setEnabled(false);
        searchField.setEnabled(false);

        this.setCursor(waitCursor);
    }

    private void unlockPackageManager() {
        refresh.setEnabled(true);
        searchField.setEnabled(true);

        this.setCursor(defaultCursor);
    }

    private ManagerTableItemNotifier packageHandler = new ManagerTableItemNotifier() {
        public void itemUpdated(ManagerTableItem before, ManagerTableItem after) {
            if (!before.isLoaded() && after.isLoaded()) {
                log.info("loading " + after.getName());
                loadPackage(after.getName());
            }
            else if (before.isLoaded() && !after.isLoaded()) {
                log.info("unloading " + after.getName());
                unloadPackage(after.getName());
            }
        }
    };

    private void loadAvailablePackagesVisual() {
        new Thread(new Runnable(){
            public void run(){
                try {
                    lockPackageManager();

                    loadAvailablePackages();

                } finally {
                    unlockPackageManager();
                }

            }
        }).start();
    }

    private void loadAvailablePackages() {

        Vector<ManagerTableItem> pList = new Vector<ManagerTableItem>();

        String textprop = System.getProperty("package.manager.test");

        if (textprop != null && textprop.equals("true")) {

            ManagerTableItemNotifier testHandler = new ManagerTableItemNotifier() {
                public void itemUpdated(ManagerTableItem before, ManagerTableItem after) {
                    if (!before.isLoaded() && after.isLoaded()) {
                        log.info("loading " + after.getName());
                    }
                    else if (before.isLoaded() && !after.isLoaded()) {
                        log.info("unloading " + after.getName());
                    }
                }
            };

            for (int i=1;i<400;i++) {
                pList.add(new ManagerTableItem(false, "AAA1i"+i, "AAA1 package "+i,testHandler));
                pList.add(new ManagerTableItem(false, "AAA2xi"+i, "Description for AAA2x package"+i,testHandler));
                pList.add(new ManagerTableItem(false, "AAA3xi"+i, "AAA1 package"+i,testHandler));
                pList.add(new ManagerTableItem(false, "AxxCB1i"+i, "Some description for the package"+i,testHandler));
                pList.add(new ManagerTableItem(false, "CAB2Ai"+i, "This is very long description for CAB2A package that will "+
                        "be long enough not to fit into a table column.",testHandler));
                pList.add(new ManagerTableItem(false, "BAADD2i"+i, "Some description",testHandler));
            }

        } else {
            if (rgui == null) return;
            if (rgui.isRAvailable()) {

                try {
                    rgui.getRLock().lock();

                    String v0 = RUtils.newTemporaryVariableName();

                    new RActionAsyncHelper(rgui, "try({ " + v0 + " <- .PrivateEnv$getPackagesInfo() }) ").run();

                    RChar pNames = (RChar) rgui.obtainR().getObject(v0 + "[, 'Package']");
                    RChar pTitles = (RChar) rgui.obtainR().getObject(v0 + "[, 'Title']");

                    rgui.obtainR().evaluate("try({rm(" + v0 + ");})");

                    String[] pNames_arr = pNames.getValue();
                    String[] pTitles_arr = pTitles.getValue();

                    RChar pLoaded = (RChar) rgui.obtainR().getObject(".packages()");

                    HashMap<String, Integer> loadedMap = new HashMap<String, Integer>();

                    for (String p : pLoaded.getValue()) {
                        loadedMap.put(p, 1);
                    }

                    for (int i=0;i<pNames_arr.length;i++) {
                        pList.add(new ManagerTableItem(loadedMap.containsKey(pNames_arr[i]),
                                pNames_arr[i], pTitles_arr[i], packageHandler));
                    }

                } catch (Exception ex) {
                    JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                            component + " - problem getting the list of available packages",
                            "", JOptionPaneExt.WARNING_MESSAGE);

                } finally {
                    rgui.getRLock().unlock();
                }
            }
        }

        setPackageList(pList);
    }

    private void loadPackage(final String packageName) {
        new Thread(new Runnable(){
            public void run(){
                if (rgui == null) return;
                if (rgui.isRAvailable()) {
                    try {
                        rgui.getRLock().lock();

                        String cmd0 = config.getString(PackageManagerConfig.LOAD_PACKAGE).
                                replaceAll("\\$PKGNAME", packageName);

                        rgui.obtainR().evaluate("try({ " + cmd0 + " })");

                    } catch (Exception ex) {
                        JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                                component + " - problem loading package " + packageName,
                                "", JOptionPaneExt.WARNING_MESSAGE);

                    } finally {
                        rgui.getRLock().unlock();
                    }
                }
            }
        }).start();
    }

    private void unloadPackage(final String packageName) {
        new Thread(new Runnable(){
            public void run(){
                if (rgui == null) return;
                if (rgui.isRAvailable()) {
                    try {
                        rgui.getRLock().lock();

                        String cmd0 = config.getString(PackageManagerConfig.UNLOAD_PACKAGE).
                                replaceAll("\\$PKGNAME", packageName);

                        rgui.obtainR().evaluate("try({ " + cmd0 + " })");

                    } catch (Exception ex) {
                        JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                                component + " - problem unloading package " + packageName,
                                "", JOptionPaneExt.WARNING_MESSAGE);

                    } finally {
                        rgui.getRLock().unlock();
                    }
                }
            }
        }).start();
    }

    private void setPackageList(Vector<ManagerTableItem> list) {
        managerTableModel.setPackageList(list);

        if (isVisible()) {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    managerTableModel.fireTableDataChanged();
                }
            });
        }
    }

    public static class MyJFrame extends JFrame {
        public MyJFrame() {
            init();
        }

        public void init(){
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            JButton button = ButtonUtil.makeButton("manager");

            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event) {
                    new Thread(new Runnable(){
                        public void run(){
                            PackageManagerDialog dialog =
                                    //PackageManagerDialog.getInstance(Workbench.this.getRootFrame(), Workbench.this);
                                    PackageManagerDialog.getInstance(MyJFrame.this, null);
                            //dialog.setLocationRelativeTo(Workbench.this.getRootFrame());
                            dialog.setVisible(true);
                        }
                    }).start();
                }
            });

            add(button, BorderLayout.SOUTH);
            setSize(new Dimension(100,100));
            setLocationRelativeTo(null);
        }
    }

    public static void main(String[] args) {

        System.setProperty("package.manager.test", "true");

        MyJFrame frame = new MyJFrame();
        frame.setVisible(true);

    }
}
