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
import uk.ac.ebi.rcloud.rpf.PropertyConst;
import uk.ac.ebi.rcloud.rpf.ServantProviderFactory;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.cluster.RSnowClusterInterface;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 10, 2010
 * Time: 12:44:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSnowClusterInterfaceImpl implements RSnowClusterInterface {

    final private static Logger log = LoggerFactory.getLogger(RSnowClusterInterfaceImpl.class);

    private ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

    public RSnowClusterInterfaceImpl(){
        System.setProperty(PropertyConst.POOLSKILLUSED, "true");
    }

    public RServices createR(String poolName) throws Exception {
        boolean wait = false;

        try {
            RServices r = null;

            if (wait) {
                r = (RServices) spFactory.getServantProvider().borrowServantProxy(poolName);
            } else {
                r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait(poolName);
            }

            if (r == null) {
                throw new RuntimeException("no r server available");
            }

            return r;
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception ex) {
            log.error("Error!",ex);
            throw new Exception("problem obtaining r server");
        }
    }

    public void releaseR(RServices r) throws Exception {
        try {
            spFactory.getServantProvider().returnServantProxy(r);

            // dont wait
            // for the audit
            r.die();
        } catch (Exception e) {
            log.error("Error!", e);
            throw new Exception("problem releasing r server");
        }
    }
}
