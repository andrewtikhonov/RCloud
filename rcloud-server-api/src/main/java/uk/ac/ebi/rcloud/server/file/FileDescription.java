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

import uk.ac.ebi.rcloud.server.file.FileInterface;

import java.io.Serializable;
import java.io.File;

public class FileDescription implements Serializable, FileInterface {

    private static final long serialVersionUID = 1L;
    
	private String _name;
    private String _path;
	private long _length;
	private boolean _isDir;
	private long _lastModified;

	public FileDescription() {
	}

	public FileDescription(File file) { //String name, long size, boolean isDir, Date modifiedOn
		this._name = file.getName();
		this._length = file.length();
		this._isDir = file.isDirectory();
		this._lastModified = file.lastModified();
        this._path = file.getPath();
	}

    public void cloneData(FileDescription desc) {
        this._name = desc.getName();
        this._length = desc.length();
        this._isDir = desc.isDirectory();
        this._lastModified = desc.lastModified();
        this._path = desc.getPath();
    }

	public long lastModified () {
		return _lastModified;
	}

	public void setLastModified(long lastModified) {
		this._lastModified = lastModified;
	}

	public String getName() {
		return _name;
	}

	public void setName(String path) {
		this._path = path;
	}

    public String getPath() {
        return _path;
    }

    public void setPath(String name) {
        this._name = name;
    }

	public long length() {
		return _length;
	}

	public void setLength(long length) {
		this._length = length;
	}

	public boolean isDirectory() {
		return _isDir;
	}

	public void setIsDirectory(boolean isDirectory) {
		this._isDir = isDirectory;
	}

	public String toString() {
		return getName();
	}

/*
	private String n;
    private String p;
	private long   l;
	private boolean d;
	private long m;

	public FileDescription() {
	}

	public FileDescription(File file) {
        this.n = file.getName();
        this.p = file.getPath();
		this.l = file.length();
		this.d = file.isDirectory();
		this.m = file.lastModified();
	}

    public void cloneData(FileDescription desc) {
        this.n = desc.getName();
        this.p = desc.getPath();
        this.l = desc.length();
        this.d = desc.isDirectory();
        this.m = desc.lastModified();
    }

    public String getName() {
        int index = p.lastIndexOf(File.pathSeparator);
        return p.substring(index + 1);
    }

    public String getPath() {
        return p;
    }

    public void setPath(String path) {
        this.p = path;
    }
    
	public long lastModified () {
		return m;
	}

	public void setLastModified(long lastModified) {
		this.m = lastModified;
	}

	public long length() {
		return l;
	}

	public void setLength(long length) {
		this.l = length;
	}

	public boolean isDirectory() {
		return d;
	}

	public void setIsDirectory(boolean isDirectory) {
		this.d = isDirectory;
	}

	public String toString() {
		return getName();
	}
	*/
}
