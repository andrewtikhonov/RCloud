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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 31, 2010
 * Time: 2:42:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class FontUtil {

    private static final Logger log = LoggerFactory.getLogger(FontUtil.class);

    public static int NORMAL_FONT_SIZE = 12;
    public static int SMALL_FONT_SIZE = 10;
    public static int BIG_FONT_SIZE = 14;

    private Vector<String> monospaceFontFamilyNames = getAvailableMonospaceFontFailyNames();
    private String commonMonospaceFontFamilyName = getCommonOrFirstMonospaceFontFailyName();

    private static Integer singletonLock = new Integer(0);
    private static FontUtil instance = null;

    public static FontUtil getInstance() {
        if (instance == null) {
            synchronized (singletonLock) {
                instance = new FontUtil();
                return instance;
            }
        } else {
            return instance;
        }
    }

    public String[] getAvailableFontnames() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return graphicsEnvironment.getAvailableFontFamilyNames();
    }

    public Vector<String> getAvailableMonospaceFontFailyNames() {
        String names[] = getAvailableFontnames();
        Vector<String> monospace = new Vector<String>();

        for(String name : names) {
            //log.info("checking " + name);

            if (isFontMonospaced(name)) {
                //log.info("-------- MONOSPACED ---------");
                monospace.add(name);
            }
        }

        return monospace;
    }

    public String getCommonMonospaceFontFailyName() {
        return commonMonospaceFontFamilyName;
    }

    public String getCommonOrFirstMonospaceFontFailyName() {
        for (String name : monospaceFontFamilyNames) {
            if (name.contains("Courier")) {
                return name;
            }
        }
        return monospaceFontFamilyNames.get(0);
    }

    public boolean isFontMonospaced(String fontFamilyName) {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.createGraphics();

        boolean isMonospaced = true;

        int fontStyle = Font.PLAIN;
        int fontSize = 12;

        Font font = new Font(fontFamilyName, fontStyle, fontSize);
        FontMetrics fontMetrics = graphics.getFontMetrics(font);

        int firstCharacterWidth = 0;
        boolean hasFirstCharacterWidth = false;
        for (int codePoint = 0; codePoint < 128; codePoint++) {
            if (Character.isValidCodePoint(codePoint) &&
                    (Character.isLetter(codePoint) || Character.isDigit(codePoint))) {

                char character = (char) codePoint;
                int characterWidth = fontMetrics.charWidth(character);
                if (hasFirstCharacterWidth) {
                    if (characterWidth != firstCharacterWidth) {
                        isMonospaced = false;
                        break;
                    }
                } else {
                    firstCharacterWidth = characterWidth;
                    hasFirstCharacterWidth = true;
                }
            }
        }

        return isMonospaced;
    }

}
