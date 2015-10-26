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

import uk.ac.ebi.rcloud.rpf.db.data.ProjectDataDB;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 20, 2010
 * Time: 4:27:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectsSQL extends SqlBase {

    public static String checkExistsStatement(String identifier) {
        return "select count(*) from PROJECTS where FOLDER=" + wrap(identifier);
    }

    public static String deleteProjectStatement(String identifier) {
        return "DELETE FROM PROJECTS WHERE FOLDER=" + wrap(identifier);
    }

    public static String projectOpenedStatement(String identifier, String sysdate) {
        return "UPDATE PROJECTS SET STATUS=" + wrap(ProjectDataDB.PROJECT_OPENED) +
                ", LAST_OPENED=" + sysdate +
                ", LAST_ACTIVITY=" + sysdate +
                ", NOTIFIED = 0" +
                ", TIMES_OPENED=(TIMES_OPENED+1) WHERE FOLDER=" + wrap(identifier);
    }

    public static String projectOnholdStatement(String identifier, String sysdate) {
        return "UPDATE PROJECTS SET STATUS=" + wrap(ProjectDataDB.PROJECT_ONHOLD) +
                ", LAST_CLOSED=" + sysdate +
                ", LAST_ACTIVITY=" + sysdate +
                ", NOTIFIED = 0" +
                ", OVERALL_TIME = (OVERALL_TIME + (CAST (" + sysdate + " AS TIMESTAMP) - LAST_OPENED) DAY TO SECOND)" +
                " WHERE FOLDER=" + wrap(identifier);
    }

    public static String projectStoppedStatement(String identifier, String sysdate) {
        return "UPDATE PROJECTS SET STATUS= " + wrap(ProjectDataDB.PROJECT_STOPPED) +
                ", LAST_CLOSED=" + sysdate +
                ", LAST_ACTIVITY=" + sysdate +
                ", NOTIFIED = 0" +
                ", OVERALL_TIME = (OVERALL_TIME + (CAST (" + sysdate + " AS TIMESTAMP) - LAST_OPENED) DAY TO SECOND)" +
                " WHERE FOLDER=" + wrap(identifier);
    }

    public static String projectActivityStatement(String identifier, String sysdate) {
        return "UPDATE PROJECTS SET LAST_ACTIVITY=" + sysdate + " WHERE FOLDER=" + wrap(identifier);
    }

    public static String registerNotificationStatement(String identifier) {
        return "UPDATE PROJECTS SET NOTIFIED=(NOTIFIED + 1) WHERE FOLDER=" + wrap(identifier);
    }

    public static String updateProjectStatement(ProjectDataDB project) {
        return "UPDATE PROJECTS SET TITLE="+wrap(project.getTitle())+
                ", DESCRIPTION="+wrap(project.getDescription())+
                " WHERE FOLDER=" + wrap(project.getIdentifier());
    }

    public static String updateBasefolderStatement(ProjectDataDB project) {
        return "UPDATE PROJECTS SET BASEFOLDER="+wrap(project.getBaseFolder())+
                " WHERE FOLDER=" + wrap(project.getIdentifier());
    }

    public static String updateProjectDescription(ProjectDataDB project) {
        return "UPDATE PROJECTS SET DESCRIPTION="+wrap(project.getDescription())+
                " WHERE FOLDER=" + wrap(project.getIdentifier());
    }

    public static String addProjectStatement(ProjectDataDB project, String sysdate) {
        String statement = "INSERT INTO PROJECTS (TITLE,FOLDER,OWNER,STATUS,DESCRIPTION,CREATED," +
                "LAST_OPENED,TIMES_OPENED,OVERALL_TIME,BASEFOLDER,NOTIFIED,LAST_ACTIVITY) "
                + "VALUES ("
                + wrap( project.getTitle() ) + ","
                + wrap( project.getIdentifier() ) + ","
                + wrap( project.getOwner() ) + ","
                + wrap( project.getStatus() ) + ","
                + wrap( project.getDescription() ) + ","
                + sysdate + ","
                + sysdate + ","
                + "0" + ","
                + "INTERVAL '0 0:00:00.0' DAY TO SECOND" + ","
                + wrap( project.getBaseFolder() ) + ","
                + "0" + ","
                + sysdate
                + ")";

        return statement;
    }
}
