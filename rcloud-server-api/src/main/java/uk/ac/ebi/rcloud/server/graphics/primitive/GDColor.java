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
package uk.ac.ebi.rcloud.server.graphics.primitive;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


public class GDColor extends GDObject {
	int col;
	Color gc;

	public GDColor(int col) {
		this.col = col;
		//log.info(">> COLOR: "+Integer.toString(col,16));
		if ((col & 0xff000000) == 0)
			gc = null; // opacity=0 -> no color -> don't paint
		else
			gc = new Color(((float) (col & 255)) / 255f, ((float) ((col >> 8) & 255)) / 255f,
					((float) ((col >> 16) & 255)) / 255f, ((float) ((col >> 24) & 255)) / 255f);
		//log.info("          "+gc);
	}

	public void paint(Component c, GDState gs, Graphics g) {
		gs.col = gc;
		//log.info(" paint > color> (col="+col+") "+gc);
		if (gc != null)
			g.setColor(gc);
	}
}
