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
public class UserDataDAO implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String username       = "username";
    public static String password       = "password";
    public static String status         = "status";
    public static String userFolder     = "userFolder";
    public static String fullName       = "fullName";
    public static String email          = "email";
    public static String registerTime   = "registerTime";
    public static String profile        = "PROFILE";

    public static String lastLoggedIn   = "lastLoggedIn";
    public static String lastLoggedOut  = "lastLoggedOut";
    public static String timesLoggedIn  = "timesLoggedIn";
    public static String poolName       = "poolName";
    public static String supervisor     = "supervisor";
    public static String groupName      = "groupName";
    public static String groupHead      = "groupHead";

    public static String USER_ONLINE    = "ONLINE";
    public static String USER_OFFLINE   = "OFFLINE";

    public UserDataDAO( HashMap<String, Object> options ) {
        map = options;
    }

    public String getUsername() {
        return (String) map.get(username);
    }

    public String getPassword() {
        return (String) map.get(password);
    }

    public String getStatus() {
        return (String) map.get(status);
    }

    public String getUserFolder() {
        return (String) map.get(userFolder);
    }

    public String getBaseFolder() {
        String userFolder = getUserFolder();
        return userFolder.substring(0, userFolder.lastIndexOf("/"));
    }

    public String getUserLibFolder() {
        return (String) map.get(userFolder) + "/RLibs";
    }

    public String getFullName() {
        return (String) map.get(fullName);
    }

    public String getEmail() {
        return (String) map.get(email);
    }

    public Integer getTimesLoggedIn() {
        return Integer.parseInt((String) map.get(timesLoggedIn));
    }

    public Timestamp getLastLoggedIn() {
        return (Timestamp) map.get(lastLoggedIn);
    }

    public Timestamp getLastLoggedOut() {
        return (Timestamp) map.get(lastLoggedOut);
    }

    public Timestamp getRegisterTime() {
        return (Timestamp) map.get(registerTime);
    }

    public String getProfile() {
        return (String) map.get(profile);
    }

    public String getPoolname() {
        return (String) map.get(poolName);
    }

    public String getGroupName() {
        return (String) map.get(groupName);
    }

    public boolean isGroupHead() {
        return ("TRUE".equalsIgnoreCase((String) map.get(groupHead)));
    }

    public boolean isSupervisor() {
        return ("TRUE".equalsIgnoreCase((String) map.get(supervisor)));
    }

    public boolean isEmpty()
    {
        return (map == null || map.isEmpty());
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public String toString() {
        return "User login=" + getUsername() + " fullname=" + getFullName();
    }

}

