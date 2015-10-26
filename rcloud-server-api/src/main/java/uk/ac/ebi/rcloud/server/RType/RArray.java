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
 * RArray.java
 * 
 * This java class corresponds to R array 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RNumeric;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.RType.RVector;

import java.util.Arrays;

public class RArray extends RObject {

    private static final long serialVersionUID = 1L;
    
    protected RVector value=new RNumeric();
    protected int[] dim=new int[]{0};
    protected RList dimnames;
    
    public RArray() { 
    }
   
    public RArray(RVector value) {
	    this.value=value;
	    int valueLength=value.length();
	    this.dim=new int[]{valueLength};
    }
 
    public RArray(RVector value, int[]dim, RList dimnames) {
            this.value=value;
            this.dim=dim;
            this.dimnames=dimnames; 
    }
    
    /**
     * Sets the value for this RArray.
     *
     * @param value
     */
    public void setValue (RVector value) {
        this.value=value;
    }
    
    /**
     * Gets the value for this RArray.
     *
     * @return value
     */
     public RVector getValue () {
        return value;
     }
     
     /**
     * Sets the dim value for this RArray.
     *
     * @param dim
     */
    public void setDim (int[] dim) {
        this.dim=dim;
    }
 
    /**
     * Gets the dim value for this RArray.
     *
     * @return dim
     */
     public int[] getDim () {
         return dim;
     }
    
    /**
     * Sets the dimnames value for this RArray.
     *
     * @param dimanmes
     */
    public void setDimnames (RList dimnames) throws Exception {
        this.dimnames=dimnames;
    }
    
    /**
     * Gets the dimnames value for this RArray.
     *
     * @return dimnames
     */
     public RList getDimnames () {
        return dimnames;
     }

     public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RArray obj=(RArray)inputObject;
	    RVector objVal=obj.getValue();
	    if ((value==null)||(objVal==null))
		res=res&&(value==objVal);
	    else
            	res=res&&(value.equals(objVal));
	    RList objDimnames=obj.getDimnames();
	    if ((dimnames==null)||(objDimnames==null))
		res=res&&(dimnames==objDimnames);
	    else
	    	res=res&&(dimnames.equals(objDimnames));
	    int[] objDim=(int[])obj.getDim();
            res=res && Arrays.equals(dim, objDim);
        }
        return res;
    }
   
     public String toString() {
        StringBuffer res=new StringBuffer(this.getClass().getName());
        res.append(" { value= "+String.valueOf(value));
        res.append(", dim= "+Arrays.toString(dim));
        res.append(", dimnames= "+String.valueOf(dimnames));
        res.append(" }");
        return res.toString();
    }   
      
}
        
