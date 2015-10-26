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

import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.views.filebrowser.models.TreeModelExt;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 23, 2009
 * Time: 1:50:53 PM
 * To change this template use File | Settings | File Templates.
 */

public class FileTreeModel implements TreeModelExt {

    protected Object root;
    protected Random rnd = new Random(System.currentTimeMillis());

    public FileTreeModel(FileNode root) {
        this.root = root; 
    }

    //@Override
    public Object getChild(Object parent, int index) {
        FileNode node = (FileNode) parent;
        return node.getChild(index);
    }

    //@Override
    public int getChildCount(Object parent) {
        FileNode node = (FileNode) parent;
        return node.getChildCount();
    }

    //@Override
    public int getIndexOfChild(Object parent, Object child) {
        FileNode par = (FileNode) parent;
        FileNode ch  = (FileNode) child;
        return par.indexOfChild(ch);
    }

    //@Override
    public Object getRoot() {
        return root;
    }

    //@Override
    public boolean isLeaf(Object node) {
        if (node != null) {
            FileNode n = (FileNode) node;
            return !n.isDirectory();
        } return true;
    }

    private void recursiveUpdateTree(ArrayList<Object> path,
                                     FileNode parent0,
                                     FileNode parent1, int sqn) {
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


    public void updateTreeRoot(Object tree) {

        FileNode root0 = (FileNode) this.root;
        FileNode root1 = (FileNode) tree;

        if (!root0.isSameFile(root1)) {
            //log.info("FileTreeModel-updateTree-fireTreeStructureChanged");

            this.root = tree;

            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    fireTreeStructureChanged(this, new Object[] { root }, null, null );
                }
            });
        } else {

            //Stopwatch sw = new Stopwatch();
            //sw.start();

            recursiveUpdateTree(
                    new ArrayList<Object>(Arrays.asList(new Object[] { root0 })),
                    root0, root1, rnd.nextInt()); //

            //sw.stop();
        }
    }

    public void updateTreeNode(Object tree) {

        FileNode root0 = (FileNode) this.root;
        FileNode root1 = (FileNode) tree;

        if (!root0.isSameFile(root1)) {
            //log.info("FileTreeModel-updateTree-fireTreeStructureChanged");

            this.root = tree;

            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    fireTreeStructureChanged(this, new Object[] { root }, null, null );
                }
            });
        } else {

            //Stopwatch sw = new Stopwatch();
            //sw.start();

            recursiveUpdateTree(
                    new ArrayList<Object>(Arrays.asList(new Object[] { root0 })),
                    root0, root1, rnd.nextInt()); //

            //sw.stop();
        }
    }

    protected EventListenerList listenerList = new EventListenerList();

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    protected void fireTreeNodesChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {

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
        this.notifier = notifier;
    }

    public void callRenderNotifier() {
        if (this.notifier != null) {
            this.notifier.run();
        }
    }


}
