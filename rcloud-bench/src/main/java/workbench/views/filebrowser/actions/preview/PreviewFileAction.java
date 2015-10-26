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
package workbench.views.filebrowser.actions.preview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;
import workbench.manager.opman.Operation;
import workbench.manager.opman.OperationCancelledException;
import workbench.util.FileLoad;
import workbench.views.filebrowser.FileBrowserPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 19, 2010
 * Time: 9:36:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class PreviewFileAction extends AbstractAction {

    private RGui rgui   = null;
    private FileBrowserPanel panel  = null;
    private static Logger log = LoggerFactory.getLogger(PreviewFileAction.class);

    public PreviewFileAction(RGui rgui, FileBrowserPanel panel, String name){
        super(name);
        this.rgui  = rgui;
        this.panel = panel;
    }

    private static HashMap<String, Boolean> imageExtensions = new HashMap<String, Boolean>();

    static {
        imageExtensions.put(".png", Boolean.TRUE);
        imageExtensions.put(".jpg", Boolean.TRUE);
        imageExtensions.put(".gif", Boolean.TRUE);
        imageExtensions.put(".tiff", Boolean.TRUE);
        imageExtensions.put(".jpeg", Boolean.TRUE);
        imageExtensions.put(".ico", Boolean.TRUE);
    }

    public ArrayBlockingQueue<Integer> prepareTokenQueue(int cnt) {
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<Integer>(cnt);
        for (int i = 0; i < cnt; i++) {
            q.add( new Integer(i) );
        }
        return q;
    }

    public boolean checkToShowAWeightyFile(String name, long length) {
        Object[] options = { "Preview", "Cancel" };

        int reply = JOptionPaneExt.showOptionDialog(panel,
                name + " seems weighty ( ~" + length + " MB )", "",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        return (reply == 0);
    }

    public void actionPerformed(ActionEvent event) {
        final Vector<FileNode> toShow = panel.getSelectedNodes();

        if (toShow.size() > 0) {
            new Thread(new Runnable(){
                public void run(){


                    int numThreads = Math.min(toShow.size(), 10);

                    final ArrayBlockingQueue<FileNode> workQueue =
                            new ArrayBlockingQueue<FileNode>(toShow.size());

                    final ArrayBlockingQueue<Integer> tokenQueue = prepareTokenQueue(numThreads);

                    workQueue.addAll(toShow);

                    final Operation previewOp =
                            rgui.getOpManager().createOperation("Preview..", true);

                    Runnable previewRunnable = new Runnable() {
                        public void run() {
                            RServices r = rgui.obtainR();

                            if (r == null) {
                                JOptionPaneExt.showMessageDialog(rgui.getRootFrame(), "Not connected", "",
                                        JOptionPaneExt.WARNING_MESSAGE);
                                return;
                            }

                            try {
                                while (workQueue.size() > 0) {
                                    FileNode previewFile = workQueue.poll();

                                    if (previewFile != null) {

                                        long threashold = 1024 * 1024;

                                        if (previewFile.length() > threashold) {

                                            if (!checkToShowAWeightyFile(previewFile.getName(),
                                                    previewFile.length() / threashold)) {
                                                continue;
                                            }
                                        }

                                        byte[] filedata =
                                                FileLoad.read(previewFile.getPath(), rgui.obtainR());

                                        if (isImage(previewFile.getName())) {
                                            previewImage(filedata);
                                        } else {
                                            previewText(filedata);
                                        }
                                    }
                                }

                            } catch (Exception ex) {
                                if (!(ex instanceof OperationCancelledException)) {
                                    //JOptionPaneExt.showExceptionDialog(rgui.getRootFrame(), ex);
                                    log.error("Problem during image preview", ex);
                                }
                            } finally {
                                tokenQueue.poll();

                                if (tokenQueue.size() == 0) {
                                    previewOp.completeOperation();
                                }
                            }
                        }
                    };

                    previewOp.startOperation();

                    for (int i = 0; i < numThreads; i++) {
                        new Thread(previewRunnable).start();
                    }
                }
            }).start();
        }
    }

    private boolean isImage(String fileName){
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            String extesion = fileName.substring(dotIndex);
            return (imageExtensions.get(extesion) != null);
        }
        return false;
    }


    private void previewImage(byte[] imagedata){
        ImagePreviewFrame frame = new ImagePreviewFrame(imagedata);
        frame.setVisible(true);
    }

    private void previewText(byte[] textdata){
        TextPreviewFrame frame = new TextPreviewFrame(new String(textdata));
        frame.setVisible(true);
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable() && panel.getOutline().getSelectedRows().length > 0);
    }
}
