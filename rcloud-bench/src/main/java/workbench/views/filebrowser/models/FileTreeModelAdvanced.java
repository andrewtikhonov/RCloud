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
package workbench.views.filebrowser.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 22, 2010
 * Time: 2:28:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileTreeModelAdvanced implements TreeModelExt {

    final private static Logger log = LoggerFactory.getLogger(FileTreeModelAdvanced.class);

    protected RGui rgui;
    protected Object root;
    protected Random rnd = new Random(System.currentTimeMillis());
    protected HashMap<String, WeakReference> pathToNodeMap = new HashMap<String, WeakReference>();

    public FileTreeModelAdvanced(RGui rgui, FileNode root) {
        this.root = root;
        this.rgui = rgui;
    }

    //@Override
    public Object getChild(Object parent, int index) {
        FileNode node = (FileNode) parent;
        return node.getChild(index);
    }

    //@Override
    public int getChildCount(Object parent) {
        //log.info("getChildCount");

        FileNode node = (FileNode) parent;

        if (node.isDirectory() && node.getChildren() == null) {
            try {
                //log.info("getChildCount-requesting-node="+node.getName());

                FileNode node0 = rgui.obtainR().readDirectory(node.getPath());

                //log.info("getChild-setting");

                node.setChildren(node0.getChildren(), node0.getChildCount());

                //log.info("getChild-updating path 2 node map");

                pathToNodeMap.put(node.getPath(), new WeakReference(node));

            } catch (RemoteException re) {
                log.error("Error!", re);
            }
        }

        return node.getChildCount();
    }

    //@Override
    public int getIndexOfChild(Object parent, Object child) {
        //log.info("getIndexOfChild");

        FileNode par = (FileNode) parent;
        FileNode ch  = (FileNode) child;
        return par.indexOfChild(ch);
    }

    //@Override
    public Object getRoot() {
        //log.info("getRoot");

        return root;
    }

    //@Override
    public boolean isLeaf(Object node) {
        if (node != null) {
            FileNode n = (FileNode) node;
            return !n.isDirectory();
        }

        return true;
    }

    private void recursiveUpdateTree(ArrayList<Object> path,
                                     FileNode parent0,
                                     FileNode parent1, int sqn) {

        //log.info("recursiveUpdateTree");

        // add files left in the files list
        for (int i = 0; i < parent1.getChildCount(); i++) {

            FileNode    kid0 = null;
            FileNode    kid1 = (FileNode) parent1.getChild(i);

            // remove file from files list
            for (int idx=0; idx < parent0.getChildCount(); idx++) {

                FileNode item = (FileNode) parent0.getChild(idx);

                if (item.getSQN() != sqn && item.isSameFile(kid1)) {
                    kid0 = item;

                    kid0.setSQN(sqn);

                    if (!kid0.isUpToDate(kid1)) {
                        kid0.update(kid1);

                        // record the event
                        final int    index = idx;
                        final Object kiddo = kid0;
                        final Object[] pth = path.toArray();

                        EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                fireTreeNodesChanged(this, pth, new int[]{index},
                                        new Object[]{kiddo});
                            }
                        });

                        //log.info("FileBrowserPanel-recursiveUpdateTreePlain2-UPDATED ="
                        //        +kid0.getFile().getPath());
                    }

                    if (kid0.isDirectory()) {
                        ArrayList<Object> newpath = new ArrayList<Object>(path);
                        newpath.add(item);
                        recursiveUpdateTree(newpath, kid0, kid1, sqn);
                    }

                    break;
                }
            }

            if (kid0 == null) {

                //log.info("FileBrowserPanel-recursiveUpdateTreePlain2-NEW ="
                //        +kid1.getFile().getPath());

                // node does not exist, create a new
                kid0 = kid1;
                kid0.setSQN(sqn);
                kid0.setAncestor(parent0);
                parent0.addChildAt(i, kid0);

                // record the event
                final int    index = i;
                final Object kiddo = kid0;
                final Object[] pth = path.toArray();

                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        fireTreeNodesInserted(this, pth, new int[]{index},
                                new Object[]{kiddo});
                    }
                });
            }

        }

        // remove deleted files from the tree
        for (int i = 0; i < parent0.getChildCount(); i++) {

            FileNode kid0 = (FileNode) parent0.getChild(i);

            if (kid0.getSQN() != sqn) {
                //log.info("FileBrowserPanel-recursiveUpdateTree-remove-kid="
                //        +kid0.getFile().getPath()+" sqn="+sqn+" kid.getSQN()="+kid0.getSQN());

                parent0.removeChildAt(i);

                // record the event
                final int    index = i;
                final Object kiddo = kid0;
                final Object[] pth = path.toArray();

                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        fireTreeNodesRemoved(this, pth, new int[]{index},
                                new Object[]{kiddo});
                    }
                });

                i--;
            }
        }
    }

    private ArrayList<Object> getNodePathArray (FileNode node) {

        FileNode parent = (FileNode) node.getAncestor();

        ArrayList<Object> pathList;

        if (parent != null) {
            pathList = getNodePathArray(parent);
        } else {
            pathList = new ArrayList<Object>();
        }

        pathList.add(node);
        return pathList;
    }

    private void plainUpdateTreeNode(FileNode parent1, int sqn) {

        //log.info("plainUpdateTreeNode");

        WeakReference nodeReference = pathToNodeMap.get(parent1.getPath());

        if (nodeReference == null) {
            log.error("plainUpdateTreeNode-nodeReference=null, parent1="+parent1.getPath()+" NO REFERENCE");
            return;
        }

        FileNode parent0 = (FileNode) nodeReference.get();

        if (parent0 == null) {
            pathToNodeMap.remove(parent1.getPath());
            log.error("plainUpdateTreeNode-parent0=null, parent1="+parent1.getPath()+" STALE REFERENCE");
            return;
        }

        final Object[] pth = getNodePathArray(parent0).toArray();

        // add files left in the files list
        for (int i = 0; i < parent1.getChildCount(); i++) {

            FileNode    kid0 = null;
            FileNode    kid1 = (FileNode) parent1.getChild(i);

            // remove file from files list
            for (int idx=0; idx < parent0.getChildCount(); idx++) {

                FileNode item = (FileNode) parent0.getChild(idx);

                if (item.getSQN() != sqn && item.isSameFile(kid1)) {
                    kid0 = item;

                    kid0.setSQN(sqn);

                    if (!kid0.isUpToDate(kid1)) {
                        kid0.update(kid1);

                        // record the event
                        final int    index = idx;
                        final Object kiddo = kid0;

                        EventQueue.invokeLater(new Runnable(){
                            public void run(){
                                fireTreeNodesChanged(this, pth, new int[]{index},
                                        new Object[]{kiddo});
                            }
                        });

                        //log.info("FileBrowserPanel-recursiveUpdateTreePlain2-UPDATED ="
                        //        +kid0.getFile().getPath());
                    }

                    break;
                }
            }

            if (kid0 == null) {

                //log.info("FileBrowserPanel-recursiveUpdateTreePlain2-NEW ="
                //        +kid1.getFile().getPath());

                // node does not exist, create a new
                kid0 = kid1;
                kid0.setSQN(sqn);
                kid0.setAncestor(parent0);
                parent0.addChildAt(i, kid0);

                // record the event
                final int    index = i;
                final Object kiddo = kid0;

                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        fireTreeNodesInserted(this, pth, new int[]{index},
                                new Object[]{kiddo});
                    }
                });
            }

        }

        // remove deleted files from the tree
        for (int i = 0; i < parent0.getChildCount(); i++) {

            FileNode kid0 = (FileNode) parent0.getChild(i);

            if (kid0.getSQN() != sqn) {
                //log.info("FileBrowserPanel-recursiveUpdateTree-remove-kid="
                //        +kid0.getFile().getPath()+" sqn="+sqn+" kid.getSQN()="+kid0.getSQN());

                parent0.removeChildAt(i);

                // record the event
                final int    index = i;
                final Object kiddo = kid0;

                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        fireTreeNodesRemoved(this, pth, new int[]{index},
                                new Object[]{kiddo});
                    }
                });

                i--;
            }
        }
    }


    public void updateTreeRoot(Object tree) {

        //log.info("updateTreeRoot");

        this.root = (FileNode) tree;

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                fireTreeStructureChanged(this, new Object[] { root }, null, null );
            }
        });
    }

    public void updateTreeNode(Object tree) {

        //log.info("updateTreeNode");

        //FileNode root0 = (FileNode) this.root;
        FileNode tree0 = (FileNode) tree;

        plainUpdateTreeNode(tree0, rnd.nextInt()); //

    }

    protected EventListenerList listenerList = new EventListenerList();

    public void valueForPathChanged(TreePath path, Object newValue) {
        //log.info("valueForPathChanged");

    }

    public void addTreeModelListener(TreeModelListener l) {
        //log.info("addTreeModelListener");

        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        //log.info("removeTreeModelListener");

        listenerList.remove(TreeModelListener.class, l);
    }

    protected void fireTreeNodesChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {

        //log.info("fireTreeNodesChanged");

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }
        }

        callRenderNotifier();
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        //log.info("fireTreeNodesInserted");

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
            }
        }

        callRenderNotifier();
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {

        //log.info("fireTreeNodesRemoved");

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
            }
        }

        callRenderNotifier();
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {

        //log.info("fireTreeStructureChanged");

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
            }
        }

        callRenderNotifier();
    }

    private Runnable notifier = null;

    public void setRenderNotifier(Runnable notifier) {
        //log.info("setRenderNotifier");

        this.notifier = notifier;
    }

    public void callRenderNotifier() {
        //log.info("callRenderNotifier");

        if (this.notifier != null) {
            this.notifier.run();
        }
    }

}
