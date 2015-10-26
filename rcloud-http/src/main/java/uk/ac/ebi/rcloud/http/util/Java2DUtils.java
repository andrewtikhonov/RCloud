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
package uk.ac.ebi.rcloud.http.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Vector;

import uk.ac.ebi.rcloud.server.graphics.action.GDActionMarker;
import uk.ac.ebi.rcloud.server.graphics.primitive.*;

public class Java2DUtils {
	private static boolean _forceAntiAliasing = true;
    private static GDState _gs=new GDState( Color.black,Color.white,new Font(null, 0, 12));

    static public void paintAll(Graphics2D g, Point o, Dimension dSize, Vector<GDObject> _l) {
		if (_forceAntiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		g.setClip(o.x, o.y, dSize.width, dSize.height); // reset
		g.setColor(Color.white);
		g.fillRect(o.x, o.y, dSize.width, dSize.height);
		g.translate(-o.x, -o.y);
		while (i < j) {
			GDObject gdo = (GDObject) _l.elementAt(i++);
			if (gdo instanceof GDActionMarker) {

			} else {
				gdo.paint(null, _gs, g);
			}
		}
	}

	static public BufferedImage getBufferedImage(Point o, Dimension dSize, Vector<GDObject> g2dObjects) {
		BufferedImage bufferedImage = new BufferedImage(dSize.width, dSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		Java2DUtils.paintAll(g2d, new Point(0, 0), new Dimension(dSize.width,dSize.height), g2dObjects);				
		g2d.dispose();
		return bufferedImage;
	}
}
