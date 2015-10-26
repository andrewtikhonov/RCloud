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
package uk.ac.ebi.rcloud.http.proxy;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import uk.ac.ebi.rcloud.http.awareness.ConnectivityCallbacks;
import uk.ac.ebi.rcloud.http.exception.ConnectionFailedException;
import uk.ac.ebi.rcloud.http.exception.NotLoggedInException;
import uk.ac.ebi.rcloud.http.exception.TunnelingException;
import uk.ac.ebi.rcloud.server.callback.GenericRActionListener;
import uk.ac.ebi.rcloud.server.callback.RAction;
import uk.ac.ebi.rcloud.server.callback.GenericRActionProvider;
import uk.ac.ebi.rcloud.server.callback.GenericCallbackDevice;
import uk.ac.ebi.rcloud.server.RServices;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.graphics.GDObjectProvider;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.util.HexUtil;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 14, 2011
 * Time: 3:10:26 PM
 * To change this template use File | Settings | File Templates.
 */

public class ServerRuntimeImpl implements ServerRuntime {

    final private static Logger log = LoggerFactory.getLogger(ServerRuntimeImpl.class);
             
    public static final String FAKE_SESSION = getRandomSession();
	
    private static ProxySettings proxySettings = null;     // detectProxySettings();
    private static ProxyParameters proxyParameters = null; // createProxyParameters(proxySettings)
    private static HttpClient mainHttpClient = createHttpClient(new MultiThreadedHttpConnectionManager());

    public static ProxySettings getProxySettings() {
        return proxySettings;
    }

    public static ProxyParameters getProxyParameters() {
        return proxyParameters;
    }

    public static ProxyParameters createProxyParameters(ProxySettings settings) {

        if (settings != null) {
            ProxyParameters params = new ProxyParameters();
            params.authScope = new AuthScope(settings.PROXY_HOST, settings.PROXY_PORT);
            params.credentials = new UsernamePasswordCredentials(settings.USERNAME, settings.PASSWORD);

            return params;

        } else {

            return null;
        }
    }

    public static void detectAndApplyProxySettings() {
        applyProxySettings(detectProxySettings());
    }

    public static void applyProxySettings(ProxySettings settings) {

        proxySettings = settings;

        proxyParameters = createProxyParameters(proxySettings);

        mainHttpClient = createHttpClient(new MultiThreadedHttpConnectionManager());
    }

    private static HttpClient createHttpClient(HttpConnectionManager manager) {
        //log.info("createHttpClient");

        HttpClient httpClient = new HttpClient(manager);

        if (proxySettings != null) {
            //log.info("createHttpClient-proxySettings");

            httpClient.getHostConfiguration().setProxy(proxySettings.PROXY_HOST, proxySettings.PROXY_PORT);
            httpClient.getState().setProxyCredentials(proxyParameters.authScope, proxyParameters.credentials);
        }

        return httpClient;
    }

