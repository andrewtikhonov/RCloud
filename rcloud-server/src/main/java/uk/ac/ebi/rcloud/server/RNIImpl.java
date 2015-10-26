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

import java.rmi.RemoteException;

import uk.ac.ebi.rcloud.server.DirectJNI;
import uk.ac.ebi.rcloud.server.RNI;


public class RNIImpl extends java.rmi.server.UnicastRemoteObject implements RNI {

	private String _status;
	StringBuffer _log = null;

	public RNIImpl(StringBuffer log) throws RemoteException {
		super();
		_log = log;
	}

	public void rniAssign(String name, long exp, long rho) throws RemoteException {
		DirectJNI.getInstance().getRServices().getRNI().rniAssign(name, exp, rho);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
	}

	public long rniCAR(long exp) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniCAR(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniCDR(long exp) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniCDR(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniCons(long head, long tail) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniCons(head, tail);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniEval(long exp, long rho) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniEval(exp, rho);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public int rniExpType(long exp) throws RemoteException {
		int result = DirectJNI.getInstance().getRServices().getRNI().rniExpType(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniGetAttr(long exp, String name) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniGetAttr(exp, name);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public int[] rniGetBoolArrayI(long exp) throws RemoteException {
		int[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetBoolArrayI(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public double[] rniGetDoubleArray(long exp) throws RemoteException {
		double[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetDoubleArray(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public int[] rniGetIntArray(long exp) throws RemoteException {
		int[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetIntArray(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long[] rniGetList(long exp) throws RemoteException {
		long[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetList(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public String rniGetString(long exp) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().getRNI().rniGetString(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public String[] rniGetStringArray(long exp) throws RemoteException {
		String[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetStringArray(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public String rniGetSymbolName(long sym) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().getRNI().rniGetSymbolName(sym);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long[] rniGetVector(long exp) throws RemoteException {
		long[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetVector(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniGetVersion() throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniGetVersion();
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public boolean rniInherits(long exp, String cName) throws RemoteException {
		boolean result = DirectJNI.getInstance().getRServices().getRNI().rniInherits(exp, cName);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniInstallSymbol(String sym) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniInstallSymbol(sym);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniParse(String s, int parts) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniParse(s, parts);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public void rniProtect(long exp) throws RemoteException {
		DirectJNI.getInstance().getRServices().getRNI().rniProtect(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
	}

	public long rniPutBoolArray(boolean[] a) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutBoolArray(a);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutBoolArrayI(int[] a) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutBoolArrayI(a);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutDoubleArray(double[] a) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutDoubleArray(a);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutIntArray(int[] a) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutIntArray(a);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutList(long[] cont) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutList(cont);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutString(String s) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutString(s);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutStringArray(String[] a) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutStringArray(a);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniPutVector(long[] exps) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniPutVector(exps);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public void rniSetAttr(long exp, String name, long attr) throws RemoteException {
		DirectJNI.getInstance().getRServices().getRNI().rniSetAttr(exp, name, attr);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
	}

	public int rniStop(int flag) throws RemoteException {
		int result = DirectJNI.getInstance().getRServices().getRNI().rniStop(flag);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public long rniTAG(long exp) throws RemoteException {
		long result = DirectJNI.getInstance().getRServices().getRNI().rniTAG(exp);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		return result;
	}

	public void rniUnprotect(int count) throws RemoteException {
		DirectJNI.getInstance().getRServices().getRNI().rniUnprotect(count);
		_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
	}

	public String getStatus() throws RemoteException {
		return _status;
	}
}