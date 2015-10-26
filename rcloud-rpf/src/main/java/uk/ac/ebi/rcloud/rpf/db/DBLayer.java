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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.URLClassLoader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

import uk.ac.ebi.rcloud.rpf.PropertyConst;
import uk.ac.ebi.rcloud.rpf.db.sql.*;
import uk.ac.ebi.rcloud.rpf.exception.LookUpInterrupted;
import uk.ac.ebi.rcloud.rpf.exception.LookUpTimeout;
import uk.ac.ebi.rcloud.rpf.PoolUtils;
import uk.ac.ebi.rcloud.rpf.db.data.*;
import interruptiblermi.InterruptibleRMIThreadFactory;

import static uk.ac.ebi.rcloud.rpf.PoolUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.util.HexUtil;

public abstract class DBLayer implements DBLayerInterface {
    private static final Logger log = LoggerFactory.getLogger(DBLayer.class);

	private Connection _connection = null;

    private ConnectionProvider _connectionProvider;

	abstract void lock(Statement stmt) throws SQLException;

	abstract void unlock(Statement stmt) throws SQLException;

	abstract String sysdateFunctionName();

    abstract protected String sysdateSelectRequest();

	abstract boolean isNoConnectionError(SQLException sqle);

	abstract boolean isConstraintViolationError(SQLException sqle);


    private DBLayerMonitor monitor = new DBLayerMonitor(new MonitorDBLayerRunnable());

    class MonitorDBLayerRunnable implements Runnable {
        public void run(){
            monitorInactivity();
        }
    }

    private long MAXINACTIVITY = 8 * 1000;

    public boolean wantSqlStatements() {
        String trace = System.getProperty("want.sql.statements");
        return (trace != null && trace.equalsIgnoreCase("true"));
    }

    /*
    @Override
    public void finalize() throws Throwable {
        log.info("finalizing");
        super.finalize();
    }
    */

    public DBLayer(ConnectionProvider provider) {
        setConnectionProvider(provider);

        try {
            checkConnection();
        }
        catch (Exception ex) {
            log.error("Error!", ex);
        }
	}

    public static DBLayer getLayer(String dbtype, ConnectionProvider connProvider) throws Exception {
        String className = "uk.ac.ebi.rcloud.rpf.db.DBLayer" +
                ("" + dbtype.charAt(0)).toUpperCase() + dbtype.substring(1);

        DBLayer result = (DBLayer) Class.forName(className)
                .getConstructor(new Class[] { ConnectionProvider.class })
                .newInstance(new Object[] { connProvider });

        return result;
    }

	public void monitorInactivity() {
        if (_connection != null) {
            try {
                _connection.close();
            } 
            catch (Exception ex) {
                log.error("Error!", ex);
            } 
            finally {
                _connection = null;
            }
        }
    }

    public boolean canReconnect() {
        if (_connectionProvider == null) {
            return false;
        }

        log.info("trying to reconnect");

        Statement stmt = null;
        try {
            stmt = createSqlStatement();
            stmt.executeQuery("select POOL_NAME from POOL_DATA");
                        log.info("reconnection aborted, connection was up");
            return false;
        } catch (SQLException sqle) {
        } finally {
            try {
                closeStatement(stmt);
            }
            catch (Exception ex) {
            }
        }

        Connection connection = null;
        try {
            connection = _connectionProvider.newConnection();
            stmt = connection.createStatement();
            stmt.executeQuery("select POOL_NAME from POOL_DATA");
        }
        catch (SQLException sqle) {
            log.info("reconnection failed");
            return false;
        }
        finally {
            try {
                closeStatement(stmt);
            }
            catch (Exception ex) {
            }
        }
        _connection = connection;

        log.info("reconnection succeeded");
        return true;
    }

    private void setConnectionProvider(ConnectionProvider connectionProvider) {
        this._connectionProvider = connectionProvider;
    }

	public void checkConnection() throws SQLException {
        monitor.setWatchdog(MAXINACTIVITY);

        if (_connection == null) {
            if (_connectionProvider == null) {
                log.error("connection provider is null");
            }
            else {
                try {
                    _connection=_connectionProvider.newConnection();
                    _connection.setAutoCommit(false);
                } 
                catch (Exception e) {
                    log.error("Error!", e);
                }
            }
        }

        if (_connection == null) {
            log.error("_connection is null after checkConnection()");
        }
	}


    private Statement createSqlStatement() throws SQLException {
        checkConnection();
        return _connection.createStatement();
    }

