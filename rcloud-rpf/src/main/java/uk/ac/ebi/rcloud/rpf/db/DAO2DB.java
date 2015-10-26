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
import uk.ac.ebi.rcloud.rpf.db.data.OptionDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.ProjectDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.ServerDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;

import java.util.HashMap;

/**
 * Created by andrew on 12/10/15.
 */
public class DAO2DB {

    public static class DBKeyDAOKeyPair {
        public String daoKey;
        public String dbKey;
        public DBKeyDAOKeyPair(String daoKeyValue, String dbKeyValue) {
            this.daoKey = daoKeyValue;
            this.dbKey = dbKeyValue;
        }
    }

    //   U S E R S
    //
    // mapping: dao key <-> db key
    //
    public static DBKeyDAOKeyPair[] userConversionKeyMap =
            {                                                                                   // DAO
                    new DBKeyDAOKeyPair(UserDataDAO.username, UserDataDB.LOGIN),                // used
                    new DBKeyDAOKeyPair(UserDataDAO.password, UserDataDB.PWD),                  // used
                    new DBKeyDAOKeyPair(UserDataDAO.status, UserDataDB.STATUS),                 // used
                    new DBKeyDAOKeyPair(UserDataDAO.userFolder, UserDataDB.USERFOLDER),         // used
                    new DBKeyDAOKeyPair(UserDataDAO.fullName, UserDataDB.FULLNAME),             // ??
                    new DBKeyDAOKeyPair(UserDataDAO.email, UserDataDB.EMAIL),                   // used
                    new DBKeyDAOKeyPair(UserDataDAO.registerTime, UserDataDB.REGISTERED),       // not used
                    new DBKeyDAOKeyPair(UserDataDAO.profile, UserDataDB.PROFILE),               // not used
                    new DBKeyDAOKeyPair(UserDataDAO.lastLoggedIn, UserDataDB.LAST_LOGGED_IN),   // not used
                    new DBKeyDAOKeyPair(UserDataDAO.lastLoggedOut, UserDataDB.LAST_LOGGED_OUT), // not used
                    new DBKeyDAOKeyPair(UserDataDAO.timesLoggedIn, UserDataDB.TIMES_LOGGED_IN), // not used
                    new DBKeyDAOKeyPair(UserDataDAO.poolName, UserDataDB.POOLNAME),             // not used
                    new DBKeyDAOKeyPair(UserDataDAO.supervisor, UserDataDB.SUPERVISOR),         // not used
                    new DBKeyDAOKeyPair(UserDataDAO.groupName, UserDataDB.GROUPNAME),           // not used
                    new DBKeyDAOKeyPair(UserDataDAO.groupHead, UserDataDB.GROUPHEAD)            // not used
            };


    // users
    public static UserDataDAO convert2UserDAO(UserDataDB dbUser) {

        HashMap<String, Object> dbMap = dbUser.getMap();
        HashMap<String, Object> daoMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: userConversionKeyMap) {
            daoMap.put(p.daoKey, dbMap.get(p.dbKey));
        }

