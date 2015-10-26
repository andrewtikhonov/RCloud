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
package workbench.actions.wb;

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.http.exception.ConnectionFailedException;
import uk.ac.ebi.rcloud.http.exception.TunnelingException;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import uk.ac.ebi.rcloud.version.SoftwareVersion;
import workbench.dialogs.SendBugReportDialog;
import workbench.runtime.RuntimeEnvironment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 07/07/2011
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class SendBugReportAction extends AbstractAction {

    private Component c;
    private RuntimeEnvironment runtime;

    public SendBugReportAction(String title0, Component c, RuntimeEnvironment runtime) {
        super(title0);
        this.c = c;
        this.runtime = runtime;
    }

    public static void showReportDialog(Component c, RuntimeEnvironment runtime, String title, String message) {
        SendBugReportDialog dialog = new SendBugReportDialog(c);

        if (runtime.getUser() == null) {
            dialog.setBugSenderEmail("your email");
            dialog.setBugSenderName("your name");
        } else {
            dialog.setBugSenderEmail(runtime.getUser().getEmail());
            dialog.setBugSenderName(runtime.getUser().getFullName());
        }

        if (title != null) {
            dialog.setBugTitle(title);
        }

        if (message != null) {
            dialog.setBugMessage(message);
        }

        dialog.setVisible(true);

        if (dialog.getResult() == SendBugReportDialog.OK) {
            try {
                ServerRuntimeImpl.sendBugReport(runtime.getSession(),
                        dialog.getBugSenderName(),
                        dialog.getBugSenderEmail(),
                        dialog.getBugTitle(),
                        dialog.getBugMessage(),
                        dialog.getBugCategory(),
                        SoftwareVersion.getVersion());

                JOptionPaneExt.showMessageDialog(c,
                        "The problem report has been successfully sent. We will contact you\n" +
                        "as soon as possible if the matter requires urgent attention.", "", JOptionPaneExt.PLAIN_MESSAGE);

            } catch (ConnectionFailedException cfe) {
                JOptionPaneExt.showMessageDialog(c,
                        "Cannot establish connection to proxy host", "", JOptionPaneExt.ERROR_MESSAGE);
            } catch (TunnelingException te) {
                JOptionPaneExt.showMessageDialog(c,
                        "Tunneling error while sending message", "", JOptionPaneExt.ERROR_MESSAGE);
            }
        }

    }

    public void actionPerformed(ActionEvent event) {
        showReportDialog(c, runtime, null, null);

    }

    public boolean isEnabled() {
        return true;
    }

    public static void main(String[] args) {

        /*
        HashMap<String, Object> opt = new HashMap<String, Object>();
        opt.put(UserDataDAO.LOGIN, "andrew");
        opt.put(UserDataDAO.EMAIL, "a@b.c");

        UserDataDAO user = new UserDataDAO(opt);
        */

        RuntimeEnvironment runtime0 = new RuntimeEnvironment();
        //runtime0.setUser(user);

        SendBugReportAction.showReportDialog(new JFrame(), runtime0,
                "problem unlinking server from project",
                "test message");

        System.exit(0);

    }

}
