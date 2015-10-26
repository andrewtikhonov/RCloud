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

import java.rmi.Remote;

public interface RNI extends Remote {

	public long rniParse(String s, int parts) throws java.rmi.RemoteException;

	public long rniEval(long exp, long rho) throws java.rmi.RemoteException;

	public void rniProtect(long exp) throws java.rmi.RemoteException;

	public void rniUnprotect(int count) throws java.rmi.RemoteException;

	public String rniGetString(long exp) throws java.rmi.RemoteException;

	public String[] rniGetStringArray(long exp) throws java.rmi.RemoteException;

	public int[] rniGetIntArray(long exp) throws java.rmi.RemoteException;

	public int[] rniGetBoolArrayI(long exp) throws java.rmi.RemoteException;

	public double[] rniGetDoubleArray(long exp) throws java.rmi.RemoteException;

	public long[] rniGetVector(long exp) throws java.rmi.RemoteException;

	public long rniPutString(String s) throws java.rmi.RemoteException;

	public long rniPutStringArray(String[] a) throws java.rmi.RemoteException;

	public long rniPutIntArray(int[] a) throws java.rmi.RemoteException;

	public long rniPutBoolArrayI(int[] a) throws java.rmi.RemoteException;

	public long rniPutBoolArray(boolean[] a) throws java.rmi.RemoteException;

	public long rniPutDoubleArray(double[] a) throws java.rmi.RemoteException;

	public long rniPutVector(long[] exps) throws java.rmi.RemoteException;

	public long rniGetAttr(long exp, String name) throws java.rmi.RemoteException;

	public void rniSetAttr(long exp, String name, long attr) throws java.rmi.RemoteException;

	public boolean rniInherits(long exp, String cName) throws java.rmi.RemoteException;

	public long rniCons(long head, long tail) throws java.rmi.RemoteException;

	public long rniCAR(long exp) throws java.rmi.RemoteException;

	public long rniCDR(long exp) throws java.rmi.RemoteException;

	public long rniTAG(long exp) throws java.rmi.RemoteException;

	public long rniPutList(long[] cont) throws java.rmi.RemoteException;

	public long[] rniGetList(long exp) throws java.rmi.RemoteException;

	public String rniGetSymbolName(long sym) throws java.rmi.RemoteException;

	public long rniInstallSymbol(String sym) throws java.rmi.RemoteException;

	public long rniGetVersion() throws java.rmi.RemoteException;

	public int rniStop(int flag) throws java.rmi.RemoteException;

	public void rniAssign(String name, long exp, long rho) throws java.rmi.RemoteException;

	public int rniExpType(long exp) throws java.rmi.RemoteException;

	public String getStatus() throws java.rmi.RemoteException;

}