    public static ProxySettings detectProxySettings() {
        
        ProxySettings settings = new ProxySettings();

        try {

            System.setProperty("java.net.useSystemProxies","true");

            java.util.List l = ProxySelector.getDefault().
                    select(new java.net.URI("http://google.com"));

            for (Iterator iter = l.iterator(); iter.hasNext(); ) {

                java.net.Proxy proxy = (java.net.Proxy) iter.next();

                InetSocketAddress addr = (InetSocketAddress)
                    proxy.address();

                if(addr == null) {

                    return null;

                } else {

                    settings.PROXY_HOST = addr.getHostName();
                    settings.PROXY_PORT = addr.getPort();

                    //if (settings != null) {
                    //    log.info("DETECTED : " + settings.toSring());
                    //}

                    return settings;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // GET-based methods
    //
    //
    private static Object sendGetToCommandServlet0(ServerSession session, String query,
                                                   HttpClient httpClient) throws TunnelingException {


        GetMethod getMethod = null;
        try {
            Object result = null;
            getMethod = new GetMethod(session.getCommandurl() + query);
            try {
                String sessionId = session.getSessionid();

                if (sessionId != null && !sessionId.equals("")) {
                    getMethod.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
                    getMethod.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
                }


                httpClient.executeMethod(getMethod);

                if (getMethod.getStatusCode() == HttpStatus.SC_OK) {
                    //
                    result = new ObjectInputStream(getMethod.getResponseBodyAsStream()).readObject();
                }
            } catch (ConnectException e) {
                throw new ConnectionFailedException();
            } catch (Exception e) {
                throw new TunnelingException("", e);
            }
            if (result != null && result instanceof TunnelingException) {
                throw (TunnelingException) result;
            }
            return result;
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    private static Object sendGetToCommandServlet(ServerSession session, String query) throws TunnelingException {
        return sendGetToCommandServlet0(session, query, mainHttpClient);
    }

    public static boolean isConnectionAlive(ServerSession session) throws TunnelingException {
        Object result = sendGetToCommandServlet(session, "?method=ping");

        return result.equals("pong");
    }

    public static boolean isServerAlive(ServerSession session,
                                        HashMap<String, Object> parameters) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=checkserver"+
                "&parameters="+ HexUtil.objectToHex(parameters));

        return result.equals("pong");
    }


    public static boolean loginUser(ServerSession session,
                                 HashMap<String, Object> parameters) throws TunnelingException {
        Object result = sendGetToCommandServlet(session, "?method=login-user" +
                "&parameters="+ HexUtil.objectToHex(parameters));

        if (result == null) {
            return false;
        }

        return (Boolean) result;
    }

    public static boolean authenticateUser(ServerSession session,
                                 HashMap<String, Object> parameters) throws TunnelingException {
        Object result = sendGetToCommandServlet(session, "?method=authenticate-user" +
                "&parameters="+ HexUtil.objectToHex(parameters));

        if (result == null) {
            return false;
        }

        return (Boolean) result;
    }

    public static void logoutUser(ServerSession session,
                                  HashMap<String, Object> parameters) throws TunnelingException {

        sendGetToCommandServlet(session, "?method=logout-user" +
                "&parameters="+ HexUtil.objectToHex(parameters));
    }

    public static void registerUser(ServerSession session,
                                 HashMap<String, Object> parameters) throws TunnelingException {

        sendGetToCommandServlet(session, "?method=register-user" +
                "&parameters="+ HexUtil.objectToHex(parameters));
    }

    public static void restoreUser(ServerSession session,
                                 HashMap<String, Object> parameters) throws TunnelingException {

        sendGetToCommandServlet(session, "?method=restore-user" +
                "&parameters="+ HexUtil.objectToHex(parameters));
    }

	public static String openProject(ServerSession session,
                                     HashMap<String, Object> parameters) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=openproject" +
                "&parameters="+ HexUtil.objectToHex(parameters));

        return (String) result;
	}

	public static void suspendProject(ServerSession session, boolean save) throws TunnelingException {
        sendGetToCommandServlet(session, "?method=suspend" +
                "&save=" + HexUtil.objectToHex(save));
    }

    public static void shutdownProject(ServerSession session, boolean save) throws TunnelingException {
        sendGetToCommandServlet(session, "?method=shutdown" +
                "&save=" + HexUtil.objectToHex(save));
    }

    public static void interrupt(ServerSession session) throws TunnelingException {
        sendGetToCommandServlet(session, "?method=interrupt");
    }

    // Graphic Device
    //
    //
    static class GDDeviceInvocationHandler implements InvocationHandler {

        private ServerSession session;
        private String deviceName;
        private HttpClient httpClient;
        private ConnectivityCallbacks callbacks;

        private Vector<GDObjectListener> listenerList = new Vector<GDObjectListener>();
        private Vector<GDObjectProvider> providerList = new Vector<GDObjectProvider>();

        private Thread popGDObjectsThread = null;
        private boolean _stopThreads = false;

        class PopGDObjectsThreadRunnable implements Runnable {
            public void run() {
                while (!_stopThreads) {
                    popActions();

                    Thread.yield();
                }
            }
        }

        public GDDeviceInvocationHandler(ServerSession session, String deviceName,
                                         HttpClient httpClient, ConnectivityCallbacks callbacks) {
            this.session = session;
            this.deviceName = deviceName;
            this.httpClient = httpClient;
            this.callbacks = callbacks;

            try {
                popGDObjectsThread = new Thread(new PopGDObjectsThreadRunnable());
                popGDObjectsThread.start();
            } catch (Exception e) {
                log.error("Error!", e);
            }
        }

        private void popActions() {
            if (providerList.size() == 0) {
                // wait longer if provider/listener
                // list is empty
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ie) {}
            }

            if (_stopThreads) return;

            if (providerList.size() > 0) {
                for (int i=0;i<providerList.size();i++) {
                    int size = providerList.size();

                    GDObjectListener listener = listenerList.elementAt(i);
                    GDObjectProvider provider = providerList.elementAt(i);

                    try {
                        Vector<GDObject> objects = provider.popObjects();

                        if (!_stopThreads && objects != null && objects.size() > 0) {
                            listener.pushObjects(objects);
                        }
                    } catch (Exception e) {
                        // silent cleanup
                        //log.info("ServerRuntimeImpl-GDDevice-cleanup");

                        log.info("graphic device cleanup");

                        if (size != providerList.size()) {
                            listenerList.removeElement(listener);
                            providerList.removeElement(provider);
                        } else {
                            listenerList.removeElementAt(i);
                            providerList.removeElementAt(i);
                        }
                    }
                }
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;

            //log.info("ServerRuntimeImpl-GDDevice-invoke-"+method.getName());

            if (method.getName().equals("addGraphicListener")) {

                if (providerList.size() > 0 || listenerList.size() > 0) {
                    throw new RuntimeException("pl.size()="+providerList.size()+
                            " ll.size()="+listenerList.size());
                }

                GDObjectProvider provider = addDeviceFeed(session, deviceName, httpClient);

                listenerList.addElement((GDObjectListener) args[0]);
                providerList.addElement((GDObjectProvider) provider);
                popGDObjectsThread.interrupt();

            } else if (method.getName().equals("removeGraphicListener")) {

                int index = listenerList.indexOf((GDObjectListener) args[0]);
                if (index != -1) {
                    listenerList.removeElementAt(index);
                    providerList.removeElementAt(index);
                }

            } else if (method.getName().equals("stopThreads")) {
                _stopThreads = true;
            } else {

                try {
                    result = ServerRuntimeImpl.invokeInternal(session, "invoke", deviceName, method.getName(),
                            method.getParameterTypes(), args, httpClient);

                } catch (NotLoggedInException nle) {
                    callbacks.handleServerNotAvailable(nle);

                } catch (TunnelingException te) {
                    callbacks.handleProxyNotAvailable(te);
                    throw te;
                }
            }

            return result;
        }

        private GDObjectProvider addDeviceFeed(ServerSession session,
                       String deviceName, HttpClient client) throws TunnelingException {

            Object result = sendGetToCommandServlet0(session, "?method=adddevicefeed" +
                    "&devicename="+deviceName, client);

            String feedName = (String) result;

            return (GDObjectProvider) ServerRuntimeImpl.getDefaultDynamicProxy(session, feedName,
                    new Class[] { GDObjectProvider.class },
                    createHttpClient(new MultiThreadedHttpConnectionManager()));
        }

        private void removeDeviceFeed(ServerSession session,
                       String deviceName, HttpClient client) throws TunnelingException {

            sendGetToCommandServlet0(session, "?method=removedevicefeed" +
                    "&devicename="+deviceName, client);
        }

        @Override
        protected void finalize() throws Throwable {
            //log.info("---> ServerRuntimeImpl-GDDevice-finalize()");

            super.finalize();
        }
    }


    private static GDDevice newGDDeviceProxy(ServerSession session,
                                             String deviceName, HttpClient httpClient,
                                             ConnectivityCallbacks callbacks) {

        //log.info("---> ServerRuntimeImpl-GDDevice-new()");

        InvocationHandler handler = new GDDeviceInvocationHandler(session,
                deviceName, httpClient, callbacks);

        Object proxy = Proxy.newProxyInstance(ServerRuntimeImpl.class.getClassLoader(),
                new Class<?>[] { GDDevice.class, HttpMarker.class }, handler);

        return (GDDevice) proxy;
    }

    private static GDDevice newDevice(ServerSession session,
                                      int width, int height, boolean broadcasted,
                                      ConnectivityCallbacks callbacks) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=newdevice" +
                "&width=" + width + "&height=" + height+
                "&broadcasted=" + broadcasted);

        String deviceName = (String) result;

        return newGDDeviceProxy(session, deviceName,
                createHttpClient(new MultiThreadedHttpConnectionManager()), callbacks);
    }

