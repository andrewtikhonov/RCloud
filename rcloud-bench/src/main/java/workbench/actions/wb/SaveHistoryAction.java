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

import workbench.RGui;
import uk.ac.ebi.rcloud.server.RServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.views.rconsole.WorkbenchRConsole;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 13, 2009
 * Time: 1:32:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaveHistoryAction extends AbstractAction {
    final private static Logger log = LoggerFactory.getLogger(SaveHistoryAction.class);
    
    private RGui rgui;

    public SaveHistoryAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(ActionEvent event) {
        if (event != null) {
            Vector<String> commands = new Vector<String>();
            commands.add(event.getActionCommand());
            appendToCommandHistory(commands);
        } else {
            saveCommandHistory();
        }
    }

    @Override
    public boolean isEnabled() {
        return rgui.isRAvailable() && rgui.getProject() != null && !rgui.getOpManager().isLocked();
    }

    public String getHistoryFilePath() {
        return rgui.getProject().getAbsolutePath() + "/.RUserHistory";
    }

    class SaveHistoryRunnable implements Runnable {
        private Vector<String> commands;
        private boolean append;
        private String filename;

        public SaveHistoryRunnable(Vector<String> commands, String filename, boolean append){
            this.commands = commands;
            this.filename = filename;
            this.append = append;
        }

        public void run(){
            RServices r = rgui.obtainR();

            if (r != null) {
                try {
                    r.saveHistory(commands, filename, append);
                } catch (Exception re) {
                    log.error("Error!", re);
                }
            }
        }
    }

    public void saveCommandHistory() {
        saveCommandHistory(null);
    }

    public void saveCommandHistory(String filename) {
        filename = (filename != null ? filename : getHistoryFilePath());

        WorkbenchRConsole console = rgui.getConsole();
        if (console != null) {
            new Thread(new SaveHistoryRunnable(console.getCommandHistory(), filename, false)).start();
        }
    }

    public void appendToCommandHistory(Vector<String> commands) {
        new Thread(new SaveHistoryRunnable(commands, getHistoryFilePath(), true)).start();
    }

}

