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
package uk.ac.ebi.rcloud.rpf;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import uk.ac.ebi.rcloud.rpf.db.DBLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.data.NodeDataDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.db.sql.SqlBase;
import uk.ac.ebi.rcloud.util.HexUtil;

public class MainServer {
    final private static Logger log = LoggerFactory.getLogger(MainServer.class);

    private static String _mainServantClassName = System.getProperty(PropertyConst.SERVANTCLASS);

	private static Class<?> mainServantClass = null;

	private static Registry rmiRegistry = null;

	private static String servantName = null;

	private static ManagedServant mservant = null;

	public static ServantCreationListener servantCreationListener = null;

	public static void main(String[] args) throws Exception {

//		PoolUtils.initLog4J();
		PoolUtils.ensurePublicIPIsUsedForRMI();
		PoolUtils.noGui();
		
		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new YesSecurityManager());
			}

            String nodename = System.getProperty(PropertyConst.NODENAME);

			boolean isNodeProvided = nodename != null && nodename.length() > 0;

            if (!isNodeProvided) {
                log.info("Proprty 'node' is not set");
                return;
            } else {
                NodeDataDB nodeData = null;
                try {
                    rmiRegistry = ServerDefaults.getRmiRegistry();

                    nodeData = ((DBLayerInterface) rmiRegistry).
                            getNodeData("NODE_NAME=" + SqlBase.wrap(nodename)).elementAt(0);

                } catch (Exception e) {
                    log.info("Couldn't retrieve Node Info for node <" + nodename + ">");
                    log.error("Error!", e);
                    return;
                }
                System.setProperty(PropertyConst.NODENAME , "true");

                ServerDefaults._servantPoolPrefix = nodeData.getPoolPrefix();

                log.info("nodedata:" + nodeData);
            }

            String autoname = System.getProperty(PropertyConst.AUTONAME);

			if (autoname != null && autoname.equalsIgnoreCase("true")) {
				log.info("Instantiating " + _mainServantClassName +
                        " with autonaming, prefix " + ServerDefaults._servantPoolPrefix);

                servantName = null;
			} else {
				// no autonaming, check the name here
                String name = System.getProperty(PropertyConst.SERVERNAME);
                String jobid = System.getProperty(PropertyConst.JOBID);

				if (name != null && name.length() > 0) {
					servantName = name;
				} else {
                    if (jobid != null && jobid.length() > 0) {
                        servantName = jobid;
                    }
                }

				log.info("Instantiating " + _mainServantClassName + " with name " + servantName +
                        " , prefix " + ServerDefaults._servantPoolPrefix);
			}

			if (rmiRegistry == null)
				rmiRegistry = ServerDefaults.getRmiRegistry();

            //
            //
            //System.setProperty(PropertyConst.CODEBASE, "");
            //log.info("### code base:" + System.getProperty(PropertyConst.CODEBASE));
			//ClassLoader cl = new URLClassLoader(PoolUtils.getURLS(System.getProperty(PropertyConst.CODEBASE)),
            //        MainServer.class.getClassLoader());

			ClassLoader cl = new URLClassLoader(new URL[]{}, MainServer.class.getClassLoader());

            Thread.currentThread().setContextClassLoader(cl);

			mainServantClass = cl.loadClass(_mainServantClassName);

			boolean isPrivateServant = !isNodeProvided && ( (System.getProperty("private") != null &&
                    System.getProperty("private").equalsIgnoreCase("true")) );

			String servantCreationListenerStub = System.getProperty(PropertyConst.LISTENERSTUB);

            if (servantCreationListenerStub != null && !servantCreationListenerStub.equals("")) {
				servantCreationListener = (ServantCreationListener) HexUtil.hexToObject(servantCreationListenerStub);
			}

			if (!isPrivateServant) {
				mservant = (ManagedServant) mainServantClass
                        .getConstructor(new Class[] { String.class, String.class, Registry.class })
                        .newInstance(new Object[] { servantName, ServerDefaults._servantPoolPrefix, rmiRegistry });
			} else {
				mservant = (ManagedServant) mainServantClass
                        .getConstructor(new Class[] { String.class, String.class, Registry.class })
                        .newInstance(new Object[] { null, "PRIVATE_", rmiRegistry });
			}

			//log.info("clone:"+mservant.cloneServer());
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, mservant, null);
			}

            //
            //
			String sname = mservant.getServantName();

			log.info("sname ::: " + sname);

			if (rmiRegistry instanceof DBLayerInterface) {

				if (isNodeProvided) {
					((DBLayerInterface) rmiRegistry).updateServantNodeName(sname, nodename);

				} else {

					Vector<NodeDataDB> nodes = ((DBLayerInterface) rmiRegistry).getNodeData("");
					for (int i = 0; i < nodes.size(); ++i) {
						String nodeName = nodes.elementAt(i).getNodeName();
						String nodeIp = nodes.elementAt(i).getHostIp();
						String nodePrefix = nodes.elementAt(i).getPoolPrefix();
						if (sname.startsWith(nodePrefix) && nodeIp.equals(PoolUtils.getHostIp())) {
							((DBLayerInterface) rmiRegistry).updateServantNodeName(sname, nodeName);
							break;
						}
					}
				}

				HashMap<String, Object> attributes = new HashMap<String, Object>();

                Enumeration<Object> sysPropKeys = (Enumeration<Object>) System.getProperties().keys();

				while (sysPropKeys.hasMoreElements()) {
					String key = (String) sysPropKeys.nextElement();
					if (key.startsWith("attr.")) {
						attributes.put(key, System.getProperty(key));
					}
				}

				((DBLayerInterface) rmiRegistry).updateServantAttributes(sname, attributes);

                //
                //
                //

			}

			//log.info("*************************$$$$$$$$$$$$");
			log.info("Server " + sname + " instantiated successfully.");

		} catch (InvocationTargetException ite) {
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, null, new RemoteException("", ite.getTargetException()));
			}
			throw new Exception(PoolUtils.getStackTraceAsString(ite.getTargetException()));

		} catch (Exception e) {

			log.info("----------------------");
			log.info(PoolUtils.getStackTraceAsString(e));
            log.error("Error!", e);
			log.error("Error!", e);

			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, null, new RemoteException("", e));
			}

			System.exit(1);
		}
	}
}
