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

import java.rmi.RemoteException;

import org.rosuda.ibase.NotifierInterface;
import org.rosuda.ibase.SCatSequence;
import org.rosuda.ibase.SMarkerInterface;

public interface SVarInterfaceRemote extends NotifierInterfaceRemote {
	public int[] getRanked()  throws RemoteException;
	public int[] getRanked(SMarkerInterface m, int markspec) throws RemoteException;
	public int getContentsType() throws RemoteException;
	public void categorize() throws RemoteException;
	public NotifierInterface getNotifier() throws RemoteException;
	public Object elementAt(int i) throws RemoteException;
	public boolean isSelected() throws RemoteException;	
	public boolean add(Object o) throws RemoteException;
	public boolean add(double d) throws RemoteException;
	public boolean add(int d) throws RemoteException;  		
	public Object[] getCategories() throws RemoteException;
	public double getMin() throws RemoteException;
	public double getMax() throws RemoteException;
	public boolean isNum() throws RemoteException;
	public boolean isCat() throws RemoteException;
	public boolean isEmpty() throws RemoteException;
	public String getName() throws RemoteException;
	public SCatSequence mainSeq() throws RemoteException;
	public boolean hasMissing() throws RemoteException;
	public boolean isMissingAt(int i) throws RemoteException;
	public int getMissingCount() throws RemoteException;
	public int getCatIndex(int i) throws RemoteException;
	public int getCatIndex(Object o) throws RemoteException;
	public int atI(int i) throws RemoteException;
	public double atF(int i) throws RemoteException;
	public double atD(int i) throws RemoteException;
	public String atS(int i) throws RemoteException;
	public Object at(int i) throws RemoteException;
	public Object[] at(int start, int end) throws RemoteException;
	public int size() throws RemoteException;
	public int getNumCats() throws RemoteException;
	public int getSizeCatAt(int i) throws RemoteException;
	public Object getCatAt(int i) throws RemoteException;
	public boolean isLinked() throws RemoteException;
}
