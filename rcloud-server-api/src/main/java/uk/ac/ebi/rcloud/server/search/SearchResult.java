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

import uk.ac.ebi.rcloud.server.file.FileNode;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 1, 2010
 * Time: 2:10:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchResult implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private FileNode file;
    private String matchedtext;
    private int linenum;

    public SearchResult(FileNode file, String matchedtext, int linenum) {
        this.file = file;
        this.matchedtext = matchedtext;
        this.linenum = linenum;
    }

    public FileNode getFile() {
        return file;
    }

    public String getMatchedtext() {
        return matchedtext;
    }

    public int getLinenum() {
        return linenum;
    }

    public void setFile(FileNode file) {
        this.file = file;
    }

    public void setMatchedtext(String matchedtext) {
        this.matchedtext = matchedtext;
    }

    public void setLinenum(int linenum) {
        this.linenum = linenum;
    }


    public String toString() {
        return "[" + linenum + "] " + matchedtext;
    }

}
