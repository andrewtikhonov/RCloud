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
package uk.ac.ebi.rcloud.server.callback;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 17, 2010
 * Time: 3:16:51 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.Serializable;
import java.util.HashMap;

public class RAction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
	private HashMap<String, Object> attributes = new HashMap<String, Object>();

	public RAction() {
	}

	public RAction(String name) {
		this.name = name;
	}

	public RAction(String name, HashMap<String, Object> attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	public RAction put(String name, Object obj) {
        attributes.put(name, obj);
        return this;
	}

	public RAction putAll(HashMap<String, Object> attributes) {
        if (attributes != null) {
            this.attributes.putAll(attributes);

            //for (String key : _executionUnitAttributes.keySet()) {
            //    action.getAttributes().put(key, _executionUnitAttributes.get(key));
            //}

        }
        return this;
	}

    public void setActionName(String name) {
		this.name = name;
	}

	public String getActionName() {
		return name;
	}

	public void setAttributes(HashMap<String, Object> attributes) {
		this.attributes = attributes;
	}

	public HashMap<String, Object> getAttributes() {
		return attributes;
	}

	public String toString() {
		return "[action name="+name+" attributes="+attributes+"]";
	}

}