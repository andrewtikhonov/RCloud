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
import java.awt.Font;
import java.awt.Graphics;


public class GDFont extends GDObject {
	double cex, ps, lineheight;
	int face;
	String family;

	Font font;

	public GDFont(double cex, double ps, double lineheight, int face, String family) {
		//log.info(">> FONT(cex="+cex+",ps="+ps+",lh="+lineheight+",face="+face+",\""+family+"\")");
		this.cex = cex;
		this.ps = ps;
		this.lineheight = lineheight;
		this.face = face;
		this.family = family;
		int jFT = Font.PLAIN;
		if (face == 2)
			jFT = Font.BOLD;
		if (face == 3)
			jFT = Font.ITALIC;
		if (face == 4)
			jFT = Font.BOLD | Font.ITALIC;
		if (face == 5)
			family = "Symbol";
		font = new Font(family.equals("") ? null : family, jFT, (int) (cex * ps + 0.5));
	}

	public Font getFont() {
		return font;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		g.setFont(font);
		gs.f = font;
	}
}