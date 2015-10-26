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

import org.rosuda.JRI.Rengine;
import uk.ac.ebi.rcloud.rpf.db.sql.SqlBase;
import uk.ac.ebi.rcloud.server.graphics.GraphicNotifierImpl;
import uk.ac.ebi.rcloud.rpf.exception.InitializingException;
import uk.ac.ebi.rcloud.server.reference.AssignInterface;
import uk.ac.ebi.rcloud.server.reference.ReferenceInterface;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.reference.AssignInterfaceImpl;
import uk.ac.ebi.rcloud.server.callback.*;
import uk.ac.ebi.rcloud.server.callback.RAction;
import uk.ac.ebi.rcloud.server.device.GDDeviceImpl;
import uk.ac.ebi.rcloud.server.file.FileDescription;
import uk.ac.ebi.rcloud.server.file.FileNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.rcloud.server.graphics.rmi.RGraphicsPanelRemote;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Future;

import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.graphics.GraphicNotifier;
import uk.ac.ebi.rcloud.server.iplots.SVarInterfaceRemote;
import uk.ac.ebi.rcloud.server.iplots.SVarSetInterfaceRemote;
import uk.ac.ebi.rcloud.rpf.db.DBLayerInterface;
import uk.ac.ebi.rcloud.rpf.*;
import uk.ac.ebi.rcloud.server.search.SearchRequest;
import uk.ac.ebi.rcloud.server.search.SearchResult;

public class RServantImpl extends ManagedServantAbstract implements RServices {

    final private static Logger log = LoggerFactory.getLogger(RServantImpl.class);

	public static final int PORT_RANGE_WIDTH=5;
	
	private StringBuffer _log = new StringBuffer();

	private HashMap<String, RPackage> _rim = null;

	private RNI _remoteRni = null;

	private AssignInterface _assignInterface = null;

	private GraphicNotifier _graphicNotifier = null;

	private HashMap<Integer, GDDevice> _deviceHashMap = new HashMap<Integer, GDDevice>();
	
	private HashMap<String, GenericCallbackDevice> _genericCallbackDeviceHashMap = new HashMap<String, GenericCallbackDevice>();
	
	private boolean _isReady = false;

	private static int _port=System.getProperty("rmi.port.start")!=null &&
            !System.getProperty("rmi.port.start").equals("") ?
                Integer.decode(System.getProperty("rmi.port.start")) : 0;
		
	private boolean archiveLog=false;

    private FileMonitor fileTreeMonitor = null;

    private String masterServerName = null;

	public String runR(ExecutionUnit eu) {
		return DirectJNI.getInstance().runR(eu);
	}

