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
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 14, 2009
 * Time: 3:05:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerDataDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String NAME               = "NAME";
    public static String IN_USE             = "IN_USE";
    public static String PING_FAILURES      = "PING_FAILURES";
    public static String NODE_NAME          = "NODE_NAME";

    public static String REGISTER_TIME      = "REGISTER_TIME";

    public static String PROCESS_ID         = "PROCESS_ID";
    public static String HOST_NAME          = "HOST_NAME";
    public static String HOST_IP            = "HOST_IP";
    public static String OS                 = "OS";

    public static String BORROW_TIME        = "BORROW_TIME";
    public static String BORROW_PROCESS_ID  = "BORROW_PROCESS_ID";
    public static String BORROW_HOST_NAME   = "BORROW_HOST_NAME";
    public static String BORROW_HOST_IP     = "BORROW_HOST_IP";

    public static String RETURN_TIME        = "RETURN_TIME";
    public static String RETURN_PROCESS_ID  = "RETURN_PROCESS_ID";
    public static String RETURN_HOST_NAME   = "RETURN_HOST_NAME";
    public static String RETURN_HOST_IP     = "RETURN_HOST_IP";

    public static String CODEBASE           = "CODEBASE";
    public static String STUB_HEX           = "STUB_HEX";

    public static String BORROW_SESSION_INFO_HEX  = "BORROW_SESSION_INFO_HEX";

    public static String ATTRIBUTES_HEX     = "ATTRIBUTES_HEX";
    public static String JOB_ID             = "JOB_ID";
    public static String JOB_NAME           = "JOB_NAME";
    public static String NOTIFY_EMAIL       = "NOTIFY_EMAIL";

    public static String NOTIFIED           = "NOTIFIED";

    public static String OWNER              = "OWNER";
    public static String PROJECT            = "PROJECT";
    public static String MASTER             = "MASTER";
    public static String DONT_MONITOR       = "DONT_MONITOR";


    public Timestamp getRegisterTime() {
        return (Timestamp) map.get(REGISTER_TIME);
    }

    public Timestamp getBorrowTime() {
        return (Timestamp) map.get(BORROW_TIME);
    }

    public String getBorrowProcessId() {
        return (String) map.get(BORROW_PROCESS_ID);
    }

    public String getBorrowHostName() {
        return (String) map.get(BORROW_HOST_NAME);
    }

    public String getBorrowHostIP() {
        return (String) map.get(BORROW_HOST_IP);
    }

    public Timestamp getReturnTime() {
        return (Timestamp) map.get(RETURN_TIME);
    }

    public String getReturnProcessId() {
        return (String) map.get(RETURN_PROCESS_ID);
    }

    public String getReturnHostName() {
        return (String) map.get(RETURN_HOST_NAME);
    }

    public String getReturnHostIP() {
        return (String) map.get(RETURN_HOST_IP);
    }

    public String getBorowSessionInfoHex() {
        return (String) map.get(BORROW_SESSION_INFO_HEX);
    }

    public Integer getNotified() {
        Object obj = map.get(NOTIFIED);

        if (obj instanceof java.math.BigDecimal){
            return ((java.math.BigDecimal) obj).intValue();
        }
        return ((Integer) obj);
    }

    public String getDontMonitor() {
        return (String) map.get(DONT_MONITOR);
    }

    public ServerDataDB( HashMap<String, Object> map ) {
        this.map = map;
    }

    public String getName() {
        return (String) map.get(NAME);
    }

    public String getStubhex() {
        return (String) map.get(STUB_HEX);
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

    public String getCodeBase() {
        return (String) map.get(CODEBASE);
    }

    public String getJobId() {
        return (String) map.get(JOB_ID);
    }

    public String getJobName() {
        return (String) map.get(JOB_NAME);
    }

    public String getEmail() {
        return (String) map.get(NOTIFY_EMAIL);
    }

    public String getOwner() {
        return (String) map.get(OWNER);
    }

    public String getProject() {
        return (String) map.get(PROJECT);
    }

    public String getMaster() {
        return (String) map.get(MASTER);
    }

    public String getNodeName() {
        return (String) map.get(NODE_NAME);
    }

    public String getAttributesHex() {
        return (String) map.get(ATTRIBUTES_HEX);
    }

    public Integer getInUse() {
        Object obj = map.get(IN_USE);

        if (obj instanceof java.math.BigDecimal){
            return ((java.math.BigDecimal) obj).intValue();
        }
        return ((Integer) obj);
    }

    public Integer getPingFailures() {
        Object obj = map.get(PING_FAILURES);

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
        return "Server NAME=" + map.get(NAME) + " STUBHEX=" + map.get(STUB_HEX);
    }

}