        return new UserDataDAO(daoMap);
    }

    public static UserDataDB  convert2UserDB(UserDataDAO daoUser) {
        HashMap<String, Object> daoMap = daoUser.getMap();
        HashMap<String, Object> dbMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: userConversionKeyMap) {
            dbMap.put(p.dbKey, daoMap.get(p.daoKey));
        }

        return new UserDataDB(dbMap);

    }

    //   P R O J E C T S
    //
    // mapping: dao key <-> db key
    //
    public static DBKeyDAOKeyPair[] projectConversionKeyMap =
            {                                                                                           // DAO
                    new DBKeyDAOKeyPair(ProjectDataDAO.title, ProjectDataDB.TITLE),                     // used
                    new DBKeyDAOKeyPair(ProjectDataDAO.identifier, ProjectDataDB.IDENTIFIER),           // used
                    new DBKeyDAOKeyPair(ProjectDataDAO.owner, ProjectDataDB.OWNER),                     // used
                    new DBKeyDAOKeyPair(ProjectDataDAO.status, ProjectDataDB.STATUS),                   // used
                    new DBKeyDAOKeyPair(ProjectDataDAO.description, ProjectDataDB.DESC),                // used
                    new DBKeyDAOKeyPair(ProjectDataDAO.createdTime, ProjectDataDB.CREATED),             //
                    new DBKeyDAOKeyPair(ProjectDataDAO.lastOpened, ProjectDataDB.LAST_OPENED),          //
                    new DBKeyDAOKeyPair(ProjectDataDAO.lastClosed, ProjectDataDB.LAST_CLOSED),          //
                    new DBKeyDAOKeyPair(ProjectDataDAO.timesOpened, ProjectDataDB.TIMES_OPENED),        //
                    new DBKeyDAOKeyPair(ProjectDataDAO.overallTime, ProjectDataDB.OVERALL_TIME),        //
                    new DBKeyDAOKeyPair(ProjectDataDAO.baseFolder, ProjectDataDB.BASEFOLDER),           //
                    new DBKeyDAOKeyPair(ProjectDataDAO.lastActivity, ProjectDataDB.LAST_ACTIVITY),      //
                    new DBKeyDAOKeyPair(ProjectDataDAO.isNotified, ProjectDataDB.NOTIFIED)              //
            };

    // projects
    public static ProjectDataDAO convert2ProjectDAO(ProjectDataDB dbProject) {
        HashMap<String, Object> dbMap = dbProject.getMap();
        HashMap<String, Object> daoMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: projectConversionKeyMap) {
            daoMap.put(p.daoKey, dbMap.get(p.dbKey));
        }

        return new ProjectDataDAO(daoMap);
    }

    public static ProjectDataDB convert2ProjectDB(ProjectDataDAO daoProject) {
        HashMap<String, Object> daoMap = daoProject.getMap();
        HashMap<String, Object> dbMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: projectConversionKeyMap) {
            dbMap.put(p.dbKey, daoMap.get(p.daoKey));
        }

        return new ProjectDataDB(dbMap);
    }


    //   S E R V E R
    //
    // mapping: dao key <-> db key
    //
    public static DBKeyDAOKeyPair[] serverConversionKeyMap =
            {
                    new DBKeyDAOKeyPair(ServerDataDAO.serverName, ServerDataDB.NAME),
                    new DBKeyDAOKeyPair(ServerDataDAO.serverInUse, ServerDataDB.IN_USE),
                    new DBKeyDAOKeyPair(ServerDataDAO.pingFailures, ServerDataDB.PING_FAILURES),
                    new DBKeyDAOKeyPair(ServerDataDAO.nodeName, ServerDataDB.NODE_NAME),
                    new DBKeyDAOKeyPair(ServerDataDAO.registerTime, ServerDataDB.REGISTER_TIME),
                    new DBKeyDAOKeyPair(ServerDataDAO.processId, ServerDataDB.PROCESS_ID),
                    new DBKeyDAOKeyPair(ServerDataDAO.hostName, ServerDataDB.HOST_NAME),
                    new DBKeyDAOKeyPair(ServerDataDAO.hostIp, ServerDataDB.HOST_IP),
                    new DBKeyDAOKeyPair(ServerDataDAO.hostOs, ServerDataDB.OS),
                    new DBKeyDAOKeyPair(ServerDataDAO.borrowTime, ServerDataDB.BORROW_TIME),
                    new DBKeyDAOKeyPair(ServerDataDAO.borrowProcessId, ServerDataDB.BORROW_PROCESS_ID),
                    new DBKeyDAOKeyPair(ServerDataDAO.borrowHostName, ServerDataDB.BORROW_HOST_NAME),
                    new DBKeyDAOKeyPair(ServerDataDAO.borrowHostIp, ServerDataDB.BORROW_HOST_IP),
                    new DBKeyDAOKeyPair(ServerDataDAO.returnTime, ServerDataDB.RETURN_TIME),
                    new DBKeyDAOKeyPair(ServerDataDAO.returnProcessId, ServerDataDB.RETURN_PROCESS_ID),
                    new DBKeyDAOKeyPair(ServerDataDAO.returnProcessId, ServerDataDB.RETURN_PROCESS_ID),
                    new DBKeyDAOKeyPair(ServerDataDAO.returnHostName, ServerDataDB.RETURN_HOST_NAME),
                    new DBKeyDAOKeyPair(ServerDataDAO.returnHostIp, ServerDataDB.RETURN_HOST_IP),
                    new DBKeyDAOKeyPair(ServerDataDAO.codeBase, ServerDataDB.CODEBASE),
                    new DBKeyDAOKeyPair(ServerDataDAO.stubHex, ServerDataDB.STUB_HEX),
                    new DBKeyDAOKeyPair(ServerDataDAO.borrowSessionInfo, ServerDataDB.BORROW_SESSION_INFO_HEX),
                    new DBKeyDAOKeyPair(ServerDataDAO.attributeSet, ServerDataDB.ATTRIBUTES_HEX),
                    new DBKeyDAOKeyPair(ServerDataDAO.jobId, ServerDataDB.JOB_ID),
                    new DBKeyDAOKeyPair(ServerDataDAO.jobName, ServerDataDB.JOB_NAME),
                    new DBKeyDAOKeyPair(ServerDataDAO.notifyEmail, ServerDataDB.NOTIFY_EMAIL),
                    new DBKeyDAOKeyPair(ServerDataDAO.isNotified, ServerDataDB.NOTIFIED),
                    new DBKeyDAOKeyPair(ServerDataDAO.owner, ServerDataDB.OWNER),
                    new DBKeyDAOKeyPair(ServerDataDAO.projectId, ServerDataDB.PROJECT),
                    new DBKeyDAOKeyPair(ServerDataDAO.masterName, ServerDataDB.MASTER),
                    new DBKeyDAOKeyPair(ServerDataDAO.dontMonitor, ServerDataDB.DONT_MONITOR)
            };


    // servers
    public static ServerDataDAO convert2ServerDAO(ServerDataDB dbServer) {
        HashMap<String, Object> dbMap = dbServer.getMap();
        HashMap<String, Object> daoMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: serverConversionKeyMap) {
            daoMap.put(p.daoKey, dbMap.get(p.dbKey));
        }

        return new ServerDataDAO(daoMap);
    }

    public static ServerDataDB convert2ServerDB(ServerDataDAO daoServer) {
        HashMap<String, Object> daoMap = daoServer.getMap();
        HashMap<String, Object> dbMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: serverConversionKeyMap) {
            dbMap.put(p.dbKey, daoMap.get(p.daoKey));
        }

        return new ServerDataDB(dbMap);
    }


    //   O P T I O N S
    //
    // mapping: dao key <-> db key
    //
    public static DBKeyDAOKeyPair[] optionConversionKeyMap =
            {
                    new DBKeyDAOKeyPair(OptionDataDAO.optionName, OptionDataDB.OPTION_NAME),
                    new DBKeyDAOKeyPair(OptionDataDAO.optionValue, OptionDataDB.OPTION_VALUE)
            };


    // options
    public static OptionDataDAO convert2OptionDAO(OptionDataDB dbOption) {
        HashMap<String, Object> dbMap = dbOption.getMap();
        HashMap<String, Object> daoMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: optionConversionKeyMap) {
            daoMap.put(p.daoKey, dbMap.get(p.dbKey));
        }

        return new OptionDataDAO(daoMap);
    }

    public static OptionDataDB convert2OptionDB(OptionDataDAO daoOption) {
        HashMap<String, Object> daoMap = daoOption.getMap();
        HashMap<String, Object> dbMap = new HashMap<String, Object>();

        for (DBKeyDAOKeyPair p: optionConversionKeyMap) {
            dbMap.put(p.dbKey, daoMap.get(p.daoKey));
        }

        return new OptionDataDB(dbMap);
    }


}