	public RServantImpl(String name, String prefix, Registry registry) throws RemoteException {
		super(name, prefix, registry, _port);
		
		log.info("$$>rmi.port.start:"+_port);
		// --------------	
		init();
				
		log.info("Stub:" + PoolUtils.stubToHex(this));
		
		
		if (System.getProperty("preloadall")!=null && System.getProperty("preloadall").equalsIgnoreCase("true") )
		{			
			try {
				Properties props = new Properties();
				props.loadFromXML(this.getClass().getResourceAsStream("/classlist.xml"));
				for (Object c:props.keySet()) {
					this.getClass().getClassLoader().loadClass((String)c); 
				}
				
			} catch (Exception e) {
				//e.printStackTrace();
			}
					
			try {
				Properties props=new Properties();
				props.loadFromXML(this.getClass().getResourceAsStream("/resourcelist.xml"));
				for (Object c:props.keySet()) {
					DirectJNI.getInstance().getResourceAsStream((String)c);
				}			
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}

        /*
        class RegisterTask extends TimerTask {

            static final int LOOKUP = 0;
            static final int REBIND = 1;
            int task = LOOKUP;

            HashMap<String, Object> server = null;

            public void lookup(){
                try{
                    server = ((DBLayerInterface)_registry).getTableData("SERVANTS", "NAME= " + SqlBase.wrap(_servantName)).elementAt(0);

                } catch (Exception ex) {
                    task = REBIND;
                }
            }

            public void rebind(){
                if (server == null) return;
                try{
                    ((DBLayerInterface)_registry).insertRecord("SERVANTS", server);

                    task = LOOKUP;
                } catch (Exception ex){
                    log.error("Error!", ex);
                }
            }

            public void run(){
                switch(task){
                    case LOOKUP: lookup(); break;
                    case REBIND: rebind(); break;
                }
            }
        }

        if (_registry instanceof DBLayerInterface) {

            Timer rebindTimer   = new Timer();
            RegisterTask rebind = new RegisterTask();

            rebindTimer.schedule(rebind, 5000, 60 * 1000);
        }
        */

        fileTreeMonitor = new FileMonitor(2000);
        fileTreeMonitor.addListener(new DirectoryListener());
        fileTreeMonitor.start();

        class RServantImplShutdownHook implements Runnable {
            public void run(){
                fileTreeMonitor.shutdown();

                disposeDevices();

                RListener.stopAllClusters();

                DirectJNI.getInstance().shutdown();
            }
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread(new RServantImplShutdownHook()));
	}

	public void init() throws RemoteException {
		try {
			DirectJNI.init(getServantName());

			_assignInterface = new AssignInterfaceImpl(this);

            DirectJNI.getInstance().setAssignInterface(
                    (AssignInterface) RemoteObject.toStub(_assignInterface));

			_remoteRni = new RNIImpl(_log);

			_graphicNotifier = new GraphicNotifierImpl();

			_rim = new HashMap<String, RPackage>();

			for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
				String shortClassName = className.substring(className.lastIndexOf('.') + 1);
				log.info(shortClassName);
                log.info("Going to load : " + className + "ImplRemote");
				_rim.put(shortClassName, (RPackage) DirectJNI
                        ._mappingClassLoader.loadClass(className + "ImplRemote").newInstance());
			}

			if (System.getProperty("preprocess.help") != null &&
                    System.getProperty("preprocess.help").equalsIgnoreCase("true")) {
				new Thread(new Runnable() {
					public void run() {
						DirectJNI.getInstance().preprocessHelp();
					}
				}).start();
			}

			if (System.getProperty("apply.sandbox") != null &&
                    System.getProperty("apply.sandbox").equalsIgnoreCase("true")) {

                DirectJNI.getInstance().applySandbox();
			}


            if (_registry instanceof DBLayerInterface) {
                SiteOptions.getInstance(getServantName())
                        .initDbLayer((DBLayerInterface) _registry);
            }

			RServices rstub = (RServices)java.rmi.server.RemoteObject.toStub(this);
			//R._instance=rstub;
			_isReady = true;

		} catch (Exception ex) {
            log.error("Error!", ex);
			throw new RemoteException("<" + Utils.getStackTraceAsString(ex) + ">");
		}
	}

	@Override
	public void logInfo(String message) throws RemoteException {
		log.info("Name:" + this.getServantName());
		log.info("Stub:" + PoolUtils.stubToHex(this));
		log.info(message);

        DirectJNI.getInstance().notifyRActionListeners(
                new RAction(RActionType.ADMIN_MESSAGE).put(RActionConst.MESSAGE, message));
	}

	public String unsafeGetObjectAsString(String cmd) throws RemoteException {
		return DirectJNI.getInstance().getRServices().unsafeGetObjectAsString(cmd);
	}
	
	public void stop() throws RemoteException {
		DirectJNI.getInstance().getRServices().stop();
	}

	public RNI getRNI() throws RemoteException {
		return _remoteRni;
	}

