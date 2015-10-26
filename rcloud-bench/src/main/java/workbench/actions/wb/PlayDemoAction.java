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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.RGui;
import workbench.Workbench;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 25, 2010
 * Time: 5:19:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayDemoAction extends AbstractAction {

    final private static Logger log = LoggerFactory.getLogger(PlayDemoAction.class);

    private RGui rgui;

    public PlayDemoAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(ActionEvent event) {
        playDemo();
    }

    @Override
    public boolean isEnabled() {
        return (rgui.isRAvailable()&& !rgui.getOpManager().isLocked());
    }

    class DemoRunnable implements Runnable {
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        Workbench.class.getResourceAsStream("demo.R")));
                String l;
                while ((l = br.readLine()) != null) {

                    l = l.trim();
                    if (l.equals(""))
                        continue;

                    if (l.equals("#SNAPSHOT")) {

                        /*
                         * try { Thread.sleep(1400); } catch (Exception ex)
                         * { } workbenchActions.get("clone").actionPerformed(new
                         * ActionEvent(_graphicPanel, 0, null));
                         */

                    } else {
                        rgui.getConsole().play(l, true);
                    }
                    try {
                        Thread.sleep(300);
                    } catch (Exception ex) {
                    }
                }
            } catch (Exception e) {
                log.error("Error!", e);
            }

        }
    }

    private void playDemo() {
		new Thread(new DemoRunnable()).start();
	}

}
