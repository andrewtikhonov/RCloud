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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.manager.opman.Operation;
import workbench.manager.opman.OperationCancelledException;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.List;
import java.util.Vector;

import workbench.util.FileLoad;
import workbench.views.filebrowser.actions.Notifier;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 21, 2009
 * Time: 1:19:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileBrowserDnDSupport extends TransferHandler {

    private static Logger log = LoggerFactory.getLogger(FileBrowserDnDSupport.class);

    private RGui rgui    = null;
    private FileBrowserPanel panel = null;

    public FileBrowserDnDSupport(RGui rgui, FileBrowserPanel panel) {
        this.rgui = rgui;
        this.panel = panel;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        for (int i = 0; i < transferFlavors.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(transferFlavors[i])) {
                return true;
            }
        }
        return false;
    }

    public ArrayBlockingQueue<Integer> prepareTokenQueue(int cnt) {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<Integer>(cnt);
        for (int i = 0; i < cnt; i++) {
            q.add( new Integer(i) );
        }
        return q;
    }

    public boolean importData(JComponent comp, Transferable t) {

        if (!canImport(comp, t.getTransferDataFlavors())) {
            return false;
        }

        try {

            Vector<File> toImport = new Vector<File>();

            toImport.addAll((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));

            toImport = fetchListOfFiles(toImport);

            final int cnt = toImport.size();
            final int numThreads = Math.min(cnt, 10);
            final int basedirlen = toImport.get(0).getPath().lastIndexOf(File.separator) + 1;

            final ArrayBlockingQueue<File> workQueue = new ArrayBlockingQueue<File>(toImport.size());
            final ArrayBlockingQueue<Integer> tokenQueue = prepareTokenQueue(numThreads);

            workQueue.addAll(toImport);

            final Operation importOp =
                    rgui.getOpManager().createOperation("Importing " + cnt + " files and folders..");

            String rootpath0 = null;

            Vector<FileNode> selectednodes = panel.getSelectedNodes();

            for (FileNode node : selectednodes) {
                if (node.isDirectory()) {
                    rootpath0 = node.getPath();
                } else {
                    String path0 = node.getPath();
                    rootpath0 = path0.substring(0, path0.length() - node.getName().length() - 1);
                }
            }

            final String destpath = rootpath0 != null ?
                    rootpath0 : ((FileNode) panel.getModel().getRoot()).getPath();

            Runnable transferRunnable = new Runnable() {
                public void run() {
                    RServices r = rgui.obtainR();

                    if (r == null) {
                        JOptionPaneExt.showMessageDialog(rgui.getRootFrame(), "Not connected", "",
                                JOptionPaneExt.WARNING_MESSAGE);
                        return;
                    }

                    try {
                        while (workQueue.size() > 0) {
                            File fromFile = workQueue.poll();

                            if (fromFile != null) {
                                int itemsLeft = workQueue.size();
                                int percentage = cnt * (cnt - itemsLeft) / 100;

                                String filename = fromFile.getPath().substring(basedirlen).
                                        replace(File.separatorChar, FileNode.separatorChar);

                                importOp.setProgress(percentage, "importing " + fromFile.getName());

                                if (fromFile.isDirectory()) {

                                    String newbasepath = destpath + FileNode.separator + filename;

                                    //log.info("newbasepath="+newbasepath);

                                    rgui.obtainR().createRandomAccessDir(newbasepath);

                                } else {
                                    String toFile = destpath + FileNode.separator + filename;

                                    //log.info("import file=" + toFile);

                                    FileLoad.upload(fromFile, toFile, r);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        if (!(ex instanceof OperationCancelledException)) {
                            //JOptionPaneExt.showExceptionDialog(rgui.getRootFrame(), ex);
                            log.error("Problem during D'n'D file import", ex);
                        }
                    } finally {
                        Integer i = tokenQueue.poll();
                        if (tokenQueue.size() == 0) {
                            importOp.completeOperation();
                        }
                    }
                }
            };


            importOp.startOperation();

            for (int i = 0; i < numThreads; i++) {
                new Thread(transferRunnable).start();
            }

            return true;

        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public Vector<File> fetchListOfFiles(Vector<File> toImport) {
        if (toImport.size() > 0) {
            for (int i = 0; i < toImport.size(); i++) {
                File file0 = toImport.get(i);

                if (file0.isDirectory()) {
                    File[] list = file0.listFiles();

                    for (File f : list) {
                        toImport.add(f);
                    }
                }
            }
        }
        return toImport;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }


    public static File createFileInTempdir(String filename) {
        return new File(getTempdir() + File.separator + filename);
    }

    public static String TEMPDIR = null;
    public static String getTempdir() {
        if (TEMPDIR == null) {
            String temp = System.getProperty("java.io.tmpdir");
            if (temp.endsWith("/") || temp.endsWith("\\")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            TEMPDIR = temp;
            return TEMPDIR;
        } else {
            return TEMPDIR;
        }
    }

    class AutoTransferableContainer implements Runnable {
        public Vector<FileNode> nodeList = new Vector<FileNode>();
        public Vector<File> fileList = new Vector<File>();
        public boolean completed = false;
        public long timestamp = 0;
        public Thread consumer = null;

        class CustomNotifier implements Notifier {
            public Operation operation;
            public int total = 1;
            public int items = 0;
            public int getProgress() {
                return 100 / total * items;
            }
            public void call(Object obj) throws Exception {
                operation.setProgress(getProgress(), obj.toString());
            }
        }

        private void doRecursiveExport(Object[] toExport, String destpath, CustomNotifier notifier,
                                       Vector<File> fileList, boolean storeInList) throws Exception {

            if (toExport == null) return;

            for (int i = 0; i < toExport.length; i++) {
                FileNode fromFile = (FileNode) toExport[i];

                if (fromFile.isDirectory()) {

                    File newfolder = new File(destpath, fromFile.getName());

                    //log.info("foler = "+newfolder.getPath());

                    newfolder.mkdirs();

                    if (storeInList) {
                        fileList.add(newfolder);
                        notifier.items++;
                    }

                    if (notifier != null) {
                        notifier.call(fromFile.getName());
                    }

                    if (fromFile.getChildren() == null) {

                        // call model to load the nodes.
                        // model caches path to node (i.e. keeps internal
                        // knowledge of node structure), which is why the
                        // model needs to be called
                        int count = panel.getModel().getChildCount(fromFile);
                    }

                    doRecursiveExport(fromFile.getChildren(), newfolder.getPath(),
                            notifier, fileList, false);

                } else {
                    File newfile = new File(destpath, fromFile.getName());

                    //log.info("file = " + newfile.getPath());

                    if (storeInList) {
                        fileList.add(newfile);
                        notifier.items++;
                    }

                    if (notifier != null) {
                        notifier.call(fromFile.getName());
                    }

                    FileLoad.download(fromFile.getPath(), newfile, rgui.obtainR());
                }
            }
        }

        public void run(){

            timestamp = System.currentTimeMillis();

            final Operation exportOp =
                    rgui.getOpManager().createOperation("Exporting " +
                            nodeList.size() + " files and folders..");

            CustomNotifier notifier = new CustomNotifier();

            notifier.operation = exportOp;
            notifier.total = nodeList.size();

            try {
                exportOp.setEventUnsafe(true);
                exportOp.startOperation();

                doRecursiveExport(nodeList.toArray(), getTempdir(), notifier,
                                       fileList, true);

            } catch (Exception ex) {
                log.error("Error! ", ex);
            } finally {
                exportOp.completeOperation();
                completed = true;
                if (consumer != null) {
                    consumer.interrupt();
                }
            }
        }
    }

    public Transferable createTransferable(JComponent c) {

        AutoTransferableContainer container = new AutoTransferableContainer();
        container.nodeList = panel.getSelectedNodes();

        new Thread(container).start();

        return new TransferringFileList(container, rgui);
    }

    public static class TransferringFileList implements Transferable, ClipboardOwner {

        public static final DataFlavor FILE_LIST_FLAVOR = DataFlavor.javaFileListFlavor;
        private static final DataFlavor[] FLAVORS = { FILE_LIST_FLAVOR };

        private RGui rgui;
        private AutoTransferableContainer container;

        public TransferringFileList(AutoTransferableContainer container, RGui rgui) {
            this.container = container;
            this.rgui = rgui;
        }
        public void lostOwnership(Clipboard clipboard, Transferable transferable) {
            // don't care
        }
        public Object getTransferData(DataFlavor flavor) {
            if (isDataFlavorSupported(flavor)) {

                long to = 1000 * 60 * 10;

                container.consumer = Thread.currentThread();

                while(!container.completed) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    }

                    // last updated container time
                    if (System.currentTimeMillis() -
                            container.timestamp > to) {
                        return null;
                    }
                }
                return container.fileList;
            } else {
                return null;
            }
        }

        public DataFlavor[] getTransferDataFlavors() { return FLAVORS; }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(FILE_LIST_FLAVOR);
        }
    }

    /*
    public Transferable createTransferable2(JComponent c) {
        //return new StringSelection(c.getSelection());
        //java.awt.datatransfer.

        Vector<File> transferable = new Vector<File>();
        Vector<FileNode> nodeList = panel.getSelectedNodes();

        int cnt = nodeList.size();
        int count = 0;

        Operation exportOp =
                rgui.getOpManager().createOperation("Exporting " + cnt + " files and folders..");

        try {
            exportOp.startOperation();

            for (FileNode node : nodeList) {

                count++;

                try {
                    int p = 100 / cnt * count;
                    exportOp.setProgress(p, node.getName());

                    File tempfile = createFileInTempdir(node.getName());// File.createTempFile("", node.getName());
                    FileLoad.download(node.getPath(), tempfile, rgui.obtainR());

                    transferable.add(tempfile);

                } catch (Exception ex) {
                    log.error("Error! ", ex);
                }
            }

        } finally {
            exportOp.completeOperation();
        }

        return new FileListSelection2(transferable);
    }

    public static class FileListSelection2 implements Transferable, ClipboardOwner {

        public static final DataFlavor FILE_LIST_FLAVOR = DataFlavor.javaFileListFlavor;
        private static final DataFlavor[] FLAVORS = { FILE_LIST_FLAVOR };

        private Vector<File> fileList;
        public FileListSelection2(Vector<File> fileList) {
            this.fileList = fileList;
        }
        public void lostOwnership(Clipboard clipboard, Transferable transferable) {
            // don't care
        }
        public Object getTransferData(DataFlavor flavor) {
            return isDataFlavorSupported(flavor) ? fileList : null;
        }

        public DataFlavor[] getTransferDataFlavors() { return FLAVORS; }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(FILE_LIST_FLAVOR);
        }
    }

    */

    /*

    public Transferable createTransferable3(JComponent c) {
        return new FileListSelection3(panel.getSelectedNodes(), rgui);
    }

    public static class FileListSelection3 implements Transferable, ClipboardOwner {

        public static final DataFlavor FILE_LIST_FLAVOR = DataFlavor.javaFileListFlavor;
        private static final DataFlavor[] FLAVORS = { FILE_LIST_FLAVOR };

        private Vector<FileNode> nodeList;
        private RGui rgui;

        public FileListSelection3(Vector<FileNode> nodeList, RGui rgui) {
            this.nodeList = nodeList;
            this.rgui = rgui;
        }
        public void lostOwnership(Clipboard clipboard, Transferable transferable) {
            // don't care
        }
        public Object getTransferData(DataFlavor flavor) {
            if (isDataFlavorSupported(flavor)) {

                Vector<File> transferable = new Vector<File>();

                int cnt = nodeList.size();
                int count = 0;

                Operation exportOp =
                        rgui.getOpManager().createOperation("Exporting " + cnt + " files and folders..");

                try {
                    exportOp.startOperation();

                    for (FileNode node : nodeList) {

                        count++;

                        try {
                            int p = 100 / cnt * count;
                            exportOp.setProgress(p, node.getName());

                            File tempfile = createFileInTempdir(node.getName());
                            //tempfile.
                            FileLoad.download(node.getPath(), tempfile, rgui.obtainR());

                            transferable.add(tempfile);

                        } catch (Exception ex) {
                            log.error("Error! ", ex);
                        }
                    }

                } finally {
                    exportOp.completeOperation();
                }

                return transferable;

            } else {
                return null;
            }
        }

        public DataFlavor[] getTransferDataFlavors() { return FLAVORS; }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(FILE_LIST_FLAVOR);
        }
    }


    */

    public void exportDone(JComponent c, Transferable t, int action) {
        if (action == MOVE) {
            //c.removeSelection();
        }
    }

}
