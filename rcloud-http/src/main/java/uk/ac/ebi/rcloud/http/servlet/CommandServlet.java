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
package uk.ac.ebi.rcloud.http.servlet;

import uk.ac.ebi.rcloud.http.callback.GDObjectListenerImpl;
import uk.ac.ebi.rcloud.http.callback.GDObjectProviderImpl;
import uk.ac.ebi.rcloud.http.callback.GenericRActionListenerImpl;
import uk.ac.ebi.rcloud.http.callback.GenericRActionProviderImpl;
import uk.ac.ebi.rcloud.http.exception.*;
import uk.ac.ebi.rcloud.http.util.LdapKit;
import uk.ac.ebi.rcloud.rpf.*;
import uk.ac.ebi.rcloud.rpf.db.DAOLayer;
import uk.ac.ebi.rcloud.rpf.db.DAOLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.data.ProjectDataDB;
import uk.ac.ebi.rcloud.rpf.db.data.ServerDataDB;
import uk.ac.ebi.rcloud.rpf.db.sql.SqlBase;
import uk.ac.ebi.rcloud.rpf.exception.RmiCallInterrupted;
import uk.ac.ebi.rcloud.rpf.exception.RmiCallTimeout;
import uk.ac.ebi.rcloud.util.DETools;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.callback.GenericCallbackDevice;
import uk.ac.ebi.rcloud.server.callback.RAction;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ConnectException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import uk.ac.ebi.rcloud.rpf.db.DBLayer;
import uk.ac.ebi.rcloud.rpf.db.DBLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.data.UserDataDB;
import interruptiblermi.InterruptibleRMIThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.http.util.EmailNotification;
import uk.ac.ebi.rcloud.util.HexUtil;


