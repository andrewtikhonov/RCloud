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
package workbench.events;


import workbench.RGui;

import java.util.HashSet;


public class VariablesChangeEvent {	
	HashSet<String> variablesHashSet;
	String originator;
	RGui rgui;

	public VariablesChangeEvent(HashSet<String> variablesHashSet, String originator, RGui rgui) {
		super();
		this.variablesHashSet = variablesHashSet;
		this.originator = originator;
		this.rgui=rgui;
	}
	public HashSet<String> getVariablesHashSet() {
		return variablesHashSet;
	}	
	public String getOriginator() {
		return originator;
	}
	public RGui getRGui() {
		return rgui;
	}
	
}
