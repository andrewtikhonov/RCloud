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
package uk.ac.ebi.rcloud.rpf.reg;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.pool.PoolableObjectFactory;
import uk.ac.ebi.rcloud.rpf.ManagedServant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServantsProxyFactoryReg implements PoolableObjectFactory {
    final private static Logger log = LoggerFactory.getLogger(ServantsProxyFactoryReg.class);
    private Vector<ManagedServant> servantsInUse = new Vector<ManagedServant>();
    private String _poolNamingPrefix;
    private Registry _rmiRegistry;

    public ServantsProxyFactoryReg(String registryHost, int registryPort, String poolNamingPrefix) {
        super();
        _poolNamingPrefix = poolNamingPrefix;
        try {
            _rmiRegistry = LocateRegistry.getRegistry(registryHost, registryPort);
            _rmiRegistry.list();
        } catch (Exception ex) {
            log.error("Error!", ex);
        }
    }

    public void activateObject(Object obj) throws Exception {
    }

    public void destroyObject(Object obj) throws Exception {
        servantsInUse.remove(((ManagedServant) obj));
    }

    public Object makeObject() throws Exception {
        String[] servantNames = null;
        try {
            servantNames = _rmiRegistry.list();
        } catch (Exception e) {
            throw new NoSuchElementException("No R Servant available / No RMI Naming Server");
        }
        for (int i = 0; i < servantNames.length; ++i) {
            if (shortRmiName(servantNames[i]).startsWith(_poolNamingPrefix)) {
                try {
                    ManagedServant servant = (ManagedServant) _rmiRegistry.lookup(servantNames[i]);
                    if (servantsInUse.contains(servant)) {
                        continue;
                    }
                    servant.ping();
                    servantsInUse.add(servant);
                    return servant;
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
        }
        throw new NoSuchElementException("No R Servant available");
    }

    public void passivateObject(Object obj) throws Exception {
        ((ManagedServant) obj).reset();
    }

    public boolean validateObject(Object obj) {
        try {
            ((ManagedServant) obj).ping();
            return true;
        } catch (Exception e) {
            log.info("## Validation failed, couldn't ping");
            return false;
        }
    }

    public static String shortRmiName(String fullRmiName) {
        return fullRmiName.substring(fullRmiName.lastIndexOf('/') + 1);
    }
}
