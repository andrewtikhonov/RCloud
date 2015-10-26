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
package uk.ac.ebi.rcloud.rpf.db.data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 15/11/2011
 * Time: 12:17
 * To change this template use File | Settings | File Templates.
 */

public class SiteDataDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String SITE_NAME          = "SITE_NAME";
    public static String CLUSTER_POOL_MAP   = "CLUSTER_POOL_MAP";

    public SiteDataDB( HashMap<String, Object> options ) {
        map = options;
    }

    public String getSiteName() {
        return (String) map.get(SITE_NAME);
    }

    public String getClusterPoolMap() {
        return (String) map.get(CLUSTER_POOL_MAP);
    }

	public String toString() {
		return "Name=" + getSiteName() + " cluster map=" + getClusterPoolMap();
	}

}
