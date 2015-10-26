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
package workbench.mac;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import workbench.RGui;
import workbench.actions.wb.ShowAboutAction;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 26/09/2011
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class MenuHandlers {

    private RGui rgui;

    class MacWorkbenchAboutHandler implements AboutHandler {
        public void handleAbout(com.apple.eawt.AppEvent.AboutEvent aboutEvent) {
            new ShowAboutAction(rgui, "about").actionPerformed(null);
        }
    }

    class MacWorkbenchQuitHandler implements QuitHandler {
        public void handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent quitEvent, com.apple.eawt.QuitResponse quitResponse) {
            if (rgui.handleWorkbenchClosure() == 1) {
                //System.exit(0);
                quitResponse.performQuit();
            } else {
                quitResponse.cancelQuit();
            }
        }
    }

    public MenuHandlers(RGui rgui) {
        this.rgui = rgui;
    }

    public void init() {
        Application macApplication = Application.getApplication();

        macApplication.setAboutHandler(new MacWorkbenchAboutHandler());
        macApplication.setQuitHandler(new MacWorkbenchQuitHandler());
    }
}
