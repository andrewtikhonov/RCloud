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
package workbench.views.findresultview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.file.FileNode;
import uk.ac.ebi.rcloud.server.search.SearchRequest;
import uk.ac.ebi.rcloud.server.search.SearchResult;
import uk.ac.ebi.rcloud.server.search.SearchResultContainer;
import workbench.RGui;
import workbench.actions.WorkbenchActionType;
import workbench.actions.wb.OpenFileAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 30, 2010
 * Time: 5:43:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultBrowser extends JPanel {

    final static private Logger log = LoggerFactory.getLogger(SearchResultBrowser.class);

    private RGui rgui;

    private JEditorPane htmlPane;
    private JTree requesttree;
    private JTree resulttree;

    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";

    private static boolean useSystemLookAndFeel = false;

    private DefaultMutableTreeNode requestnode;
    private DefaultMutableTreeNode resultnode;

    private HashMap<String, AbstractAction> actions = initActions();

    private SearchResultPopupMenu menu = new SearchResultPopupMenu(actions);

    private HashMap<String, AbstractAction> initActions() {
        HashMap<String, AbstractAction> actions = new HashMap<String, AbstractAction>();

        actions.put(SearchResultActionType.JUMP_TO_SOURCE_ACTION, new JumpToSourceAction("Jump To Source"));

            /*
            final Vector<FileNode> toProcess = panel.getSelectedNodes();
            if (toProcess.size() > 0) {
                new Thread(new Runnable(){
                    public void run(){
                        String defaultString = "arguments";

                        String args = (String) JOptionPaneExt.showInputDialog(panel,
                                            command,
                                            title, JOptionPane.PLAIN_MESSAGE,
                                            null, null, defaultString);


            workbenchActions.put("openfile", new AbstractAction(null) {
                public void actionPerformed(final ActionEvent e) {
                    String filename     = e.getActionCommand();
                    String title        = filename;

                    if (filename.contains(FileNode.separator)) {
                        title = title.substring(title.lastIndexOf(FileNode.separator) + 1);
                    }

                    invokeBasicEditor(title, null, filename);
                }

                public boolean isEnabled() {
                    return true;
                }
            });
            */

        return actions;
    }

    class JumpToSourceAction extends AbstractAction {

        public JumpToSourceAction(String name){
            super(name);
        }

        public void actionPerformed(ActionEvent event) {
            //event.getSource();

            TreePath path = resulttree.getSelectionPath();

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            jumpToSource((SearchResult) node.getUserObject());

        }
    }


    public class SearchBrowserMouseAdaptor extends MouseAdapter {

        public SearchBrowserMouseAdaptor() {
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {

                int row = resulttree.getRowForLocation(e.getX(), e.getY());
                int rows[] = resulttree.getSelectionRows();

                if (row == -1) {

                    resulttree.clearSelection();

                } else {

                    boolean found = false;

                    for(int i : rows) {
                        if (i == row) found = true;
                    }

                    if (!found) {
                        resulttree.setSelectionInterval(row, row);
                    }
                }

                menu.updatePopupMenu();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }


        public void mouseClicked(MouseEvent event) {
            //if (event.getClickCount() == 2) {
            //    panel.getActions().get("open").actionPerformed(null);
            //}

            int row = resulttree.getRowForLocation(event.getX(), event.getY());

            TreePath path = resulttree.getPathForLocation(event.getX(), event.getY());

            if(row != -1) {
                if(event.getClickCount() == 1) {
                    //mySingleClick(selRow, selPath);

                    //selPath.getPathComponent()

                    //DefaultMutableTreeNode node = tree.getModel().getChild()

                }

                else if(event.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                            path.getLastPathComponent();

                    if (node.isLeaf()) {

                        SearchResult result = (SearchResult) node.getUserObject();

                        //log.info("going to diaply = " + result.getFile() + " line number " + result.getLinenum());

                        jumpToSource(result);
                    }
                }
            }

        }

        public void mousePressed(MouseEvent event) {
            checkPopup(event);
        }

        public void mouseReleased(MouseEvent event) {
            checkPopup(event);
        }

    }

    public void jumpToSource(SearchResult result) {
        ((OpenFileAction)rgui.getActions().get(WorkbenchActionType.OPENFILE))
                .openWithLinenum(
                        result.getFile().getPath(),
                        result.getLinenum());
    }

    class MyTreeSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent event) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    resulttree.getLastSelectedPathComponent();

            if (node == null) return;

            Object nodeInfo = node.getUserObject();

            if (node.isLeaf()) {

                SearchResult result = (SearchResult) nodeInfo;

                log.info("going to diaply = " + result.getFile() + " line number " + result.getLinenum());

                jumpToSource(result);
            }
        }
    }

    public SearchResultBrowser(RGui rgui) {

        super();

        this.rgui = rgui;

        setLayout(new BorderLayout());

        requestnode = new DefaultMutableTreeNode("Request");
        resultnode = new DefaultMutableTreeNode("Found usages");

        //Create a tree that allows one selection at a time.
        requesttree = new JTree(requestnode);
        resulttree = new JTree(resultnode);

        requesttree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        requesttree.setRootVisible(true);

        resulttree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        resulttree.setRootVisible(true);

        //Listen for when the selection changes.
        //tree.addTreeSelectionListener(new MyTreeSelectionListener());
        resulttree.addMouseListener(new SearchBrowserMouseAdaptor());


        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            resulttree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(resulttree);
        treeView.setBorder(null); // new EmptyBorder(0,0,0,0)

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);

        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the split pane to this panel.
        setPreferredSize(new Dimension(500, 500));
        setLayout(new BorderLayout());
        add(requesttree, BorderLayout.NORTH);
        add(treeView, BorderLayout.CENTER);
    }

    // test GUI
    //
    //

    class SearchFolder {
        private String path;

        public String getPath() {
            return path;
        }

        public SearchFolder(String path) {
            this.path = path;
        }

        public String toString() {
            return path;
        }

    }

    public void addAllSearchInfoSafe(final SearchResultContainer container) { //  Vector<SearchResult> resultset

        EventQueue.invokeLater(new Runnable() {
            public void run(){
                requestnode.removeAllChildren();
                resultnode.removeAllChildren();
                //top.removeAllChildren();

                addSearchRequestInfo(container.getRequest(), false);

                for(SearchResult r: container.getResult()){
                    addSearchResultInfo(r, false);
                }
                requesttree.updateUI();
                resulttree.updateUI();
            }
        });
    }

    public void addSearchRequestInfo(SearchRequest request, boolean updateUI) {
        if (request != null) {
            requestnode.add(new DefaultMutableTreeNode(request));
        } else {
            requestnode.add(new DefaultMutableTreeNode("null"));
        }
    }

    public void addSearchResultInfo(SearchResult result) {
        addSearchResultInfo(result, true);
    }

    public void addSearchResultInfo(SearchResult result, boolean updateUI) {
        String path = result.getFile().getPath();

        int cnt = resultnode.getChildCount();

        for (int i = 0; i < cnt;i++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) resultnode.getChildAt(i);

            SearchFolder folder = (SearchFolder) node.getUserObject();

            if (folder.path.equals(path)) {
                node.add(new DefaultMutableTreeNode(result));
                return;
            }
        }

        DefaultMutableTreeNode folder =
                new DefaultMutableTreeNode(new SearchFolder(result.getFile().getPath()));
        
        folder.add(new DefaultMutableTreeNode(result)); // again, duplicate it

        resultnode.add(folder);

        if (updateUI) {
            requesttree.updateUI();
            resulttree.updateUI();
        }
    }

    private void createTestNodes() {

        Vector<SearchResult> resultList = new Vector<SearchResult>();

        resultList.add(new SearchResult(new FileNode(new File("project-1")),
                "test text", 31));

        resultList.add(new SearchResult(new FileNode(new File("project-2")),
                "another test text 2", 51));

        for (SearchResult result : resultList) {
            addSearchResultInfo(result);
        }

    }

    public static void createGUI(){
        if (useSystemLookAndFeel) {
            try {
                log.info("createGUI - UIManager.setLookAndFeel, NOT SwingEvent Thread");
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        JFrame frame = new JFrame();

        SearchResultBrowser browser = new SearchResultBrowser(null);

        browser.createTestNodes();

        frame.setLayout(new BorderLayout());
        frame.add(browser, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public static void main(String args[]){
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                createGUI();
            }
        });
    }

}
