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
import java.awt.geom.Point2D;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;


public interface GDDevice extends Remote {
	public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException;

	public boolean hasGraphicObjects() throws RemoteException;

	void fireSizeChangedEvent(int w, int h) throws RemoteException;

	public void dispose() throws RemoteException;

	public Dimension getSize() throws RemoteException;

	public void putLocation(Point2D p) throws RemoteException;

	public boolean hasLocations() throws RemoteException;

	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException;

	public int getDeviceNumber() throws RemoteException;

	public boolean isCurrentDevice() throws RemoteException;

	public void setAsCurrentDevice() throws RemoteException;

	public Vector<String> getSVGAsText() throws RemoteException;
	
	public byte[] getSVG() throws RemoteException;

	public byte[] getPostScript() throws RemoteException;

	public byte[] getPdf() throws RemoteException;

	public byte[] getPictex() throws RemoteException;

	public byte[] getPng() throws RemoteException;

	public byte[] getJpg() throws RemoteException;

	public byte[] getBmp() throws RemoteException;

	public byte[] getXfig() throws RemoteException;
	
	public String getId() throws RemoteException;
	
	public boolean isBroadcasted() throws RemoteException;
	
	public void broadcast() throws RemoteException;

    public void addGraphicListener(GDObjectListener objectListener) throws RemoteException;

    public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException;

}
