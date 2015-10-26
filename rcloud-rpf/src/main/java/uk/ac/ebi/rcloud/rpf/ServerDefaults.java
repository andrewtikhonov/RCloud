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
package uk.ac.ebi.rcloud.rpf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import uk.ac.ebi.rcloud.rpf.db.ConnectionProvider;
import uk.ac.ebi.rcloud.rpf.db.DBLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.rcloud.rpf.PoolUtils.*;

public abstract class ServerDefaults {
    final private static Logger log = LoggerFactory.getLogger(ServerDefaults.class);

	public static String _namingMode;	
	public static String _servantPoolPrefix;	
	public static String _registryHost;
	public static int _registryPort;
	public static String _dbUrl ;
	public static String _dbDriver ;
	public static String _dbUser;
	public static String _dbPassword;
	public static int _memoryMin;
	public static int _memoryMax;

    //private static boolean _propertiesInjected = false;
    private static Properties staticprops = new Properties();

	static {
		init();
	}

    private static String initProperty(String name, String defaultValue) {
        String value = System.getProperty(name);
        String valueToAdd = (value != null && value.length() > 0) ? value : defaultValue;
        staticprops.put(name, valueToAdd);
        return valueToAdd;
    }

	public static void init()	{

        _namingMode = initProperty("naming.mode", DEFAULT_NAMING_MODE);

        log.info("naming mode :" + _namingMode);
		
		_servantPoolPrefix = initProperty("prefix", DEFAULT_PREFIX);
		_registryHost = initProperty("registry.host", DEFAULT_REGISTRY_HOST);
        _registryPort = Integer.decode(initProperty("registry.port", Integer.toString(DEFAULT_REGISTRY_PORT)));

        _memoryMin = Integer.decode(initProperty("memorymin", Integer.toString(DEFAULT_MEMORY_MIN)));
        _memoryMax = Integer.decode(initProperty("memorymax", Integer.toString(DEFAULT_MEMORY_MAX)));

        String _DB_TYPE = initProperty("db.type", DEFAULT_DB_TYPE);
        String _DB_HOST = initProperty("db.host", DEFAULT_DB_HOST);
		int    _DB_PORT = Integer.decode(initProperty("db.port", Integer.toString(DEFAULT_DB_PORT)));
        String _DB_NAME = initProperty("db.name", DEFAULT_DB_NAME);
        String _DB_DIR = initProperty("db.dir", DEFAULT_DB_DIR);

        _DB_DIR=_DB_DIR.replace('\\', '/');	if (!_DB_DIR.equals("") && !_DB_DIR.endsWith("/")) _DB_DIR=_DB_DIR+"/";

        //log.info("DB Dir:" + _DB_DIR);

		if (_DB_TYPE.equals("derby")) {
			_dbUrl = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_DIR+_DB_NAME+";create=true";
			_dbDriver="org.apache.derby.jdbc.ClientDriver";			
		} else if (_DB_TYPE.equals("derbyembeddedserver")) {
			_dbUrl = "jdbc:derby:biocep;create=true";
			_dbDriver="org.apache.derby.jdbc.EmbeddedDriver";

            try {
                NetworkServerControl server = new NetworkServerControl (InetAddress.getByName("localhost"),1527);
                server.start(null);
            } catch (Exception e) {
                log.error("Error!", e);
            }
		} else if (_DB_TYPE.equals("derbyembeddedclient")) {
            _dbUrl = "jdbc:derby://localhost:1527/biocep";
            _dbDriver="org.apache.derby.jdbc.ClientDriver";				
		} else if (_DB_TYPE.equals("mysql")) {
			_dbUrl = "jdbc:mysql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;			
			_dbDriver="org.gjt.mm.mysql.Driver";
				
		} else if (_DB_TYPE.equals("oracle")) {			
			_dbUrl = "jdbc:oracle:thin:@"+_DB_HOST+":"+_DB_PORT+":"+_DB_NAME; 
			_dbDriver="oracle.jdbc.driver.OracleDriver";
        } else if (_DB_TYPE.equals("postgresql")) {
            _dbUrl = "jdbc:postgresql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;
            _dbDriver="org.postgresql.Driver";
		}

		_dbUser = initProperty("db.user", DEFAULT_DB_USER);
		_dbPassword = initProperty("db.password", DEFAULT_DB_PASSWORD);
		
        log.info("url :" + _dbUrl);

	}

