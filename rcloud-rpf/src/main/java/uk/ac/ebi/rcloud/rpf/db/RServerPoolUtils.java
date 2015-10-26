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

import javax.swing.*;

import uk.ac.ebi.rcloud.rpf.PoolUtils;
import uk.ac.ebi.rcloud.rpf.ServerDefaults;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.getDBType;
import uk.ac.ebi.rcloud.rpf.db.data.NodeDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.PoolDataDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.HashMap;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 24, 2009
 * Time: 2:04:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class RServerPoolUtils {
    final private static Logger log = LoggerFactory.getLogger(RServerPoolUtils.class);

    public static String readProperty(String name, String defaultValue) {
        String prop = System.getProperty(name);
        if (prop != null && !prop.equals("")) {
            return prop;
        } else {
            if (defaultValue == null) {
                log.info("mandatory property " + name + " not defined");
                System.exit(0);
            }
            return defaultValue;
        }
    }

    public static void createNewPool() throws Exception {

        String login = readProperty("LOGIN", null);
        String pwd  = readProperty("PWD", null);

        Class.forName(ServerDefaults._dbDriver);

        log.info("DB URL:" + ServerDefaults._dbUrl);

        DBLayer dbLayer = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
            public Connection newConnection() throws SQLException {
                return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
            }
        });

        HashMap <String, Object> map = new HashMap<String, Object>();
        map.put("POOL_NAME", readProperty("POOL_NAME", null));
        map.put("POOL_PREFIX", readProperty("POOL_PREFIX", null));
        map.put("TIMEOUT", readProperty("TIMEOUT", "400000"));
        map.put("ON_DEMAND", readProperty("ON_DEMAND", null));

        PoolDataDB pool = new PoolDataDB(map);

        String installDir = PoolUtils.getBiocepHome();

        installDir = installDir == null ? "null" : installDir;

        String defAddCmd = "echo create command";
        String defDelCmd = "echo kill command";

        HashMap<String, Object> opts = new HashMap<String, Object>();

        opts.put(NodeDataDB.NODE_NAME, null);
        opts.put(NodeDataDB.NODE_TYPE, null);
        opts.put(NodeDataDB.HOST_IP, "172.18.1.85");
        opts.put(NodeDataDB.POOL_PREFIX, null);
        opts.put(NodeDataDB.HOST_NAME, "bar.ebi.ac.uk");
        opts.put(NodeDataDB.LOGIN, login);
        opts.put(NodeDataDB.PWD, pwd);
        opts.put(NodeDataDB.INSTALL_DIR, installDir);
        opts.put(NodeDataDB.CREATE_SERVANT_COMMAND, defAddCmd);
        opts.put(NodeDataDB.KILL_SERVANT_COMMAND, defDelCmd);
        opts.put(NodeDataDB.OS, PoolUtils.getOs());
        opts.put(NodeDataDB.SERVANT_NBR_MIN, 3);
        opts.put(NodeDataDB.SERVANT_NBR_MAX, 5);
        opts.put(NodeDataDB.PROCESS_COUNTER, 0);
        opts.put(NodeDataDB.STEP, 5);
        opts.put(NodeDataDB.THRESHOLD, 10);

        NodeDataDB node = new NodeDataDB( opts );

        try {
            dbLayer.addPool(pool);
            dbLayer.addNode(node);
        } catch (Exception ex) {
            log.error("Error!", ex);
        }

        log.info("Right. We've added a new pool.");

    }

    public static void removePool() throws Exception {

        Class.forName(ServerDefaults._dbDriver);

        log.info("DB URL:" + ServerDefaults._dbUrl);

        DBLayer dbLayer = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
            public Connection newConnection() throws SQLException {
                return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
            }
        });

        String poolName = readProperty("POOL_NAME", null);
        String nodeName = readProperty("NODE_NAME", null);

        try {
            dbLayer.removePool(poolName);
            dbLayer.removeNode(nodeName);
        } catch (Exception ex) {
            log.error("Error!", ex);
        }

        log.info("Right. We've removed one of pools.");

    }


    public static void main(String[] args) throws Exception {

        int response = JOptionPane.showConfirmDialog(null,
                "Executing this command may result in Database modification. Proceed ?",
                "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null);

        if (response != JOptionPane.OK_OPTION) {
            return;
        }

        if (args.length > 0){
            if (args[0].equals("createpool") || args[0].equals("addpool")){
                createNewPool();
            }
            if (args[0].equals("removepool") || args[0].equals("deletepool")){
                removePool();
            }
        } else {
            log.info("nothing happened");
        }

        System.exit(0);
    }
}


