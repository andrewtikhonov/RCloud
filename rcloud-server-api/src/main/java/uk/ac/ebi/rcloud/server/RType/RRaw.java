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
 * RRaw.java
 * 
 * This java class corresponds to R raw Vector 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RVector;

import java.util.Arrays;

public class RRaw extends RObject {

    private static final long serialVersionUID = 1L;

    protected int[] value=new int[0];
    
    public RRaw() { 
    }
    
    public RRaw(int[] value) {
            this.value=value;
    }
    
    /**
     * Sets the value for this RRaw.
     *
     * @param value
     */
    public void setValue (int[] value) {
        this.value=value;
    }

    /**
     * Gets the value for this RRaw.
     *
     * @return value
     */
    public int[] getValue () {
        return value;
    }

    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RRaw obj=(RRaw)inputObject;
            int[] objValue=obj.getValue();
            res=res && Arrays.equals(value, objValue);
        }
        return res;
    }

    public String toString() {
        StringBuffer res=new StringBuffer("RRaw {");
        res.append("value= "+Arrays.toString(value));
        res.append(" }");
        return res.toString();
    }
}
        
