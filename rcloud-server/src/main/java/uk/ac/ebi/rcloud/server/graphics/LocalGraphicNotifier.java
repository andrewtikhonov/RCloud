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


import java.awt.Dimension;
import java.awt.Point;
import java.rmi.RemoteException;

import uk.ac.ebi.rcloud.server.DirectJNI;
import uk.ac.ebi.rcloud.server.graphics.GDContainer;
import uk.ac.ebi.rcloud.server.graphics.GraphicNotifier;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JavaGD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalGraphicNotifier implements GraphicNotifier {
    final private Logger log = LoggerFactory.getLogger(getClass());
    
	public void fireSizeChangedEvent(final int devNr) throws RemoteException {
        log.info(DirectJNI.getInstance().getRServices().evaluate(
                "try(.C(\"javaGDresize\",as.integer(" + devNr + ")),silent=TRUE)"));
	}

	public void registerContainer(GDContainer container) throws RemoteException {
		JavaGD.setGDContainer(container);
		Dimension dim = container.getSize();
        log.info(DirectJNI.getInstance().getRServices().evaluate(
                "JavaGD(name='JavaGD', width=" + dim.getWidth() + ", height=" + dim.getHeight() + ", ps=12)"));
	}

	public void executeDevOff(int devNr) throws RemoteException {
        log.info(DirectJNI.getInstance().getRServices().evaluate(
                "try({ .PrivateEnv$dev.set(" + (devNr + 1) + "); .PrivateEnv$dev.off()},silent=TRUE)"));
	}

	public void putLocation(Point p) throws RemoteException {
		GDInterface.putLocatorLocation(p);
	}

};
