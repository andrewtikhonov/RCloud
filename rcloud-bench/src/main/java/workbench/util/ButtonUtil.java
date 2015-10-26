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

import uk.ac.ebi.rcloud.common.components.button.JRoundButton;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 27/06/2011
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public class ButtonUtil {

    public static Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public static int DEFAULT_BUTTON_SIZE = 16; // 24

    public static JButton makeButton(String caption) {
        return makeButton(caption, null, null);
    }


    public static JButton makeButton(String caption, String iconPath, String tooltip) {

        JButton b = caption != null ? new JRoundButton(caption) : new JRoundButton();

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);

        // if "true", causes problems as
        // lot's of changes are actualized
        // on focusLost event
        //
        //b.setFocusable(false);

        b.setCursor(hand);

        if (tooltip != null) {
            b.setToolTipText(tooltip);
        }

        if (iconPath != null) {
            ImageIcon icon = new ImageIcon(
                    ImageLoader.load(iconPath));

            if (icon.getImage().getWidth(null) > DEFAULT_BUTTON_SIZE ||
                    icon.getImage().getHeight(null) > DEFAULT_BUTTON_SIZE) {

                icon.setImage(GraphicsUtilities.scaleImage(icon.getImage(), DEFAULT_BUTTON_SIZE));
            }

            b.setIcon(icon);
        }

        return(b);
    }

    public static JButton makeRoundButton(String caption, String iconPath) {

        JButton b = caption != null ? new JRoundButton(caption) : new JRoundButton();

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);

        // if "true", focusLost events are not fired
        // and UI changes are not actualized
        //
        //
        //b.setFocusable(false);

        b.setCursor(hand);

        if (iconPath != null) {
            ImageIcon icon = new ImageIcon(
                    ImageLoader.load(iconPath));
            icon.setImage(GraphicsUtilities.scaleImage(icon.getImage(), 24));
            b.setIcon(icon);
        }

        return(b);
    }


}
