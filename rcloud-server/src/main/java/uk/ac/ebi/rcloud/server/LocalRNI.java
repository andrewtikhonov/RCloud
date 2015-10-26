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
import uk.ac.ebi.rcloud.server.ExecutionUnit;
import uk.ac.ebi.rcloud.server.RNI;
import org.rosuda.JRI.Rengine;


public class LocalRNI implements RNI {

	DirectJNI _jni;
	String _status;

	public LocalRNI(DirectJNI jni) {
		_jni = jni;
	}

	public void rniAssign(final String name, final long exp, final long rho) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniAssign(name, exp, rho);
			}
		});
	}

	public long rniCAR(final long exp) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniCAR(exp);
			}
		});
		return holder[0];
	}

	public long rniCDR(final long exp) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniCDR(exp);
			}
		});
		return holder[0];
	}

	public long rniCons(final long head, final long tail) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniCons(head, tail);
			}
		});
		return holder[0];
	}

	public long rniEval(final long exp, final long rho) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniEval(exp, rho);
			}
		});
		return holder[0];
	}

	public int rniExpType(final long exp) throws RemoteException {
		final int[] holder = new int[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniExpType(exp);
			}
		});
		return holder[0];
	}

	public long rniGetAttr(final long exp, final String name) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetAttr(exp, name);
			}
		});
		return holder[0];
	}

	public int[] rniGetBoolArrayI(final long exp) throws RemoteException {
		final int[][] holder = new int[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetBoolArrayI(exp);
			}
		});
		return holder[0];
	}

	public double[] rniGetDoubleArray(final long exp) throws RemoteException {
		final double[][] holder = new double[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetDoubleArray(exp);
			}
		});
		return holder[0];
	}

	public int[] rniGetIntArray(final long exp) throws RemoteException {
		final int[][] holder = new int[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetIntArray(exp);
			}
		});
		return holder[0];
	}

	public long[] rniGetList(final long exp) throws RemoteException {
		final long[][] holder = new long[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetList(exp);
			}
		});
		return holder[0];
	}

	public String rniGetString(final long exp) throws RemoteException {
		final String[] holder = new String[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetString(exp);
			}
		});
		return holder[0];
	}

	public String[] rniGetStringArray(final long exp) throws RemoteException {
		final String[][] holder = new String[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetStringArray(exp);
			}
		});
		return holder[0];
	}

	public String rniGetSymbolName(final long sym) throws RemoteException {
		final String[] holder = new String[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetSymbolName(sym);
			}
		});
		return holder[0];
	}

	public long[] rniGetVector(final long exp) throws RemoteException {
		final long[][] holder = new long[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetList(exp);
			}
		});
		return holder[0];
	}

	public long rniGetVersion() throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = Rengine.rniGetVersion();
			}
		});
		return holder[0];
	}

	public boolean rniInherits(final long exp, final String cName) throws RemoteException {
		final boolean[] holder = new boolean[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniInherits(exp, cName);
			}
		});
		return holder[0];
	}

	public long rniInstallSymbol(final String sym) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniInstallSymbol(sym);
			}
		});
		return holder[0];
	}

	public long rniParse(final String s, final int parts) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniParse(s, parts);
			}
		});
		return holder[0];
	}

	public void rniProtect(final long exp) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniProtect(exp);
			}
		});
	}

	public long rniPutBoolArray(final boolean[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutBoolArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutBoolArrayI(final int[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutBoolArrayI(a);
			}
		});
		return holder[0];
	}

	public long rniPutDoubleArray(final double[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutDoubleArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutIntArray(final int[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutIntArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutList(final long[] cont) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutList(cont);
			}
		});
		return holder[0];
	}

	public long rniPutString(final String s) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutString(s);
			}
		});
		return holder[0];
	}

	public long rniPutStringArray(final String[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutStringArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutVector(final long[] exps) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutVector(exps);
			}
		});
		return holder[0];
	}

	public void rniSetAttr(final long exp, final String name, final long attr) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniSetAttr(exp, name, attr);
			}
		});
	}

	public int rniStop(final int flag) throws RemoteException {
		final int[] holder = new int[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniStop(flag);
			}
		});
		return holder[0];
	}

	public long rniTAG(final long exp) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniTAG(exp);
			}
		});
		return holder[0];
	}

	public void rniUnprotect(final int count) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniUnprotect(count);
			}
		});
	}

	public String getStatus() throws RemoteException {
		return _status;
	}
}
