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

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 6, 2009
 * Time: 5:13:55 PM
 * To change this template use File | Settings | File Templates.
 */

public abstract class ServantProviderFactory {

    final private static Logger log = LoggerFactory.getLogger(ServantProviderFactory.class);

	static ServantProviderFactory _defaultFacory;
    
	synchronized public static void init() {
		if (_defaultFacory != null)
			return;
		PoolUtils.injectSystemProperties(true);
		PoolUtils.initRmiSocketFactory();
		if (System.getProperty("pools.provider.factory") != null && !System.getProperty("pools.provider.factory").equals("")) {
			try {
				_defaultFacory = (ServantProviderFactory) Class.forName(System.getProperty("pools.provider.factory")).newInstance();
			} catch (Exception e) {
                log.error("Error!", e);
			}

		} else {
			try {
				_defaultFacory = (ServantProviderFactory) Class.forName("uk.ac.ebi.rcloud.rpf.reg.ServantProviderFactoryReg").newInstance();
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}

	// static { init(); }

	public static ServantProviderFactory getFactory() {
		if (_defaultFacory == null)
			init();
		return _defaultFacory;
	}

	public abstract ServantProvider getServantProvider();

}
