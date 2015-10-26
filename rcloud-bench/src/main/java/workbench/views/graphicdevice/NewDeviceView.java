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
package workbench.views.graphicdevice;

import workbench.graphics.GDContainerPanel;
import workbench.views.DynamicView;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 3, 2010
 * Time: 5:12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewDeviceView extends DynamicView {
	GDContainerPanel panel;

	public NewDeviceView(String title, Icon icon, Component component, int id) {
		super(title, icon, component, id);
	}

	public GDContainerPanel getPanel() {
		return panel;
	}

	public void setPanel(GDContainerPanel panel) {
		this.panel = panel;
	}
}
