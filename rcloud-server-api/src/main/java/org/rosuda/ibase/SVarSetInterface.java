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
package org.rosuda.ibase;

import java.io.Serializable;

import uk.ac.ebi.rcloud.server.iplots.SVarSetInterfaceRemote;


public interface SVarSetInterface extends Serializable{	
	public SVarInterface at(int i);
    public int count();
    public void setName(String s);
    public int indexOf(String nam);
    public String getName();
    public SMarkerInterface getMarker();    
    public SVarInterface byName(String nam);
    public SVarSetInterfaceRemote getRemote();    
    //public void setMarker(SMarkerInterface m);    
    //public int add(SVarInterface v);
}
