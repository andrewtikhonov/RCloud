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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class GDText extends GDObject {
	double x, y, r, h;
	String txt;

	public GDText(double x, double y, double r, double h, String txt) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.h = h;
		this.txt = txt;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (gs.col != null) {
			double rx = x, ry = y;
			double hc = 0d;
			if (h != 0d) {
				FontMetrics fm = g.getFontMetrics();
				int w = fm.stringWidth(txt);
				hc = ((double) w) * h;
				rx = x - (((double) w) * h);
			}
			int ix = (int) (rx + 0.5), iy = (int) (ry + 0.5);

			if (r != 0d) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.translate(x, y);
				double rr = -r / 180d * Math.PI;
				g2d.rotate(rr);
				if (hc != 0d)
					g2d.translate(-hc, 0d);
				g2d.drawString(txt, 0, 0);
				if (hc != 0d)
					g2d.translate(hc, 0d);
				g2d.rotate(-rr);
				g2d.translate(-x, -y);
			} else
				g.drawString(txt, ix, iy);
		}
	}
}
