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
 * Date: May 7, 2009
 * Time: 6:00:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectDataDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static String TITLE  = "TITLE";
    public static String IDENTIFIER = "FOLDER";
    public static String OWNER  = "OWNER";
    public static String STATUS = "STATUS";
    public static String DESC   = "DESCRIPTION";
    public static String CREATED    = "CREATED";
    public static String LAST_OPENED = "LAST_OPENED";
    public static String LAST_CLOSED = "LAST_CLOSED";
    public static String TIMES_OPENED = "TIMES_OPENED";
    public static String OVERALL_TIME = "OVERALL_TIME";
    public static String BASEFOLDER = "BASEFOLDER";

    public static String LAST_ACTIVITY = "LAST_ACTIVITY";
    public static String NOTIFIED = "NOTIFIED";

    public static String PROJECT_OPENED = "OPENED";
    public static String PROJECT_ONHOLD = "ONHOLD";
    public static String PROJECT_STOPPED = "STOPPED";

    public ProjectDataDB( final HashMap<String, Object> options ) {
        map = options;
    }

    public String getTitle() {
        return (String) map.get(TITLE);
    }

    public String getIdentifier() {
        return (String) map.get(IDENTIFIER);
    }

    public String getAbsolutePath() {
        return map.get(BASEFOLDER) + "/" + map.get(IDENTIFIER);
    }

    public String getBaseFolder() {
        return (String) map.get(BASEFOLDER);
    }

    public String getOwner() {
        return (String) map.get(OWNER);
    }

    public String getStatus() {
        return (String) map.get(STATUS);
    }

    public String getDescription() {
        return (String) map.get(DESC);
    }

    public int getTimesOpened() {
        return (Integer) map.get(TIMES_OPENED);
    }

    public int getNotified() {
        return (Integer) map.get(NOTIFIED);
    }

    public String getOverallTime() {
        Object obj = map.get(OVERALL_TIME);
        if (obj instanceof oracle.sql.INTERVALDS) {
            return obj.toString();
        }
        return (String) obj; //map.get(OVERALL_TIME);
    }

    public Timestamp getCreated() {
        return (Timestamp) map.get(CREATED);
    }

    public Timestamp getLastOpened() {
        return (Timestamp) map.get(LAST_OPENED);
    }

    public Timestamp getLastClosed() {
        return (Timestamp) map.get(LAST_CLOSED);
    }

    public Timestamp getLastActivity() {
        return (Timestamp) map.get(LAST_ACTIVITY);
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


