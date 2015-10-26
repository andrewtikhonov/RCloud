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
 * RFactor.java
 * 
 * This java class corresponds to R factor 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RObject;

import java.util.Arrays;

public class RFactor extends RObject {

    private static final long serialVersionUID = 1L;
    
    protected String[] levels=new String[0];
    protected int[] code=new int[0];
    
    public RFactor () {
    }
    
    public RFactor (String[] levels, int[] code) {
        this.levels=levels;
        this.code=code;
    }
    
    /**
     * Sets the levels for this RFactor.
     *
     * @param levels
     */
    public void setLevels (String[] levels) {
        this.levels=levels;
    }
    
    /**
     * Sets the levels for this RFactor.
     *
     * @param levels
     */
    public void setLevelsI (String levels) {
        this.levels=new String[]{levels};
    }
    
    /**
     * Gets the levels for this RArray.
     *
     * @return levels
     */
     public String[] getLevels () {
        return levels;
     }
     
     /**
     * Sets the code for this RFactor.
     *
     * @param code
     */
    public void setCode (int[] code) {
        this.code=code;
    }

     /**
     * Sets the code for this RFactor.
     *
     * @param code
     */
    
    public void setCodeI (Integer code) {
        this.code=new int[]{code.intValue()};
    }
    
    
    /**
     * Gets the code value for this RFactor.
     *
     * @return code
     */
     public int[] getCode () {
         return code;
     }
     
     /**
     * Gets the String[] representation of this RFactor.
     * i.e. all the code are converted to their corresponding levels
     *
     * @return data
     */
     public String[] asData () {
        String[] data=new String[code.length];
        for (int i=0; i< code.length; i++) {
            if (code[i]<0) 
                data[i]="NA";
            else
                data[i]=levels[code[i]-1];
        }
        return data;
     }

    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
	    RFactor obj=(RFactor)inputObject;
            int[] objCode=obj.getCode();
            String[] objLevels=obj.getLevels();
            res=res && Arrays.equals(code, objCode);
            res=res && Arrays.equals(levels, objLevels);
        }
        return res;
    }
     
     public String toString() {
        StringBuffer res=new StringBuffer("");
        res.append("{ data=[ ");
        for (int i=0; i< code.length; i++) {
            if (code[i]<0) 
                res.append("NA ");
            else
                res.append(levels[code[i]-1]).append(" ");
        }
        res.append("], levels=[");
        for (int i=0; i<levels.length; i++) 
            res.append(levels[i]).append(" ");
        res.append("] }");
        return res.toString();
    }           
}
        
