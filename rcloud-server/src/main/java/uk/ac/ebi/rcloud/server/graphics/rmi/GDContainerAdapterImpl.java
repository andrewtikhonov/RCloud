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
package uk.ac.ebi.rcloud.server.graphics.rmi;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import uk.ac.ebi.rcloud.server.graphics.GDContainer;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;

public class GDContainerAdapterImpl extends UnicastRemoteObject implements GDContainer {

	private GDContainer _gdContainer;

	public GDContainerAdapterImpl(GDContainer gdContainer) throws RemoteException {
		super();
		_gdContainer = gdContainer;
		// log.info(_gdContainer.getClass().getName());
	}

	public void add(GDObject o) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.add(o);
		else
			throw new RemoteException("No GD Container connected");
	}

	public void closeDisplay() throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.closeDisplay();
		else
			throw new RemoteException("No GD Container connected");
	}

	public int getDeviceNumber() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getDeviceNumber();
		else
			throw new RemoteException("No GD Container connected");
	}

	public void setGFont(Font f) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.setGFont(f);
		else
			throw new RemoteException("No GD Container connected");
	}

	public Font getGFont() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getGFont();
		else
			throw new RemoteException("No GD Container connected");
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getGFontMetrics();
		else
			throw new RemoteException("No GD Container connected");
	}

	public Dimension getSize() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getSize();
		else
			throw new RemoteException("No GD Container connected");
	}

	public void reset() throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.reset();
		else
			throw new RemoteException("No GD Container connected");

	}

	public void setDeviceNumber(int dn) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.setDeviceNumber(dn);
		else
			throw new RemoteException("No GD Container connected");
	}

	public void syncDisplay(boolean finish) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.syncDisplay(finish);
		else
			throw new RemoteException("No GD Container connected");
	}

    public void addGraphicListener(GDObjectListener objectListener) throws RemoteException {
        if (_gdContainer != null)
            _gdContainer.addGraphicListener(objectListener);
        else
            throw new RemoteException("No GD Container connected");
    }

    public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException {
        if (_gdContainer != null)
            _gdContainer.removeGraphicListener(objectListener);
        else
            throw new RemoteException("No GD Container connected");
    }

}
