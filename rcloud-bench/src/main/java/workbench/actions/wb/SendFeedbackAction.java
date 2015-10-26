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
import workbench.dialogs.SendFeedbackDialog;
import workbench.runtime.RuntimeEnvironment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 07/07/2011
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
public class SendFeedbackAction extends AbstractAction {

    private Component c;
    private RuntimeEnvironment runtime;

    public SendFeedbackAction(String title0, Component c, RuntimeEnvironment runtime) {
        super(title0);
        this.c = c;
        this.runtime = runtime;
    }

    public static void showFeedbackDialog(Component c, RuntimeEnvironment runtime, String subject, String message) {

        SendFeedbackDialog dialog = SendFeedbackDialog.getInstance(c);

        if (runtime.getUser() == null) {
            dialog.setFeedbackSenderName("your name");
            dialog.setFeedbackSenderEmail("your email");
        } else {
            dialog.setFeedbackSenderName(runtime.getUser().getFullName());
            dialog.setFeedbackSenderEmail(runtime.getUser().getEmail());
        }

        if (subject != null) {
            dialog.setFeedbackSubject(subject);
        }

        if (message != null) {
            dialog.setFeedbackMessage(message);
        }

        dialog.setVisible(true);

        if (dialog.getResult() == SendFeedbackDialog.OK) {
            try {
                ServerRuntimeImpl.sendMessage(runtime.getSession(),
                        dialog.getFeedbackSenderName(),
                        dialog.getFeedbackSenderEmail(),
                        dialog.getFeedbackSubject(),
                        dialog.getFeedbackMessage(),
                        SoftwareVersion.getVersion());

                JOptionPaneExt.showMessageDialog(c,
                        "Thanks! We really appreciate your feedback.", "", JOptionPaneExt.PLAIN_MESSAGE);

            } catch (ConnectionFailedException cfe) {
                JOptionPaneExt.showMessageDialog(c,
                        "Cannot establish connection to proxy server", "", JOptionPaneExt.ERROR_MESSAGE);
            } catch (TunnelingException te) {
                JOptionPaneExt.showMessageDialog(c,
                        "Tunneling error while sending message", "", JOptionPaneExt.ERROR_MESSAGE);
            }
        }

    }

    public void actionPerformed(ActionEvent event) {
        showFeedbackDialog(c, runtime, null, null);
    }

    public boolean isEnabled() {
        return true;
    }
}
