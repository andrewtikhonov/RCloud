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

import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;
import uk.ac.ebi.rcloud.util.DETools;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 20, 2010
 * Time: 6:20:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsersSQL extends SqlBase {

    public static String deleteUserStatement(String username) {
        return "DELETE FROM USERS WHERE LOGIN=" + wrap(username);
    }

    public static String checkExistsStatement(String username) {
        return "select count(*) from USERS where LOGIN=" + wrap(username);
    }

    public static String addUserStatement(UserDataDB user, String sysdate) {
        String statement = "insert into USERS " +
                "(LOGIN,PWD,STATUS,USERFOLDER,FULLNAME,EMAIL,REGISTERED,PROFILE," +
                "LAST_LOGGED_IN,TIMES_LOGGED_IN,OVERALL_TIME,POOLNAME) "
                + "values ("
                + wrap(user.getLogin())      + ","
                + wrap(user.getPwd()
                    .equals("") ? "" :
                    DETools.cipherString(
                            user.getPwd()))  + ","
                + wrap(user.getStatus())     + ","
                + wrap(user.getUserFolder()) + ","
                + wrap(user.getFullName())   + ","
                + wrap(user.getEmail())      + ","
                + sysdate                    + ","
                + wrap(user.getProfile())    + ","
                + sysdate + ","
                + "0" + ","
                + "INTERVAL '0 0:00:00.0' DAY TO SECOND" + ","
                + wrap(user.getPoolname()) + ")";

        return statement;
    }

    public static String updateUserStatement(UserDataDB user) {
        return "UPDATE USERS SET " +
                " FULLNAME = " + wrap(user.getFullName()) +
                ",EMAIL = " + wrap(user.getEmail()) +
                ",PROFILE = " + wrap(user.getProfile()) +
                " WHERE LOGIN=" + wrap(user.getLogin());
    }

    public static String updateUserClusterStatement(UserDataDB user) {
        return "UPDATE USERS SET " +
                " POOLNAME = " + wrap(user.getPoolname()) +
                ",USERFOLDER = " + wrap(user.getUserFolder()) +
                " WHERE LOGIN=" + wrap(user.getLogin());
    }

    public static String updateToLoggedInStatement(String username, String sysdate) {
        return "UPDATE USERS SET STATUS='ONLINE', LAST_LOGGED_IN=" + sysdate +
                ", TIMES_LOGGED_IN=(TIMES_LOGGED_IN+1) WHERE LOGIN=" + wrap(username);
    }

    public static String updateToLoggedOutStatement(String username, String sysdate) {
        return "UPDATE USERS SET STATUS='OFFLINE', LAST_LOGGED_OUT=" + sysdate +
               ", OVERALL_TIME = (OVERALL_TIME + (CAST (" + sysdate + " AS TIMESTAMP) - LAST_LOGGED_IN) DAY TO SECOND)" +
               " WHERE LOGIN=" + wrap(username);
    }



}
