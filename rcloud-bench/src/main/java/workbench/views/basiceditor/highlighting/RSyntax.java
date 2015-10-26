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
package workbench.views.basiceditor.highlighting;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 17, 2009
 * Time: 4:24:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSyntax {
    private static HashSet keywords = new HashSet();

    static
    {

        keywords.add( "break" );
        keywords.add( "class" );
        keywords.add( "do" );
        keywords.add( "done" );
        keywords.add( "else" );
        keywords.add( "F" );
        keywords.add( "FALSE" );
        keywords.add( "for" );
        keywords.add( "function" );
        keywords.add( "if" );
        keywords.add( "ifelse" );
        keywords.add( "in" );
        keywords.add( "Inf" );
        keywords.add( "inherits" );
        keywords.add( "NA" );
        keywords.add( "NaN" );
        keywords.add( "next" );
        keywords.add( "NULL" );
        keywords.add( "repeat" );
        keywords.add( "return" );
        keywords.add( "switch" );
        keywords.add( "T" );
        keywords.add( "then" );
        keywords.add( "TRUE" );
        keywords.add( "unclass" );
        keywords.add( "while" );
    }

    public static HashSet keywords()
    {
        return keywords;
    }
}
