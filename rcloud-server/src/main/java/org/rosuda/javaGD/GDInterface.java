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
package org.rosuda.javaGD;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import uk.ac.ebi.rcloud.server.graphics.GDContainer;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDCircle;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDClip;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDColor;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDFill;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDFont;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDLine;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDLinePar;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDPolygon;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDRect;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*---- external API: those methods are called via JNI from the GD C code

 public void     gdOpen(int devNr, double w, double h);
 public void     gdActivate();
 public void     gdCircle(double x, double y, double r);
 public void     gdClip(double x0, double x1, double y0, double y1);
 public void     gdClose();
 public void     gdDeactivate();
 public void     gdHold();
 public double[] gdLocator();
 public void     gdLine(double x1, double y1, double x2, double y2);
 public double[] gdMetricInfo(int ch);
 public void     gdMode(int mode);
 public void     gdNewPage(int deviceNumber);
 public void     gdPolygon(int n, double[] x, double[] y);
 public void     gdPolyline(int n, double[] x, double[] y);
 public void     gdRect(double x0, double y0, double x1, double y1);
 public double[] gdSize();
 public double   gdStrWidth(String str);
 public void     gdText(double x, double y, String str, double rot, double hadj);


 -- GDC - manipulation of the current graphics state
 public void gdcSetColor(int cc);
 public void gdcSetFill(int cc);
 public void gdcSetLine(double lwd, int lty);
 public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily);

 -- implementation --*/

public class GDInterface {
    final private static Logger log = LoggerFactory.getLogger(GDInterface.class);

	public boolean active = false;

	public boolean open = false;

	int devNr = -1;

	public GDContainer c = null;

	//public LocatorSync ls = null;

	public void gdOpen(double w, double h) {
        //log.info("gdOpen");
		open = true;
	}

	public void gdActivate() {
        //log.info("gdActivate");
		active = true;
	}

