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
package workbench.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 15, 2010
 * Time: 11:48:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScreenCapture {

    private static Logger log = LoggerFactory.getLogger(ScreenCapture.class);

    public static BufferedImage captureScreen(Rectangle rect) {
        try {
            BufferedImage capturedImage = new Robot().createScreenCapture(rect);
            return capturedImage;
        } catch (AWTException awte) {
            awte.printStackTrace();
        }
        return null;
    }

    public static void captureScreen(Rectangle rect, OutputStream out) {
        try {
            ImageIO.write(captureScreen(rect), "png", out);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static BufferedImage captureScreen() {
        return captureScreen(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    public static void captureScreen(OutputStream out) {
        captureScreen(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()), out);
    }

    public static void captureScreen(Component comp, OutputStream out) {
        captureScreen(new Rectangle(comp.getLocationOnScreen(),
                comp.getSize()),
                out);
    }

}
