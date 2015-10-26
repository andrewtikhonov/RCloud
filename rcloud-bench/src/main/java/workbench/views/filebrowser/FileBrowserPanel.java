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
package workbench.views.filebrowser;

import org.netbeans.swing.etable.ETableColumnModel;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.file.FileNode;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import workbench.RGui;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.util.ButtonUtil;
import workbench.views.filebrowser.actions.developer.*;
import workbench.views.filebrowser.actions.preview.PreviewFileAction;
import workbench.views.filebrowser.components.*;
import workbench.views.filebrowser.models.FileRowModel;
import workbench.views.filebrowser.models.FileTreeModel;
import workbench.views.filebrowser.actions.*;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.views.filebrowser.models.FileTreeModelAdvanced;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 23, 2009
 * Time: 1:57:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileBrowserPanel extends JPanel {
    final private static Logger log = LoggerFactory.getLogger(FileBrowserPanel.class);

    private boolean enabled = true;

    private OutlineExtended outline;
    private RGui          rgui = null;
    //private FileTreeModel treeMdl;
    private FileTreeModelAdvanced treeMdl;
    private OutlineModel  mdl;
    private JScrollPane   scroll;

    private JButton       prevB;
    private JButton       nextB;
    private JButton       addB;
    private JTextField addressField = new JTextField();

    private JComboBox               bookmarksCombo = new JComboBox();
    private HashMap<String, String> bookmarksHash = new HashMap<String, String>();

    // lock
    private Object fileTreeLock = new Object();

    // history cache
    private Vector<String> historyCache = new Vector<String>();
    private int            historyIndex = -1;

    private Cursor         hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private HashMap<String, AbstractAction> browserActions;
    private FileBrowserPopupMenu fileBrowserPopupMenu;
    private FileBrowserMouseAdapter fileMouseListener;

    private File foofile = new File("foo");
    private FileNode default_root = new FileNode(foofile);

    private OutlineExpandAction expandAction = new OutlineExpandAction() {
        public void nodeExpanded(Object obj) {
            followLink(((FileNode) obj).getPath());
        }
    };

    private static int NAME = 0;
    private static int DATE = 1;
    private static int TIME = 2;
    private static int SIZE = 3;

    public FileBrowserPanel(RGui gui) {

        this.rgui = gui;

        setLayout(new BorderLayout());

        outline = new OutlineExtended(expandAction);
        outline.setRootVisible(false);
        outline.revalidate();

        browserActions = initBrowserActions();
        fileBrowserPopupMenu = new FileBrowserPopupMenu(browserActions);
        fileMouseListener = new FileBrowserMouseAdapter(this, fileBrowserPopupMenu);

        //treeMdl = new FileTreeModel(default_root);
        treeMdl = new FileTreeModelAdvanced(rgui, default_root);
        mdl     = DefaultOutlineModel.createOutlineModel(treeMdl, new FileRowModel(), true, "Files");

        outline.setModel(mdl);

        outline.getColumnModel().getColumn(NAME).setPreferredWidth(200);
        outline.getColumnModel().getColumn(DATE).setPreferredWidth(60);
        outline.getColumnModel().getColumn(TIME).setPreferredWidth(60);
        outline.getColumnModel().getColumn(SIZE).setPreferredWidth(40);


        outline.getColumnModel().getColumn(DATE).setCellRenderer(new OutlineDateCellRenderer());
        outline.getColumnModel().getColumn(TIME).setCellRenderer(new OutlineTimeCellRenderer());
        outline.getColumnModel().getColumn(SIZE).setCellRenderer(new OutlineSizeCellRenderer());


        outline.getTableHeader().setPreferredSize(new Dimension(2, 16));
        outline.addMouseListener(fileMouseListener);

        // hide 2 columns starting from DATE
        ((ETableColumnModel)outline.getColumnModel()).
                setColumnHidden(outline.getColumnModel().getColumn(DATE), true);

        ((ETableColumnModel)outline.getColumnModel()).
                setColumnHidden(outline.getColumnModel().getColumn(DATE), true);

        scroll = new JScrollPane(outline);
        scroll.addMouseListener(fileMouseListener);

        add(scroll, BorderLayout.CENTER);

        final JPanel topPanel       = new JPanel(new GridLayout(2,0));
        final JPanel toolPanel      = new JPanel(new BorderLayout(0, 0));
        final JPanel toolPanelLeft  = new JPanel(new FlowLayout(0, 0, FlowLayout.CENTER));

        topPanel.setOpaque(false);
        toolPanel.setOpaque(false);
        toolPanelLeft.setOpaque(false);
                                 //String iconPath, String tooltip
                //String caption, String iconPath
        prevB = ButtonUtil.makeButton(null, "/views/images/filebrowser/mainwindow/go-previous.png",
                "Go Back");

        toolPanelLeft.add(prevB);

        nextB = ButtonUtil.makeButton(null, "/views/images/filebrowser/mainwindow/go-next.png",
                "Go Forward");

        toolPanelLeft.add(nextB);

        addB = ButtonUtil.makeButton(null, "/views/images/filebrowser/mainwindow/star_1.png",
                "Add to Bookmarks");

        toolPanelLeft.add(addB);

        toolPanel.add(toolPanelLeft, BorderLayout.WEST);
        toolPanel.add(bookmarksCombo, BorderLayout.CENTER);

        final JPanel addressPanel = new JPanel(new BorderLayout(0, 0));
        addressPanel.setOpaque(false);

        addressField = new JTextField("");

        addressPanel.add(addressField, BorderLayout.NORTH);

        topPanel.add(toolPanel);
        topPanel.add(addressPanel);
        add(topPanel, BorderLayout.NORTH);
        
        // add action listener
        //

        ActionListener toolsListener = new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                if (!FileBrowserPanel.this.enabled) return;

                Object source = event.getSource();

                if (source instanceof JButton) {
                    if (source == prevB) {
                        if (historyIndex > 0) {
                            historyIndex--;
                            followLinkInternal(historyCache.get(historyIndex));
                        }
                    }
                    if (source == nextB) {
                        if (historyIndex < historyCache.size() - 1) {
                            historyIndex++;
                            followLinkInternal(historyCache.get(historyIndex));
                        }
                    }
                    if (source == addB) {
                        String link = addressField.getText();

                        if (link != null && !link.equals("")) {

                            if (link.endsWith(FileNode.separator)) {
                                link = link.substring(0, link.length() - 1);
                            }

                            String name = getPathShortName(link);

                            addBookmark(name, link);
                        }
                    }
                }
                if (source instanceof JTextField) {
                    if (source == addressField) {
                        followLink(addressField.getText());
                    }
                }
                if (source instanceof JComboBox) {
                    if (source == bookmarksCombo) {
                        String key = (String) bookmarksCombo.getSelectedItem();
                        if (key != null) {
                            String value = bookmarksHash.get(key);
                            if (value != null) {
                                followLink(value);
                            }
                        }
                    }
                }
            }
        };

        prevB.addActionListener(toolsListener);
        nextB.addActionListener(toolsListener);
        addB.addActionListener(toolsListener);
        addressField.addActionListener(toolsListener);
        bookmarksCombo.addActionListener(toolsListener);

        initBrowserDnDSupport();

        // disable it
        setEnabled(false);
        addressField.setEnabled(false);
        bookmarksCombo.setEnabled(false);

    }

    private String getPathShortName(String path) {
        String name = path;

        int idx = 0;
        int cnt = 0;

        while((idx = name.indexOf(FileNode.separator, idx)) != -1) {
            idx++;
            cnt++;

            if (cnt > 5) {
                name = "..."+name.substring(idx);
                break;
            }
        }
        return name;
    }

    public Vector<FileNode> getSelectedNodes() {
        Vector<FileNode> result = new Vector<FileNode>();
        synchronized (fileTreeLock) {
            int[] rows = outline.getSelectedRows();
            if (rows.length > 0) {
                int ci = outline.getColumnIndex("Files");
                for (int row : rows) {
                    result.add((FileNode) outline.getValueAt(row, ci));
                }
            }
        }
        return result;
    }

    public int[] getSelectedRows() {
        synchronized (fileTreeLock) {
            return  outline.getSelectedRows();
        }
    }

    public HashMap<String, AbstractAction> initBrowserActions() {

        HashMap<String, AbstractAction> actions = new HashMap<String, AbstractAction>();

        actions.put(FileBrowserActionType.OPEN,     new OpenFileAction(rgui, this, "Open"));
        actions.put(FileBrowserActionType.NEWFOLDER,    new CreateFolderAction(rgui, this,    "Create Folder"));
        actions.put(FileBrowserActionType.NEWFILE,  new CreateFileAction(rgui, this,    "Create File"));
        actions.put(FileBrowserActionType.IMPORT,   new FileImportAction(rgui, this, "Import into Project"));
        actions.put(FileBrowserActionType.EXPORT,   new FileExportAction(rgui, this, "Export from Project"));
        actions.put(FileBrowserActionType.RENAME,   new RenameFileAction(rgui, this, "Rename"));
        actions.put(FileBrowserActionType.REMOVE,   new RemoveFileAction(rgui, this, "Remove"));


        actions.put(FileBrowserActionType.FIND,     new FindInPathAction(rgui, this, "Find in path"));
        actions.put(FileBrowserActionType.TEMPLATE, new TemplatePackageAction(rgui, this, "Package Template"));
        actions.put(FileBrowserActionType.INSTALL,  new InstallPackageAction(rgui, this, "Install Package"));
        actions.put(FileBrowserActionType.CHECK,    new CheckPackageAction(rgui, this, "Check Package"));
        actions.put(FileBrowserActionType.CONFIG,   new ConfigPackageAction(rgui, this, "Config Package"));
        actions.put(FileBrowserActionType.BUILD,    new BuildPackageAction(rgui, this, "Build Package"));
        actions.put(FileBrowserActionType.COMMAND,  new RunCommandAction(rgui, this, "Run Command"));

        actions.put(FileBrowserActionType.COMPRESS,     new CompressFilesAction(rgui, this, "Zip"));
        actions.put(FileBrowserActionType.UNCOMPRESS,   new UncompressFilesAction(rgui, this, "Unzip"));

        actions.put(FileBrowserActionType.MAKELINK,     new FileLinkDisplayAction(rgui, this, "Make Link"));
        actions.put(FileBrowserActionType.IMPORTLINK,   new FileLinkImportAction(rgui, this, "Import Link"));

        actions.put(FileBrowserActionType.PREVIEW,  new PreviewFileAction(rgui, this, "Preview"));

        initOutlineKeyActions(actions);

        return actions;
    }

    public void initBrowserDnDSupport() {

        FileBrowserDnDSupport dnd = new FileBrowserDnDSupport(this.rgui, this);

        outline.setDragEnabled(true);
        outline.setTransferHandler(dnd);
        scroll.setTransferHandler(dnd);
    }

    public void initOutlineKeyActions(HashMap<String, AbstractAction> actions) {

        ActionMap actionMap = outline.getActionMap();
        InputMap inputMap = outline.getInputMap();

        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove-files0");
        actionMap.put("remove-files0", actions.get(FileBrowserActionType.REMOVE));

        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.META_MASK), "remove-files1");
        actionMap.put("remove-files1", actions.get(FileBrowserActionType.REMOVE));
    }

    class SetAddressLinkRunnable implements Runnable {
        private String path;
        private boolean enabled;
        public SetAddressLinkRunnable(String path, boolean enabled){
            this.path = path;
            this.enabled = enabled;
        }
        public void run(){
            if (enabled != addressField.isEnabled()) {
                addressField.setEnabled(enabled);
            }
            addressField.setText(path);
        }
    }

    class FollowLinkInternalRunnable implements Runnable {
        private String addressString;
        public FollowLinkInternalRunnable(String addressString) {
            this.addressString = addressString;
        }

        public void run() {

            if (rgui.isRAvailable()) {
                RServices r = rgui.obtainR();
                String path = addressString;

                if (addressString.startsWith("r:")) {
                    try {
                        String cmd = addressString.substring(2);
                        path = (String) r.getObjectConverted(cmd);
                    } catch (RemoteException ex) {
                        log.error("Error!", ex);
                    }
                }

                EventQueue.invokeLater(new SetAddressLinkRunnable(path, true));

                try {
                    setEnabled(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    r.setRootDirectory(path);
                } catch (RemoteException re) {
                    log.error("Error!", re);
                }
            }
        }
    }

    private void followLinkInternal(String addressString) {

        if (addressString == null) {
            try {
                throw new NullPointerException("link == null");
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }
        
        new Thread(new FollowLinkInternalRunnable(addressString)).start();

    }

    public void cacheLink(String link) {

        if (historyIndex < historyCache.size() - 1) {
            for(int i=historyCache.size()-1; i>historyIndex; i--) {
                historyCache.remove(i);
            }
        }

        historyCache.add(link);
        historyIndex = historyCache.size() - 1;
    }

    public void emptyCache() {

        historyCache.removeAllElements();
        historyIndex = historyCache.size() - 1;
    }

    public void followLink(String path) {

        cacheLink(path);
        followLinkInternal(path);
    }

    class AddBookmarkRunnable implements Runnable {
        private String name;
        private String link;
        public AddBookmarkRunnable(String name, String link) {
            this.name = name;
            this.link = link;
        }
        public void run(){
            boolean add = !bookmarksHash.containsKey(name);

            bookmarksHash.put(name, link);
            if (add) {
                bookmarksCombo.addItem(name);
            }

            if (!bookmarksCombo.isEnabled())
                bookmarksCombo.setEnabled(true);
        }
    }

    public void addBookmark(String name, String link) {
        EventQueue.invokeLater(new AddBookmarkRunnable(name, link));
    }

    class EmptyBookmarksRunnable implements Runnable {
        public void run(){
            bookmarksHash.clear();
            bookmarksCombo.removeAllItems();

            if (bookmarksCombo.isEnabled())
                bookmarksCombo.setEnabled(false);
        }
    }

    public void emptyBookmarks() {
        EventQueue.invokeLater(new EmptyBookmarksRunnable());
    }

    public void updateTreeRoot(FileNode root) {
        setEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        synchronized (fileTreeLock) {
            treeMdl.updateTreeRoot(root);
        }
    }

    public void updateTreeNode(FileNode node) {
        synchronized (fileTreeLock) {
            treeMdl.updateTreeNode(node);
        }
    }

    public void emptyTree() {
        updateTreeRoot(default_root);
    }

    public void emptyAddressBar() {
        EventQueue.invokeLater(new SetAddressLinkRunnable("", false));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HashMap<String, AbstractAction> getActions() {
        return browserActions;
    }

    public OutlineExtended getOutline() {
        return outline;
    }

    public FileTreeModelAdvanced getModel() {
        return treeMdl;
    }

    public Object getLock() {
        return fileTreeLock;
    }

    public static void setupGUI(){
        JFrame frame = new JFrame();
        FileBrowserPanel bro = new FileBrowserPanel(null);

        frame.add(bro);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    static class SetupGUIRunnable implements Runnable {
        public void run(){
            setupGUI();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new SetupGUIRunnable());
    }
}

