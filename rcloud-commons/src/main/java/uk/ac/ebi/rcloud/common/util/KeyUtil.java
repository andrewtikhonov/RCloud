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

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 26, 2009
 * Time: 10:44:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class KeyUtil {
    private static Logger log = LoggerFactory.getLogger(KeyUtil.class);

    private static HashMap<KeyStroke, String> map = new HashMap<KeyStroke, String>();
    private static boolean onMac = OsUtil.isMacOs();
    public static boolean debug = false;


    public static KeyStroke getKeyStroke(int i0, int i1) {

        if ((i1 & KeyEvent.META_MASK) != 0) {
            if (!onMac) {
                i1 = i1 - KeyEvent.META_MASK + KeyEvent.CTRL_MASK;
            }
        }

        KeyStroke ks = KeyStroke.getKeyStroke(i0, i1);

        if (debug) {
            Exception ex = new Exception();
            StackTraceElement[] trace = ex.getStackTrace();
            String caller = trace[1].toString();
            if (map.containsKey(ks)) {
                log.info("ks (" + ks.toString() + ") being mapped in (" + caller + ") already mapped in (" + map.get(ks) + ")");
            } else {
                map.put(ks, caller);
            }
        }

        return ks;
    }
}
