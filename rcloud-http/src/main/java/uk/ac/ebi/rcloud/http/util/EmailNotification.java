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
package uk.ac.ebi.rcloud.http.util;

import uk.ac.ebi.rcloud.naming.Names;
import uk.ac.ebi.rcloud.rpf.db.data.ProjectDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;
import uk.ac.ebi.rcloud.util.MailClient;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 11, 2009
 * Time: 3:43:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailNotification {

    private static String benchurl = "http://www.ebi.ac.uk/Tools/rcloud";

    public static void sendBugReport(String username, String useremail, String subject,
                                     String message, String category, String version){
        try {
            MailClient client = new MailClient();

            String subj   = "Bench Bug Report: "+subject;
            message       = message.replaceAll("\n", "<br>");
            String text   =
                    "A new bug report!<br><br>" +
                            "<table border=\"0\" width=\"50%\">\n" +
                            "        <tr bgcolor=#e0e0e0><td>User Name: </td><td><p align=left>"+username+"</p></td></tr>\n" +
                            "        <tr bgcolor=#e0e0e0><td>Subject: </td><td><p align=left>"+subject+"</p></td></tr>\n" +
                            "        <tr bgcolor=#f0f0f0><td>Category: </td><td><p align=left>"+category+"</p></td></tr>\n" +
                            "        <tr bgcolor=#e0e0e0><td>Message: </td><td><p align=left>"+message+"</p></td></tr>\n" +
                            "        <tr bgcolor=#f0f0f0><td>Version: </td><td><p align=left>"+version+"</p></td></tr>\n" +
                            "</table>\n";

            // get arrays
            String[] to = { "andrew@ebi.ac.uk", "ostolop@ebi.ac.uk" };
            String[] a = {};

            client.sendMail( useremail, to, subj, text, a );

        } catch ( Exception e ) {
            //e.printStackTrace();
        }
    }

    public static void sendMessageToDevelopment(String username, String useremail, String subject,
                                                String message, String version){
        MailClient client = new MailClient();

        String subj   = "Bench Feedback: "+subject;
        message       = message.replaceAll("\n", "<br>");
        String text   =
                "Great! You've got feedback!<br><br>" +
                        "<table border=\"0\" width=\"50%\">\n" +
                        "        <tr bgcolor=#e0e0e0><td><p align=left>"+username+"</p></td></tr>\n" +
                        "        <tr bgcolor=#e0e0e0><td><p align=left>"+subject+"</p></td></tr>\n" +
                        "        <tr bgcolor=#f0f0f0><td><p align=left>"+message+"</p></td></tr>\n" +
                        "        <tr bgcolor=#e0e0e0><td><p align=left>"+version+"</p></td></tr>\n" +
                        "</table>\n";

        // get arrays
        String[] to = { "andrew@ebi.ac.uk", "ostolop@ebi.ac.uk" };
        String[] a = {};

        try {
            client.sendMail( useremail, to, subj, text, a );
        } catch ( Exception e ) {
        }
    }


    public static void sendRegistrationNotification(UserDataDB user){
        MailClient client = new MailClient();

        String subj   = "Registration Details for " + Names.getWorkbenchName();
        String text   = "Great, <br><p align=\"left\">" +
                        "You've successfully registered to use the " + Names.getConceptName() + ". "+
                        "And you're welcome to the community!<br><br>" +
                        "Please visit the project web page <br> <a href="+ benchurl + ">" +
                        benchurl + "</a><br><p align=\"left\">"+
                        "Registration details:<br>"+
                        "Name: " + user.getFullName() + "<br>" +
                        "Username: " + user.getLogin() + "<br>" +
                        "Password: " + user.getPwd() + "<br><br>" +
                        "Regards,<br>" +
                        Names.getTeamName() + "<br>";

        String from   = "rcloud@ebi.ac.uk";

        // get arrays
        String[] to = { user.getEmail() };
        String[] a = {};


        try {
            client.sendMail( from, to, subj, text, a );
        } catch ( Exception e ) {
        }
    }

    public static void sendRestorationNotification(Vector<UserDataDB> users){
        if (users.size() > 0) {

            MailClient client = new MailClient();

            String subj   = "Registration Details for " + Names.getWorkbenchName();
            String text   = "Your " + Names.getConceptName() + " registration details:<br>";

            for (UserDataDB user : users) {
                text += "User: "+user.getFullName() + "<br>";
                text += "Username: " + user.getLogin() + "<br><br>";
            }

            text +=         "Regards,<br>" +
                            Names.getTeamName() + "<br>";

            String from   = "rcloud@ebi.ac.uk";

            // get arrays
            String[] to = { users.get(0).getEmail() };
            String[] a = {};

            try {
                client.sendMail( from, to, subj, text, a );
            } catch ( Exception e ) {
            }
        }
    }

    public static void sendMaxUptimeReminder(UserDataDB user, ProjectDataDB project, int maxtime_hr){

        MailClient client = new MailClient();

        String subj   = "\"" + project.getTitle() + "\" Max Server Time Reminder";
        String text   = "Dear user, <br><br>" +
                        "In order to allow more fair distribution of cpu time and easier maintenance, the maximum running " +
                        "time of a single R server is limited to " + maxtime_hr + " hours (~" +  Math.ceil(maxtime_hr/24) + " days). "+
                        "After the allotted time slice, the server will be automatically stopped. All data will be saved. " +
                        "The project can be relaunched.<br><br>" +
                        "This however doesn't impose any restrictions on the number of times a project can be launched.<br><br>" +

                        "Regards,<br>" +
                        Names.getTeamName() + "<br>";

        String from   = "rcloud@ebi.ac.uk";

        // get arrays
        String[] to = { user.getEmail() };
        String[] a = {};

        try {
            client.sendMail( from, to, subj, text, a );
        } catch ( Exception e ) {
        }

    }

    public static void sendMaxUptimeNotification(UserDataDB user, ProjectDataDB project, int maxtime_hr, int timeleft_hr){

        MailClient client = new MailClient();

        String subj   = "\"" + project.getTitle() + "\" Max Server Time Reminder";
        String text   = "Dear user, <br><br>" +
                        "The server will be stopped in " + timeleft_hr + " hours." +
                        "You can relaunch the project as necessary.<br><br>" +

                        "Regards,<br>" +
                        Names.getTeamName() + "<br>";

        String from   = "rcloud@ebi.ac.uk";

        // get arrays
        String[] to = { user.getEmail() };
        String[] a = {};

        try {
            client.sendMail( from, to, subj, text, a );
        } catch ( Exception e ) {
        }

    }


    public static void main(String[] args) {
        HashMap<String, Object> opt1 = new HashMap<String, Object>();

        opt1.put(UserDataDB.EMAIL, System.getProperty("email"));
        opt1.put(UserDataDB.FULLNAME, "AT");
        opt1.put(UserDataDB.LOGIN, "login");
        opt1.put(UserDataDB.PWD, "password");

        UserDataDB user0 = new UserDataDB(opt1);

        Vector<UserDataDB> userllist = new Vector<UserDataDB>();

        userllist.add(user0);


        //
        HashMap<String, Object> opt2 = new HashMap<String, Object>();

        opt2.put(ProjectDataDB.TITLE, "My Project");

        ProjectDataDB proj = new ProjectDataDB(opt2);

        sendMaxUptimeReminder(user0, proj, 168);
        sendRestorationNotification(userllist);
        sendRegistrationNotification(user0);
    }

}
