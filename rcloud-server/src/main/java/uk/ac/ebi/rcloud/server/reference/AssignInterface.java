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

import uk.ac.ebi.rcloud.server.RNI;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.RType.RVector;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;


public interface AssignInterface extends Remote {

	long assign(long rObjectId, String slotsPath, RObject robj) throws RemoteException;

	String[] getNames(long rObjectId, String slotsPath) throws RemoteException;

	long setNames(long rObjectId, String slotsPath, String[] names) throws RemoteException;

	public int length(long rObjectId, String slotsPath) throws RemoteException;

	String[] getValueStringArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueStringArray(long rObjectId, String slotsPath, String[] value) throws RemoteException;

	boolean[] getValueBoolArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueBoolArray(long rObjectId, String slotsPath, boolean[] value) throws RemoteException;

	double[] getValueDoubleArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueDoubleArray(long rObjectId, String slotsPath, double[] value) throws RemoteException;

	int[] getValueIntArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueIntArray(long rObjectId, String slotsPath, int[] value) throws RemoteException;

	double[] getValueCPReal(long rObjectId, String slotsPath) throws RemoteException;

	double[] getValueCPImaginary(long rObjectId, String slotsPath) throws RemoteException;

	long setValueCP(long rObjectId, String slotsPath, double[] real, double[] imaginary) throws RemoteException;

	public int[] getIndexNA(long rObjectId, String slotsPath) throws RemoteException;

	public long setIndexNA(long rObjectId, String slotsPath, int[] indexNA) throws RemoteException;

    public RList getAttributes(long rObjectId, String slotsPath) throws RemoteException;

    public long setAttributes(long rObjectId, String slotsPath, RList attr) throws RemoteException;

	public RObject getObjectFromReference(RObject refObj) throws RemoteException;

	public RNI getRNI() throws RemoteException;

	public String getName() throws RemoteException;

	// Arrays
	public RVector getArrayValue(long rObjectId, String slotsPath) throws RemoteException;

	public long setArrayValue(long rObjectId, String slotsPath, RVector value) throws RemoteException;

	public int[] getArrayDim(long rObjectId, String slotsPath) throws RemoteException;

	public long setArrayDim(long rObjectId, String slotsPath, int[] dim) throws RemoteException;

	public RList getArrayDimnames(long rObjectId, String slotsPath) throws RemoteException;

	public long setArrayDimnames(long rObjectId, String slotsPath, RList dimnames) throws RemoteException;

	// Factors
	public String[] factorAsData(long rObjectId, String slotsPath) throws RemoteException;

	public int[] getFactorCode(long rObjectId, String slotsPath) throws RemoteException;

	public String[] getFactorLevels(long rObjectId, String slotsPath) throws RemoteException;

	public long setFactorCode(long rObjectId, String slotsPath, int[] code) throws RemoteException;

	public long setFactorLevels(long rObjectId, String slotsPath, String[] levels) throws RemoteException;

	// Dataframes
	public RList getDataframeData(long rObjectId, String slotsPath) throws RemoteException;

	public String[] getDataframeRowNames(long rObjectId, String slotsPath) throws RemoteException;

	public long setDataframeData(long rObjectId, String slotsPath, RList data) throws RemoteException;

	public long setDataframeRowNames(long rObjectId, String slotsPath, String[] rowNames) throws RemoteException;

	// Lists
	public RObject[] getListValue(long rObjectId, String slotsPath) throws RemoteException;

	public long setListValue(long rObjectId, String slotsPath, RObject[] value) throws RemoteException;

	// S3
	public String[] getS3ClassAttribute(long rObjectId, String slotsPath) throws RemoteException;

	public long setS3ClassAttribute(long rObjectId, String slotsPath, String[] classAttribute) throws RemoteException;

	// Env
	public HashMap getEnvData(long rObjectId, String slotsPath) throws RemoteException;

	public long putEnv(long rObjectId, String slotsPath, String theKey, RObject theValue) throws RemoteException;

	public long setEnvData(long rObjectId, String slotsPath, HashMap<String, RObject> data) throws RemoteException;
	
	
}
