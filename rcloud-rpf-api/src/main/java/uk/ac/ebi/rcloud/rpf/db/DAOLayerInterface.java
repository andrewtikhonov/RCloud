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
package uk.ac.ebi.rcloud.rpf.db;

import uk.ac.ebi.rcloud.rpf.db.dao.OptionDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.ServerDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * Created by andrew on 12/10/15.
 */
public interface DAOLayerInterface {

    // projects
    public Vector<ProjectDataDAO> getProjectsOwnedByUser(String owner) throws RemoteException;
    public void createProject(ProjectDataDAO project) throws AlreadyBoundException, RemoteException;
    public void deleteProject(ProjectDataDAO project) throws RemoteException, NotBoundException;
    public ProjectDataDAO getProject(String projectId) throws RemoteException;
    public void updateProjectDescription(ProjectDataDAO project) throws RemoteException;

    // servers
    public void registerPingFailure(String serverName) throws RemoteException, NotBoundException;
    public void setProject(String name, String project) throws RemoteException;
    public Vector<ServerDataDAO> getServerByProjectId(String projectId) throws RemoteException;
    public Vector<ServerDataDAO> getServersByOwner(String owner) throws RemoteException;

    // users
    public UserDataDAO getUserByUsername(String username) throws RemoteException;
    public void updateUser(UserDataDAO user) throws RemoteException;

    // option
    public OptionDataDAO getOption(String optionName) throws RemoteException;

}
