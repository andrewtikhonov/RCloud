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
public class ProjectDataDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String title  = "title";
    public static String identifier = "identifier";
    public static String owner  = "owner";
    public static String status = "status";
    public static String description   = "description";
    public static String createdTime    = "createdTime";
    public static String lastOpened = "lastOpened";
    public static String lastClosed = "lastClosed";
    public static String timesOpened = "timesOpened";
    public static String overallTime = "overallTime";
    public static String baseFolder = "baseFolder";

    public static String lastActivity = "lastActivity";
    public static String isNotified  = "isNotified";

    public static String PROJECT_NEW     = "NEW";
    public static String PROJECT_OPENED  = "OPENED";
    public static String PROJECT_ONHOLD  = "ONHOLD";
    public static String PROJECT_STOPPED = "STOPPED";

    public ProjectDataDAO( final HashMap<String, Object> options ) {
        map = options;
    }

    public String getTitle() {
        return (String) map.get(title);
    }

    public String getIdentifier() {
        return (String) map.get(identifier);
    }

    public String getAbsolutePath() {
        return map.get(baseFolder) + "/" + map.get(identifier);
    }

    public String getBaseFolder() {
        return (String) map.get(baseFolder);
    }

    public String getOwner() {
        return (String) map.get(owner);
    }

    public String getStatus() {
        return (String) map.get(status);
    }

    public String getDescription() {
        return (String) map.get(description);
    }

    public int getTimesOpened() {
        return (Integer) map.get(timesOpened);
    }

    public int getNotified() {
        return (Integer) map.get(isNotified);
    }

    public String getOverallTime() {
        Object obj = map.get(overallTime);
        if (obj instanceof oracle.sql.INTERVALDS) {
            return obj.toString();
        }
        return (String) obj; //map.get(OVERALL_TIME);
    }

    public Timestamp getCreatedTime() {
        return (Timestamp) map.get(createdTime);
    }

    public Timestamp getLastOpened() {
        return (Timestamp) map.get(lastOpened);
    }

    public Timestamp getLastClosed() {
        return (Timestamp) map.get(lastClosed);
    }

    public Timestamp getLastActivity() {
        return (Timestamp) map.get(lastActivity);
    }

    public boolean isEmpty()
    {
        return (map == null || map.isEmpty());
    }

    public HashMap<String, Object> getMap() {
        return map;
    }

    public String toString() {
        return "Project title=" + getTitle() + " folder=" + getAbsolutePath();
    }

}


