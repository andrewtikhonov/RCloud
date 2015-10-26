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
package workbench.views.shellconsole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import workbench.RGui;
import workbench.views.extraconsole.ConsolePanelBase;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 30, 2010
 * Time: 1:31:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShellConsole extends ConsolePanelBase {

    final static private Logger log = LoggerFactory.getLogger(ShellConsole.class);

    private RGui rgui;

    private String CONSOLE_ID = "shell-";

    private String PROMPT = "% ";

    public ShellConsole(RGui rgui, int id) {

        this.rgui = rgui;

        setFont(new Font("Courier", Font.PLAIN, getFont().getSize()));

        printPrompt(PROMPT);

        addActionListener(new ShellConsoleActionListener());

        CONSOLE_ID = CONSOLE_ID + Integer.toString(id);

        rgui.addConsole(CONSOLE_ID, this);
    }

    //   P U B L I C
    //

    public void dispose() {
        rgui.removeConsole(CONSOLE_ID);
    }

    //   A C T I O N S
    //

    class CommandRunnable implements Runnable {
        private String command;
        public CommandRunnable(String command){
            this.command = command;
        }

        public void run() {
            try {
                if (rgui.obtainR() != null) {

                    HashMap<String, Object> attributes
                            = new HashMap<String, Object>();

                    attributes.put("originator", CONSOLE_ID);

                    rgui.obtainR().exec(command, attributes);

                } else {
                    JOptionPaneExt.showMessageDialog(ShellConsole.this,
                            "Sorry, but you're not connected to R server");
                }

            } catch (RemoteException re) {
            }

        }
    }

    class ShellConsoleActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            new Thread(new CommandRunnable(event.getActionCommand().trim())).start();
        }
    }
}
