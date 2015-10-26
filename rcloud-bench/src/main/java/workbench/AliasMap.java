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
package workbench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 15, 2009
 * Time: 10:30:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class AliasMap {

    final private static Logger log = LoggerFactory.getLogger(AliasMap.class);

    private static HashMap<String, String> aliasNames = new HashMap<String, String>();
    private static HashMap<String, String> systemNames = new HashMap<String, String>();

    public static String getServerAliasName(String name) {

        int index = Integer.parseInt(getNameIndex(name));

        int namestotal = aliasNames.size();

        int div = index / namestotal + 1;
        int rem = index % namestotal;

        String key = Integer.toString(rem);

        return  aliasNames.get(key) + (div == 1 ? "" : " " + Integer.toString(div));
    }

    private static String getNameIndex(String name) {
        int len = name.length();
        String digits = null;
        for (int i=len-1;i>=0;i--) {
            if (!Character.isDigit(name.charAt(i))) {
                digits = name.substring(i + 1);
                break;
            }
        }
        return digits;
    }

    public static void mapSystemName2Alias(String systemName, String aliasName) {
        aliasNames.put(systemName, aliasName);
        systemNames.put(aliasName, systemName);
    }

    static {
        int i = 1;

        String[] aliasNames = WorkbenchConfig.getInstance().getAliasNames();

        for (String alias : aliasNames) {
            mapSystemName2Alias(Integer.toString(i++), alias);
        }
    }

    private static void debugLogName(String name) {
        log.info(name+" = "+getServerAliasName(name));
    }

    public static void main(String[] args) {
        debugLogName("COOLCAT_1");
        debugLogName("COOLCAT_2");
        debugLogName("COOLCAT_4");
        debugLogName("COOLCAT_45");
        debugLogName("COOLCAT_1470");
        debugLogName("BENCH_4");

    }

}
