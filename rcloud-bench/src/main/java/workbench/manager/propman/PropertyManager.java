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
package workbench.manager.propman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 14, 2009
 * Time: 1:20:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyManager {

    private static final Logger log = LoggerFactory.getLogger(PropertyManager.class);
    private static final Preferences staticPrefs =
            Preferences.userNodeForPackage(PropertyManager.class);

    private Preferences prefs;

    public PropertyManager(Preferences prefs) {
        this.prefs = prefs;
    }

    public String getProperty(String name, String defaultValue) {
        return prefs.get(name, defaultValue);
    }

    public void setProperty(String name, String value) {
        prefs.put(name, value);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            for (String key : prefs.keys()) {
                sb.append(key + "=" + prefs.get(key, null) + " ");
            }
            return sb.toString();
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getPreference(String name, String defaultValue) {
        return staticPrefs.get(name, defaultValue);
    }

    public static void setPreference(String name, String value) {
        staticPrefs.put(name, value);
    }


}
