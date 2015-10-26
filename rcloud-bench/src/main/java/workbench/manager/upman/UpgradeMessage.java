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
package workbench.manager.upman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 12, 2010
 * Time: 9:55:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpgradeMessage {

    final private static Logger log = LoggerFactory.getLogger(UpgradeMessage.class);

    public static class MyHyperlinkListener implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    URI uri = event.getURL().toURI();

                    java.awt.Desktop.getDesktop().browse(uri);

                } catch (Exception ex) {
                    log.error("Error!", ex);
                }
            }
        }
    }

    public static void versionMismatch(Component c, AbstractAction action, String serverversion, String benchversion) {

        Color c0 = c.getBackground();

        String message = "<font face=\"verdana\" size=\"10px\">"
                + "Client and Server versions do not match!</font>"
                + "<br><br>"
                + "<table style=\"font-family:verdana;font-size:10px\" border=\"0\" width=\"100%\">\n"
                + "        <tr bgcolor=" + toHtmlColor(c0,DARKER) + "><td>Bench version: </td><td><p align=left>"+benchversion+"</p></td></tr>\n"
                + "        <tr bgcolor=" + toHtmlColor(c0,BRIGHTER) + "><td>Server version: </td><td><p align=left>"+serverversion+"</p></td></tr>\n"
                + "</table><br>"
                + "<br>"
                + "<font face=\"verdana\" size=\"10px\">"
                + "Would you like to shutdown server and restart the project ?"
                + "</font>";

        JEditorPane messageArea = new JEditorPane("text/html", message);
        messageArea.setBackground(c.getBackground());
        messageArea.addHyperlinkListener(new MyHyperlinkListener());
        messageArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(messageArea);
        scroll.setPreferredSize(new Dimension(350, 170));
        scroll.setBorder(null);


        String[] options = { "Shutdown Server", "No, thanks." };

        int reply = JOptionPaneExt.showOptionDialog(c,
                scroll,
                "Version Mismatch",
                JOptionPaneExt.DEFAULT_OPTION,
                JOptionPaneExt.QUESTION_MESSAGE,
                null, options, options[0]);


        if (reply == 0) {
            try {
                action.actionPerformed(null);

            } catch (Exception ex) {
                log.error("Error!", ex);
            }

        } else {
            //return;
        }


    }

    private static int NOCHANGE = 0;
    private static int BRIGHTER = 1;
    private static int DARKER = 2;


    public static String toHtmlColor(Color c, int opcode) {
        int r0 = c.getRed();
        int g0 = c.getGreen();
        int b0 = c.getBlue();

        if (opcode == NOCHANGE) {
        } else if (opcode == BRIGHTER) {
            r0 = Math.min(r0 + 20, 255);
            g0 = Math.min(g0 + 20, 255);
            b0 = Math.min(b0 + 20, 255);
        } else if (opcode == DARKER) {
            r0 = Math.max(r0 - 20, 0);
            g0 = Math.max(g0 - 20, 0);
            b0 = Math.max(b0 - 20, 0);
        }

        return("rgb(" + r0 + "," + g0 + "," + b0 +")");
    }

    private static class LaunchWebActionListener implements ActionListener {
        private String url;
        public LaunchWebActionListener(String url) {
            this.url = url;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                URI uri = new URI(url);

                java.awt.Desktop.getDesktop().browse(uri);

            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }
    }


    public static void newVersion(Component c, String baseurl, String currentversion, String availableversion) {

        Color c0 = c.getBackground();

        String message = "<font face=\"verdana\" size=\"10px\">"
                + "New R Cloud Workbench version is available!"
                + "<br><br>"
                + "<table style=\"font-family:verdana;font-size:10px\" border=\"0\" width=\"100%\">\n"
                + "        <tr bgcolor=" + toHtmlColor(c0,DARKER) + "><td>Available version: </td><td><p align=left>"+availableversion+"</p></td></tr>\n"
                + "        <tr bgcolor=" + toHtmlColor(c0,BRIGHTER) + "><td>Current version: </td><td><p align=left>"+currentversion+"</p></td></tr>\n"
                + "</table><br>"
                + "<br>"
                + "<font face=\"verdana\" size=\"10px\">"
                + "Would you like to visit the web site ?"
                + "</font>";

        JEditorPane messageArea = new JEditorPane("text/html", message);
        messageArea.setBackground(c.getBackground());
        messageArea.addHyperlinkListener(new MyHyperlinkListener());
        messageArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(messageArea);
        scroll.setPreferredSize(new Dimension(350, 170));
        scroll.setBorder(null);


        String[] options = { "Visit R Cloud Website", "No, thanks." };

        int reply = JOptionPaneExt.showOptionDialog(c,
                scroll,
                "New version is available",
                JOptionPaneExt.DEFAULT_OPTION,
                JOptionPaneExt.QUESTION_MESSAGE,
                null, options, options[0]);


        if (reply == 0) {
            try {
                URI uri = new URI(baseurl);

                java.awt.Desktop.getDesktop().browse(uri);

            } catch (Exception ex) {
                log.error("Error!", ex);
            }

        } else {
            //return;
        }

    }

    public static void main(String[] args) {

        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setBackground(new Color(128,0,0));
        frame.setSize(new Dimension(100,100));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        newVersion(frame.getRootPane(), "http://www.ebi.ac.uk/Tools/rcloud", "1.1.1", "2.2.2");
        versionMismatch(frame.getRootPane(), null, "1.1.1", "2.2.2");

    }
}