	public static boolean isRegistryAccessible() {
		try {
			ServerDefaults.getRmiRegistry().list();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Registry _registry = null;
	public static Integer _lock = new Integer(0);
	public static Registry getRmiRegistry() throws Exception {	
		if (_registry != null)
			return _registry;
		synchronized (_lock) {
			if (_registry == null) {
				if (_namingMode.equals("db")) {						
					
					Class.forName(_dbDriver);
					_registry = DBLayer.getLayer(PoolUtils.getDBType(_dbUrl), new ConnectionProvider() {
						public Connection newConnection() throws java.sql.SQLException {
							return DriverManager.getConnection(_dbUrl, _dbUser, _dbPassword);
						};
					});
					
				} else if (_namingMode.equals("self")){
					_registry = LocalRmiRegistry.getInstance();
				} else if (_namingMode.equals("registry")) {
					_registry = LocateRegistry.getRegistry(_registryHost, _registryPort);
				} else {					
					_registry = ((RegistryProvider)ServerDefaults.class.
                            forName(_namingMode+"Class").newInstance() ).getRegistry(System.getProperties());
				}
			}
			return _registry;
		}
	}

	public static Registry getRegistry(Properties props) throws Exception	{
		String namingMode;		
		String registryHost;
		int registryPort;
		String dbUrl=null;
		String dbDriver=null;
		String dbUser;
		String dbPassword;			

        namingMode = props.getProperty("naming.mode", DEFAULT_NAMING_MODE);
		registryHost = props.getProperty("registry.host", DEFAULT_REGISTRY_HOST);
        registryPort = Integer.decode(props.getProperty("registry.port", Integer.toString(DEFAULT_REGISTRY_PORT)));

        String _DB_TYPE = props.getProperty("db.type", DEFAULT_DB_TYPE);
        String _DB_HOST = props.getProperty("db.host", DEFAULT_DB_HOST);
        int    _DB_PORT = Integer.decode(props.getProperty("db.port", Integer.toString(DEFAULT_DB_PORT)));
		String _DB_NAME = props.getProperty("db.name", DEFAULT_DB_NAME);
		String _DB_DIR = props.getProperty("db.dir", DEFAULT_DB_DIR);

		_DB_DIR=_DB_DIR.replace('\\', '/');if (!_DB_DIR.equals("") && !_DB_DIR.endsWith("/")) _DB_DIR=_DB_DIR+"/";
		
		
		if (_DB_TYPE.equals("derby")) {
			dbUrl = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_DIR+_DB_NAME+";create=true";
			dbDriver="org.apache.derby.jdbc.ClientDriver";
			
		} else if (_DB_TYPE.equals("mysql")) {			
			dbUrl = "jdbc:mysql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;			
			dbDriver="org.gjt.mm.mysql.Driver";
				
		} else if (_DB_TYPE.equals("oracle")) {			
			dbUrl = "jdbc:oracle:thin:@"+_DB_HOST+":"+_DB_PORT+":"+_DB_NAME; 
			dbDriver="oracle.jdbc.driver.OracleDriver";
        } else if (_DB_TYPE.equals("postgresql")) {
      			dbUrl = "jdbc:postgresql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;
      			dbDriver="org.postgresql.Driver";
		}
		
		dbUser = props.getProperty("db.user", DEFAULT_DB_USER);
		dbPassword = props.getProperty("db.password", DEFAULT_DB_PASSWORD);
		
		 Registry registry = null;
		 
		if (namingMode.equals("db")) {
			
			Class.forName(dbDriver);
			final String dbUrlFinal=dbUrl ;
			final String dbUserFinal=dbUser;
			final String dbPasswordFinal=dbPassword;				
			registry = DBLayer.getLayer(PoolUtils.getDBType(dbUrl), new ConnectionProvider() {
				public Connection newConnection() throws java.sql.SQLException {
					return DriverManager.getConnection(dbUrlFinal, dbUserFinal, dbPasswordFinal);
				};
			});
			
		}  else if (namingMode.equals("self")){
			registry = LocalRmiRegistry.getInstance();
		} else if (namingMode.equals("registry")) {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
		} else {					
			registry = ((RegistryProvider)ServerDefaults.class.forName(namingMode+"Class").
                    newInstance() ).getRegistry(props);
		}

		return registry;
	}

}
