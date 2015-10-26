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

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 17/02/2012
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class RSyntaxDocumentChange {

    public static final int UNDEF  = 0;
    public static final int REMOVE = 1;
    public static final int INSERT = 2;

    public int op      = UNDEF;
    public int offset  = 0;
    public int length  = 0;
    public String str  = null;

    public RSyntaxDocumentChange(int op, int offset, int length, String str) {
        this.op     = op;
        this.offset = offset;
        this.length = length;
        this.str    = str;
    }

}
