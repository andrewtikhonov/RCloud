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
 * RNumeric.java
 * 
 * This java class corresponds to R Character Vector 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RVector;

import java.util.Arrays;

public class RNumeric extends RVector {

    private static final long serialVersionUID = 1L;
    
    protected double[] value=new double[0];
    protected int[] indexNA;
    
    public RNumeric() { 
    }
    
    public RNumeric(double... value) {
    	this.value=value;
    }
    
    public RNumeric(double[] value, int[] indexNA, String[] names) {
            super(names);
            this.value=value;
	    this.indexNA=indexNA;
    }
    
    /**
     * Sets the value for this RNumeric.
     *
     * @param value
     */
    public void setValue (double[] value) {
        this.value=value;
    }
    
    /**
     * Sets the value for this RNumeric.
     *
     * @param value
     */
    public void setValueI (Double value) {
        this.value=new double[]{value.doubleValue()};
    }
    
    /**
     * Gets the value for this RNumeric.
     *
     * @return value
     */
    public double[] getValue () {
        return value;
    }  

    /**
     * Sets the NA indices for this RInteger, start from 0
     *
     * @param indexNA
     */
    public void setIndexNA (int[] indexNA) {
        this.indexNA=indexNA;
    }

    /**
     * Gets the NA indices for this RInteger.
     *
     * @return indexNA
     */
    public int[] getIndexNA () {
        return indexNA;
    }

    public int length() {
        int res=0;
        if (value!=null)
                res= value.length;
        return res;
    }

    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RNumeric obj=(RNumeric)inputObject;
	    int[] objIndexNA=obj.getIndexNA();
            res=res && Arrays.equals(indexNA, objIndexNA);
            double[] objValue=obj.getValue();
            String[] objNames=obj.getNames();
	    if (res&&(indexNA!=null)) {
                if((objValue!=null)&&(value!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objValue[indexNA[i]]=value[indexNA[i]];
                }
                if((objNames!=null)&&(names!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objNames[indexNA[i]]=names[indexNA[i]];
                }
            }
            res=res && Arrays.equals(value, objValue);
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
    
    public String toString() {
        StringBuffer res=new StringBuffer("RNumeric {");
        res.append("value= "+Arrays.toString(value));
        res.append(", name= "+Arrays.toString(names));
	res.append(", NA indices= "+Arrays.toString(indexNA));
        res.append(" }");
        return res.toString();
    }  
    
}
        
