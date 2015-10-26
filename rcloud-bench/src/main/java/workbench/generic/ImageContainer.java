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
package workbench.generic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 19, 2010
 * Time: 4:03:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageContainer {
    private BufferedImage image;
    private byte[] rawdata;

    public ImageContainer(byte[] rawdata, BufferedImage image){
        this.rawdata = rawdata;
        this.image = image;
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
        if (image == null) {
            image = fromByteArray(rawdata);
        }
        return image;
    }

    public byte[] getRawdata() {
        return rawdata;
    }


    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setRawdata(byte[] rawdata) {
        this.rawdata = rawdata;
    }



}
