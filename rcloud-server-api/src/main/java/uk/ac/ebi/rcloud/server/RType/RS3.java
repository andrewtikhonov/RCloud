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
package uk.ac.ebi.rcloud.server.RType;

import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;

import java.util.Arrays;

public class RS3 extends RList {

    private static final long serialVersionUID = 1L;
    
	private String[] classAttribute=null;
	
	public RS3() {
		super();
	}

	public RS3(RObject[] value, String[] names) {
		super(value, names);
	}

	public RS3(RObject[] value, String[] names, String[] classAttribute) {
		super(value, names);
		this.classAttribute=classAttribute;
	}

	public String[] getClassAttribute() {
		return classAttribute;
	}

	public void setClassAttribute(String[] classAttribute) {
		this.classAttribute = classAttribute;
	}
	
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
        	RS3 obj=(RS3)inputObject;
            Object[] objValue=obj.getValue();
            res=res && Arrays.deepEquals(value, objValue);
            String[] objNames=obj.getNames();
            
            boolean ce=false;
            if (classAttribute==null && obj.classAttribute==null) ce=true;
            else if (classAttribute==null && obj.classAttribute!=null) ce=false;
            else if (classAttribute!=null && obj.classAttribute==null) ce=false;
            else if (classAttribute.length!=obj.classAttribute.length) ce=false; 
            else {            	
            	ce=true;
            	for (int i=0; i< classAttribute.length; ++i) {
            		if (!classAttribute[i].equals(obj.classAttribute[i])) ce=false; break;
            	}
            }
            res=res && Arrays.equals(names, objNames) && ce;
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RS3 {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= "+Arrays.deepToString(value));
        res.append(", class= "+Arrays.toString(classAttribute));
        res.append(" }");
        return res.toString();
    } 

	
}
