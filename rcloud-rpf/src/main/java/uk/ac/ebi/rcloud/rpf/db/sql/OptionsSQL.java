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
package uk.ac.ebi.rcloud.rpf.db.sql;

import uk.ac.ebi.rcloud.rpf.db.data.OptionDataDB;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 21, 2010
 * Time: 3:11:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class OptionsSQL extends SqlBase {

    public static String optionExistsStatement(String optionname) {
        return "select count(*) from OPTIONS where OPTION_NAME=" + wrap(optionname);
    }

    public static String deleteOptionStatement(String optionname) {
        return "DELETE FROM OPTIONS WHERE OPTION_NAME=" + wrap(optionname);
    }

    public static String updateOptionStatement(OptionDataDB option) {
        return "UPDATE OPTIONS SET OPTION_VALUE="+wrap(option.getOptionValue())+
                " WHERE OPTION_NAME=" + wrap(option.getOptionName());
    }

    public static String addOptionStatement(OptionDataDB option) {
        String statement = "INSERT INTO OPTIONS (OPTION_NAME, OPTION_VALUE) "
                + " VALUES ("
                + wrap( option.getOptionName() ) + ","
                + wrap( option.getOptionValue() ) + ")";

        return statement;
    }

}
