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

import java.rmi.RemoteException;
import java.util.HashMap;

import uk.ac.ebi.rcloud.server.DirectJNI;
import uk.ac.ebi.rcloud.server.RServantImpl;
import uk.ac.ebi.rcloud.server.reference.AssignInterface;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.RType.RVector;
import uk.ac.ebi.rcloud.server.RNI;


public class AssignInterfaceImpl extends java.rmi.server.UnicastRemoteObject implements AssignInterface {
	RServantImpl _rservantImpl = null;

	public AssignInterfaceImpl(RServantImpl rservantImpl) throws RemoteException {
		super();
		_rservantImpl = rservantImpl;
	}

	public long assign(long rObjectId, String slotsPath, RObject robj) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().assign(rObjectId, slotsPath, robj);
	}

	public RObject getObjectFromReference(RObject refObj) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getObjectFromReference(refObj);
	}

	public String[] getNames(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getNames(rObjectId, slotsPath);
	}

	public long setNames(long rObjectId, String slotsPath, String[] names) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setNames(rObjectId, slotsPath, names);
	}

	public int length(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().length(rObjectId, slotsPath);
	}

	public String[] getValueStringArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueStringArray(rObjectId, slotsPath);
	}

	public long setValueStringArray(long rObjectId, String slotsPath, String[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueStringArray(rObjectId, slotsPath, value);
	}

	public boolean[] getValueBoolArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueBoolArray(rObjectId, slotsPath);
	}

	public long setValueBoolArray(long rObjectId, String slotsPath, boolean[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueBoolArray(rObjectId, slotsPath, value);
	}

	public double[] getValueDoubleArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueDoubleArray(rObjectId, slotsPath);
	}

	public long setValueDoubleArray(long rObjectId, String slotsPath, double[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueDoubleArray(rObjectId, slotsPath, value);
	}

	public int[] getValueIntArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueIntArray(rObjectId, slotsPath);
	}

	public long setValueIntArray(long rObjectId, String slotsPath, int[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueIntArray(rObjectId, slotsPath, value);
	}

	public double[] getValueCPImaginary(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueCPImaginary(rObjectId, slotsPath);
	}

	public double[] getValueCPReal(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueCPReal(rObjectId, slotsPath);
	}

	public long setValueCP(long rObjectId, String slotsPath, double[] real, double[] imaginary) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueCP(rObjectId, slotsPath, real, imaginary);
	}

	public RNI getRNI() throws RemoteException {
		return _rservantImpl.getRNI();
	}

	public String getName() throws RemoteException {
		return _rservantImpl.getServantName();
	}

	public int[] getIndexNA(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getIndexNA(rObjectId, slotsPath);
	}

	public long setIndexNA(long rObjectId, String slotsPath, int[] indexNA) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setIndexNA(rObjectId, slotsPath, indexNA);
	}

    public RList getAttributes(long rObjectId, String slotsPath) throws RemoteException {
        return DirectJNI.getInstance().getDefaultAssignInterface().getAttributes(rObjectId, slotsPath);
    }

    public long setAttributes(long rObjectId, String slotsPath, RList attr) throws RemoteException {
        return DirectJNI.getInstance().getDefaultAssignInterface().setAttributes(rObjectId, slotsPath, attr);
    }

	public RVector getArrayValue(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getArrayValue(rObjectId, slotsPath);
	}

	public long setArrayValue(long rObjectId, String slotsPath, RVector value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setArrayValue(rObjectId, slotsPath, value);
	}

	public int[] getArrayDim(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getArrayDim(rObjectId, slotsPath);
	}

	public long setArrayDim(long rObjectId, String slotsPath, int[] dim) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setArrayDim(rObjectId, slotsPath, dim);
	}

	public RList getArrayDimnames(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getArrayDimnames(rObjectId, slotsPath);
	}

	public long setArrayDimnames(long rObjectId, String slotsPath, RList dimnames) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setArrayDimnames(rObjectId, slotsPath, dimnames);
	}

	// Factors
	public String[] factorAsData(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().factorAsData(rObjectId, slotsPath);
	}

	public int[] getFactorCode(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getFactorCode(rObjectId, slotsPath);
	}

	public String[] getFactorLevels(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getFactorLevels(rObjectId, slotsPath);
	}

	public long setFactorCode(long rObjectId, String slotsPath, int[] code) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setFactorCode(rObjectId, slotsPath, code);
	}

	public long setFactorLevels(long rObjectId, String slotsPath, String[] levels) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setFactorLevels(rObjectId, slotsPath, levels);
	}

	// Dataframes
	public RList getDataframeData(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getDataframeData(rObjectId, slotsPath);
	}

	public String[] getDataframeRowNames(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getDataframeRowNames(rObjectId, slotsPath);
	}

	public long setDataframeData(long rObjectId, String slotsPath, RList data) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setDataframeData(rObjectId, slotsPath, data);
	}

	public long setDataframeRowNames(long rObjectId, String slotsPath, String[] rowNames) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setDataframeRowNames(rObjectId, slotsPath, rowNames);
	}

	// Lists
	public RObject[] getListValue(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getListValue(rObjectId, slotsPath);
	}

	public long setListValue(long rObjectId, String slotsPath, RObject[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setListValue(rObjectId, slotsPath, value);
	}

	// env
	public HashMap getEnvData(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getEnvData(rObjectId, slotsPath);
	}

	public long putEnv(long rObjectId, String slotsPath, String theKey, RObject theValue) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().putEnv(rObjectId, slotsPath, theKey, theValue);
	}

	public long setEnvData(long rObjectId, String slotsPath, HashMap<String, RObject> data) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setEnvData(rObjectId, slotsPath, data);
	}
	
	public String[] getS3ClassAttribute(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getS3ClassAttribute(rObjectId, slotsPath);
	}

	public long setS3ClassAttribute(long rObjectId, String slotsPath, String[] classAttribute) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setS3ClassAttribute(rObjectId, slotsPath, classAttribute);
	}	

}