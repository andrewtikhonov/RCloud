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
package org.rosuda.ibase;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

import uk.ac.ebi.rcloud.server.iplots.NotifierInterfaceRemote;
import uk.ac.ebi.rcloud.server.iplots.SVarSetInterfaceRemote;

public interface SMarkerInterfaceRemote extends NotifierInterfaceRemote, CommanderRemote{
	public void resize(int newsize) throws RemoteException;
    public int size() throws RemoteException;
    public int marked() throws RemoteException;
    public int get(int pos) throws RemoteException;
    public int getSec(int pos) throws RemoteException;
    public boolean at(int pos) throws RemoteException;
    public Vector getList() throws RemoteException;
    public int[] getSelectedIDs() throws RemoteException;
    public int[] getMaskCopy(int maskType) throws RemoteException;
    public void set(int pos, boolean pMark) throws RemoteException;
    public void setSec(int pos, int mark) throws RemoteException;
    public void setSelected(int mark) throws RemoteException;
    public int getMaxMark() throws RemoteException;
	public int getSecCount() throws RemoteException;
    public Enumeration elements() throws RemoteException;
    public void selectNone() throws RemoteException;
    public void selectAll() throws RemoteException;	
    public void selectInverse() throws RemoteException;
	public void resetSec() throws RemoteException;
    public SVarSetInterfaceRemote getMasterSet() throws RemoteException;
    public void setSecBySelection(int markSel, int markNonsel) throws RemoteException;
}
