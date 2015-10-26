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

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 20, 2010
 * Time: 4:56:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class PoolsSQL extends SqlBase {

    public static String addPoolStatement(PoolDataDB pooldata) {

        return "insert INTO POOL_DATA(POOL_NAME, TIMEOUT, POOL_PREFIXES, ON_DEMAND) values (" +
                wrap(pooldata.getPoolName()) + "," + pooldata.getBorrowTimeout() + "," +
                wrap(pooldata.getPrefixesRaw()) + "," + pooldata.getOnDemand() + ")";
    }

    public static String updatePoolStatement(PoolDataDB pooldata) {

        return "UPDATE POOL_DATA set TIMEOUT=" + pooldata.getBorrowTimeout() + "," +
                " POOL_PREFIXES=" + wrap(pooldata.getPrefixesRaw()) + "," +
                " ON_DEMAND=" + pooldata.getOnDemand() + " where POOL_NAME=" + wrap(pooldata.getPoolName());
    }

    public static String removePoolStatement(String poolname) {
        return "delete from POOL_DATA where POOL_NAME=" + wrap(poolname);
    }


}
