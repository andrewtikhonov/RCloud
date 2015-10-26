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

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 07/07/2011
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public class ColorUtil {

    public static Color brighter(Color c, int d) {
        return new Color(Math.min(c.getRed() + d, 255),
                Math.min(c.getGreen() + d, 255),
                Math.min(c.getBlue() + d, 255),
                c.getAlpha());

    }

    public static Color darker(Color c, int d) {
        return new Color(Math.max(c.getRed() - d, 0),
                Math.max(c.getGreen() - d, 0),
                Math.max(c.getBlue() - d, 0),
                c.getAlpha());

    }

}
