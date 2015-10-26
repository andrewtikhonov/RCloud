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
import uk.ac.ebi.rcloud.rpf.db.data.*;

import java.io.InputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

public interface DBLayerInterface extends Registry {

    // common
    public void lock() throws RemoteException ;
    public void unlock() throws RemoteException;
    public void commit() throws SQLException;
    public boolean canReconnect();
    public Vector<HashMap<String, Object>> getTableData(String tableName) throws RemoteException;
    public Vector<HashMap<String, Object>> getTableData(String tableName, String condition) throws RemoteException;

    // servers
    public Vector<String> list(String[] prefixes) throws RemoteException;
    public Vector<HashMap<String, Object>> listKillable() throws RemoteException;
    public Vector<HashMap<String, Object>> listKillable(String nodeIp, String nodePrefix) throws RemoteException;

    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException ;
    public void bind(ServerDataDB server) throws RemoteException, AlreadyBoundException;
	public void rebind(String name, Remote obj) throws RemoteException;
    public Vector<ServerDataDB> getServerData(String condition) throws RemoteException;
    public ServerDataDB getServerRecord(String serverName) throws RemoteException;
    public Vector<ServerDataDB> getServersByProjectId(String projectId) throws RemoteException;
    public Vector<ServerDataDB> getServersByOwner(String owner) throws RemoteException;
    public String getNameFromStub(Remote stub) throws RemoteException;

    public void reserve(String name) throws RemoteException, NotBoundException;
    public void unReserve(String name) throws RemoteException, NotBoundException;
    public void unlockServant(String servantName) throws RemoteException;

    public void registerPingFailure(String name) throws RemoteException, NotBoundException;
    public void updateServantNodeName(String servantName, String nodeName) throws RemoteException, NotBoundException;
    public void updateServantAttributes(String servantName, HashMap<String, Object> attributes) throws RemoteException, NotBoundException;

    public void setOwner(String name, String owner) throws RemoteException;
    public void setProject(String name, String project) throws RemoteException;
    public void setJobID(String servantName, String jobID) throws RemoteException;
    public void setMaster(String serverName, String masterName) throws RemoteException;
    public void setOvertimeNotificationSent(String serverName) throws RemoteException;
    public void releaseFreeServersRunningLongerThan(int hours) throws RemoteException;

    // nodes
    public void addNode(NodeDataDB nodeData) throws RemoteException;
    public void removeNode(String nodeName) throws RemoteException;
    public void updateNode(NodeDataDB nodeData) throws RemoteException;
    public Vector<NodeDataDB> getNodeData(String condition) throws RemoteException;
    public void incrementNodeProcessCounter(String nodeName) throws RemoteException, NotBoundException;

    // pools
    public void addPool(PoolDataDB poolData) throws RemoteException;
    public void removePool(String poolName) throws RemoteException;
    public void updatePool(PoolDataDB poolData) throws RemoteException;
    public Vector<PoolDataDB> getPoolData() throws RemoteException;
    public HashMap<String, PoolDataDB> getPoolDataHashMap() throws RemoteException;

    // users
    public void createUser(UserDataDB user) throws AlreadyBoundException, RemoteException;
    public void deleteUser(UserDataDB user) throws RemoteException, NotBoundException;
    public Vector<UserDataDB> getUserData(String condition) throws RemoteException;
    public UserDataDB getUser(String login) throws RemoteException;
    public void updateUser(UserDataDB user) throws RemoteException;
    public void updateUserClusterDetails(UserDataDB user) throws RemoteException;
    public void updateUserLoggedIn(String login) throws RemoteException;
    public void updateUserLoggedOut(String login) throws RemoteException;

    // projects
    public void createProject(ProjectDataDB project) throws AlreadyBoundException, RemoteException;
    public void deleteProject(ProjectDataDB project) throws RemoteException, NotBoundException;
    public Vector<ProjectDataDB> getProjectData(String condition) throws RemoteException;
    public Vector<ProjectDataDB> getProjectsByOwner(String owner) throws RemoteException;
    public ProjectDataDB getProject(String projectid) throws RemoteException;
    public void updateProject(ProjectDataDB project) throws RemoteException;
    public void updateProjectBasefolder(ProjectDataDB project) throws RemoteException;
    public void updateProjectDescription(ProjectDataDB project) throws RemoteException;
    public void updateProjectOpened(String projectid) throws RemoteException;
    public void updateProjectOnHold(String projectid) throws RemoteException;
    public void updateProjectStopped(String projectid) throws RemoteException;
    public void updateProjectActivity(String projectid) throws RemoteException;
    public void updateProjectOwnerNotified(String projectid) throws RemoteException;

    // options
    public void createOption(OptionDataDB option) throws AlreadyBoundException, RemoteException;
    public void deleteOption(OptionDataDB option) throws RemoteException, NotBoundException;
    public Vector<OptionDataDB> getOptionData(String condition) throws RemoteException;
    public OptionDataDB getOption(String optionname) throws RemoteException;
    public void updateOption(OptionDataDB option) throws RemoteException;

    // site data
    public void createSite(SiteDataDB sitedata) throws AlreadyBoundException, RemoteException;
    public void deleteSite(String sitename) throws RemoteException, NotBoundException;
    public Vector<SiteDataDB> getSiteData(String condition) throws RemoteException;
    public SiteDataDB getSite(String sitename) throws RemoteException;
    public void updateSite(SiteDataDB sitedata) throws RemoteException;

    // time & date
    public TimestampDataDB getTimestamp() throws RemoteException;

    //select SUBSTR(OVERALL_TIME, 1,30) from PROJECTS;
}
