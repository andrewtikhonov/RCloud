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
 * RList.java
 * 
 * This java class corresponds to R List 
 */
package uk.ac.ebi.rcloud.server.RType;
import java.util.Arrays;

public class RList extends RObject {

    private static final long serialVersionUID = 1L;
    
    protected RObject[] value = new RObject[0];
    protected String[] names;
    
    public RList() { 
    }
    
    public RList(RObject[] value, String[] names) {
            this.value=value;
            this.names=names;
    }
    
    /**
     * Sets the value for this RList.
     *
     * @param value
     */
    public void setValue (RObject[] value) {
        this.value=value;
    }
    
    /**
     * Gets the value for this RList.
     *
     * @return value
     */
     public RObject[] getValue () {
        return value;
     }
   
    /**
     * Sets the names for this RList.
     *
     * @param names
     */
    public void setNames (String[] names) {
        this.names=names;
    }
    
    
    public void setNamesI (String names) {
        this.names=new String[]{names};
    }
    

    public RObject getValueByName(String name) {
        for(int i = 0;i < names.length;i++) {
            if (names[i].equals(name)) {
                return value[i];
            }
        }
        return null;
    }


    /**
     * Gets the names for this RList.
     *
     * @return value
     */
    public String[] getNames () {
        return names;
    } 
   
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RList obj=(RList)inputObject;
            Object[] objValue=obj.getValue();
            res=res && Arrays.deepEquals(value, objValue);
            String[] objNames=obj.getNames();
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RList {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= "+Arrays.deepToString(value));
        res.append(" }");
        return res.toString();
    } 
      
}
        
