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

import uk.ac.ebi.rcloud.rpf.PoolUtils;
import uk.ac.ebi.rcloud.rpf.RPFSessionInfo;
import uk.ac.ebi.rcloud.rpf.db.data.ServerDataDB;
import uk.ac.ebi.rcloud.util.HexUtil;

import static uk.ac.ebi.rcloud.rpf.PoolUtils.getHostIp;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.getHostName;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.getProcessId;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 20, 2010
 * Time: 2:58:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServersSQL extends SqlBase {

    private static final String UPDATE = "update SERVANTS set ";

    public static String insertServerStatement(ServerDataDB server, String sysdate) {

        String statement = "INSERT INTO SERVANTS (NAME,STUB_HEX,IN_USE,PING_FAILURES," +
                "REGISTER_TIME,PROCESS_ID,HOST_NAME,HOST_IP,OS,CODEBASE,JOB_ID,JOB_NAME,NOTIFY_EMAIL,NOTIFIED,OWNER,PROJECT) "
                            + "VALUES ("
                            + wrap(server.getName())    + ","
                            + wrap(server.getStubhex()) + ","
                            + "0,0,"
                            + sysdate + "," // sysdateFunctionName()
                            + wrap(server.getProcessId()) + ","
                            + wrap(server.getHostName())  + ","
                            + wrap(server.getHostIp())    + ","
                            + wrap(server.getOsName())    + ","
                            + sqlString(server.getCodeBase() , "NULL") + ","
                            + sqlString(server.getJobId()    , "NULL") + ","
                            + sqlString(server.getJobName()  , "NULL") + ","
                            + sqlString(server.getEmail()    , "NULL") + ","
                            + "0,"
                            + sqlString(server.getOwner()    , "NULL") + ","
                            + sqlString(server.getProject()  , "NULL") + ")";

        return statement;
    }

    // ServerDataDB server
    public static String checkExistsStatement(String name) {
        return "select count(*) from SERVANTS where NAME=" + wrap(name);
    }

    public static String deleteServerStatement(String name) {
        return "DELETE FROM SERVANTS WHERE NAME=" + wrap(name);
    }

    public static String selectAllNamesStatement() {
        return "select NAME from SERVANTS";
    }

    public static String selectAllNamesStatement(String[] prefixes) {
        String statement = "select NAME from SERVANTS where IN_USE=0 AND PING_FAILURES<" +
                PoolUtils.PING_FAILURES_NBR_MAX + " AND (NAME like " + wrap( prefixes[0] + "%" );

        for (int i = 1; i < prefixes.length; ++i) {
            statement += " OR NAME like " + wrap( prefixes[i] + "%" );
        }

        statement += ")";

        return statement;
    }

    public static String lookupStatement(String name) {
        return "select STUB_HEX,CODEBASE from SERVANTS where NAME=" + wrap( name );
    }

    public static String pingFailureStatement(String name) {
        return UPDATE + "PING_FAILURES=(PING_FAILURES + 1) WHERE NAME=" + wrap(name);
    }

    public static String releaseFreeResourcesOutOfLifespanStatement(String sysdate, int hours) {
        return UPDATE + "PING_FAILURES = (PING_FAILURES + 1) WHERE IN_USE = 0 and (REGISTER_TIME < " +
                sysdate + " - interval '" + hours + "' hour)";
    }

    public static String unreserveStatement(String name, String sysdate) {
        return UPDATE + "IN_USE=0  " +
                ",RETURN_TIME=" + sysdate + ",RETURN_HOST_NAME=" + wrap(getHostName()) +
                ",RETURN_HOST_IP=" + wrap(getHostIp()) + ",RETURN_PROCESS_ID=" + wrap(getProcessId()) +
                " WHERE NAME=" + wrap(name);
    }

    public static String reserveStatement(String name, String sysdate) {
        return UPDATE + "IN_USE=1, BORROW_TIME=" + sysdate +
                ",BORROW_HOST_NAME=" + wrap(getHostName()) +
                ",BORROW_HOST_IP=" + wrap(getHostIp()) +
                ",BORROW_PROCESS_ID=" + wrap(getProcessId()) +
                ",BORROW_SESSION_INFO_HEX=" +
                    (RPFSessionInfo.get() == null ? "NULL" : wrap(HexUtil.objectToHex(RPFSessionInfo.get()) )) +
                ",RETURN_TIME=NULL" + ",RETURN_HOST_NAME=NULL" +
                ",RETURN_HOST_IP=NULL" + ",RETURN_PROCESS_ID=NULL" +
                " WHERE NAME=" + wrap(name);
    }

    public static String updateNodenameStatement(String servername, String nodename) {
        return UPDATE + "NODE_NAME=" + wrap(nodename) + " WHERE NAME=" + wrap(servername);
    }

    public static String updateAttributesStatement(String servername, String attributes) {
        return UPDATE + "ATTRIBUTES_HEX=" + wrap(attributes) + " WHERE NAME=" + wrap(servername);
    }

    public static String nameFromStubhexStatement(String stub_hex) {
        return "select NAME from SERVANTS where STUB_HEX=" + wrap(stub_hex);
    }

    public static String updateJobIDStatement(String servername, String jobID) {
        return UPDATE + "JOB_ID=" + wrap(jobID) + " WHERE NAME=" + wrap(servername);
    }

    public static String updateOwnerStatement(String servername, String owner) {
        return UPDATE + "OWNER=" + sqlString(owner, "NULL") + " WHERE NAME=" + wrap(servername);
    }

    public static String updateProjectStatement(String servername, String project) {
        return UPDATE + "PROJECT=" + sqlString(project,"NULL") + " WHERE NAME=" + wrap(servername);
    }

    public static String updateMasterStatement(String servername, String mastername) {
        return UPDATE + "MASTER=" + sqlString(mastername, "NULL") + " WHERE NAME=" + wrap(servername);
    }

    public static String incrementNotificationStatement(String servername) {
        return UPDATE + "NOTIFIED=(NOTIFIED + 1) WHERE NAME=" + wrap(servername);
    }

}
