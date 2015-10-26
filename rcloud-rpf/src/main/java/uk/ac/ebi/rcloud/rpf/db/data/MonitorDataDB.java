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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 19/05/2011
 * Time: 19:46
 * To change this template use File | Settings | File Templates.
 */
public class MonitorDataDB implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(MonitorDataDB.class);
    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String NAME            = "NAME";
    public static String STUB_HEX        = "STUB_HEX";
    public static String PING_FAILURES   = "PING_FAILURES";
    public static String REGISTER_TIME   = "REGISTER_TIME";
    public static String PROCESS_ID      = "PROCESS_ID";
    public static String HOST_NAME       = "HOST_NAME";
    public static String HOST_IP         = "HOST_IP";
    public static String OS              = "OS";

    public MonitorDataDB(HashMap<String, Object> options) {
        map = options;
    }

    public String getName() {
        return (String) map.get(NAME);
    }

    public String getStubhex() {
        return (String) map.get(STUB_HEX);
    }

    public Integer getPingFailures() {
        return ((Integer) map.get(PING_FAILURES));
    }

    public Timestamp getRegisterTime() {
        return (Timestamp) map.get(REGISTER_TIME);
    }

    public String getProcessId() {
        return (String) map.get(PROCESS_ID);
    }

    public String getHostName() {
        return (String) map.get(HOST_NAME);
    }

    public String getHostIp() {
        return (String) map.get(HOST_IP);
    }

    public String getOsName() {
        return (String) map.get(OS);
    }

    public boolean isEmpty()
    {
        return (map == null || map.isEmpty());
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

	public String toString() {
		return "Name=" + getName();
	}

}
