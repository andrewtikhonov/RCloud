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
/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * REnvironment.java
 * 
 * This java class corresponds to R List 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RObject;

import java.util.HashMap;

public class REnvironment extends RObject {

    private static final long serialVersionUID = 1L;
    
    protected HashMap<String, RObject> data = new HashMap<String, RObject>();
    
    public REnvironment() { 
    }
    
    /**
     * Sets the data for this REnvironment.
     *
     * @param data
     */
    public void setData (HashMap<String, RObject> data) {
        this.data=data;
    }
    
    /**
     * Gets the data for this REnvironment.
     *
     * @return data
     */
     public HashMap<String, RObject> getData () {
        return data;
     }
    
     public void put(String theKey, RObject theValue) {
	data.put(theKey, theValue);
     }

     public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            REnvironment obj=(REnvironment)inputObject;
            HashMap objData=obj.getData();
	    if (data==null)
		res=res&(data==objData);
	    else
            	res=res&&data.equals(objData);
        }
        return res;
    }
 
     public String toString() {
        String res="REnvironment {"+String.valueOf(data)+"}";
        return res;
    }    
}
        