public class CommandServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    final private Logger log = LoggerFactory.getLogger(getClass());

	private static final int RMICALL_TIMEOUT_MILLISEC = 1000 * 60 * 60 * 24 * 7;

	public CommandServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //Thread.currentThread().
        try {
            doAny(request, response);
        } catch (Exception ex) {
            log.error("Unexpected Exception ", ex);
        }
	}

	class ThreadsHolder {
		private transient Vector<Thread> _threads = new Vector<Thread>();
		Vector<Thread> getThreads() {
			return _threads;
		}
	}

	class StopHolder {
        private boolean stopRequired = false;
        public boolean isStopRequired() {
            return stopRequired;
        }
        public void setStopRequired(boolean stopRequired) {
            this.stopRequired = stopRequired;
        }
	}

	private void saveSessionAttributes(HttpSession session) {
		((HashMap<String, HashMap<String, Object>>)
                getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP")).
                put(session.getId(), cloneAttributes(session));
	}

	protected HashMap<String, Object> getRequestParameters(HttpServletRequest request) {
        return (HashMap<String, Object>) HexUtil.hexToObject(request.getParameter("parameters"));
    }


    private String getOneLDAPAttribute(Attributes attrs, String name, String defaultValue) {
        try {
            Attribute a = attrs.get(name);
            return a == null ? (defaultValue) : a.get().toString();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = null;
		Object result = null;

		try {
			final String command = request.getParameter("method");
			do {

				if (command.equals("ping")) {
					result="pong";
					break;					
                } else if (command.equals("authenticate-user")) {

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    String login = (String) parameters.get("username");
                    String passw = (String) parameters.get("password");

                    UserDataDB user = getDBLayer().getUser(login);

                    if (user == null || !user.getPwd().equals(DETools.cipherString(passw))) {
                        result = new BadLoginPasswordException();
                        break;
                    }

                    result = new Boolean(true);

                    break;


				} else if (command.equals("login-user")) {

                    boolean ldap_auth_enabled = "true".equalsIgnoreCase(System.getProperty("ldap.auth.enabled"));

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    String login = (String) parameters.get("username");
                    String passw = (String) parameters.get("password");

                    UserDataDB user = getDBLayer().getUser(login);

                    if (user == null) {
                        if (ldap_auth_enabled) {
                            //
                            //
                            LdapKit.TraditionalPersonDaoImpl i = new LdapKit.TraditionalPersonDaoImpl();

                            boolean ldap_user_authenticated = i.authenticateUserAccount(login, passw);

                            if (ldap_user_authenticated) {
                                String USER_DEFAULT_POOL = "USER_DEFAULT_POOL";
                                String USER_DEFAULT_FOLDER = "USER_DEFAULT_FOLDER";

                                //
                                //
                                String defaultpool = getDBLayer().getOption(USER_DEFAULT_POOL).getOptionValue();
                                String defaultfolder = getDBLayer().getOption(USER_DEFAULT_FOLDER).getOptionValue();

                                List<Attributes> list = i.getPersonByUid(login);

                                if (list.size() > 1) {
                                    // too many users under the same uid
                                    // need to manage the uid uniqueness..
                                }

                                if (list.size() == 0) {
                                    // cant get user data
                                    //
                                    result = new BadLoginPasswordException();
                                    break;
                                }

                                Attributes attr0 = list.get(0);

                                HashMap<String, Object> map = new HashMap<String, Object>();

                                map.put(UserDataDB.LOGIN,       login);
                                map.put(UserDataDB.PWD,         passw);
                                map.put(UserDataDB.STATUS,      UserDataDB.USER_OFFLINE);
                                map.put(UserDataDB.USERFOLDER,  defaultfolder + "/" + login);
                                map.put(UserDataDB.FULLNAME,    getOneLDAPAttribute(attr0, "cn", "default"));
                                map.put(UserDataDB.EMAIL,       getOneLDAPAttribute(attr0, "mail", "rcloud@ebi.ac.uk"));
                                map.put(UserDataDB.PROFILE,     getOneLDAPAttribute(attr0, "profile", "default"));
                                map.put(UserDataDB.POOLNAME,    defaultpool);

                                user = new UserDataDB( map );

                                // create db record
                                getDBLayer().createUser(user);

                                EmailNotification.sendRegistrationNotification(user);

                            } else {
                                result = new BadLoginPasswordException();
                                break;
                            }
                        } else {
                            result = new BadLoginPasswordException();
                            break;
                        }
                    } else {
                        if (!user.getPwd().equals(DETools.cipherString(passw))) {
                            result = new BadLoginPasswordException();
                            break;
                        }
                    }

                    ((HashMap<String, UserDataDB>) getServletContext().
                            getAttribute("USERMAP")).put(login, user);

                    getDBLayer().updateUserLoggedIn(user.getLogin());

                    result = new Boolean(true);

                    break;

                } else if (command.equals("logout-user")) {

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    String login = (String) parameters.get("username");

                    ((HashMap<String, UserDataDB>) getServletContext().
                            getAttribute("USERMAP")).remove(login);

                    getDBLayer().updateUserLoggedOut(login);

                    result = "OK";
                    break;

                } else if (command.equals("register-user")) {

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    UserDataDB user = (UserDataDB) parameters.get("user");

                    // create db record
                    getDBLayer().createUser(user);

                    EmailNotification.sendRegistrationNotification(user);

                    ((HashMap<String, UserDataDB>) getServletContext().
                            getAttribute("USERMAP")).put(user.getLogin(), user);

                    getDBLayer().updateUserLoggedIn(user.getLogin());

                    result = "OK";

                    break;
				} else if (command.equals("restore-user")) {

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    String email = (String) parameters.get("email");

                    Vector<UserDataDB> users = getDBLayer().getUserData("EMAIL="+ SqlBase.wrap(email));

                    if (users != null && !users.isEmpty()){
                        EmailNotification.sendRestorationNotification(users);
                    }

                    break;

                } else if (command.equals("dbinvoke")) {

                    String objectName = (String) HexUtil.hexToObject(request.getParameter("objectname"));

                    Object obj = getServletContext().getAttribute(objectName);

                    if (obj == null) {
                        throw new Exception("Bad Servlet Object :" + objectName);
                    }
                    result = invoke(obj, request,
                            (ThreadsHolder) getServletContext().getAttribute("THREADS"),
                            new StopHolder());

                    break;

				} else if (command.equals("checkserver")) {

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    String serverName = (String) parameters.get("server");

                    try {
                        result = ((RServices) getDBLayer().lookup(serverName)).ping();

                    } catch (Exception ex) {
                        log.error("Error!", ex);
                        result = ex;
                    }

                    break;

				} else if (command.equals("openproject")) {

                    session = request.getSession(false);

					if (session != null) {
                        session.invalidate();

                        //RServices r = (RServices) session.getAttribute("R");
                        //if (r != null) {
                        //    result = session.getId();
                        //    break;
                        //}
					}

                    HashMap<String, Object> parameters = getRequestParameters(request);

                    UserDataDB user0 = (UserDataDB) parameters.get("user");
                    ProjectDataDB project = (ProjectDataDB) parameters.get("project");
                    //;

                    if (user0 == null) {
                        throw new RuntimeException("user is null");
                    }

                    if (project == null) {
                        throw new RuntimeException("project is null");
                    }

                    String poolname = user0.getPoolname();

                    boolean wait = parameters.keySet().contains("wait") &&
                            ((String) parameters.get("wait")).equalsIgnoreCase("true");

                    UserDataDB user = ((HashMap<String, UserDataDB>)
                            getServletContext().getAttribute("USERMAP")).get(user0.getLogin());

                    if (user == null) {
                        result = new NotLoggedInException(); break;
                    }

                    String serverName = "";

                    Vector<ServerDataDB> servers = getDBLayer().getServerData("PING_FAILURES<" +
                            PoolUtils.PING_FAILURES_NBR_MAX + "AND PROJECT= " + SqlBase.wrap(project.getIdentifier()));

                    if (servers != null && servers.size() > 0) {
                        serverName = servers.get(0).getName();

                        if (servers.size() > 1) {
                            log.error("Error! ", new ProjectDBInconsistencyException());
                        }
                    }

                    boolean namedAccessMode = serverName.length() > 0;

					RPFSessionInfo.get().put("USER", user);
					RPFSessionInfo.get().put("REMOTE_ADDR", request.getRemoteAddr());
					RPFSessionInfo.get().put("REMOTE_HOST", request.getRemoteHost());

                    boolean save = true;

					RServices r = null;

                    if (namedAccessMode) {
                        try {
                            r = (RServices) getDBLayer().lookup(serverName);
                        } catch (Exception e) {
                            log.error("Error!", e);
                        }
                    } else {
                        ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

                        if (spFactory == null) {
                            result = new NoRegistryAvailableException();
                            break;
                        }

                        if (wait) {
                            r = (RServices) (poolname==null || poolname.trim().equals("")?
                                    spFactory.getServantProvider().borrowServantProxy() :
                                    spFactory.getServantProvider().borrowServantProxy(poolname));
                        } else {
                            r = (RServices) (poolname==null || poolname.trim().equals("")?
                                    spFactory.getServantProvider().borrowServantProxyNoWait():
                                    spFactory.getServantProvider().borrowServantProxyNoWait(poolname));
                        }
                    }

					if (r == null) {
						result = new NoServantAvailableException();
						break;
					}

					session = request.getSession(true);

                    /*
					Integer sessionTimeOut=null;
					try {
						if (options.get("sessiontimeout")!=null)
						sessionTimeOut = Integer.decode((String)options.get("sessiontimeout"));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
					
					if (sessionTimeOut!=null) {
						session.setMaxInactiveInterval(sessionTimeOut);
					}
					*/

    				session.setAttribute("TYPE", "RS");
					session.setAttribute("R", r);
					session.setAttribute("SERVERNAME", r.getServantName());
                    session.setAttribute("USER", user);
                    session.setAttribute("PROJECT", project);
                    session.setAttribute("LOGOFFMODE", "suspend");
                    session.setAttribute("STOPHOLDER", new StopHolder());
                    session.setAttribute("THREADS", new ThreadsHolder());
                    session.setAttribute("SAVE", save);

					((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).put(session.getId(), session);
					saveSessionAttributes(session);

					Vector<HttpSession> sessionVector = ((HashMap<RServices, Vector<HttpSession>>) getServletContext().getAttribute("R_SESSIONS")).get(r);
					if (sessionVector == null) {
						sessionVector = new Vector<HttpSession>();
						((HashMap<RServices, Vector<HttpSession>>) getServletContext().getAttribute("R_SESSIONS")).put(r, sessionVector);
					}

					sessionVector.add(session);

                    if (project != null) {
                        getDBLayer().updateProjectOpened(project.getIdentifier());
                    }

                    r.setOwner(user.getLogin());

                    if (!namedAccessMode) {
                        // a new server is being taken
                        if (project != null) {

                            r.setProject(project.getIdentifier());
                            r.setWorkingDirectory(project.getAbsolutePath());
                        }

                        // make sure User lib folder is created
                        r.createRandomAccessDir(user.getUserLibFolder());
                        r.evaluate("try({ .libPaths(c(\""+user.getUserLibFolder()+"\",.libPaths())) })");
                    }


                    log.info("---> Has Collaboration Listeners:" + r.hasRCollaborationListeners());

                    log.info("---> caching Graphic Devices");

                    GDDevice[] devices = r.listDevices();
                    for (int i = 0; i < devices.length; ++i) {
                        String deviceName = devices[i].getId();
                        log.info("  - graphic device = " + deviceName);
                        session.setAttribute(deviceName, devices[i]);
                    }

                    log.info("---> caching Callback Devices");

                    GenericCallbackDevice[] callbackdevices = r.listGenericCallbackDevices();
                    for (int i = 0; i < callbackdevices.length; ++i) {
                        String deviceName = callbackdevices[i].getId();
                        log.info("  - callback device = " + deviceName);
                        session.setAttribute(deviceName, callbackdevices[i]);
                    }

					result = session.getId();

					break;

                } else if (command.equals("sendmessage")) {

                    String senderName = (String) HexUtil.
                            hexToObject(request.getParameter("senderName"));

                    String senderEmail = (String) HexUtil.
                            hexToObject(request.getParameter("senderEmail"));

                    String subject = (String) HexUtil.
                            hexToObject(request.getParameter("subject"));
                    String message = (String) HexUtil.
                            hexToObject(request.getParameter("message"));
                    String version = (String) HexUtil.
                            hexToObject(request.getParameter("version"));

                    EmailNotification.sendMessageToDevelopment(senderName, senderEmail,
                            subject, message, version);

                    break;
                } else if (command.equals("sendbugreport")) {

                    String senderName = (String) HexUtil.
                            hexToObject(request.getParameter("senderName"));

                    String senderEmail = (String) HexUtil.
                            hexToObject(request.getParameter("senderEmail"));

                    String subject = (String) HexUtil.
                            hexToObject(request.getParameter("subject"));
                    String description = (String) HexUtil.
                            hexToObject(request.getParameter("description"));
                    String category = (String) HexUtil.
                            hexToObject(request.getParameter("category"));
                    String version = (String) HexUtil.
                            hexToObject(request.getParameter("version"));

                    EmailNotification.sendBugReport(senderName, senderEmail, subject,
                            description, category, version);

                    break;
                } else if (command.equals("save-screen")) {

                    String location = (String) HexUtil.
                            hexToObject(request.getParameter("location"));
                    BufferedImage image = (BufferedImage) HexUtil.
                            hexToObject(request.getParameter("image"));

                    //EmailNotification.sendMessageToDevelopment(sender,
                    //        subject, message, version);

                    break;
                } else if (command.equals("load-screen")) {

                    String location = (String) HexUtil.
                            hexToObject(request.getParameter("location"));

                    //EmailNotification.sendBugReport(sender, subject,
                    //        description, category, version);

                    break;
                }

				session = request.getSession(false);
				if (session == null) {
					result = new NotLoggedInException();
					break;
				}

				if (command.equals("suspend")) {

                    session.setAttribute("SAVE",
                            (Boolean) HexUtil.hexToObject(request.getParameter("save")));
                    session.setAttribute("LOGOFFMODE", "suspend");
                    saveSessionAttributes(session);

					try {
						session.invalidate();
					} catch (Exception ex) {
                        log.error("Error!", ex);
					}
					result = null;
					break;
				} else if (command.equals("shutdown")) {

                    session.setAttribute("SAVE", 
                            (Boolean) HexUtil.hexToObject(request.getParameter("save")));
                    session.setAttribute("LOGOFFMODE", "shutdown");
                    saveSessionAttributes(session);

                    try {
                        session.invalidate();

                    } catch (Exception ex) {
                        log.error("Error!", ex);
                    }
                    result = null;
                    break;
                } else if (command.equals("invoke")) {

                    String objectName = (String) HexUtil.hexToObject(request.getParameter("objectname"));

                    Object obj = session.getAttribute(objectName);
                    if (obj == null) {
                        throw new Exception("Bad Session Object :" + objectName);
                    }

                    result = invoke(obj, request,
                            (ThreadsHolder) session.getAttribute("THREADS"),
                            (StopHolder) session.getAttribute("STOPHOLDER"));


                    if (result instanceof ConnectException) {
                        session.invalidate();
                        result = new NotLoggedInException();
                    }

					break;
				} else if (command.equals("interrupt")) {
					final Vector<Thread> tvec = (Vector<Thread>)
                            ((ThreadsHolder) session.getAttribute("THREADS")).getThreads().clone();

					for (int i = 0; i < tvec.size(); ++i) {
						try {
							tvec.elementAt(i).interrupt();
						} catch (Exception e) {
                            log.error("Error!", e);
						}
					}

                    ((StopHolder) session.getAttribute("STOPHOLDER")).setStopRequired(true);
					((ThreadsHolder) session.getAttribute("THREADS")).getThreads().removeAllElements();

					result = null;
					break;

				} else if (command.equals("saveimage")) {
                    ProjectDataDB project = (ProjectDataDB) session.getAttribute("PROJECT");

                    RServices rservices = (RServices) session.getAttribute("R");

                    rservices.saveWorkspace(project.getAbsolutePath());

					result = null;
					break;

				} else if (command.equals("loadimage")) {
                    ProjectDataDB project = (ProjectDataDB) session.getAttribute("PROJECT");

                    RServices rservices = (RServices) session.getAttribute("R");

                    rservices.loadWorkspace(project.getAbsolutePath());

					result = null;
					break;

				} else if (command.equals("newdevice")) {
                    boolean broadcasted = new Boolean(request.getParameter("broadcasted"));
                    GDDevice deviceProxy = null;

                    if (broadcasted) {
                        deviceProxy = ((RServices) session.getAttribute("R")).newBroadcastedDevice(
                                Integer.decode(request.getParameter("width")),
                                Integer.decode(request.getParameter("height")));
                    } else {
                        deviceProxy = ((RServices) session.getAttribute("R")).newDevice(
                                Integer.decode(request.getParameter("width")),
                                Integer.decode(request.getParameter("height")));
                    }

                    String deviceName = deviceProxy.getId();

                    log.info("deviceName=" + deviceName);
                    session.setAttribute(deviceName, deviceProxy);

                    saveSessionAttributes(session);

                    result = deviceName;
                    break;


                } else if (command.equals("adddevicefeed")) {
                    String deviceName = request.getParameter("devicename");

                    ArrayBlockingQueue<Vector<GDObject>> gdObjectQueue =
                            new ArrayBlockingQueue<Vector<GDObject>>(500);

                    GDObjectListenerImpl objListener = new GDObjectListenerImpl(gdObjectQueue);
                    GDObjectProviderImpl objPopper   = new GDObjectProviderImpl(gdObjectQueue, 20000);

                    GDDevice device =
                            (GDDevice) session.getAttribute(deviceName);

                    device.addGraphicListener(objListener);

                    session.setAttribute(deviceName + "_objQueue", gdObjectQueue);
                    session.setAttribute(deviceName + "_objListener", objListener);
                    session.setAttribute(deviceName + "_objPopper", objPopper);

                    saveSessionAttributes(session);
                    
                    result = deviceName + "_objPopper";
                    break;

                } else if (command.equals("removedevicefeed")) {
                    String deviceName = request.getParameter("devicename");

                    GDObjectListenerImpl listener =
                            (GDObjectListenerImpl) session.getAttribute(deviceName + "_objListener");

                    GDDevice device =
                            (GDDevice) session.getAttribute(deviceName);

                    device.removeGraphicListener(listener);

                    ArrayBlockingQueue queue = (ArrayBlockingQueue)
                            session.getAttribute(deviceName + "_objQueue");

                    queue.clear();

                    session.removeAttribute(deviceName + "_objQueue");
                    session.removeAttribute(deviceName + "_objListener");
                    session.removeAttribute(deviceName + "_objPopper");

                    saveSessionAttributes(session);

                    result = "ok";
                    break;

				} else if (command.equals("listdevices")) {
                    result = new Vector<String>();
                    for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
                        String attributeName = e.nextElement();
                        if (attributeName.startsWith("device_")) {
                            ((Vector<String>) result).add(attributeName);
                        }
                    }

                    break;

                } else if (command.equals("newcallbackdevice")) {
                    GenericCallbackDevice genericCallBackDevice =
                            ((RServices) session.getAttribute("R")).newGenericCallbackDevice();

                    String genericCallBackDeviceName = genericCallBackDevice.getId();

                    session.setAttribute(genericCallBackDeviceName, genericCallBackDevice);

                    saveSessionAttributes(session);

                    result = genericCallBackDeviceName;

                    break;

                } else if (command.equals("addcallbackfeed")) {

                    String callbackDeviceName = request.getParameter("callbackdevicename");

                    ArrayBlockingQueue<Vector<RAction>> actionQueue = new ArrayBlockingQueue<Vector<RAction>>(500);
                    GenericRActionListenerImpl rListener   = new GenericRActionListenerImpl(actionQueue);
                    GenericRActionProviderImpl popper      = new GenericRActionProviderImpl(actionQueue, 20000);

                    GenericCallbackDevice callbackdevice =
                            (GenericCallbackDevice) session.getAttribute(callbackDeviceName);

                    callbackdevice.addListener(rListener);

                    session.setAttribute(callbackDeviceName + "_actQueue", actionQueue);
                    session.setAttribute(callbackDeviceName + "_actListener", rListener);
                    session.setAttribute(callbackDeviceName + "_actPopper", popper);

                    saveSessionAttributes(session);

                    result = callbackDeviceName + "_actPopper";
                    break;

                } else if (command.equals("removecallbackfeed")) {

                    String callbackDeviceName = request.getParameter("callbackdevicename");

                    GenericRActionListenerImpl listener =
                            (GenericRActionListenerImpl) session.getAttribute(callbackDeviceName + "_actListener");

                    GenericCallbackDevice callbackdevice =
                            (GenericCallbackDevice) session.getAttribute(callbackDeviceName);

                    callbackdevice.addListener(listener);

                    ArrayBlockingQueue queue = (ArrayBlockingQueue)
                            session.getAttribute(callbackDeviceName + "_actQueue");

                    queue.clear();

                    session.removeAttribute(callbackDeviceName + "_actjQueue");
                    session.removeAttribute(callbackDeviceName + "_actListener");
                    session.removeAttribute(callbackDeviceName + "_actPopper");

                    saveSessionAttributes(session);

                    result = "ok";
                    break;


                } else if (command.equals("listcallbackdevices")) {
                    result = new Vector<String>();

                    for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
                        String attributeName = e.nextElement();
                        if (attributeName.startsWith(GenericCallbackDevice.GenericCallbackDeviceIdPrefix)) {
                            ((Vector<String>) result).add(attributeName);
                        }
                    }

                    break;
				}

			} while (true);

		} catch (TunnelingException te) {

            log.info("tunnelling exception", te);

            if (te.getCause() instanceof ConnectException && session != null) {
                session.invalidate();
                result = new NotLoggedInException();
            } else {
                result = te;
            }

            log.info("result1=" + (result != null ? result.toString() : "null"));

        } catch (Throwable e) {
			result = new TunnelingException("Server Side", e);
            log.error("Error!", e);
		}

        response.setContentType("application/x-java-serialized-object");
		new ObjectOutputStream(response.getOutputStream()).writeObject(result);
		response.flushBuffer();

	}

    private DBLayerInterface getDBLayer() {
        return (DBLayerInterface) getServletContext().getAttribute("DBLAYER");
    }

    private DAOLayerInterface getDAOLayer() {
        return (DAOLayerInterface) getServletContext().getAttribute("DAOLAYER");
    }

    class RmiRunnable implements Runnable {
        volatile private Object resultHolder;
        volatile private Thread currentThread;
        volatile private Method method;
        volatile private Object[] methodParams;
        volatile private Object object;
        volatile private boolean completed = false;

        public void run() {
            try {

                Object result = method.invoke(object, methodParams);
                setResultHolder(result);

            } catch (InvocationTargetException ite) {
                setResultHolder(ite.getCause());

            } catch (Exception e) {
                final boolean wasInterrupted = Thread.interrupted();
                if (wasInterrupted) {
                    setResultHolder(new RmiCallInterrupted());
                } else {
                    setResultHolder(e);
                }
            } finally {
                setCompleted(true);
                currentThread.interrupt();
            }

            // sleep for a bit
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }
        }

        public void setResultHolder(Object resultHolder) {
            this.resultHolder = resultHolder;
            //log.info("object=" + object.toString() + " resultHolder="
            //        + (resultHolder != null ? resultHolder.toString() : "null"));
        }

        public void setCurrentThread(Thread currentThread) {
            this.currentThread = currentThread;
            //log.info("currentThread=" + currentThread.toString());
        }

        public void setMethod(Method method) {
            this.method = method;
            //log.info("method=" + method.toString());
        }

        public void setMethodParams(Object[] methodParams) {
            this.methodParams = methodParams;
            //log.info("methodParams=" + (methodParams != null ? methodParams.toString() : "null"));
        }

        public void setObject(Object object) {
            this.object = object;
            //log.info("object=" + object.toString());
        }

        public Object getResultHolder() {
            return resultHolder;
        }

        public Thread getCurrentThread() {
            return currentThread;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getMethodParams() {
            return methodParams;
        }

        public Object getObject() {
            return object;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
    
    private Object invoke(Object obj, HttpServletRequest request,
                          ThreadsHolder threadHolder, StopHolder stopHolder) throws Throwable {

        RmiRunnable rmiRunnable = new RmiRunnable();


        String methodname = (String) HexUtil.hexToObject(request.getParameter("methodname"));
        Class<?>[] signature = (Class[]) HexUtil.hexToObject(request.getParameter("methodsignature"));

        Method method = obj.getClass().getMethod(methodname, signature);

        if (method == null) {
            throw new Exception("Bad Method Name :" + methodname);
        }

        Object[] params = (Object[]) HexUtil.hexToObject(request.getParameter("methodparameters"));

        rmiRunnable.setMethodParams(params);
        rmiRunnable.setMethod(method);
        rmiRunnable.setObject(obj);
        rmiRunnable.setCurrentThread(Thread.currentThread());

        Thread rmiThread = InterruptibleRMIThreadFactory.getInstance().newThread(rmiRunnable);
        threadHolder.getThreads().add(rmiThread);

        long t1 = System.currentTimeMillis();

        rmiThread.start();

        while (!rmiRunnable.isCompleted()) {

            //log.info("waiting rmiRunnable.getResultHolder()=" +
            //        (rmiRunnable.getResultHolder() != null ? rmiRunnable.getResultHolder().toString() : "null"));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                //log.info("interrupted, rmiRunnable.isCompleted()="+ rmiRunnable.isCompleted() +
                //        " rmiRunnable.getResultHolder()=" +
                //        (rmiRunnable.getResultHolder() != null ? rmiRunnable.getResultHolder().toString() : "null"));

                //break;
            }

            if ((System.currentTimeMillis() - t1) >
                    RMICALL_TIMEOUT_MILLISEC || stopHolder.isStopRequired()) {

                //log.info("interrupting rmi thread");

                rmiRunnable.setResultHolder(new RmiCallTimeout());
                rmiRunnable.setCompleted(true);
                rmiThread.interrupt();
                break;
            }
        }

        //log.info("rmiRunnable.isCompleted()="+rmiRunnable.isCompleted()+
        //        " rmiRunnable.getResultHolder()=" +
        //        (rmiRunnable.getResultHolder() != null ? rmiRunnable.getResultHolder().toString() : "null"));

        try {
            threadHolder.getThreads().remove(rmiThread);
        } catch (IllegalStateException e) {
        }

        if (rmiRunnable.getResultHolder() instanceof Throwable) {
            throw (Throwable) rmiRunnable.getResultHolder();
        }

        //log.info("returning rmiRunnable.getResultHolder()=" +
        //        (rmiRunnable.getResultHolder() != null ? rmiRunnable.getResultHolder().toString() : "null"));

        return rmiRunnable.getResultHolder();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            doAny(request, response);
        } catch (Exception ex) {
            log.error("Unexpected exception ", ex);
        }
	}

	public void init(ServletConfig sConfig) throws ServletException {
		super.init(sConfig);
		log.info("command servlet init - fixed");

        PoolUtils.injectSystemProperties(true); //ServerDefaults.init();

        DBLayerInterface dbLayer = null;
        DAOLayerInterface daoLayer = null;
        try {
             dbLayer = (DBLayer) ServerDefaults.getRmiRegistry();
             daoLayer = new DAOLayer(dbLayer);
        } catch (Exception ex) {
            log.error("Error! ", ex);
        }

		PoolUtils.initRmiSocketFactory();
		getServletContext().setAttribute("SESSIONS_MAP", new HashMap<String, HttpSession>());
		getServletContext().setAttribute("SESSIONS_ATTRIBUTES_MAP", new HashMap<String, HashMap<String, Object>>());
		getServletContext().setAttribute("R_SESSIONS", new HashMap<RServices, HttpSession>());
        getServletContext().setAttribute("THREADS", new ThreadsHolder());
        getServletContext().setAttribute("STOPHOLDER", new StopHolder());
        getServletContext().setAttribute("DBLAYER", dbLayer);
        getServletContext().setAttribute("DAOLAYER", daoLayer);
        getServletContext().setAttribute("USERMAP", new HashMap<String, UserDataDB>());


		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}
	}

	HashMap<String, Object> cloneAttributes(HttpSession session) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result.put(name, session.getAttribute(name));
		}

		return result;
	}

    public static void main(String[] args) {
    }

}