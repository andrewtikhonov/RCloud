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
package workbench.manager.opman;

import uk.ac.ebi.rcloud.common.components.dialog.JDialogExt;
import uk.ac.ebi.rcloud.common.components.panel.JProgressPanelA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.util.ButtonUtil;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 10, 2009
 * Time: 3:35:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperationManager {
    final private static Logger log = LoggerFactory.getLogger(OperationManager.class);

    public static final int NONE           = 0;
    public static final int OPENPROJECT    = 1;
    public static final int CLOSEPROJECT   = 2;
    public static final int SHUTDOWNSERVER = 3;
    public static final int SHUTDOWNBENCH  = 4;
    
    private JProgressPanelA progressPanel;
    private JDialogExt     progressDialog;
    private Operation      operation;
    private Component      parent;

    private Vector<OperationStateListener> listenerList = new Vector<OperationStateListener>();

    public OperationManager(Component c) {
        this.parent = c;

        progressPanel = new JProgressPanelA();
        progressPanel.setCancelAction(new Runnable(){
            public void run() {
                if (operation != null) {
                    operation.abortOperation();
                }
            }
        });

        progressDialog = new JDialogExt((Frame) c, "");
        //progressDialog = new JDialog((Frame) c, "");

        progressDialog.setModal(false);
        progressDialog.setSize(400, 160);
        //progressDialog.setContentPane(progressPanel);
        progressDialog.add(progressPanel, BorderLayout.CENTER);
        progressDialog.setLocationRelativeTo(c);
    }

    public Operation createOperation(String title) {
        return new Operation(title, this);
    }

    public Operation createOperation(String title, boolean indefinite) {
        return new Operation(title, this, indefinite);
    }

    public void updateOperationProgress(Operation op) {

        if (op.getId() == operation.getId()) {
            progressPanel.setProgress(op.getProgress());
            if (operation.isEventUnsafe()) {
                forceRepaint();
            }

        } else {
            log.error("OperationManager-setProgress-operation=" + operation + " op="+op);
        }
    }

    public void updateToolTip(String tip) {
        progressPanel.setToolTip(tip);
    }

    public void resetProgressPanel() {
        progressDialog.setVisible(false);
        progressPanel.setLabel(" ");
        progressPanel.setToolTip(" ");
        operation = null;
    }

    public void  updateOperationState(Operation op) {
        switch (op.getState()) {
            case OperationState.NONE :
                break;

            case OperationState.STARTED :
                if (isLocked()) {
                    log.error("OperationManager-updateOperationState-locked-operation=" + operation + " op="+op);
                }

                operation = op;
                progressPanel.setEventUnsafe(operation.isEventUnsafe());
                progressPanel.setLabel(operation.getTitle());
                progressPanel.setToolTip(" ");
                progressPanel.setProgress(0);
                progressPanel.getProgressBar().setIndeterminate(op.isIndeterminate());
                progressDialog.setLocationRelativeTo(parent);

                //progressDialog.repaint();
                progressDialog.setVisible(true);
                progressDialog.validate();

                fireOperationStarted(op);

                if (operation.isEventUnsafe()) {
                    forceRepaint();
                }
                break;

            case OperationState.COMPLETED :
                resetProgressPanel();

                fireOperationCompleted(op);
                break;

            case OperationState.ABORTED :
                resetProgressPanel();

                fireOperationAborted(op);
                break;
        }
    }

    public boolean isOperationInProgress(Operation op) {
        return (operation.getId() == op.getId());
    }

    public boolean isLocked() {
        return operation != null;
    }

    public void addOperationStateListener(OperationStateListener listener) {
        listenerList.add(listener);
    }

    public void removeOperationStateListener(OperationStateListener listener) {
        listenerList.remove(listener);
    }

    private void fireOperationStarted(Operation op) {
        for(OperationStateListener l : listenerList) {
            l.operationStarted(op);
        }
    }

    private void fireOperationCompleted(Operation op) {
        for(OperationStateListener l : listenerList) {
            l.operationCompleted(op);
        }
    }

    private void fireOperationAborted(Operation op) {
        for(OperationStateListener l : listenerList) {
            l.operationAborted(op);
        }
    }

    public void forceRepaint() {
        progressDialog.paintAll(progressDialog.getGraphics());
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame();

        JButton button = ButtonUtil.makeButton("run");

        final OperationManager gom = new OperationManager(frame);

        final Runnable job = new Runnable(){
            public void run(){

                Operation op = gom.createOperation("some cool operation...");

                op.startOperation();

                int i = 0;
                while(i < 100) {
                    try { Thread.sleep(1000 + (int)(Math.random() * 200)); } catch (Exception ex) {}
                    i+=5;

                    try {
                        //log.info("i="+i+" currentThread()="+Thread.currentThread());

                        op.setProgress(i, "progress "+i+"%");
                    } catch (OperationCancelledException oae) {
                        log.info("OperationCancelledException");
                        return;
                    }
                }

                op.completeOperation();
            }
        };

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                new Thread(job).start();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(button);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
