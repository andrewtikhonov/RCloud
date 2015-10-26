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
 * RVector.java
 * 
 * This java class corresponds to R Vector (exclude list)
 */
package uk.ac.ebi.rcloud.server.RType;

import uk.ac.ebi.rcloud.server.RType.RObject;

/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

public abstract class RVector extends RObject {

    private static final long serialVersionUID = 1L;
    
    protected String[] names;
    
    public RVector() { 
    }
    
    public RVector(String[] names) {
            this.names=names;
    }
    
    /**
     * Sets the names for this RVector.
     *
     * @param names
     */
    public void setNames (String[] names) {
        this.names=names;
    }
    
    /**
     * Gets the names for this RVector.
     *
     * @return names
     */
    public String[] getNames () {
        return names;
    }    
   
    public int length() {
	return 0;
    } 
}
        
