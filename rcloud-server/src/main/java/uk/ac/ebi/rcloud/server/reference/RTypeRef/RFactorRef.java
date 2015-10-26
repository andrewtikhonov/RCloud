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
package uk.ac.ebi.rcloud.server.reference.RTypeRef;

import uk.ac.ebi.rcloud.server.reference.AssignInterface;
import uk.ac.ebi.rcloud.server.reference.ReferenceInterface;
import uk.ac.ebi.rcloud.server.RType.RFactor;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

public class RFactorRef extends RFactor implements ReferenceInterface, StandardReference, Externalizable {
    final private Logger log = LoggerFactory.getLogger(getClass());
    
	private long[] _rObjectIdHolder;

	private String _slotsPath;

	private AssignInterface _assignInterface;

	public void setAssignInterface(AssignInterface assignInterface) {
		_assignInterface = assignInterface;
	}

	public AssignInterface getAssignInterface() {
		return _assignInterface;
	}

	public RObject extractRObject() {
		try {
			return _assignInterface.getObjectFromReference(this);
		} catch (RemoteException re) {
			throw new RuntimeException(Utils.getStackTraceAsString(re));
		}
	}

	public long getRObjectId() {
		return _rObjectIdHolder[0];
	}

	public String getSlotsPath() {
		return _slotsPath;
	}

	public RFactorRef() {
		super();
		_rObjectIdHolder = new long[1];
	}

	public RFactorRef(long rObjectId, String slotsPath) {
		super();
		_rObjectIdHolder = new long[1];
		_rObjectIdHolder[0] = rObjectId;
		_slotsPath = slotsPath;
	}

	public RFactorRef(long[] rObjectIdHolder, String slotsPath) {
		super();
		_rObjectIdHolder = rObjectIdHolder;
		_slotsPath = slotsPath;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(_rObjectIdHolder[0]);
		out.writeUTF(_slotsPath);
		out.writeObject(_assignInterface);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_rObjectIdHolder[0] = in.readLong();
		_slotsPath = in.readUTF();
		_assignInterface = (AssignInterface) in.readObject();
	}

	@Override
	public boolean equals(Object inputObject) {
		if (inputObject == null || !(inputObject instanceof RFactorRef))
			return false;
		return ((RFactorRef) inputObject)._rObjectIdHolder[0] == _rObjectIdHolder[0] && ((RFactorRef) inputObject)._slotsPath.equals(_slotsPath);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append("A Reference to an object of Class \"RFactor\" on the R servant <" + _assignInterface.getName() + ">  [" + _rObjectIdHolder[0] + "/"
					+ _slotsPath + "]\n");
		} catch (java.rmi.RemoteException e) {
            log.error("Error!", e);
		}
		return result.toString();
	}

	@Override
	public String[] asData() {
		try {
			return _assignInterface.factorAsData(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public int[] getCode() {
		try {
			return _assignInterface.getFactorCode(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public String[] getLevels() {
		try {
			return _assignInterface.getFactorLevels(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setCode(int[] code) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setFactorCode(_rObjectIdHolder[0], _slotsPath, code);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setCodeI(Integer code) {
		setCode(new int[] { code });
	}

	@Override
	public void setLevelsI(String levels) {
		setLevels(new String[] { levels });
	}

	@Override
	public void setLevels(String[] levels) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setFactorLevels(_rObjectIdHolder[0], _slotsPath, levels);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

    @Override
    public RList getAttributes() {
        try {
            return _assignInterface.getAttributes(_rObjectIdHolder[0], _slotsPath);
        } catch (Exception e) {
            throw new RuntimeException(Utils.getStackTraceAsString(e));
        }

    }

    @Override
    public void setAttributes(RList attrs) {
        try {
            _rObjectIdHolder[0] = _assignInterface.setAttributes(_rObjectIdHolder[0], _slotsPath, attrs);
        } catch (Exception e) {
            throw new RuntimeException(Utils.getStackTraceAsString(e));
        }
    }

}
