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

import java.net.URI;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 6, 2010
 * Time: 1:29:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrowserUtil {

    public static boolean isWebBrowserSupported() throws Exception {
        return java.awt.Desktop.isDesktopSupported();
    }

    public static void showDocument(URL url) throws Exception {
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }


}
