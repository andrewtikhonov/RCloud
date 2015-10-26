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
package uk.ac.ebi.rcloud.http;

import java.rmi.NotBoundException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import uk.ac.ebi.rcloud.rpf.db.DBLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.data.ProjectDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;
import uk.ac.ebi.rcloud.server.callback.GenericCallbackDevice;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.callback.GenericRActionListener;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.rpf.ServantProviderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.ServantProvider;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;


public class FreeResourcesListener implements HttpSessionListener {
    final private Logger log = LoggerFactory.getLogger(getClass());

	public void sessionCreated(HttpSessionEvent sessionEvent) {
        log.info(" % Session created :" + sessionEvent.getSession().getId());
	}

	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        log.info(" % Session to destroy :" + sessionEvent.getSession().getId());

		HashMap<String, HashMap<String, Object>> map = ((HashMap<String, HashMap<String, Object>>)
                sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP"));

        if (map == null)return;

        HashMap<String, Object> attributes = map.get(sessionEvent.getSession().getId());

		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_MAP")).remove(sessionEvent.getSession().getId());
		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP")).remove(sessionEvent.getSession().getId());
		
		if (attributes == null)	return;
		
		if (!attributes.get("TYPE").equals("RS")) return;
		
		final RServices rservices = (RServices) attributes.get("R");

		if (rservices == null)
			return;
		
		Vector<HttpSession> r_sessions=((HashMap<RServices, Vector<HttpSession>>) sessionEvent.getSession().getServletContext().getAttribute("R_SESSIONS")).get(rservices);

        log.info("sessions linked to this r :" + r_sessions);
		for (int i=0; i<r_sessions.size();++i) log.info(r_sessions.elementAt(i).getId() + " ");

		r_sessions.remove(sessionEvent.getSession());

        log.info("session attributes = " + attributes);

        log.info("---> removing Callback Device feeds");

		for (String deviceName : attributes.keySet()) {
            if (deviceName.startsWith(GenericCallbackDevice.GenericCallbackDeviceIdPrefix)) {
                Object callbackdevice = attributes.get(deviceName);
                try {
                    if (callbackdevice instanceof GenericCallbackDevice) {
                        log.info("---> disposing device feeds : " + deviceName);

                        String qName = deviceName + "_actQueue";

                        Object queue = attributes.get(qName);

                        if (queue instanceof ArrayBlockingQueue) {
                            ((ArrayBlockingQueue)queue).clear();
                        } else {
                            log.info(qName +
                                    " is not ArrayBlockingQueue " + queue.toString());
                        }

                        String listenerName = deviceName + "_actListener";

                        Object listener = attributes.get(listenerName);

                        if (listener instanceof GenericRActionListener) {
                            try {
                                ((GenericCallbackDevice) callbackdevice).removeListener((GenericRActionListener)listener);
                            } catch (RemoteException re) { // we tried our best
                            }

                            UnicastRemoteObject.unexportObject((Remote)listener, true);
                        } else {
                            log.info(listenerName +
                                    " is not GDObjectListenerImpl " + listener.toString());
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
		}

        log.info("---> removing GD Device feeds");
        for (String deviceName : attributes.keySet()) {
            if (deviceName.startsWith("device_")) {
                Object device = attributes.get(deviceName);
                try {
                    if (device instanceof GDDevice) {
                        log.info("---> disposing device feeds : " + deviceName);

                        String qName = deviceName + "_objQueue";

                        Object queue = attributes.get(qName);

                        if (queue instanceof ArrayBlockingQueue) {
                            ((ArrayBlockingQueue)queue).clear();
                        } else {
                            log.info(qName +
                                    " is not ArrayBlockingQueue " + queue.toString());
                        }

                        String listenerName = deviceName + "_objListener";

                        Object listener = attributes.get(listenerName);

                        if (listener instanceof GDObjectListener) {
                            try {
                                ((GDDevice) device).removeGraphicListener((GDObjectListener)listener);
                            } catch (RemoteException re) { // we tried our best
                            }

                            UnicastRemoteObject.unexportObject((Remote)listener, true);
                        } else {
                            log.info(listenerName +
                                    " is not GDObjectListenerImpl " + listener.toString());
                        }
                    }

                } catch (Exception e) {
                    log.error("Error!", e);
                }
            }
        }

		//UserDataDB user    = (UserDataDB) attributes.get("USER");

        final String servername = (String) attributes.get("SERVERNAME");
		final ProjectDataDB project = (ProjectDataDB) attributes.get("PROJECT");
        final boolean save = (Boolean) attributes.get("SAVE");
        final DBLayerInterface dbLayer = (DBLayerInterface)
                sessionEvent.getSession().getServletContext().getAttribute("DBLAYER");

        String logoffmode = (String) attributes.get("LOGOFFMODE");

        if (logoffmode.equals("suspend")) {

            new Thread(new Runnable(){
                public void run(){
                    log.info("updating project status to 'onhold'");

                    try {
                        dbLayer.updateProjectOnHold(project.getIdentifier());
                    } catch (Exception ex) {
                        log.error("Error!", ex);
                    }

                    if (save) {
                        log.info("saving workspace");

                        try {
                            rservices.saveWorkspace(project.getAbsolutePath());
                        } catch (Exception ex) {
                        }
                    }
                }
            }).start();

        } else if (logoffmode.equals("shutdown")) {
            new Thread(new Runnable(){
                public void run(){
                    log.info("updating project status to 'stopped'");

                    try {
                        dbLayer.updateProjectStopped(project.getIdentifier());
                    } catch (Exception ex) {
                        log.error("Error!", ex);
                    }

                    log.info("removing project record");

                    try {
                        // unlink the project
                        dbLayer.setProject(servername, null);
                    } catch(Exception ex){
                    }

                    if (save) {
                        log.info("saving workspace");

                        try {
                            rservices.saveWorkspace(project.getAbsolutePath());
                        } catch(Exception ex){
                        }
                    }

                    try {
                        // mark it unused in case it's in fact dead
                        dbLayer.registerPingFailure(servername);
                    } catch(Exception ex){
                    }

                    log.info("shutting server down");

                    try {
                        rservices.die();
                    } catch(Exception ex){
                    }
                }
            }).start();
        }
	}
}
