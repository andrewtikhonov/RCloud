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
package uk.ac.ebi.rcloud.server.reference.RTypeName;

import uk.ac.ebi.rcloud.server.reference.RTypeName.ObjectNameInterface;
import uk.ac.ebi.rcloud.server.RType.RDataFrame;


public class RDataFrameObjectName extends RDataFrame implements ObjectNameInterface {
	private String _name; 
	private String _env;
	
	public RDataFrameObjectName() {
	}

	public RDataFrameObjectName(String name) {
		this._name = name;
		this._env = ".GlobalEnv";
	}

	public RDataFrameObjectName(String environment, String name) {
		this._name = name;
		this._env = environment;
	}

	
	public String getRObjectName() {return _name;}
	public void setRObjectName(String _name) {this._name = _name;}
	public String getRObjectEnvironment() {return _env;}
	public void setRObjectEnvironment(String _env) {this._env = _env;}


	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ObjectNameInterface) )	return false;
		return (((ObjectNameInterface) obj).getRObjectName().equals(this._name)) && (((ObjectNameInterface) obj).getRObjectEnvironment().equals(_env));
	}
	
	public String toString() {
		return "RDataFrameObjectName:"+_env+"$"+_name;
	}

    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException {
    	out.writeUTF(_env);
    	out.writeUTF(_name);
    }

    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException {
    	_env=in.readUTF();
    	_name=in.readUTF();
    }
}