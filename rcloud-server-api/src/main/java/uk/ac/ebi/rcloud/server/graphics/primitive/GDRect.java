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

import java.awt.Component;
import java.awt.Graphics;


public class GDRect extends GDObject {
	double x1, y1, x2, y2;

	public GDRect(double x1, double y1, double x2, double y2) {
		double tmp;
		if (x1 > x2) {
			tmp = x1;
			x1 = x2;
			x2 = tmp;
		}
		if (y1 > y2) {
			tmp = y1;
			y1 = y2;
			y2 = tmp;
		}
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		//log.info(">> RECT "+x1+":"+y1+" "+x2+":"+y2);
	}

	public void paint(Component c, GDState gs, Graphics g) {
		//log.info(" paint> rect: "+x1+":"+y1+" "+x2+":"+y2);
		int x = (int) (x1 + 0.5);
		int y = (int) (y1 + 0.5);
		int w = (int) (x2 + 0.5) - x;
		int h = (int) (y2 + 0.5) - y;
		if (gs.fill != null) {
			g.setColor(gs.fill);
			g.fillRect(x, y, w + 1, h + 1);
			if (gs.col != null)
				g.setColor(gs.col);
		}
		if (gs.col != null)
			g.drawRect(x, y, w, h);
	}
}