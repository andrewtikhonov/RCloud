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

import uk.ac.ebi.rcloud.rpf.db.data.MonitorDataDB;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 19/05/2011
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
public class MonitorsSQL extends SqlBase {

    private static final String UPDATE = "update MONITORS set ";

    public static String addMonitorStatement(MonitorDataDB monitor, String sysdate) {

        return "INSERT INTO MONITORS (NAME, STUB_HEX, PING_FAILURES, REGISTER_TIME, PROCESS_ID, HOST_NAME, HOST_IP, OS) "
                            + "VALUES ("
                            + wrap(monitor.getName())    + ","
                            + wrap(monitor.getStubhex()) + ","
                            + "0,"
                            + sysdate + "," // sysdateFunctionName()
                            + wrap(monitor.getProcessId()) + ","
                            + wrap(monitor.getHostName())  + ","
                            + wrap(monitor.getHostIp())    + ","
                            + wrap(monitor.getOsName())    + ")";

    }

    // ServerDataDB server
    public static String checkExistsStatement(String name) {
        return "select count(*) from MONITORS where NAME=" + wrap(name);
    }

    public static String deleteMonitorStatement(String name) {
        return "DELETE FROM MONITORS WHERE NAME=" + wrap(name);
    }

    public static String pingFailureStatement(String name) {
        return UPDATE + "PING_FAILURES=(PING_FAILURES+1) WHERE NAME=" + wrap(name);
    }

}

