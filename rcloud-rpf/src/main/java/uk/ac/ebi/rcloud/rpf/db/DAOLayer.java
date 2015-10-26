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
import uk.ac.ebi.rcloud.rpf.db.data.ProjectDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.ServerDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * Created by andrew on 13/10/15.
 */
public class DAOLayer implements DAOLayerInterface {

    private DBLayerInterface dbLayer = null;

    public DAOLayer(DBLayerInterface dbLayer) {
        this.dbLayer = dbLayer;
    }

    //   P R O J E C T S
    //
    //
    public Vector<ProjectDataDAO> getProjectsOwnedByUser(String owner) throws RemoteException {
        Vector<ProjectDataDB> projects = dbLayer.getProjectsByOwner(owner);
        Vector<ProjectDataDAO> projectsDAOList = new Vector<ProjectDataDAO>();

        for (ProjectDataDB p : projects) {
            projectsDAOList.add(DAO2DB.convert2ProjectDAO(p));
        }

        return projectsDAOList;
    }

    public void createProject(ProjectDataDAO project) throws AlreadyBoundException, RemoteException {
        dbLayer.createProject(DAO2DB.convert2ProjectDB(project));
    }
     public void deleteProject(ProjectDataDAO project) throws RemoteException, NotBoundException {
        dbLayer.deleteProject(DAO2DB.convert2ProjectDB(project));
    }

    public ProjectDataDAO getProject(String projectId) throws RemoteException {
        ProjectDataDB p = dbLayer.getProject(projectId);
        return DAO2DB.convert2ProjectDAO( p );
    }

    public void updateProjectDescription(ProjectDataDAO project) throws RemoteException {
        dbLayer.updateProjectDescription(DAO2DB.convert2ProjectDB( project ));
    }

    //   S E R V E R S
    //
    //
    public void registerPingFailure(String serverName) throws RemoteException, NotBoundException {
        dbLayer.registerPingFailure(serverName);
    }

    public void setProject(String name, String project) throws RemoteException {
        dbLayer.setProject(name, project);
    }

    public Vector<ServerDataDAO> getServerByProjectId(String projectId) throws RemoteException {
        Vector<ServerDataDB> servers = dbLayer.getServersByProjectId(projectId);

        Vector<ServerDataDAO> serverDAOList = new Vector<ServerDataDAO>();
        for (ServerDataDB s : servers) {
            serverDAOList.add(DAO2DB.convert2ServerDAO( s ));
        }
        return serverDAOList;
    }

    public Vector<ServerDataDAO> getServersByOwner(String owner) throws RemoteException {
        Vector<ServerDataDB> servers = dbLayer.getServersByOwner(owner);

        Vector<ServerDataDAO> serverDAOList = new Vector<ServerDataDAO>();
        for (ServerDataDB s : servers) {
            serverDAOList.add(DAO2DB.convert2ServerDAO( s ));
        }
        return serverDAOList;
    }

    //   U S E R S
    //
    //
    public UserDataDAO getUserByUsername(String username) throws RemoteException {
        UserDataDB user = dbLayer.getUser(username);
        return DAO2DB.convert2UserDAO( user );
    }


    public void updateUser(UserDataDAO user) throws RemoteException {
        dbLayer.updateUser(DAO2DB.convert2UserDB( user ));
    }

    //   O P T I O N S
    //
    //
    public OptionDataDAO getOption(String optionName) throws RemoteException {
        return DAO2DB.convert2OptionDAO(dbLayer.getOption(optionName));
    }

}