    private static GDDevice[] listDevices(ServerSession session,
                                          ConnectivityCallbacks callbacks) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=listdevices");

        Vector<String> deviceNames = (Vector<String>) result;

        GDDevice[] devices = new GDDevice[deviceNames.size()];

        for (int i = 0; i < deviceNames.size(); ++i) {
            devices[i] = newGDDeviceProxy(session, deviceNames.elementAt(i),
                    createHttpClient(new MultiThreadedHttpConnectionManager()), callbacks);
        }

        return devices;
    }

    // Callback Devices
    //
    //
    static class GenericCallbackDeviceInvocationHandler implements InvocationHandler {
        private ServerSession session;
        private String deviceName;
        private HttpClient httpClient;
        private ConnectivityCallbacks callbacks;

        private boolean _stopThreads = false;

        private Vector<GenericRActionListener> listenerList = new Vector<GenericRActionListener>();
        private Vector<GenericRActionProvider> providerList = new Vector<GenericRActionProvider>();
        private Thread popActionsThread = null;

        class PopActionsThreadRunnable implements Runnable {
            public void run() {
                while (!_stopThreads) {
                    popActions();

                    Thread.yield();
                }

            }
        }

        public GenericCallbackDeviceInvocationHandler(ServerSession session, String deviceName,
                                         HttpClient httpClient, ConnectivityCallbacks callbacks) {
            this.session = session;
            this.deviceName = deviceName;
            this.httpClient = httpClient;
            this.callbacks = callbacks;

            try {
                popActionsThread = new Thread(new PopActionsThreadRunnable());
                popActionsThread.start();
            } catch (Exception e) {
                log.error("Error!", e);
            }

        }

        private void popActions() {
            if (_stopThreads) return;

            if (providerList.size() == 0) {
                // wait longer if provider/listener
                // list is empty
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ie) {}
            }

            if (_stopThreads) return;

            if (providerList.size() > 0) {
                for (int i=0;i<providerList.size();i++) {

                    int size = providerList.size();

                    GenericRActionListener listener = listenerList.elementAt(i);
                    GenericRActionProvider provider = providerList.elementAt(i);

                    try {
                        Vector<RAction> actions = provider.popActions();

                        if (!_stopThreads && actions != null && actions.size() > 0) {
                            //log.info("actions.size()=" + actions.size());

                            listener.pushActions(actions);
                        }
                    } catch (Exception e) {

                        //log.error("Error!", e);
                        log.info("generic device cleanup");

                        if (size != providerList.size()) {
                            listenerList.removeElement(listener);
                            providerList.removeElement(provider);
                        } else {
                            listenerList.removeElementAt(i);
                            providerList.removeElementAt(i);
                        }
                    }
                }
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;

            //log.info("ServerRuntimeImpl-GenericCallbackDevice-invoke-"+method.getName());

            if (method.getName().equals("addListener")) {
                if (providerList.size() > 0 || listenerList.size() > 0) {
                    throw new RuntimeException("pl.size()="+providerList.size()+
                            " ll.size()="+listenerList.size());
                }

                GenericRActionProvider provider = addDeviceFeed(session, deviceName, httpClient);

                listenerList.addElement((GenericRActionListener) args[0]);
                providerList.addElement((GenericRActionProvider) provider);

                popActionsThread.interrupt();

            } else if (method.getName().equals("removeListener")) {

                int index = listenerList.indexOf((GenericRActionListener) args[0]);
                if (index != -1) {
                    listenerList.removeElementAt(index);
                    providerList.removeElementAt(index);
                }

            } else if (method.getName().equals("stopThreads")) {
                _stopThreads = true;

                //JOptionPaneExt.showExceptionDialog(null, new Exception());
            } else {
                try {
                    result = ServerRuntimeImpl.invokeInternal(session, "invoke", deviceName, method.getName(),
                            method.getParameterTypes(), args, httpClient);

                } catch (NotLoggedInException nle) {
                    callbacks.handleServerNotAvailable(nle);
                } catch (TunnelingException te) {
                    callbacks.handleProxyNotAvailable(te);
                    throw te;
                }
            }

            return result;
        }

        private GenericRActionProvider addDeviceFeed(ServerSession session,
                       String deviceName, HttpClient client) throws TunnelingException {

            Object result = sendGetToCommandServlet0(session, "?method=addcallbackfeed" +
                    "&callbackdevicename="+deviceName, client);

            String feedName = (String) result;

            return (GenericRActionProvider) ServerRuntimeImpl.getDefaultDynamicProxy(session, feedName,
                    new Class[] { GenericRActionProvider.class },
                    createHttpClient(new MultiThreadedHttpConnectionManager()));
        }

        private void removeDeviceFeed(ServerSession session,
                       String deviceName, HttpClient client) throws TunnelingException {

            sendGetToCommandServlet0(session, "?method=removecallbackfeed" +
                    "&callbackdevicename="+deviceName, client);
        }

        @Override
        protected void finalize() throws Throwable {
            //log.info("---> ServerRuntimeImpl-GenericCallbackDevice-finalize()");
            super.finalize();
        }
    }

    private static GenericCallbackDevice newCallbackDeviceProxy(ServerSession session,
                          String deviceName, HttpClient httpClient,
                          ConnectivityCallbacks callbacks) {

        //log.info("---> ServerRuntimeImpl-GenericCallbackDevice-new()");

        InvocationHandler handler = new GenericCallbackDeviceInvocationHandler(
                session, deviceName, httpClient, callbacks);

        Object proxy = Proxy.newProxyInstance(ServerRuntimeImpl.class.getClassLoader(),
                new Class<?>[] { GenericCallbackDevice.class, HttpMarker.class }, handler);

        return (GenericCallbackDevice) proxy;
    }

    private static GenericCallbackDevice newCallbackDevice(ServerSession session,
                          ConnectivityCallbacks callbacks) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=newcallbackdevice");

        String deviceName = (String) result;

        return newCallbackDeviceProxy(session, deviceName,
                createHttpClient(new MultiThreadedHttpConnectionManager()), callbacks);
    }

    private static GenericCallbackDevice[] listCallbackDevices(ServerSession session,
                                                  ConnectivityCallbacks callbacks) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=listcallbackdevices");

        Vector<String> deviceNames = (Vector<String>) result;

        for(String name : deviceNames) {
            log.info("GenericCallbackDevice - " + name);
        }

        GenericCallbackDevice[] devices = new GenericCallbackDevice[deviceNames.size()];

        for (int i = 0; i < deviceNames.size(); ++i) {
            devices[i] = newCallbackDeviceProxy(session, deviceNames.elementAt(i),
                    createHttpClient(new MultiThreadedHttpConnectionManager()), callbacks);
        }

        return devices;
    }
    

    public static void saveimage(ServerSession session) throws TunnelingException {

        sendGetToCommandServlet(session, "?method=saveimage");
    }

    public static void loadimage(ServerSession session) throws TunnelingException {

        sendGetToCommandServlet(session, "?method=loadimage");
    }

    public static String logOnDB(ServerSession session, String login, String pwd,
                                 HashMap<String, Object> options) throws TunnelingException {

        Object result = sendGetToCommandServlet(session, "?method=logondb&login=" +
                HexUtil.objectToHex(login) + "&pwd=" +
                HexUtil.objectToHex(pwd) + "&options="+
                HexUtil.objectToHex(options));

        return (String) result;
    }

    // POST-based methods
    //
    //

    public static Object sendPostToCommandServlet(ServerSession session, String query,
                                                  NameValuePair[] data) throws TunnelingException {

		PostMethod postPush = null;
		try {
			Object result = null;
			try {
				postPush = new PostMethod(session.getCommandurl() + query);
                postPush.setRequestBody(data);

                mainHttpClient.executeMethod(postPush);
				result = new ObjectInputStream(postPush.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("Client Side", e);
			}
			if (result != null && result instanceof TunnelingException) {
                log.error("Error!", new Exception());
				throw (TunnelingException) result;
			}
			return result;
		} finally {
			if (postPush != null) {
				postPush.releaseConnection();
			}
		}
	}

    public static Object sendMessage(ServerSession session, String senderName, String senderEmail, String subject,
                                     String message, String version) throws TunnelingException {

        return sendPostToCommandServlet(session, "?method=sendmessage",
                new NameValuePair[]{
                        new NameValuePair( "sendername", HexUtil.objectToHex(senderName) ),
                        new NameValuePair( "senderemail", HexUtil.objectToHex(senderEmail) ),
                        new NameValuePair( "subject", HexUtil.objectToHex(subject) ),
                        new NameValuePair( "message", HexUtil.objectToHex(message) ),
                        new NameValuePair( "version", HexUtil.objectToHex(version) )
                });
	}

    public static Object sendBugReport(ServerSession session, String senderName, String senderEmail,
                                       String subject, String message, String category,
                                       String version) throws TunnelingException {

        return sendPostToCommandServlet(session, "?method=sendbugreport",
                new NameValuePair[]{
                        new NameValuePair( "senderName", HexUtil.objectToHex(senderName) ),
                        new NameValuePair( "senderEmail", HexUtil.objectToHex(senderEmail) ),
                        new NameValuePair( "subject", HexUtil.objectToHex(subject) ),
                        new NameValuePair( "description", HexUtil.objectToHex(message) ),
                        new NameValuePair( "category", HexUtil.objectToHex(category) ),
                        new NameValuePair( "version", HexUtil.objectToHex(version) )
                });
	}

    public static Object saveScreen(ServerSession session, String location, BufferedImage image)
            throws TunnelingException {

        return sendPostToCommandServlet(session, "?method=save-screen",
                new NameValuePair[]{
                        new NameValuePair( "location", HexUtil.objectToHex(location) ),
                        new NameValuePair( "image", HexUtil.objectToHex(image) )
                });
	}

    public static BufferedImage loadScreen(ServerSession session, String location)
            throws TunnelingException {

        return (BufferedImage) sendPostToCommandServlet(session, "?method=load-screen",
                new NameValuePair[]{
                        new NameValuePair( "location", HexUtil.objectToHex(location) )
                });
	}

    private static Object invokeInternal(ServerSession session, String handler, String object,
                                String methodName, Class<?>[] methodSignature, Object[] methodParameters,
                                HttpClient httpClient) throws TunnelingException {

		PostMethod postPush = null;
		try {
			Object result = null;
			try {
				postPush = new PostMethod(session.getCommandurl() + "?method=" + handler);
				NameValuePair[] data = { new NameValuePair("objectname", HexUtil.objectToHex(object)),
						new NameValuePair("methodname", HexUtil.objectToHex(methodName)),
						new NameValuePair("methodsignature", HexUtil.objectToHex(methodSignature)),
						new NameValuePair("methodparameters", HexUtil.objectToHex(methodParameters)) };
				postPush.setRequestBody(data);

                String sessionId = session.getSessionid();

				if (sessionId != null && !sessionId.equals("")) {
					postPush.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					postPush.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
				}


                httpClient.executeMethod(postPush);

                if (postPush.getStatusCode() == 200) {

                    /*
                    InputStream is = postPush.getResponseBodyAsStream();
                    if (methodName.equals("popActions")) {
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

                        int bytesread = 0;

                        //int att = 5;
                        while(bytesread != -1) {
                            bytesread = is.read(buffer);

                            log.info("bytesread="+bytesread);

                            if (bytesread > 0) {
                                baos.write(buffer, 0, bytesread);
                            }
                        }

                        byte[] objectdata = baos.toByteArray();

                        log.info("objectdata.length="+objectdata.length);

                        ByteArrayInputStream bais = new ByteArrayInputStream(objectdata);

                        result = new ObjectInputStream(bais).readObject();

                        log.info("result=" + (result != null ? result.toString() : "null"));

                    } else {
                        result = new ObjectInputStream(is).readObject();
                    }
                    */

                    result = new ObjectInputStream(postPush.getResponseBodyAsStream()).readObject();

                } else {
                    log.error("Tunnelling Server Status Code " + postPush.getStatusCode());

                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(postPush.getResponseBodyAsStream()));

                    String line = null;
                    try {
                        while ((line = br.readLine()) != null) {
                            log.error(line);
                        }
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }

                    throw new ConnectException();
                }

			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("Client Side", e);
			}
			if (result != null && result instanceof TunnelingException) {
				//new Exception().printStackTrace();
				throw (TunnelingException) result;
			}
			return result;
		} finally {
			if (postPush != null) {
				postPush.releaseConnection();
			}
		}
	}

    static class DynamicProxyInvocationHandler implements InvocationHandler {
        private ServerSession session;
        private String objectname;
        private String handlername;
        private HttpClient httpClient;

        public DynamicProxyInvocationHandler(ServerSession session, String handlername,
                                             String objectname, HttpClient httpClient){
            this.session = session;
            this.objectname = objectname;
            this.handlername = handlername;
            this.httpClient = httpClient;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            return ServerRuntimeImpl.invokeInternal(session, handlername, objectname,
                    method.getName(), method.getParameterTypes(), args, httpClient);
        }

    } 

	public static Object getDynamicProxy(ServerSession session, String handlername,
                                         String objectname, Class<?>[] c, HttpClient httpClient) {

        return Proxy.newProxyInstance(ServerRuntimeImpl.class.getClassLoader(), c,
                new DynamicProxyInvocationHandler(session, handlername, objectname, httpClient));
	}

    public static Object getDefaultDynamicProxy(ServerSession session,
                                         String objectname, Class<?>[] c, HttpClient httpClient) {

        return getDynamicProxy(session, "invoke", objectname, c, httpClient);
    }

    static class RServicesInvocationHandler implements InvocationHandler {
        private ServerSession session;
        private HttpClient httpClient;
        private ConnectivityCallbacks callbacks;
        private HashMap<String, Integer> notSupported = initNotSupportedMap();

        public RServicesInvocationHandler(ServerSession session, HttpClient httpClient,
                                          ConnectivityCallbacks callbacks) {
            this.session = session;
            this.httpClient = httpClient;
            this.callbacks = callbacks;
        }

        private HashMap<String, Integer> initNotSupportedMap() {
            HashMap<String, Integer> map = new HashMap<String, Integer>();

            map.put("addRCallback", 1);
            map.put("removeRCallback", 1);
            map.put("removeAllRCallbacks", 1);
            map.put("addRCollaborationListener", 1);
            map.put("removeRCollaborationListener", 1);
            map.put("removeAllRCollaborationListeners", 1);
            map.put("addRConsoleActionListener", 1);
            map.put("removeRConsoleActionListener", 1);
            map.put("removeAllRConsoleActionListeners", 1);
            map.put("stopThreads", 1);
            map.put("popActions", 1);

            return map;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;
            try {

                if (notSupported.containsKey(method.getName())) {
                    throw new RuntimeException(method.getName()+" not supported");
                }

                if (method.getName().equals("newDevice")) {
                    result = newDevice(session, (Integer) args[0], (Integer) args[1], false, callbacks);
                } else if (method.getName().equals("newBroadcastedDevice")) {
                    result = newDevice(session, (Integer) args[0], (Integer) args[1], true, callbacks);
                } else if (method.getName().equals("listDevices")) {
                    result = listDevices(session, callbacks);
                } else if (method.getName().equals("newGenericCallbackDevice")) {
                    result = newCallbackDevice(session, callbacks);
                } else if (method.getName().equals("listGenericCallbackDevices")) {
                    result = listCallbackDevices(session, callbacks);
                } else {

                    result = ServerRuntimeImpl.invokeInternal(session, "invoke", "R", method.getName(),
                            method.getParameterTypes(), args, httpClient);
                }

            } catch (NotLoggedInException nle) {
                callbacks.handleServerNotAvailable(nle);
            } catch (TunnelingException te) {
                callbacks.handleProxyNotAvailable(te);

                throw te;
            }

            return result;
        }

    }

	public static RServices getR(ServerSession session,
                                 ConnectivityCallbacks callbacks) {

		Object proxy = Proxy.newProxyInstance(ServerRuntimeImpl.class.getClassLoader(),
                new Class<?>[] { RServices.class, HttpMarker.class },
				new RServicesInvocationHandler(session,
                        createHttpClient(new MultiThreadedHttpConnectionManager()), callbacks));

		return (RServices) proxy;
	}
	
	private static String getRandomSession() {
		String HexDigits[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		Random rnd=new Random(System.currentTimeMillis());
		String result="";
		for (int i=0; i<32;++i) result+=HexDigits[rnd.nextInt(16)];
		return result;
	}

}
