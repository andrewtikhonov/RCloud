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
package uk.ac.ebi.rcloud.common.components.dialog;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 18, 2010
 * Time: 3:56:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class JDialogBase extends JDialog {

    private static String propString = System.getProperty("dialog-decoration");
    private static boolean doDecoration = (propString != null && propString.equalsIgnoreCase("true"));

    public JDialogBase(){
        super();
    }

    public JDialogBase(Frame owner, boolean modal) {
        super(owner, modal);
    }

    public JDialogBase(Frame owner, String title) {
        super(owner, title);
    }

    public boolean isDecorationSupported(){
        return doDecoration;
    }
    
    public static final String WDRAG = "apple.awt.draggableWindowBackground";
    public void fixWindowDragging(Window w) {
        if (w instanceof RootPaneContainer) {
            JRootPane p = ((RootPaneContainer)w).getRootPane();
            Boolean oldDraggable = (Boolean)p.getClientProperty(WDRAG);
            if (oldDraggable == null) {
                p.putClientProperty(WDRAG, Boolean.FALSE);
                if (w.isDisplayable()) {
                    String context = "-context-";
                    System.err.println(context + "(): To avoid content dragging, "
                            + context + "() must be called before the window is realized, or "
                            + WDRAG + " must be set to Boolean.FALSE before the window is realized. \n"
                            + " If you really want content dragging, set "
                            + WDRAG + " on the window's root pane to Boolean.TRUE before calling "
                            + context + "() to hide this message.");
                }
            }
        }
    }

}
