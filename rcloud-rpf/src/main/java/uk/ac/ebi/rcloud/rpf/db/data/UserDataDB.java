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
 * Date: May 7, 2009
 * Time: 5:49:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDataDB implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(UserDataDB.class);

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String LOGIN          = "LOGIN";
    public static String PWD            = "PWD";
    public static String STATUS         = "STATUS";
    public static String USERFOLDER     = "USERFOLDER";
    public static String FULLNAME       = "FULLNAME";
    public static String EMAIL          = "EMAIL";
    public static String REGISTERED     = "REGISTERED";
    public static String PROFILE        = "PROFILE";

    public static String LAST_LOGGED_IN = "LAST_LOGGED_IN";
    public static String LAST_LOGGED_OUT = "LAST_LOGGED_OUT";
    public static String TIMES_LOGGED_IN = "TIMES_LOGGED_IN";
    public static String POOLNAME       = "POOLNAME";
    public static String SUPERVISOR       = "SUPERVISOR";
    public static String GROUPNAME       = "GROUPNAME";
    public static String GROUPHEAD       = "GROUPHEAD";

    public static String USER_ONLINE    = "ONLINE";
    public static String USER_OFFLINE   = "OFFLINE";

    public UserDataDB( HashMap<String, Object> options ) {
        map = options;
    }

    public String getLogin() {
        return (String) map.get(LOGIN);
    }

    public String getPwd() {
        return (String) map.get(PWD);
    }

    public String getStatus() {
        return (String) map.get(STATUS);
    }

    public String getUserFolder() {
        return (String) map.get(USERFOLDER);
    }

    public String getBaseFolder() {
        String userFolder = getUserFolder();
        return userFolder.substring(0, userFolder.lastIndexOf("/"));
    }

    public String getUserLibFolder() {
        return (String) map.get(USERFOLDER) + "/RLibs";
    }

    public String getFullName() {
        return (String) map.get(FULLNAME);
    }

    public String getEmail() {
        return (String) map.get(EMAIL);
    }

    public Integer getTimesLoggedIn() {
        return Integer.parseInt((String) map.get(TIMES_LOGGED_IN));
    }

    public Timestamp getLastLoggedIn() {
        return (Timestamp) map.get(LAST_LOGGED_IN);
    }

    public Timestamp getLastLoggedOut() {
        return (Timestamp) map.get(LAST_LOGGED_OUT);
    }

    public Timestamp getRegistered() {
        return (Timestamp) map.get(REGISTERED);
    }

    public String getProfile() {
        return (String) map.get(PROFILE);
    }

    public String getPoolname() {
        return (String) map.get(POOLNAME);
    }

    public String getGroupname() {
        return (String) map.get(GROUPNAME);
    }

    public boolean isGrouphead() {
        return ("TRUE".equalsIgnoreCase((String) map.get(GROUPHEAD)));
    }

    public boolean isSupervisor() {
        return ("TRUE".equalsIgnoreCase((String) map.get(SUPERVISOR)));
    }

    public boolean isEmpty()
    {
        return (map == null || map.isEmpty());
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public String toString() {
        return "User login=" + getLogin() + " fullname=" + getFullName();
    }

}

