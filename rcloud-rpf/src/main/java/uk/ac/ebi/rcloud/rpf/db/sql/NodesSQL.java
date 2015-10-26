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

import uk.ac.ebi.rcloud.rpf.db.data.NodeDataDB;
import uk.ac.ebi.rcloud.util.DETools;

import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 20, 2010
 * Time: 5:03:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodesSQL extends SqlBase {

    public static String addNodeStatement(NodeDataDB nodedata) {

        String statement = "INSERT INTO NODE_DATA(NODE_NAME, HOST_IP, HOST_NAME, LOGIN,"
                +"PWD, INSTALL_DIR, CREATE_SERVANT_COMMAND, KILL_SERVANT_COMMAND, LIST_COMMAND, SETUP_PROFILE_COMMAND,"
                +"OS, SERVANT_NBR_MIN, SERVANT_NBR_MAX, POOL_PREFIX, PROCESS_COUNTER, STEP, THRESHOLD) VALUES ("
                + wrap( nodedata.getNodeName() )  + ","
                + wrap( nodedata.getHostIp() )    + ","
                + wrap( nodedata.getHostName() )  + ","
                + wrap( nodedata.getLogin() )     + ","
                + wrap( nodedata.getPwd().trim().equals("") ? "" : DETools.cipherString(nodedata.getPwd())) + ","
                + wrap( nodedata.getInstallDir() ) + ","
                + wrap( nodedata.getCreateServantCommand() ) + ","
                + wrap( nodedata.getKillServantCommand() ) + ","
                + wrap( nodedata.getListCommand() ) + ","
                + wrap( nodedata.getSetupProfileCommand() ) + ","
                + wrap( nodedata.getOS() ) + ","
                + nodedata.getServantNbrMin() + ","
                + nodedata.getServantNbrMax() + ","
                + wrap( nodedata.getPoolPrefix() ) + ","
                + "0," +
                + nodedata.getStep() + ","
                + nodedata.getThreshold()
                + ")";

        return statement;
    }

    public static String removeNodeStatement(String nodename) {
        return "delete from NODE_DATA where NODE_NAME=" + wrap(nodename);
    }

    public static String incrementNodePCStatement(String nodename) {
        return "update NODE_DATA set PROCESS_COUNTER=(PROCESS_COUNTER+1) WHERE NODE_NAME=" + wrap(nodename);
    }

    public static String updateNodeStatement(NodeDataDB nodedata) {

        String ip = nodedata.getHostIp().trim();
        String host = nodedata.getHostName().trim();
        String prefix = nodedata.getPoolPrefix().trim();
        String nodeName = nodedata.getNodeName();

        if (ip.equals("")) {
            try {
                ip = InetAddress.getByName(nodedata.getHostName()).getHostAddress();
            } catch (Exception e) {
            }
        }

        String password = (nodedata.getPwd().trim().equals("") ? "" : DETools.cipherString(nodedata.getPwd()));

        return "UPDATE NODE_DATA set " +
                " HOST_IP=" + wrap(ip) + "," +
                " HOST_NAME=" + wrap(host) + "," +
                " POOL_PREFIX=" + wrap(prefix) + "," +
                " LOGIN=" + wrap(nodedata.getLogin()) + "," +
                " PWD=" + wrap(password) + "," +
                " INSTALL_DIR=" + wrap(nodedata.getInstallDir()) + "," +
                " CREATE_SERVANT_COMMAND=" + wrap(nodedata.getCreateServantCommand()) + "," +
                " KILL_SERVANT_COMMAND=" + wrap(nodedata.getKillServantCommand()) + "," +
                " OS=" + wrap(nodedata.getOS()) + "," +
                " SERVANT_NBR_MIN=" + nodedata.getServantNbrMin() + "," +
                " SERVANT_NBR_MAX=" + nodedata.getServantNbrMax() + "," +
                " STEP=" + nodedata.getStep() + "," +
                " THRESHOLD=" + nodedata.getThreshold() + "," +
                " BASEFOLDER=" + wrap(nodedata.getBaseFolder()) + "," +
                " LIST_COMMAND=" + wrap(nodedata.getListCommand()) + "," +
                " SETUP_PROFILE_COMMAND=" + wrap(nodedata.getSetupProfileCommand()) +
                " where NODE_NAME=" + wrap(nodeName);
    }

}
