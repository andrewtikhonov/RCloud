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

import uk.ac.ebi.rcloud.server.RServices;
import workbench.RGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 13, 2009
 * Time: 1:16:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadHistoryAction extends AbstractAction {
    final private static Logger log = LoggerFactory.getLogger(LoadHistoryAction.class);
    
    private RGui rgui;

    public LoadHistoryAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(ActionEvent event) {
        loadHistoryFromServer(rgui.obtainR(),
                rgui.getProject().getAbsolutePath() + "/.RUserHistory");
    }

    @Override
    public boolean isEnabled() {
        return rgui.isRAvailable()&& rgui.getProject() != null && !rgui.getOpManager().isLocked();
    }

    class LoadRunnable implements Runnable {
        private RServices r;
        private String filename;
        public LoadRunnable(RServices r, String filename){
            this.r = r;
            this.filename = filename;
        }

        public void run(){
            if (r != null && rgui.getConsole() != null) {
                try {
                    rgui.getConsole().setCommandHistory(r.loadHistory(filename));

                } catch (RemoteException re) {
                    log.error("Error!", re);
                }
            }
        }
    }

    public void loadHistoryFromServer(RServices r, String filename) {

        new Thread(new LoadRunnable(r, filename)).start();
    }

}
