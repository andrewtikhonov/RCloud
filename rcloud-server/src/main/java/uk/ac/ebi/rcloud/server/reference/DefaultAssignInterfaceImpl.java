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
package uk.ac.ebi.rcloud.server.reference;

import uk.ac.ebi.rcloud.server.DirectJNI;
import uk.ac.ebi.rcloud.server.ExecutionUnit;
import uk.ac.ebi.rcloud.server.RNI;
import uk.ac.ebi.rcloud.server.reference.ReferenceInterface;
import uk.ac.ebi.rcloud.server.RType.*;
import static uk.ac.ebi.rcloud.server.RConst.STRSXP;
import java.rmi.RemoteException;
import java.util.HashMap;

import uk.ac.ebi.rcloud.server.RType.REnvironment;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.RType.RVector;
import uk.ac.ebi.rcloud.server.reference.AssignInterface;
import uk.ac.ebi.rcloud.server.Utils;
import org.rosuda.JRI.Rengine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAssignInterfaceImpl implements AssignInterface {
    final private static Logger log = LoggerFactory.getLogger(DefaultAssignInterfaceImpl.class);

	public long assign(final long rObjectId, final String slotsPath, final RObject robj) throws RemoteException {
		log.info("Assigning.. to obj id " + rObjectId + " " + slotsPath + "   --> " + robj);
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (!(robj instanceof ReferenceInterface)) {
						e.rniAssign(argvar, DirectJNI.getInstance().putObject(robj), 0);
					} else {
						String argvar2 = DirectJNI.getInstance().newTemporaryVariableName();
						e.rniAssign(argvar2, ((ReferenceInterface) robj).getRObjectId(), 0);
						e.rniEval(e.rniParse(argvar + "<-" + argvar2 + ((ReferenceInterface) robj).getSlotsPath(), 1), 0);
						e.rniEval(e.rniParse("rm(" + argvar2 + ")", 1), 0);
					}

					e.rniEval(e.rniParse(rootvar + slotsPath + "<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});

		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	};

	public RObject getObjectFromReference(final RObject refObj) throws RemoteException {

		if (DirectJNI.getInstance().runRInProgress()) {
			try {
				return DirectJNI.getInstance().getObjectFromReference((ReferenceInterface) refObj);
			} catch (Exception e) {
				throw new RemoteException(Utils.getStackTraceAsString(e));
			}
		} else {
			final RObject[] robjHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			DirectJNI.getInstance().runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {

						robjHolder[0] = DirectJNI.getInstance().getObjectFromReference((ReferenceInterface) refObj);

					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null)
				throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
			return robjHolder[0];
		}

	}

    public RList getAttributes(final long rObjectId, final String slotsPath) throws RemoteException {
        final RList[] result = new RList[1];
        final Exception[] exceptionHolder = new Exception[1];
        DirectJNI.getInstance().runR(new ExecutionUnit() {
            public void run(Rengine e) {
                try {
                    String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
                    e.rniAssign(rootvar, rObjectId, 0);
                    result[0] = ((RList) DirectJNI.getInstance().getObjectFrom("attributes("+rootvar + slotsPath+")", false));
                    e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
                } catch (Exception ex) {
                    exceptionHolder[0] = ex;
                }
            }
        });

        if (exceptionHolder[0] != null)
            throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
        return result[0];

    }

    public long setAttributes(final long rObjectId, final String slotsPath, final RList attrs) throws RemoteException {
        final long[] result = new long[1];
        final Exception[] exceptionHolder = new Exception[1];
        DirectJNI.getInstance().runR(new ExecutionUnit() {
            public void run(Rengine e) {
                try {
                    String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
                    String argvar = DirectJNI.getInstance().newTemporaryVariableName();
                    e.rniAssign(rootvar, rObjectId, 0);
                    if (attrs != null) {
                        e.rniAssign(argvar, DirectJNI.getInstance().putObject(attrs), 0);
                    } else {
                        e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
                    }
                    e.rniEval(e.rniParse("attributes(" + rootvar + slotsPath + ")<-" + argvar, 1), 0);
                    result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
                    e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);


                } catch (Exception ex) {
                    exceptionHolder[0] = ex;
                }
            }
        });
        if (exceptionHolder[0] != null)
            throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
        return result[0];
    }

	public int[] getIndexNA(final long rObjectId, final String slotsPath) throws RemoteException {
		final int[][] result = new int[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					int[] isNaIdx = e.rniGetIntArray(e.rniEval(
							e.rniParse("(0:(length(" + rootvar + slotsPath + ")-1))[is.na(" + rootvar + slotsPath + ")]", 1), 0));
					result[0] = isNaIdx.length == 0 ? null : isNaIdx;
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});

		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setIndexNA(final long rObjectId, final String slotsPath, final int[] indexNA) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (indexNA != null) {
						int length = e.rniGetIntArray(e.rniEval(e.rniParse("length(" + rootvar + slotsPath + ")", 1), 0))[0];
						boolean[] naBooleans = new boolean[length];
						for (int i = 0; i < indexNA.length; ++i)
							naBooleans[indexNA[i]] = true;
						String naBooleansVar = DirectJNI.getInstance().newTemporaryVariableName();
						e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
						e.rniEval(e.rniParse("is.na(" + rootvar + slotsPath + ")<-" + naBooleansVar, 1), 0);
						e.rniEval(e.rniParse("rm(" + naBooleansVar + ")", 1), 0);
					}

					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public String[] getNames(final long rObjectId, final String slotsPath) throws RemoteException {
		final String[][] result = new String[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String[] names = null;
					long namesId = e.rniEval(e.rniParse("names(" + rootvar + slotsPath + ")", 1), 0);
					if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
						names = e.rniGetStringArray(namesId);
					}
					result[0] = names;
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setNames(final long rObjectId, final String slotsPath, final String[] names) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];

        log.info("before set name ->" + rObjectId);

		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (names != null) {
						e.rniAssign(argvar, e.rniPutStringArray(names), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse("names(" + rootvar + slotsPath + ")<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		// log.info("after set name ->" + result[0]);
		return result[0];

	}

	public String[] getValueStringArray(final long rObjectId, final String slotsPath) throws RemoteException {
		final String[][] result = new String[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.null(" + rootvar + slotsPath + ")", 1), 0))[0] == 1) {
						result[0] = null;
						return;
					}

					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetStringArray(e.rniEval(e.rniParse(rootvar + slotsPath, 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setValueStringArray(final long rObjectId, final String slotsPath, final String[] value) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();

					if (value != null) {
						e.rniAssign(argvar, e.rniPutStringArray(value), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse(rootvar + slotsPath + "<-" + argvar, 1), 0);

					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];

	}

	public boolean[] getValueBoolArray(final long rObjectId, final String slotsPath) throws RemoteException {
		final boolean[][] result = new boolean[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.null(" + rootvar + slotsPath + ")", 1), 0))[0] == 1) {
						result[0] = null;
						return;
					}
					e.rniAssign(rootvar, rObjectId, 0);
					int[] bAsInt = e.rniGetBoolArrayI(e.rniEval(e.rniParse(rootvar + slotsPath, 1), 0));
					result[0] = new boolean[bAsInt.length];
					for (int i = 0; i < bAsInt.length; ++i)
						result[0][i] = bAsInt[i] == 1;
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setValueBoolArray(final long rObjectId, final String slotsPath, final boolean[] value) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();

					if (value != null) {
						e.rniAssign(argvar, e.rniPutBoolArray(value), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse(rootvar + slotsPath + "<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public double[] getValueDoubleArray(final long rObjectId, final String slotsPath) throws RemoteException {
		final double[][] result = new double[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.null(" + rootvar + slotsPath + ")", 1), 0))[0] == 1) {
						result[0] = null;
						return;
					}

					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetDoubleArray(e.rniEval(e.rniParse(rootvar + slotsPath, 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setValueDoubleArray(final long rObjectId, final String slotsPath, final double[] value) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();

					if (value != null) {
						e.rniAssign(argvar, e.rniPutDoubleArray(value), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse(rootvar + slotsPath + "<-" + argvar, 1), 0);

					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public int[] getValueIntArray(final long rObjectId, final String slotsPath) throws RemoteException {
		final int[][] result = new int[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.null(" + rootvar + slotsPath + ")", 1), 0))[0] == 1) {
						result[0] = null;
						return;
					}

					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetIntArray(e.rniEval(e.rniParse(rootvar + slotsPath, 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setValueIntArray(final long rObjectId, final String slotsPath, final int[] value) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();

					if (value != null) {
						e.rniAssign(argvar, e.rniPutIntArray(value), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse(rootvar + slotsPath + "<-" + argvar, 1), 0);

					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];

	}

	public double[] getValueCPReal(final long rObjectId, final String slotsPath) throws RemoteException {
		final double[][] result = new double[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.null(" + rootvar + slotsPath + ")", 1), 0))[0] == 1) {
						result[0] = null;
						return;
					}

					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetDoubleArray(e.rniEval(e.rniParse("Re(" + rootvar + slotsPath + ")", 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public double[] getValueCPImaginary(final long rObjectId, final String slotsPath) throws RemoteException {
		final double[][] result = new double[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.null(" + rootvar + slotsPath + ")", 1), 0))[0] == 1) {
						result[0] = null;
						return;
					}

					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetDoubleArray(e.rniEval(e.rniParse("Im(" + rootvar + slotsPath + ")", 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setValueCP(final long rObjectId, final String slotsPath, final double[] real, final double[] imaginary) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();

					if (real != null && imaginary != null) {

						String v_temp_1 = DirectJNI.getInstance().newTemporaryVariableName();
						String v_temp_2 = DirectJNI.getInstance().newTemporaryVariableName();
						e.rniAssign(v_temp_1, e.rniPutDoubleArray(real), 0);
						e.rniAssign(v_temp_2, e.rniPutDoubleArray(imaginary), 0);
						e.rniAssign(argvar, e.rniEval(e.rniParse(v_temp_1 + "+1i*" + v_temp_2, 1), 0), 0);
						e.rniEval(e.rniParse("rm(" + v_temp_1 + "," + v_temp_2 + ")", 1), 0);

					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}
					e.rniEval(e.rniParse(rootvar + slotsPath + "<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public int length(final long rObjectId, final String slotsPath) throws RemoteException {

		final int[] result = new int[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetIntArray(e.rniEval(e.rniParse("length(" + rootvar + slotsPath + ")", 1), 0))[0];
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});

		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public RVector getArrayValue(final long rObjectId, final String slotsPath) throws RemoteException {
		final RVector[] result = new RVector[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = ((RArray) DirectJNI.getInstance().getObjectFrom(rootvar + slotsPath, true)).getValue();
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setArrayValue(final long rObjectId, final String slotsPath, final RVector value) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					e.rniAssign(argvar, DirectJNI.getInstance().putObject(
							value instanceof ReferenceInterface ? (RList) ((ReferenceInterface) value).extractRObject() : value), 0);
					e.rniEval(e.rniParse("dim(" + argvar + ")<-" + "dim(" + rootvar + slotsPath + ")", 1), 0);
					e.rniEval(e.rniParse("dimnames(" + argvar + ")<-" + "dimnames(" + rootvar + slotsPath + ")", 1), 0);
					result[0] = e.rniEval(e.rniParse(argvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];

	}

	public int[] getArrayDim(final long rObjectId, final String slotsPath) throws RemoteException {
		final int[][] result = new int[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					result[0] = e.rniGetIntArray(e.rniEval(e.rniParse("dim(" + rootvar + slotsPath + ")", 1), 0));

					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setArrayDim(final long rObjectId, final String slotsPath, final int[] dim) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					if (dim != null) {
						e.rniAssign(argvar, e.rniPutIntArray(dim), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse("dim(" + rootvar + slotsPath + ")<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	public RList getArrayDimnames(final long rObjectId, final String slotsPath) throws RemoteException {
		final RList[] result = new RList[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = (RList) DirectJNI.getInstance().getObjectFrom("dimnames(" + rootvar + slotsPath + ")", true);
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];

	}

	public long setArrayDimnames(final long rObjectId, final String slotsPath, final RList dimnames) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					if (dimnames != null) {
						e.rniAssign(argvar, DirectJNI.getInstance().putObject(dimnames), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}

					e.rniEval(e.rniParse("dimnames(" + rootvar + slotsPath + ")<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];

	}

	// Factors
	public String[] factorAsData(final long rObjectId, final String slotsPath) throws RemoteException {
		final String[][] result = new String[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetStringArray(e.rniEval(e.rniParse("as.character(" + rootvar + slotsPath + ")", 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public int[] getFactorCode(final long rObjectId, final String slotsPath) throws RemoteException {
		final int[][] result = new int[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetIntArray(e.rniEval(e.rniParse("as.integer(" + rootvar + slotsPath + ")", 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public String[] getFactorLevels(final long rObjectId, final String slotsPath) throws RemoteException {
		final String[][] result = new String[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = e.rniGetStringArray(e.rniEval(e.rniParse("levels(" + rootvar + slotsPath + ")", 1), 0));
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];

	}

	public long setFactorCode(final long rObjectId, final String slotsPath, final int[] code) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					e.rniAssign(argvar, DirectJNI.getInstance().putObject(new RFactor(null, code)), 0);
					e.rniEval(e.rniParse("levels(" + argvar + ")<-" + "levels(" + rootvar + slotsPath + ")", 1), 0);
					result[0] = e.rniEval(e.rniParse(argvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	public long setFactorLevels(final long rObjectId, final String slotsPath, final String[] levels) throws RemoteException {

		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					if (levels != null) {
						e.rniAssign(argvar, e.rniPutStringArray(levels), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}
					e.rniEval(e.rniParse("levels(" + rootvar + slotsPath + ")<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	// Dataframes
	public RList getDataframeData(final long rObjectId, final String slotsPath) throws RemoteException {

		final RList[] result = new RList[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = ((RDataFrame) DirectJNI.getInstance().getObjectFrom(rootvar + slotsPath, true)).getData();
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];

	}

	public String[] getDataframeRowNames(final long rObjectId, final String slotsPath) throws RemoteException {
		final String[][] result = new String[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);

					result[0] = e.rniGetStringArray(e.rniEval(e.rniParse("row.names(" + rootvar + slotsPath + ")", 1), 0));

					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setDataframeData(final long rObjectId, final String slotsPath, final RList data) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					e.rniAssign(argvar, DirectJNI.getInstance().putObject(
							new RDataFrame(data instanceof ReferenceInterface ? (RList) ((ReferenceInterface) data).extractRObject() : data, null)), 0);
					e.rniEval(e.rniParse("row.names(" + argvar + ")<-" + "row.names(" + rootvar + slotsPath + ")", 1), 0);
					result[0] = e.rniEval(e.rniParse(argvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	public long setDataframeRowNames(final long rObjectId, final String slotsPath, final String[] rowNames) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					if (rowNames != null) {
						e.rniAssign(argvar, e.rniPutStringArray(rowNames), 0);
					} else {
						e.rniEval(e.rniParse(argvar + "<-NULL", 1), 0);
					}
					e.rniEval(e.rniParse("row.names(" + rootvar + slotsPath + ")<-" + argvar, 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	// Lists
	public RObject[] getListValue(final long rObjectId, final String slotsPath) throws RemoteException {
		final RObject[][] result = new RObject[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = ((RList) DirectJNI.getInstance().getObjectFrom(rootvar + slotsPath, true)).getValue();
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setListValue(final long rObjectId, final String slotsPath, final RObject[] value) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					RObject[] plainvalue = new RObject[value.length];
					for (int i = 0; i < value.length; ++i) {
						if (value[i] instanceof ReferenceInterface) {
							plainvalue[i] = ((ReferenceInterface) value[i]).extractRObject();
						} else {
							plainvalue[i] = value[i];
						}
					}

					e.rniAssign(argvar, DirectJNI.getInstance().putObject(new RList(plainvalue, null)), 0);
					e.rniEval(e.rniParse("names(" + argvar + ")<-" + "names(" + rootvar + slotsPath + ")", 1), 0);
					result[0] = e.rniEval(e.rniParse(argvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	public HashMap getEnvData(final long rObjectId, final String slotsPath) throws RemoteException {
		final HashMap[] result = new HashMap[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					result[0] = ((REnvironment) DirectJNI.getInstance().getObjectFrom(rootvar + slotsPath, true)).getData();
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long putEnv(final long rObjectId, final String slotsPath, final String theKey, final RObject theValue) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					String argvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					e.rniAssign(argvar, DirectJNI.getInstance().putObject(
							theValue instanceof ReferenceInterface ? ((ReferenceInterface) theValue).extractRObject() : theValue), 0);
					e.rniEval(e.rniParse("assign('" + theKey + "'," + argvar + ", env=" + rootvar + slotsPath + ")", 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + "," + argvar + ")", 1), 0);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	public long setEnvData(final long rObjectId, final String slotsPath, final HashMap<String, RObject> data) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					HashMap<String, RObject> plaindata = new HashMap<String, RObject>();
					for (String key : data.keySet()) {
						RObject o = data.get(key);
						if (o instanceof ReferenceInterface)
							plaindata.put(key, ((ReferenceInterface) o).extractRObject());
						else
							plaindata.put(key, o);
					}
					REnvironment env = new REnvironment();
					env.setData(plaindata);
					result[0] = DirectJNI.getInstance().putObject(env);
				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));

		return result[0];
	}

	public RNI getRNI() throws RemoteException {
		return DirectJNI.getInstance().getRNI();
	}

	public String getName() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getServantName();
	}

	public String[] getS3ClassAttribute(final long rObjectId, final String slotsPath) throws RemoteException {
		final String[][] result = new String[1][];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String[] comment = null;
					long commentId = e.rniEval(e.rniParse("class(" + rootvar + slotsPath + ")", 1), 0);
					if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
						comment = e.rniGetStringArray(commentId);
					}
					result[0] = comment;
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});

		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

	public long setS3ClassAttribute(final long rObjectId, final String slotsPath, final String[] classAttribute) throws RemoteException {
		final long[] result = new long[1];
		final Exception[] exceptionHolder = new Exception[1];
		DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {

					String rootvar = DirectJNI.getInstance().newTemporaryVariableName();
					e.rniAssign(rootvar, rObjectId, 0);
					String ca="c(";
					for (int i=0; i<classAttribute.length;++i) {
						ca+="'"+classAttribute[i]+"'"+(i==classAttribute.length-1 ? "" : ",");
					}
					ca+=")";
					e.rniEval(e.rniParse("class(" + rootvar + slotsPath + ")<-" + (classAttribute == null ? "NULL" : ca), 1), 0);
					result[0] = e.rniEval(e.rniParse(rootvar, 1), 0);
					e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

				} catch (Exception ex) {
					exceptionHolder[0] = ex;
				}
			}
		});
		if (exceptionHolder[0] != null)
			throw new RemoteException(Utils.getStackTraceAsString(exceptionHolder[0]));
		return result[0];
	}

}
