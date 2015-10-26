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
 * RComplex.java
 * 
 * This java class corresponds to R Character Vector 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RVector;

import java.util.Arrays;

public class RComplex extends RVector {

    private static final long serialVersionUID = 1L;
    
    protected double[] real=new double[0];
    protected double[] imaginary=new double[0];
    protected int[] indexNA;
    
    public RComplex() { 
    }
    
    public RComplex(double[] real, double[] imaginary, int[] indexNA, String[] names) {
            super(names);
            this.real=real;
            this.imaginary=imaginary;
	    this.indexNA=indexNA;
    }
    
    /**
     * Sets the real for this RComplex.
     *
     * @param real
     */
    public void setReal (double... real) {
        this.real=real;
    }
    
    /**
     * Sets the real for this RComplex.
     *
     * @param real
     */
    public void setRealI(Double real) {
        this.real=new double[]{real.doubleValue()};
    }
    
    /**
     * Gets the real for this RComplex.
     *
     * @return real
     */
    public double[] getReal () {
        return real;
    }    
    
    /**
     * Sets the imaginary for this RComplex.
     *
     * @param imaginary
     */
    public void setImaginary (double... imaginary) {
        this.imaginary=imaginary;
    }
    
    /**
     * Sets the imaginary for this RComplex.
     *
     * @param imaginary
     */
    public void setImaginaryI (Double imaginary) {
        this.imaginary=new double[]{imaginary.doubleValue()};
    }
    
    /**
     * Gets the imaginary for this RComplex.
     *
     * @return imaginary
     */
    public double[] getImaginary () {
        return imaginary;
    } 
 
    /**
     * Sets the NA indices for this RComplex, start from 0
     *
     * @param indexNA
     */
    public void setIndexNA (int[] indexNA) {
        this.indexNA=indexNA;
    }

    /**
     * Gets the NA indices for this RComplex.
     *
     * @return indexNA
     */
    public int[] getIndexNA () {
        return indexNA;
    }

 
    public int length() {
        int res=0;
        if (imaginary!=null)
                res= imaginary.length;
        return res;
    }
 
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RComplex obj=(RComplex)inputObject;
	    int[] objIndexNA=obj.getIndexNA();
            res=res && Arrays.equals(indexNA, objIndexNA);
	    double[] objReal=obj.getReal();
	    double[] objImaginary=obj.getImaginary();
            String[] objNames=obj.getNames();
	    if (res&&(indexNA!=null)) {
                if((objReal!=null)&&(real!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objReal[indexNA[i]]=real[indexNA[i]];
                }
                if((objImaginary!=null)&&(imaginary!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objImaginary[indexNA[i]]=imaginary[indexNA[i]];
                }
                if((objNames!=null)&&(names!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objNames[indexNA[i]]=names[indexNA[i]];
                }
            }
            res=res && Arrays.equals(real, objReal);
            res=res && Arrays.equals(imaginary, objImaginary);
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RComplex {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= ");
        if ((real==null)||(imaginary==null))
            res.append("null");
        else {
            res.append("[ ");
	    boolean[] isNA=new boolean[real.length];
	    for (int i=0; i<real.length; i++)
		isNA[i]=false;
	    if (indexNA!=null) {
		for (int i=0; i<indexNA.length; i++)
			isNA[indexNA[i]]=true;
	    }
            for (int i=0; i<real.length; i++) { 
		if (isNA[i])
		    res.append("NA ");
		else
                    res.append(real[i]).append("+(").append(imaginary[i]).append(")i ");
	    }
            res.append("]");
        }
        res.append(" }");
        return res.toString();
    }  

}
        
