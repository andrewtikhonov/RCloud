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

import java.awt.event.ActionEvent;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 2, 2010
 * Time: 1:08:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpenFunctionAction extends InvokeEditorAction {
    final private static Logger log = LoggerFactory.getLogger(OpenFunctionAction.class);

    private RGui rgui;

    public OpenFunctionAction(RGui rgui, String name){
        super(rgui, name);
        this.rgui = rgui;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String function     = event.getActionCommand();

        invokeBasicEditor(function, function, null);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
