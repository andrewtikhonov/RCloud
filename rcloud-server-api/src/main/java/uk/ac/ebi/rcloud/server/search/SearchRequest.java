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
package uk.ac.ebi.rcloud.server.search;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 1, 2010
 * Time: 2:15:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path = null;
    private String pattern = null;
    private String mask = null;
    private boolean usemask = false;
    private boolean casesensitive = false;
    private boolean wholewords = false;
    private boolean recursive = false;

    public SearchRequest(String path, String pattern, String mask, boolean usemask, boolean casesensitive, boolean wholewords, boolean recursive) {
        this.path = path;
        this.pattern = pattern;
        this.mask = mask;
        this.usemask = usemask;
        this.casesensitive = casesensitive;
        this.wholewords = wholewords;
        this.recursive = recursive;
    }

    public String getPath() {
        return path;
    }

    public String getPattern() {
        return pattern;
    }

    public String getMask() {
        return mask;
    }

    public boolean isUsemask() {
        return usemask;
    }

    public boolean isCasesensitive() {
        return casesensitive;
    }

    public boolean isWholewords() {
        return wholewords;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public String toString() {
        return "Search for: " + pattern + " in " + path + " mask: " + mask;
    }

}
