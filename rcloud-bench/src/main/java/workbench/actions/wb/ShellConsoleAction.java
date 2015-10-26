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

import net.infonode.docking.DockingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.TabWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.RGui;
import workbench.Workbench;
import workbench.util.AbstractDockingWindowListener;
import workbench.views.extraconsole.RConsoleView;
import workbench.views.shellconsole.ShellConsoleView;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 1, 2010
 * Time: 5:00:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShellConsoleAction extends AbstractAction {

    final private static Logger log = LoggerFactory.getLogger(ShellConsoleAction.class);

    private RGui rgui;

    public ShellConsoleAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }


    public void actionPerformed(ActionEvent event) {
        invokeShellConsole();
    }

    public boolean isEnabled() {
        return rgui.isRAvailable();
    }

    public void invokeShellConsole() {
        int id = rgui.getViewManager().getDynamicViewId();

        ShellConsoleView shellview = new ShellConsoleView("Shell Console", null, id, rgui);

        rgui.getMainTabWindow().addTab(shellview);

        shellview.addListener(new ShellConsoleDockingWindowListener(shellview));
    }

    class ShellConsoleDockingWindowListener extends AbstractDockingWindowListener {
        ShellConsoleView view;

        public ShellConsoleDockingWindowListener(ShellConsoleView view){
            this.view = view;
        }

        public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
            view.dispose();
        }
    }

}
