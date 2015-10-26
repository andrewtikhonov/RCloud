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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;


public class MainRegistry {
    final private static Logger log = LoggerFactory.getLogger(MainRegistry.class);

	static Registry registry;
	
	public static final String REGISTRY_MANAGER_NAME="REGMANAGER";

	public static void main(String[] args) throws Exception {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}

		if (System.getProperty("kill") != null && System.getProperty("kill").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME));
			try {
				rk.kill();
			} catch (Exception e) {

			}
		} else if (System.getProperty("show") != null && System.getProperty("show").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			String[] list = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME)).show();
            log.info("+Retrieved from remiregistry on " + new Date() + " : ");
			for (int i = 0; i < list.length; ++i) {
                log.info(list[i]);
			}

		} else if (System.getProperty("unbindall") != null && System.getProperty("unbindall").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME));
			try {
				rk.unbindAll();
			} catch (Exception e) {

			}
		} else if (System.getProperty("invoke") != null && !System.getProperty("invoke").equals("")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME));
			String[] list = registry.list();
			for (int i = 0; i < list.length; ++i) {
				if (!list[i].equalsIgnoreCase(REGISTRY_MANAGER_NAME)) {
					try {
						Remote r = registry.lookup(list[i]);
						r.getClass().getMethod(System.getProperty("invoke")).invoke(r);
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}
		} else {
			registry = LocateRegistry.createRegistry(Integer.decode(System.getProperty("port")));
            log.info("rmiregistry process id : " + PoolUtils.getProcessId());
			registry.rebind(REGISTRY_MANAGER_NAME, new RegistryKillerImpl());
			while (true) {
				Thread.sleep(100);
			}
		}
	}

	interface RegistryKiller extends Remote {
		void kill() throws RemoteException;

		String[] show() throws RemoteException;

		void unbindAll() throws RemoteException;
	}

	static class RegistryKillerImpl extends UnicastRemoteObject implements RegistryKiller {
		public RegistryKillerImpl() throws RemoteException {
			super();
		}

		public void kill() throws RemoteException {
            log.info("rmiregistry is going to die");
			System.exit(0);
		}

		public String[] show() throws RemoteException {
			String[] list = registry.list();
            log.info((new Date()).toString());
			for (int i = 0; i < list.length; ++i) {
                log.info(list[i]);
			}
			return list;
		}

		public void unbindAll() throws RemoteException {
            log.info("unbinding all");
			String[] list = registry.list();
			for (int i = 0; i < list.length; ++i) {
				if (!list[i].equalsIgnoreCase(REGISTRY_MANAGER_NAME)) {
					try {
						registry.unbind(list[i]);
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}
		}
	}

}
