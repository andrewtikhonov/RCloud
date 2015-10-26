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
 * RDataFrame.java
 * 
 * This java class corresponds to R data frame 
 */
package uk.ac.ebi.rcloud.server.RType;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;

import java.util.Arrays;

public class RDataFrame extends RObject {

    private static final long serialVersionUID = 1L;
    
    protected RList data=new RList();
    protected String[] rowNames=new String[0];
    
    public RDataFrame () {
    }
    
    public RDataFrame (RList data, String[] rowNames) {
        this.data=data;
        this.rowNames=rowNames;
    }

    
    /**
     * Sets the data for this RDataFrame.
     *
     * @param data
     */
    public void setData (RList data) {
        this.data=data;
    }
    
    /**
     * Gets the data for this RDataFrame.
     *
     * @return data
     */
     public RList getData () {
        return data;
     }
     
     /**
     * Sets the rowNames for this RDataFrame.
     *
     * @param rowNames
     */
    public void setRowNames (String[] rowNames) {
        this.rowNames=rowNames;
    }
    
     /**
     * Sets the rowNames for this RDataFrame.
     *
     * @param rowNames
     */
    public void setRowNamesI (String rowNames) {
        this.rowNames=new String[]{rowNames};
    }
    
    /**
     * Gets the rowNames value for this RDataFrame.
     *
     * @return rowNames
     */
     public String[] getRowNames () {
         return rowNames;
     }


    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
	    RDataFrame obj=(RDataFrame)inputObject;
            RList objData=obj.getData();
            String[] objRowNames=obj.getRowNames();
	    if (data==null)
		res=res&&(data==objData);
	    else
            	res=res&&data.equals(objData);
            res=res && Arrays.equals(rowNames, objRowNames);
        }
        return res;
    }
     
     public String toString() {
        StringBuffer res=new StringBuffer("RDdataFrame {\ndata=");
        res.append(String.valueOf(data));
        res.append("\nrownames="+Arrays.toString(rowNames));
        res.append("\n}");
        return res.toString();
    } 
}
        