    private void closeStatement(Statement stmt) throws RemoteException {
        if (stmt != null) {
            try {
                stmt.close();
            } 
            catch (Exception e) {
                throw new RemoteException("", e);
            }
        }
    }

    private static String wrap(String value) {
        value = value.replaceAll("'", "''");
        return ("'" + value + "'");
    }


    public void executeStatement(String statement) throws RemoteException {
        if (wantSqlStatements()) log.info(statement);

        Statement stmt = null;
        try {
            stmt = createSqlStatement();
            stmt.execute(statement);
        }
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                executeStatement(statement);
            } 
            else {
                throw new RemoteException("", sqle);
            }
        } 
        finally {
            try {
                if (stmt != null) {
                    _connection.commit();
                }
            }
            catch (Exception e) {
                throw new RemoteException("", e);
            }

            closeStatement(stmt);
        }
    }

    public void executeStatementLocked(String statement) throws RemoteException {

        if (wantSqlStatements()) log.info(statement);

        Statement stmt = null;
        try {
            stmt = createSqlStatement();
            lock(stmt);
            stmt.execute(statement);
        } 
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                executeStatementLocked(statement);
            } else {
                throw new RemoteException("", sqle);
            }
        } 
        finally {
            try {
                if (stmt != null) {
                    unlock(stmt);
                    _connection.commit();
                }
            } 
            catch (Exception e) {
                throw new RemoteException("", e);
            }
            closeStatement(stmt);
        }
    }

    public void insertRecordInternal(String checkRecordStatement,
                                     String insertRecordStatement,
                                     String removeRecordStatement) throws AlreadyBoundException, RemoteException {
        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = createSqlStatement();
            lock(stmt);
            
            if (wantSqlStatements()) log.info(checkRecordStatement);
            
            rset = stmt.executeQuery(checkRecordStatement);
            rset.next();
            if (rset.getInt(1) > 0) {
                if (removeRecordStatement != null) {

                    if (wantSqlStatements()) log.info(checkRecordStatement);

                    stmt.execute(removeRecordStatement);
                } else {
                    throw new AlreadyBoundException();
                }
            }
        } 
        catch (AlreadyBoundException abe) {
            try {
                if (stmt != null) {
                    unlock(stmt);
                    _connection.commit();
                }
            } catch (Exception e) {
                throw new RemoteException("", e);
            }
            throw abe;
        } catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                insertRecordInternal(checkRecordStatement,
                        insertRecordStatement,
                        removeRecordStatement);
            } else {
                throw new RemoteException("", sqle);
            }
        } finally {
            closeStatement(stmt);
        }

        stmt = null;

        try {
            stmt = createSqlStatement();
            
            if (wantSqlStatements()) log.info(insertRecordStatement);
            
            stmt.execute(insertRecordStatement);

        } 
        catch (SQLException sqle) {
            log.error("Error!", sqle);
            if (isConstraintViolationError(sqle)) {
                throw new AlreadyBoundException();
            }
            else {
                throw new RemoteException("", sqle);
            }
        }
        finally {
            try {
                if (stmt != null) {
                    unlock(stmt);
                    _connection.commit();
                }
            } 
            catch (Exception e) {
                throw new RemoteException("", e);
            }
            closeStatement(stmt);
        }
    }
    
    public void lock() throws RemoteException {
        Statement stmt = null;
        try {
            stmt = createSqlStatement();
            lock(stmt);
        }
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                lock();
            } else {
                throw new RemoteException("", sqle);
            }
        }
        finally {
            closeStatement(stmt);
        }
    }

    public void unlock() throws RemoteException {
        Statement stmt = null;
        try {
            stmt = createSqlStatement();
            unlock(stmt);
        }
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                unlock();
            } else {
                throw new RemoteException("", sqle);
            }
        } finally {
            closeStatement(stmt);
        }
    }

    public void insertRecord(String tableName, HashMap<String, Object> record) throws RemoteException, AlreadyBoundException {

        String recordFields = "";
        String recordValues = "";
        String separator = "";
        String comma = ",";

        for (String key : record.keySet()) {

            recordFields += (separator + key);
            recordValues += (separator + "?");

            separator = comma;
        }

        String statement = "INSERT INTO " + tableName + " ("+recordFields+") VALUES ("+recordValues+")";

        if (wantSqlStatements()) log.info(statement);

        PreparedStatement pstmt = null;

        try {
            checkConnection();
            pstmt = _connection.prepareStatement(statement);

            int index = 1;
            for (String key : record.keySet()) {

                Object obj = record.get(key);

                if (obj instanceof Integer) {
                    pstmt.setInt(index, (Integer) obj);
                } else if (obj instanceof Timestamp) {
                    pstmt.setTimestamp(index, (Timestamp) obj);
                } else if (obj instanceof String) {
                    pstmt.setString(index, (String) obj);
                } else {
                    pstmt.setString(index, (String) obj);
                }

                index++;
            }

            pstmt.executeUpdate();
            _connection.commit();
        } catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                executeStatement(statement);
            }
            else {
                throw new RemoteException("", sqle);
            }
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                }
                catch (Exception e) {
                    throw new RemoteException("", e);
                }
            }
        }
    }

    public void commit() throws SQLException {
        _connection.commit();
    }

    public Vector<HashMap<String, Object>> getTableData(String tableName) throws RemoteException {
        return getTableData(tableName, null);
    }

    public Vector<HashMap<String, Object>> getTableData(String tableName, String condition) throws RemoteException {
        Vector<HashMap<String, Object>> result = new Vector<HashMap<String, Object>>();
        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = createSqlStatement();

            String statement = "select * from " + tableName +
                    (condition == null || condition.equals("") ? "" : " WHERE " + condition);

            if (wantSqlStatements()) log.info(statement);

            rset = stmt.executeQuery(statement);

            while (rset.next()) {
                HashMap<String, Object> hm = new HashMap<String, Object>();
                for (int i = 1; i <= rset.getMetaData().getColumnCount(); ++i) {

                    Object obj = rset.getObject(i);

                    if (obj instanceof oracle.sql.TIMESTAMP) {

                        if (((oracle.sql.TIMESTAMP)obj).getLength() > 0) {
                            obj = ((oracle.sql.TIMESTAMP)obj).timestampValue();
                        } else {
                            obj = new Timestamp(0);
                        }

                    } else if (obj instanceof oracle.sql.INTERVALDS) {
                        oracle.sql.INTERVALDS interval = (oracle.sql.INTERVALDS) obj;
                        obj = interval.toString();

                    } else if (obj instanceof org.postgresql.util.PGInterval) {
                        org.postgresql.util.PGInterval interval = (org.postgresql.util.PGInterval) obj;
                        obj = interval.toString();

                    } else if (obj instanceof BigDecimal) {
                        obj = ((BigDecimal)obj).intValue();
                    }

                    hm.put(rset.getMetaData().getColumnName(i).toUpperCase(), obj);
                }
                result.add(hm);
            }
        } catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                return getTableData(tableName, condition);
            } else {
                throw new RemoteException("", sqle);
            }
        } finally {
            closeStatement(stmt);
        }
        return result;
    }


    //
    //  S E R V E R S
    //

    public ServerDataDB initServerData(String name, Remote obj) throws RemoteException {

        HashMap<String, Object> opts=new HashMap<String, Object>();

        opts.put(ServerDataDB.NAME,         name);
        opts.put(ServerDataDB.STUB_HEX,     stubToHex(obj));
        opts.put(ServerDataDB.PROCESS_ID,   getProcessId());
        opts.put(ServerDataDB.HOST_NAME,    getHostName());
        opts.put(ServerDataDB.HOST_IP,      getHostIp());
        opts.put(ServerDataDB.OS,           System.getProperty(PropertyConst.OSNAME));
        opts.put(ServerDataDB.CODEBASE,     System.getProperty(PropertyConst.RMISERVERCODEBASE));
        opts.put(ServerDataDB.JOB_ID,       System.getProperty(PropertyConst.JOBID));
        opts.put(ServerDataDB.JOB_NAME,     System.getProperty(PropertyConst.JOBNAME));
        opts.put(ServerDataDB.NOTIFY_EMAIL, System.getProperty(PropertyConst.NOTIFYEMAIL));

        return (new ServerDataDB( opts ));
    }

	public void bind(ServerDataDB server) throws RemoteException, AlreadyBoundException {
        insertRecordInternal(
                ServersSQL.checkExistsStatement(server.getName()),
                ServersSQL.insertServerStatement(server, sysdateFunctionName()),
                null);
	}

    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException {
        ServerDataDB server = initServerData(name, obj);
		bind(server);
	}

    public String[] list() throws RemoteException {
		Vector<String> result = new Vector<String>();

        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = createSqlStatement();

            String statement = ServersSQL.selectAllNamesStatement();

            if (wantSqlStatements()) log.info(statement);

            rset = stmt.executeQuery(statement);

            while (rset.next()) {
                result.add(rset.getString(1));
            }

            return result.toArray(new String[0]);
        }
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                return list();
            } else {
                throw new RemoteException("", sqle);
            }
        }
        finally {
            closeStatement(stmt);
        }
	}

    public Vector<String> list(String[] prefixes) throws RemoteException {
        Vector<String> result = new Vector<String>();

        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = createSqlStatement();

            String statement = ServersSQL.selectAllNamesStatement(prefixes);

            if (wantSqlStatements()) log.info(statement);

            rset = stmt.executeQuery(statement);

            while (rset.next()) {
                result.add(rset.getString(1));
            }

            return result;
        }
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                return list(prefixes);
            } else {
                throw new RemoteException("", sqle);
            }
        }
        finally {
            closeStatement(stmt);
        }
    }

	public Remote lookup(String name) throws RemoteException, NotBoundException {
        Statement stmt = null;
        ResultSet rset = null;

        log.info("lookup " + name);

        try {
            stmt = createSqlStatement();

            String statement = ServersSQL.lookupStatement(name);

            if (wantSqlStatements()) log.info(statement);

            rset = stmt.executeQuery(statement);
            if (rset.next()) {

                final String stubHex = rset.getString(1);
                final String codeBaseStr = rset.getString(2);
                final ClassLoader cl = (codeBaseStr != null ? new URLClassLoader(PoolUtils.getURLS(codeBaseStr), DBLayer.class.getClassLoader())
                        : DBLayer.class.getClassLoader());

                //log.info("codeBaseStr :: " + codeBaseStr);

                final Object[] resultHolder = new Object[1];
                Runnable lookupRunnable = new Runnable() {
                    public void run() {
                        try {
                            resultHolder[0] = hexToStub(stubHex, cl);
                        } catch (Exception e) {
                            final boolean wasInterrupted = Thread.interrupted();
                            if (wasInterrupted) {
                                resultHolder[0] = new LookUpInterrupted();
                            } else {
                                resultHolder[0] = e;
                            }
                        }
                    }
                };

                Thread lookupThread = InterruptibleRMIThreadFactory.getInstance().newThread(lookupRunnable);
                lookupThread.start();

                long t1 = System.currentTimeMillis();
                while (resultHolder[0] == null) {
                    if ((System.currentTimeMillis() - t1) > PoolUtils.LOOKUP_TIMEOUT_MILLISEC) {
                        lookupThread.interrupt();
                        resultHolder[0] = new LookUpTimeout();
                        registerPingFailure(name);
                        break;
                    }
                    Thread.sleep(10);
                }

                if (resultHolder[0] instanceof Throwable) {
                    if (resultHolder[0] instanceof NotBoundException)
                        throw (NotBoundException) resultHolder[0];
                    else
                        throw (RemoteException) resultHolder[0];
                }

                return (Remote) resultHolder[0];

            } else {
                throw new NotBoundException();
            }
        } catch (NotBoundException nbe) {
            throw nbe;
        } catch (LookUpTimeout lue) {
            throw lue;
        } catch (LookUpInterrupted lui) {
            throw lui;
        } catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                return lookup(name);
            } else {
                throw new RemoteException("", (sqle));
            }
        } catch (Exception e) {
            throw new RemoteException("", (e));
        } finally {
            closeStatement(stmt);

            /*
            if (rset != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    throw new RemoteException("", (e));
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    throw new RemoteException("", (e));
                }
            }
            */
        }
	}

    public void rebind(ServerDataDB server) throws RemoteException {
        try {
            insertRecordInternal(
                    ServersSQL.checkExistsStatement(server.getName()),
                    ServersSQL.insertServerStatement(server, sysdateFunctionName()),
                    ServersSQL.deleteServerStatement(server.getName()));
        }
        catch (AlreadyBoundException abe) {
            log.error("Error!", abe);
            throw new RemoteException("Error!", abe);
        }
	}

	public void rebind(String name, Remote obj) throws RemoteException {
        ServerDataDB server = initServerData(name, obj);
		rebind(server);
	}

	public void unbind(String name) throws RemoteException, NotBoundException {
		executeStatementLocked(ServersSQL.deleteServerStatement(name));
	}

	public Vector<HashMap<String, Object>> listKillable() throws RemoteException {
		return getTableData("SERVANTS", "PING_FAILURES>=" + PoolUtils.PING_FAILURES_NBR_MAX);
	}

	public Vector<HashMap<String, Object>> listKillable(String nodeIp,
                                                        String nodePrefix) throws RemoteException {

        return getTableData("SERVANTS", "PING_FAILURES>=" + PoolUtils.PING_FAILURES_NBR_MAX +
                " AND " + "HOST_IP=" + wrap( nodeIp ) + " AND NAME like " + wrap( nodePrefix + "%" ));
	}

	public void reserve(String name) throws RemoteException, NotBoundException {
        executeStatement(ServersSQL.reserveStatement(name, sysdateFunctionName()));
	}

	public void unReserve(String name) throws RemoteException, NotBoundException {
        executeStatement(ServersSQL.unreserveStatement(name, sysdateFunctionName()));
	}

	public void registerPingFailure(String name)
            throws RemoteException, NotBoundException {
        executeStatement(ServersSQL.pingFailureStatement(name));
	}

    public void updateServantNodeName(String servername, String nodename)
            throws RemoteException, NotBoundException {
        executeStatement(ServersSQL.updateNodenameStatement(servername, nodename));
    }

    public void updateServantAttributes(String servername, HashMap<String, Object> attributes)
            throws RemoteException, NotBoundException {

        executeStatement(ServersSQL.updateAttributesStatement(servername, HexUtil.objectToHex(attributes)));

    }

    public void unlockServant(String servantName) throws RemoteException {
        executeStatement("UPDATE SERVANTS SET IN_USE=0, PING_FAILURES=0" +
                ",BORROW_HOST_NAME=NULL" + ",BORROW_HOST_IP=NULL" + ",BORROW_PROCESS_ID=NULL" +
                ",BORROW_SESSION_INFO_HEX=NULL" + ",RETURN_TIME=" + sysdateFunctionName() +
                ",RETURN_HOST_NAME=" + wrap(getHostName()) +
                ",RETURN_HOST_IP=" + wrap(getHostIp()) +
                ",RETURN_PROCESS_ID=" + wrap(getProcessId()) +
                " WHERE NAME=" + wrap(servantName));

    }

    public String getNameFromStub(Remote stub) throws RemoteException {

        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(baoStream).writeObject(stub);
        } catch (Exception e) {
            log.error("Error!", e);
        }
        String stub_hex = HexUtil.bytesToHex(baoStream.toByteArray());

        Statement stmt = null;
        ResultSet rset = null;
        try {
            stmt = createSqlStatement();

            String statement = ServersSQL.nameFromStubhexStatement(stub_hex);

            if (wantSqlStatements()) log.info(statement);

            rset = stmt.executeQuery(statement);

            if (!rset.next()) {
                throw new RemoteException("no corresponding servant in DB");
            }
            return rset.getString(1);
        }
        catch (SQLException sqle) {
            if (isNoConnectionError(sqle) && canReconnect()) {
                return getNameFromStub(stub);
            } else {
                throw new RemoteException("", sqle);
            }
        }
        finally {
            closeStatement(stmt);
        }
    }

    public void setJobID(String servername, String jobID) throws RemoteException {
        executeStatement(ServersSQL.updateJobIDStatement(servername, jobID));
    }

    public void setOwner(String servername, String owner) throws RemoteException {
        executeStatement(ServersSQL.updateOwnerStatement(servername, owner));
    }

    public void setProject(String servername, String project) throws RemoteException {
        executeStatement(ServersSQL.updateProjectStatement(servername, project));
    }

    public void setMaster(String servername, String mastername) throws RemoteException {
        executeStatement(ServersSQL.updateMasterStatement(servername, mastername));
    }

    public void setOvertimeNotificationSent(String servername) throws RemoteException {
        executeStatement(ServersSQL.incrementNotificationStatement(servername));
    }

    public void releaseFreeServersRunningLongerThan(int hours) throws RemoteException {
        executeStatementLocked(ServersSQL.releaseFreeResourcesOutOfLifespanStatement(sysdateFunctionName(), hours));
    }


    public Vector<ServerDataDB> getServerData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> serverTable = getTableData("SERVANTS", condition);
        Vector<ServerDataDB> result = new Vector<ServerDataDB>();
        for (HashMap<String, Object> hm : serverTable) {

            result.add(new ServerDataDB( hm ));
        }

        return result;
    }

    public ServerDataDB getServerRecord(String serverName) throws RemoteException {
        Vector<ServerDataDB> serverdata = getServerData("NAME=" + wrap(serverName));

        if (serverdata.size() > 0) {
            return serverdata.elementAt(0);
        }

        return null;
    }

    public Vector<ServerDataDB> getServersByProjectId(String projectId) throws RemoteException {
        Vector<ServerDataDB> servers = getServerData("PROJECT=" + wrap(projectId));
        return servers;
    }

    public Vector<ServerDataDB> getServersByOwner(String owner) throws RemoteException {
        Vector<ServerDataDB> servers = getServerData("OWNER=" + wrap(owner));
        return servers;
    }

    //
    //   N O D E S
    //


	public void incrementNodeProcessCounter(String nodename)
            throws RemoteException, NotBoundException {

        executeStatement(NodesSQL.incrementNodePCStatement(nodename));
	}

    public Vector<NodeDataDB> getNodeData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> nodeTable = getTableData("NODE_DATA", condition);
        Vector<NodeDataDB> result = new Vector<NodeDataDB>();
        for (HashMap<String, Object> hm : nodeTable) {
            result.add(new NodeDataDB( hm ));
        }
        return result;
    }

    public void addNode(NodeDataDB nodedata) throws RemoteException {
        executeStatement(NodesSQL.addNodeStatement(nodedata));
    }

    public void removeNode(String nodename) throws RemoteException {
        executeStatement(NodesSQL.removeNodeStatement(nodename));
    }

    public void updateNode(NodeDataDB nodedata) throws RemoteException {
        executeStatement(NodesSQL.updateNodeStatement(nodedata));
    }

    /*
	public Remote getRemoteObject(String stub, String codeBaseStr) throws RemoteException {
		try {
			ClassLoader cl = null;
			if (codeBaseStr != null) {
				cl = new URLClassLoader(new URL[] { new URL(codeBaseStr) }, DBLayer.class.getClassLoader());
			}
			return hexToStub(stub, cl);
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}
	*/

    //
    //   P O O L S
    //

	public HashMap<String, PoolDataDB> getPoolDataHashMap() throws RemoteException {
		HashMap<String, PoolDataDB> result = new HashMap<String, PoolDataDB>();

        Vector<PoolDataDB> poolData = getPoolData();

        for (PoolDataDB pool : poolData) {
            result.put(pool.getPoolName(), pool);
        }

        return result;
	}

	public static String[] getPrefixes(String prefixes) {
		StringTokenizer st = new StringTokenizer(prefixes, ",");
		Vector<String> pv = new Vector<String>();
		while (st.hasMoreElements())
			pv.add((String) st.nextElement());
		return pv.toArray(new String[0]);
	}

	public Vector<PoolDataDB> getPoolData() throws RemoteException {
        Vector<HashMap<String, Object>> nodeTable = getTableData("POOL_DATA");
		Vector<PoolDataDB> result = new Vector<PoolDataDB>();
        for (HashMap<String, Object> hm : nodeTable) {

            result.add(new PoolDataDB( hm ));
        }

        return result;
	}

    public Integer toInteger(Object obj) {
        if (obj instanceof BigDecimal){
            return ((BigDecimal) obj).intValue();
        }
        return ((Integer) obj);
    }


	public void addPool(PoolDataDB pooldata) throws RemoteException {
        executeStatement(PoolsSQL.addPoolStatement(pooldata));
	}

	public void removePool(String poolname) throws RemoteException {
        executeStatement(PoolsSQL.removePoolStatement(poolname));
	}

	public void updatePool(PoolDataDB pooldata) throws RemoteException {
		executeStatement(PoolsSQL.updatePoolStatement(pooldata));
	}

    //
    //   U S E R S
    //

    public void createUser(UserDataDB user) throws AlreadyBoundException, RemoteException {
        insertRecordInternal(UsersSQL.checkExistsStatement(user.getLogin()),
                UsersSQL.addUserStatement(user, sysdateFunctionName()),
                null);
    }

    public void deleteUser(UserDataDB user) throws RemoteException, NotBoundException {
        executeStatement(UsersSQL.deleteUserStatement(user.getLogin()));
    }


    public Vector<UserDataDB> getUserData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> userTable = getTableData("USERS", condition);
        Vector<UserDataDB> result = new Vector<UserDataDB>();
        for (HashMap<String, Object> hm : userTable) {
            result.add(new UserDataDB( hm ));
        }

        return result;
    }


    public UserDataDB getUser(String login) throws RemoteException {
        Vector<UserDataDB> users = getUserData("LOGIN = " + wrap(login));

        if (users.size() > 0) {
            return users.elementAt(0);
        }

        return null;
    }


    public void updateUser(UserDataDB user) throws RemoteException {
        executeStatement(UsersSQL.updateUserStatement(user));
    }

    public void updateUserClusterDetails(UserDataDB user) throws RemoteException {
        executeStatement(UsersSQL.updateUserClusterStatement(user));
    }

    public void updateUserLoggedIn(String username) throws RemoteException {
        executeStatement(UsersSQL.updateToLoggedInStatement(username, sysdateFunctionName()));
    }

    public void updateUserLoggedOut(String username) throws RemoteException {
        executeStatement(UsersSQL.updateToLoggedOutStatement(username, sysdateFunctionName()));
    }

    //
    //   P R O J E C T S
    //

    public void createProject(ProjectDataDB project) throws AlreadyBoundException, RemoteException {
        insertRecordInternal(ProjectsSQL.checkExistsStatement(project.getIdentifier()),
                ProjectsSQL.addProjectStatement(project, sysdateFunctionName()),
                null);
    }

    public void deleteProject(ProjectDataDB project) throws RemoteException, NotBoundException {
        executeStatement(ProjectsSQL.deleteProjectStatement(project.getIdentifier()));
    }


    public Vector<ProjectDataDB> getProjectData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> projectTable = getTableData("PROJECTS", condition);
        Vector<ProjectDataDB> result = new Vector<ProjectDataDB>();
        for (HashMap<String, Object> hm : projectTable) {
            result.add(new ProjectDataDB( hm ));
        }

        return result;
    }

    public Vector<ProjectDataDB> getProjectsByOwner(String owner) throws RemoteException {
        return getProjectData(ProjectDataDB.OWNER + "=" + SqlBase.wrap(owner));
    }

    public ProjectDataDB getProject(String projectid) throws RemoteException {
        Vector<ProjectDataDB> projects = getProjectData("FOLDER = " + wrap(projectid));

        if (projects.size() > 0) {
            return projects.elementAt(0);
        }

        return null;
    }

    public void updateProjectDescription(ProjectDataDB project) throws RemoteException {
        executeStatement(ProjectsSQL.updateProjectDescription(project));
    }

    public void updateProjectOpened(String projectid) throws RemoteException {
        executeStatement(ProjectsSQL.projectOpenedStatement(
                projectid, sysdateFunctionName()));
    }

    public void updateProjectOnHold(String projectid) throws RemoteException {
        executeStatement(ProjectsSQL.projectOnholdStatement(
                projectid, sysdateFunctionName()));
    }

    public void updateProjectStopped(String projectid) throws RemoteException {
        executeStatement(ProjectsSQL.projectStoppedStatement(
                projectid, sysdateFunctionName()));
    }

    public void updateProject(ProjectDataDB project) throws RemoteException {
        executeStatement(ProjectsSQL.updateProjectStatement(project));
    }

    public void updateProjectBasefolder(ProjectDataDB project) throws RemoteException {
        executeStatement(ProjectsSQL.updateBasefolderStatement(project));
    }

    public void updateProjectActivity(String projectid) throws RemoteException {
        executeStatement(ProjectsSQL.projectActivityStatement(
                projectid, sysdateFunctionName()));
    }

    public void updateProjectOwnerNotified(String projectid) throws RemoteException {
        executeStatement(ProjectsSQL.registerNotificationStatement(
                projectid));
    }

    //
    // DATE & TIME
    //
    public TimestampDataDB getTimestamp() throws RemoteException {
        Vector<HashMap<String, Object>> sysdate =
                getTableData(sysdateSelectRequest(), null);

        if (sysdate.size() > 0) {
            return new TimestampDataDB( sysdate.elementAt(0) );
        } else {
            return null;
        }
    }

    //
    // MONITORS
    //

    /*

    CREATE TABLE MONITORS (
        NAME VARCHAR2 (1000),
        STUB_HEX VARCHAR2 (4000),
        PING_FAILURES NUMBER,
        REGISTER_TIME TIMESTAMP(6),
        PROCESS_ID VARCHAR2	(100),
        HOST_NAME VARCHAR2 (500),
        HOST_IP VARCHAR2 (100),
        OS VARCHAR2 (100)
    )


    * */


    /*
    public MonitorDataDB initMonitorData(String name, Remote obj) throws RemoteException {

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(MonitorDataDB.NAME,          name);
        map.put(MonitorDataDB.STUB_HEX,      stubToHex(obj));
        map.put(MonitorDataDB.PROCESS_ID,    getProcessId());
        map.put(MonitorDataDB.HOST_NAME,     getHostName());
        map.put(MonitorDataDB.HOST_IP,       getHostIp());
        map.put(MonitorDataDB.OS,            System.getProperty(PropertyConst.OSNAME));

        return (new MonitorDataDB( map ));
    }


    public void createMonitor(String name, Remote obj) throws AlreadyBoundException, RemoteException {
        createMonitor(initMonitorData(name, obj));
    }

    public void createMonitor(MonitorDataDB monitor) throws AlreadyBoundException, RemoteException {
        insertRecordInternal(MonitorsSQL.checkExistsStatement(monitor.getName()),
                MonitorsSQL.addMonitorStatement(monitor, sysdateFunctionName()),
                null);

    }

    public void deleteMonitor(MonitorDataDB monitor) throws RemoteException, NotBoundException {
        executeStatement(MonitorsSQL.deleteMonitorStatement(monitor.getName()));

    }

    public Vector<MonitorDataDB> getMonitorData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> monitors = getTableData("MONITORS", condition);
        Vector<MonitorDataDB> result = new Vector<MonitorDataDB>();
        for (HashMap<String, Object> hm : monitors) {
            result.add(new MonitorDataDB( hm ));
        }

        return result;
    }

    public MonitorDataDB getMonitor(String name) throws RemoteException {
        Vector<MonitorDataDB> monitors = getMonitorData("NAME = " + wrap(name));

        if (monitors.size() > 0) {
            return monitors.elementAt(0);
        }

        return null;

    }

    public void registerMonitorFailure(String name) throws RemoteException, NotBoundException {
        executeStatement(MonitorsSQL.pingFailureStatement(name));
    }

    */



    //
    //   O P T I O N S
    //
    public void createOption(OptionDataDB option) throws AlreadyBoundException, RemoteException {
        insertRecordInternal(OptionsSQL.optionExistsStatement(option.getOptionName()),
                OptionsSQL.addOptionStatement(option),
                null);
    }

    public void deleteOption(OptionDataDB option) throws RemoteException, NotBoundException {
        executeStatement(OptionsSQL.deleteOptionStatement(option.getOptionName()));
    }

    public Vector<OptionDataDB> getOptionData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> optionsTable = getTableData("OPTIONS", condition);
        Vector<OptionDataDB> result = new Vector<OptionDataDB>();
        for (HashMap<String, Object> hm : optionsTable) {
            result.add(new OptionDataDB( hm ));
        }
        return result;
    }

    public OptionDataDB getOption(String optionname) throws RemoteException {
        Vector<HashMap<String, Object>> optionsTable = getTableData("OPTIONS", "OPTION_NAME = " + wrap( optionname ));
        for (HashMap<String, Object> hm : optionsTable) {
            return new OptionDataDB( hm );
        }
        return null;
    }

    public void updateOption(OptionDataDB option) throws RemoteException {
        executeStatement(OptionsSQL.updateOptionStatement(option));
    }

    //
    // SITE DATA
    //
    //
    public void createSite(SiteDataDB sitedata) throws AlreadyBoundException, RemoteException {
        insertRecordInternal(SiteSQL.siteExistsStatement(sitedata.getSiteName()),
                SiteSQL.addSiteStatement(sitedata),
                null);
    }

    public void deleteSite(String sitename) throws RemoteException, NotBoundException {
        executeStatement(SiteSQL.deleteSiteStatement(sitename));
    }

    public Vector<SiteDataDB> getSiteData(String condition) throws RemoteException {
        Vector<HashMap<String, Object>> siteTable = getTableData("SITE_DATA", condition);
        Vector<SiteDataDB> result = new Vector<SiteDataDB>();
        for (HashMap<String, Object> hm : siteTable) {
            result.add(new SiteDataDB( hm ));
        }
        return result;
    }

    public SiteDataDB getSite(String sitename) throws RemoteException {
        Vector<HashMap<String, Object>> siteTable = getTableData("SITE_DATA", "SITE_NAME = " + wrap( sitename ));
        for (HashMap<String, Object> hm : siteTable) {
            return new SiteDataDB( hm );
        }
        return null;
    }

    public void updateSite(SiteDataDB sitedata) throws RemoteException {
        executeStatement(SiteSQL.updateSiteStatement(sitedata));
    }

    //
    //   U T I L S
    //

	public static String replaceCode(String s) {
		int p1 = 0;
		while ((p1 = s.indexOf("<%=")) != -1) {
			int p2 = s.indexOf("%>", p1 + 3);
			String expression = s.substring(p1 + 3, p2);
			String className = expression.substring(0, expression.lastIndexOf('.'));
			String functionName = expression.substring(expression.lastIndexOf('.') + 1, expression.lastIndexOf("()"));
			String replaceWith = "ERROR";
			try {
				replaceWith = (String) Class.forName(className)
                        .getMethod(functionName, (Class[]) null)
                        .invoke(null, (Object[]) null);
			} catch (Exception e) {
                log.error("Error!", e);
			}
			s = s.substring(0, p1) + replaceWith + s.substring(p2 + 2);
		}
		return s;
	}

}

