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
package uk.ac.ebi.rcloud.rpf.db.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Created by andrew on 12/10/15.
 */
public class ServerDataDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String serverName         = "serverName";
    public static String serverInUse        = "serverInUse";
    public static String pingFailures       = "pingFailures";
    public static String nodeName           = "nodeName";

    public static String registerTime       = "registerTime";

    public static String processId          = "processId";
    public static String hostName           = "HostName";
    public static String hostIp             = "hostIp";
    public static String hostOs             = "hostOs";

    public static String borrowTime         = "borrowTime";
    public static String borrowProcessId    = "borrowProcessId";
    public static String borrowHostName     = "borrowHostName";
    public static String borrowHostIp       = "borrowHostIp";

    public static String returnTime         = "returnTime";
    public static String returnProcessId    = "returnProcessId";
    public static String returnHostName     = "returnHostName";
    public static String returnHostIp       = "returnHostIp";

    public static String codeBase           = "codeBase";
    public static String stubHex            = "stubHex";

    public static String borrowSessionInfo  = "borrowSessionInfo";

    public static String attributeSet       = "attributeSet";
    public static String jobId              = "jobId";
    public static String jobName            = "jobName";
    public static String notifyEmail        = "notifyEmail";

    public static String isNotified         = "isNotified";

    public static String owner              = "owner";
    public static String projectId          = "projectId";
    public static String masterName         = "masterName";
    public static String dontMonitor        = "dontMonitor";


    public Timestamp getRegisterTime() {
        return (Timestamp) map.get(registerTime);
    }

    public Timestamp getBorrowTime() {
        return (Timestamp) map.get(borrowTime);
    }

    public String getBorrowProcessId() {
        return (String) map.get(borrowProcessId);
    }

    public String getBorrowHostName() {
        return (String) map.get(borrowHostName);
    }

    public String getBorrowHostIP() {
        return (String) map.get(borrowHostIp);
    }

    public Timestamp getReturnTime() {
        return (Timestamp) map.get(returnTime);
    }

    public String getReturnProcessId() {
        return (String) map.get(returnProcessId);
    }

    public String getReturnHostName() {
        return (String) map.get(returnHostName);
    }

    public String getReturnHostIP() {
        return (String) map.get(returnHostIp);
    }

    public String getBorowSessionInfoHex() {
        return (String) map.get(borrowSessionInfo);
    }

    public Integer getNotified() {
        Object obj = map.get(isNotified);

        if (obj instanceof java.math.BigDecimal){
            return ((java.math.BigDecimal) obj).intValue();
        }
        return ((Integer) obj);
    }

    public String getDontMonitor() {
        return (String) map.get(dontMonitor);
    }

    public ServerDataDAO( HashMap<String, Object> map ) {
        this.map = map;
    }

    public String getServerName() {
        return (String) map.get(serverName);
    }

    public String getStubHex() {
        return (String) map.get(stubHex);
    }

    public String getProcessId() {
        return (String) map.get(processId);
    }

    public String getHostName() {
        return (String) map.get(hostName);
    }

    public String getHostIp() {
        return (String) map.get(hostIp);
    }

    public String getOsName() {
        return (String) map.get(hostOs);
    }

    public String getCodeBase() {
        return (String) map.get(codeBase);
    }

    public String getJobId() {
        return (String) map.get(jobId);
    }

    public String getJobName() {
        return (String) map.get(jobName);
    }

    public String getEmail() {
        return (String) map.get(notifyEmail);
    }

    public String getOwner() {
        return (String) map.get(owner);
    }

    public String getProjectId() {
        return (String) map.get(projectId);
    }

    public String getMaster() {
        return (String) map.get(masterName);
    }

    public String getNodeName() {
        return (String) map.get(nodeName);
    }

    public String getAttributesHex() {
        return (String) map.get(attributeSet);
    }

    public Integer getServerInUse() {
        Object obj = map.get(serverInUse);

        if (obj instanceof java.math.BigDecimal){
            return ((java.math.BigDecimal) obj).intValue();
        }
        return ((Integer) obj);
    }

    public Integer getPingFailures() {
        Object obj = map.get(pingFailures);

        if (obj instanceof java.math.BigDecimal){
            return ((java.math.BigDecimal) obj).intValue();
        }
        return ((Integer) obj);
    }

    public boolean isEmpty()
    {
        return (map == null || map.isEmpty());
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public String toString() {
        return "Server NAME=" + getServerName() + " STUBHEX=" + getStubHex();
    }

}
