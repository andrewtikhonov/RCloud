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
package uk.ac.ebi.rcloud.server;

import uk.ac.ebi.rcloud.server.callback.GenericRActionListener;
import uk.ac.ebi.rcloud.server.callback.RAction;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;


public interface Device extends Remote {
	public void dispose() throws RemoteException;
	public String getId() throws RemoteException;
    public void addListener(GenericRActionListener listener) throws RemoteException;
    public void removeListener(GenericRActionListener listener) throws RemoteException;
}
