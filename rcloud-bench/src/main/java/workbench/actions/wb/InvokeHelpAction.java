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
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import uk.ac.ebi.rcloud.util.HexUtil;
import workbench.RGui;

import javax.swing.*;
import java.awt.event.ActionEvent;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 25, 2010
 * Time: 4:50:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class InvokeHelpAction extends AbstractAction {

    final private static Logger log = LoggerFactory.getLogger(InvokeHelpAction.class);

    private RGui rgui;

    public InvokeHelpAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(ActionEvent e) {
        UserDataDAO user = rgui.getRuntime().getUser();

        if (user != null) {
            rgui.setHelpBrowserURL(rgui.getRuntime().getHelpUrl() +
                    "/doc/html/index.html");
        } else {
        }
    }

    public boolean isEnabled() {
        return (rgui.isRAvailable() && !rgui.getOpManager().isLocked());
    }

}
