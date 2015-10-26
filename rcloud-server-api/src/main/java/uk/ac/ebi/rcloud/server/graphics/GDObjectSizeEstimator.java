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
package uk.ac.ebi.rcloud.server.graphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.graphics.primitive.*;
import uk.ac.ebi.rcloud.util.HexUtil;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 15, 2010
 * Time: 2:19:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GDObjectSizeEstimator {
    private static Logger log = LoggerFactory.getLogger(GDObjectSizeEstimator.class);

    private static HashMap<Class, Integer> map = new HashMap<Class, Integer>();

    public static boolean isSupported(Class class0) {
        return map.containsKey(class0);
    }

    public static int getSize(Class class0) {
        return map.get(class0);
    }

    /*
    static {
        main(null);

    }
    */

    public static void cacheit(Object obj) {
        Class class0 = obj.getClass();
        String hex = HexUtil.objectToHex(obj);

        map.put(class0, hex.length() / 2);
        log.info(class0 + ".length() = " + map.get(class0));
        //log.info("hex = " + hex);
    }


    public static void main(String[] args) {
        GDCircle circle = new GDCircle(10,10,10);
        cacheit(circle);


        GDClip clip = new GDClip(10d,10d,20d,20d);
        cacheit(clip);

        //map.put(GDClip.class, PoolUtils.objectToHex(clip).length());
        //log.info("GDClip.length() = " + map.get(GDClip.class));


        GDColor color = new GDColor(255);
        cacheit(color);
        //map.put(GDColor.class, PoolUtils.objectToHex(color).length());
        //log.info("GDColor.length() = " + map.get(GDColor.class));


        GDFill fill = new GDFill(255);
        cacheit(fill);
        //map.put(GDFill.class, PoolUtils.objectToHex(fill).length());
        //log.info("GDFill.length() = " + map.get(GDFill.class));


        GDFont font = new GDFont(10d, 10d, 10d, 10, "Font family");
        cacheit(font);

        //map.put(GDFill.class, PoolUtils.objectToHex(fill).length());
        //log.info("GDFill.length() = " + map.get(GDFill.class));


        GDLine line = new GDLine(10d,10d,20d,20d);
        cacheit(line);


        GDLinePar linepar = new GDLinePar(10d,20);
        cacheit(linepar);


        GDPolygon polygon = new GDPolygon(3, new double[]{10d,20d,30d}, new double[]{10d,20d,30d}, true);
        cacheit(polygon);


        GDRect rect = new GDRect(10d,20d,30d,40d);
        cacheit(rect);


        GDState state = new GDState();
        cacheit(state);


        GDText text = new GDText(10d,20d,30d,40d, "Some Test Text");
        cacheit(text);

    }

}
