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
package uk.ac.ebi.rcloud.rpf.db.sql;

import uk.ac.ebi.rcloud.rpf.db.data.PoolDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.SiteDataDB;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 15/11/2011
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class SiteSQL extends SqlBase {

    public static String siteExistsStatement(String sitename) {
        return "select count(*) from SITE_DATA where SITE_NAME=" + wrap(sitename);
    }

    public static String addSiteStatement(SiteDataDB sitedata) {

        return "insert INTO SITE_DATA (SITE_NAME, CLUSTER_POOL_MAP) values (" +
                wrap(sitedata.getSiteName()) + "," + sitedata.getClusterPoolMap() + ")";
    }

    public static String updateSiteStatement(SiteDataDB sitedata) {

        return "UPDATE SITE_DATA set CLUSTER_POOL_MAP=" + sitedata.getClusterPoolMap() +
                " where SITE_NAME = " + wrap(sitedata.getSiteName());
    }

    public static String deleteSiteStatement(String sitename) {
        return "delete from SITE_DATA where SITE_NAME = " + wrap(sitename);
    }

}
