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

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;

public interface GDContainer extends Remote {

	public void add(GDObject o) throws RemoteException;

	public void reset() throws RemoteException;

	//public void repaint();
	//public void repaint(long tm);        

	public void syncDisplay(boolean finish) throws RemoteException;

	public void setDeviceNumber(int dn) throws RemoteException;

	public void closeDisplay() throws RemoteException;

	public void setGFont(Font f) throws RemoteException;

	public int getDeviceNumber() throws RemoteException;

	public Dimension getSize() throws RemoteException;

	public Font getGFont() throws RemoteException;

	public FontMetrics getGFontMetrics() throws RemoteException;

    public void addGraphicListener(GDObjectListener objectListener) throws RemoteException;

    public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException;

}
