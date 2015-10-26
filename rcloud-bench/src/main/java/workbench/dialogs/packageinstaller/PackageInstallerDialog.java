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
package workbench.dialogs.packageinstaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.textfield.JSearchField;
import uk.ac.ebi.rcloud.server.RType.RChar;
import org.netbeans.swing.etable.ETable;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;
import workbench.manager.opman.Operation;
import workbench.manager.opman.OperationCancelledException;
import workbench.manager.runner.RActionAsyncHelper;
import workbench.util.ButtonUtil;
import workbench.util.RUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 1, 2009
 * Time: 11:22:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class PackageInstallerDialog extends JDialogA { //JDialogExt {

    final private static Logger log = LoggerFactory.getLogger(PackageInstallerDialog.class);

    private RGui rgui;

    private String[] levelItems = {
            "At User Level",
            "At System Level",
            "In Other Location (Will Be Asked)"
    };

    private HashMap<String, PackageRepository>
        reposMap = new HashMap<String, PackageRepository>();

    private JComboBox   reposCombo   = new JComboBox();
    private JSearchField searchField = new JSearchField();
    private JTextField  urlField    = new JTextField("enter repository URL");
    private JButton    refreshPackages = ButtonUtil.makeButton("Refresh");

    private ETable     packageTable  = new ETable();
    private JScrollPane tableScroll  = new JScrollPane(packageTable);

    private JButton installSelected  = ButtonUtil.makeButton("Install Selected");
    private JButton updateInstalled  = ButtonUtil.makeButton("Update Installed");

    private JComboBox    levelCombo  = new JComboBox(levelItems);
    private JTextField locationField = new JTextField("enter location");

    private JLabel    fillerLabel    = new JLabel(" ");

    private InstallerTableModel installerModel = new InstallerTableModel();

    private PackageInstallerConfig config = PackageInstallerConfig.getInstance();

    private Integer updateListLock    = new Integer(0);
    //private InstallerTableView mainView = new InstallerTableView();

    private String[] installedPackages = new String[0];
    private String[] installedVersions = new String[0];

    Runnable updateListRunnable = new Runnable(){
        public void run(){
            synchronized (updateListLock) {
                installerModel.fireTableDataChanged();
            }
        }
    };

    private static Integer singletonLock = new Integer(0);
    private static PackageInstallerDialog dialog = null;

    public static PackageInstallerDialog getInstance(Component c, RGui rgui) {
        if (dialog == null) {
            synchronized (singletonLock) {
                dialog = new PackageInstallerDialog(c, rgui);
                dialog.registerRepositories();
                dialog.initPackageInstaller();
                return dialog;
            }
        } else {
            return dialog;
        }
    }

    private void selectRepositoryField() {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                urlField.requestFocus();
                urlField.setSelectionStart(0);
                urlField.setSelectionEnd(500);
            }
        });
    }

    private void selectLocationField() {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                locationField.requestFocus();
                locationField.setSelectionStart(0);
                locationField.setSelectionEnd(500);
            }
        });
    }

    private Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    private boolean installerLocked = false;

    private void lockInstaller() {
        try {
            reposCombo.setEnabled(false);
            searchField.setEnabled(false);
            refreshPackages.setEnabled(false);
            installSelected.setEnabled(false);
            updateInstalled.setEnabled(false);
            levelCombo.setEnabled(false);
            this.setCursor(waitCursor);
        } finally {
            installerLocked = true;
        }
    }

    private void unlockInstaller() {
        try {
            reposCombo.setEnabled(true);
            searchField.setEnabled(true);
            refreshPackages.setEnabled(true);
            installSelected.setEnabled(true);
            updateInstalled.setEnabled(true);
            levelCombo.setEnabled(true);
            this.setCursor(defaultCursor);
        } finally {
            installerLocked = false;
        }
    }

    private String defaulttext = "enter repository URL";

    private PackageInstallerDialog(Component c, RGui rg) {

        //super((Frame) c, false);
        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), false);

        this.rgui = rg;

        setTitle("Package Installer");
        
        setLayout(new BorderLayout());

        JPanel topContainer  = new JPanel(new BorderLayout(0,0));
        topContainer.setOpaque(false);
        topContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel centerContainer  = new JPanel(new BorderLayout(0,0));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel bottomContainer  = new JPanel(new BorderLayout(0,0));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(topContainer, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        // top
        topContainer.setLayout(new BorderLayout(0,0));
        JPanel topLeft  = new JPanel(new GridLayout(2,1));
        topLeft.setOpaque(false);

        JPanel topRight = new JPanel(new GridLayout(2,1));
        topRight.setOpaque(false);

        JPanel tableSearch = new JPanel(new BorderLayout(0,0));
        tableSearch.setOpaque(false);
        tableSearch.add(searchField, BorderLayout.CENTER);
        
        topLeft.add(reposCombo);
        topLeft.add(refreshPackages);

        topRight.add(urlField);
        topRight.add(tableSearch);

        urlField.setEditable(false);

        topContainer.add(topLeft, BorderLayout.WEST);
        topContainer.add(topRight, BorderLayout.CENTER);

        // center
        centerContainer.setLayout(new BorderLayout(0,0));
        centerContainer.add(tableScroll, BorderLayout.CENTER);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        InstallerTableRenderer renderer = new InstallerTableRenderer();

        packageTable.setGridColor(Color.LIGHT_GRAY);
        packageTable.setModel(installerModel);
        packageTable.setDefaultRenderer(String.class, renderer);
        packageTable.setRowHeight(20);
        packageTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        packageTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        packageTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        packageTable.getTableHeader().setPreferredSize(new Dimension(2, 16));

        // bottom
        bottomContainer.setLayout(new BorderLayout(0,0));

        JPanel bottomLeft  = new JPanel(new GridLayout(3,1));
        JPanel bottomRight = new JPanel(new GridLayout(3,1));
        bottomLeft.setOpaque(false);
        bottomRight.setOpaque(false);

        bottomLeft.add(levelCombo);
        bottomLeft.add(locationField);
        bottomLeft.add(fillerLabel);

        locationField.setEnabled(false);

        bottomRight.add(installSelected);
        bottomRight.add(updateInstalled);
        bottomRight.add(fillerLabel);

        bottomContainer.add(bottomLeft, BorderLayout.CENTER);
        bottomContainer.add(bottomRight, BorderLayout.EAST);


        InstallerTableRowFilter rowFilter = new InstallerTableRowFilter(){
            public String getMask(){
                return searchField.getText();
            }
        };

        installerModel.addRowFilter(rowFilter);

        KeyListener searchListener = new KeyListener() {
            public void keyPressed(KeyEvent event) {}
            public void keyReleased(KeyEvent event) {}
            public void keyTyped(KeyEvent event) {
                EventQueue.invokeLater(updateListRunnable);
            }
        };

        searchField.addKeyListener(searchListener);

        levelCombo.addActionListener(new LevelComboListener());

        reposCombo.addActionListener(new RepoComboListener());

        refreshPackages.addActionListener(new PackageRefreshListener());

        installSelected.addActionListener(new PackageInstallListener());

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

        Dimension windowSize = new Dimension(580, 720);
        setSize(windowSize);
        setPreferredSize(windowSize);

        //pack();
        setLocationRelativeTo(c);
        setResizable(true);
    }

    class PackageRefreshListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PackageRepository repo =
                    reposMap.get(reposCombo.getSelectedItem());

            if (!repo.isDefinedUrl()) {
                try {
                    String url = urlField.getText();

                    new URL(url);
                    repo.setUrl(url);
                } catch (MalformedURLException mue) {

                    new Thread(new Runnable(){
                        public void run(){
                            JOptionPaneExt.showMessageDialog(PackageInstallerDialog.this,
                                    "Repository URL does not look like an URL at all", "",
                                    JOptionPaneExt.WARNING_MESSAGE);

                            selectRepositoryField();
                        }
                    }).start();
                    return;
                }
            }

            updatePackageListVisual(repo);
        }
    }

    class PackageInstallListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            PackageRepository repo =
                    reposMap.get(reposCombo.getSelectedItem());

            installPackages(repo);
        }
    }

    class RepoComboListener implements ActionListener {
        final String REPO_PROP = "repo";

        public void actionPerformed(ActionEvent event){
            if (event.getSource() == reposCombo) {
                final PackageRepository repo =
                        reposMap.get(reposCombo.getSelectedItem());

                PackageRepository repo01 = (PackageRepository)
                        reposCombo.getClientProperty(REPO_PROP);

                if (repo01 == null) {
                    reposCombo.putClientProperty(REPO_PROP, repo);
                }

                EventQueue.invokeLater(new Runnable(){
                    public void run(){

                        String text = urlField.getText();

                        PackageRepository repo0 = (PackageRepository)
                                reposCombo.getClientProperty(REPO_PROP);

                        if (!repo0.isDefinedUrl() && !text.equals(defaulttext)) {
                            repo0.setUrl(text);
                        }

                        if (repo.getUrl().length() == 0) {
                            urlField.setText(defaulttext);
                        } else {
                            urlField.setText(repo.getUrl());
                        }

                        urlField.setEditable(!repo.isDefinedUrl());

                        if (!repo.isDefinedUrl()) {
                            selectRepositoryField();
                        }

                        reposCombo.putClientProperty(REPO_PROP, repo);
                    }
                });

                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        installerModel.setPackageList(repo.getView().getList());
                        installerModel.fireTableDataChanged();
                    }
                });
            }
        }
    }

    class LevelComboListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            if (event.getSource() == levelCombo) {
                boolean other = levelCombo.getSelectedItem().equals(levelItems[2]);
                if (locationField.isEnabled() != other) {
                    locationField.setEnabled(other);
                    if (other) {
                        selectLocationField();
                    }
                }
            }
        }
    }

    private void registerRepository(PackageRepository repo) {
        reposMap.put(repo.getTitle(), repo);
        reposCombo.addItem(repo.getTitle());
    }

    private void unregisterAllRepositories() {
        reposCombo.removeAllItems();
        reposMap.clear();
    }

    private void registerRepositories() {

        unregisterAllRepositories();

        int cnt = config.getRepositoryCount();

        for (int i = 0; i < cnt; i++) {
            registerRepository(new PackageRepository(config.getRepositoryName(i),
                    config.getRepositoryUrl(i),
                    config.getRepositoryInstallCmd(i),
                    config.getRepositoryInitCmd(i),
                    new InstallerTableView()));

            log.info("registering repository " +
                    config.getRepositoryName(i) + " (" + config.getRepositoryUrl(i) + ")");
        }
    }

    private Runnable initInstaller = new Runnable() {
        public void run(){
            if (rgui == null) return;
            if (rgui.isRAvailable()) {

                try {
                    rgui.getRLock().lock();

                    String[] cmds = config.getInitCommands();

                    for (String cmd0 : cmds) {

                        if (cmd0.length() > 0) {
                            String command =
                                    cmd0.replaceAll("\\$VAR0", RUtils.newTemporaryVariableName());

                            new RActionAsyncHelper(rgui, command).run();
                            //rgui.obtainR().evaluate(command);
                        }
                    }

                } catch (Exception ex) {
                    JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                            "Problem initializing package installer", "", JOptionPaneExt.WARNING_MESSAGE);

                    //JOptionPaneExt.showExceptionDialog(rgui.getRootFrame(), ex);
                } finally {
                    rgui.getRLock().unlock();
                }
            }
        }
    };

    private boolean anythingNotWarmedUp(Collection<PackageRepository> repos) {
        for (PackageRepository repo : repos) {
            if (!repo.isWarm()) {
                return true;
            }
        }
        return false;
    }

    private void waitForWarmUpToComplete(Collection<PackageRepository> repos, int timeout) {
        long ct = System.currentTimeMillis();
        while (anythingNotWarmedUp(repos)) {
            if (System.currentTimeMillis() > ct + timeout) break;
            try { Thread.sleep(1000); } catch(Exception ex) {}
        }
    }

    private Runnable warmupRepositories = new Runnable() {
        public void run(){
            if (rgui == null) return;
            if (rgui.isRAvailable()) {

                try {
                    lockInstaller();

                    Collection<PackageRepository> repos = reposMap.values();

                    if (anythingNotWarmedUp(repos)) {
                        for (PackageRepository repo : repos) {
                            final PackageRepository repoToUpdate = repo;

                            new Thread(new Runnable() {
                                public void run(){
                                    cachePackages(repoToUpdate);
                                }
                            }).start();
                        }
                    }

                    waitForWarmUpToComplete(repos, 30 * 1000); // 20 seconds

                    recacheInstalledPackages();

                    for (PackageRepository repo : repos) {
                        updateInstalledPackages(repo);
                    }

                    final PackageRepository repo =
                            reposMap.get(reposCombo.getSelectedItem());

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            installerModel.setPackageList(repo.getView().getList());
                            installerModel.fireTableDataChanged();
                        }
                    });

                } finally {
                    unlockInstaller();
                }
            }
        }
    };


    private void initPackageInstaller() {

        new Thread(initInstaller).start();
    }

    public void initAndWarmupRepositories() {

        new Thread(new Runnable(){
            public void run(){
                initInstaller.run();
                warmupRepositories.run();
            }
        }).start();
    }

    private void cachePackages(PackageRepository repo) {
        try {
            if (repo.getUrl() == null || repo.getUrl().length() == 0) return;
            if (rgui.isRAvailable()) {

                repo.setView(new InstallerTableView());

                try {
                    rgui.getRLock().lock();

                    String v0 = RUtils.newTemporaryVariableName();

                    String cmd0 = "try({ " + v0 + " <- available.packages(" +
                            "contriburl = contrib.url('" + repo.getUrl() + "')) }) ";

                    //rgui.obtainR().evaluate(cmd0);

                    new RActionAsyncHelper(rgui, cmd0).run();

                    RChar apackages = (RChar) rgui.obtainR().getObject(v0 + "[,'Package']");
                    RChar aversions = (RChar) rgui.obtainR().getObject(v0 + "[,'Version']");

                    String cmd2 = "try({rm(" + v0 + ")})";

                    rgui.obtainR().evaluate(cmd2);

                    String[] apackages_arr = apackages.getValue();
                    String[] aversions_arr = aversions.getValue();

                    for (int i=0;i<apackages_arr.length;i++) {
                        repo.getView().add(new InstallerTableItem(apackages_arr[i],
                                InstallerTableItem.NOTINSTALLED,
                                InstallerTableItem.UNDEFINED,
                                aversions_arr[i]));
                    }

                } catch (Exception ex) {
                    JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                            "Problem getting the list of packages from " + repo.getUrl(),
                            "", JOptionPaneExt.WARNING_MESSAGE);

                } finally {
                    rgui.getRLock().unlock();
                }
            }
        } finally {
            repo.setWarm(true);
        }
    }

    private void updatePackageListVisual(final PackageRepository repo) {
        if (rgui == null) return;

        new Thread(new Runnable(){
            public void run(){

                try {
                    lockInstaller();

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            installerModel.setPackageList(new Vector<InstallerTableItem>());
                            installerModel.fireTableDataChanged();
                        }
                    });

                    cachePackages(repo);

                    recacheInstalledPackages();

                    updateInstalledPackages(repo);

                    EventQueue.invokeLater(new Runnable(){
                        public void run(){
                            installerModel.setPackageList(repo.getView().getList());
                            installerModel.fireTableDataChanged();
                        }
                    });

                } finally {
                    unlockInstaller();
                }
            }
        }).start();
    }

    private void updateInstalledPackages(final PackageRepository repo) {

        //log.info("updateInstalledPackages");

        Vector<InstallerTableItem> repoList = repo.getView().getList();

        if (repoList.size() > 0) {

            for (InstallerTableItem item : repoList) {
                item.setInstalledVersion("");
            }

            boolean updateView = false;

            for (int i=0;i<installedPackages.length;i++) {
                InstallerTableItem item = repo.getView().find(installedPackages[i]);

                if (item != null) {
                    item.setInstalledVersion(installedVersions[i]);
                    updateView = true;
                }
            }

            if (updateView) {
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        installerModel.fireTableDataChanged();
                    }
                });
            }
        }

        //log.info("updateInstalledPackages - done");

    }

    private void recacheInstalledPackages() {
        if (rgui == null) return;
        if (rgui.isRAvailable()) {

            //log.info("recacheInstalledPackages");

            try {
                rgui.getRLock().lock();

                //log.info("recacheInstalledPackages - locked");

                String v0 = RUtils.newTemporaryVariableName();

                String cmd1 = "try({ " + v0 + " <- installed.packages() }) ";

                //rgui.obtainR().evaluate(cmd1);

                new RActionAsyncHelper(rgui, cmd1).run();

                RChar ipackages = (RChar) rgui.obtainR().getObject(v0 + "[,'Package']");
                RChar iversions = (RChar) rgui.obtainR().getObject(v0 + "[,'Version']");

                rgui.obtainR().evaluate("try({rm(" + v0 + ");})");

                installedPackages = ipackages.getValue();
                installedVersions = iversions.getValue();

                //log.info("recacheInstalledPackages - done");

            } catch (Exception ex) {
                JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                        "Problem getting the list of installed packages",
                        "", JOptionPaneExt.WARNING_MESSAGE);

            } finally {
                rgui.getRLock().unlock();

                //log.info("recacheInstalledPackages - unlocked");
            }
        }
    }

    private void installPackages(final PackageRepository repo) {
        if (rgui == null) return;

        new Thread(new Runnable(){
            public void run(){
                int[] rows = packageTable.getSelectedRows();

                if (rows.length > 0) {

                    int ci = 0;//packageTable.getColumnModel().getColumnIndex("Package");

                    Vector<String> toInstall = new Vector<String>();

                    for (ci = 0; ci < packageTable.getColumnCount(); ci++) {
                        if (packageTable.getColumnName(ci).equals("Package")) break;
                    }

                    for (int row : rows) {
                        toInstall.add((String)packageTable.getValueAt(row, ci));
                    }

                    try {
                        lockInstaller();

                        final Operation installPackageOp =
                                rgui.getOpManager().createOperation("Installing packages..");

                        int cnt = 0;
                        int tot = toInstall.size();

                        if (rgui.isRAvailable()) {

                            installPackageOp.startOperation();

                            try {
                                String cmd0 = repo.getCmd().replaceAll("\\$LIBPATH",
                                        rgui.getUser().getUserLibFolder()).replaceAll("\\$REPOS", repo.getUrl());

                                for (String packageName : toInstall) {
                                    try {
                                        installPackageOp.setProgress(100 * cnt++ / tot,
                                                "installing " + packageName);

                                    } catch (OperationCancelledException oce) {
                                        break;
                                    }

                                    String cmd1 = cmd0.replaceAll("\\$PKGNAME", packageName);
                                    String cmd2 = "try({ " + cmd1 + " })";


                                    try {
                                        rgui.getRLock().lock();
                                        rgui.obtainR().evaluate(cmd2);
                                    } catch (Exception ex) {
                                        JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                                                "Problem installing a package",
                                                "", JOptionPaneExt.WARNING_MESSAGE);
                                        break;
                                    } finally {
                                        rgui.getRLock().unlock();
                                    }
                                }

                            } finally {
                                installPackageOp.completeOperation();
                            }
                        }

                        recacheInstalledPackages();

                        updateInstalledPackages(repo);

                    } finally {
                        unlockInstaller();
                    }

                } else {
                    JOptionPaneExt.showMessageDialog(rgui.getRootFrame(),
                            "Please select a package to install", "",
                            JOptionPaneExt.WARNING_MESSAGE);
                }  
            }
        }).start();
    }

    private void setPackageList(Vector<InstallerTableItem> list) {
        installerModel.setPackageList(list);
    }


    public static void main(String[] args) {
        PackageInstallerDialog dialog = getInstance(new JFrame(), null);

        Vector<InstallerTableItem> list = new Vector<InstallerTableItem>();

        list.add(new InstallerTableItem("AAA1", InstallerTableItem.NOTINSTALLED, "", "1.0.2"));
        list.add(new InstallerTableItem("AAA2", InstallerTableItem.INSTALLED,    "1.2-2", "1.4-2"));
        list.add(new InstallerTableItem("AAA3", InstallerTableItem.INSTALLED,    "1.3-0", "1.5-0"));
        list.add(new InstallerTableItem("BBB0", InstallerTableItem.NOTINSTALLED, "",   "1.0.2"));
        list.add(new InstallerTableItem("BBB3", InstallerTableItem.NOTINSTALLED, "",   "1.0.2"));
        list.add(new InstallerTableItem("CCC0", InstallerTableItem.INSTALLED, "2.4",   "2.5"));
        list.add(new InstallerTableItem("CCC1", InstallerTableItem.INSTALLED, "2.4",   "2.5"));
        list.add(new InstallerTableItem("CCC2", InstallerTableItem.INSTALLED, "2.4",   "2.5"));
        list.add(new InstallerTableItem("CCC3", InstallerTableItem.INSTALLED, "2.4",   "2.5"));
        list.add(new InstallerTableItem("DDD1", InstallerTableItem.INSTALLED, "1.2",   "1.2"));
        list.add(new InstallerTableItem("DDD2", InstallerTableItem.INSTALLED, "1.3",   "1.3"));

        dialog.setPackageList(list);
        //dialog.setModal(true);
        dialog.setVisible(true);
        //System.exit(0);
    }
}
