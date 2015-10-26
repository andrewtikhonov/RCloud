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
package uk.ac.ebi.rcloud.server.iplots;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.rosuda.ibase.SMarkerInterfaceRemote;

public interface SVarSetInterfaceRemote extends Remote{
	
	public int count() throws RemoteException;
	public SVarInterfaceRemote at(int i) throws RemoteException;
	public SVarInterfaceRemote byName(String nam) throws RemoteException;
	public int indexOf(String nam) throws RemoteException;	
	public String getName() throws RemoteException;	
    public void setName(String s) throws RemoteException;        
    public SMarkerInterfaceRemote getMarker() throws RemoteException;    
    
}
