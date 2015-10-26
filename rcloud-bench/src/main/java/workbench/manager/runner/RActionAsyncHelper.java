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
package workbench.manager.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.callback.RAction;
import uk.ac.ebi.rcloud.server.callback.RActionConst;
import uk.ac.ebi.rcloud.server.callback.RActionType;
import workbench.RGui;
import workbench.manager.opman.Operation;

import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 1, 2010
 * Time: 4:04:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class RActionAsyncHelper extends RunHelper implements RActionHandlerInterface {

    private static final Logger log = LoggerFactory.getLogger(RActionAsyncHelper.class);

    private static long idCounter = 0;

    private long id = idCounter++;
    private String command;
    private RGui rgui;
    private HashMap<String, Object> attributes = new HashMap<String, Object>();

    public RActionAsyncHelper(RGui rgui, String command) {
        this.command = command;
        this.rgui = rgui;
        attributes.put(RActionConst.HELPER, id);
    }

    public void actionPerformed(RAction action) {
        Long idToCheck = (Long) action.getAttributes().get(RActionConst.HELPER);
        if (idToCheck != null && idToCheck == id) {
            actionCompleted();
        }
    }

    public void mainAction() {
        if (rgui.obtainR() != null) {
            try {
                rgui.obtainR().asynchronousConsoleSubmit(command, attributes);
            } catch (RemoteException re) {
            }
        }
    }

    public void preAction() {
        rgui.addRActionHandler(RActionType.COMPLETE, this);
    }

    public void postAction() {
        rgui.removeRActionHandler(RActionType.COMPLETE, this);
    }

}
