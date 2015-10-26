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
package uk.ac.ebi.rcloud.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 12, 2009
 * Time: 4:46:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageLoader {

    final private static Logger log = LoggerFactory.getLogger(ImageLoader.class);

    private static HashMap<String, Image> imageCache = new HashMap<String, Image>();

    /** Load the specified image. If the image cannot be loaded, a dummy
     *  image is returned.
     * @param path      Path to the image within the package.
     *                  e.g. "/org/package/images/image.png"
     * @return          The loaded image or a dummy image
     */
    public static Image load(String path) {

        Image image = imageCache.get(path);

        if (image != null) {
            return image;
        } else {
            try {
                Image loadedimage = ImageIO.read(ImageLoader.class.getResource(path));
                imageCache.put(path, loadedimage);
                
                return loadedimage;
            } catch (Exception ex) {
                log.error("Icon ({}) missing", path);
                return getDummyImage();
            }
        }

    }

    /** Get a dummy image
     * @return          A dummy image
     */
    public static Image getDummyImage() {
        int h = 20;
        int w = 20;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();

        g2d.fillRect(0,0,w,h);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(2,2,w-3,h-3);
        g2d.drawLine(w-3,2,2,h-3);

        g2d.dispose();

        return img;
    }

}
