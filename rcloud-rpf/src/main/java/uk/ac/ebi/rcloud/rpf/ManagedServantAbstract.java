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

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;
import uk.ac.ebi.rcloud.rpf.util.ServerUtil;


public abstract class ManagedServantAbstract extends java.rmi.server.UnicastRemoteObject implements ManagedServant {

    final private Logger log = LoggerFactory.getLogger(getClass());

	protected String _servantName = null;
	private boolean _resetEnabled = true;
	protected String _jobId = "";
	protected Registry _registry = null;
    protected UserDataDB _owner = null;
    protected String _projectFolder = null;

	public ManagedServantAbstract(String name, String prefix, Registry registry) throws RemoteException {
		this(name, prefix, registry, 0);
	}

	public ManagedServantAbstract(String name, String prefix, Registry registry, int port) throws RemoteException {
		super(port);

		_registry = registry;
		try {
			registry.list();
			log.info("ping registry:ok");
		} catch (ConnectException ce) {
			String message = "can't connect to the naming server, make sure an instance of rmiregistry is running";
			log.info(message);
			throw new RemoteException(message);
		} catch (Exception e) {
            log.error("Error!", e);
		}

        String newname = null;

		if (name == null) {
			while (true) {
				newname = ServerUtil.makeName(prefix, registry);
				try {
                    log.info("Registering " + newname + " in the database.");

					registry.bind(newname, java.rmi.server.RemoteObject.toStub(this));
					break;
				} catch (AlreadyBoundException e) {
                    log.info(newname + " already registered. Trying another name.");
				}
			}
		} else {
            try {
                log.info("Registering " + name + " in the database.");

                registry.bind(name, java.rmi.server.RemoteObject.toStub(this));
            } catch (AlreadyBoundException e) {
                String err = "Error: server " + name + " already registered! Shutting down.";
                log.error(err);

                System.exit(0);
                //throw new RemoteException(err);
            }
		}

        _servantName = name == null ? newname : name;

        // check the instance is in the database
        try {
            log.error("Checking instance is successfully registered.");

            Object o = registry.lookup(_servantName);

            if (o == null) {
                log.error(_servantName + " instance not registered.");
                throw new RemoteException(_servantName + " instance not registered");
            }

        } catch (Exception ex) {
            log.error("Error!", ex);
            throw new RemoteException("");
        }


		final Registry reg = registry;

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
                //log.info("ManagedServantAbstract.addShutdownHook");

                try {
                    //log.info("ManagedServantAbstract-unbinding from DB..");
                    reg.unbind(_servantName);
                } catch (Exception e) {
                    //log.error("Error!", e);
                    //log.info("unbind error");
                    //e.printStackTrace();
                }

                /*
                //log.info("ManagedServantAbstract-Shutting Down");

                System.err.println("Shutdown Hook 1");
                

				try {
                    //log.info("ManagedServantAbstract-unregistering RMI..");
                    UnicastRemoteObject.unexportObject(ManagedServantAbstract.this, true);
				} catch (Exception e) {
                    //log.error("Error!", e);
                    log.info("unexportObject error");
                    e.printStackTrace();

				}

                //log.info("ManagedServantAbstract-done...");
                */
			}
		}));
	}


    //
    // INTERFACE
    //
    //


    public String ping() throws java.rmi.RemoteException {
        return "pong";

    }

    public void die() throws java.rmi.RemoteException {

    }

	public String getServantName() throws RemoteException {
		return _servantName;
	}

    public String getLogs() throws java.rmi.RemoteException {
        throw new RemoteException("get logs not supported");
    }


    public void addLogListener(RemoteLogListener listener) throws RemoteException {
		RemoteAppender.addLogListener(listener);
	}

	public void removeLogListener(RemoteLogListener listener) throws RemoteException {
		RemoteAppender.removeLogListener(listener);
	}

	public void removeAllLogListeners() throws RemoteException {
		RemoteAppender.removeAllLogListeners();
	}

	public void logInfo(String message) throws RemoteException {
		log.info(message);
	}

	public boolean isResetEnabled() throws RemoteException {
		return _resetEnabled;
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
		_resetEnabled = enable;
	}

    public void reset() throws java.rmi.RemoteException {
        throw new RemoteException("reset not supported");
    }

	public boolean hasConsoleMode() throws RemoteException {
		return false;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		throw new RemoteException("console mode not supported");
	}

    public String consoleSubmit(String cmd, HashMap<String, Object> attributes) throws RemoteException {
        throw new RemoteException("console mode not supported");
    }

    public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
        throw new RemoteException("console mode not supported");
    }

    public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> attributes) throws RemoteException {
        throw new RemoteException("console mode not supported");
    }

	public boolean hasPushPopMode() throws RemoteException {
		return false;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
		throw new RemoteException("push/pop mode not supported");
	}

	public Serializable pop(String symbol) throws RemoteException {
		throw new RemoteException("push/pop mode not supported");
	}

	public String[] listSymbols() throws java.rmi.RemoteException {
		throw new RemoteException("push/pop mode not supported");
	}

	public boolean hasGraphicMode() throws RemoteException {
		return false;
	}

	public void startGraphicSession() throws RemoteException {
		throw new RemoteException("graphic mode not supported");
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		throw new RemoteException("graphic mode not supported");
	}

	public void endGraphicSession() throws RemoteException {
		throw new RemoteException("graphic mode not supported");

	}

	public String getProcessId() throws RemoteException {
		return PoolUtils.getProcessId();
	}

	public String getHostIp() throws RemoteException {
		return PoolUtils.getHostIp();
	}

    public String getHostname() throws RemoteException {
        return PoolUtils.getHostName();
    }

	public String getJobId() throws RemoteException {
		return _jobId;
	}

	public void setJobId(String jobId) throws RemoteException {
		_jobId = jobId;
	}

    public void setProject(String projectFolder) throws RemoteException{
        _projectFolder = projectFolder;
    }

    public String getProject() throws RemoteException{
        return _projectFolder;
    }

	public boolean isBusy() throws RemoteException {
		return false;
	}

	public String getStub() throws RemoteException {
		return PoolUtils.stubToHex(this);
	}

	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
		try {
			Registry registry = ServerDefaults.getRegistry(namingRegistryProperties);
			if (autoName) {
				String newname = null;
				while (true) {
					newname = ServerUtil.makeName(prefixOrName, registry);
					try {
						registry.bind(newname, java.rmi.server.RemoteObject.toStub(this));
						break;
					} catch (AlreadyBoundException e) {
					}
				}
				return newname;
			} else {
				registry.rebind(prefixOrName, java.rmi.server.RemoteObject.toStub(this));
				return prefixOrName;
			}
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

	public String toString() {
		return super.toString() + " " + _servantName;
	}

    public String getProperty(String name) throws RemoteException {
        return System.getProperty(name);
    }

    public Hashtable<Object, Object> getProperties() throws RemoteException {
        return System.getProperties();
    }

    public void setProperty(String name, String value) throws RemoteException {
        System.setProperty(name, value);
    }

}