	public void gdCircle(double x, double y, double r) {
        //log.info("gdCircle");
		if (c == null)
			return;
		try {
			c.add(new GDCircle(x, y, r));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdClip(double x0, double x1, double y0, double y1) {
        //log.info("gdClip");
		if (c == null)
			return;
		try {
			c.add(new GDClip(x0, y0, x1, y1));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdClose() {
        //log.info("gdClose");
        try {
			if (c != null)
				c.closeDisplay();
			open = false;
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdDeactivate() {
        //log.info("gdDeactivate");
		active = false;
	}

	public void gdHold() {
        //log.info("gdHold");
    }

	public static void putLocatorLocation(Point2D p) {
        //log.info("putLocatorLocation");
		try {
			coords.put(p);
		} catch (Exception e) {
            log.error("Error!", e);
		}
	}
	
	public static boolean hasLocations() {
        //log.info("hasLocations");
		return coords.size()>0;
	}

	private static ArrayBlockingQueue<Point2D> coords = new ArrayBlockingQueue<Point2D>(200);
	private static Vector<Point2D> savedCoords = null;

	public static void saveLocations() {
        //log.info("saveLocations");
		savedCoords = new Vector<Point2D>();
		Iterator<Point2D> iter = coords.iterator();
		while (iter.hasNext()) {
			savedCoords.add(iter.next());
		}
		coords = new ArrayBlockingQueue<Point2D>(200);
	}

	public static void restoreLocations() {
        //log.info("restoreLocations");
		if (savedCoords != null) {
			for (int i = 0; i < savedCoords.size(); ++i) {
				putLocatorLocation(savedCoords.elementAt(i));
			}
			savedCoords = null;
		}
	}

	public double[] gdLocator() {
        //log.info("gdLocator");
        if (c == null)
			return null;
		try {
			Point2D p = coords.poll();
			if (p != null) {
				double[] pos = new double[2];
				pos[0] = p.getX();
				pos[1] = p.getY();
				return pos;
			} else {
				return null;
			}
		} catch (Throwable e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdLine(double x1, double y1, double x2, double y2) {
        //log.info("gdLine");
		if (c == null)
			return;
		try {
			c.add(new GDLine(x1, y1, x2, y2));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public double[] gdMetricInfo(int ch) {
        //log.info("gdMetricInfo");

        try {
			double[] res = new double[3];
			double ascent = 0.0, descent = 0.0, width = 8.0;
			if (c != null) {
				FontMetrics fm = c.getGFontMetrics();
				if (fm != null) {
					ascent = (double) fm.getAscent();
					descent = (double) fm.getDescent();
					width = (double) fm.charWidth((ch == 0) ? 77 : ch);
				}
			}
			res[0] = ascent;
			res[1] = descent;
			res[2] = width;
			return res;
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdMode(int mode) {
        //log.info("gdMode, mode = " + mode);

        if (c != null) {
			try {
				c.syncDisplay(mode == 0);
			} catch (RemoteException e) {
                log.error("Error!", e);
				c = null;
				throw new RuntimeException(getStackTraceAsString(e));
			}
		}
	}

	public void gdNewPage() {
        //log.info("gdNewPage");

		if (c != null) {
			try {
				c.reset();
			} catch (RemoteException e) {
                log.error("Error!", e);
				c = null;
				throw new RuntimeException(getStackTraceAsString(e));
			}
		}

	}

	public void gdNewPage(int devNr) { // new API: provides the device Nr.
        //log.info("gdNewPage");
		try {
			this.devNr = devNr;
			if (c != null) {
				c.reset();
				c.setDeviceNumber(devNr);
			}
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdPolygon(int n, double[] x, double[] y) {
        //log.info("gdPolygon");
		// System.out.println("gdPolygon");
		if (c == null)
			return;
		try {
			c.add(new GDPolygon(n, x, y, false));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdPolyline(int n, double[] x, double[] y) {
        //log.info("gdPolyline");

        // System.out.println("gdPolyline");
		if (c == null)
			return;
		try {
			c.add(new GDPolygon(n, x, y, true));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdRect(double x0, double y0, double x1, double y1) {
        //log.info("gdRect");

		if (c == null)
			return;
		try {
			c.add(new GDRect(x0, y0, x1, y1));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public double[] gdSize() {
        //log.info("gdSize");

        try {

			double[] res = new double[4];
			double width = 0d, height = 0d;
			if (c != null) {
				Dimension d = c.getSize();
				width = d.getWidth();
				height = d.getHeight();
			}
			res[0] = 0d;
			res[1] = width;
			res[2] = height;
			res[3] = 0;
			return res;
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public double gdStrWidth(String str) {
        //log.info("gdStrWidth");

        try {
			double width = (double) (8 * str.length()); // rough estimate
			if (c != null) { // if canvas is active, we can do better
				FontMetrics fm = c.getGFontMetrics();
				if (fm != null)
					width = (double) fm.stringWidth(str);
			}
			return width;
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdText(double x, double y, String str, double rot, double hadj) {
        //log.info("gdText");

        if (c == null)
			return;
		try {
			c.add(new GDText(x, y, rot, hadj, str));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	/*-- GDC - manipulation of the current graphics state */
	public void gdcSetColor(int cc) {
        //log.info("gdcSetColor");

        if (c == null)
			return;
		try {
			c.add(new GDColor(cc));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdcSetFill(int cc) {
        //log.info("gdcSetFill");

        if (c == null)
			return;
		try {
			c.add(new GDFill(cc));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdcSetLine(double lwd, int lty) {
        //log.info("gdcSetLine");

        if (c == null)
			return;
		try {
			c.add(new GDLinePar(lwd, lty));
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily) {
        //log.info("gdcSetFont");

        if (c == null)
			return;
		try {
			GDFont f = new GDFont(cex, ps, lineheight, fontface, fontfamily);
			c.add(f);
			c.setGFont(f.getFont());
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public int getDeviceNumber() {
        //log.info("getDeviceNumber");

        try {
			return (c == null) ? devNr : c.getDeviceNumber();
		} catch (RemoteException e) {
            log.error("Error!", e);
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}
}
