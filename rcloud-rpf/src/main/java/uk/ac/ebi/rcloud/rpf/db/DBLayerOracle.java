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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;


public class DBLayerOracle extends DBLayer {
    final private Logger log = LoggerFactory.getLogger(getClass());
	public DBLayerOracle(ConnectionProvider provider) {
		super(provider);
	}

	protected String sysdateFunctionName() {
		return "CURRENT_TIMESTAMP";
	}

    protected String sysdateSelectRequest() {
   		return "(select " + sysdateFunctionName() + " \"TIMESTAMP\" from dual)";
   	}

	protected void lock(Statement stmt) throws SQLException {
		stmt.execute("LOCK TABLE SERVANTS IN EXCLUSIVE MODE");
	}

	protected void unlock(Statement stmt) throws SQLException {
	}

	@Override
	boolean isNoConnectionError(SQLException sqle) {
        if (sqle instanceof java.sql.SQLRecoverableException) {
            return true;
        }
        return false;
        //log.error("Error!", sqle);
	}

	@Override
	boolean isConstraintViolationError(SQLException sqle) {
		return true;
	}
}
