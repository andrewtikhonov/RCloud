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


public class GDPolygon extends GDObject {
	int n;
	double x[], y[];
	int xi[], yi[];
	boolean isPolyline;

	public GDPolygon(int n, double[] x, double[] y, boolean isPolyline) {
		this.x = x;
		this.y = y;
		this.n = n;
		this.isPolyline = isPolyline;
		int i = 0;
		xi = new int[n];
		yi = new int[n];
		while (i < n) {
			xi[i] = (int) (x[i] + 0.5);
			yi[i] = (int) (y[i] + 0.5);
			i++;
		}
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (gs.fill != null && !isPolyline) {
			g.setColor(gs.fill);
			g.fillPolygon(xi, yi, n);
			if (gs.col != null)
				g.setColor(gs.col);
		}
		if (gs.col != null) {
			if (isPolyline)
				g.drawPolyline(xi, yi, n);
			else
				g.drawPolygon(xi, yi, n);
		}
	}
}
