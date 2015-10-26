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
package uk.ac.ebi.rcloud.server.callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import uk.ac.ebi.rcloud.server.DirectJNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenericCallbackDeviceImpl extends UnicastRemoteObject implements GenericCallbackDevice, Runnable {

    final private Logger log = LoggerFactory.getLogger(getClass());
	                  
    private HashMap<String, GenericCallbackDevice> genericCallbackDeviceHashMap;


    private static int genericCallbackDeviceCounter = 0;
	private String deviceId = GenericCallbackDeviceIdPrefix + (genericCallbackDeviceCounter++);
	private static int port = System.getProperty("rmi.port.start")!=null &&
            !System.getProperty("rmi.port.start").equals("") ? 2 + Integer.decode(System.getProperty("rmi.port.start")) : 0;



    private Vector<GenericRActionListener> actionListenerList = new Vector<GenericRActionListener>();
    private Vector<RAction> rActions = new Vector<RAction>();

    private Integer actionBufferLock = new Integer(0);

    private Thread actionProcessorThread;

	public GenericCallbackDeviceImpl(HashMap<String, GenericCallbackDevice> genericDeviceMap) throws RemoteException{
		super(port);

        genericCallbackDeviceHashMap = genericDeviceMap;
		genericCallbackDeviceHashMap.put(deviceId, this);

        actionProcessorThread = new Thread(this);
        actionProcessorThread.start();
	}

    //   L I S T E N E R S
    //
    //

    public void addListener(GenericRActionListener listener) throws RemoteException {
        actionListenerList.add(listener);
        log.info("addListener");
    }

    public void removeListener(GenericRActionListener listener) throws RemoteException {
        actionListenerList.remove(listener);
        log.info("removeListener");
    }

    public void removeAllListeners() throws RemoteException {
        actionListenerList.removeAllElements();
        log.info("removeAllListeners");
    }

    //   C A L L B A C K S
    //
    //

	public void notify(RAction action) throws RemoteException {
        addAction(action);
	}
		
	public void chat(String sourceUID, String user, String message) throws RemoteException {
        addAction(new RAction(RActionType.CHAT).
                put(RActionConst.MESSAGE, message).
                put(RActionConst.USER, user).
                put(RActionConst.SOURCEUID, sourceUID));
	}
	
	public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
        addAction(new RAction(RActionType.COLLABORATION_PRINT).
                put(RActionConst.SOURCEUID, sourceUID).
                put(RActionConst.USER, user).
                put(RActionConst.RESULT, result).
                put(RActionConst.COMMAND, expression));
	}

    public void write(String text) throws RemoteException {
        addAction(new RAction(RActionType.LOG).put(RActionConst.LOGTEXT, text));
    }

    public RAction crunch(Vector<RAction> actions) {
        return new RAction(RActionType.DEFLATED).put(RActionConst.DATA, deflate(actions, 9));
    }

    public byte[] deflate(Object object, int level) {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream(1024);
        Deflater drCrunch = new Deflater(level);
        DeflaterOutputStream crunchout = new DeflaterOutputStream(compressed, drCrunch);

        try {
            ObjectOutputStream objectout = new ObjectOutputStream(crunchout);
            objectout.writeObject(object);
            objectout.close();

            byte[] compresseddata = compressed.toByteArray();

            //ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            //ObjectOutputStream objectout = new ObjectOutputStream(bos);
            //objectout.writeObject(object);
            //objectout.close();
            //byte[] objectdata = bos.toByteArray();
            //crunchout.write(objectdata, 0, objectdata.length);
            //crunchout.close();
            //byte[] compresseddata = compressed.toByteArray();
            //int ratio = (objectdata.length - compresseddata.length) * 100 / objectdata.length;
            //log.info("actions=" + ((Vector) object).size() +
            //        " raw length="+objectdata.length +
            //        " compressed length="+compresseddata.length +
            //        " ratio=" + ratio + "%");

            return compresseddata;

        } catch (IOException ioe) {
            //log.error("Error!", ioe);
            ioe.printStackTrace();
        }

        return null;
    }


    //   C L E A N U P
    //
    //

    class DeviceShutdownRunnable implements Runnable {
        private String deviceid;

        public DeviceShutdownRunnable(String deviceid) {
            this.deviceid = deviceid;
        }
        public void run(){
            boolean shutdownSucceeded = false;
            while (true) {
                try {
                    shutdownSucceeded = unexportObject(GenericCallbackDeviceImpl.this, false);
                } catch (Exception e) {
                    log.error("Error!", e);
                    shutdownSucceeded = true;
                }

                log.info("object shutdown status:" + shutdownSucceeded);

                if (shutdownSucceeded) {
                    genericCallbackDeviceHashMap.remove(deviceid);

                    log.info("device disposed");
                    break;
                }
                try {Thread.sleep(200);} catch (Exception e) {}
            }

        }
    }

    public void dispose() throws RemoteException {

        log.info("disposing " + deviceId);

        try {
            DirectJNI.getInstance().getRServices().removeRConsoleActionListener(this);
            DirectJNI.getInstance().getRServices().removeRCollaborationListener(this);
            DirectJNI.getInstance().getRServices().removeLogListener(this);
        } catch (Exception e) {
            log.error("Error!", e);
        }

        stopActionProcessorThread();

        removeAllListeners();

        new Thread(new DeviceShutdownRunnable(deviceId)).start();
    }

    public void stopActionProcessorThread() {

        Thread thread = actionProcessorThread;

        actionProcessorThread = null;

        thread.interrupt();
    }

    //
    //
    //

	public String getId() throws RemoteException {
		return deviceId;
	}

    public void addAction(RAction action) {
        synchronized (actionBufferLock) {
            rActions.add(action);
        }
        if (actionProcessorThread != null) actionProcessorThread.interrupt();
    }

    public void run() {
        while(actionProcessorThread == Thread.currentThread()) {
            //log.info("GenericCallbackDeviceImpl-"+deviceId+
            //        "-actionProcessorThread-running");

            if (rActions.size() > 0 && actionListenerList.size() > 0) {

                Vector<RAction> result;

                synchronized (actionBufferLock) {
                    result   = rActions;
                    rActions = new Vector<RAction>();
                }

                Vector<RAction> crunched = new Vector<RAction>();
                crunched.add(crunch(result));

                for (int i = 0; i < actionListenerList.size(); i++) {
                    try {
                        //actionListenerList.elementAt(i).pushActions(result);
                        actionListenerList.elementAt(i).pushActions(crunched);
                    } catch (RemoteException re) {
                        actionListenerList.removeElementAt(i);
                        i--;
                    }
                }
            }

            if (rActions.size() > 0 && actionListenerList.size() > 0) {
                Thread.yield();
            } else {
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                }
            }
        }

        log.info("actionProcessorThread stopped");

        actionProcessorThread = null;
    }



}
