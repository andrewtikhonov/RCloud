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
 * Date: Mar 18, 2010
 * Time: 2:05:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeDataDB implements Serializable{

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String NODE_NAME                 = "NODE_NAME";
    public static String NODE_TYPE                 = "NODE_TYPE";
    public static String HOST_IP                   = "HOST_IP";
    public static String POOL_PREFIX               = "POOL_PREFIX";
    public static String HOST_NAME                 = "HOST_NAME";
    public static String LOGIN                     = "LOGIN";
    public static String PWD                       = "PWD";
    public static String INSTALL_DIR               = "INSTALL_DIR";
    public static String CREATE_SERVANT_COMMAND    = "CREATE_SERVANT_COMMAND";
    public static String KILL_SERVANT_COMMAND      = "KILL_SERVANT_COMMAND";
    public static String OS                        = "OS";
    public static String SERVANT_NBR_MIN           = "SERVANT_NBR_MIN";
    public static String SERVANT_NBR_MAX           = "SERVANT_NBR_MAX";
    public static String PROCESS_COUNTER           = "PROCESS_COUNTER";
    public static String STEP                      = "STEP";
    public static String THRESHOLD                 = "THRESHOLD";
    public static String BASEFOLDER                = "BASEFOLDER";
    public static String LIST_COMMAND              = "LIST_COMMAND";
    public static String SETUP_PROFILE_COMMAND     = "SETUP_PROFILE_COMMAND";

    public NodeDataDB( HashMap<String, Object> options ) {
        map = options;
    }

    public String getNodeName() {
        return (String) map.get(NODE_NAME);
    }

    public String getNodeType() {
        return (String) map.get(NODE_TYPE);
    }

	public String getHostIp() {
        return (String) map.get(HOST_IP);
	}

    public String getPoolPrefix() {
        return (String) map.get(POOL_PREFIX);
    }

    public String getHostName() {
        return (String) map.get(HOST_NAME);
    }

    public String getLogin() {
        return (String) map.get(LOGIN);
    }

    public String getPwd() {
        return (String) map.get(PWD);
    }

    public String getInstallDir() {
        return (String) map.get(INSTALL_DIR);
    }

    public String getCreateServantCommand() {
        return (String) map.get(CREATE_SERVANT_COMMAND);
    }

    public String getKillServantCommand() {
        return (String) map.get(KILL_SERVANT_COMMAND);
    }

    public String getOS() {
        return (String) map.get(OS);
    }

	public int getServantNbrMin() {
        return (Integer) map.get(SERVANT_NBR_MIN);
	}

	public int getServantNbrMax() {
        return (Integer) map.get(SERVANT_NBR_MAX);
	}

    public int getProcessCounter() {
        return (Integer) map.get(PROCESS_COUNTER);
    }

    public int getStep() {
        return (Integer) map.get(STEP);
    }

    public int getThreshold() {
        return (Integer) map.get(THRESHOLD);
    }

    public String getBaseFolder() {
        return (String) map.get(BASEFOLDER);
    }

    public String getSetupProfileCommand() {
        return (String) map.get(SETUP_PROFILE_COMMAND);
    }

    public String getListCommand() {
        return (String) map.get(LIST_COMMAND);
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

	public String toString() {
		return "Name=" + getNodeName() + " prefix=" + getPoolPrefix();
	}

}
