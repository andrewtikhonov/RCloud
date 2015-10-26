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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import uk.ac.ebi.rcloud.rpf.PropertyConst;
import uk.ac.ebi.rcloud.rpf.db.ConnectionProvider;
import uk.ac.ebi.rcloud.rpf.db.DBLayer;
import uk.ac.ebi.rcloud.rpf.db.DBLayerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.rcloud.rpf.PoolUtils.*;

public class ServantProxyPoolSingletonDB {
    final private static Logger log = LoggerFactory.getLogger(ServantProxyFactoryDB.class);
	static java.util.Hashtable<String, GenericObjectPool> _pool = new Hashtable<String, GenericObjectPool>();
	static Integer lock = new Integer(0);

	private static boolean _shuttingDown = false;

    public static class ServerObjectPool extends GenericObjectPool {
        Vector<Object> borrowedObjects = new Vector<Object>();

        class ShutdownHookRunnable implements Runnable {
            public void run() {
                synchronized (ServerObjectPool.this) {

                    final Vector<Object> bo = (Vector<Object>) borrowedObjects.clone();

                    _shuttingDown = true;
                    try {
                        for (int i = 0; i < bo.size(); ++i)
                            ServerObjectPool.this.returnObject(bo.elementAt(i));
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }
        }

        public ServerObjectPool(PoolableObjectFactory factory) {
            super(factory);

            String hookEnabled = System
                    .getProperty(PropertyConst.POOLSSHUTDOWNHOOK);

            if (hookEnabled != null
                    && hookEnabled.equalsIgnoreCase("false")) {
            } else {
                Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookRunnable()));
            }

        }

        @Override
        public synchronized Object borrowObject() throws Exception {
            if (_shuttingDown)
                throw new NoSuchElementException();
            Object result = super.borrowObject();
            borrowedObjects.add(result);
            return result;
        }

        @Override
        public synchronized void returnObject(Object obj) throws Exception {
            super.returnObject(obj);
            borrowedObjects.remove(obj);
        }
    }

	public static GenericObjectPool getInstance(String poolName, String driver,
                                                final String url, final String user, final String password) {

        String key = driver + "%" + poolName + "%" + url + "%" + user + "%" + password;

        //log.info("getInstance-key="+key);
        
        if (_pool.get(key) != null)
			return _pool.get(key);

        synchronized (lock) {
			if (_pool.get(key) == null) {
                try {
					Class.forName(driver);

                    DBLayerInterface dbLayer = DBLayer.getLayer(getDBType(url), new ConnectionProvider() {
						public Connection newConnection() throws SQLException {
							return DriverManager.getConnection(url, user, password);
						}
					});

                    GenericObjectPool p = new ServerObjectPool(new ServantProxyFactoryDB(poolName, dbLayer));

					_pool.put(key, p);
					p.setMaxIdle(0);
                    p.setMaxActive(-1);
                    p.setTestOnBorrow(true);
					p.setTestOnReturn(true);
				} catch (Exception e) {
					throw new RuntimeException(getStackTraceAsString(e));
				}

			}
			return _pool.get(key);
		}
	}

}