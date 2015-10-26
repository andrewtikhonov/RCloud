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
package uk.ac.ebi.rcloud.server.graphics.utils;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.Vector;

import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;

public class BufferDevice implements GDDevice {
	private Dimension _dim;
	private Vector<GDObject> _l;

	public BufferDevice(Vector<GDObject> l, Dimension dim) {
		_l = l;
		_dim = dim;
	}

	public void dispose() throws RemoteException {
	}

	public void fireSizeChangedEvent(int w, int h) throws RemoteException {
	}

	public Dimension getSize() throws RemoteException {
		return _dim;
	}

	public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException {
		return _l;
	}

	public boolean hasGraphicObjects() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public void putLocation(Point2D p) throws RemoteException {
	}

	public boolean hasLocations() throws RemoteException {

		return false;
	}

	public int getDeviceNumber() throws RemoteException {
		return -1;
	}

	public boolean isCurrentDevice() throws RemoteException {
		return false;
	}

	public void setAsCurrentDevice() throws RemoteException {
	}

	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
		return null;
	}

	public Vector<String> getSVGAsText() throws RemoteException {
		return null;
	}

	public Vector<String> getSVGAsBytes() throws RemoteException {
		return null;
	}
	
	public byte[] getSVG() throws RemoteException {
		return null;
	}
	
	public byte[] getBmp() throws RemoteException {
		return null;
	}

	public byte[] getJpg() throws RemoteException {
		return null;
	}

	public byte[] getPdf() throws RemoteException {
		return null;
	}

	public byte[] getPictex() throws RemoteException {
		return null;
	}

	public byte[] getPng() throws RemoteException {
		return null;
	}

	public byte[] getPostScript() throws RemoteException {
		return null;
	}

	public byte[] getXfig() throws RemoteException {
		return null;
	}
	
	public String getId() throws RemoteException {
		return null;
	}
	
	public boolean isBroadcasted() throws RemoteException {
		return false;
	}
	
	public void broadcast() throws RemoteException {
		
	}

    public void addGraphicListener(GDObjectListener objectListener) throws RemoteException {

    }

    public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException {

    }

}
