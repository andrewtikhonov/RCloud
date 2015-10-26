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
package uk.ac.ebi.rcloud.server.graphics;


import java.awt.Point;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import uk.ac.ebi.rcloud.server.DirectJNI;
import uk.ac.ebi.rcloud.server.graphics.GDContainer;
import uk.ac.ebi.rcloud.server.graphics.GraphicNotifier;

public class GraphicNotifierImpl extends UnicastRemoteObject implements GraphicNotifier {
	public GraphicNotifierImpl() throws RemoteException {
		super();
	}

	public void fireSizeChangedEvent(final int devNr) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().fireSizeChangedEvent(devNr);
	}

	public void registerContainer(GDContainer container) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().registerContainer(container);
	}

	public void executeDevOff(int devNr) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().executeDevOff(devNr);
	}

	public void putLocation(Point p) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().putLocation(p);
	}

}