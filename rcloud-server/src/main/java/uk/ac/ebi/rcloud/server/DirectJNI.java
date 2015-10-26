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

import sun.misc.Signal;
import sun.misc.SignalHandler;
import uk.ac.ebi.rcloud.server.deadlock.ThreadWarningSystem;
import uk.ac.ebi.rcloud.server.file.FileNode;
import uk.ac.ebi.rcloud.server.graphics.LocalGraphicNotifier;
import uk.ac.ebi.rcloud.rpf.PoolUtils;
import uk.ac.ebi.rcloud.rpf.RemoteLogListener;
import uk.ac.ebi.rcloud.server.reference.RTypeName.ObjectNameInterface;

import uk.ac.ebi.rcloud.server.reference.RTypeRef.*;
import uk.ac.ebi.rcloud.server.reference.ReferenceInterface;

import uk.ac.ebi.rcloud.server.RType.*;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.RType.RFactor;
import uk.ac.ebi.rcloud.server.RType.RUnknown;
import uk.ac.ebi.rcloud.server.RType.RNamedArgument;
import uk.ac.ebi.rcloud.server.RType.RMatrix;
import uk.ac.ebi.rcloud.server.RType.REnvironment;
import uk.ac.ebi.rcloud.server.RType.RLogical;
import uk.ac.ebi.rcloud.server.RType.RNumeric;
import uk.ac.ebi.rcloud.server.RType.RInteger;
import uk.ac.ebi.rcloud.server.RType.RVector;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RS3;

import uk.ac.ebi.rcloud.server.callback.*;
import uk.ac.ebi.rcloud.server.callback.RCollaborationListener;
import uk.ac.ebi.rcloud.server.reference.AssignInterface;
import uk.ac.ebi.rcloud.server.exception.NoMappingAvailable;
import uk.ac.ebi.rcloud.server.file.FileDescription;
import uk.ac.ebi.rcloud.server.graphics.GDContainerBag;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.GraphicNotifier;
import uk.ac.ebi.rcloud.server.graphics.DoublePoint;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import uk.ac.ebi.rcloud.server.graphics.rmi.RGraphicsPanelRemote;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.*;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import uk.ac.ebi.rcloud.server.iplots.SVarInterfaceRemote;
import uk.ac.ebi.rcloud.server.iplots.SVarSetInterfaceRemote;
import uk.ac.ebi.rcloud.rpf.RemotePanel;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.RengineWrapper;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JavaGD;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.rcloud.server.RConst.*;

import uk.ac.ebi.rcloud.server.search.SearchRequest;
import uk.ac.ebi.rcloud.server.search.SearchResult;
import uk.ac.ebi.rcloud.server.search.SearchResultContainer;
import uk.ac.ebi.rcloud.version.SoftwareVersion;

public class DirectJNI {

    final private static Logger log = LoggerFactory.getLogger(DirectJNI.class);

	public static ClassLoader _mappingClassLoader = DirectJNI.class.getClassLoader();
	public static ClassLoader _resourcesClassLoader = DirectJNI.class.getClassLoader();

	public static Vector<String> _abstractFactories = new Vector<String>();
	public static HashMap<String, String> _factoriesMapping = new HashMap<String, String>();
	public static HashMap<String, String> _s4BeansMappingRevert = new HashMap<String, String>();
	public static HashMap<String, String> _s4BeansMapping = new HashMap<String, String>();
	public static Vector<String> _packageNames = new Vector<String>();
	public static HashMap<String, Class<?>> _s4BeansHash = new HashMap<String, Class<?>>();
	public static HashMap<String, Vector<Class<?>>> _rPackageInterfacesHash = new HashMap<String, Vector<Class<?>>>();

	private static final String V_NAME_PREFIXE = "V__";
	private static final String V_TEMP_PREFIXE = V_NAME_PREFIXE + "TEMP__";
	private static final String PENV = ".PrivateEnv";
	private static final String PROTECT_VAR_PREFIXE = "PROTECT_";
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private static final String ECHO_VAR_NAME = ".echo___";
	private static final Integer singletonLock = new Integer(0);
	private static DirectJNI _djni = null;
	private static String INSTANCE_NAME = "LOCAL_R";
	private static String DEFAULT_WDIR_ROOT = System.getProperty("java.io.tmpdir");
	private static String WDIR = null;
	private static HashMap<String, String> _symbolUriMap = new HashMap<String, String>();
	public Rengine _rEngine = null;
	public static RMainLoopCallbacksImpl _rCallbacks = null;

    private static long DEFAULT_R_CALL_TIMEOUT = 1000 * 60 * 60 * 24;
    private static String R_CALL_COMEPLETE = "complete";

    private final ReentrantLock _runRlock = new ReentrantLock();
	private final ReentrantLock _mainLock = new ReentrantLock();
	private long _varCounter = 0;
	private long _tempCounter = 0;
	private String _continueStr = null;
	private String _promptStr = null;
	private Vector<String> _bootstrapRObjects = new Vector<String>();
	private long _privateEnvExp;
	private String[] _packNames = null;
	private HashMap<String, Vector<String>> _nameSpacesHash = new HashMap<String, Vector<String>>();
	private HashMap<String, RPackage> _packs = new HashMap<String, RPackage>();
	private Vector<Long> _protectedExpReference = new Vector<Long>();
	private static Vector<String> demosList = null;
	private HashMap<String, byte[]> _resourceCache = new HashMap<String, byte[]>();
	private String _userInput = null;
	private boolean _stopRequired = false;
	private HashMap<String, UserStatus> _usersHash = new HashMap<String, UserStatus>();
	private Vector<RActionProcessor> _rActionProcessors = new Vector<RActionProcessor>();
	private String _originatorUID;
	private Vector<RCollaborationListener> _rCollaborationListeners = new Vector<RCollaborationListener>();

    private static long DEFAULT_SHELL_EXEC_STDOUT_TIMEOUT = 1000 * 5;

    private boolean _shutdownRequested = false;

    private ThreadWarningSystem threadWarningSystem = initThreadWarningSystem();

    private ThreadWarningSystem initThreadWarningSystem() {
        ThreadWarningSystem tws = new ThreadWarningSystem(500);

        tws.addListener(new ThreadWarningSystem.Listener() {
            public void deadlockDetected(ThreadInfo deadlockedThread) {
                log.info("Deadlocked Thread:");
                log.info("------------------");
                log.info(deadlockedThread.toString());
                for (StackTraceElement ste : deadlockedThread.getStackTrace()) {
                    log.info(("\t" + ste));
                }
            }

            public void thresholdExceeded(ThreadInfo[] allThreads) {
                log.info("Thread Threshold Exceed");
            }
        });

        return tws;
    }

	public static DirectJNI getInstance() {
		if (_djni != null)
			return _djni;
		synchronized (singletonLock) {
			if (_djni == null) {
				_djni = new DirectJNI();
			}
			return _djni;
		}
	}

	public String runR(ExecutionUnit eu) {

        if (Thread.currentThread() == _rEngine) {
            throw new RuntimeException("runR called from within the R MainLoop Thread");
        } else {

            //log.info("runR-eu.getInput()=" + eu.getInput());

            ArrayBlockingQueue<String> notifyQueue = new ArrayBlockingQueue<String>(2);

            eu.setNotifyQueue(notifyQueue);

            try {
                executionQueue.put(eu);
            } catch (InterruptedException ie) {
                log.info("runR-InterruptedException");
            }

            try {
                String complete = notifyQueue.poll(DEFAULT_R_CALL_TIMEOUT, TimeUnit.MILLISECONDS);

                if (complete != null) {
                    if (complete.equals(R_CALL_COMEPLETE)) {
                        // good
                    } else {
                        log.info("runR-complete="+complete);
                        log.info("runR-eu.getResult()="+eu.getResult().toString());
                    }
                } else {
                    log.info("runR-timeout-command="+eu.getInput());
                }
            } catch (InterruptedException ie) {
                log.info("Error!", ie);
            }
        }
        return eu.getResult().toString();
	}

