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
package uk.ac.ebi.rcloud.server;

import uk.ac.ebi.rcloud.rpf.ManagedServant;
import uk.ac.ebi.rcloud.server.file.FileNode;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 5, 2009
 * Time: 3:49:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ManagedRServer extends ManagedServant {

    // owner
    //
    public void setOwner(String owner) throws java.rmi.RemoteException;

    public String getOwner() throws java.rmi.RemoteException;

    // project
    //
    public void setProject(String project) throws java.rmi.RemoteException;

    public String getProject() throws java.rmi.RemoteException;

    // working dir
    //
    public void setRootDirectory(String root) throws java.rmi.RemoteException;

    // working dir
    //
    public FileNode readDirectory(String directory) throws java.rmi.RemoteException;

    // cluster
    //
    public void setMaster(String master) throws java.rmi.RemoteException;

    public String getMaster() throws java.rmi.RemoteException;

}
