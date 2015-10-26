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

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 3, 2009
 * Time: 11:04:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class RUtils {

    private static final String V_NAME_PREFIXE = "B__";
    private static final String V_TEMP_PREFIXE = V_NAME_PREFIXE + "BTEMP__";
    private static long _varCounter = 0L;
    private static long _tempCounter = 0L;

    public static synchronized String newTemporaryVariableName() {
        return V_TEMP_PREFIXE + _tempCounter++;
    }

    public static synchronized String newVariableName() {
        return V_NAME_PREFIXE + _varCounter++;
    }

}
