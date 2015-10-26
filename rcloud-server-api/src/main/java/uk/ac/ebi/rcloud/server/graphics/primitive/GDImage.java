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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 9, 2010
 * Time: 1:16:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class GDImage extends GDObject {

    private static final long serialVersionUID = 1L;

    private double x, y;
	private int width, height;
	private byte[] imagedata;
    private Object assembledimage = null;
    private HashMap<String, Object> attributes = new HashMap<String, Object>();

    public GDImage(double x, double y, BufferedImage image) {
		this.x = x;
		this.y = y;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.imagedata = toByteArray(image);
	}

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    public GDImage put(String name, Object obj) {
        attributes.put(name, obj);
        return this;
    }

    public GDImage putAll(HashMap<String, Object> attributes) {
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
        return this;
    }

    private byte[] toByteArray(BufferedImage image) {
        if(image != null) {

            String encoder = System.getProperty("graphics.encoder");

            if (encoder == null || encoder.length() == 0) {
                encoder = "png";
            } else if (encoder.equals("default")) {
                encoder = "png";
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, encoder, baos);

            } catch (IOException e) {
                throw new IllegalStateException(e.toString());
            }
            byte[] b = baos.toByteArray();
            return b;
        }

        return new byte[0];
    }

    private BufferedImage fromByteArray(byte[] imagebytes) {
        try {
            if (imagebytes != null && (imagebytes.length > 0)) {
                BufferedImage im = ImageIO.read(new ByteArrayInputStream(imagebytes));
                return im;
            }
            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public BufferedImage getImage() {
        if (assembledimage == null) {
            assembledimage = fromByteArray(imagedata);
        }
        return (BufferedImage) assembledimage;
    }

    public byte[] getImageData() {
        return imagedata;
    }

	public void paint(Component c, GDState gs, Graphics g) {
        g.drawImage(getImage(), (int)x, (int)y, null);
    }

}
