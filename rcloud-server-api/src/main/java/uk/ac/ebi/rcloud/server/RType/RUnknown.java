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
 * RUnknown.java
 * 
 * This java class corresponds to R objects that have no corresponding covert functions
 */
package uk.ac.ebi.rcloud.server.RType;

import java.util.Arrays;

public class RUnknown extends RRaw {

    private static final long serialVersionUID = 1L;
    
	public RUnknown() {
		super();
	}

	public RUnknown(int[] value) {
		super(value);
	}

    public String toString() {
        StringBuffer res=new StringBuffer("RUnknown {");
        res.append("value= "+ Arrays.toString(value));
        res.append(" }");
        return res.toString();
    }
}
        