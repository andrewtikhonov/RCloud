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
package uk.ac.ebi.rcloud.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.db.DBLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.data.OptionDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.SiteDataDB;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 21, 2010
 * Time: 3:34:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class SiteOptions {
    // static
    //

    // log
    private static final Logger log = LoggerFactory.getLogger(SiteOptions.class);


    // singleton
    private static final Integer singletonLock = new Integer(0);
    private static HashMap<String, SiteOptions> instances = new HashMap<String, SiteOptions>();

    //
    private static DBLayerInterface dbLayer = null;

    // per instance
    //
    private String sitename = null;
    private SiteDataDB sitedata = null;

    public static SiteOptions getInstance(String sitename) {
        if (instances.containsKey(sitename))
            return instances.get(sitename);

        synchronized (singletonLock) {
            if (instances.containsKey(sitename))
                return instances.get(sitename);

            instances.put(sitename, new SiteOptions(sitename));

            return instances.get(sitename);
        }
    }

    private SiteOptions(String sitename) {
        this.sitename = sitename;
    }

    public SiteDataDB getSiteData() {
        return sitedata;
    }

    public void initDbLayer(DBLayerInterface dbLayer) {
        this.dbLayer = dbLayer;
        refresh();
    }

    public void refresh() {
        try {
            sitedata = dbLayer.getSite(sitename);
        }
        catch (Exception ex) {
            log.error("Error!", ex);
        }
    }

}