	InputStream getResourceAsStream(String resource) {
		if (resource.startsWith("/"))
			resource = resource.substring(1);
		byte[] buffer = _resourceCache.get(resource);
		if (buffer != null) {
			return new ByteArrayInputStream(buffer);
		} else {
			InputStream is = _resourcesClassLoader.getResourceAsStream(resource);
			if (is == null)
				return null;
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int b;
				while ((b = is.read()) != -1) {
					baos.write(b);
				}
				buffer = baos.toByteArray();
			} catch (Exception e) {
                log.error("Error!", e);
				return null;
			}
			_resourceCache.put(resource, buffer);
			return new ByteArrayInputStream(buffer);
		}
	}

    public void shutdown() {
        removeAllRActionListeners();
        _shutdownRequested = true;
        _rEngine.rniStop(0);
        _rEngine.end();
    }

    public void saveAndShutdown(String path) {

        try{
            DirectJNI.getInstance().getRServices().saveWorkspace(path);
        } catch (Exception ex) {
        }

        shutdown();
    }

	boolean resourceExists(String resource) {
		return getResourceAsStream(resource) != null;
	}

	public boolean runRInProgress() {
		return _runRlock.isLocked();
	}

	public void snapRBusinessIndicator() {
		int b = _rCallbacks.busy ? 1 : 0;
		int notb = _rCallbacks.busy ? 0 : 1;

        _rCallbacks.rBusy(null, notb);
		_rCallbacks.rBusy(null, b);
	}

    private ArrayBlockingQueue<ExecutionUnit> executionQueue
            = new ArrayBlockingQueue<ExecutionUnit>(100);

	private class RMainLoopCallbacksImpl implements RMainLoopCallbacks {

        public boolean busy = false;
        private ExecutionUnit executionUnit = null;
        private Vector<RActivityCallback> busycallbacks = new Vector<RActivityCallback>();
        private ArrayBlockingQueue<Integer> saveHistoryQueue = new ArrayBlockingQueue<Integer>(1);
        private ArrayBlockingQueue<Integer> loadHistoryQueue = new ArrayBlockingQueue<Integer>(1);


        public RMainLoopCallbacksImpl(){
            //System.setOut(new PrintStream(new OutputStreamWrapper(System.out)));
        }

        //long[] variablePointersBefore = null;

        public void addBusyCallback(RActivityCallback callback) {
            try {
                busycallbacks.add(callback);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }

        public void removeBusyCallback(RActivityCallback callback) {
            try {
                busycallbacks.remove(callback);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }

        public ArrayBlockingQueue<Integer> getSaveHistoryQueue() {
            return saveHistoryQueue;
        }

        public ArrayBlockingQueue<Integer> getLoadHistoryQueue() {
            return loadHistoryQueue;
        }

        class OutputStreamWrapper extends OutputStream {

            private PrintStream out;
            public OutputStreamWrapper(PrintStream out) {
                this.out = out;
            }

            public void write(final byte[] b) throws IOException {
                try {
                    out.write(b);
                    appendText("STDOUT " + new String(b));
                } catch (Exception ex) {
                    log.error("Error!", ex);
                }
            }

            public void write(final byte[] b, final int off, final int len) throws IOException {
                try {
                    out.write(b, off, len);
                    appendText("STDOUT " + new String(b, off, len));
                } catch (Exception ex) {
                    log.error("Error!", ex);
                }

            }

            public void write(final int b) throws IOException {
                try {
                    out.write(b);
                    appendText("STDOUT " + new String(new byte[] { (byte) b, (byte) (b >> 8) }));
                } catch (Exception ex) {
                    log.error("Error!", ex);
                }
            }
        }

		public void rBusy(Rengine re, int which) {
            try {
                boolean busy0 = (which == 1);

                if (busy != busy0) {
                    notifyRActionListeners(new RAction(RActionType.BUSY_ACTION)
                            .put(RActionConst.BUSY, new Integer(which)));

                    try {
                        for (RActivityCallback callback : busycallbacks) {
                            callback.notify(which, executionUnit.getAttributes());
                        }
                    } catch (Throwable ex) {
                    }
                }
                busy = busy0;
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
		}

		public String rChooseFile(Rengine re, int newFile) {
			return null;
		}

		public void rFlushConsole(Rengine re) {
			//log.info("rFlushConsole");
		}

        public String rReadConsole(Rengine re, String prompt, int addToHistory) {

            String input = null;

            // unlock R
            if (_mainLock.isLocked()) {
                try {
                    _mainLock.unlock();
                } catch (Exception ex) {
                    log.error("Error unlocking the _mainLock", ex);
                }
            }

            while (!_shutdownRequested) {

                if (executionUnit != null) {

                    // notify completion
                    if (executionUnit.getNotifyQueue() != null) {
                        executionUnit.getNotifyQueue().offer(R_CALL_COMEPLETE);
                    }


                    if (executionUnit.getInput() != null) {

                        // notify listeners
                        notifyRActionListeners(new RAction(RActionType.COMPLETE)
                                .put(RActionConst.COMMAND, executionUnit.getInput())
                                .put(RActionConst.RESULT, executionUnit.getResult().toString())
                                .putAll(executionUnit.getAttributes()));

                        // notify the new prompt
                        notifyRActionListeners(new RAction(RActionType.PROMPT)
                                .put(RActionConst.PROMPT, prompt)
                                .putAll(executionUnit.getAttributes()));
                    }
                }


                try {
                    //fireVariableChangedEvents(variablePointersBefore);

                    // reset execution unit
                    executionUnit = null;

                    // dequeue a new command
                    executionUnit = executionQueue.poll(20000, TimeUnit.MILLISECONDS);

                } catch (InterruptedException ie) {
                    // nothing to do
                    //
                    //executionUnit = null;
                }

                if (executionUnit != null) {
                    try {
                        _mainLock.lock();
                        executionUnit.run(re);
                    } catch (Exception ex) {
                        log.info("Error!", ex);
                    } finally {
                        try {
                            _mainLock.unlock();
                        } catch (Exception ex) {
                            log.error("Error unlocking the _mainLock", ex);
                        }
                    }

                    if (executionUnit.getInput() != null) {
                        input = executionUnit.getInput() + "\n";
                        break;
                    }
                }


                //variablePointersBefore = getVariablePointersBefore();
            }

            _mainLock.lock();

            return input;
        }

        public void rLoadHistory(Rengine re, String filename) {
            loadHistoryQueue.clear();
            notifyRActionListeners(new RAction(RActionType.LOAD_HISTORY).
                    put(RActionConst.FILENAME, filename));

            try {
                loadHistoryQueue.poll(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                log.info("Error!", ie);
            }
        }

		public void rSaveHistory(Rengine re, String filename) {
            saveHistoryQueue.clear();
            notifyRActionListeners(new RAction(RActionType.SAVE_HISTORY).
                    put(RActionConst.FILENAME, filename));

            try {
                saveHistoryQueue.poll(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                log.info("Error!", ie);
            }
        }

		public void rShowMessage(Rengine re, String message) {
            try {
                appendText("Message From R :" + message);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
		}

		private void appendText(String t) {
            if (executionUnit != null) {
                executionUnit.getResult().append(t);
            }

			notifyRActionListeners(new RAction(RActionType.CONSOLE)
                    .put(RActionConst.OUTPUT, t)
                    .putAll(executionUnit != null ? executionUnit.getAttributes() : null));
		}

		public void rWriteConsole(Rengine re, String s, int i) {
			try {
                appendText(s);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
		}
	}

    private void initialInitR() {
        try {
            //get
            getRServices().sourceFromResource("/rscripts/initial.R");
        } catch (Exception e) {
            log.error("Error!", e);
        }
    }

    private void reloadInitR() {
        try {
            getRServices().sourceFromResource("/rscripts/reload.R");
            sourceFromResourceDirect("/rscripts/direct.R");
        } catch (Exception e) {
            log.error("Error!", e);
        }
    }

    private DirectJNI(boolean empty) {

    }

	private DirectJNI() {

        log.info("INIT: started");

        System.setProperty("graphics.cached", "true");
        System.setProperty("graphics.estimation", "false");
        System.setProperty("graphics.compression", "9");
        System.setProperty("graphics.encoder", "png");

        System.setProperty("server.version", SoftwareVersion.getVersion());

        String usehandler = System.getProperty("useIntHandler");

        if (usehandler != null && usehandler.equalsIgnoreCase("true")) {
            try {
                Signal.handle(new Signal("INT"), new SignalHandler() {
                  public void handle(Signal sig) {
                    System.out.println(
                      "interrupt signal received");
                  }
                });

                log.info("--- added INT handler ---");

            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }

		_rCallbacks = new RMainLoopCallbacksImpl();

        log.info("INIT: callbacks created");

        _rEngine = new RengineWrapper(new String[] { "--no-save" }, true, _rCallbacks);

        log.info("INIT: rEngine created");

        //_rEngine.startMainLoop();
        //_rEngine.end();//start();

		if (!_rEngine.waitForR()) {
			log.info("Cannot load R");
			return;
		}

        log.info("INIT: R is ready");

		try {
            String workingdir = System.getProperty("working.dir.root");

            WDIR = workingdir != null && workingdir.length() > 0 ? workingdir
                    + "/" + INSTANCE_NAME : DEFAULT_WDIR_ROOT;

            initialInitR();

            log.info("INIT: initial R init done");

            reloadInitR();

            log.info("INIT: reload R init done");

			initPrivateEnv();

            log.info("INIT: private Env init done");

            testR();

			_continueStr = ((RChar) getRServices().getObject("as.character(options('continue'))")).getValue()[0];

            log.info("INIT: initialized continue option");

            _promptStr = ((RChar) getRServices().getObject("as.character(options('prompt'))")).getValue()[0];

            log.info("INIT: initialized prompt option");

			upgdateBootstrapObjects();

            log.info("INIT: updated boostrap objects");

			try {
				WDIR = new File(WDIR).getCanonicalPath().replace('\\', '/');
				regenerateWorkingDirectory(true);

                log.info("INIT: regenerated working directory");

			} catch (Exception e) {
                log.error("Error!", e);
			}

		} catch (Exception e) {
            log.error("Error!", e);
		}

        log.info("INIT: finished");

	}

	public Vector<String> getBootStrapRObjects() {
		return _bootstrapRObjects;
	}

	public void applySandbox() {
		try {
			getRServices().sourceFromResource("/rscripts/sandbox.R");
			upgdateBootstrapObjects();
		} catch (Exception e) {
            log.error("Error!", e);
		}
	}

	public void preprocessHelp() {

        try {
            _packNames = ((RChar) getRServices().getObject(".packages(all=T)")).getValue();

            for (int i = 0; i < _packNames.length; ++i) {
                try {

                    String uriPrefix = "/library/" + _packNames[i] + "/html/";
                    String indexFile = null;

                    if (System.getenv().get("R_LIBS") != null && !System.getenv().get("R_LIBS").equals("")) {
                        indexFile = System.getenv().get("R_LIBS") + "/" + _packNames[i] + "/html/" + "00Index.html";
                        if (!new File(indexFile).exists()) {
                            indexFile = null;
                        } else {
                            // log.info("index file:" + indexFile);
                        }
                    }

                    if (indexFile == null) {
                        indexFile = System.getenv().get("R_HOME") + uriPrefix + "00Index.html";
                        if (!new File(indexFile).exists())
                            indexFile = null;
                    }

                    if (indexFile == null)
                        continue;

                    Parser p = new Parser(indexFile);
                    processNode(_packNames[i], uriPrefix, p.extractAllNodesThatMatch(new TagNameFilter("BODY")).elementAt(0));

                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }

        } catch (Exception e) {
            log.error("Error!", e);
        }
	}

	public static void processNode(String packageName, String uriPrefix, Node n) {
		if (n instanceof LinkTag) {
			LinkTag lt = (LinkTag) n;
			if (lt.getLinkText() != null && !lt.getLinkText().equals("") && !lt.getLinkText().equalsIgnoreCase("overview")
					&& !lt.getLinkText().equalsIgnoreCase("directory") && lt.extractLink().endsWith(".html")) {
				_symbolUriMap.put(packageName + "~" + lt.getLinkText(),
                        uriPrefix + lt.extractLink().substring(lt.extractLink().lastIndexOf('/') + 1));
			}
		}
		NodeList children = n.getChildren();
		if (children != null) {
			for (int i = 0; i < children.size(); ++i) {
				processNode(packageName, uriPrefix, children.elementAt(i));
			}
		}
	}

	public void initPackages() {
		for (int i = 0; i < _packageNames.size(); ++i) {
			try {
				getRServices().getPackage(_packageNames.elementAt(i));
			} catch (Exception ex) {
                log.error("Error!", ex);
			}
		}
	}

	public long putObject(Object obj) throws Exception {
        return putObject(obj, null);

    }

	public long putObject(Object obj, Class<?>[] classHolder) throws Exception {

		Rengine e = _rEngine;

		if (obj == null) {
			return e.rniEval(e.rniParse("NULL", 1), 0);
		}

		if (obj instanceof ReferenceInterface) {
			throw new Exception("putObject is not allowed on proxy objects");
		}

		if (!(obj instanceof RObject)) {
			if (!obj.getClass().isArray()) {
				if (obj instanceof Integer)
					obj = new RInteger((Integer) obj);
				else if (obj instanceof Long)
					obj = new RInteger((int) ((Long) obj).longValue());
				else if (obj instanceof String)
					obj = new RChar((String) obj);
				else if (obj instanceof Double)
					obj = new RNumeric((Double) obj);
				else if (obj instanceof Float)
					obj = new RNumeric((Float) obj);
				else if (obj instanceof Boolean)
					obj = new RLogical((Boolean) obj);
				else if (obj instanceof ArrayList) {

					if (((ArrayList<?>) obj).size() > 0) {
						Class<?> componentType = ((ArrayList<?>) obj).get(0).getClass();
						if (componentType == Integer.class)
							obj = getRArrayFromJavaArray((Integer[]) ((ArrayList<?>) obj).toArray(new Integer[0]));
						else if (componentType == Long.class)
							obj = getRArrayFromJavaArray((Long[]) ((ArrayList<?>) obj).toArray(new Long[0]));
						else if (componentType == String.class)
							obj = getRArrayFromJavaArray((String[]) ((ArrayList<?>) obj).toArray(new String[0]));
						else if (componentType == Double.class)
							obj = getRArrayFromJavaArray((Double[]) ((ArrayList<?>) obj).toArray(new Double[0]));
						else if (componentType == Float.class)
							obj = getRArrayFromJavaArray((Float[]) ((ArrayList<?>) obj).toArray(new Float[0]));
						else if (componentType == Boolean.class)
							obj = getRArrayFromJavaArray((Boolean[]) ((ArrayList<?>) obj).toArray(new Boolean[0]));
						else {
							throw new Exception("cannot convert type in ArrayList");
						}
					} else {
						throw new Exception("empty ArrayList");
					}

				} else
					throw new Exception("argument classe must be a subclass of RObject or Standard Java Types");
			} else {
				Class<?> componentType = obj.getClass().getComponentType();
				if (componentType == int.class)
					obj = new RInteger((int[]) obj);
				else if (componentType == String.class)
					obj = new RChar((String[]) obj);
				else if (componentType == double.class)
					obj = new RNumeric((double[]) obj);
				else if (componentType == boolean.class)
					obj = new RLogical((boolean[]) obj);
				else {
					obj = getRArrayFromJavaArray(obj);

					// throw new Exception("argument classe must be a subclass
					// of RObject or Standard Java Types");
				}
			}
		}

		if (obj instanceof ObjectNameInterface) {
			String env = ((ObjectNameInterface) obj).getRObjectEnvironment();
			if (env == null || env.equals(""))
				env = ".GlobalEnv";
			return e.rniEval(e.rniParse(env + "$" + ((ObjectNameInterface) obj).getRObjectName(), 1), 0);
		}

		long resultId = -1;

		if (obj instanceof RLogical) {

			RLogical vec = (RLogical) obj;

			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('logical')", 1), 0);
			} else {
				resultId = e.rniPutBoolArray(vec.getValue());
			}

			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RInteger) {

			RInteger vec = (RInteger) obj;
			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('integer')", 1), 0);
			} else {
				resultId = e.rniPutIntArray(vec.getValue());
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RNumeric) {

			RNumeric vec = (RNumeric) obj;
			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('numeric')", 1), 0);
			} else {
				resultId = e.rniPutDoubleArray(vec.getValue());
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RComplex) {

			RComplex vec = (RComplex) obj;

			if (vec.getReal() != null && vec.getReal().length == 0 &&
                    vec.getImaginary() != null && vec.getImaginary().length == 0) {
				resultId = e.rniEval(e.rniParse("new('complex')", 1), 0);
			} else {
				String v_temp_1 = newTemporaryVariableName();
				String v_temp_2 = newTemporaryVariableName();
				e.rniAssign(v_temp_1, e.rniPutDoubleArray(vec.getReal()), 0);
				e.rniAssign(v_temp_2, e.rniPutDoubleArray(vec.getImaginary()), 0);
				resultId = e.rniEval(e.rniParse(v_temp_1 + "+1i*" + v_temp_2, 1), 0);
				e.rniEval(e.rniParse("rm(" + v_temp_1 + "," + v_temp_2 + ")", 1), 0);
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RChar) {

			RChar vec = (RChar) obj;
			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('character')", 1), 0);
			} else {
				resultId = e.rniPutStringArray(vec.getValue());
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RMatrix || obj instanceof RArray) {

			RArray rarray = (RArray) obj;
			resultId = putObject(rarray.getValue());
			e.rniSetAttr(resultId, "dim", e.rniPutIntArray(rarray.getDim()));
			if (rarray.getDimnames() != null) {
				e.rniSetAttr(resultId, "dimnames", putObject(rarray.getDimnames()));
			}

			if (rarray.getValue().getNames() != null) {
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(rarray.getValue().getNames()));
			}

			int[] vecIndexNa = null;
			if (rarray.getValue() instanceof RNumeric) {
				vecIndexNa = ((RNumeric) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RInteger) {
				vecIndexNa = ((RInteger) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RComplex) {
				vecIndexNa = ((RComplex) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RChar) {
				vecIndexNa = ((RChar) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RLogical) {
				vecIndexNa = ((RLogical) rarray.getValue()).getIndexNA();
			}

			if (vecIndexNa != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[rarray.getValue().length()];
				for (int i = 0; i < vecIndexNa.length; ++i)
					naBooleans[vecIndexNa[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RList) {

			RList rlist = (RList) obj;
			if (rlist.getValue() != null && rlist.getValue().length > 0) {
				long[] value_ids = new long[rlist.getValue().length];
				for (int i = 0; i < value_ids.length; ++i) {
					RObject v = (RObject) rlist.getValue()[i];
					if (v != null)
						value_ids[i] = putObject(v);
					else {
						value_ids[i] = e.rniEval(e.rniParse("NULL", 1), 0);
					}
				}
				resultId = e.rniPutVector(value_ids);
				if (rlist.getNames() != null) {
					e.rniSetAttr(resultId, "names", e.rniPutStringArray(rlist.getNames()));
				}
			} else {
				resultId = e.rniEval(e.rniParse("new(\"list\")", 1), 0);
				if (rlist.getNames() != null) {
					e.rniSetAttr(resultId, "names", e.rniPutStringArray(rlist.getNames()));
				}
			}

			if (obj instanceof RS3) {
				String[] classAttribute = ((RS3) obj).getClassAttribute();
				if (classAttribute != null) {
					e.rniSetAttr(resultId, "class", e.rniPutStringArray(classAttribute));
				}
			}

		} else if (obj instanceof RDataFrame) {

			RDataFrame dataframe = (RDataFrame) obj;
			resultId = putObject(dataframe.getData());
			e.rniSetAttr(resultId, "row.names", e.rniPutStringArray(dataframe.getRowNames()));
			e.rniSetAttr(resultId, "class", e.rniPutString("data.frame"));

		} else if (obj instanceof RFactor) {
			RFactor factor = (RFactor) obj;

			if (factor.getCode() == null || factor.getCode().length == 0) {
				resultId = e.rniEval(e.rniParse("new('integer')", 1), 0);
			} else {
				resultId = e.rniPutIntArray(factor.getCode());
			}

			e.rniSetAttr(resultId, "levels", e.rniPutStringArray(factor.getLevels()));
			e.rniSetAttr(resultId, "class", e.rniPutString("factor"));

		} else if (obj instanceof REnvironment) {

			REnvironment environment = (REnvironment) obj;

			resultId = e.rniEval(e.rniParse("new.env(parent=baseenv())", 1), 0);

			String resultTemp = newTemporaryVariableName();
			e.rniAssign(resultTemp, resultId, 0);
			for (Iterator<?> iter = environment.getData().keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				String temp = newTemporaryVariableName();
				RObject value = (RObject) environment.getData().get(key);
				e.rniAssign(temp, putObject(value), 0);
				e.rniEval(e.rniParse("assign(\"" + key + "\", " + temp + ", env=" + resultTemp + ")", 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + ")", 1), 0);
			}
			e.rniEval(e.rniParse("rm(" + resultTemp + ")", 1), 0);

        } else if (obj instanceof RRaw) {

            String temp = newTemporaryVariableName();
            String resulatVar = newTemporaryVariableName();
            e.rniAssign(temp, e.rniPutIntArray(((RRaw)obj).getValue()) , 0);
            if (obj instanceof RFunction || obj instanceof RUnknown) {
                e.rniEval(e.rniParse(resulatVar+"<-unserialize(as.raw("+temp+"))", 1), 0);
            }  else {
                e.rniEval(e.rniParse(resulatVar+"<-as.raw("+temp+")", 1), 0);
            }
            resultId = e.rniEval(e.rniParse(resulatVar, 1), 0);
            e.rniEval(e.rniParse("rm(" + temp + ","+ resulatVar + ")", 1), 0);

		} else {

			String rclass = DirectJNI._s4BeansMappingRevert.get(obj.getClass().getName());
			// log.info("**rclass:" + rclass);
//			if (rclass == null)
//				log.info(DirectJNI._s4BeansMappingRevert);

			if (rclass != null) {

				long slotsId = e.rniEval(e.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
				String[] slots = e.rniGetStringArray(e.rniGetAttr(slotsId, "names"));

				Field[] fields = obj.getClass().getDeclaredFields();
				String[] temps = new String[fields.length];

				String constructorArgs = "";
				for (int i = 0; i < fields.length; ++i) {

					String getterName = fields[i].getClass().equals(Boolean.class) ? "is"
                            + Utils.captalizeFirstChar(fields[i].getName()) : "get"
							+ Utils.captalizeFirstChar(fields[i].getName());
					Object fieldValue = obj.getClass().getMethod(getterName, (Class[]) null).invoke(obj, (Object[]) null);

					if (fieldValue instanceof RList && (((RList) fieldValue).getValue() == null || ((RList) fieldValue).getValue().length == 0)
							&& (((RList) fieldValue).getNames() == null || ((RList) fieldValue).getNames().length == 0)) {
						fieldValue = null;
					}

					if (fieldValue != null) {
						temps[i] = newTemporaryVariableName();
						e.rniAssign(temps[i], putObject((RObject) fieldValue), 0);
						if (!isNull(temps[i])) {
							if (!constructorArgs.equals("")) {
								constructorArgs += ",";
							}
							constructorArgs += slots[i] + "=" + temps[i];
						}
					} else {
						temps[i] = null;
					}
				}

				String var = newVariableName();
				if (constructorArgs.equals("")) {
					if (slots.length > 0) {
						e.rniEval(e.rniParse(var + "<-NULL", 1), 0);
					} else {
						e.rniEval(e.rniParse(var + "<-new(\"" + rclass + "\")", 1), 0);
					}
				} else {
					e.rniEval(e.rniParse(var + "<-new(\"" + rclass + "\", " + constructorArgs + " )", 1), 0);
				}

				resultId = e.rniEval(e.rniParse(var, 1), 0);
				e.rniEval(e.rniParse("rm(" + var + ")", 1), 0);

				for (int i = 0; i < temps.length; ++i) {
					if (temps[i] != null) {
						e.rniEval(e.rniParse("rm(" + temps[i] + ")", 1), 0);
					}
				}

			} else {
				try {
					Method getDataMethod = obj.getClass().getMethod("getData", (Class[]) null);
					if (getDataMethod != null) {
						resultId = putObject((RObject) getDataMethod.invoke(obj, (Object[]) null));
					}
				} catch (NoSuchMethodException ex) {
					throw new Exception("don't know how to deal with the object of type " + obj.getClass().getName());
				}
			}

		}

        if (((RObject) obj).getAttributes()!=null) {
            String[] aNames=((RObject) obj).getAttributes().getNames();
            for (int i=0; i<aNames.length; ++i) {
                e.rniSetAttr(resultId, aNames[i], putObject( ((RObject) obj).getAttributes().getValue()[i] ) );
            }
        }

        if (classHolder!=null) classHolder[0]=obj.getClass();
        return resultId;
	}

	private String expressionClass(String expression) {
		String cls = _rEngine.rniGetString(_rEngine.rniEval(_rEngine.rniParse("class(" + expression + ")", 1), 0));
		if (cls.equals("NULL"))
			throw new RuntimeException("NULL CLASS");
		return cls;
	}

	private boolean isS3Class(String expression) {
		boolean isObject = _rEngine.rniGetBoolArrayI(_rEngine.rniEval(_rEngine.rniParse("is.object(" + expression + ")", 1), 0))[0] == 1;
		if (!isObject)
			return false;
		boolean isClass = _rEngine.rniGetBoolArrayI(_rEngine.rniEval(_rEngine.rniParse("isClass(class(" + expression + "))", 1), 0))[0] == 1;
		return !isClass;
	}

	private boolean isNull(String expression) {
		boolean isNull = _rEngine.rniGetBoolArrayI(_rEngine.rniEval(_rEngine.rniParse("is.null(" + expression + ")", 1), 0))[0] == 1;
		return isNull;
	}

	// public for internal use only (DefaultAssignInterface Use)
	public RObject getObjectFrom(String expression, boolean setAttributes) throws Exception {
		if (isNull(expression))
			return null;
		return getObjectFrom(expression, expressionClass(expression), setAttributes);

	}

	// public for internal use only (RListener)
	public void putObjectAndAssignName(RObject obj, String name, boolean privateEnv) throws Exception {
		// log.info("putObjectAndAssignName called, obj:" + obj);
		long resultId = putObject(obj);
		// log.info("Result id=" + resultId);

		//_rEngine.rniAssign(name, resultId, (privateEnv ? _privateEnvExp : 0));
		_rEngine.rniAssign(name, resultId, 0);
	}

	// public for internal use only (RListener)
	public void evaluate(String expression, int n) throws Exception {
		_rEngine.rniEval(_rEngine.rniParse(expression, n), 0);
		//_rEngine.createRJavaRef();//rniEval(_rEngine.rniParse(expression, n), 0);
	}

	private RObject getObjectFrom(String expression, String rclass, boolean setAttributes) throws NoMappingAvailable, Exception {
		// log.info(".... quering for =" + expression + " rclass="+rclass);
		Rengine e = _rEngine;
		long expressionId = e.rniEval(e.rniParse(expression, 1), 0);
		RObject result = null;
		String typeStr = null;
		int rmode = e.rniExpType(expressionId);
		boolean isVirtual = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isVirtualClass(\"" + rclass + "\")", 1), 0))[0] == 1;
		boolean isClass = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isClass(\"" + rclass + "\")", 1), 0))[0] == 1;

        if (isClass && isVirtual) {

            String[] unionrclass = e.rniGetStringArray(e.rniEval(e.rniParse("class(" + expression + ")", 1), 0));
            log.info("Union Class : "+Arrays.toString(unionrclass));
            // log.info(">>> union r class=" + unionrclass );
            if (rclass.equals("formula")) {
                result = new RUnknown(e.rniGetIntArray(e.rniEval(e.rniParse("as.integer(serialize(" + expression + ",NULL))", 1), 0)));
            } else if (unionrclass.length==1) {

                RObject o = getObjectFrom(expression, unionrclass[0],true);
                if (rmode != S4SXP) {
                    if (DirectJNI._s4BeansMapping.get(unionrclass) != null) {

                        o = (RObject) DirectJNI._mappingClassLoader.loadClass(DirectJNI._s4BeansMapping.get(unionrclass)).getConstructor(
                                new Class[] { o.getClass() }).newInstance(new Object[] { o });
                    } else {
                    }
                }

                String factoryJavaClassName = DirectJNI._factoriesMapping.get(Utils.captalizeFirstChar(rclass) + "FactoryForR" + unionrclass);
                result = (RObject) DirectJNI._mappingClassLoader.loadClass(factoryJavaClassName).newInstance();
                Method setDataM = result.getClass().getMethod("setData", new Class[] { RObject.class });
                setDataM.invoke(result, o);

            } else {
                if (rmode==VECSXP) {
                    HashSet<String> unionClassSet=new HashSet<String>();for (int i=0; i<unionrclass.length;++i) unionClassSet.add(unionrclass[i]);
                    if (unionClassSet.contains("data.frame")) {
                        result = getObjectFrom(expression, "data.frame",true);
                    } else if (unionClassSet.contains("list")) {
                        result = getObjectFrom(expression, "list",true);
                    } else {
                        RList rlist=(RList)getObjectFrom(expression, "list",true);
                        result = new RS3(rlist.getValue(), rlist.getNames(), unionrclass);
                    }
                } else {
                    result = new RUnknown(e.rniGetIntArray(e.rniEval(e.rniParse("as.integer(serialize(" + expression + ",NULL))", 1), 0)));
                }
            }

		} else {

			RVector vector = null;

			switch (rmode) {

			case NILSXP:
				typeStr = "nil = NULL";
				result = null;
				break;

			case SYMSXP:
				typeStr = "symbols";
				break;

			case LISTSXP:
				typeStr = "lists of dotted pairs";
				break;

			case CLOSXP:
				typeStr = "closures";
				break;

			case ENVSXP:
				typeStr = "environments";
				String[] vars = e.rniGetStringArray(e.rniEval(e.rniParse("ls(" + expression + ")", 1), 0));
				HashMap<String, RObject> data = new HashMap<String, RObject>();

				for (int i = 0; i < vars.length; ++i) {
					String varname = expression + "$" + vars[i];
					String varclass = expressionClass(varname);
					data.put(vars[i], (RObject) getObjectFrom(varname, varclass,true));
				}

				result = new REnvironment();
				((REnvironment) result).setData(data);

				break;

			case PROMSXP:
				typeStr = "promises: [un]evaluated closure";
				break;

			case LANGSXP:
				typeStr = "language constructs (special lists)";
				break;

			case SPECIALSXP:
				typeStr = "special forms";
				break;

			case BUILTINSXP:
				typeStr = "builtin non-special forms";
				break;

			case CHARSXP:
				typeStr = "'scalar' string type (internal only)";
				break;

			case LGLSXP: {

				typeStr = "logical vectors";

				int[] bAsInt = e.rniGetBoolArrayI(expressionId);
				boolean[] b = new boolean[bAsInt.length];
				for (int i = 0; i < bAsInt.length; ++i)
					b[i] = bAsInt[i] == 1;

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RLogical(b, isNaIdx.length == 0 ? null : isNaIdx, names);
				if (rclass.equals("logical") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					if (rclass.equals("matrix")) {
						result = new RMatrix();
					} else if (rclass.equals("array")) {
						result = new RArray();
					}
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")", true));
				} else {
					result = vector;
				}

                 if (setAttributes && (e.rniGetBoolArrayI(e.rniEval(e.rniParse("!is.null(attributes(" + expression + "))", 1), 0))[0] == 1) ) {
                    result.setAttributes((RList)getObjectFrom("attributes("+expression+")", "list", false));
                }

				break;
			}

			case INTSXP: {
				typeStr = "integer vectors";

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RInteger(e.rniGetIntArray(expressionId), isNaIdx.length == 0 ? null : isNaIdx, names);
				if (rclass.equals("integer") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")", true));

				} else if (rclass.equals("factor")) {
					String[] levels = e.rniGetStringArray(e.rniGetAttr(expressionId, "levels"));
					result = new RFactor(levels, e.rniGetIntArray(expressionId));
				} else {
					result = vector;
				}

                if (setAttributes && (e.rniGetBoolArrayI(e.rniEval(e.rniParse("!is.null(attributes(" + expression + "))", 1), 0))[0] == 1) ) {
                    result.setAttributes((RList)getObjectFrom("attributes("+expression+")", "list", false));
                }

				break;
			}

			case REALSXP: {
				typeStr = "real variables";

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}
				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RNumeric(e.rniGetDoubleArray(expressionId), isNaIdx.length == 0 ? null : isNaIdx, names);

				if (rclass.equals("numeric") || rclass.equals("double") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")", true));
				} else {
					result = vector;
				}

                if (setAttributes && (e.rniGetBoolArrayI(e.rniEval(e.rniParse("!is.null(attributes(" + expression + "))", 1), 0))[0] == 1) ) {
                    result.setAttributes((RList)getObjectFrom("attributes("+expression+")", "list", false));
                }

				break;
			}

			case CPLXSXP: {
				typeStr = "complex variables";
				double[] c_real = e.rniGetDoubleArray(e.rniEval(e.rniParse("Re(" + expression + ")", 1), 0));
				double[] c_imaginary = e.rniGetDoubleArray(e.rniEval(e.rniParse("Im(" + expression + ")", 1), 0));

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RComplex(c_real, c_imaginary, isNaIdx.length == 0 ? null : isNaIdx, names);

				if (rclass.equals("complex") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")", true));
				} else {
					result = vector;
				}

                if (setAttributes && (e.rniGetBoolArrayI(e.rniEval(e.rniParse("!is.null(attributes(" + expression + "))", 1), 0))[0] == 1) ) {
                    result.setAttributes((RList)getObjectFrom("attributes("+expression+")", "list", false));
                }

				break;
			}

			case STRSXP: {
				typeStr = "string vectors";

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RChar(e.rniGetStringArray(expressionId), isNaIdx.length == 0 ? null : isNaIdx, names);
				if (rclass.equals("character") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {

					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")", true));

				} else {
					result = vector;
				}

                if (setAttributes && (e.rniGetBoolArrayI(e.rniEval(e.rniParse("!is.null(attributes(" + expression + "))", 1), 0))[0] == 1) ) {
                    result.setAttributes((RList)getObjectFrom("attributes("+expression+")", "list", false));
                }

				break;
			}

			case DOTSXP:
				typeStr = "dot-dot-dot object";
				break;

			case ANYSXP:
				typeStr = "make 'any' args work";
				break;

			case VECSXP: {
				typeStr = "generic vectors";

				String[] names = null;
				RObject[] objects = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				long[] objectIds = e.rniGetVector(expressionId);
				int[] types = new int[objectIds.length];
				for (int i = 0; i < objectIds.length; ++i) {
					types[i] = e.rniExpType(objectIds[i]);
				}

				if (objectIds.length > 0) {
					objects = new RObject[objectIds.length];
					for (int i = 0; i < objects.length; ++i) {
						String varname = expression + "[[" + (i + 1) + "]]";
						if (!isNull(varname)) {
							objects[i] = getObjectFrom(varname, expressionClass(varname), true);
						}
					}
				}

				RList rlist = new RList(objects, names);

				if (rclass.equals("list")) {
					result = rlist;
				} else if (rclass.equals("data.frame")) {

					// String[] rowNames =
					// e.rniGetStringArray(e.rniGetAttr(expressionId,
					// "row.names"));
					String[] rowNames = e.rniGetStringArray(e.rniEval(e.rniParse("row.names(" + expression + ")", 1), 0));

					result = new RDataFrame(rlist, rowNames);
				} else {
					boolean isObject = e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.object(" + expression + ")", 1), 0))[0] == 1;
					if (isObject && !isClass)
						result = new RS3(rlist.getValue(), rlist.getNames(), e.rniGetStringArray(e.rniEval(e.rniParse("class(" + expression + ")", 1), 0)));
				}

                if (setAttributes && (e.rniGetBoolArrayI(e.rniEval(e.rniParse("!is.null(attributes(" + expression + "))", 1), 0))[0] == 1) ) {
                    result.setAttributes((RList)getObjectFrom("attributes("+expression+")", "list", false));
                }

				break;
			}

			case EXPRSXP:
				typeStr = "expressions vectors";
				break;

			case BCODESXP:
				typeStr = "byte code";
				break;

			case EXTPTRSXP:
				typeStr = "external pointer";
				break;

			case WEAKREFSXP:
				typeStr = "weak reference";
				break;

			case RAWSXP:
				typeStr = "raw bytes";
                result = new RRaw(e.rniGetIntArray(e.rniEval(e.rniParse("as.integer(" + expression + ")", 1), 0)));
                break;
			case S4SXP:

				Class<?> s4Java_class = null;
				try {
					s4Java_class = DirectJNI._mappingClassLoader.loadClass(DirectJNI._s4BeansMapping.get(rclass));
				} catch (Exception ex) {
                    result = new RUnknown(e.rniGetIntArray(e.rniEval(e.rniParse("as.integer(serialize(" + expression + ",NULL))", 1), 0)));
                    break;
				}

				long slotsId = e.rniEval(e.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
				String[] slots = e.rniGetStringArray(e.rniGetAttr(slotsId, "names"));
				String[] slotsRClasses = e.rniGetStringArray(slotsId);
				Object[] params = new Object[slots.length];
				for (int i = 0; i < slots.length; ++i) {
					params[i] = getObjectFrom(expression + "@" + slots[i], slotsRClasses[i], true);
				}

				Constructor<?> constr = null;
				for (int i = 0; i < s4Java_class.getConstructors().length; ++i) {
					if (s4Java_class.getConstructors()[i].getParameterTypes().length > 0) {
						constr = s4Java_class.getConstructors()[i];
						break;
					}
				}

				result = (RObject) constr.newInstance(params);
				typeStr = "S4 object";

				break;

			case FUNSXP:
				typeStr = "Closure or Builtin";
				break;

			default:
				throw new Exception("type of <" + expression + "> not recognized");
			}
		}

		if (false)
			log.info("TYPE STR FOR<" + expression + ">:" + typeStr + " result:" + result + " type hint was : " + rclass);

		return result;
	}

	public static boolean hasDistributedReferences(ReferenceInterface arg) {
		try {
			for (int i = 0; i < arg.getClass().getSuperclass().getDeclaredFields().length; ++i) {
				Field f = arg.getClass().getSuperclass().getDeclaredFields()[i];
				try {
					f.setAccessible(true);
					ReferenceInterface fobj = (ReferenceInterface) f.get(arg);
					if (fobj == null)
						continue;
					if (fobj.getRObjectId() != arg.getRObjectId() || !fobj.getAssignInterface().equals(arg.getAssignInterface())) {
						return true;
					}
					if (hasDistributedReferences(fobj))
						return true;
				} finally {
					f.setAccessible(false);
				}
			}
			return false;
		} catch (Exception e) {
            log.error("Error!", e);
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

    private RObject call(boolean resultAsReference, String varName, RFunction method, Object... args) throws Exception {
        String functionName=newTemporaryVariableName();
        try {
            if (method instanceof ReferenceInterface) {
                ReferenceInterface mRef = (ReferenceInterface) method;
                if (mRef.getAssignInterface().equals(_ai)) {
                    _rEngine.rniAssign(functionName, mRef.getRObjectId(), 0);
                } else {
                    _rEngine.rniAssign(functionName, putObject(mRef.extractRObject()), 0);
                }
            } else {
                _rEngine.rniAssign(functionName, putObject(method), 0);
            }
            return call(resultAsReference, varName, functionName, args);
        } finally {
            _rEngine.rniEval(_rEngine.rniParse("rm("+functionName+")", 1), 0);
        }
    }

	private RObject call(boolean resultAsReference, String varName, String methodName, Object... args) throws Exception {

		Rengine e = _rEngine;

		Vector<String> usedVars = new Vector<String>();
		String callStr = methodName + "(";
		for (Object arg : args) {
			if (arg != null && arg instanceof RNamedArgument) {
				callStr += ((RNamedArgument) arg).getName() + "=";
				arg = ((RNamedArgument) arg).getRobject();
			}
			if (arg != null) {
				String argvar = newTemporaryVariableName();
				usedVars.add(argvar);

				if (arg instanceof ReferenceInterface) {

					ReferenceInterface argRef = (ReferenceInterface) arg;

					if (argRef.getAssignInterface().equals(_ai)) {
						e.rniAssign(argvar, argRef.getRObjectId(), 0);
						callStr += argvar + argRef.getSlotsPath();

						setFields(argvar + argRef.getSlotsPath(), argRef);

					} else {
						e.rniAssign(argvar, putObject(argRef.extractRObject()), 0);
						callStr += argvar;
					}

				} else {
					e.rniAssign(argvar, putObject(arg), 0);
					callStr += argvar;
				}
			} else {
				callStr += "NULL";
			}

			callStr += ",";
		}

		if (callStr.endsWith(","))
			callStr = callStr.substring(0, callStr.length() - 1);
		callStr += ")";

		log.info("call str >>>" + callStr);

		RObject result = null;
		if (varName == null) {
			result = evalAndGetObject(callStr, resultAsReference);
		} else {
			e.rniEval(e.rniParse(varName + "<-" + callStr, 1), 0);
			result = null;
		}

		String rmString = "rm(";
		for (int i = 0; i < usedVars.size(); ++i) {
			rmString += usedVars.elementAt(i) + (i == usedVars.size() - 1 ? "" : ",");
		}
		rmString += ")";
		e.rniEval(e.rniParse(rmString, 1), 0);

		return result;
	}

	public void protectSafe(long expReference) {
		_rEngine.rniAssign(PROTECT_VAR_PREFIXE + expReference, expReference, _privateEnvExp);
		_protectedExpReference.add(expReference);
	}

	public void unprotectSafe(long expReference) {
		_rEngine.rniEval(_rEngine.rniParse("rm(" + PROTECT_VAR_PREFIXE + expReference + ", envir=" + PENV + ")", 1), 0);
		_protectedExpReference.remove(expReference);
	}

	public void unprotectAll() {
		if (_protectedExpReference.size() > 0) {
			Vector<Long> _protectedExpReferenceClone = (Vector<Long>) _protectedExpReference.clone();
			for (int i = 0; i < _protectedExpReferenceClone.size(); ++i)
				unprotectSafe(_protectedExpReferenceClone.elementAt(i));
		}
	}

	private void setFields(String refLabel, ReferenceInterface obj) throws Exception {
		if (obj instanceof StandardReference)
			return;

		String javaBaseObjectName = obj.getClass().getName();
		javaBaseObjectName = javaBaseObjectName.substring(0, javaBaseObjectName.length() - "Ref".length());
		String rclass = DirectJNI._s4BeansMappingRevert.get(javaBaseObjectName);
		long slotsId = _rEngine.rniEval(_rEngine.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
		String[] slots = _rEngine.rniGetStringArray(_rEngine.rniGetAttr(slotsId, "names"));

		Field[] fields = obj.getClass().getSuperclass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			try {
				fields[i].setAccessible(true);

				ReferenceInterface fieldValue = (ReferenceInterface) fields[i].get(obj);

				if (fieldValue != null) {
					String temp = newTemporaryVariableName();

					RObject concreteFieldValue = null;

					if (fieldValue.getAssignInterface().equals(_ai)) {

						concreteFieldValue = getObjectFromReference(fieldValue);

					} else {

						concreteFieldValue = fieldValue.extractRObject();

					}

					_rEngine.rniAssign(temp, putObject(concreteFieldValue), 0);
					_rEngine.rniEval(_rEngine.rniParse(refLabel + "@" + slots[i] + "<-" + temp, 1), 0);
					_rEngine.rniEval(_rEngine.rniParse("rm(" + temp + ")", 1), 0);
				}
			} finally {
				fields[i].setAccessible(false);
			}
		}
	}

	public RObject getObjectFromReference(final ReferenceInterface refObj) throws Exception {
		// log.info( "-->getObjectFromReference :" + refObj);
		Rengine e = _rEngine;

		if (refObj.getAssignInterface().equals(_ai)) {
			String rootvar = newTemporaryVariableName();
			e.rniAssign(rootvar, refObj.getRObjectId(), 0);
			String refLabel = rootvar + refObj.getSlotsPath();
			setFields(refLabel, refObj);
			RObject result = getObjectFrom(refLabel, true);
			e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
			return result;
		} else {
			return refObj.extractRObject();
		}

	}

	public ReferenceInterface putObjectAndGetReference(final Object obj) throws Exception {
        Class<?>[] classHolder=new Class<?>[1];
        long resultId = putObject(obj, classHolder);
		protectSafe(resultId);

		Class<?> javaClass = DirectJNI._mappingClassLoader.loadClass(classHolder[0].getName() + "Ref");
		ReferenceInterface result = (ReferenceInterface)
                javaClass.getConstructor(new Class[] { long.class, String.class }).newInstance(
				new Object[] { resultId, "" });
		result.setAssignInterface(_ai);
		return result;
	}

	public String guessJavaClassRef(String rclass, int rtype, boolean isS3) {
		String javaClassName = null;

		if (DirectJNI._s4BeansMapping.get(rclass) != null) {
			javaClassName = DirectJNI._s4BeansMapping.get(rclass) + "Ref";
		} else {

			switch (rtype) {
			case ENVSXP:
				javaClassName = REnvironmentRef.class.getName();
				break;

            case CLOSXP:
                javaClassName = RFunctionRef.class.getName();
                break;

            case RAWSXP:
                javaClassName = RRawRef.class.getName();
                break;

			case LGLSXP:
				if (rclass.equals("logical") || rclass.equals("vector"))
					javaClassName = RLogicalRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RLogicalRef.class.getName();/* TODO */
				}
				break;

			case INTSXP:

				if (rclass.equals("integer") || rclass.equals("vector"))
					javaClassName = RIntegerRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else if (rclass.equals("factor"))
					javaClassName = RFactorRef.class.getName();
				else {
					javaClassName = RIntegerRef.class.getName();/* TODO */
				}
				break;

			case REALSXP:

				if (rclass.equals("numeric") || rclass.equals("double") || rclass.equals("vector"))
					javaClassName = RNumericRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RNumericRef.class.getName();/* TODO */
				}
				break;

			case CPLXSXP:

				if (rclass.equals("complex") || rclass.equals("vector"))
					javaClassName = RComplexRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RComplexRef.class.getName();/* TODO */
				}
				break;

			case STRSXP:

				if (rclass.equals("character") || rclass.equals("vector"))
					javaClassName = RCharRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RCharRef.class.getName(); /* TODO */
				}
				break;

			case VECSXP:

				if (rclass.equals("list"))
					javaClassName = RListRef.class.getName();
				else if (rclass.equals("data.frame"))
					javaClassName = RDataFrameRef.class.getName();
				else if (isS3) {
					javaClassName = RS3Ref.class.getName();
				} else {

					javaClassName = RListRef.class.getName();/* TODO */
				}

				break;

			}

		}

		return javaClassName;
	}

	private RObject evalAndGetObject(String expression, boolean resultAsReference) throws Exception {

		log.info("+ ------evalAndGetObject from :" + expression);

		try {
			Rengine e = _rEngine;

			long resultId = e.rniEval(e.rniParse(expression, 1), 0);
			int resultType = e.rniExpType(resultId);
			if (resultType == NILSXP)
				return null;

			String resultvar = newTemporaryVariableName();
			e.rniAssign(resultvar, resultId, 0);

			RObject result = null;
			if (!resultAsReference) {
				result = getObjectFrom(resultvar, true);
			} else {
				String rclass = expressionClass(resultvar);
				String javaClassName = guessJavaClassRef(rclass, resultType, isS3Class(expression));
				Class<?> javaClass = DirectJNI._mappingClassLoader.loadClass(javaClassName);
				protectSafe(resultId);
				result = (RObject) javaClass.getConstructor(new Class[] { long.class, String.class })
                        .newInstance(new Object[] { resultId, "" });
				((ReferenceInterface) result).setAssignInterface(_ai);
			}
			e.rniEval(e.rniParse("rm(" + resultvar + ")", 1), 0);
			return result;

		} catch (NoMappingAvailable re) {
			log.info("****" + Utils.getStackTraceAsString(re));
			throw re;
		} catch (Throwable ex) {
			log.info("#####" + Utils.getStackTraceAsString(ex));
			return null;
		}

	}

	private boolean isExportable(String symbol) {
		if (symbol.equals(ECHO_VAR_NAME) || symbol.equals(".required"))
			return false;
		Rengine e = _rEngine;
		long symbolId = e.rniEval(e.rniParse(symbol, 1), 0);
		if (symbolId <= 0)
			return false;
		int symbolType = e.rniExpType(symbolId);
		if (symbolType == NILSXP)
			return false;
		String rclass = expressionClass(symbol);
		return guessJavaClassRef(rclass, symbolType, isS3Class(symbol)) != null;
	}

	private String[] listExportableSymbols() {
		Rengine e = _rEngine;
		long resultId = e.rniEval(e.rniParse("ls()", 1), 0);
		String[] slist = _rEngine.rniGetStringArray(resultId);
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < slist.length; ++i) {
			if (isExportable(slist[i]) && !slist[i].equals(PENV))
				result.add(slist[i]);
		}
		return (String[]) result.toArray(new String[0]);
	}

	// public for internal use only
	public String sourceFromResource(String resource) {
		try {
			return sourceFromResource_(resource, _resourcesClassLoader);
		} catch (Exception e) {
            log.error("Error!", e);
			return null;
		}
	}

    private void sourceFromResourceDirect(final String resource) {

        long[] variablePointersBefore = getVariablePointersBefore();
        final Exception[] exceptionHolder = new Exception[1];
        runR(new ExecutionUnit() {
            public void run(Rengine e) {
                try {
                    sourceFromResourcDirect_(resource, _resourcesClassLoader);
					// broadcast(e);
				} catch (Exception ex) {
                    log.error("Error!", ex);
                }
            }
        });

        fireVariableChangedEvents(variablePointersBefore);
    }

	private String sourceFromResource_(String resource, ClassLoader refClassLoader) {

        log.info("sourcing " + resource);

		if (resource.startsWith("/"))
			resource = resource.substring(1);

		try {
            File tempFile = File.createTempFile("source", ".resource");

			BufferedReader breader = new BufferedReader(new InputStreamReader(refClassLoader.getResourceAsStream(resource)));
			PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
			String line;
			do {
				line = breader.readLine();
				if (line != null) {
					pwriter.println(line);
				}
			} while (line != null);
			pwriter.close();

			_rEngine.rniEval(_rEngine.rniParse("source(\"" + tempFile.getAbsolutePath().replace('\\', '/') + "\")", 1), 0);

			tempFile.delete();

		} catch (Exception e) {
            log.error("Error!", e);
		}

        return null;
	}

	private String sourceFromResourcDirect_(String resource, ClassLoader refClassLoader) {

        log.info("sourcing " + resource);

		if (resource.startsWith("/"))
			resource = resource.substring(1);

        //StringBuffer buffer = new StringBuffer();
        Vector<String> lines = new Vector<String>();

		try {
            //File tempFile = File.createTempFile("source", ".resource");

			BufferedReader breader = new BufferedReader(new InputStreamReader(refClassLoader.getResourceAsStream(resource)));
			//PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
			String line;
			do {
				line = breader.readLine();
				if (line != null) {
                    //buffer.append(line);
                    //buffer.append("\n");

                    lines.add(line);
					//pwriter.println(line);
				}
			} while (line != null);
			//pwriter.close();

            for(String line0 : lines) {
                //log.info("code\n"+buffer.toString());

                _rEngine.rniEval(_rEngine.rniParse(line0, 1), 0);

            }
			//_rEngine.rniEval(_rEngine.rniParse("source(\"" + tempFile.getAbsolutePath().replace('\\', '/') + "\")", 1), 0);

			//tempFile.delete();

		} catch (Exception e) {
            log.error("Error!", e);
		}

        return null;
	}

	public String sourceFromBuffer(String buffer) {
		try {
			File tempFile = PoolUtils.createFileFromBuffer(null, buffer);
			_rEngine.rniEval(_rEngine.rniParse("source(\"" + tempFile.getAbsolutePath().replace('\\', '/') + "\")", 1), 0);
			tempFile.delete();
		} catch (Exception e) {
            log.error("Error!", e);
		}

        return null;
	}

	void shutdownDevices(String deviceType) throws RemoteException {
		RInteger devices = (RInteger) getRServices().getObject(".PrivateEnv$dev.list()");
		if (devices != null) {
			for (int i = 0; i < devices.getValue().length; ++i) {
				if (devices.getNames()[i].equals(deviceType)) {
					getRServices().getObject(".PrivateEnv$dev.off(" + devices.getValue()[i] + ")");
				}
			}
		}
	}

	Integer getDevice(String deviceType) throws RemoteException {
		RInteger devices = (RInteger) getRServices().getObject(".PrivateEnv$dev.list()");
		for (int i = 0; i < devices.getValue().length; ++i) {
			if (devices.getNames()[i].equals(deviceType)) {
				return devices.getValue()[i];
			}
		}
		return null;
	}

	public String newTemporaryVariableName() {
		return V_TEMP_PREFIXE + _tempCounter++;
	}

	public String newVariableName() {
		return V_NAME_PREFIXE + _varCounter++;
	}

	//RNI _rni = new LocalRNI(this);

    public RNI getRNI() {
        return _rni;
    }

    RNI _rni = null;

	private AssignInterface _defaultAssignInterface = null; //new DefaultAssignInterfaceImpl();

	AssignInterface _ai = _defaultAssignInterface;

	public AssignInterface getAssignInterface() {
		return _ai;
	}

	public void setAssignInterface(AssignInterface ai) {
		_ai = ai;
	}

	public AssignInterface getDefaultAssignInterface() {
		return _defaultAssignInterface;
	}

	public RServices getRServices() {
		return _rServices;
	}

	public static void generateMaps(URL jarUrl) {
		DirectJNI.generateMaps(jarUrl, false);
	}

	public static Object getJavaArrayFromRArray(RArray array) {
		int[] dim = array.getDim();

		RVector vector = array.getValue();
		Class<?> componentType = null;
		if (vector instanceof RInteger)
			componentType = int.class;
		else if (vector instanceof RNumeric)
			componentType = double.class;
		else if (vector instanceof RChar)
			componentType = String.class;
		else if (vector instanceof RLogical)
			componentType = boolean.class;

		Object result = null;
		try {
			result = Array.newInstance(componentType, dim);
		} catch (Exception e) {
            log.error("Error!", e);
		}

		Vector<Integer> v1 = new Vector<Integer>();
		int p1 = 1;
		for (int i = dim.length - 1; i > 0; --i) {
			p1 = p1 * dim[i];
			v1.add(0, p1);
		}
		Vector<Integer> v2 = new Vector<Integer>();
		int p2 = 1;
		for (int i = 0; i < dim.length - 1; ++i) {
			p2 = p2 * dim[i];
			v2.add(0, p2);
		}

		for (int bi = 0; bi < p1 * dim[0]; ++bi) {
			int bindex = bi;
			int[] indexes = new int[dim.length];
			for (int i = 0; i < indexes.length - 1; ++i) {
				indexes[i] = bindex / v1.elementAt(i);
				bindex = bindex % v1.elementAt(i);
			}
			indexes[indexes.length - 1] = bindex;

			Object arrayTail = null;
			if (dim.length == 1) {
				arrayTail = result;
			} else {
				arrayTail = Array.get(result, indexes[0]);
				for (int i = 1; i < indexes.length - 1; ++i)
					arrayTail = Array.get(arrayTail, indexes[i]);
			}

			int linearVectorIndex = 0;
			for (int i = (indexes.length - 1); i > 0; --i)
				linearVectorIndex += indexes[i] * v2.elementAt((indexes.length - 1) - i);
			linearVectorIndex += indexes[0];
			// log.info("linearVectorIndex:"+linearVectorIndex);

			if (vector instanceof RInteger)
				Array.setInt(arrayTail, indexes[indexes.length - 1], ((RInteger) vector).getValue()[linearVectorIndex]);
			else if (vector instanceof RNumeric)
				Array.setDouble(arrayTail, indexes[indexes.length - 1], ((RNumeric) vector).getValue()[linearVectorIndex]);
			else if (vector instanceof RChar)
				Array.set(arrayTail, indexes[indexes.length - 1], ((RChar) vector).getValue()[linearVectorIndex]);
			else if (vector instanceof RLogical)
				Array.setBoolean(arrayTail, indexes[indexes.length - 1], ((RLogical) vector).getValue()[linearVectorIndex]);
		}

		return result;
	}

	public static int[] getJavaArrayDimensions(Object table, Class<?>[] classHolder, int[] lengthHolder) {
		Vector<Integer> dimV = new Vector<Integer>();
		Object obj = table;
		while (Array.get(obj, 0).getClass().isArray()) {
			dimV.add(Array.getLength(obj));
			obj = Array.get(obj, 0);
		}
		dimV.add(Array.getLength(obj));
		classHolder[0] = Array.get(obj, 0).getClass();

		int[] result = new int[dimV.size()];
		lengthHolder[0] = 1;
		for (int i = 0; i < dimV.size(); ++i) {
			result[i] = dimV.elementAt(i);
			lengthHolder[0] = lengthHolder[0] * result[i];
		}
		return result;
	}

	public static RArray getRArrayFromJavaArray(Object javaArray) {
		Class<?>[] classHolder = new Class<?>[1];
		int[] lengthHolder = new int[1];

		int[] dim = getJavaArrayDimensions(javaArray, classHolder, lengthHolder);
		RVector vector = null;

		Class<?> componentType = classHolder[0];
		if (componentType == Integer.class || componentType == int.class)
			vector = new RInteger(new int[lengthHolder[0]]);
		else if (componentType == Double.class || componentType == double.class)
			vector = new RNumeric(new double[lengthHolder[0]]);
		else if (componentType == Boolean.class || componentType == boolean.class)
			vector = new RLogical(new boolean[lengthHolder[0]]);
		else if (componentType == String.class)
			vector = new RChar(new String[lengthHolder[0]]);
		else
			throw new RuntimeException("unsupported elements class type :" + componentType);

		Vector<Integer> v1 = new Vector<Integer>();
		int p1 = 1;
		for (int i = dim.length - 1; i > 0; --i) {
			p1 = p1 * dim[i];
			v1.add(0, p1);
		}
		Vector<Integer> v2 = new Vector<Integer>();
		int p2 = 1;
		for (int i = 0; i < dim.length - 1; ++i) {
			p2 = p2 * dim[i];
			v2.add(0, p2);
		}

		for (int bi = 0; bi < p1 * dim[0]; ++bi) {
			int bindex = bi;
			int[] indexes = new int[dim.length];
			for (int i = 0; i < indexes.length - 1; ++i) {
				indexes[i] = bindex / v1.elementAt(i);
				bindex = bindex % v1.elementAt(i);
			}
			indexes[indexes.length - 1] = bindex;

			Object arrayTail = null;
			if (dim.length == 1) {
				arrayTail = javaArray;
			} else {
				arrayTail = Array.get(javaArray, indexes[0]);
				for (int i = 1; i < indexes.length - 1; ++i)
					arrayTail = Array.get(arrayTail, indexes[i]);
			}

			int linearVectorIndex = 0;
			for (int i = (indexes.length - 1); i > 0; --i)
				linearVectorIndex += indexes[i] * v2.elementAt((indexes.length - 1) - i);
			linearVectorIndex += indexes[0];
			// log.info("linearVectorIndex:"+linearVectorIndex);

			if (vector instanceof RInteger)
				((RInteger) vector).getValue()[linearVectorIndex] = (Integer) Array.get(arrayTail, indexes[indexes.length - 1]);
			else if (vector instanceof RNumeric)
				((RNumeric) vector).getValue()[linearVectorIndex] = (Double) Array.get(arrayTail, indexes[indexes.length - 1]);
			else if (vector instanceof RChar)
				((RChar) vector).getValue()[linearVectorIndex] = (String) Array.get(arrayTail, indexes[indexes.length - 1]);
			else if (vector instanceof RLogical)
				((RLogical) vector).getValue()[linearVectorIndex] = (Boolean) Array.get(arrayTail, indexes[indexes.length - 1]);
		}

		return new RArray(vector, dim, null);
	}

	private Object convert(RObject obj) {
		// log.info("obj:" + obj);
		Object result = obj;
		if (result instanceof RInteger) {
			if (((RInteger) result).getValue().length == 1) {
				result = ((RInteger) result).getValue()[0];
			} else {
				result = ((RInteger) result).getValue();
			}
		} else if (result instanceof RNumeric) {
			if (((RNumeric) result).getValue().length == 1) {
				result = ((RNumeric) result).getValue()[0];
			} else {
				result = ((RNumeric) result).getValue();
			}
		} else if (result instanceof RChar) {
			if (((RChar) result).getValue().length == 1) {
				result = ((RChar) result).getValue()[0];
			} else {
				result = ((RChar) result).getValue();
			}
		} else if (result instanceof RLogical) {
			if (((RLogical) result).getValue().length == 1) {
				result = ((RLogical) result).getValue()[0];
			} else {
				result = ((RLogical) result).getValue();
			}
		} else if (result instanceof RArray) {
			result = getJavaArrayFromRArray((RArray) result);
		}
		return result;
	}

	private String[] probedVariables = new String[0];

	private long[] getVariablePointersBefore() {
		if (probedVariables.length > 0) {
			try {
				final long[][] finalVariablePointers = new long[1][probedVariables.length];
				runR(new ExecutionUnit() {
					public void run(Rengine e) {
						for (int i = 0; i < probedVariables.length; ++i) {
							int[] exists = e.rniGetBoolArrayI(e.rniEval(e.rniParse("exists('" + probedVariables[i] + "')", 1), 0));
							if (exists[0] == 1) {
								finalVariablePointers[0][i] = e.rniEval(e.rniParse(probedVariables[i], 1), 0);
							}
						}
					}
				});
				return finalVariablePointers[0];
			} catch (Exception e) {
                log.error("Error!", e);
				return null;
			}
		} else {
			return null;
		}
	}

	private void fireVariableChangedEvents(long[] variablePointersBefore) {
		if (probedVariables.length > 0) {
			try {
				final long[][] variablePointersAfter = new long[1][probedVariables.length];
				runR(new ExecutionUnit() {
					public void run(Rengine e) {
						for (int i = 0; i < probedVariables.length; ++i) {
							int[] exists = e.rniGetBoolArrayI(e.rniEval(e.rniParse("exists('" + probedVariables[i] + "')", 1), 0));
							if (exists[0] == 1) {
								variablePointersAfter[0][i] = e.rniEval(e.rniParse(probedVariables[i], 1), 0);
							}
						}
					}
				});

				HashSet<String> changedVariablesHashSet = new HashSet<String>();
				for (int i = 0; i < probedVariables.length; ++i)
					if (variablePointersBefore[i] != variablePointersAfter[0][i])
						changedVariablesHashSet.add(probedVariables[i]);
				if (changedVariablesHashSet.size() > 0) {
					notifyRActionListeners(new RAction(RActionType.VARIABLES_CHANGE).
                            put(RActionConst.VARIABLES, changedVariablesHashSet));
				}
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}

    public void saveHistory(Vector<String> history, String filename, boolean append) {

        final File file = new File(filename);
        StringBuffer buffer = new StringBuffer();
        for (String line : history){
            buffer.append(line + "\n");
        }

        try {
            PrintWriter pwriter = new PrintWriter(new FileWriter(file, append));
            pwriter.print(buffer.toString());
            pwriter.close();

        } catch (Exception ex) {
            log.error("Error!", ex);
        } finally {
            if (!append) {
                _rCallbacks.getSaveHistoryQueue().offer(new Integer(0));
            }
        }
    }

    public Vector<String> loadHistory(String filename) {

        final File file = new File(filename);

        if (!file.exists()) return null;

        Vector<String> history = new Vector<String>();

        String line = null;
        try {
            BufferedReader breader  = new BufferedReader(new FileReader(filename));
            while((line = breader.readLine()) != null) {
                history.add(line);
            }

            breader.close();

            return history;
        } catch (Exception ex) {
            log.error("Error!", ex);
            return null;
        } finally {
            _rCallbacks.getLoadHistoryQueue().offer(new Integer(0));
        }
    }

    class StreamProcessor implements Runnable {
        private InputStream input;
        private HashMap<String, Object> attributes;
        private Vector<String> collector;
        private Boolean verbose;

        public StreamProcessor (InputStream input, Vector<String> collector,
                                Boolean verbose, HashMap<String, Object> attributes) {
            this.input = input;
            this.attributes = attributes;
            this.collector = collector;
            this.verbose = verbose;
        }

        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(input);
                BufferedReader br = new BufferedReader(reader);
                String line = null;
                while ((line = br.readLine()) != null) {
                    log.info("EXEC - " + line);

                    if (verbose) {
                        _rEngine.rniEval(_rEngine.rniParse("cat('" + line + "\n')", 1), 0);
                    }

                    if (collector != null) {
                        collector.add(line);
                    }

                    if (attributes != null) {
                        notifyRActionListeners(new RAction(RActionType.CONSOLE)
                                .put(RActionConst.OUTPUT, line + "\n")
                                .putAll(attributes));
                    }
                }

                try {
                    reader.close();
                } catch (Exception ex) {
                }

            } catch (Exception e) {
                log.error("Error!", e);
            }
        }
    }

    public String[] exec(String command) {
        return exec(command, false, false, null);
    }

    public String[] exec(String command, HashMap<String, Object> attributes) {
        return exec(command, false, false, attributes);
    }

    public String[] exec(String command, Boolean interpret, Boolean verbose) {
        return exec(command, interpret, verbose, null);
    }

    public String[] exec(String command, Boolean interpret, Boolean verbose,
                         HashMap<String, Object> attributes) {

        Vector<String> cmd = new Vector<String>();

        Vector<String> outcollector = interpret ? new Vector<String>() : null;

        cmd.add("sh");
        cmd.add("-c");
        cmd.add(command);

        StringBuilder sb = new StringBuilder();
        for(String str : cmd) {
            sb.append(str);
            sb.append(" ");
        }

        /*
        StringBuilder sb = new StringBuilder();
        for (String c : cmdArr) {
            sb.append(c + " ");
        }
        log.info("RListener-systemExec-command="+sb.toString());
        */

        try {
            log.info("EXEC - launching " + sb.toString());

            Process proc = Runtime.getRuntime().exec(cmd.toArray(new String[0]));

            Thread stderrThread = new Thread(new StreamProcessor(proc.getErrorStream(), outcollector, verbose, attributes));
            Thread stdoutThread = new Thread(new StreamProcessor(proc.getInputStream(), outcollector, verbose, attributes));

            log.info("EXEC - launching listeners");

            stdoutThread.start();
            stderrThread.start();

            log.info("EXEC - waiting for proc to complete");

            int rc = proc.waitFor();

            log.info("EXEC - waiting for STDOUT");

            try {
                stdoutThread.join(DEFAULT_SHELL_EXEC_STDOUT_TIMEOUT);
            } catch (InterruptedException ie) {
                try {
                    proc.getInputStream().close();
                } catch (Exception ex) {
                }
            }

            log.info("EXEC - waiting for STDERR");

            try {
                stderrThread.join(DEFAULT_SHELL_EXEC_STDOUT_TIMEOUT);
            } catch (InterruptedException ie) {
                try {
                    proc.getErrorStream().close();
                } catch (Exception ex) {
                }
            }

            log.info("EXEC - sending notifications");

            if (attributes != null) {
                notifyRActionListeners(new RAction(RActionType.PROMPT)
                        .put(RActionConst.PROMPT, "% ")
                        .putAll(attributes));
            }

            log.info("EXEC - returning");

            if (interpret) {
                return outcollector.toArray(new String[0]);

            } else {
                return new String[] { Integer.toString(rc) };
            }

        }
        catch (InterruptedException ie) {
            log.error("Error!", ie);
        }
        catch (IOException ioe) {
            log.error("Error!", ioe);
        }

        return new String[] { "-1" };
    }

    public String cutRHomeFromPath(String path) throws RemoteException {
        String result = path;
        String rhomepath = System.getenv().get("R_HOME");
        if (path.startsWith(rhomepath)) {
            result = path.substring(rhomepath.length());
        }

        return result;
    }


    private Vector<SearchResult> searchInFile(File file, SearchRequest request) {

        String pattern = request.getPattern();
        boolean wholeword = request.isWholewords(); // unused for now
        boolean casesensitive = request.isCasesensitive();

        Vector<SearchResult> result = new Vector<SearchResult>();

        if (!casesensitive) {
            pattern = pattern.toLowerCase();
        }

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                int linenum = 0;

                while ((line = input.readLine()) != null) {

                    String linemod = line;

                    if (!casesensitive) {
                        linemod = linemod.toLowerCase();
                    }

                    if (linemod.indexOf(pattern, 0) != -1) {
                        result.add(new SearchResult(new FileNode(file), line, linenum));
                    }

                    linenum++;
                }

            } finally {
                try {
                    input.close();
                } catch (IOException ioe) {
                }
            }

        } catch (FileNotFoundException fnfe) {
            log.error("Error!", fnfe);
        } catch (IOException ioe) {
            log.error("Error!", ioe);
        }

        return result;
    }

    static class MyFileFilter implements FileFilter {
        private boolean usemask;
        private String mask;

        public MyFileFilter(boolean usemask, String mask){
            this.usemask = usemask;
            this.mask = mask.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\.*");
            log.info("fixed mask="+this.mask);
        }
        
        public boolean accept(File file) {
            if (!file.isDirectory() && usemask) {
                if (file.getName().matches(mask)) {
                    //log.info("MATCH !!! : file="+file.getName()+" mask="+mask);
                    return true;
                } else {
                    //log.info("NO MATCH : file="+file.getName()+" mask="+mask);
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    private Vector<SearchResult> recursiveSearch(File root, SearchRequest request, MyFileFilter filter) {

        Vector<SearchResult> result = new Vector<SearchResult>();

        if (root.isDirectory()) {

            File[] filelist = root.listFiles(filter);

            if (filelist != null && filelist.length > 0) {
                for (File file0 : filelist) {
                    if (file0.isDirectory()) {
                        if (request.isRecursive()) {
                            result.addAll(recursiveSearch(file0, request, filter));
                        }
                    } else {
                        result.addAll(searchInFile(file0, request));
                    }
                }
            }
        } else {
            result.addAll(searchInFile(root, request));
        }

        return result;
    }

    public void searchAsync(SearchRequest request) throws RemoteException {
        searchAsync(request, null);
    }

    public void searchAsync(final SearchRequest request, final HashMap<String, Object> attributes) throws RemoteException {
        new Thread(new Runnable(){
            public void run(){
                try {
                    log.info("request="+request.getPath()+" pattern="+request.getPattern()+
                            " mask"+request.getMask()+" use mask="+request.isUsemask());

                    Vector<SearchResult> searchresult = search(request, attributes);

                    notifyRActionListeners(new RAction(RActionType.SEARCH)
                            .put(RActionConst.RESULT, new SearchResultContainer(request, searchresult))
                            .putAll(attributes));

                } catch (RemoteException re) {
                    log.error("Error!", re);
                }
            }
        }).start();
    }

    public Vector<SearchResult> search(SearchRequest request) throws RemoteException {
        return search(request, null);
    }

    public Vector<SearchResult> search(SearchRequest request, HashMap<String, Object> attributes) throws RemoteException {

        Vector<SearchResult> result = new Vector<SearchResult>();

        File searchroot = new File(request.getPath());

        if (searchroot.exists()) {
            result = recursiveSearch(searchroot, request,
                    new MyFileFilter(request.isUsemask(), request.getMask()));
        }

        return result;
    }

	private RServices _rServices = new RServices() {

		private String _lastStatus = null;

        public String getStatus() {
            return clean(_lastStatus);
        }

        public void shutdown() throws RemoteException {
            DirectJNI.getInstance().shutdown();
        }

        public void saveAndShutdown(String path) throws RemoteException {
            DirectJNI.getInstance().saveAndShutdown(path);
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
            DirectJNI.getInstance().searchAsync(request, null);
        }

        public void searchAsync(final SearchRequest request, final HashMap<String, Object> attributes) throws RemoteException {
            DirectJNI.getInstance().searchAsync(request, attributes);
        }

        public void saveHistory(Vector<String> history, String filename, boolean create) throws RemoteException {
        }

        public Vector<String> loadHistory(String filename) throws RemoteException {
            return null;
        }

		public String evaluate(final String expression, final int n) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						e.rniEval(e.rniParse(expression, n), 0);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(_lastStatus);
		}

        public String sourceFromResource(String resource) throws RemoteException {
            return sourceFromResource(resource, null);
        }

        public String sourceFromBuffer(String buffer) throws RemoteException {
            return sourceFromBuffer(buffer, null);
        }

		public String sourceFromResource(final String resource, HashMap<String, Object> attributes) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit(attributes) {
				public void run(Rengine e) {
					try {
						DirectJNI.this.sourceFromResource(resource);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(_lastStatus);
		}

		public String sourceFromBuffer(final String buffer, HashMap<String, Object> attributes) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit(attributes) {
				public void run(Rengine e) {
					try {
						DirectJNI.this.sourceFromBuffer(buffer);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(_lastStatus);
		}

		public String evaluate(String expression) throws RemoteException {
			return evaluate(expression, 1);
		}

		public RObject call(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(false, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public void callAndAssign(final String varName, final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						DirectJNI.this.call(false, varName, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

		}

		public RObject callAndGetReference(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(true, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public RObject callAndGetObjectName(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(true, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			try {
				String refClassName = objHolder[0].getClass().getName();
				ObjectNameInterface objectName = (ObjectNameInterface) _mappingClassLoader.loadClass(
						refClassName.substring(0, refClassName.length() - "Ref".length()) + "ObjectName").newInstance();
				objectName.setRObjectName(PROTECT_VAR_PREFIXE + ((ReferenceInterface) objHolder[0]).getRObjectId());
				objectName.setRObjectEnvironment(PENV);
				return (RObject) objectName;
			} catch (Exception e) {
                log.error("Error!", e);
				throw new RemoteException("", e);
			}
		}

		public Object callAndConvert(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(false, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			return DirectJNI.this.convert(objHolder[0]);
		}

        public RObject call(final RFunction method, final Object... args) throws RemoteException {

            long[] variablePointersBefore = getVariablePointersBefore();

            final RObject[] objHolder = new RObject[1];
            final Exception[] exceptionHolder = new Exception[1];
            _lastStatus = runR(new ExecutionUnit() {
                public void run(Rengine e) {
                    try {
                        objHolder[0] = DirectJNI.this.call(false, null, method, args);
                        // broadcast(e);
                    } catch (Exception ex) {
                        exceptionHolder[0] = ex;
                    }
                }
            });

            fireVariableChangedEvents(variablePointersBefore);

            if (exceptionHolder[0] != null) {
                log.error(_lastStatus);
                if (exceptionHolder[0] instanceof RemoteException) {
                    throw (RemoteException) exceptionHolder[0];
                } else {
                    throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
                }
            } else if (!_lastStatus.equals("")) {
                log.info(_lastStatus);
            }
            return objHolder[0];
        }

        public void callAndAssign(final String varName, final RFunction method, final Object... args) throws RemoteException {

            long[] variablePointersBefore = getVariablePointersBefore();

            final Exception[] exceptionHolder = new Exception[1];
            _lastStatus = runR(new ExecutionUnit() {
                public void run(Rengine e) {
                    try {
                        DirectJNI.this.call(false, varName, method, args);
                        // broadcast(e);
                    } catch (Exception ex) {
                        exceptionHolder[0] = ex;
                    }
                }
            });

            fireVariableChangedEvents(variablePointersBefore);

            if (exceptionHolder[0] != null) {
                log.error(_lastStatus);
                if (exceptionHolder[0] instanceof RemoteException) {
                    throw (RemoteException) exceptionHolder[0];
                } else {
                    throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
                }
            } else if (!_lastStatus.equals("")) {
                log.info(_lastStatus);
            }

        }

        public RObject callAndGetReference(final RFunction method, final Object... args) throws RemoteException {

            long[] variablePointersBefore = getVariablePointersBefore();

            final RObject[] objHolder = new RObject[1];
            final Exception[] exceptionHolder = new Exception[1];
            _lastStatus = runR(new ExecutionUnit() {
                public void run(Rengine e) {
                    try {
                        objHolder[0] = DirectJNI.this.call(true, null, method, args);
                        // broadcast(e);
                    } catch (Exception ex) {
                        exceptionHolder[0] = ex;
                    }
                }
            });

            fireVariableChangedEvents(variablePointersBefore);

            if (exceptionHolder[0] != null) {
                log.error(_lastStatus);
                if (exceptionHolder[0] instanceof RemoteException) {
                    throw (RemoteException) exceptionHolder[0];
                } else {
                    throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
                }
            } else if (!_lastStatus.equals("")) {
                log.info(_lastStatus);
            }
            return objHolder[0];
        }

        public RObject callAndGetObjectName(final RFunction method, final Object... args) throws RemoteException {

            long[] variablePointersBefore = getVariablePointersBefore();

            final RObject[] objHolder = new RObject[1];
            final Exception[] exceptionHolder = new Exception[1];
            _lastStatus = runR(new ExecutionUnit() {
                public void run(Rengine e) {
                    try {
                        objHolder[0] = DirectJNI.this.call(true, null, method, args);
                        // broadcast(e);
                    } catch (Exception ex) {
                        exceptionHolder[0] = ex;
                    }
                }
            });

            fireVariableChangedEvents(variablePointersBefore);

            if (exceptionHolder[0] != null) {
                log.error(_lastStatus);
                if (exceptionHolder[0] instanceof RemoteException) {
                    throw (RemoteException) exceptionHolder[0];
                } else {
                    throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
                }
            } else if (!_lastStatus.equals("")) {
                log.info(_lastStatus);
            }

            try {
                String refClassName = objHolder[0].getClass().getName();
                ObjectNameInterface objectName = (ObjectNameInterface) _mappingClassLoader.loadClass(
                        refClassName.substring(0, refClassName.length() - "Ref".length()) + "ObjectName").newInstance();
                objectName.setRObjectName(PROTECT_VAR_PREFIXE + ((ReferenceInterface) objHolder[0]).getRObjectId());
                objectName.setRObjectEnvironment(PENV);
                return (RObject) objectName;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RemoteException("", e);
            }
        }

        public Object callAndConvert(final RFunction method, final Object... args) throws RemoteException {

            long[] variablePointersBefore = getVariablePointersBefore();

            final RObject[] objHolder = new RObject[1];
            final Exception[] exceptionHolder = new Exception[1];
            _lastStatus = runR(new ExecutionUnit() {
                public void run(Rengine e) {
                    try {
                        objHolder[0] = DirectJNI.this.call(false, null, method, args);
                        // broadcast(e);
                    } catch (Exception ex) {
                        exceptionHolder[0] = ex;
                    }
                }
            });

            fireVariableChangedEvents(variablePointersBefore);

            if (exceptionHolder[0] != null) {
                log.error(_lastStatus);
                if (exceptionHolder[0] instanceof RemoteException) {
                    throw (RemoteException) exceptionHolder[0];
                } else {
                    throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
                }
            } else if (!_lastStatus.equals("")) {
                log.info(_lastStatus);
            }

            return DirectJNI.this.convert(objHolder[0]);
        }


		public void freeReference(final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an object reference");
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						unprotectSafe(((ReferenceInterface) refObj).getRObjectId());
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
		}

		public void freeAllReferences() throws RemoteException {
			DirectJNI.this.unprotectAll();
		}

		public RObject getObjectName(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, true);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			try {
				String refClassName = objHolder[0].getClass().getName();
				ObjectNameInterface objectName = (ObjectNameInterface) _mappingClassLoader.loadClass(
						refClassName.substring(0, refClassName.length() - "Ref".length()) + "ObjectName").newInstance();
				objectName.setRObjectName(PROTECT_VAR_PREFIXE + ((ReferenceInterface) objHolder[0]).getRObjectId());
				objectName.setRObjectEnvironment(PENV);
				return (RObject) objectName;
			} catch (Exception e) {
                log.error("Error!", e);
				throw new RemoteException("", e);
			}

		}

		public Object realizeObjectName(final RObject objectName, boolean convert) throws RemoteException {
			if (!(objectName instanceof ObjectNameInterface))
				throw new RemoteException("not an object name");
			final RObject[] robjHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						robjHolder[0] = DirectJNI.this.getObjectFrom(((ObjectNameInterface) objectName).getRObjectEnvironment() + "$"
								+ ((ObjectNameInterface) objectName).getRObjectName(), true);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			if (convert) {
				return DirectJNI.this.convert(robjHolder[0]);
			} else {
				return robjHolder[0];
			}
		}

		public RObject realizeObjectName(final RObject objectName) throws RemoteException {
			return (RObject) realizeObjectName(objectName, false);
		}

		public Object realizeObjectNameConverted(RObject objectName) throws RemoteException {
			return realizeObjectName(objectName, true);
		}

		public RObject referenceToObject(final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an object reference");
			final RObject[] robjHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						robjHolder[0] = DirectJNI.this.getObjectFromReference((ReferenceInterface) refObj);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return robjHolder[0];
		};

		public boolean isReference(RObject obj) {
			return obj instanceof ReferenceInterface;
		}

		public RObject putAndGetReference(final Object obj) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] refHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						refHolder[0] = (RObject) DirectJNI.this.putObjectAndGetReference(obj);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return refHolder[0];
		}

		public void putAndAssign(final Object obj, final String name) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						long resultId = putObject(obj);
						e.rniAssign(name, resultId, 0);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

		}

		public RObject getObject(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, false);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);

				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}

			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public RObject getReference(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, true);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public Object getObjectConverted(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, false);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);

				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}

			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			return DirectJNI.this.convert(objHolder[0]);
		}

		public Object convert(RObject obj) throws RemoteException {
			return DirectJNI.this.convert(obj);
		}

		public void assignReference(final String name, final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an an object reference");

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						String rootvar = newTemporaryVariableName();
						e.rniAssign(rootvar, ((ReferenceInterface) refObj).getRObjectId(), 0);
						e.rniEval(e.rniParse(name + "<-" + rootvar + ((ReferenceInterface) refObj).getSlotsPath(), 1), 0);
						e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
		}

		public boolean symbolExists(final String symbol) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];
			final Boolean[] objHolder = new Boolean[1];

			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						long id = e.rniEval(e.rniParse(symbol, 1), 0);
						objHolder[0] = id > 0;
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			return objHolder[0];
		}

		public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
            log.info("addRCollaborationListener");
			_rCollaborationListeners.add(collaborationListener);
		}

		public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
			_rCollaborationListeners.remove(collaborationListener);
		}

		public void removeAllRCollaborationListeners() throws RemoteException {
			_rCollaborationListeners.removeAllElements();
		}

		public boolean hasRCollaborationListeners() throws RemoteException {
			return _rCollaborationListeners.size() > 0;
		}

		public void addRConsoleActionListener(RActionListener helpListener) throws RemoteException {
			DirectJNI.this.addRActionListener(helpListener);
		}

		public void removeRConsoleActionListener(RActionListener helpListener) throws RemoteException {
			DirectJNI.this.removeRActionListener(helpListener);
		}

		public void removeAllRConsoleActionListeners() throws RemoteException {
			DirectJNI.this.removeAllRActionListeners();
		}

		public void registerUser(String sourceUID, String user) throws RemoteException {
			_usersHash.put(sourceUID, new UserStatus(sourceUID, user, false));
			notifyRActionListeners(new RAction(RActionType.UPDATE_USERS));
		}

		public void unregisterUser(String sourceUID) throws RemoteException {
			_usersHash.remove(sourceUID);
			notifyRActionListeners(new RAction(RActionType.UPDATE_USERS));
		}

		public void updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException {
			_usersHash.put(sourceUID, userStatus);
			notifyRActionListeners(new RAction(RActionType.UPDATE_USERS));
		}

		public UserStatus[] getUserStatusTable() throws RemoteException {
			UserStatus[] result = new UserStatus[_usersHash.values().size()];
			int i = 0;
			for (UserStatus us : _usersHash.values())
				result[i++] = us;
			return result;
		}

		public void setUserInput(String userInput) throws RemoteException {
			_userInput = userInput;
		}

		public void chat(String sourceUID, String user, String message) throws RemoteException {
			Vector<RCollaborationListener> removeList = new Vector<RCollaborationListener>();
			for (int i = 0; i < _rCollaborationListeners.size(); ++i) {
				try {
					_rCollaborationListeners.elementAt(i).chat(sourceUID, user, message);
				} catch (Exception e) {
					removeList.add(_rCollaborationListeners.elementAt(i));
				}
			}
			_rCollaborationListeners.removeAll(removeList);
		}

		public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
			Vector<RCollaborationListener> removeList = new Vector<RCollaborationListener>();
			for (int i = 0; i < _rCollaborationListeners.size(); ++i) {
				try {
					_rCollaborationListeners.elementAt(i).consolePrint(sourceUID, user, expression, result);
				} catch (Exception e) {
					removeList.add(_rCollaborationListeners.elementAt(i));
				}
			}
			_rCollaborationListeners.removeAll(removeList);
		}

		public GenericCallbackDevice newGenericCallbackDevice() throws RemoteException {
			return null;
		}

		public GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException {
			return null;
		}

		public String[] listPackages() throws RemoteException {
			return ((String[]) DirectJNI._packageNames.toArray(new String[0]));
		}

		public RPackage getPackage(String packageName) throws RemoteException {
			if (!DirectJNI._packageNames.contains(packageName))
				throw new RemoteException("Bad Package Name");
			if (_packs.get(packageName) == null) {
				try {
					for (Iterator<?> iter = DirectJNI._rPackageInterfacesHash.keySet().iterator(); iter.hasNext();) {
						String className = (String) iter.next();
						if (className.substring(className.lastIndexOf('.') + 1).equals(packageName)) {
							_packs.put(packageName, (RPackage)
                                    DirectJNI._mappingClassLoader.loadClass(className + "Impl").getMethod("getInstance",
									(Class[]) null).invoke((Object) null, (Object[]) null));
							break;
						}
					}
				} catch (Exception ex) {
                    log.error("Error!", ex);
				}
			}
			return _packs.get(packageName);
		}

		public String unsafeGetObjectAsString(String cmd) throws RemoteException {
			if (cmd.trim().equals(""))
				return "";
			try {
				Object result = DirectJNI.this.evalAndGetObject(cmd, false);
				if (result == null)
					return "";
				else {
					return result.toString();
				}
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public void stop() throws RemoteException {

            log.info("DirectJNI-stop");

			if (PoolUtils.isWindowsOs()) {
                log.info("stop-_rEngine.rniStop(0)");
				_rEngine.rniStop(0);
			} else {
				//_rEngine.rniEval(_rEngine.rniParse("stop('stop required')", 1), 0);
                log.info("stop-_rEngine.rniStop(0)");

                _rEngine.rniStop(0);
			}
			_stopRequired = true;
		}

		public RNI getRNI() throws RemoteException {
			return _rni;
		}

		public void die() throws RemoteException {

		}

		public String getLogs() throws RemoteException {

			return null;
		}

		public String getServantName() throws RemoteException {
			return null;
		}


		public String getJobId() throws RemoteException {
			return null;
		}


		public void setJobId(String jobId) throws RemoteException {
		}

        public void setOwner(String owner) throws java.rmi.RemoteException{
        }

        public String getOwner() throws java.rmi.RemoteException{
            return null;
        }

        public void setProject(String project) throws java.rmi.RemoteException{
        }

        public String getProject() throws java.rmi.RemoteException{
            return null;
        }

        public void setMaster(String master) throws java.rmi.RemoteException {
        }

        public String getMaster() throws java.rmi.RemoteException {
            return null;
        }


		public String ping() throws RemoteException {

			return null;
		}

		public void reset() throws RemoteException {

		}

		public void addLogListener(RemoteLogListener listener) throws RemoteException {

		}

		public void removeLogListener(RemoteLogListener listener) throws RemoteException {

		}

		public void removeAllLogListeners() throws RemoteException {

		}

		public void logInfo(String message) throws RemoteException {

		}

		public boolean hasConsoleMode() throws RemoteException {
			return true;
		}

        /*
        class ConsoleExecutionUnit extends ExecutionUnit {
            private String command;

            public ConsoleExecutionUnit(String command){
                this.command = command;
            }

            public void run(Rengine e) {
            }

            public String getConsoleInput() {
                if (command.startsWith("?")) {
                    return "help(" + command.substring(1) + ")";
                } else {
                    return command;
                }
            }
        }
        */

        public String consoleSubmit(String cmd) throws RemoteException {
            return consoleSubmit(cmd, null);
        }

		public String consoleSubmit(String cmd, HashMap<String, Object> attributes) throws RemoteException {
            return runR(new CommandUnit(cmd, attributes));
		}

		public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
            asynchronousConsoleSubmit(cmd, null);
		}

        public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> attributes) throws java.rmi.RemoteException {
            try {
                executionQueue.put(new CommandUnit(cmd, attributes));
            } catch (InterruptedException ie) {
                log.info("asynchronousConsoleSubmit-InterruptedException");
            }
        }

		public boolean isBusy() throws RemoteException {
			return _mainLock.isLocked();
		}

		public boolean isResetEnabled() throws RemoteException {
			return false;
		}

		public void setResetEnabled(boolean enable) throws RemoteException {
		}

		public boolean hasPushPopMode() throws RemoteException {
			return true;
		}

		public void push(String symbol, Serializable object) throws RemoteException {
			DirectJNI.getInstance().getRServices().putAndAssign((RObject) object, symbol);
		}

		public Serializable pop(String symbol) throws RemoteException {
			Serializable result = DirectJNI.getInstance().getRServices().getObject(symbol);
			return result;
		}

		public String[] listSymbols() throws RemoteException {

			final String[][] objHolder = new String[1][];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.listExportableSymbols();
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public boolean hasGraphicMode() throws RemoteException {
			return true;
		}

		public RemotePanel getPanel(int w, int h) throws RemoteException {
			return new RGraphicsPanelRemote(w, h, gn);
		}

		public GDDevice newDevice(int w, int h) throws RemoteException {
			return new GDDeviceLocal(w, h, _rCallbacks);
		}

		public GDDevice newBroadcastedDevice(int w, int h) throws RemoteException {
			return new GDDeviceLocal(w, h, true, _rCallbacks);
		}

		public GDDevice[] listDevices() throws RemoteException {
			GDDevice[] result = new GDDevice[_localDeviceHashMap.values().size()];
			int i = 0;
			for (GDDevice d : _localDeviceHashMap.values())
				result[i++] = d;
			return result;
		}

        // random access
        public void createRandomAccessFile(String fileName) throws java.rmi.RemoteException {
            try {
                File f = new File(fileName);

                String name = f.getAbsolutePath().replace('\\', '/');
                new File(name.substring(0, name.lastIndexOf('/'))).mkdirs();

                RandomAccessFile raf = new RandomAccessFile(f, "rw");
                raf.setLength(0);
                raf.close();
            } catch (Exception e) {
                throw new RemoteException(PoolUtils.getStackTraceAsString(e));
            }
        }


        public void createRandomAccessDir(String dirName) throws java.rmi.RemoteException {
            File dir = new File(dirName);
            dir.mkdirs();
        }

        public void renameRandomAccessFile(String fileName1, String fileName2) throws java.rmi.RemoteException {
            File file1 = new File(fileName1);
            File file2 = new File(fileName2);
            file1.renameTo(file2);
        }

        public void removeRandomAccessFile(String fileName) throws java.rmi.RemoteException {
            try {
                File f = new File(fileName);
                if (f.isDirectory()) {
                    PoolUtils.deleteDirectory(f);
                } else {
                    f.delete();
                }
            } catch (Exception e) {
                throw new RemoteException(PoolUtils.getStackTraceAsString(e));
            }
        }

        public FileDescription getRandomAccessFileDescription(String fileName) throws java.rmi.RemoteException {
            File f = new File(fileName);
            //return new FileDescription(fileName, f.length(), f.isDirectory(), new Date(f.lastModified()));
            return new FileDescription(f);
        }


        public byte[] readRandomAccessFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException {
            try {
                RandomAccessFile raf = new RandomAccessFile(new File(fileName), "r");
                raf.seek(offset);
                byte[] result = new byte[blocksize];
                int n = raf.read(result);
                raf.close();

                if (n < blocksize) {
                    byte[] temp = new byte[n];
                    System.arraycopy(result, 0, temp, 0, n);
                    result = temp;
                }
                return result;
            } catch (Exception e) {
                throw new RemoteException("Exception Holder", e);
            }
        }

        public void appendBlockToRandomAccessFile(String fileName, byte[] block) throws java.rmi.RemoteException {
            try {
                RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
                raf.seek(raf.length());
                raf.write(block);
                raf.close();
            } catch (Exception e) {
                throw new RemoteException("Exception Holder", e);
            }
        }

        private void copyFile(File scrFile, File dstFile) throws IOException {

            InputStream in = new FileInputStream(scrFile);
            OutputStream out = new FileOutputStream(dstFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

        private void recursiveCopyDir(File scrPath, File dstPath) throws IOException {

            log.info("scrPath="+scrPath+" dstPath="+dstPath);
            
            if (scrPath.isDirectory()) {
                dstPath.mkdirs();

                String children[] = scrPath.list();
                for (String file : children) {
                    recursiveCopyDir(new File(scrPath, file),
                            new File(dstPath, file));
                }
            } else {
                copyFile(scrPath, dstPath);
            }
        }

        private void moveFile(File scrFile, File dstFile) throws IOException {
            copyFile(scrFile, dstFile);
            scrFile.delete();
        }

        private void recursiveMoveDir(File scrPath, File dstPath) throws IOException {
            if (scrPath.isDirectory()) {
                dstPath.mkdirs();

                String children[] = scrPath.list();
                for (String file : children) {
                    recursiveMoveDir(new File(scrPath, file),
                            new File(dstPath, file));
                }
                scrPath.delete();
            } else {
                moveFile(scrPath, dstPath);
            }
        }


        private void createLink(String scrFileName, String dstFileName) throws IOException {
            /*
            Path newLink;
            Path target;

            try {
                newLink.createSymbolicLink(target);
            } catch (IOException x) {
                System.err.println(x);
            } catch (UnsupportedOperationException x) {
                //Some file systems do not support symbolic links.
                System.err.println(x);
            }


            copyFile(scrFileName, dstFileName);

            File srcF = new File(scrFileName);

            srcF.delete();
            */
        }

        public void copyRandomAccessFile(String scrFileName, String dstFileName) throws java.rmi.RemoteException {
            try{
                copyFile(new File(scrFileName), new File(dstFileName));
            } catch (IOException ioe) {
                throw new RemoteException("Exception Holder", ioe);
            }
        }

        public void copyRandomAccessDir(String scrPath, String dstPath) throws java.rmi.RemoteException {
            try{
                recursiveCopyDir(new File(scrPath), new File(dstPath));
            } catch (IOException ioe) {
                throw new RemoteException("Exception Holder", ioe);
            }
        }

        public void moveRandomAccessFile(String scrFileName, String dstFileName) throws java.rmi.RemoteException {
            try{
                moveFile(new File(scrFileName), new File(dstFileName));
            } catch (IOException ioe) {
                throw new RemoteException("Exception Holder", ioe);
            }
        }

        public void moveRandomAccessDir(String scrPath, String dstPath) throws java.rmi.RemoteException {
            try{
                recursiveMoveDir(new File(scrPath), new File(dstPath));
            } catch (IOException ioe) {
                throw new RemoteException("Exception Holder", ioe);
            }
        }

        // working directory
        //
		private void getWorkingDirectoryFileNames(File path, Vector<String> result) throws java.rmi.RemoteException {
			File[] files = path.listFiles();
			if (files == null)
				return;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					getWorkingDirectoryFileNames(files[i], result);
				} else {
					String name = path.getAbsolutePath().substring(WDIR.length(), path.getAbsolutePath().length())
                            + System.getProperty("file.separator")
							+ files[i].getName();
					name = (name.substring(1, name.length())).replace('\\', '/');
				}
			}
		}

		private void getWorkingDirectoryFileDescriptions(File path, Vector<FileDescription> result) throws java.rmi.RemoteException {
			File[] files = path.listFiles();
			if (files == null)
				return;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					String name = path.getAbsolutePath().substring(WDIR.length(), path.getAbsolutePath().length())
                            + System.getProperty("file.separator")
							+ files[i].getName();
					name = name.substring(1, name.length()).replace('\\', '/');
					//result.add(new FileDescription(name, files[i].length(), true, new Date(files[i].lastModified())));
                    result.add(new FileDescription(files[i]));

					/*
					 * String name =
					 * (files[i].getAbsolutePath().substring(WDIR.length() + 1,
					 * files[i].getAbsolutePath().length())).replace('\\', '/');
					 * result.add(new FileDescription(name, 0, true, new
					 * Date(files[i].lastModified())));
					 * getWorkingDirectoryFileDescriptions(files[i], result);
					 */
				} else {
					String name = path.getAbsolutePath().substring(WDIR.length(), path.getAbsolutePath().length()) + System.getProperty("file.separator")
							+ files[i].getName();
					name = name.substring(1, name.length()).replace('\\', '/');
					//result.add(new FileDescription(name, files[i].length(), false, new Date(files[i].lastModified())));
                    result.add(new FileDescription(files[i]));
				}
			}
		}

		public FileDescription[] getWorkingDirectoryFileDescriptions() throws java.rmi.RemoteException {
			Vector<FileDescription> result = new Vector<FileDescription>();
			getWorkingDirectoryFileDescriptions(new File(WDIR), result);
			return (FileDescription[]) result.toArray(new FileDescription[0]);
		}

		public FileDescription getWorkingDirectoryFileDescription(String fileName) throws java.rmi.RemoteException {
			return getRandomAccessFileDescription(WDIR + System.getProperty("file.separator") + fileName);
		}


        // reinit
        public void reinitServer() throws java.rmi.RemoteException {
            reloadInitR();
        }

        // workspace

        private String WKS_FILE_NAME = ".RData";

        public void loadWorkspace(String path) throws java.rmi.RemoteException {
            String workspace = ( path != null ? path + "/" : "")  + WKS_FILE_NAME;

            if (new File(workspace).exists()) {
                HashMap<String, Object> attrs = new HashMap<String, Object>();
                attrs.put(RActionConst.WORKSPACELOADED, true);
                asynchronousConsoleSubmit("load('" + workspace + "', .GlobalEnv)", attrs);
            }
        }

        public void saveWorkspace(String path) throws java.rmi.RemoteException {
            String workspace = ( path != null ? path + "/" : "")  + WKS_FILE_NAME;
            HashMap<String, Object> attrs = new HashMap<String, Object>();
            attrs.put(RActionConst.WORKSPACESAVED, true);
            asynchronousConsoleSubmit("save.image(file = '" + workspace + "')", attrs);
        }

        // working directory

        public void createWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
            createRandomAccessFile(WDIR + '/' + fileName);
        }

        public void removeWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
            removeRandomAccessFile(WDIR + '/' + fileName);
        }

		public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException {
			return readRandomAccessFileBlock(WDIR + '/' + fileName, offset, blocksize);
		}

		public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws java.rmi.RemoteException {
			appendBlockToRandomAccessFile(WDIR + '/' + fileName, block);
		}

		public byte[] getRHelpFile(String uri) throws RemoteException {
			try {
                //log.info("getRHelpFile uri="+uri);

				if (uri.indexOf('#') != -1) {
					uri = uri.substring(0, uri.indexOf('#'));
				}

				String filePath = null;
				if (System.getenv().get("R_LIBS") != null && !System.getenv().get("R_LIBS").equals("") && uri.startsWith("/library/")) {
					filePath = System.getenv().get("R_LIBS") + uri.substring(8);
					if (!new File(filePath).exists())
						filePath = null;
				}

				if (filePath == null) {
					filePath = System.getenv().get("R_HOME") + uri;
				}

				RandomAccessFile raf = new RandomAccessFile(filePath, "r");
				byte[] result = new byte[(int) raf.length()];
				raf.readFully(result);
				raf.close();

				return result;
			} catch (Exception e) {
				throw new RemoteException("Exception Holder", e);
			}
		}

		public String getRHelpFileUri(String topic, String pack) throws RemoteException {
            String rhomepath = System.getenv().get("R_HOME");

            if (pack != null && pack.length() > 0 && rhomepath != null && rhomepath.length() > 0) {
                String uri1 = "/library/" + pack + "/html/" + topic + ".html";
                String uri2 = "/library/" + pack + "/html/00Index.html";

                if (new File(rhomepath + uri1).exists()) {
                    return uri1;
                } else if (new File(rhomepath + uri2).exists()) {
                    return uri2;
                }

                return _symbolUriMap.get(pack + "~" + topic);

            } else {
                return null;
            }
		}

		public String[] listDemos() throws RemoteException {
			if (demosList == null) {
				demosList = new Vector<String>();
				try {
					Properties props = new Properties();
					props.loadFromXML(getResourceAsStream("/rdemos/list.properties"));
					for (Object key : PoolUtils.orderO(props.keySet())) {
						demosList.add(props.getProperty((String) key));
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}

			}

			return demosList.toArray(new String[0]);

		}

		public String getDemoSource(String demoName) throws RemoteException {
			if (!resourceExists("/rdemos/" + demoName + ".r")) {
				throw new RemoteException("no demo with name <" + demoName + ">");
			} else {
				try {
					StringBuffer result = new StringBuffer();
					BufferedReader br = new BufferedReader(new InputStreamReader(getResourceAsStream("/rdemos/" + demoName + ".r")));
					String line = null;
					while ((line = br.readLine()) != null) {
						result.append(line + "\n");
					}
					return result.toString();
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
			}
		}

		public String getProcessId() throws RemoteException {
			return PoolUtils.getProcessId();
		}

		public String getHostIp() throws RemoteException {
			return PoolUtils.getHostIp();
		}

        public String getHostname() throws RemoteException {
            return PoolUtils.getHostName();
        }

		public Vector<String> getSvg(String script, int width, int height) throws RemoteException {

			File tempFile = null;
			String status = "";

			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".svg").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
                log.error("Error!", e);
				throw new RemoteException("", e);
			}

			// boolean svgFunctionAvailable=symbolExists("svg");
			boolean svgFunctionAvailable = false;
			boolean CairoFunctionAvailable = false;
			if (!svgFunctionAvailable) {
				evaluate("library(Cairo)");
				CairoFunctionAvailable = symbolExists("Cairo");
			}

			if (!(svgFunctionAvailable || CairoFunctionAvailable)) {

//				DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
				String svgNS = "http://www.w3.org/2000/svg";
//				Document document = domImpl.createDocument(svgNS, "svg", null);
//				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

				Dimension dSize = new Dimension(width, height);
				GDDevice device = null;
				try {
					device = newDevice(dSize.width, dSize.height);

					sourceFromBuffer(script);
					status = getStatus();
					if (!status.equals("")) {
						log.info(status);
					}

					Vector<GDObject> g2dObjects = device.popAllGraphicObjects(-1);
//					Java2DUtils.paintAll(svgGenerator, new Point(0, 0), dSize, g2dObjects);
				} catch (Exception e) {
					throw new RemoteException("", e);
				} finally {
					device.dispose();
				}

				try {
//					svgGenerator.setSVGCanvasSize(dSize);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					boolean useCSS = true;
					Writer out = new OutputStreamWriter(baos, "UTF-8");
//					svgGenerator.stream(out, useCSS);
					out.close();

					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = builder.parse(new ByteArrayInputStream(baos.toByteArray()));
					((Element) doc.getElementsByTagName("svg").item(0))
                            .setAttribute("viewBox", "0,0," + dSize.width + "," + dSize.height);

					Writer stringW = new StringWriter();
					DOMSource source = new DOMSource(doc);
					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					transformer.transform(source, new StreamResult(stringW));

					Vector<String> result = new Vector<String>();
					BufferedReader br = new BufferedReader(new StringReader(stringW.toString()));
					String line = null;
					while ((line = br.readLine()) != null)
						result.add(line);

					_lastStatus = status;
					return result;
				} catch (Exception e) {
					throw new RemoteException("", e);
				}

			} else {
				String command = "CairoSVG(file = \"" + tempFile.getAbsolutePath().replace('\\', '/') + "\", width = "
                        + new Double(10 * (width / height)) + ", height = " + 10
                        + " , onefile = TRUE, bg = \"transparent\" ,pointsize = 12);";
				evaluate(command);
				if (!getStatus().equals("")) {
					log.info(getStatus());
				}

				sourceFromBuffer(script);
				status = getStatus();
				if (!status.equals("")) {
					log.info(status);
				}

				if (tempFile.exists()) {
					Vector<String> result = null;
					try {

						DirectJNI.getInstance().shutdownDevices("Cairo");

						result = new Vector<String>();
						BufferedReader br = new BufferedReader(new FileReader(tempFile));
						String line = null;
						while ((line = br.readLine()) != null) {
							result.add(line);
						}
						br.close();
						tempFile.delete();

					} catch (Exception e) {
                        log.error("Error!", e);
						throw new RemoteException("", e);
					}
					_lastStatus = status;
					return result;
				} else {
					return null;
				}

			}
		}

		public byte[] getPdf(String script, int width, int height) throws RemoteException {

			String status = "";
			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".pdf").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
                log.error("Error!", e);
				throw new RemoteException("", e);
			}

			int currentDevice = ((RInteger) getObject(".PrivateEnv$dev.cur()")).getValue()[0];
			shutdownDevices("pdf");

			final String createDeviceCommand = "pdf(file = \"" + tempFile.getAbsolutePath().replace('\\', '/') + "\", width = "
					+ new Double(6 * (width / height)) + ", height = " + 6 + " , onefile = TRUE, title = '', fonts = NULL, version = '1.1' )";
			evaluate(createDeviceCommand);
			if (!getStatus().equals("")) {
				log.info(getStatus());
			}

			sourceFromBuffer(script);
			status = getStatus();
			if (!status.equals("")) {
				log.info(status);
			}
			evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
			if (tempFile.exists()) {
				byte[] result = null;
				try {
					shutdownDevices("pdf");
					RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
					result = new byte[(int) raf.length()];
					raf.readFully(result);
					raf.close();
					tempFile.delete();
				} catch (Exception e) {
                    log.error("Error!", e);
					throw new RemoteException("", e);
				}
				_lastStatus = status;
				return result;
			} else {
				_lastStatus = status;
				return null;
			}
		}

		public int countSets() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarSetInterfaceRemote getSet(int i) throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarSetInterfaceRemote getCurrentSet() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public int curSetId() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarInterfaceRemote getVar(int setId, int i) throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarInterfaceRemote getVar(int setId, String name) throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public String getStub() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		synchronized public void addProbeOnVariables(String[] variables) throws RemoteException {
			HashSet<String> pvHash = new HashSet<String>();
			for (int i = 0; i < probedVariables.length; ++i)
				pvHash.add(probedVariables[i]);
			for (int i = 0; i < variables.length; ++i)
				pvHash.add(variables[i]);

			String[] newProbedVariables = new String[pvHash.size()];
			int i = 0;
			for (String k : pvHash)
				newProbedVariables[i++] = k;
			probedVariables = newProbedVariables;
		}

		synchronized public void removeProbeOnVariables(String[] variables) throws RemoteException {
			HashSet<String> pvHash = new HashSet<String>();
			for (int i = 0; i < probedVariables.length; ++i)
				pvHash.add(probedVariables[i]);
			for (int i = 0; i < variables.length; ++i)
				pvHash.remove(variables[i]);

			String[] newProbedVariables = new String[pvHash.size()];
			int i = 0;
			for (String k : pvHash)
				newProbedVariables[i++] = k;
			probedVariables = newProbedVariables;
			;
		}

		public void setProbedVariables(String[] variables) throws RemoteException {
			HashSet<String> pvHash = new HashSet<String>();
			for (int i = 0; i < variables.length; ++i)
				pvHash.add(variables[i]);
			String[] newProbedVariables = new String[pvHash.size()];
			int i = 0;
			for (String k : pvHash)
				newProbedVariables[i++] = k;
			probedVariables = newProbedVariables;
		}
		
		public String[] getProbedVariables() throws RemoteException {
			return probedVariables;
		}

		public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException {
			return null;
		}

		public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
			return null;
		}

        public String getWorkingDirectory() throws RemoteException {
            return DirectJNI.this.getWorkingDirectory();
        }

        public void setWorkingDirectory(String dir) throws RemoteException {
            DirectJNI.this.setWorkingDirectory(dir);
        }

        public void setRootDirectory(String root)  throws RemoteException { }

        public FileNode readDirectory(String directory) throws RemoteException {
            return null;
        }

        public String getProperty(String name) throws RemoteException {
            return System.getProperty(name);
        }

        public Hashtable<Object, Object> getProperties() throws RemoteException {
            return System.getProperties();
        }

        public void setProperty(String name, String value) throws RemoteException {
            System.setProperty(name, value);
        }
	};

	static private HashMap<Integer, GDDevice> _localDeviceHashMap = new HashMap<Integer, GDDevice>();
	static private HashSet<Integer> _localBroadcastedDevices = new HashSet<Integer>();

	public boolean broadcastRequired(int currentDevice) {
		return _localBroadcastedDevices.size() > 1 && _localBroadcastedDevices.contains(currentDevice);
	}

	public static class GDDeviceLocal implements GDDevice {
		GDContainerBag gdBag = null;
        RMainLoopCallbacksImpl rCallbacks;

		public GDDeviceLocal(int w, int h, RMainLoopCallbacksImpl callbacks) throws RemoteException {
            gdBag = new GDContainerBag(w, h);
			JavaGD.setGDContainer(gdBag);
			Dimension dim = gdBag.getSize();

            this.rCallbacks = callbacks;

			RInteger devicesBefore = (RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.list()");
			Vector<Integer> devicesVector = new Vector<Integer>();
			if (devicesBefore != null) {
				for (int i = 0; i < devicesBefore.getValue().length; ++i)
					devicesVector.add(devicesBefore.getValue()[i]);
			}
            log.info("devices before :" + devicesBefore);

            log.info(DirectJNI.getInstance().getRServices().evaluate(
                    "JavaGD(name='JavaGD', width=" + dim.getWidth() + ", height=" + dim.getHeight() + ", ps=12)"));

			RInteger devicesAfter = (RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.list()");
			for (int i = 0; i < devicesAfter.getValue().length; ++i)
				if (!devicesVector.contains(devicesAfter.getValue()[i])) {
                    log.info("caught:" + devicesAfter.getValue()[i]);
					gdBag.setDeviceNumber(devicesAfter.getValue()[i]);
					break;
				}

			// log.info(DirectJNI.getInstance().getRServices().
			// consoleSubmit(".PrivateEnv$dev.list()"));

			_localDeviceHashMap.put(gdBag.getDeviceNumber(), this);

            rCallbacks.addBusyCallback(gdBag);
		}

		public GDDeviceLocal(int w, int h, boolean broadcasted,
                             RMainLoopCallbacksImpl callbacks) throws RemoteException {
			this(w, h, callbacks);
			if (broadcasted)
				_localBroadcastedDevices.add(gdBag.getDeviceNumber());
		}

        public void addGraphicListener(GDObjectListener objectListener) throws RemoteException {
            gdBag.addGraphicListener(objectListener);
        }

        public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException {
            gdBag.removeGraphicListener(objectListener);

        }

		public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException {
			return gdBag.popAllGraphicObjects(maxNbrGraphicPrimitives);
		}

		public boolean hasGraphicObjects() throws RemoteException {
			return gdBag.hasGraphicObjects();
		}

		public void fireSizeChangedEvent(int w, int h) throws RemoteException {
			gdBag.setSize(w, h);
			DirectJNI.getInstance().getRServices().evaluate("try( {.C(\"javaGDresize\",as.integer("
                    + gdBag.getDeviceNumber() + "))}, silent=TRUE)");
			DirectJNI.getInstance().snapRBusinessIndicator(); //

            if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
                log.info(DirectJNI.getInstance().getRServices().getStatus());
			}
			// DirectJNI.getInstance().getRServices().consoleSubmit("1");
		}

		public void dispose() throws RemoteException {
			log.info("dispose");

            gdBag.dispose();

            _localDeviceHashMap.remove(gdBag.getDeviceNumber());

            _localBroadcastedDevices.remove(gdBag.getDeviceNumber());

            rCallbacks.removeBusyCallback(gdBag);

            new Thread(new Runnable(){
                public void run() {
                    try {
                        RServices r = DirectJNI.getInstance().getRServices();
                        r.evaluate("try({ .PrivateEnv$dev.off(which="
                                + gdBag.getDeviceNumber() + ")},silent=TRUE)");
                    } catch (Exception ex) {
                    }
                }
            }).start();
		}

		public int getDeviceNumber() throws RemoteException {
			return gdBag.getDeviceNumber();
		}

		public boolean isCurrentDevice() throws RemoteException {
			int d = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
			return d == gdBag.getDeviceNumber();
		}

		public void setAsCurrentDevice() throws RemoteException {
			DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ")");
		}

		public Dimension getSize() throws RemoteException {
			return gdBag.getSize();
		}

		public void putLocation(Point2D p) throws RemoteException {
			GDInterface.putLocatorLocation(p);
		}

		public boolean hasLocations() throws RemoteException {
			return GDInterface.hasLocations();
		}

		public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
			GDInterface.saveLocations();
			try {
				for (int i = 0; i < points.length; ++i) {
					GDInterface.putLocatorLocation(points[i]);
				}

				RList l = (RList) DirectJNI.getInstance().getRServices().getObject("locator()");

				Point2D[] result = new Point2D[points.length];
				for (int i = 0; i < points.length; ++i) {
					result[i] = new DoublePoint(((RNumeric) l.getValue()[0]).getValue()[i],
                            ((RNumeric) l.getValue()[1]).getValue()[i]);
				}
				return result;
			} finally {
				GDInterface.restoreLocations();
			}

		}

		synchronized public byte[] getSVG() throws RemoteException {
			Vector<String> result = getSVGAsText();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.size(); ++i)
				sb.append(result.elementAt(i));
			return sb.toString().getBytes();
		}

		synchronized public Vector<String> getSVGAsText() throws RemoteException {

			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".svg").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
                log.error("Error!", e);
				throw new RemoteException("", e);
			}

			// boolean
			// svgFunctionAvailable=DirectJNI.getInstance().getRServices(
			// ).symbolExists("svg");
			boolean svgFunctionAvailable = false;
			boolean CairoFunctionAvailable = false;
			if (!svgFunctionAvailable) {
				DirectJNI.getInstance().getRServices().evaluate("library(Cairo)");
				CairoFunctionAvailable = DirectJNI.getInstance().getRServices().symbolExists("Cairo");
			}

			if (!(svgFunctionAvailable || CairoFunctionAvailable)) {

//				DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
				String svgNS = "http://www.w3.org/2000/svg";
//				Document document = domImpl.createDocument(svgNS, "svg", null);
//				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

				Dimension dSize = getSize();

				GDDevice device = null;
				try {
					int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices()
                            .getObject(".PrivateEnv$dev.cur()")).getValue()[0];
					device = DirectJNI.getInstance().getRServices().newDevice(dSize.width, dSize.height);
					DirectJNI.getInstance().getRServices().evaluate(
							".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which="
                                    + device.getDeviceNumber() + ");"
									+ ".PrivateEnv$dev.set(" + currentDevice + ");", 3);

//					Java2DUtils.paintAll(svgGenerator, new Point(0, 0), dSize, device.popAllGraphicObjects(-1));
				} catch (Exception e) {
					throw new RemoteException("", e);
				} finally {
					device.dispose();
				}

				try {
//					svgGenerator.setSVGCanvasSize(dSize);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					boolean useCSS = true;
					Writer out = new OutputStreamWriter(baos, "UTF-8");
//					svgGenerator.stream(out, useCSS);
					out.close();

					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = builder.parse(new ByteArrayInputStream(baos.toByteArray()));
					((Element) doc.getElementsByTagName("svg").item(0)).setAttribute("viewBox", "0,0," + dSize.width + "," + dSize.height);

					Writer stringW = new StringWriter();
					DOMSource source = new DOMSource(doc);
					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					transformer.transform(source, new StreamResult(stringW));

					Vector<String> result = new Vector<String>();
					BufferedReader br = new BufferedReader(new StringReader(stringW.toString()));
					String line = null;
					while ((line = br.readLine()) != null)
						result.add(line);

					return result;
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
			} else {

				String SvgDeviceName = null;
				if (svgFunctionAvailable)
					SvgDeviceName = "svg";
				else
					SvgDeviceName = "Cairo";

				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];

				DirectJNI.getInstance().shutdownDevices(SvgDeviceName);

				final String createDeviceCommand = SvgDeviceName + (SvgDeviceName.equals("svg") ? "" : "SVG") + "(file = \""
						+ tempFile.getAbsolutePath().replace('\\', '/') + "\", width = " + new Double(10 * (getSize().width / getSize().height))
						+ ", height = " + 10 + " , onefile = TRUE, bg = \"transparent\" ,pointsize = 12)";

                log.info("createDeviceCommand:" + createDeviceCommand);
				DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
				if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
					log.info(DirectJNI.getInstance().getRServices().getStatus());
                    log.info("Status:" + DirectJNI.getInstance().getRServices().getStatus());
				}

				int cairoDevice = DirectJNI.getInstance().getDevice(SvgDeviceName);
				DirectJNI.getInstance().getRServices().evaluate(
						".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + cairoDevice + ");" + ".PrivateEnv$dev.set("
								+ currentDevice + ");", 3);

				if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
					log.info(DirectJNI.getInstance().getRServices().getStatus());
				}

				if (tempFile.exists()) {

					Vector<String> result = null;
					try {

						DirectJNI.getInstance().shutdownDevices(SvgDeviceName);

						result = new Vector<String>();
						BufferedReader br = new BufferedReader(new FileReader(tempFile));
						String line = null;
						while ((line = br.readLine()) != null) {
							result.add(line);
						}
						br.close();
						tempFile.delete();

					} catch (Exception e) {
                        log.error("Error!", e);
						throw new RemoteException("", e);
					}
					return result;
				} else {
					return null;
				}

			}
		}

		public byte[] getBmp() throws RemoteException {
			return gdBag.getBmp();
		}

		public byte[] getJpg() throws RemoteException {
            return gdBag.getJpg();
		}

        public byte[] getPng() throws RemoteException {
            return gdBag.getPng();
        }

		public byte[] getPdf() throws RemoteException {
			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".pdf").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
                log.error("Error!", e);
				throw new RemoteException("", e);
			}

			int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];

			DirectJNI.getInstance().shutdownDevices("pdf");

			final String createDeviceCommand = "pdf(file = \"" + tempFile.getAbsolutePath().replace('\\', '/') + "\", width = "
					+ new Double(6 * (getSize().width / getSize().height)) + ", height = " + 6
					+ " , onefile = TRUE, title = '', fonts = NULL, version = '1.1' )";
			DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			int pdfDevice = DirectJNI.getInstance().getDevice("pdf");
			DirectJNI.getInstance().getRServices().evaluate(
					".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + pdfDevice + ");" + ".PrivateEnv$dev.set("
							+ currentDevice + ");", 3);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			if (tempFile.exists()) {

				byte[] result = null;
				try {

					DirectJNI.getInstance().shutdownDevices("pdf");

					RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
					result = new byte[(int) raf.length()];
					raf.readFully(result);
					raf.close();

					tempFile.delete();

				} catch (Exception e) {
                    log.error("Error!", e);
					throw new RemoteException("", e);
				}
				return result;
			} else {
				return null;
			}
		}

		public byte[] getPictex() throws RemoteException {
			return null;
		}

		public byte[] getPostScript() throws RemoteException {
			return null;
		}

		public byte[] getXfig() throws RemoteException {
			return null;
		}

		public String getId() throws RemoteException {
			return null;
		}

		public boolean isBroadcasted() throws RemoteException {
			return _localBroadcastedDevices.contains(gdBag.getDeviceNumber());
		}

		public void broadcast() throws RemoteException {
			if (_localBroadcastedDevices.size() <= 1 || !_localBroadcastedDevices.contains(gdBag.getDeviceNumber()))
				return;

			final Exception[] exceptionHolder = new Exception[1];
			String lastStatus = DirectJNI.getInstance().runR(new ExecutionUnit() {
				public void run(Rengine e) {
					try {

						int currentDevice = e.rniGetIntArray(e.rniEval(e.rniParse(".PrivateEnv$dev.cur()", 1), 0))[0];
						String code = ".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");";
						for (Integer d : _localBroadcastedDevices) {
							if (d != gdBag.getDeviceNumber()) {
								code += ".PrivateEnv$dev.copy(which=" + d + ");";
							}
						}
						code += ".PrivateEnv$dev.set(" + currentDevice + ");";
						e.rniEval(e.rniParse(code, _localBroadcastedDevices.size() + 1), 0);

                        log.info("code =" + code);

					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!lastStatus.equals("")) {
				log.info(lastStatus);
			}
		}
	}

	private GraphicNotifier gn = new LocalGraphicNotifier();

	public GraphicNotifier getGraphicNotifier() {
		return gn;
	}

	private static boolean _initHasBeenCalled = false;

	public void initPrivateEnv() {
		runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
					_privateEnvExp = e.rniEval(e.rniParse(PENV, 1), 0);
				} catch (Exception ex) {
                    log.error("Error!", ex);
				}
			}
		});
	}

	public void testR() {
        //int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices()
        //                                    .getObject(".PrivateEnv$dev.cur()")).getValue()[0]

		runR(new ExecutionUnit() {
			public void run(Rengine e) {
				try {
                    RObject obj = DirectJNI.this.evalAndGetObject("2+2", false);

                    //_rEngine.rniEval(_rEngine.rniParse("2+2", 1), 0);
                    log.info("Tested R. Expression 2+2=" + obj.toString());
				} catch (Exception ex) {
                    log.error("Error!", ex);
				}
			}
		});
	}

	private static void init(ClassLoader cl) throws Exception {
		Properties props = new Properties();
		InputStream is = cl.getResourceAsStream("maps/rjbmaps.xml");
        log.info("####### is:" + is);
		props.loadFromXML(is);
		DirectJNI._packageNames = (Vector<String>) PoolUtils.hexToObject((String) props.get("PACKAGE_NAMES"), cl);
		DirectJNI._s4BeansMapping = (HashMap<String, String>) PoolUtils.hexToObject((String) props.get("S4BEANS_MAP"), cl);
		DirectJNI._s4BeansMappingRevert = (HashMap<String, String>) PoolUtils.hexToObject((String) props.get("S4BEANS_REVERT_MAP"), cl);
		DirectJNI._factoriesMapping = (HashMap<String, String>) PoolUtils.hexToObject((String) props.get("FACTORIES_MAPPING"), cl);
		DirectJNI._s4BeansHash = (HashMap<String, Class<?>>) PoolUtils.hexToObject((String) props.get("S4BEANS_HASH"), cl);
		DirectJNI._rPackageInterfacesHash = (HashMap<String, Vector<Class<?>>>) PoolUtils.hexToObject((String) props.get("R_PACKAGE_INTERFACES_HASH"), cl);
		DirectJNI._abstractFactories = (Vector<String>) PoolUtils.hexToObject((String) props.get("ABSTRACT_FACTORIES"), cl);
		log.info("<> rPackageInterfaces:" + DirectJNI._packageNames);
		log.info("<> s4Beans MAP :" + DirectJNI._s4BeansMapping);
		log.info("<> s4Beans Revert MAP :" + DirectJNI._s4BeansMappingRevert);
		log.info("<> factories :" + DirectJNI._factoriesMapping);
		_mappingClassLoader = cl;
		_resourcesClassLoader = cl;
		Thread.currentThread().setContextClassLoader(_resourcesClassLoader);
		DirectJNI.getInstance().getRServices().sourceFromResource("/bootstrap.R");
		DirectJNI.getInstance().initPackages();
		DirectJNI.getInstance().upgdateBootstrapObjects();
	}

	private void upgdateBootstrapObjects() throws Exception {
        try {
            RChar objs = (RChar) getRServices().getObject(".PrivateEnv$ls(all.names=TRUE)");
          		for (int i = 0; i < objs.getValue().length; ++i)
          			if (!_bootstrapRObjects.contains(objs.getValue()[i]))
          				_bootstrapRObjects.add(objs.getValue()[i]);
        } catch (Exception ex) {
            log.error("Error!", ex);
        }
	}

	static private void scanMapping() {
		if (!_initHasBeenCalled) {
			_initHasBeenCalled = true;

			if (DirectJNI.class.getClassLoader().getResource("maps/rjbmaps.xml") != null) {
                log.info("<1> " + DirectJNI.class.getClassLoader().getResource("maps/rjbmaps.xml"));
				try {
					init(DirectJNI.class.getClassLoader());
				} catch (Exception e) {
                    log.error("Error!", e);
				}
				return;

			} else if (System.getProperty("java.rmi.server.codebase") != null) {
				ClassLoader codebaseClassLoader = new URLClassLoader(
                        PoolUtils.getURLS(System.getProperty("java.rmi.server.codebase")), DirectJNI.class
						.getClassLoader());
				if (codebaseClassLoader.getResource("maps/rjbmaps.xml") != null) {
                    log.info("<2> " + codebaseClassLoader.getResource("maps/rjbmaps.xml"));
					try {
						init(codebaseClassLoader);
					} catch (Exception e) {
                        log.error("Error!", e);
					}
					return;
				}
			} else {
                log.info("!! No mapping found");
			}
		}
	}

	public void regenerateWorkingDirectory(boolean callR) throws Exception {
		File wdirFile = new File(WDIR);
		if (!wdirFile.exists()) {
            wdirFile.mkdirs();
		}
		if (callR)
			getRServices().evaluate(".PrivateEnv$setwd(\"" + WDIR + "\")");
	}

	// public for internal use only
	public void reinitWorkingDirectory(String dir) throws Exception {
		File d = new File(dir);
		if (!d.exists() || !d.isDirectory()) {
			throw new Exception("Bad Directory");
		}
		WDIR = d.getCanonicalPath().replace('\\', '/');
		regenerateWorkingDirectory(false);
	}

    public String getWorkingDirectory() {
        return WDIR;
    }

    public void setWorkingDirectory(String dir) {
        WDIR = dir.replace('\\', '/');
        try {
            regenerateWorkingDirectory(true);
        } catch (Exception ex) {
        }
    }

	public static final String LOC_STR_LEFT = "It represents the S4 Class";
	public static final String LOC_STR_RIGHT = "in R package";

	public static String getRClassForBean(JarFile jarFile, String beanClassName) throws Exception {
		BufferedReader br = new BufferedReader(
                new InputStreamReader(jarFile.getInputStream(jarFile.getEntry(beanClassName.replace('.', '/') + ".java"))));
		do {
			String line = br.readLine();
			if (line != null) {
				int p = line.indexOf(LOC_STR_LEFT);
				if (p != -1) {
					return line.substring(p + LOC_STR_LEFT.length(), line.indexOf(LOC_STR_RIGHT)).trim();
				}
			} else
				break;
		} while (true);
		return null;
	}

	public static void generateMaps(URL jarUrl, boolean rawClasses) {

		try {

			_mappingClassLoader = new URLClassLoader(new URL[] { jarUrl }, DirectJNI.class.getClassLoader());
			Vector<String> list = new Vector<String>();
			JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
			JarFile jarfile = jarConnection.getJarFile();
			Enumeration<JarEntry> enu = jarfile.entries();
			while (enu.hasMoreElements()) {
				String entry = enu.nextElement().toString();
				if (entry.endsWith(".class"))
					list.add(entry.replace('/', '.').substring(0, entry.length() - ".class".length()));
			}

			for (int i = 0; i < list.size(); ++i) {
				String className = list.elementAt(i);
				if (className.startsWith("org.kchine.r.packages.") && !className.startsWith("org.kchine.r.packages.rservices")) {
					Class<?> c_ = _mappingClassLoader.loadClass(className);

					if (c_.getSuperclass() != null && c_.getSuperclass().equals(RObject.class)
                            && !Modifier.isAbstract(c_.getModifiers())) {

						if (c_.equals(RLogical.class) || c_.equals(RInteger.class)
                                || c_.equals(RNumeric.class) || c_.equals(RComplex.class)
								|| c_.equals(RChar.class) || c_.equals(RMatrix.class)
                                || c_.equals(RArray.class) || c_.equals(RList.class)
								|| c_.equals(RDataFrame.class) || c_.equals(RFactor.class)
                                || c_.equals(REnvironment.class) || c_.equals(RVector.class)
								|| c_.equals(RUnknown.class)) {
						} else {
							String rclass = DirectJNI.getRClassForBean(jarfile, className);
							_s4BeansHash.put(className, c_);
							_s4BeansMapping.put(rclass, className);
							_s4BeansMappingRevert.put(className, rclass);
						}

					} else if ((rawClasses && c_.getSuperclass() != null && c_.getSuperclass().equals(Object.class))
							|| (!rawClasses && RPackage.class.isAssignableFrom(c_) && (c_.isInterface()))) {

						String shortClassName = className.substring(className.lastIndexOf('.') + 1);
						_packageNames.add(shortClassName);

						Vector<Class<?>> v = _rPackageInterfacesHash.get(className);
						if (v == null) {
							v = new Vector<Class<?>>();
							_rPackageInterfacesHash.put(className, v);
						}
						v.add(c_);

					} else {
						String nameWithoutPackage = className.substring(className.lastIndexOf('.') + 1);
						if (nameWithoutPackage.indexOf("Factory") != -1 && c_.getMethod("setData",
                                new Class[] { RObject.class }) != null) {
							// if
							// (DirectJNI._factoriesMapping.get(nameWithoutPackage
							// )
							// != null) throw new Exception("Factories Names
							// Conflict : two " + nameWithoutPackage);
							_factoriesMapping.put(nameWithoutPackage, className);
							if (Modifier.isAbstract(c_.getModifiers()))
								_abstractFactories.add(className);
						}
					}
				}
			}

			// log.info("s4Beans:" +s4Beans);
			log.info("rPackageInterfaces:" + _packageNames);
			log.info("s4Beans MAP :" + _s4BeansMapping);
			log.info("s4Beans Revert MAP :" + _s4BeansMappingRevert);
			log.info("factories :" + _factoriesMapping);
			log.info("r package interface hash :" + _rPackageInterfacesHash);

		} catch (Exception ex) {
            log.error("Error!", ex);
		}
	}

	synchronized public static void init(String instanceName) {
		setInstanceName(instanceName);
		scanMapping();
	}

    /*
	public static void init() {
		init((String) null);
	}
	*/

	private static void setInstanceName(String instanceName) {
		if (instanceName == null || instanceName.trim().equals("")) {
			instanceName = "LOCAL_R";
		}
		INSTANCE_NAME = instanceName.trim();

        log.info("INSTANCE_NAME="+INSTANCE_NAME);
	}

	public static String getInstanceName() {
		return INSTANCE_NAME;
	}

	public static String clean(String str) {
		byte[] tab = str.getBytes();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tab.length; ++i) {
			if (tab[i] < 32 && tab[i] != (byte) '\n')
				sb.append(" ");
			else
				sb.append((char) tab[i]);
		}
		return sb.toString();
	}

	public void notifyRActionListeners(RAction action) {

        Iterator<RActionProcessor> i = _rActionProcessors.iterator();

        while (i.hasNext()) {
            RActionProcessor p = i.next();

            if (p.isShutdownRequested() || p.getListener() == null) {
                i.remove();
            } else {
                p.notifyListeners(action);
            }
        }
	}

	public void removeAllRActionListeners() {
        Iterator<RActionProcessor> i = _rActionProcessors.iterator();

        while (i.hasNext()) {
            RActionProcessor p = i.next();

            p.setShutdownRequested(true);
            i.remove();
        }
	}

	public void removeRActionListener(RActionListener ractionListener) {

        Iterator<RActionProcessor> i = _rActionProcessors.iterator();

        while (i.hasNext()) {
            RActionProcessor p = i.next();

            if (ractionListener.equals(p.getListener())) {

                p.setShutdownRequested(true);
                i.remove();

                return;
            }

        }
	}

	public void addRActionListener(RActionListener ractionListener) {

        _rActionProcessors.add(new RActionProcessor(ractionListener));
	}

    public boolean areRActionListenersAvailable() {
        return (_rActionProcessors.size() > 0);
    }

	public String getOriginatorUID() {
		return _originatorUID;
	}

	public void setOrginatorUID(String uid) {
        _originatorUID=uid;
	}

}
