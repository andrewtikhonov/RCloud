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
package uk.ac.ebi.rcloud.server.file;

import uk.ac.ebi.rcloud.server.file.FileDescription;

import java.io.Serializable;
import java.io.File;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 4, 2009
 * Time: 11:29:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileNode3 implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private int sqn           = 0;
    private static int MAXGSN = 65535;

    private FileDescription file;

    private Vector<Object>  k = new Vector<Object>();

    public FileNode3(FileDescription file) {
        this.file = file;
    }

    public Vector<Object> getChildren() {
        return k;
    }

    public void addChildrenNodes(File[] list) {
        for (File f : list) {
            k.add(new FileNode3(new FileDescription(f)));
        }
    }

    public FileDescription getFile() {
        return file;
    }

    public String toString() {
        return file.getName();
    }

    public int getSQN() {
        return sqn;
    }

    public void setSQN(int sqn) {
        this.sqn = sqn;
    }

    public boolean isSameFile(FileNode3 node) {
        return (this.file.getPath().equals(node.getFile().getPath()));
    }

    public boolean isUpToDate(FileNode3 node) {
        return (this.file.length()       == node.getFile().length() &&
                this.file.lastModified() == node.getFile().lastModified());
    }

    public void update(FileNode3 node) {
        this.file.setLength(node.getFile().length());
        this.file.setLastModified(node.getFile().lastModified());
    }
}
