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
package uk.ac.ebi.rcloud.http.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 6, 2009
 * Time: 4:57:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServletUtils {

    private static Vector<String> orderP(String[] keys) {
        Arrays.sort(keys);
        Vector<String> result = new Vector<String>();
        for (int i = 0; i < keys.length; ++i)
            result.add((String) keys[i]);
        return result;
    }

    public static Vector<String> orderO(Collection<Object> c) {
        String[] keys = new String[c.size()];
        int i = 0;
        for (Object k : c)
            keys[i++] = (String) k;
        return orderP(keys);
    }

    public static Vector<String> orderS(Collection<String> c) {
        String[] keys = new String[c.size()];
        int i = 0;
        for (String k : c)
            keys[i++] = k;
        return orderP(keys);
    }

}