	public String getStatus() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getStatus();
	}

	public boolean isReference(RObject obj) throws RemoteException {
		return DirectJNI.getInstance().getRServices().isReference(obj);
	}

	public RObject call(String methodName, Object... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().call(methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject callAndGetReference(String methodName, Object... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().callAndGetReference(methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public RObject callAndGetObjectName(String methodName, Object... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().callAndGetObjectName(methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void callAndAssign(String varName, String methodName, Object... args) throws RemoteException {
		DirectJNI.getInstance().getRServices().callAndAssign(varName, methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}
	
	public Object callAndConvert(String methodName, Object... args) throws RemoteException {
		Object result=DirectJNI.getInstance().getRServices().callAndConvert( methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void freeReference(RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().freeReference(refObj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject referenceToObject(RObject refObj) throws RemoteException {

		ReferenceInterface refObjCast = (ReferenceInterface) refObj;
		if (refObjCast.getAssignInterface().equals(_assignInterface)) {
			RObject result = DirectJNI.getInstance().getRServices().referenceToObject(refObj);
			if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
			return result;
		} else {
			return refObjCast.extractRObject();
		}
	};

	public RObject putAndGetReference(Object obj) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().putAndGetReference(obj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void putAndAssign(Object obj, String name) throws RemoteException {
		DirectJNI.getInstance().getRServices().putAndAssign(obj, name);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject getObject(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getObject(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject getReference(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getReference(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public Object getObjectConverted(String expression) throws RemoteException {
		Object result = DirectJNI.getInstance().getRServices().getObjectConverted(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public RObject getObjectName(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getObjectName(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}


	public RObject realizeObjectName(RObject objectName) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().realizeObjectName(objectName);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	
	public Object realizeObjectNameConverted(RObject objectName) throws RemoteException {
		Object result = DirectJNI.getInstance().getRServices().realizeObjectNameConverted(objectName);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public void freeAllReferences() throws RemoteException {
		DirectJNI.getInstance().getRServices().freeAllReferences();
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		
	}
	
	public Object convert(RObject obj) throws RemoteException {
		Object result = DirectJNI.getInstance().getRServices().convert(obj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void assignReference(String varname, RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().assignReference(varname, refObj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public String evaluate(String expression) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String evaluate(final String expression, final int n) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression, n);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromResource(String resource) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromResource(resource);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromBuffer(String buffer) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromBuffer(buffer);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromResource(String resource, HashMap<String, Object> attributes) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromResource(resource, attributes);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromBuffer(String buffer, HashMap<String, Object> attributes) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromBuffer(buffer, attributes);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public boolean symbolExists(String symbol) throws RemoteException {
		return DirectJNI.getInstance().getRServices().symbolExists(symbol);
	}
	
	public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().addRCollaborationListener(collaborationListener);
	}
	
	public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().removeRCollaborationListener(collaborationListener);
	}
	
	public void removeAllRCollaborationListeners() throws RemoteException {
		DirectJNI.getInstance().getRServices().removeAllRCollaborationListeners();
	}
	
	public boolean hasRCollaborationListeners() throws RemoteException {
		return DirectJNI.getInstance().getRServices().hasRCollaborationListeners();
	}
	
	public void addRConsoleActionListener(RActionListener helpListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().addRConsoleActionListener(helpListener);
	}	
	
	public void removeRConsoleActionListener(RActionListener helpListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().removeRConsoleActionListener(helpListener);		
	}
	
	public void removeAllRConsoleActionListeners() throws RemoteException {
		DirectJNI.getInstance().getRServices().removeAllRConsoleActionListeners();		
	}
	
	public void  registerUser(String sourceUID,String user) throws RemoteException {
		DirectJNI.getInstance().getRServices().registerUser(sourceUID,user);
	}
	
	public void  unregisterUser(String sourceUID) throws RemoteException {
		DirectJNI.getInstance().getRServices().unregisterUser(sourceUID);
	}
	
	public void  updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException {
		DirectJNI.getInstance().getRServices().updateUserStatus(sourceUID,userStatus);
	}
	
	public UserStatus[] getUserStatusTable() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getUserStatusTable();
	}
	
	
	public void setUserInput(String userInput) throws RemoteException {
		DirectJNI.getInstance().getRServices().setUserInput(userInput);		
	}
	
	public void chat(String sourceUID, String user, String message) throws RemoteException {
		DirectJNI.getInstance().getRServices().chat(sourceUID, user, message);
	}
	
	public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
		DirectJNI.getInstance().getRServices().consolePrint(sourceUID, user, expression, result);		
	}
			
	public GenericCallbackDevice newGenericCallbackDevice() throws RemoteException {
		GenericCallbackDevice result = new GenericCallbackDeviceImpl(_genericCallbackDeviceHashMap);
		DirectJNI.getInstance().getRServices().addRConsoleActionListener(result);
		DirectJNI.getInstance().getRServices().addRCollaborationListener(result);
        addLogListener(result);

        return result;
	}
	
	public GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException {
		GenericCallbackDevice[] result=new GenericCallbackDevice[_genericCallbackDeviceHashMap.values().size()];
		int i=0; for (GenericCallbackDevice d:_genericCallbackDeviceHashMap.values()) result[i++]=d; 
		return result;
	}
	
	public String[] listPackages() throws RemoteException {
		return (String[]) _rim.keySet().toArray(new String[0]);
	}

	public RPackage getPackage(String packageName) throws RemoteException {
		return _rim.get(packageName);
	}

	public String getLogs() throws RemoteException {
		return _log.toString();
	}

    private void disposeDevices() {

        Vector<Integer> devices = new Vector<Integer>();
        for (Integer d : _deviceHashMap.keySet())
            devices.add(d);

        log.info("graphic devices: " + devices);

        for (Integer d : devices) {
            try {
                log.info("disposing graphic device: " + d);

                _deviceHashMap.get(d).dispose();
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }

        Vector<String> genericDevices = new Vector<String>();
        for (String generic : _genericCallbackDeviceHashMap.keySet()) {
            genericDevices.add(generic);
        }

        log.info("generic devices: " + genericDevices);

        for (String generic : genericDevices) {
            try {
                log.info("disposing generic device: " + generic);

                _genericCallbackDeviceHashMap.get(generic).dispose();
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }
    }

	public void reset() throws RemoteException {
		if (isResetEnabled()) {

            log.info("reset");

            runR(new ExecutionUnit() {
				public void run(Rengine e) {

					String[] allobj = e.rniGetStringArray(
                            e.rniEval(e.rniParse(".PrivateEnv$ls(all.names=TRUE)", 1), 0));

                    if (allobj != null) {
						for (int i = 0; i < allobj.length; ++i) {
                            if (DirectJNI.getInstance().getBootStrapRObjects().contains(allobj[i])) {
                            } else {
                                e.rniEval(e.rniParse("rm(" + allobj[i] + ")", 1), 0);
                            }
                        }
					}

					DirectJNI.getInstance().unprotectAll();
				}
			});

            disposeDevices();
		}
	}

	public String ping() throws java.rmi.RemoteException {
		if (_isReady) {
            return super.ping();
        } else {
			throw new InitializingException();
		}
	}

	public void die() throws java.rmi.RemoteException {
		shutdown();
	}

	public boolean hasConsoleMode() throws RemoteException {
		return true;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		return DirectJNI.getInstance().getRServices().consoleSubmit(cmd);
	}

	public String consoleSubmit(String cmd, HashMap<String, Object> attributes) throws RemoteException {
		return DirectJNI.getInstance().getRServices().consoleSubmit(cmd, attributes);
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
        DirectJNI.getInstance().getRServices().asynchronousConsoleSubmit(cmd);
	}

	public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> attributes) throws RemoteException {
        DirectJNI.getInstance().getRServices().asynchronousConsoleSubmit(cmd, attributes);
	}

	public boolean hasPushPopMode() throws RemoteException {
		return true;
	}

	public Serializable pop(String symbol) throws RemoteException {
		Serializable result = DirectJNI.getInstance().getRServices().getObject(symbol);
        log.info("result for " + symbol + " : " + result);
		return result;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
		DirectJNI.getInstance().getRServices().putAndAssign((RObject) object, symbol);
	}

	public String[] listSymbols() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listSymbols();
	}

	public boolean hasGraphicMode() throws RemoteException {
		return true;
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		return new RGraphicsPanelRemote(w, h, _graphicNotifier);
	}

	public GDDevice newDevice(int w, int h) throws RemoteException {
		return new GDDeviceImpl(w, h, false, _deviceHashMap);
	}

	public GDDevice newBroadcastedDevice(int w, int h) throws RemoteException {
		return new GDDeviceImpl(w, h, true, _deviceHashMap);
	}
    
	public GDDevice[] listDevices() throws RemoteException {
		GDDevice[] result=new GDDevice[_deviceHashMap.values().size()];
		int i=0; for (GDDevice d:_deviceHashMap.values()) result[i++]=d; 
		return result;
	}

    // random access
    public FileDescription getRandomAccessFileDescription(String fileName) throws java.rmi.RemoteException {
        return DirectJNI.getInstance().getRServices().getRandomAccessFileDescription(fileName);
    }

    public void createRandomAccessFile(String fileName) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().createRandomAccessFile(fileName);
    }

    public void createRandomAccessDir(String dirName) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().createRandomAccessDir(dirName);
    }

    public void renameRandomAccessFile(String fileName1, String fileName2) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().renameRandomAccessFile(fileName1, fileName2);
    }

    public void removeRandomAccessFile(String fileName) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().removeRandomAccessFile(fileName);
    }

    public byte[] readRandomAccessFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException {
        return DirectJNI.getInstance().getRServices().readRandomAccessFileBlock(fileName, offset, blocksize);
    }

    public void appendBlockToRandomAccessFile(String fileName, byte[] block) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().appendBlockToRandomAccessFile(fileName, block);
    }

    public void copyRandomAccessFile(String srcFileName, String dstFileName) throws java.rmi.RemoteException{
        DirectJNI.getInstance().getRServices().copyRandomAccessFile(srcFileName, dstFileName);
    }

    public void copyRandomAccessDir(String srcPath, String dstPath) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().copyRandomAccessDir(srcPath, dstPath);
    }

    public void moveRandomAccessFile(String srcFileName, String dstFileName) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().moveRandomAccessFile(srcFileName, dstFileName);
    }

    public void moveRandomAccessDir(String srcPath, String dstPath) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().moveRandomAccessDir(srcPath, dstPath);
    }

    // workspace
    public void loadWorkspace(String path) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().loadWorkspace(path);
    }

    public void saveWorkspace(String path) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().saveWorkspace(path);
    }

    // reinit
    public void reinitServer() throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().reinitServer();
    }
    
    // working directory
	public FileDescription[] getWorkingDirectoryFileDescriptions() throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().getWorkingDirectoryFileDescriptions();
	}

	public FileDescription getWorkingDirectoryFileDescription(String fileName) throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().getWorkingDirectoryFileDescription(fileName);
	}

	public void createWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
		DirectJNI.getInstance().getRServices().createWorkingDirectoryFile(fileName);
	}

	public void removeWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
		DirectJNI.getInstance().getRServices().removeWorkingDirectoryFile(fileName);
	}

	public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().readWorkingDirectoryFileBlock(fileName, offset, blocksize);
	}

	public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws java.rmi.RemoteException {
		DirectJNI.getInstance().getRServices().appendBlockToWorkingDirectoryFile(fileName, block);
	}

	public byte[] getRHelpFile(String uri) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getRHelpFile(uri);
	}

	public String getRHelpFileUri(String topic, String pack) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getRHelpFileUri(topic, pack);
	}

	public String[] listDemos() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listDemos();
	}

	public String getDemoSource(String demoName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getDemoSource(demoName);
	}

	public boolean isBusy() throws RemoteException {
		return DirectJNI.getInstance().getRServices().isBusy();
	}

	static int countCreated(Future<RServices>[] futures) {
		int result = 0;
		for (int i = 0; i < futures.length; ++i)
			if (futures[i].isDone())
				++result;
		return result;
	}

	public Vector<String> getSvg(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getSvg(script, width, height);
	}

	public byte[] getPdf(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPdf(script, width, height);
	}

	public void setJobId(String jobId) throws RemoteException {
		_jobId = jobId;
		if (_registry instanceof DBLayerInterface) {
			((DBLayerInterface)_registry).setJobID(_servantName, _jobId);
		}		
	}

    public void setOwner(String owner) throws java.rmi.RemoteException {
        if (_registry instanceof DBLayerInterface) {
            _owner = ((DBLayerInterface)_registry).getUser(owner);

            ((DBLayerInterface)_registry).setOwner(_servantName, _owner.getLogin());
        }
    }

    public String getOwner() throws java.rmi.RemoteException {
        return ((_owner != null) ? (_owner.getLogin()) : (null));
    }

    public void setProject(String projectFolder) throws java.rmi.RemoteException {
        _projectFolder = projectFolder;
        if (_registry instanceof DBLayerInterface) {
            ((DBLayerInterface)_registry).setProject(_servantName, _projectFolder);
        }
    }

    public void setMaster(String master) throws java.rmi.RemoteException {
        masterServerName = master;
        if (_registry instanceof DBLayerInterface) {
            ((DBLayerInterface)_registry).setMaster(_servantName, masterServerName);
        }
    }

    public String getMaster() throws java.rmi.RemoteException {
        return masterServerName;
    }

	public String getStub() throws RemoteException {
		return super.getStub();
	}
	
    public void addProbeOnVariables(String[] variables) throws RemoteException {
    	DirectJNI.getInstance().getRServices().addProbeOnVariables(variables);
    }

    class ShutdownRunnable implements Runnable {
        private boolean save;
        private String path;

        public ShutdownRunnable(boolean save, String path){
            this.save = save;
            this.path = path;
        }

        public void run() {
            // initial delay
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }

            disposeDevices();

            RListener.stopAllClusters();

            try {
                UnicastRemoteObject.unexportObject(RServantImpl.this, true);
            } catch (Exception ex) {
            }

            if (save) {
                DirectJNI.getInstance().saveAndShutdown(path);
            } else {
                DirectJNI.getInstance().shutdown();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

            System.exit(0);
        }

    }

    public void shutdown() throws java.rmi.RemoteException {

        new Thread(new ShutdownRunnable(false, null)).start();
    }

    public void saveAndShutdown(String path) throws java.rmi.RemoteException {

        new Thread(new ShutdownRunnable(true, path)).start();
    }

    public void removeProbeOnVariables(String[] variables) throws RemoteException{
    	DirectJNI.getInstance().getRServices().removeProbeOnVariables(variables);
    }
    
    public String[] getProbedVariables() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getProbedVariables();
    }
    public void setProbedVariables(String[] variables) throws RemoteException {
    	DirectJNI.getInstance().getRServices().setProbedVariables(variables);    	
    }
    
    public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getMissingLibraries(requiredLibraries);
    }

    public String getWorkingDirectory() throws java.rmi.RemoteException {
        return DirectJNI.getInstance().getRServices().getWorkingDirectory();
    }

    public void setWorkingDirectory(String dir) throws java.rmi.RemoteException {
        DirectJNI.getInstance().getRServices().setWorkingDirectory(dir);
    }

    public void saveHistory(Vector<String> history, String filename, boolean append) throws RemoteException {
        DirectJNI.getInstance().saveHistory(history, filename, append);
    }

    public Vector<String> loadHistory(String filename) throws RemoteException {
        return DirectJNI.getInstance().loadHistory(filename);
    }

    public String[] exec(String command) throws RemoteException {
        return DirectJNI.getInstance().exec(command);
    }

    public String[] exec(String command, HashMap<String, Object> attributes) throws RemoteException {
        return DirectJNI.getInstance().exec(command, attributes);
    }

    public Vector<SearchResult> search(SearchRequest request) throws RemoteException {
        return DirectJNI.getInstance().search(request);
    }

    public Vector<SearchResult> search(SearchRequest request, HashMap<String, Object> attributes) throws RemoteException {
        return DirectJNI.getInstance().search(request, attributes);
    }

    public void searchAsync(SearchRequest request) throws RemoteException {
        DirectJNI.getInstance().searchAsync(request);
    }

    public void searchAsync(SearchRequest request, HashMap<String, Object> attributes) throws RemoteException {
        DirectJNI.getInstance().searchAsync(request, attributes);
    }

    /*
    private Checksum recursiveGetTree2(Object obj, Checksum cs) {
        FileNode node = (FileNode) obj;
        File[] filelist = new File(node.getPath()).listFiles();
        if (filelist != null && filelist.length > 0) {

            node.setChildrenNodes(filelist);

            for (int j=0;j<node.getChildCount();j++) {
                cs = recursiveGetTree2(node.getChild(j), cs);
            }
        }
        if (cs != null) {
            cs.add(node.length() + 1); // add 1 in case length is zero
            cs.add(node.getName().hashCode());
        }

        return cs;
    }
    */

    private void plainGetFileList(Object obj) {
        FileNode node = (FileNode) obj;
        File[] filelist = new File(node.getPath()).listFiles();
        if (filelist != null && filelist.length > 0) {
            node.setChildrenNodes(filelist);
        }
    }


    public void setRootDirectory(String rootfolder) throws java.rmi.RemoteException {
        //log.info("setRootDirectory-root="+root);

        fileTreeMonitor.removeAllFiles();

        if (DirectJNI.getInstance().areRActionListenersAvailable()) {

            if (_owner != null) {
                FileNode rootnode = null;

                if (isPathInSandbox(rootfolder) || _owner.isSupervisor()) {
                    // set the root folder
                    //
                    rootnode = new FileNode(new File(rootfolder));
                } else {

                    // create fake root node
                    //
                    rootnode = new FileNode(rootfolder, 0, 0, false);
                }

                DirectJNI.getInstance().notifyRActionListeners(
                        new RAction(RActionType.UPDATE_FILETREE).put(RActionConst.ROOTNODE, rootnode));
            }

        }

        if (fileTreeMonitor != null) {
            fileTreeMonitor.wakeup();
        }
    }

    public boolean isPathInSandbox(String directory) {
        return (directory.startsWith(_owner.getUserFolder())
                && !directory.contains(".."));
    }

    public FileNode readDirectory(String directory) throws java.rmi.RemoteException {

        if (_owner != null) {
            if (isPathInSandbox(directory) || _owner.isSupervisor()) {
                // start monitoring
                fileTreeMonitor.addFile(new File(directory));

                // read the contents
                return readDirectoryInternal(directory);
            }
        }

        // don't read
        //
        return makeEmptyFileNode(directory);
    }

    private FileNode makeEmptyFileNode(String directory) {
        return new FileNode(new File(directory));
    }


    private FileNode readDirectoryInternal(String directory) {

        //log.info("readDirectoryInternal-directory="+directory);

        File file = new File(directory);
        
        FileNode node = new FileNode(file);

        File[] filelist = file.listFiles();

        if (filelist != null) {
            node.setChildrenNodes(filelist);
        } else {
            node.setChildrenNodes(new File[0]);
        }

        return node;
    }

    class DirectoryListener implements FileListener {
        public void fileChanged (FileEvent event) {
            //log.info("fileChanged-file="+((File)event.getSource()).getPath());

            File file = (File) event.getSource();

            if (!file.exists()) {
                fileTreeMonitor.removeFile(file);

                //log.info("fileChanged-remove the file="+((File)event.getSource()).getPath());
            } else {
                if (DirectJNI.getInstance().areRActionListenersAvailable()) {

                    FileNode node = readDirectoryInternal(file.getPath());

                    DirectJNI.getInstance().notifyRActionListeners(
                            new RAction(RActionType.UPDATE_FILENODE).put(RActionConst.FILENODE, node));
                }
            }
        }
    }


    static class FileEvent implements Serializable {

        private static final long serialVersionUID = 1L;

        public static final String names[] =
                { "FILE_CREATED", "FILE_DELETED", "FILE_MODIFIED" };

        public static final int FILE_CREATED = 0;
        public static final int FILE_DELETED = 1;
        public static final int FILE_MODIFIED = 2;

        private int id;
        private Object source;

        public FileEvent(Object source, int id) {
            this.id = id;
            this.source = source;
        }

        public int getId() {
            return id;
        }

        public Object getSource() {
            return source;
        }

        public String toString(){
            return "id=" + names[id] + " source="+source.toString();
        }
    }

    static interface FileListener {
        void fileChanged (FileEvent event);
    }

    public static class FileMonitor extends Thread {

        private HashMap files;       // File -> Long
        private Collection listeners;   // of WeakReference(FileListener)

        private boolean shutdown = false;
        private long pollingInterval = 10000;

        public FileMonitor (long pollingInterval) {
            this.pollingInterval = pollingInterval;

            files = new HashMap();
            listeners = new ArrayList();
        }

        public void shutdown() {
            shutdown = true;

            files = new HashMap();
            listeners = new ArrayList();

            this.interrupt();
        }

        public synchronized void addFile (File file) {
            //log.info("addFile-file="+file.getPath());

            if (!files.containsKey (file)){
                long modifiedTime = file.exists() ? file.lastModified() : -1;
                files.put (file, new Long (modifiedTime));
            }
        }

        public synchronized void removeFile(File file) {
            //log.info("removeFile-file="+file.getPath());
            files.remove (file);
        }

        public synchronized void removeAllFiles() {
            //log.info("removeAllFiles");
            files = new HashMap();
        }

        public synchronized void addListener (FileListener fileListener) {

            //log.info("addListener");

            for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                //WeakReference reference = (WeakReference) i.next();
                //FileListener listener = (FileListener) reference.get();

                FileListener listener = (FileListener) i.next();
                if (listener == fileListener) {
                    return;
                }
            }

            // Use WeakReference to avoid memory leak if this becomes the
            // sole reference to the object.

            //listeners.add (new WeakReference (fileListener));
            listeners.add (fileListener);
        }


        public synchronized void removeListener (FileListener fileListener) {
            //log.info("removeListener");

            for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                //WeakReference reference = (WeakReference) i.next();
                //FileListener listener = (FileListener) reference.get();
                FileListener listener = (FileListener) i.next();
                
                if (listener == fileListener) {
                    i.remove();
                    break;
                }
            }
        }

        @Override
        public void run() {
            while(!shutdown) {

                //log.info("FileMonitor-run");
                //updateFileTree();

                runMonitor();

                try {
                    Thread.sleep(pollingInterval);
                } catch (Exception ex) {
                }
            }

        }


        public void runMonitor() {
            //log.info("FileMonitor-runMonitor");

            Collection fkeyset = new ArrayList (files.keySet());

            Iterator i = fkeyset.iterator();

            while( i.hasNext() ) {

                File file = (File) i.next();

                //log.info("FileMonitor-runMonitor-file="+file.getPath());

                long lastModifiedTime = (Long) files.get(file);
                long newModifiedTime  = file.exists() ? file.lastModified() : -1;

                //log.info("FileMonitor-runMonitor-lastModifiedTime="+lastModifiedTime);
                //log.info("FileMonitor-runMonitor-newModifiedTime="+newModifiedTime);


                // Chek if file has changed
                if (newModifiedTime != lastModifiedTime) {

                    // Register new modified time
                    files.put (file, newModifiedTime);

                    // Notify listeners
                    for (Iterator j = listeners.iterator(); j.hasNext(); ) {

                        //WeakReference reference = (WeakReference) j.next();
                        //FileListener listener = (FileListener) reference.get();

                        FileListener listener = (FileListener) j.next();

                        //log.info("FileMonitor-runMonitor-got listener");

                        // Remove from list if the back-end object has been GC'd
                        if (listener == null) {
                            j.remove();
                        } else {
                            //log.info("FileMonitor-runMonitor-calling listener");

                            listener.fileChanged(new FileEvent(file, FileEvent.FILE_MODIFIED));
                        }
                    }
                }
            }
        }

        public void wakeup() {
            this.interrupt();
        }
    }

}
