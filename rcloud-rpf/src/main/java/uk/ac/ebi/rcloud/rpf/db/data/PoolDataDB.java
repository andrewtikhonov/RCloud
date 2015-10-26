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
import java.util.StringTokenizer;
import java.util.Vector;
import uk.ac.ebi.rcloud.rpf.PoolUtils;


/**
 * @author Andrew Tikhonov
 */
public class PoolDataDB implements Serializable{

    private static final long serialVersionUID = 2L;
    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String POOL_NAME  = "POOL_NAME";
    public static String TIMEOUT    = "TIMEOUT";
    public static String POOL_PREFIXES = "POOL_PREFIXES";
    public static String ON_DEMAND = "ON_DEMAND";

    public PoolDataDB( HashMap<String, Object> options ) {
        map = options;
    }

	public int getBorrowTimeout() {
        return (Integer) map.get(TIMEOUT);
	}

	public int getOnDemand() {
        return (Integer) map.get(ON_DEMAND);
	}

	public String getPoolName() {
        return (String) map.get(POOL_NAME);
	}

	public String[] getPrefixes() {
        return getPrefixes((String) map.get(POOL_PREFIXES));
	}

	public String getPrefixesRaw() {
        return (String) map.get(POOL_PREFIXES);
	}

    public HashMap<String, Object> getMap() {
        return map;
    }

	public String toString() {
		return "PoolData[name=" + getPoolName() + " prefixes=" +
                PoolUtils.flatArray(getPrefixes()) + " bto=" + getBorrowTimeout() + " ondemand=" + getOnDemand() + "]";
	}

	public static String[] getPrefixes(String prefixes) {
		StringTokenizer st = new StringTokenizer(prefixes, ",");
		Vector<String> pv = new Vector<String>();
		while (st.hasMoreElements())
			pv.add((String) st.nextElement());
		return pv.toArray(new String[0]);
	}

}
