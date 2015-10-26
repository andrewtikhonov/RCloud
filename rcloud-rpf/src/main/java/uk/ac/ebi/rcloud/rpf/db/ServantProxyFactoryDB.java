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
package uk.ac.ebi.rcloud.rpf.db;

import java.rmi.RemoteException;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.pool.PoolableObjectFactory;
import uk.ac.ebi.rcloud.rpf.*;
import uk.ac.ebi.rcloud.rpf.exception.LookUpTimeout;
import uk.ac.ebi.rcloud.rpf.db.data.PoolDataDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.exception.InitializingException;
import uk.ac.ebi.rcloud.rpf.db.data.ServerDataDB;


public class ServantProxyFactoryDB implements PoolableObjectFactory {
    final private static Logger log = LoggerFactory.getLogger(ServantProxyFactoryDB.class);

	DBLayerInterface _dbLayer = null;
	String _poolName = null;
	PoolDataDB _poolData = null;

	public ServantProxyFactoryDB(String poolName, DBLayerInterface dbLayer) throws Exception {
		super();
		_poolName = poolName;
		_dbLayer = dbLayer;
		_poolData = _dbLayer.getPoolDataHashMap().get(poolName);
	}

    private boolean killUsed() {
        String killusedprop = System.getProperty(PropertyConst.POOLSKILLUSED);
        return (killusedprop != null && killusedprop.equalsIgnoreCase("true"));
    }

	public void activateObject(Object obj) throws Exception {
	    //log.info("activateObject");
    }

	public void destroyObject(Object obj) throws Exception {
        //log.info("destroyObject");

		_dbLayer.lock();
		try {
			String servantName = _dbLayer.getNameFromStub((ManagedServant) obj);
			_dbLayer.unReserve(servantName);

			if (killUsed()) {
                log.info("killused");
    			_dbLayer.registerPingFailure(servantName);
			}
		} finally {
			_dbLayer.unlock();
			_dbLayer.commit();
		}
	}

	public Object makeObject() throws Exception {
        //log.info("makeObject");

        if (_poolData.getOnDemand() > 0) {

            //
            //
            Exception ex0 = null;

            /*
            boolean doit = true;

            while(doit) {
                log.info("server launch");

                ManagedServant server = ServerManagerUtil.
                        createServer(_poolData.getPoolName()); // pool name instead
                                                               // of node name, potentially
                                                               // a bug, change asap

                log.info("server created");

                String servername = server.getServantName();

                log.info("server name " + servername);

                int MAX_INIT_TIMEOUT = 20 * 1000;
                int INIT_SLEEP_DELAY = 100;

                for(int i = 0; i < MAX_INIT_TIMEOUT;i+=INIT_SLEEP_DELAY) {
                    try {
                        log.info("server verification");
                        PoolUtils.ping(server);
                        try {
                            _dbLayer.lock();
                            _dbLayer.reserve(servername);
                            log.info("server reserved " + servername);
                            return server;
                        } finally {
                            _dbLayer.unlock();
                            _dbLayer.commit();
                        }

                    } catch (RemoteException re) {
                        if (re.getCause() instanceof InitializingException) {
                            // sleep
                            Thread.sleep(INIT_SLEEP_DELAY);
                        } else {
                            _dbLayer.registerPingFailure(servername);
                            ex0 = (Exception) re.getCause();
                            //throw new NoSuchElementException(re.getCause().getMessage());
                            break;
                        }
                    }
                }
            }
            */

            //throw new NoSuchElementException("Cannot Create Server: " + ex0.getMessage());
            throw new NoSuchElementException("Cannot Create Server: " + "method unsupported");

        } else {

            //
            //

            Vector<String> servantNames = new Vector<String>();

            try {
                servantNames.addAll(_dbLayer.list(_poolData.getPrefixes()));
                // log.info("servant Names : " + servantNames);
            } catch (Exception e) {
                log.error("Error!", e);
                throw new NoSuchElementException("No R Servant available / No DB ");
            }

            Vector<Integer> order = null;
            if (servantNames.size() > 0)
                order = PoolUtils.getRandomOrder(servantNames.size());

            for (int i = 0; i < servantNames.size(); ++i) {
                String name = servantNames.elementAt(order.elementAt(i));

                try {
                    ManagedServant servant = (ManagedServant) _dbLayer.lookup(name);

                    log.info("checking server " + name);
                    PoolUtils.ping(servant);
                    try {
                        _dbLayer.lock();
                        ServerDataDB server = _dbLayer.getServerRecord(name);
                        if (server != null) {
                            if (server.getInUse() == 0) {
                                _dbLayer.reserve(name);

                                log.info("reserved " + name);
                                return servant;
                            } else {
                                log.info("server " + name + " is in use");
                            }
                        } else {
                            log.error("server " + name + " is a SPECTRE");
                        }
                    } finally {
                        _dbLayer.unlock();
                        _dbLayer.commit();
                    }
                } catch (LookUpTimeout e) {
                    _dbLayer.registerPingFailure(name);
                } catch (RemoteException re) {
                    if (re.getCause() instanceof InitializingException) {
                    } else {
                        _dbLayer.registerPingFailure(name);
                    }
                }
            }

            throw new NoSuchElementException("No Servant available");
        }

	}

	public void passivateObject(Object obj) throws Exception {
        //log.info("passivateObject");

        if (killUsed()) {
        } else {
            PoolUtils.reset((ManagedServant) obj);
        }
	}

	public boolean validateObject(Object obj) {
        //log.info("validateObject");

		try {
			PoolUtils.ping((ManagedServant) obj);
			return true;
		} catch (RemoteException re) {

			if (re.getCause() instanceof InitializingException) {
			} else {
				try {

					_dbLayer.lock();
					try {
						String servantName = _dbLayer.getNameFromStub((ManagedServant) obj);
						_dbLayer.registerPingFailure(servantName);
						_dbLayer.unReserve(servantName);
					} finally {
						_dbLayer.unlock();
						_dbLayer.commit();
					}

				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}

			log.info("## Validation failed, couldn't ping");
			return false;
		}
	}

}