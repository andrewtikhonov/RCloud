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
package uk.ac.ebi.rcloud.server.cluster.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.ServantProviderFactory;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.cluster.RClusterInterface;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 10, 2010
 * Time: 1:59:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class RClusterInterfaceImpl implements RClusterInterface {

    final private static Logger log = LoggerFactory.getLogger(RClusterInterfaceImpl.class);

    public Vector<RServices> createRs(int n, String poolName) throws Exception {
        Vector<RServices> workers = null;

        try {
            ServantProviderFactory spFactory = ServantProviderFactory.getFactory();
            for (int i = 0; i < n; ++i) {
                RServices r = null;
                boolean wait = false;

                if (wait) {
                    r = (RServices) spFactory.getServantProvider().borrowServantProxy(poolName);
                } else {
                    r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait(poolName);
                }

                if (r == null) {
                    throw new Exception("not enough number of servants available");
                }
                workers.add(r);
            }

            return workers;
        } catch (Exception e) {
            if (workers.size() > 0) {
                for (int i = 0; i < workers.size(); ++i) {
                    try {
                        ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(workers.elementAt(i));
                    } catch (Exception ex) {
                        log.error("Error!", ex);
                    }
                }
            }
            throw e;
        }
    }

    public void releaseRs(Vector<RServices> rs) throws Exception {
        for (RServices r : rs) {
            try {
                r.die();
            } catch (Exception e) {
                log.error("Error!", e);
            }
        }
    }
}
