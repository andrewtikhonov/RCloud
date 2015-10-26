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
package workbench.dialogs.packageinstaller;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 1, 2009
 * Time: 12:48:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstallerTableItem {
    public final static int NOTINSTALLED = 0;
    public final static int INSTALLED    = 1;
    public final static String UNDEFINED = "";

    public String name;
    public int    status;
    public String installedversion;
    public String repositoryversion;

    public InstallerTableItem(String name, int status, String installedversion, String repositoryversion) {
        this.name   = name;
        this.status = status;
        this.installedversion = installedversion;
        this.repositoryversion = repositoryversion;
    }

    public InstallerTableItem(String name) {
        this(name, NOTINSTALLED, UNDEFINED, UNDEFINED);
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public String getInstalledVersion() {
        return installedversion;
    }

    public String getRepositoryVersion() {
        return repositoryversion; 
    }

    public void setRepositoryVersion(String ver) {
        this.repositoryversion = ver;
    }

    public void setInstalledVersion(String ver) {
        this.installedversion = ver;
    }

    public void setStatus(int status) {
        this.status = status; 
    }

}
