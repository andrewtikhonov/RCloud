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
package workbench;

import net.infonode.docking.util.PropertiesUtil;
import net.infonode.tabbedpanel.theme.GradientTheme;
import net.infonode.tabbedpanel.theme.TabbedPanelTitledTabTheme;
import net.infonode.tabbedpanel.titledtab.TitledTabProperties;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.label.JStatusLabel;
import uk.ac.ebi.rcloud.common.util.OsUtil;
import uk.ac.ebi.rcloud.http.awareness.ConnectivityCallbacks;
import uk.ac.ebi.rcloud.http.exception.*;
import uk.ac.ebi.rcloud.http.proxy.DAOHttpProxy;
import uk.ac.ebi.rcloud.http.proxy.HttpMarker;
import uk.ac.ebi.rcloud.http.proxy.ProxySettings;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import uk.ac.ebi.rcloud.server.ExtendedReentrantLock;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.RType.RChar;
import uk.ac.ebi.rcloud.server.callback.*;
import uk.ac.ebi.rcloud.server.exception.BadSshHostException;
import uk.ac.ebi.rcloud.server.exception.BadSshLoginPwdException;
import uk.ac.ebi.rcloud.server.file.FileDescription;
import uk.ac.ebi.rcloud.server.file.FileNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.search.SearchResult;
import uk.ac.ebi.rcloud.server.search.SearchResultContainer;
import uk.ac.ebi.rcloud.util.HexUtil;
import workbench.actions.WorkbenchActionType;
import workbench.actions.wb.*;
import workbench.actions.wb.FitDeviceAction;
import workbench.dialogs.InDialog;
import uk.ac.ebi.rcloud.server.exception.ServantCreationFailed;
import uk.ac.ebi.rcloud.server.callback.RAction;
import uk.ac.ebi.rcloud.http.exception.TunnelingException;
import uk.ac.ebi.rcloud.http.exception.NotLoggedInException;
import uk.ac.ebi.rcloud.version.SoftwareVersion;
import workbench.dialogs.login.LoginTabbedContainer;
import workbench.generic.RConsole;
import workbench.graphics.GDContainerPanel;
import workbench.graphics.GDControlPanel;
import workbench.graphics.RConsoleProvider;
import workbench.mac.MenuHandlers;
import workbench.manager.runner.RActionHandlerAbstract;
import workbench.manager.runner.RActionHandlerInterface;
import workbench.manager.upman.UpgradeMessage;
import workbench.runtime.RuntimeEnvironment;
import workbench.dialogs.projectbrowser.ProjectBrowserDialog;
import workbench.dialogs.propertyviewer.PropertyBrowser;
import workbench.manager.logman.WorkbenchLogContainer;
import uk.ac.ebi.rcloud.http.exception.NoNodeManagerFound;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.dialogs.*;
import workbench.dialogs.packageinstaller.PackageInstallerDialog;
import workbench.dialogs.packagemanager.PackageManagerDialog;
import workbench.events.VariablesChangeEvent;
import workbench.events.VariablesChangeListener;
import workbench.exceptions.BadServantNameException;
import workbench.exceptions.NoDbRegistryAvailableException;
import workbench.exceptions.NoRmiRegistryAvailableException;
import workbench.exceptions.PingRServerFailedException;
import workbench.exceptions.RBusyException;
import workbench.graphics.JGDPanelPop;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import workbench.manager.propman.PropertyManager;
import workbench.manager.viewman.ViewManager;
import workbench.util.FileLoad;
import workbench.views.benchlogviewer.LogViewerView;
import workbench.util.AbstractDockingWindowListener;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.views.*;
import workbench.views.findresultview.SearchResultBrowser;
import workbench.views.findresultview.SearchResultView;
import workbench.views.rconsole.WorkbenchRConsole;
import workbench.views.serverlogviewer.LogListener;
import workbench.views.serverlogviewer.ServerLogView;
import workbench.views.graphicdevice.DeviceView;
import workbench.views.basiceditor.BasicEditorView;
import workbench.views.helpview.HelpView;
import workbench.views.helpview.HelpBrowserPanel;
import workbench.views.filebrowser.FileBrowserPanel;

import workbench.manager.opman.*;


public class Workbench extends JFrame implements RGui {
    final static private Logger log = LoggerFactory.getLogger(Workbench.class);

    // static stuff
    //public static String SETTINGS_FILE = ServerManager.INSTALL_DIR + "settings.xml";
    public static String SETTINGS_FILE = "settings.xml";

	// Runtime stuff
	private JPanel _graphicPanel;
	private JPanel _rootGraphicPanel;
    private RServices _rInstance;
    private GDDevice _currentDevice;
    private RuntimeEnvironment runtime = new RuntimeEnvironment();

    // Bench Actions
    //
	private HashMap<String, AbstractAction> workbenchActions = new HashMap<String, AbstractAction>();

    // Bench Console
	private WorkbenchRConsole consolePanel = null;

    // Bench Look and Feel related
    private int _lf;
    private LookAndFeelInfo[] installedLFs = UIManager.getInstalledLookAndFeels();

    // File Browser
    private FileBrowserPanel  fileBrowser = null;

    // Status panel
    private JStatusLabel userLabel = null;
    private JStatusLabel projectLabel = null;
    private JStatusLabel memoryLabel = null;

    // Operation Manager
    //
    private OperationManager  opMan = new OperationManager(this);

    // Bench Menus
    private JMenu sessionMenu = null;
    private JMenu projectMenu = null;
    private JMenu appboxMenu = null;
    private JMenu graphicsMenu = null;
    private JMenu packageMenu = null;
    private JMenu helpMenu = null;

    // Remote events
    private RemoteLogListenerImpl logListener = new RemoteLogListenerImpl();
    private GenericRActionListener   genericRActionListenerImpl = new LocalGenericRActionListenerImpl();
    private GenericCallbackDevice    genericCallbackDevice;

    // Property Manager
    public static PropertyManager propMan = new PropertyManager(Preferences.userNodeForPackage(Workbench.class));

    // Infornode docking related stuff
    private DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
    private RootWindowProperties properties = new RootWindowProperties();
    private RootWindow benchRootWindow;

    // views container & map
    private View[] views = new View[2];
    private TabWindow mainTabWindow = null;
    private ViewMap viewMap = new ViewMap();
    private ViewManager viewMan = new ViewManager();

    // sceenshot
    //private BufferedImage screenshot = null;

    // log container
    private WorkbenchLogContainer logContainer = new WorkbenchLogContainer();

    // console provider
    private WorkbenchRConsoleProvider consoleProvider = new WorkbenchRConsoleProvider();

    public void initMacApp() {
        try {
            new MenuHandlers(Workbench.this).init();
        } catch (Exception ex) {
            //log.info("--- MAC MENU HANDLERS NOT LOADED ---");
        }
    }


    public String getProperty(String name) {
        return getProperty(name, null);
    }

    public String getProperty(String name, String defaultValue) {
        String value = propMan.getProperty(name, defaultValue);
        if (value == null) {
            value = System.getProperty(name);
        }
        return value;
    }

    class WorkbenchRConsoleProvider implements RConsoleProvider {
        public HashMap<String, RConsole> getColsoleMap() {
            return consoleList;
        }
    }

    public void setProperty(String name, String value) {
        propMan.setProperty(name, value);
    }

    private void updateFloatingWindow(FloatingWindow fw) {
        //log.info("Workbench-updateFloatingWindow");

        fw.addListener(new DockingWindowAdapter() {
            public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
                //log.info("Workbench-DockingWindowAdapter-windowAdded");
            }

            public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
                //log.info("Workbench-DockingWindowAdapter-windowRemoved");
            }

            public void windowClosing(DockingWindow window) throws OperationAbortedException {
                //log.info("Workbench-DockingWindowAdapter-windowClosing");

                if (window instanceof TabWindow) {

                    TabWindow tw = (TabWindow) window;

                    for (int i = 0; i < tw.getChildWindowCount(); ++i) {
                        DockingWindow w = tw.getChildWindow(i);
                        if (w == views[0] || w == views[1]) {
                            throw new OperationAbortedException("Window close was aborted!");
                        }
                    }
                }
            }

            public void windowDocking(DockingWindow window) throws OperationAbortedException {
                //log.info("Workbench-DockingWindowAdapter-windowDocking");
            }

            public void windowUndocking(DockingWindow window) throws OperationAbortedException {
                //log.info("Workbench-DockingWindowAdapter-windowUndocking");
            }
        });
    }

    private class RemoteLogListenerImpl {

        private Vector<String> storage = new Vector<String>();
        private Vector<LogListener> listenerList = new Vector<LogListener>();

        public void addListener(LogListener listener) {
            listenerList.add(listener);
        }

        public void removeListener(LogListener listener) {
            listenerList.remove(listener);
        }

        public void removeAllListeners() {
            listenerList.removeAllElements();
        }

        public void write(String aString) {
            storage.add(aString);
            for (LogListener l : listenerList) {
                l.write(aString);
            }
        }

        public Vector<String> getStorage() {
            return storage;
        }
    }

	private final ReentrantLock _protectR = new ExtendedReentrantLock() {

		@Override
		public void lock() {
			super.lock();

            updateConsoleIcon(_busyIcon);
            consolePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		@Override
		public void unlock() {

            updateConsoleIcon(_connectedIcon);

            super.unlock();

			consolePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		@Override
		public void rawUnlock() {
            super.unlock();
		}

		@Override
		public void rawLock() {
			super.lock();
		}

		public boolean isLocked() {
            try {
				if (_rInstance.isBusy()) {
					return true;
				}
			} catch (Exception e) { }

            return super.isLocked();
		}
	};
	private String[] _expressionSave = new String[] { "" };

	private ConsoleLogger _consoleLogger = new ConsoleLogger() {

		public void printAsInput(String message) {
			consolePanel.print(message, null);
		}

		public void printAsOutput(String message) {
			consolePanel.print(null, message);
		}

		public void print(String expression, String result) {
			consolePanel.print(expression, result);
		}

		public void pasteToConsoleEditor() {
			consolePanel.pasteToConsoleEditor();
		}

	};

	private ImageIcon _currentDeviceIcon = null;
	private ImageIcon _inactiveDeviceIcon = null;

	private ImageIcon _connectedIcon = null;
	private ImageIcon _disconnectedIcon = null;
	private ImageIcon _busyIcon = null;

    private ConnectivityCallbacks stateCallbacks = new ConnectivityCallbacksImpl();

    class ConnectivityCallbacksImpl implements ConnectivityCallbacks {
        public void handleProxyNotAvailable(Throwable ex) {
            boolean tunnelFailure = (ex instanceof ConnectionFailedException);
            boolean serverFailure = (ex.getCause() instanceof java.rmi.ConnectException) ||
                    (ex.getCause() instanceof java.net.ConnectException);

            if (isRAvailable()) {
                if (tunnelFailure) {
                    handleServerFailure();
                    JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                            "Connection to Tunnelling Server has been lost", "Connection Lost",
                            JOptionPaneExt.WARNING_MESSAGE);
                }
                if (serverFailure) {
                    handleServerFailure();
                    JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                            "Connection to R Server has been lost", "Connection Lost",
                            JOptionPaneExt.WARNING_MESSAGE);
                }
            }
        }
        public void handleServerNotAvailable(Throwable ex) {
            if (isRAvailable()) {
                handleServerFailure();
                JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                        "Connection to R Server has been lost", "Connection Lost",
                        JOptionPaneExt.WARNING_MESSAGE);
            }
        }
    }

    private void loadOneProperty(String name, String defaultValue, boolean mandatory) {
        String value = System.getProperty(name);
        if (value == null) {
            value = getProperty(name);
            if (value == null) {
                if (defaultValue != null) {
                    setProperty(name, defaultValue);
                    //log.info("property ({}) not defined, using default ({})", name, defaultValue);
                } else {
                    if (mandatory) {
                        JOptionPaneExt.showMessageDialog(Workbench.this.getRootFrame(),
                                "Mandatory parameter " + name + " not defined. ", "", JOptionPaneExt.WARNING_MESSAGE);
                        System.exit(0);
                    } else {
                        //log.info("property ({}) not defined, not mandatory, omitting", name);
                    }
                }
            } else {
                //log.info("property ({}) not set, using preference ({})", name, value);
            }
        } else {
            setProperty(name, value);
        }
    }

    private void loadBenchProperties() {

        loadOneProperty("baseurl", null, true);
        loadOneProperty("autologon", "true", false);
        loadOneProperty("demo", null, false);
        loadOneProperty("debug", null, false);
        loadOneProperty("login", "", false);
        loadOneProperty("save",  null, false);
        loadOneProperty("mode", "http", false);
        loadOneProperty("lf", "1", false);
        loadOneProperty("stub", null, false);
        loadOneProperty("name", null, false);
        loadOneProperty("registry.host", null, false);
        loadOneProperty("registry.port", null, false);
        loadOneProperty("privatename", null, false);
        loadOneProperty("noconfirmation", null, false);
        loadOneProperty("desktopapplication", "true", true);
        loadOneProperty("selfish", null, false);
        loadOneProperty("local.repos", "true", false);
        loadOneProperty("local.repos.url", "http://www.ebi.ac.uk/Tools/rcloud", false);

        //log.info("params=" + propMan.toString());
    }

	public Workbench() {

        loadBenchProperties();
        setupUI();

        //setBackground(new Color(0,0,0,0));
    }

    public void handleComponentMoved() {
        if (consolePanel != null) { consolePanel.handleComponentMoved(); }
    }

    public void handleComponentResized() {
        if (consolePanel != null) { consolePanel.handleComponentResized(); }
    }

	private boolean isDesktopApplication() {
		return getProperty("desktopapplication") != null && getProperty("desktopapplication").equalsIgnoreCase("true");
	}

	File pluginsDir;

    private GDContainerPanel createGDContainerPanel(final JPanel container, GDDevice d) throws RemoteException {

        GDContainerPanel panel = new GDContainerPanel(d, null);
        GDControlPanel control = new GDControlPanel(panel);

        panel.setConsoleProvider(consoleProvider);

        container.removeAll();
        container.setLayout(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        container.add(control, BorderLayout.SOUTH);

        return panel;
    }

    public DeviceView createEmptyDeviceView() throws RemoteException {

        JPanel rootGraphicPanel = new JPanel();
        rootGraphicPanel.setLayout(new BorderLayout());

        DeviceView deviceView = new DeviceView("Graphic Device",
                null, rootGraphicPanel, viewMan.getDynamicViewId());

        viewMan.updateViews(deviceView, true);

        /*
        ((TabWindow) views[1].getWindowParent()).addTab(deviceView);

        int rnd = (int) (Math.random() * 100);

        deviceView.undock(new Point(200 + rnd, 200 + rnd));
        */

        //FloatingWindow window = benchRootWindow.createFloatingWindow(
        //        new Point(200 + rnd, 200 + rnd), new Dimension(400,400), deviceView);

        //window.setVisible(true);

        /*
        final JGDPanelPop gDpanel = createJGDPanelPop(rootGraphicPanel, device);

        deviceView.setPanel(gDpanel);

        new Thread(new Runnable() {
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        gDpanel.fit();
                    }
                });
            }
        }).start();
        */

        return deviceView;
    }

    private void handleConnectionException(Exception ex){

        try {
            throw ex;
            
        } catch (NotLoggedInException nlie) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Sorry, but you're not logged in", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (java.net.ConnectException cone) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Sorry, we could not connect to the Tunnelling Server", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (NoServantAvailableException e) {
            projectClosed();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Sorry, no R servers are available, we could not load project", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (NoRegistryAvailableException nrae) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "No Registry available, can not log on", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (NoNodeManagerFound nne) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "No Node Manager Found", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (ConnectionFailedException cfe) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Sorry, but we could not connect to the tunnelling server", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (BadLoginPasswordException e) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Bad Login or Password", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (TunnelingException te) {
            projectClosed();
            userLoggedOut();

            log.error("Network Problem!", te);
            //JOptionPaneExt.showExceptionDialog(Workbench.this.getContentPane(), te);

        } catch (NoRmiRegistryAvailableException normie) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "No RMI Registry Available", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (NoDbRegistryAvailableException nodbe) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "No DB Registry Available", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (BadServantNameException bsne) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Bad RMI Servant Name", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (BadSshHostException bh_ssh_e) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Cannot connect to Remote SSH Host", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (BadSshLoginPwdException blp_ssh_e) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Bad SSH Login or Password", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (PingRServerFailedException prsf_e) {
            projectClosed();
            userLoggedOut();

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Ping R Server Failed", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (RBusyException rb_e) {

            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "Connection Failed, R Server is Busy", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (ServantCreationFailed scf) {
            JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(),
                    "R Server Creation Failed", "",
                    JOptionPaneExt.WARNING_MESSAGE);

        } catch (RemoteException re) {

            log.error("Remote Server Problem", re);
            //JOptionPaneExt.showExceptionDialog(Workbench.this.getContentPane(), re);
            
        } catch (OperationCancelledException oae) {

        } catch (Exception unknow) {
            log.error("Unhandled Problem", unknow);
            //JOptionPaneExt.showExceptionDialog(Workbench.this.getContentPane(), unknow);
        }
    }

    public int handleWorkbenchClosure() {
        String[] options = { "Exit", "Cancel" };

        //screenshot = ScreenCapture.captureScreen();

        int reply = 0;

        if (runtime.getUser() != null) {
            reply = JOptionPaneExt.showOptionDialog(Workbench.this.getContentPane(),
                    "Exit the Workbench?",
                    "Confirm Exit",
                    JOptionPaneExt.DEFAULT_OPTION,
                    JOptionPaneExt.QUESTION_MESSAGE,
                    null, options, options[0]);
        }

        if (reply == 0) {
            logoutUserRunnable.closed = true;
            logoutUserRunnable.run();
            if (logoutUserRunnable.closed) {
                return 1;
            }
        }

        return 0 ;
    }

    class MemoryStatusRunnable implements Runnable {
        private JLabel label;
        private long FIVESECONDS = 5000;
        public MemoryStatusRunnable(JLabel label) {
            this.label = label;
        }

        class UpdateRunnable implements Runnable {
            private String text;
            public UpdateRunnable(String text){
                this.text = text;
            }
            public void run(){
                label.setText(text);
            }
        }

        public void update(){
            String status = " ";

            RServices r = obtainR();
            if (r != null) {
                try {
                    getRLock().lock();

                    RChar var = (RChar) r.getObject("as.character(sum(gc()[,2]))");

                    status = var.getValue()[0] + "M";

                } catch (RemoteException re) {
                    log.error("Error!", re);
                } finally {
                    getRLock().unlock();
                }
            }

            EventQueue.invokeLater(new UpdateRunnable(status));
        }

        public void run(){
            while(true) {

                update();

                try {
                    Thread.sleep(FIVESECONDS);
                } catch (InterruptedException ie) {
                }
            }
        }
    }


    class ConsoleSnapshotRunnable implements Runnable {
        private long THIRTYSECONDS = 5000;

        public void run(){
            while(true) {

                if (runtime != null) {
                    if (runtime.getProject() != null) {
                        if (getConsole().isConsoleDirty()) {
                            getConsole().resetConsoleDirty();
                            saveConsoleSnapshot();
                        }
                    }
                }

                try {
                    Thread.sleep(THIRTYSECONDS);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

	public void setupUI() {

        setTitle("R Cloud Workbench " + SoftwareVersion.getVersion());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (handleWorkbenchClosure() == 1) {
                    System.exit(0);
                }
            }
        });

        if (OsUtil.isMacOs()) {
            initMacApp();
        }

        initRActionHandlers();
		System.setErr(System.out);

		//PoolUtils.initRmiSocketFactory();

        /*
        if (getProperty("debug") != null && getProperty("debug").equalsIgnoreCase("true")) {
			IOUtil.redirectIO();
		}
		*/

        addComponentListener(new ComponentListener(){
            public void componentHidden(ComponentEvent e) {}
                public void componentMoved(ComponentEvent e) {
                    handleComponentMoved();
                }
                public void componentResized(ComponentEvent e) {
                    handleComponentResized();
                }
                public void componentShown(ComponentEvent e) {}
            });

		restoreState();

        runtime.setMode(RuntimeEnvironment.HTTP_MODE);
        runtime.setBaseUrl(getProperty("baseurl"));

		try {

            _currentDeviceIcon = new ImageIcon(ImageLoader.load("/views/images/gdevices/iMovie.png"));
            _currentDeviceIcon.setImage(GraphicsUtilities.scaleImage(_currentDeviceIcon.getImage(), 20));

            _inactiveDeviceIcon = new ImageIcon(ImageLoader.load("/views/images/gdevices/Preview.png"));
            _inactiveDeviceIcon.setImage(GraphicsUtilities.scaleImage(_inactiveDeviceIcon.getImage(), 20));

            _connectedIcon = new ImageIcon(ImageLoader.load("/views/images/rconsole/WiFi.png"));
            _connectedIcon.setImage(GraphicsUtilities.scaleImage(_connectedIcon.getImage(), 20));

            _disconnectedIcon = new ImageIcon(ImageLoader.load("/views/images/rconsole/Pause.png"));
            _disconnectedIcon.setImage(GraphicsUtilities.scaleImage(_disconnectedIcon.getImage(), 20));

            _busyIcon = new ImageIcon(ImageLoader.load("/views/images/rconsole/NetWork.png"));
            _busyIcon.setImage(GraphicsUtilities.scaleImage(_busyIcon.getImage(), 20));


			int lf = 0;
			try {
				lf = Integer.decode(getProperty("lf"));
			} catch (Exception e) {
			}


            /*
            LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();

            installedLFs = new LookAndFeelInfo[lafs.length + 0];

            System.arraycopy(lafs, 0, installedLFs, 0, lafs.length);

            //int index = lafs.length;
            //installedLFs[index++] = new LookAndFeelInfo("Synthetica-Standard", "de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
            //installedLFs[index++] = new LookAndFeelInfo("Synthetica-BlackStar", "de.javasoft.plaf.synthetica.SyntheticaBlackStarLookAndFeel");

            if (lf >= installedLFs.length) {
                lf = 0;
            }

			_lf = lf;
			*/


            /*
            if (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1) {
                for(int i=0;i<installedLFs.length;i++) {
                    String n = installedLFs[i].getName();

                    if (n.toLowerCase().contains("mac")) {
                        _lf = i; break;
                    }
                }
            }
            */

            /*
            try {
				UIManager.setLookAndFeel(getLookAndFeelClassName());
			} catch (Exception e) {
                log.error("Error!", e);
			}
			*/



            /*
			try {
				UIManager.setLookAndFeel(new InfoNodeLookAndFeel());
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/

            /*
            try {
                UIManager.setLookAndFeel(new InfoNodeLookAndFeel());
            } catch (UnsupportedLookAndFeelException ulafe) {
                ulafe.printStackTrace();
            }
            */


			_rootGraphicPanel = new JPanel();
			_rootGraphicPanel.setLayout(new BorderLayout());
            _rootGraphicPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.GRAY));
			_graphicPanel = new JPanel();

            Dimension _graphicPanelSize = new Dimension(400, 400);

            _graphicPanel.setPreferredSize(_graphicPanelSize);
            _graphicPanel.setSize(_graphicPanelSize);

			_rootGraphicPanel.add(_graphicPanel, BorderLayout.CENTER);

            fileBrowser = new FileBrowserPanel(this);
            fileBrowser.setEnabled(false);

            initWorkbenchActions();

			consolePanel = new WorkbenchRConsole(this);

            JPanel statusPanel    = new JPanel(new BorderLayout());

            statusPanel.setOpaque(false);

            userLabel    = new JStatusLabel(" ");
            userLabel.setPreferredSize(new Dimension(300, 24));

            projectLabel = new JStatusLabel(" ");
            projectLabel.setPreferredSize(new Dimension(400, 24));

            memoryLabel  = new JStatusLabel(" ");
            memoryLabel.setPreferredSize(new Dimension(100, 24));

            //new Thread(new MemoryStatusRunnable(memoryLabel)).start();

            statusPanel.add(userLabel, BorderLayout.WEST);
            statusPanel.add(projectLabel, BorderLayout.CENTER);
            statusPanel.add(memoryLabel, BorderLayout.EAST);

            views[0] = new View("File Browser", null, fileBrowser);
            views[1] = new View("R Console", null, consolePanel);

			viewMap.addView(0, views[0]);
			viewMap.addView(1, views[1]);

            initWorkbenchMenus();
            
			MixedViewHandler handler = new MixedViewHandler(viewMap, new ViewSerializer() {
				public void writeView(View view, ObjectOutputStream out) throws IOException {
					out.writeInt(((DynamicView) view).getId());
				}

				public View readView(ObjectInputStream in) throws IOException {
					return viewMan.getAllViews().get(in.readInt());
				}
			});

			benchRootWindow = DockingUtil.createRootWindow(viewMap, handler, true);
			//rootWindow.setBackground(new Color(0,0,0,0));

            benchRootWindow.getWindowBar(Direction.DOWN).setEnabled(true);

            /*
            net.infonode.docking.theme.BlueHighlightDockingTheme t1 = new net.infonode.docking.theme.BlueHighlightDockingTheme();
            net.infonode.docking.theme.ClassicDockingTheme t2 = new net.infonode.docking.theme.ClassicDockingTheme();
            net.infonode.docking.theme.DefaultDockingTheme t3 = new net.infonode.docking.theme.DefaultDockingTheme();
            net.infonode.docking.theme.GradientDockingTheme t5 = new net.infonode.docking.theme.GradientDockingTheme();
            net.infonode.docking.theme.LookAndFeelDockingTheme t6 = new net.infonode.docking.theme.LookAndFeelDockingTheme();
            net.infonode.docking.theme.SlimFlatDockingTheme t7 = new net.infonode.docking.theme.SlimFlatDockingTheme();
            net.infonode.docking.theme.SoftBlueIceDockingTheme t8 = new net.infonode.docking.theme.SoftBlueIceDockingTheme();
            */


            RootWindowProperties properties = new RootWindowProperties();

            TitledTabProperties titledTabProperties = new TitledTabProperties();
            TabbedPanelTitledTabTheme titledTabTheme = new GradientTheme(true, true);

            ShapedGradientDockingTheme t0 = new ShapedGradientDockingTheme();

            properties.addSuperObject(t0.getRootWindowProperties());
            properties.addSuperObject(PropertiesUtil.createTitleBarStyleRootWindowProperties());

            benchRootWindow.getRootWindowProperties().addSuperObject(properties);
			benchRootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
			benchRootWindow.addListener(new WorkbenchDockingWindowListener());

			getContentPane().setLayout(new BorderLayout());

            mainTabWindow = new TabWindow(new DockingWindow[] { views[1] });

            JPanel mainPanel = new JPanel();

            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(statusPanel, BorderLayout.SOUTH);
            mainPanel.add(benchRootWindow, BorderLayout.CENTER);
			add(mainPanel, BorderLayout.CENTER);

            SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					benchRootWindow.setWindow(
                            new SplitWindow(true, 0.3f, views[0], mainTabWindow));
				}
			});

		} catch (Exception e) {
            log.error("Error!", e);
		}

        String wait = getProperty("wait");

        runtime.setWait(wait != null && wait.equalsIgnoreCase("true"));

		//_instance = this;

        new Thread(new ConsoleSnapshotRunnable()).start();

        loadProxySettings();

        establishDBConnection();

        openLoginDialog();

	}

    private void establishDBConnection() {
        try {

            runtime.setDAOLayer(DAOHttpProxy.getDAOLayer(runtime.getProxySettings(),
                    runtime.getSession()));


        } catch (Exception ex) {
            handleConnectionException(ex);
        }
    }

    private void openLoginDialog() {
        if (getProperty("autologon") == null || getProperty("autologon").equalsIgnoreCase("true")) {
            workbenchActions.get(WorkbenchActionType.LOGINUSER).actionPerformed(null);
        }
    }

    public static final String PROXYAUTO = "PROXYAUTO";
    public static final String PROXYHOST = "PROXYHOST";
    public static final String PROXYPORT = "PROXYPORT";
    public static final String PROXYUSER = "PROXYUSER";
    public static final String PROXYPSWD = "PROXYPSWD";

    public void setJavaProxyProperties(ProxySettings settings) {
        System.setProperty("http.proxyHost", settings.PROXY_HOST);
        System.setProperty("http.proxyPort", Integer.toString(settings.PROXY_PORT));
        System.setProperty("http.proxyUser", settings.USERNAME);
        System.setProperty("http.proxyUserName", settings.USERNAME);
        System.setProperty("http.proxyPassword", settings.PASSWORD);
    }

    public void loadProxySettings() {

        //log.info("loadProxySettings");

        boolean proxyauto = Boolean.parseBoolean(propMan.getProperty(PROXYAUTO, "true"));

        ProxySettings settings = null;

        if (proxyauto) {
            settings = ServerRuntimeImpl.detectProxySettings();

            if (settings != null) {
                ServerRuntimeImpl.applyProxySettings(settings);

                setJavaProxyProperties(settings);
            }

        } else {

            settings = new ProxySettings(propMan.getProperty(PROXYHOST, "localhost"),
                    Integer.decode(propMan.getProperty(PROXYPORT, "8080")),
                    propMan.getProperty(PROXYUSER, "user"),
                    propMan.getProperty(PROXYPSWD, "pswd"));

            ServerRuntimeImpl.applyProxySettings(settings);

            setJavaProxyProperties(settings);
        }

        runtime.setProxyAuto(proxyauto);

        runtime.setProxySettings(settings);

        //if (settings != null) {
        //    log.info("PROXY: " + (proxyauto ? "AUTO" : ("MANUAL " + settings.toSring())));
        //}

    }

    public void persistProxySettings(boolean proxyauto, ProxySettings settings) {

        //log.info("persistProxySettings");

        propMan.setProperty(PROXYAUTO, Boolean.toString(proxyauto));

        if (!proxyauto) {
            propMan.setProperty(PROXYHOST, settings.PROXY_HOST);
            propMan.setProperty(PROXYPORT, Integer.toString(settings.PROXY_PORT));
            propMan.setProperty(PROXYUSER, settings.USERNAME);
            propMan.setProperty(PROXYPSWD, settings.PASSWORD);
        }

        //if (settings != null) {
        //    log.info("PROXY: " + (proxyauto ? "AUTO" : ("MANUAL " + settings.toSring())));
        //}

    }

    class WorkbenchDockingWindowListener implements DockingWindowListener {

        public void handleDeviceViewClosure(DockingWindow window) throws OperationAbortedException {

            Vector<DeviceView> deviceViews = getDeviceViews();

            boolean isLastDevice = (deviceViews.size() == 1);

            if (isLastDevice) {
                window.putClientProperty(ViewManager.WINDOW_OPERATION,
                        ViewManager.HIDEWINDOW);

            } else {
                Object[] options = { "Hide Device", "Close Device", "Cancel" };

                int reply = JOptionPaneExt.showOptionDialog(Workbench.this.getContentPane(),
                        "Device window is being closed",
                        "Confirm Closure",
                        JOptionPaneExt.DEFAULT_OPTION,
                        JOptionPaneExt.QUESTION_MESSAGE,
                        null, options, options[0]);

                switch(reply) {
                    case 0: // hide
                        window.putClientProperty(ViewManager.WINDOW_OPERATION,
                                ViewManager.HIDEWINDOW);

                        break;
                    case 1: // close

                        deviceViews.remove(((DeviceView) window));

                        try {
                            //log.info("windowClosing-DeviceView-setCurrentDevice");

                            GDDevice d = deviceViews.get(0).getPanel().getGdDevice();
                            setCurrentDevice(d);
                            d.setAsCurrentDevice();

                        } catch (Exception e) {
                            log.error("Error!", e);
                        }

                        //log.info("windowClosing-DeviceView-(DeviceView)window.getPanel().dispose()");

                        ((DeviceView) window).getPanel().dispose();

                        window.putClientProperty(ViewManager.WINDOW_OPERATION,
                                ViewManager.CLOSEWINDOW);

                        break;
                    case 2: // cancel
                        throw new OperationAbortedException("Window close was aborted!");

                    default: // cancel
                        throw new OperationAbortedException("Window close was aborted!");
                }

            }
        }

        public void handleContainerViewClosure(DockingWindow window) throws OperationAbortedException {

            for (int i = 0; i < window.getChildWindowCount(); ++i) {
                DockingWindow w = window.getChildWindow(i);
                if (w == views[0] || w == views[1]) {
                    throw new OperationAbortedException("Window close was aborted!");
                }
                if (w instanceof DeviceView) {
                    handleDeviceViewClosure(w);
                }

                if (w instanceof TabWindow) {
                    handleContainerViewClosure(w);
                }
            }
        }


        public void viewFocusChanged(View arg0, View arg1) {
            //log.info("Workbench-DockingWindowListener-viewFocusChanged");
        }

        public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
            //log.info("Workbench-DockingWindowListener-windowAdded");

            viewMan.updateViews(addedWindow, true);

            if (addedWindow instanceof FloatingWindow)
                updateFloatingWindow((FloatingWindow) addedWindow);

        }

        public void windowClosed(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowClosed");
        }

        public void windowClosing(DockingWindow window) throws OperationAbortedException {

            if (window == views[0] || window == views[1])
                throw new OperationAbortedException("Window close was aborted!");

            if (window instanceof DeviceView) {

                handleDeviceViewClosure(window);

                //log.info("windowClosing-DeviceView");
            }

            if (window instanceof TabWindow || window instanceof FloatingWindow) {
                handleContainerViewClosure(window);
            }
        }

        public void windowDocked(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowDocked");
        }

        public void windowDocking(DockingWindow arg0) throws OperationAbortedException {
            //log.info("Workbench-DockingWindowListener-windowDocking");
        }

        public void windowHidden(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowHidden");
        }

        public void windowMaximized(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowMaximized");
        }

        public void windowMaximizing(DockingWindow window) throws OperationAbortedException {
            //log.info("Workbench-DockingWindowListener-windowMaximizing");
        }

        public void windowMinimized(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowMinimized");
        }

        public void windowMinimizing(DockingWindow window) throws OperationAbortedException {
            //log.info("Workbench-DockingWindowListener-windowMinimizing");
        }

        public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {

            viewMan.updateViews(removedWindow, false);
        }

        public void windowRestored(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowRestored");
        }

        public void windowRestoring(DockingWindow arg0) throws OperationAbortedException {
            //log.info("Workbench-DockingWindowListener-windowRestoring");
        }

        public void windowShown(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowShown");
        }

        public void windowUndocked(DockingWindow arg0) {
            //log.info("Workbench-DockingWindowListener-windowUndocked");
        }

        public void windowUndocking(DockingWindow arg0) throws OperationAbortedException {
            //log.info("Workbench-DockingWindowListener-windowUndocking");
        }
    }


    private void setTheme(DockingWindowsTheme theme) {
      properties.replaceSuperObject(currentTheme.getRootWindowProperties(),
                                    theme.getRootWindowProperties());
      currentTheme = theme;
    }


    private void userLoggedIn() {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                userLabel.setText(" User: " + runtime.getUser().getFullName());
            }
        });

    }

    private void userLoggedOut() {
        runtime.setUser(null);

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                userLabel.setText(" ");
            }
        });
    }

    private void projectOpened() {
        //log.info("Workbench-projectOpened");

        fileBrowser.setEnabled(true);

        if (runtime != null) {
            if (runtime.getProject() != null) {
                fileBrowser.addBookmark(runtime.getProject().getTitle(),
                        runtime.getProject().getAbsolutePath());
            }
            if (runtime.getUser() != null) {
                fileBrowser.addBookmark("Home", runtime.getUser().getUserFolder());
                fileBrowser.addBookmark("Library", runtime.getUser().getUserLibFolder());
            }
        }

        fileBrowser.addBookmark("Working Directory", "r: getwd()");

        updateMenuState(sessionMenu);
        updateMenuState(projectMenu);
        updateMenuState(appboxMenu);
        updateMenuState(graphicsMenu);
        updateMenuState(packageMenu);
        updateMenuState(helpMenu);

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                projectLabel.setText(" Project: " + runtime.getProject().getTitle());
            }
        });
    }

    private void projectClosed() {
        //log.info("Workbench-projectClosed");

        runtime.setProject(null);

        fileBrowser.emptyCache();
        fileBrowser.emptyBookmarks();
        fileBrowser.emptyAddressBar();
        fileBrowser.emptyTree();

        fileBrowser.setEnabled(false);

        // clear console
        //consolePanel.clearScreen();
        
        updateMenuState(sessionMenu);
        updateMenuState(projectMenu);
        updateMenuState(appboxMenu);
        updateMenuState(graphicsMenu);
        updateMenuState(packageMenu);
        updateMenuState(helpMenu);

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                projectLabel.setText(" ");
            }
        });

    }

	private void updateConsoleIcon(final Icon icon) {

        Icon iconToSet = icon;

        if (icon == null) {
            if (!isRAvailable()) {
                iconToSet = _disconnectedIcon; 
            } else {
                try {
                    if (obtainR().isBusy()) {
                        iconToSet = _busyIcon;
                    } else {
                        iconToSet = _connectedIcon;
                    }
                } catch (Exception e) {
                }
            }
        } else {
            iconToSet = icon;
        }

        final Icon final_iconToSet = iconToSet;

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                views[1].getViewProperties().setIcon(final_iconToSet);
            }
        });
	}

	private HelpView getOpenedBrowserView() {
        return (HelpView) viewMan.getFirstViewOfClass(HelpView.class);
	}

	private SearchResultView getOpenedSearchBrowserView() {
        return (SearchResultView) viewMan.getFirstViewOfClass(SearchResultView.class);
	}

	private Vector<DeviceView> getDeviceViews() {
        //return new Vector<DeviceView>((Collection)viewMan.getAllViewsOfClass(DeviceView.class));
        Vector<DynamicView> views = viewMan.getAllViewsOfClass(DeviceView.class);

        Vector<DeviceView> result = new Vector<DeviceView>();

        for (DynamicView view : views) {
            result.add((DeviceView)view);
        }

        return result;
	}

	private ServerLogView getOpenedServerLogView() {
        return (ServerLogView) viewMan.getFirstViewOfClass(ServerLogView.class);
	}

	private LogViewerView getOpenedWorkbenchLogView() {
        return (LogViewerView) viewMan.getFirstViewOfClass(LogViewerView.class);
	}

	public void setHelpBrowserURL(String url) {
		HelpView openedBrowserView = getOpenedBrowserView();
		if (openedBrowserView == null) {

			HelpBrowserPanel _helpBrowser = new HelpBrowserPanel(this);

            HelpView helpView = new HelpView("Help View", null, _helpBrowser,
                    viewMan.getDynamicViewId());

            ((TabWindow) views[1].getWindowParent()).addTab(helpView);

            try {
                _helpBrowser.setURL(url);
            } catch (Exception e) {
                log.error("Error!", e);
            }

            //helpView.undock(new Point(100,100));

		} else {

			try {
				openedBrowserView.getBrowser().setURL(url);
			} catch (Exception e) {
                log.error("Error!", e);
			}

            openedBrowserView.makeVisible();
		}
	}


	private void setSearchResults(SearchResultContainer resultcontaner) { // Vector<SearchResult> resultset
        SearchResultView browserView = getOpenedSearchBrowserView();

		if (browserView == null) {

			SearchResultBrowser searchBrowser = new SearchResultBrowser(this);

            browserView = new SearchResultView("Search Results", null, searchBrowser,
                    viewMan.getDynamicViewId());

            ((TabWindow) views[1].getWindowParent()).addTab(browserView);

            searchBrowser.addAllSearchInfoSafe(resultcontaner);

            //for (SearchResult result : resultset) {
            //    searchBrowser.addSearchResultInfo(result);
            //}

		} else {

            browserView.getBrowser().addAllSearchInfoSafe(resultcontaner);

            //for (SearchResult result : resultset) {
            //    browserView.getBrowser().addSearchResultInfo(result);
            //}

            browserView.makeVisible();
		}
	}

	private Properties stateProperties = new Properties();

	private void restoreState() {

		File settings = new File(Workbench.SETTINGS_FILE);
		if (settings.exists()) {

			try {

				stateProperties.loadFromXML(new FileInputStream(settings));

				if (stateProperties.get("mode") != null) {
					try {
						runtime.setMode(Integer.decode((String) stateProperties.get("mode")));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

				if (stateProperties.get("baseurl") != null) {
					try {
						runtime.setBaseUrl((String) stateProperties.get("baseurl"));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

                /*
				if (stateProperties.get("default.r.bin") != null) {
					try {
						LoginDialog.defaultRBin_str = (String) stateProperties.get("default.r.bin");
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

				if (stateProperties.get("default.r") != null) {
					try {
						LoginDialog.defaultR_bool = new Boolean((String) stateProperties.get("default.r"));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

				if (stateProperties.get("memorymin") != null) {
					try {
						LoginDialog.memoryMin_int = Integer.decode((String) stateProperties.get("memorymin"));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

				if (stateProperties.get("memorymax") != null) {
					try {
						LoginDialog.memoryMax_int = Integer.decode((String) stateProperties.get("memorymax"));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

				*/

			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}

    /*
	synchronized private void persistState() {

        stateProperties.put("command.history", HexUtil.objectToHex(consolePanel.getCommandHistory()));
        stateProperties.put("mode", runtime.getMode());
        stateProperties.put("baseurl", runtime.getBaseUrl());

        try {
            stateProperties.storeToXML(new FileOutputStream(new File(Workbench.SETTINGS_FILE)), "settings");
        } catch (IOException ioe) {
            log.error("Error!", ioe);
        }
	}
	*/

	public void destroy() {

        //!!!!!!!!!!!!!!!!!!!!!!!!!
        //TO-DO: rework to make it a dialog
        /*
        log.info("destroy called on " + new Date());
		if (_sessionId != null) {
			try {
				if (runtime.getMode() == HTTP_MODE) {
					disposeDevices();
					ServerRuntimeImpl.suspendProject(runtime.getCommandUrl(), _sessionId, true);
				}
			} catch (TunnelingException e) {
				// e.printStackTrace();
			}
			noSession();
		} else {

			//persistState();

		}
		*/
	}

    public boolean isViewFocussed(View obj) {
        Class viewClass = View.class;
        try {
            Field field = viewClass.getDeclaredField("isfocused");
            field.setAccessible(true);
            return (Boolean) field.get(obj);
        } catch (Exception ex) {
            log.error("Error! ", ex);
        }

        return false;
    }


    public BasicEditorView getActiveBasicEditorView() {
        Vector<DynamicView> views = viewMan.getAllViewsOfClass(BasicEditorView.class);
        for (DynamicView wiew : views) {
            if (isViewFocussed(wiew)) {
                return (BasicEditorView) wiew;
            }
        }
        return null;
    }

    class ViewUserProfileRunnable implements Runnable {

        public void run() {

            UserProfileDialog userProfile = UserProfileDialog.getInstance(
                    Workbench.this.getContentPane(), runtime);

            userProfile.setLocationRelativeTo(Workbench.this.getRootFrame());
            userProfile.setVisible(true);
        }
    }

    private Runnable viewUserProfileRunnable = new ViewUserProfileRunnable();

    class LoginUserRunnable implements Runnable {

        boolean newVersionChecked = false;

        public void checkVersion(){

            try {
                URL versionURL = new URL(runtime.getBaseUrl() + "/version.jsp");

                Properties p = new Properties();

                p.loadFromXML(versionURL.openStream());

                String availableversion = (String) p.get("version");

                if (!availableversion.equals(SoftwareVersion.getVersion())) {
                    UpgradeMessage.newVersion(getContentPane(), runtime.getBaseUrl(),
                            SoftwareVersion.getVersion(), availableversion);
                }

            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }

        public void run() {

            if (!newVersionChecked) {
                checkVersion();
                newVersionChecked = true;
            }


            LoginTabbedContainer loginDialog = LoginTabbedContainer.getInstance(Workbench.this, runtime);

            loginDialog.setLocationRelativeTo(Workbench.this.getRootFrame());
            loginDialog.setVisible(true);

            if (loginDialog.getResult() == LoginTabbedContainer.CANCEL ||
                    runtime.getUser() == null) {

                return;
            } else {
                userLoggedIn();
                workbenchActions.get(WorkbenchActionType.OPENPROJECT).actionPerformed(null);
            }
        }
    }

    private Runnable loginUserRunnable = new LoginUserRunnable();

    class LogoutUserRunnable implements Runnable {
        boolean closed = false;

        public void run() {
            closed = true;

            if (runtime.getSession().getSessionid() != null) {
                closeProjectRunnable.run();
                closed = closeProjectRunnable.closed;
            }

            if (closed) {
                try {
                    HashMap<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("username", runtime.getUser().getUsername());
                    ServerRuntimeImpl.logoutUser(runtime.getSession(), parameters);

                    userLoggedOut();
                } catch (Exception ex) {
                }
            }
        }
    }

    private LogoutUserRunnable logoutUserRunnable = new LogoutUserRunnable();

    private void saveConsoleSnapshot() {

        Vector<Object> model = getConsole().getObjectModel();

        try {
            FileLoad.write(HexUtil.objectToBytes(model),
                    runtime.getProject().getAbsolutePath() + "/.RConsole", obtainR());
        } catch (Exception ex) {
            log.error("Error!", ex);
        }
    }

    private void loadConsoleSnapshot() {

        try {
            getConsole().clearScreen();

            byte[] data = FileLoad.read(runtime.getProject().getAbsolutePath() + "/.RConsole", obtainR());

            if (data.length > 0) {
                Object obj = HexUtil.bytesToObject(data);

                if (obj instanceof Vector) {
                    getConsole().setObjectModel((Vector<Object>) obj);
                }
            }

        } catch (Exception ex) {
            log.info("Error!", ex);
        }
    }

    /*
    private int DEFAULT_SCREENSHOT_SIZE = 80;
    */

    /*
    private void saveScreenshot() {

        //screenshot = ScreenCapture.captureScreen();

        try {

            BufferedImage img = GraphicsUtilities.toBufferedImage(
                    GraphicsUtilities.scaleImage(screenshot, DEFAULT_SCREENSHOT_SIZE));

            FileLoad.write(HexUtil.objectToBytes(img),
                    runtime.getProject().getAbsolutePath() + "/.RScreen", obtainR());
        } catch (Exception ex) {
            log.error("Error!", ex);
        }
    }
    */

    class CloseProjectRunnable implements Runnable {

        public boolean closed = false;

        public Operation closeProjectOp = opMan.createOperation("Closing Project..");

        private OperationStateListener opManStateListener = new OperationStateListener() {
            public void operationStarted(Operation o) {}
            public void operationCompleted(Operation o) {
                if (o.getId() == closeProjectOp.getId()) {
                    handleProjectClosure();
                }
            }

            public void operationAborted(Operation o) {
                if (o.getId() == closeProjectOp.getId()) {
                    handleProjectClosure();
                }
            }
        };

        public CloseProjectRunnable() {
            opMan.addOperationStateListener(opManStateListener);
        }

        public void run(){
            boolean save = false;

            closed = true;

            Vector<DynamicView> editorviews = (Vector<DynamicView>)
                    viewMan.getAllViewsOfClass(BasicEditorView.class).clone();

            for (DynamicView wiew : editorviews) {
                BasicEditorView beview = (BasicEditorView) wiew;
                beview.makeVisible();
                if (!beview.getEditor().handleWindowClosing()) {
                    closed = false;
                    return;
                }
                beview.close();
            }


            Object[] options = { "Save", "Discard", "Cancel" };

            int reply = JOptionPaneExt.showOptionDialog(Workbench.this.getContentPane(),
                    "Do you want to save project data ?\n" +
                    "(saved automatically in background)",
                    "Confirm Closure",
                    JOptionPaneExt.DEFAULT_OPTION,
                    JOptionPaneExt.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch(reply) {
                case 0: save = true; break;
                case 1: save = false; break;
                case 2:  closed = false; return;
                default: closed = false; return;
            }

            closeProjectOp.setEventUnsafe(true);
            closeProjectOp.startOperation();

            try {

                closeProjectOp.setProgress(10, "saving console");

                saveConsoleSnapshot();

                closeProjectOp.setProgress(20, "unregistering listeners");

                ServerLogView logview = getOpenedServerLogView();
                if (logview != null) {
                    try {
                        logListener.removeListener(logview.getListener());
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }
                }

                closeProjectOp.setProgress(40, "disconnecting devices");
                disconnectDevices();
                //if (!getRLock().isLocked()) {
                //}

                closeProjectOp.setProgress(60, "signing off");

                ServerRuntimeImpl.suspendProject(runtime.getSession(), save);

                closeProjectOp.completeOperation();

                /*
                String loggedOut = "Server: "+_servantAliasName+"\n";
                return "Logged Off\n"+loggedOut;
                */
            } catch (Exception ex) {
                handleConnectionException(ex);

            } finally {
                //noSession();
                closeProjectOp.abortOperation();
                runtime.setProject(null);
            }
        }

    }

    private CloseProjectRunnable closeProjectRunnable = new CloseProjectRunnable();

    class OpenProjectRunnable implements Runnable {

        public Operation openProjectOp = opMan.createOperation("Loading Project..");

        private OperationStateListener opManStateListener = new OperationStateListener() {
            public void operationStarted(Operation o) {}
            public void operationCompleted(Operation o) {
                if (o.getId() == openProjectOp.getId()) {
                    projectOpened();
                }
            }

            public void operationAborted(Operation o) {
                if (o.getId() == openProjectOp.getId()) {
                    handleProjectClosure();
                }
            }
        };

        public OpenProjectRunnable() {
            opMan.addOperationStateListener(opManStateListener);
        }

        public void run(){
            if (isRAvailable()) {
                return;
            }

            try {

                boolean showLoginDialog = true;

                ProjectBrowserDialog projectDialog = ProjectBrowserDialog.getInstance(
                        Workbench.this.getContentPane(),
                        runtime);

                if (showLoginDialog) {
                    projectDialog.setLocationRelativeTo(Workbench.this.getRootFrame());
                    projectDialog.setVisible(true);
                }

                //getObjectModel

                if (projectDialog.getResult() == ProjectBrowserDialog.CANCEL) {
                    return;
                }

                openProjectOp.startOperation("...");

                //_commandServletUrl = runtime.getUrl();

                String oldSessionId = runtime.getSession().getSessionid();

                HashMap<String, Object> parameters = new HashMap<String, Object>();

                parameters.put("wait", new Boolean(runtime.isWait()).toString());
                parameters.put("user", runtime.getUser());
                parameters.put("project", runtime.getProject());

                //options.put("save", "true");

                openProjectOp.setProgress(5, "creating server");

                String newSessionId = ServerRuntimeImpl.openProject(runtime.getSession(), parameters);

                runtime.getSession().setSessionid(newSessionId);

                if (newSessionId.equals(oldSessionId)) {
                    log.info("using exising session newSessionId = "+newSessionId);
                    throw new RuntimeException("session has not been released newSessionId = " + newSessionId);
                }

                openProjectOp.setProgress(10, "getting R services");

                _rInstance = ServerRuntimeImpl.getR(runtime.getSession(), stateCallbacks);

                //if (new File(Workbench.NEW_R_STUB_FILE).exists())
                //    new File(Workbench.NEW_R_STUB_FILE).delete();

                openProjectOp.setProgress(15, "loading console");

                loadConsoleSnapshot();

                openProjectOp.setProgress(20, "listing callback devices");

                // list or bind in Generic Callback Device
                //
                GenericCallbackDevice[] callbackdevices = _rInstance.listGenericCallbackDevices();

                if (callbackdevices.length == 0) {

                    openProjectOp.setProgress(25, "creating callback devices");

                    genericCallbackDevice = _rInstance.newGenericCallbackDevice();

                } else if (callbackdevices.length == 1) {
                    genericCallbackDevice = callbackdevices[0];

                } else {
                    genericCallbackDevice = callbackdevices[0];

                    log.error("More than 1 Generic Callback Device - " + callbackdevices.length + " devices");

                    for (GenericCallbackDevice gcd : callbackdevices) {
                        log.error(gcd.getId());
                    }

                    //throw new RuntimeException("More than one GCD length="+callbackdevices.length);
                }

                openProjectOp.setProgress(30, "setting callback listeners");

                // and genericRActionListener
                //
                genericCallbackDevice.addListener(genericRActionListenerImpl);

                openProjectOp.setProgress(40, "listing graphic devices");

                // list GD Devices

                Stack<GDDevice> deviceStack = new Stack<GDDevice>();

                GDDevice[] ldevices = _rInstance.listDevices();

                for (int i = ldevices.length - 1; i >= 0; --i) {
                    deviceStack.push(ldevices[i]);
                }


                openProjectOp.setProgress(50, "creating graphic devices");

                GDDevice d = null;

                final Vector<DeviceView> deviceViews = getDeviceViews();

                if (deviceStack.empty()) {

                    //log.info("_graphicPanel.getWidth()="+_graphicPanel.getWidth());
                    //log.info("_graphicPanel.getHeight()="+_graphicPanel.getHeight());

                    //d = _rInstance.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());

                    d = _rInstance.newDevice(CreateDeviceAction.DEFAULT_WIDTH, CreateDeviceAction.DEFAULT_HEIGHT);

                } else {

                    d = deviceStack.pop();
                }

                DeviceView dview = null;

                if (deviceViews.size() > 0) {
                    dview = deviceViews.remove(deviceViews.size()-1);
                } else {
                    dview = createEmptyDeviceView();
                }

                //JGDPanelPop gp = createJGDPanelPop((JPanel) dview.getComponent(), d);
                GDContainerPanel gp = createGDContainerPanel((JPanel) dview.getComponent(), d);

                dview.setPanel(gp);


                //_graphicPanel = createJGDPanelPop(_rootGraphicPanel, d);

                while(!deviceStack.empty()){

                    GDDevice newDevice = deviceStack.pop();

                    if (deviceViews.size() > 0) {
                        dview = deviceViews.remove(deviceViews.size()-1);
                    } else {
                        dview = createEmptyDeviceView();
                    }

                    //dview.setPanel(createJGDPanelPop((JPanel) dview.getComponent(), newDevice));
                    dview.setPanel(createGDContainerPanel((JPanel) dview.getComponent(), newDevice));
                }

                if (deviceViews.size() > 0) {
                    for(DeviceView devview : deviceViews) {
                        devview.close();
                    }

                    deviceViews.removeAllElements();
                }

                openProjectOp.setProgress(60, "deferred device activation");

                final GDDevice final_d0 = d;
                new Thread(new Runnable(){
                    public void run(){
                        try {
                            _currentDevice = final_d0;
                            setCurrentDevice(final_d0);

                            final_d0.setAsCurrentDevice();
                        } catch (RemoteException re) {
                        }
                    }
                }).start();

                openProjectOp.setProgress(70, "loading history");

                workbenchActions.get("loadhistory").actionPerformed(null);

                openProjectOp.setProgress(80, "registering logview");

                ServerLogView logview = getOpenedServerLogView();
                if (logview != null) {
                    logListener.addListener(logview.getListener());
                }

                openProjectOp.completeOperation();

                new Thread(new RunnerRunnable(new Runnable[]{
                        // load workspace
                        new LoadWorkspaceRunnable(Workbench.this),

                        // warm up package installer
                        //new InstallerWarmupRunnable(),

                        // check version
                        new VersionCheckerRunnable(Workbench.this)
                })).start();

            } catch (Exception ex) {
                handleConnectionException(ex);
                runtime.setProject(null);

            } finally {
                openProjectOp.abortOperation();
            }
        }
    }

    class RunnerRunnable implements Runnable {
        private Runnable[] executables;
        public RunnerRunnable(Runnable[] executables){
            this.executables = executables;
        }

        public void run(){
            for (Runnable executable : executables) {
                executable.run();
            }
        }
    }

    class InstallerWarmupRunnable implements Runnable {
        public void run(){
            PackageInstallerDialog dialog =
                    PackageInstallerDialog.getInstance(
                            Workbench.this.getRootFrame().getContentPane(),
                            Workbench.this);

            dialog.initAndWarmupRepositories();
        }
    }

    class VersionCheckerRunnable implements Runnable {
        private RGui rgui;
        public VersionCheckerRunnable(RGui rgui) {
            this.rgui = rgui;
        }
        public void run(){

            try {
                String serverversion = rgui.obtainR().getProperty("server.version");
                String benchversion = SoftwareVersion.getVersion();

                if (!serverversion.equals(benchversion)) {
                    UpgradeMessage.versionMismatch(rgui.getRootFrame(),
                            workbenchActions.get(WorkbenchActionType.SHUTDOWNSERVER), serverversion, benchversion);
                }

            } catch (RemoteException re) {
            }
        }
    }

    class LoadWorkspaceRunnable extends RActionHandlerAbstract implements Runnable {

        private RGui rgui;
        private boolean completed = false;
        private Thread currentthread = null;
        private String AVERAGERATEPROPERTY = "average-loading-rate";

        private long ONE_K = 1024;
        private long ONE_M = ONE_K * ONE_K;
        private long ONE_G = ONE_M * ONE_K;

        private String defaultaverageratestr = "12706722";

        public LoadWorkspaceRunnable(RGui rgui) {
            this.rgui = rgui;
        }

        public void actionPerformed(RAction action) {
            if (action.getAttributes().get(RActionConst.WORKSPACELOADED) != null) {
                completed = true;
                if (currentthread != null) currentthread.interrupt();
            }
        }

        public String secondsToTime(long timeseconds) {

            long minutes = timeseconds / 60;
            long seconds = timeseconds - (minutes * 60);

            return (minutes > 0 ? minutes + " minutes " : " ")
                     + seconds + " seconds";
        }

        public String cutdigits(String text, int digits) {
            String result = text;

            int dotindex = result.indexOf(".");

            if (dotindex != -1) {
                if (digits > 0 && Integer.parseInt(
                        result.substring(dotindex + 1, dotindex + digits + 1)) > 0) {

                    result = result.substring(0,
                            Math.min(dotindex + 1 + digits, text.length()));
                } else {
                    result = result.substring(0, dotindex);
                }
            }
            return result;
        }

        public String filesize2str(long length) {

            String result = null;

            if (length >= ONE_G) {
                double result0 = (double) length / (double) ONE_G;

                result = cutdigits(Double.toString(result0), 1) + "G";
            } else if (length >= ONE_M) {
                double result0 = (double) length / (double) ONE_M;

                result = cutdigits(Double.toString(result0), 1) + "M";
            } else {
                double result0 = (double) length / (double) ONE_K;

                result = cutdigits(Double.toString(result0), 1) + "K";
            }

            return result;
        }

        public long getAverageRate() {

            long averagerate = Long.parseLong(
                    propMan.getProperty(AVERAGERATEPROPERTY, defaultaverageratestr));

            long defaultaveragerate = Long.parseLong(defaultaverageratestr);

            if (averagerate < defaultaveragerate / 10) {
                averagerate = defaultaveragerate;
            }

            return averagerate;
        }

        public long getWorkspaceLength() {
            try {
                FileDescription fdesc = rgui.obtainR().getRandomAccessFileDescription(
                        runtime.getProject().getAbsolutePath() + "/.RData");

                if (fdesc != null) {
                    return fdesc.length();
                }

            } catch (Exception ex) {
            }

            return 0;
        }

        public void run(){

            // server is running no need to reload workspace
            if (runtime.getServer() != null) return;

            currentthread = Thread.currentThread();

            Operation workspaceOp =
                    rgui.getOpManager().createOperation("Loading Workspace ", false);

            long averagerate = getAverageRate();
            long workspacelength = getWorkspaceLength();
            long estimatedtime = (workspacelength / averagerate) + 1;
            String workspacelengthstr = filesize2str(workspacelength);

            if (workspacelength > 0) {

                try {
                    rgui.getRLock().lock();

                    workspaceOp.startOperation();

                    addRActionHandler(RActionType.COMPLETE, this);

                    long ct0 = System.currentTimeMillis();

                    rgui.obtainR().loadWorkspace(runtime.getProject().getAbsolutePath());

                    while(!completed) {

                        try {
                            long elapsedtime = (System.currentTimeMillis() - ct0)/1000;
                            int progres = Math.min((int) (100 * elapsedtime / estimatedtime), 100);
                            long timeleft = Math.max(estimatedtime - elapsedtime, 0);

                            String status = (timeleft > 0 ?
                                    "Loading " + workspacelengthstr + " workspace, "
                                            + secondsToTime(timeleft) + " remain" :

                                    "Still loading.. please wait");

                            workspaceOp.setProgress(progres, status);
                            Thread.sleep(1000);

                        } catch (InterruptedException ie) {
                        } catch(OperationCancelledException oce) {
                            break;
                        }
                    }

                    if (completed) {
                        propMan.setProperty(AVERAGERATEPROPERTY,
                                Long.toString((workspacelength * 1000 / (System.currentTimeMillis() - ct0 + 1))));
                    }

                    rgui.obtainR().reinitServer();

                } catch (Exception ex) {
                    log.error("Error!", ex);
                } finally {
                    removeRActionHandler(RActionType.COMPLETE, this);

                    workspaceOp.completeOperation();

                    rgui.getRLock().unlock();
                }
            }
        }
    }

    private OpenProjectRunnable openProjectRunnable = new OpenProjectRunnable();


    class ShutdownServerRunnable implements Runnable {

        public Operation shutdownServerOp = opMan.createOperation("Shutting Down R Server..");

        private OperationStateListener opManStateListener = new OperationStateListener() {
            public void operationStarted(Operation o) {}
            public void operationCompleted(Operation o) {
                if (o.getId() == shutdownServerOp.getId()) {
                    handleProjectClosure();
                }
            }

            public void operationAborted(Operation o) {
                if (o.getId() == shutdownServerOp.getId()) {
                    handleProjectClosure();
                }
            }
        };

        {
            opMan.addOperationStateListener(opManStateListener);
        }

        public void run(){
            boolean save = false;

            Object[] options = { "Save", "Discard", "Cancel" };

            int reply = JOptionPaneExt.showOptionDialog(Workbench.this.getContentPane(),
                    "Do you want to save the project before shutting down ?\n" +
                    "(project data will be saved automatically in background)",
                    "Save?",
                    JOptionPaneExt.DEFAULT_OPTION,
                    JOptionPaneExt.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch(reply) {
                case 0: save = true; break;
                case 1: save = false; break;
                case 2: return;
                default: return;
            }

            try {
                shutdownServerOp.setEventUnsafe(true);

                shutdownServerOp.startOperation("shutting down");

                shutdownServerOp.setProgress(10, "saving console");

                saveConsoleSnapshot();

                shutdownServerOp.setProgress(20, "unregistering listeners");

                ServerLogView logview = getOpenedServerLogView();
                if (logview != null) {
                    try {
                        logListener.removeListener(logview.getListener());
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }
                }

                shutdownServerOp.setProgress(40, "disconnecting devices");

                disconnectDevices();
                //if (!getRLock().isLocked()) {
                //}

                shutdownServerOp.setProgress(80, "logging off");

                ServerRuntimeImpl.shutdownProject(runtime.getSession(), save);

                shutdownServerOp.completeOperation();

            } catch (Exception ex) {
                handleConnectionException(ex);

            } finally {
                shutdownServerOp.abortOperation();
            }
        }
    }

    private ShutdownServerRunnable shutdownServerRunnable = new ShutdownServerRunnable();

	private void initWorkbenchActions() {

        workbenchActions.put(WorkbenchActionType.LOADHISTORY, new LoadHistoryAction(this, "Load History"));
        workbenchActions.put(WorkbenchActionType.SAVEHISTORY, new SaveHistoryAction(this, "Save History"));
        workbenchActions.put(WorkbenchActionType.IMPORTFILE, fileBrowser.getActions().get("import"));
        workbenchActions.put(WorkbenchActionType.EXPORTFILE, fileBrowser.getActions().get("export"));

		workbenchActions.put(WorkbenchActionType.STOPEVAL,      new StopEvalAction(this, "Stop R"));
		workbenchActions.put(WorkbenchActionType.INTERRUPTEVAL, new InterruptEvalAction(this, "Interrupt Server Call"));
		workbenchActions.put(WorkbenchActionType.HELP,          new InvokeHelpAction(this, "Help Contents"));
		workbenchActions.put(WorkbenchActionType.RBIOCMANUAL,   new RBiocManualAction(this, "R/Bioconductor Manual"));
		workbenchActions.put(WorkbenchActionType.GRAPHTASKPAGE, new GraphTaskPageAction(this, "Graphics Task Page"));
		workbenchActions.put(WorkbenchActionType.GRAPHGALERY,   new GraphGaleryAction(this, "The R Graph Gallery"));
		workbenchActions.put(WorkbenchActionType.RGRAPHMANUAL,  new RGraphManualAction(this, "R Graphical Manual"));
		workbenchActions.put(WorkbenchActionType.ABOUT,     new ShowAboutAction(this, "About R Bench"));
		workbenchActions.put(WorkbenchActionType.PLAYDEMO,  new PlayDemoAction(this, "Play Demo"));
		workbenchActions.put(WorkbenchActionType.SAVEIMAGE, new SaveImageAction(this, "Save Workspace"));
		workbenchActions.put(WorkbenchActionType.LOADIMAGE, new LoadImageAction(this, "Load Workspace"));
		workbenchActions.put(WorkbenchActionType.EXTRACONSOLE, new ExtraConsoleAction(this, "Extra Console"));
		workbenchActions.put(WorkbenchActionType.SHELLCONSOLE, new ShellConsoleAction(this, "Shell"));
		workbenchActions.put(WorkbenchActionType.NEWEDITOR,    new InvokeEditorAction(this, "New Editor"));
        workbenchActions.put(WorkbenchActionType.OPENFUNCTION, new OpenFunctionAction(this, "Open Function"));
        workbenchActions.put(WorkbenchActionType.OPENFILE,     new OpenFileAction(this, "Open File"));
        workbenchActions.put(WorkbenchActionType.REQUESTHELP,  new RequestHelpAction(this, "Request Help"));
        workbenchActions.put(WorkbenchActionType.OBJECTEXPLORER, new ObjectExplorerAction(this, "Object Explorer"));
        workbenchActions.put(WorkbenchActionType.FITDEVICE,      new FitDeviceAction(this, "Fit Device To Panel"));
		workbenchActions.put(WorkbenchActionType.CREATEDEVICE, new CreateDeviceAction(this, "New Device"));

		workbenchActions.put(WorkbenchActionType.SERVERLOG, new AbstractAction("Server Log") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerLogView() == null) {

                    final ServerLogView lv = new ServerLogView("Server Log Viewer",
                            null, viewMan.getDynamicViewId(), logListener.getStorage());

                    ((TabWindow) views[1].getWindowParent()).addTab(lv);

                    logListener.addListener(lv.getListener());

                    lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
                            logListener.removeListener(lv.getListener());
                            lv.dispose();
						}
					});
				}
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		workbenchActions.put(WorkbenchActionType.OPENDEVICE, new AbstractAction("Open Devices") {
			public void actionPerformed(final ActionEvent event) {

                Vector<DeviceView> views = getDeviceViews();

                Point location = getLocationOnScreen();

                for(DeviceView view : views){

                    if (view.isVisible() && view.isValid()) {

                        // nothig
                    } else {
                        int rnd = (int) (Math.random() * 50);

                        FloatingWindow window = getBenchRootWindow().
                                createFloatingWindow(new Point(location.x + rnd, location.y + rnd),
                                        view.getPreferredSize(), view);

                        window.getTopLevelAncestor().setVisible(true);

                    }

                    //window.setVisible(true);
                    //getMainTabWindow().addTab(v);
                }
			}

			@Override
			public boolean isEnabled() {
                return (isRAvailable());
			}
		});

		workbenchActions.put(WorkbenchActionType.WORKBENCHLOG, new AbstractAction("Workbench Log") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedWorkbenchLogView() == null) {

                    final LogViewerView lview = new LogViewerView("Workbench Log Viewer",
                            null, viewMan.getDynamicViewId(), Workbench.this);

                    ((TabWindow) views[1].getWindowParent()).addTab(lview);

                    lview.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
                            lview.dispose();
						}

					});
				}
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});


		workbenchActions.put(WorkbenchActionType.QUIT, new AbstractAction("Quit") {
			public void actionPerformed(final ActionEvent e) {
                if (handleWorkbenchClosure() == 1) {
                    System.exit(0);
                }
			}

			@Override
			public boolean isEnabled() {
				return (!opMan.isLocked());
			}
		});

		workbenchActions.put(WorkbenchActionType.SESSIONINFO, new AbstractAction("Show R Server Info") {
			public void actionPerformed(ActionEvent e) {
				try {
					String sessionMode = null;
					if (runtime.getMode() == RuntimeEnvironment.HTTP_MODE)
						sessionMode = "CONNECT TO HTTP";
					else if (runtime.getMode() == RuntimeEnvironment.RMI_MODE)
						sessionMode = "CONNECT TO RMI";
					else if (runtime.getMode() == RuntimeEnvironment.NEW_R_MODE)
						sessionMode = "NEW R";

                    String name = obtainR().getServantName();
                    String a_name = AliasMap.getServerAliasName(name);

					getConsoleLogger().printAsOutput("Session Mode :" + sessionMode + "\n");
					getConsoleLogger().printAsOutput("Server Name :" + name + " " + a_name + "\n");
					getConsoleLogger().printAsOutput("Server Process ID :" + obtainR().getProcessId() + "\n");
					getConsoleLogger().printAsOutput("Server Host IP :" + obtainR().getHostIp() + "\n");
					getConsoleLogger().printAsOutput("STUB : \n" + obtainR().getStub() + "\n");

				} catch (Exception ex) {
                    log.error("Error!", ex);
				}
			}

			public boolean isEnabled() {
				return (isRAvailable() && !opMan.isLocked());
			}

		});


        workbenchActions.put(WorkbenchActionType.MESSAGEDEV, new SendFeedbackAction("Send Feedback",
                Workbench.this.getContentPane(), getRuntime()));

        workbenchActions.put(WorkbenchActionType.REPORTABUG, new SendBugReportAction("Report A Bug",
                Workbench.this.getContentPane(), getRuntime()));

        workbenchActions.put(WorkbenchActionType.PACKAGEINSTALLER, new AbstractAction("Package Installer") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(new Runnable(){
                    public void run(){
                        PackageInstallerDialog dialog =
                                PackageInstallerDialog.getInstance(
                                        Workbench.this.getRootFrame().getContentPane(),
                                        Workbench.this);

                        //dialog.setLocationRelativeTo(Workbench.this.getRootFrame());
                        dialog.setVisible(true);
                    }
                }).start();
            }

            @Override
            public boolean isEnabled() {
                return (isRAvailable() && !opMan.isLocked());
            }
        });

        workbenchActions.put(WorkbenchActionType.PACKAGEMANAGER, new AbstractAction("Package Manager") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(new Runnable(){
                    public void run(){
                        PackageManagerDialog dialog =
                                PackageManagerDialog.getInstance(
                                        Workbench.this.getRootFrame().getContentPane(),
                                        Workbench.this);

                        //dialog.setLocationRelativeTo(Workbench.this.getRootFrame());
                        dialog.setVisible(true);
                    }
                }).start();
            }

            @Override
            public boolean isEnabled() {
                return (isRAvailable() && !opMan.isLocked());
            }
        });

        workbenchActions.put(WorkbenchActionType.PROPERTYBROWSER, new AbstractAction("Property Browser") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(new Runnable(){
                    public void run(){
                        PropertyBrowser browser =
                                PropertyBrowser.getInstance(Workbench.this.getRootFrame(), Workbench.this);

                        browser.setVisible(true);
                    }
                }).start();
            }

            @Override
            public boolean isEnabled() {
                return (isRAvailable());
            }
        });

        workbenchActions.put(WorkbenchActionType.VIEWPROFILE, new AbstractAction("Profile") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(viewUserProfileRunnable).start();
            }

            @Override
            public boolean isEnabled() {
                return (runtime.getUser() != null && !opMan.isLocked());
            }
        });

        workbenchActions.put(WorkbenchActionType.LOGINUSER, new AbstractAction("Login") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(loginUserRunnable).start();
            }

            @Override
            public boolean isEnabled() {
                return (runtime.getUser() == null && !opMan.isLocked());
            }
        });

        workbenchActions.put(WorkbenchActionType.LOGOUTUSER, new AbstractAction("Logout") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(logoutUserRunnable).start();
            }

            @Override
            public boolean isEnabled() {
                return (runtime.getUser() != null && !opMan.isLocked());
            }
        });

        workbenchActions.put(WorkbenchActionType.OPENPROJECT, new AbstractAction("Open Project") {
            public void actionPerformed(final ActionEvent e) {

                new Thread(openProjectRunnable).start();
            }

            @Override
            public boolean isEnabled() {
                return (runtime.getUser() != null && !isRAvailable() && !opMan.isLocked());
            }
        });

        workbenchActions.put(WorkbenchActionType.CLOSEPROJECT, new AbstractAction("Close Project") {
            public void actionPerformed(ActionEvent e) {
                new Thread(closeProjectRunnable).start();
            }

            public boolean isEnabled() {
                return runtime.getUser() != null && isRAvailable() && !opMan.isLocked();
            }
        });

        workbenchActions.put(WorkbenchActionType.SHUTDOWNSERVER, new AbstractAction("Shutdown Server") {
            public void actionPerformed(ActionEvent e) {
                new Thread(shutdownServerRunnable).start();
            }

            public boolean isEnabled() {
                return runtime.getUser() != null && isRAvailable() && !opMan.isLocked();
            }
        });

	}


    private JMenuItem makeMenuItem(AbstractAction action, String iconPath, KeyStroke ks) {
        JMenuItem menuItem = new JMenuItem();

        if (action != null)   menuItem.setAction(action);
        if (iconPath != null) menuItem.setIcon(new ImageIcon(ImageLoader.load(iconPath)));
        if (ks != null)       menuItem.setAccelerator(ks);

        return menuItem;
    }

    private void updateMenuState(JMenu menu) {
        if (menu == null) return;

        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem mi = menu.getItem(i);
            if (mi != null) {
                if (mi instanceof JMenu) {
                    updateMenuState((JMenu) mi);
                } else {
                    Action action = mi.getAction();
                    if (action != null) {
                        mi.setEnabled(action.isEnabled());
                    }
                }
            }
        }
    }

    private void initWorkbenchMenus(){

        WorkbenchMenuListener wbMenuListener = new WorkbenchMenuListener();

        JMenuBar menuBar = new JMenuBar();

        sessionMenu = new JMenu("Session");

        JMenuItem loginUser = makeMenuItem(workbenchActions.get(WorkbenchActionType.LOGINUSER),
                "/menu/images/sessionmenu/user.png", null);

        JMenuItem logoutUser = makeMenuItem(workbenchActions.get(WorkbenchActionType.LOGOUTUSER),
                "/menu/images/sessionmenu/user_go.png", null);

        JMenuItem viewProfile = makeMenuItem(workbenchActions.get(WorkbenchActionType.VIEWPROFILE),
                "/menu/images/sessionmenu/user_business_boss.png", null);


        sessionMenu.add(loginUser);
        sessionMenu.add(logoutUser);
        sessionMenu.addSeparator();
        sessionMenu.add(viewProfile);
        sessionMenu.addMenuListener(wbMenuListener);

        menuBar.add(sessionMenu);

        projectMenu = new JMenu("Project");

        JMenuItem openProject = makeMenuItem(workbenchActions.get(WorkbenchActionType.OPENPROJECT),
                "/menu/images/projectmenu/brick_add.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK));

        JMenuItem closeProject = makeMenuItem(workbenchActions.get(WorkbenchActionType.CLOSEPROJECT),
                "/menu/images/projectmenu/brick_delete.png", null);

        JMenuItem termProject = makeMenuItem(workbenchActions.get(WorkbenchActionType.SHUTDOWNSERVER),
                "/menu/images/projectmenu/cross.png", null);


        JMenuItem serverLog = makeMenuItem(workbenchActions.get(WorkbenchActionType.SERVERLOG),
                "/menu/images/projectmenu/drawer.png", null);

        JMenuItem benchLog = makeMenuItem(workbenchActions.get(WorkbenchActionType.WORKBENCHLOG),
                "/menu/images/projectmenu/accessories-text-editor.png", null);


        JMenuItem quitBench = makeMenuItem(workbenchActions.get(WorkbenchActionType.QUIT),
                "/menu/images/projectmenu/accept.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_MASK));

        projectMenu.add(openProject);
        projectMenu.add(closeProject);
        projectMenu.add(termProject);
        projectMenu.addSeparator();

        projectMenu.add(workbenchActions.get(WorkbenchActionType.LOADIMAGE));
        projectMenu.add(workbenchActions.get(WorkbenchActionType.SAVEIMAGE));
        projectMenu.addSeparator();

        projectMenu.add(workbenchActions.get(WorkbenchActionType.IMPORTFILE));
        projectMenu.add(workbenchActions.get(WorkbenchActionType.EXPORTFILE));
        projectMenu.addSeparator();

        projectMenu.add(workbenchActions.get(WorkbenchActionType.SESSIONINFO));

        JMenu logMenu = new JMenu("Logs");
        logMenu.add(serverLog);
        logMenu.add(benchLog);

        projectMenu.add(logMenu);
        projectMenu.addSeparator();

        projectMenu.add(quitBench);

        projectMenu.addMenuListener(wbMenuListener);

        menuBar.add(projectMenu);


        packageMenu = new JMenu("Packages");

        JMenuItem packageInstaller = makeMenuItem(workbenchActions.get(WorkbenchActionType.PACKAGEINSTALLER),
                "/menu/images/packagemenu/package_add.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_I, KeyEvent.META_MASK));

        JMenuItem packageManager = makeMenuItem(workbenchActions.get(WorkbenchActionType.PACKAGEMANAGER),
                "/menu/images/packagemenu/package_green.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_M, KeyEvent.META_MASK));


        packageMenu.add(packageInstaller);
        packageMenu.add(packageManager);

        packageMenu.addMenuListener(wbMenuListener);

        menuBar.add(packageMenu);

        //ApplicationListener i;


        appboxMenu = new JMenu("Apps");

        appboxMenu.add(makeMenuItem(workbenchActions.get(WorkbenchActionType.NEWEDITOR),
                "/menu/images/appmenu/page_white_edit.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_E, KeyEvent.META_MASK)));

        appboxMenu.add(makeMenuItem(workbenchActions.get(WorkbenchActionType.OBJECTEXPLORER),
                "/menu/images/appmenu/application_form_magnify.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_P, KeyEvent.META_MASK)));

        appboxMenu.add(makeMenuItem(workbenchActions.get(WorkbenchActionType.EXTRACONSOLE),
                "/menu/images/appmenu/application_osx_terminal.png", null));

        String dev = System.getProperty("developer");

        if (dev != null && dev.equalsIgnoreCase("true")) {

            appboxMenu.add(makeMenuItem(workbenchActions.get(WorkbenchActionType.SHELLCONSOLE),
                    "/menu/images/appmenu/application_osx_terminal.png", null));

            appboxMenu.add(makeMenuItem(workbenchActions.get(WorkbenchActionType.PROPERTYBROWSER),
                    "/menu/images/appmenu/cog_edit.png", null));
        }

        appboxMenu.addMenuListener(wbMenuListener);


        menuBar.add(appboxMenu);

        graphicsMenu = new JMenu("Graphics");

        JMenuItem newDevice = makeMenuItem(workbenchActions.get(WorkbenchActionType.CREATEDEVICE),
                "/menu/images/devicemenu/monitor_add.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_D, KeyEvent.META_MASK + KeyEvent.SHIFT_MASK ));

        JMenuItem fitDevice = makeMenuItem(workbenchActions.get(WorkbenchActionType.FITDEVICE),
                "/menu/images/devicemenu/arrow_out.png", null);

        JMenuItem openDevice = makeMenuItem(workbenchActions.get(WorkbenchActionType.OPENDEVICE),
                "/menu/images/devicemenu/application_form_add.png", null);

        graphicsMenu.removeAll();

        graphicsMenu.add(newDevice);
        graphicsMenu.add(fitDevice);
        graphicsMenu.add(openDevice);


        graphicsMenu.addMenuListener(wbMenuListener);

        menuBar.add(graphicsMenu);

        helpMenu = new JMenu("Help");

        helpMenu.add(workbenchActions.get("help"));

        JMenuItem sendMessage = makeMenuItem(workbenchActions.get(WorkbenchActionType.MESSAGEDEV),
                "/menu/images/helpmenu/comment.png", null);

        JMenuItem sendBugreport = makeMenuItem(workbenchActions.get(WorkbenchActionType.REPORTABUG),
                "/menu/images/helpmenu/bug2.png", null);

        helpMenu.add(sendMessage);
        helpMenu.add(sendBugreport);
        helpMenu.addSeparator();

        //helpMenu.add(workbenchActions.get("rbiocmanual"));
        //helpMenu.add(workbenchActions.get("graphicstaskpage"));
        //helpMenu.add(workbenchActions.get("thergraphgallery"));
        //helpMenu.add(workbenchActions.get("rgraphicalmanual"));

        helpMenu.add(workbenchActions.get(WorkbenchActionType.ABOUT));
        
        helpMenu.addMenuListener(wbMenuListener);

        menuBar.add(helpMenu);

        /*
        JMenu lafMenu = new JMenu("LAF");

        LookAndFeelInfo[] lafs = installedLFs;// UIManager.getInstalledLookAndFeels();

        for (LookAndFeelInfo laf : lafs) {
            final LookAndFeelInfo laf_final = laf;

            lafMenu.add(new AbstractAction(laf_final.getName()) {
                public void actionPerformed(ActionEvent event) {
                    try {
                        UIManager.setLookAndFeel(laf_final.getClassName());
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }
                }
            });
        }

        menuBar.add(lafMenu);
        */

        setJMenuBar(menuBar);

    }

    class WorkbenchMenuListener implements MenuListener {
        public void menuSelected(MenuEvent event) {
            updateMenuState((JMenu)event.getSource());
        }
        public void menuCanceled(MenuEvent event) {
        }

        public void menuDeselected(MenuEvent event) {
        }
    }


    private void disconnectDevicesSafe() {
        //log.info("Workbench-disconnectDevicesSafe");
        
        try {

            if (genericCallbackDevice != null) {
                if (genericCallbackDevice instanceof HttpMarker) {
                    ((HttpMarker) genericCallbackDevice).stopThreads();
                }
                /*
                try {
                    genericCallbackDevice.removeListener(genericRActionListenerImpl);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                */
            }

            if (_graphicPanel != null) {
                if (_graphicPanel instanceof JGDPanelPop) {
                    ((JGDPanelPop) _graphicPanel).disconnect();
                }
            }

            Vector<DeviceView> deviceViews = getDeviceViews();
            for (int i = 0; i < deviceViews.size(); ++i) {
                deviceViews.elementAt(i).getPanel().disconnect();
            }

            /*
            Vector<CollaborativeSpreadsheetView> collaborativeSpreadsheetViews = getCollaborativeSpreadsheetViews();
            for (int i = 0; i < collaborativeSpreadsheetViews.size(); ++i) {
                collaborativeSpreadsheetViews.elementAt(i).close();
            }
            */

            /*
            try {
                _rInstance.removeRConsoleActionListener(_rConsoleActionListenerImpl);
                UnicastRemoteObject.unexportObject(_rConsoleActionListenerImpl, false);
                _rConsoleActionListenerImpl = null;
            } catch (Exception ex) {
            }
            */

            /*
            try {
                _rInstance.removeRCollaborationListener(_collaborationListenerImpl);
                UnicastRemoteObject.unexportObject(_collaborationListenerImpl, false);
                _collaborationListenerImpl = null;
            } catch (Exception ex) {
            }
            */

        } catch (Exception ex) {
            log.error("Error!", ex);
        }

    }

	private void disconnectDevices() {
        //log.info("Workbench-disconnectDevices");

        if (genericCallbackDevice != null) {
            if (genericCallbackDevice instanceof HttpMarker) {
                ((HttpMarker) genericCallbackDevice).stopThreads();
            }
            /*
            try {
                genericCallbackDevice.removeListener(genericRActionListenerImpl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            */
        }

        if (_graphicPanel != null) {
            if (_graphicPanel instanceof JGDPanelPop) {
                ((JGDPanelPop) _graphicPanel).disconnect();
            }
        }

        Vector<DeviceView> deviceViews = getDeviceViews();
        for (int i = 0; i < deviceViews.size(); ++i) {
            deviceViews.elementAt(i).getPanel().disconnect();
        }

        /*
        try {
			_rInstance.removeRConsoleActionListener(_rConsoleActionListenerImpl);
			UnicastRemoteObject.unexportObject(_rConsoleActionListenerImpl, false);
			_rConsoleActionListenerImpl = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		/*
        try {
			_rInstance.removeRCollaborationListener(_collaborationListenerImpl);
			UnicastRemoteObject.unexportObject(_collaborationListenerImpl, false);
			_collaborationListenerImpl = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		/*
        if (obtainR() instanceof HttpMarker) {
			((HttpMarker) obtainR()).stopThreads();
		} else {
			try {
				if (_rInstance.hasRCollaborationListeners()) {
					((JGDPanelPop) _graphicPanel).dispose();
					for (int i = 0; i < deviceViews.size(); ++i)
						deviceViews.elementAt(i).getPanel().dispose();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/

	}

    /*
	private void setInteractor(int interactor) {
		((JGDPanelPop) _graphicPanel).setInteractor(interactor);
		Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i)
			deviceViews.elementAt(i).getPanel().setInteractor(interactor);
	}
	*/

	public GDContainerPanel getCurrentJGPanelPop() {
        Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i) {
			if (deviceViews.elementAt(i).getPanel().getGdDevice() == _currentDevice) {
				return deviceViews.elementAt(i).getPanel();
			}
		}
        return null;
	}


    public synchronized void invalidateSession() {
        //log.info("Workbench-invalidateSession");
        runtime.getSession().setSessionid(null);

        _rInstance = null;
        genericCallbackDevice = null;

        updateConsoleIcon(_disconnectedIcon);
    }

    public void handleServerFailure() {
        //log.info("Workbench-handleServerFailure");
        try {
            disconnectDevicesSafe();
        } finally {
            invalidateSession();
            projectClosed();
        }
    }

    public void handleProjectClosure() {
        //log.info("Workbench-handleProjectClosure");
        try {
            disconnectDevices();
        } catch (Exception e) {
            log.error("Error!", e);
        } finally {
            invalidateSession();
            projectClosed();            
        }

    }

    public void noSession() {
        invalidateSession();
    }

	public static class PopupListener extends MouseAdapter {
		private JPopupMenu popup;

		public PopupListener(JPopupMenu popup) {
			this.popup = popup;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		protected void maybeShowPopup(MouseEvent e) {
			if (popup.isPopupTrigger(e)) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public String getLookAndFeelClassName() {
        return installedLFs[_lf].getClassName();
	}

    public static void setupLookAndFeel(){

        Runnable setupLookAndFeelRunnable = new Runnable() {
            public void run(){
                String lfString = System.getProperty("lf");

                UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();

                String className = lfString != null ?
                        lafs[ Math.min(lafs.length-1, Integer.parseInt(lfString)) ].getClassName() :
                        UIManager.getSystemLookAndFeelClassName();
                try {
                    UIManager.setLookAndFeel(className);
                } catch (Exception e) {
                    log.error("Error!", e);
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            setupLookAndFeelRunnable.run();
  		} else {
            try {
                log.info("SwingUtilities.invokeAndWait(setupLookAndFeelRunnable) - UIManager.setLookAndFeel");
                SwingUtilities.invokeAndWait(setupLookAndFeelRunnable);
            } catch (Exception e) {
                log.error("Error!", e);
            }
        }

    }

	public String getSessionId() {
		return runtime.getSession().getSessionid();
	}

	
	public String getHelpRootUrl() {
		return runtime.getHelpUrl();
	}

    // RKit

    public boolean isRAvailable() {
        return _rInstance != null;
    }
    public boolean isRForFilesAvailable() {
        return _rInstance != null;
    }

    public RServices obtainR() {
        return _rInstance;
    }

    public RServices obtainRUnsafe() {
        return _rInstance;
    }

    public RServices obtainRForFiles() {
        return _rInstance;
    }

	public ReentrantLock getRLock() {
		return _protectR;
	}

	public ConsoleLogger getConsoleLogger() {
		return _consoleLogger;
	}

	public String getInstallDir() {
		return ""; //ServerManager.INSTALL_DIR;
	}

	public GDDevice getCurrentDevice() {
		return _currentDevice;
	}

	public void setCurrentDevice(GDDevice device) {

        /*
		JGDPanelPop lastCurrentPanel = getCurrentJGPanelPop();
        if (lastCurrentPanel != null) {
            int interactor = lastCurrentPanel.getInteractor();
            boolean showCoordinates = lastCurrentPanel.isShowCoordinates();
            lastCurrentPanel.setInteractor(INTERACTOR_NULL);
            lastCurrentPanel.setShowCoordinates(false);
            lastCurrentPanel.removeAllCoupledTo();
        }

		try {
			if (_currentDevice.hasLocations())
				safeConsoleSubmit("locator()");
		} catch (Exception e) {
            log.error("Error!", e);
		}
		*/

		_currentDevice = device;

        Vector<DeviceView> deviceViews = getDeviceViews();

        for (int i = 0; i < deviceViews.size(); i++) {
            deviceViews.elementAt(i).getViewProperties().setIcon(_inactiveDeviceIcon);
        }

        for (int i = 0; i < deviceViews.size(); i++) {
            DeviceView dv = deviceViews.elementAt(i);
            if (dv.getPanel().getGdDevice() == _currentDevice) {
                dv.getViewProperties().setIcon(_currentDeviceIcon);
                //dv.getPanel().setInteractor(interactor);
                //dv.getPanel().setShowCoordinates(showCoordinates);
                break;
            }
        }
	}

	public Component getRootComponent() {
		return getContentPane();
	}

	String safeConsoleSubmit(final String cmd) throws RemoteException {
		if (getRLock().isLocked()) {
			return "R is busy, please retry\n";
		}
		try {
			getRLock().lock();

			final String log = _rInstance.consoleSubmit(cmd);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getConsoleLogger().print(cmd, log);
				}
			});
			return log;

		} finally {
			getRLock().unlock();
		}

	}

	public View createView(final Component panel, final String title) {
		final View[] result = new View[1];
		Runnable createRunnable = new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(getLookAndFeelClassName());
				} catch (Exception e) {
                    log.error("Error!", e);
				}
				SwingUtilities.updateComponentTreeUI(panel);

				DynamicView v = new DynamicView(title, null, panel,
                        viewMan.getDynamicViewId());

                ((TabWindow) views[1].getWindowParent()).addTab(v);
				result[0] = v;
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			createRunnable.run();
		} else {
			try {
                log.info("SwingUtilities.invokeAndWait(createRunnable) - UIManager.setLookAndFeel");
				SwingUtilities.invokeAndWait(createRunnable);
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
		return result[0];

	}

	public static Component getComponentParent(Component comp, Class<?> clazz) {
		for (;;) {
			if (comp == null)
				break;

			if (comp instanceof JComponent) {
				Component real = (Component) ((JComponent) comp).getClientProperty("KORTE_REAL_FRAME");
				if (real != null)
					comp = real;
			}

			if (clazz.isAssignableFrom(comp.getClass()))
				return comp;
			else if (comp instanceof JPopupMenu) {
				comp = ((JPopupMenu) comp).getInvoker();
			}

			// cut dependency on jEdit
			/*
			 * else if (comp instanceof FloatingWindowContainer) { comp =
			 * ((FloatingWindowContainer) comp).getDockableWindowManager(); }
			 */else
				comp = comp.getParent();
		}
		return null;

	}

	//public void upload(File localFile, String fileName) throws Exception {
	//	FileLoad.upload(localFile, fileName, obtainR());
	//}

	private String uid = null;

	public String getUID() {
		if (uid == null) {
			uid = UUID.randomUUID().toString();
		}
		return uid;
	}

    class LocalGenericRActionListenerImpl implements GenericRActionListener {

        public void pushActions(Vector<RAction> ractions) throws RemoteException {
            if (ractions != null && ractions.size() > 0) {

                for (int i = 0; i < ractions.size(); ++i) {
                    RAction action = ractions.elementAt(i);

                    String name = action.getActionName();

                    //log.info(action.toString());

                    Vector<RActionHandlerInterface>
                            handlerlist = (Vector<RActionHandlerInterface>) rActionHandlerMap.get(name).clone();

                    if (handlerlist != null) {
                        for (RActionHandlerInterface handler : handlerlist) {
                            handler.actionPerformed(action);
                        }
                    } else {
                        throw new RuntimeException("action not supported " + name);
                    }
                }
            }
        }
    }

	public String getUserName() {
		return System.getProperty("user.name");
	}

    class ServerLogRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            logListener.write((String) action.getAttributes().get(RActionConst.LOGTEXT));
        }
    }

    class CollaborationPrintActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {

            String sourceUID= (String)action.getAttributes().get(RActionConst.SOURCEUID);
            String user= (String)action.getAttributes().get(RActionConst.USER);
            String expression= (String)action.getAttributes().get(RActionConst.COMMAND);
            String result= (String)action.getAttributes().get(RActionConst.RESULT);

            if (!getUID().equals(sourceUID)) {
                consolePanel.print(expression == null ? null : "[" + user + "] - " + expression, result);
            }
        }
    }

    class HelpRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            final String url = (String) action.getAttributes().get(RActionConst.URL);

            //log.info("url=", url);

            new Thread(new Runnable() {
                public void run() {
                    UserDataDAO user = getRuntime().getUser();

                    if (user != null) {

                        if (url == null) {
                            setHelpBrowserURL(getHelpRootUrl() + "/doc/html/index.html");
                        } else {
                            setHelpBrowserURL(getHelpRootUrl() + url);
                        }

                    } else {
                    }
                }
            }).start();
        }
    }

    class BrowseRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            String url = (String) action.getAttributes().get(RActionConst.URL);

            log.info("BrowseRActionHandler-url0=" + url);

            /*
            url = url.substring(url.indexOf("//") + 2);

            log.info("BrowseRActionHandler-url1=" + url);

            url = url.substring(url.indexOf("/"));

            log.info("BrowseRActionHandler-url2=" + url);
            */

            final String url_final = url;

            new Thread(new Runnable() {
                public void run() {
                    setHelpBrowserURL(url_final);

                    /*
                    if (url_final == null) {
                        setHelpBrowserURL(getHelpRootUrl() + "/doc/html/index.html");
                    } else {
                        //setHelpBrowserURL(getHelpRootUrl() + url_final);
                        setHelpBrowserURL(getHelpRootUrl() + url_final);
                    }
                    */
                }
            }).start();
        }
    }


    class QuitRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            new Thread(new Runnable(){
                public void run() {
                    workbenchActions.get("quit").actionPerformed(null);
                }
            }).start();
        }
    }

    class EditRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            workbenchActions.get("openfunction").actionPerformed(
                    new ActionEvent(this, 0,
                            (String)action.getAttributes().get(RActionConst.FUNCTION)));
        }
    }

    class AsyncSubmitRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            /*
             * SwingUtilities.invokeLater(new Runnable() { public void
             * run() { consolePanel.print(null, (String)
             * action.getAttributes().get(RActionConst.RESULT)); } });
             */
        }
    }

    class UserInputRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        InDialog dialog = new InDialog(Workbench.this.getRootPane(),
                                "  R Console Input  ", new String[] { "" });
                        dialog.setVisible(true);
                        if (dialog.getExpr() != null)
                            obtainR().setUserInput(dialog.getExpr());
                        else
                            obtainR().setUserInput("");
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }
                }
            });
        }
    }

    class ConsoleLogRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {

            String originatorID = (String) action.getAttributes().get(RActionConst.ORIGINATOR);

            if (originatorID != null) {
                RConsole console = consoleList.get(originatorID);

                if (console != null) {
                    console.print((String) action.getAttributes().get(RActionConst.OUTPUT));
                } else {
                    log.info("ConsoleLogRActionHandler-unknown originatorID " + originatorID);
                }
            }
        }
    }

    class ConsoleContRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {

            String originatorID = (String) action.getAttributes().get(RActionConst.ORIGINATOR);

            if (originatorID != null) {
                RConsole console = consoleList.get(originatorID);

                if (console != null) {
                    console.print((String) action.getAttributes().get(RActionConst.OUTPUT) + "\n");
                } else {
                    log.info("ConsoleContRActionHandler-unknown originatorID " + originatorID);
                }
            }
        }
    }

    class PromptRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            String prompt = (String) action.getAttributes().get(RActionConst.PROMPT);
            String originator = (String) action.getAttributes().get(RActionConst.ORIGINATOR);

            if (originator != null) {
                RConsole console = consoleList.get(originator);

                if (console != null) {
                    console.printPrompt(prompt);
                } else {
                    log.info("PromptRActionHandler-unknown originatorID " + originator);
                }
            }
        }
    }

    class SearchRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            Object result = action.getAttributes().get(RActionConst.RESULT);

            if (result instanceof SearchResultContainer) {
                setSearchResults((SearchResultContainer)result);
            } else if (result instanceof Vector) {
                setSearchResults(new SearchResultContainer(null, (Vector<SearchResult>)result));
            } else {
                log.info("unknown result class " + result.getClass());
            }
        }
    }

    class UserMessageRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            final String message = (String) action.getAttributes().get(RActionConst.MESSAGE);
            new Thread(new Runnable(){
                public void run(){
                    JOptionPaneExt.showMessageDialog(Workbench.this.getContentPane(), message);
                }
            }).start();
        }
    }

    class VarChangeRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            if (varsListners.size() > 0) {
                VariablesChangeEvent event = new VariablesChangeEvent((HashSet<String>)
                        action.getAttributes().get(RActionConst.VARIABLES), (String) action
                        .getAttributes().get(RActionConst.ORIGINATOR), Workbench.this);
                for (int i = 0; i < varsListners.size(); ++i) {
                    try {
                        varsListners.elementAt(i).variablesChanged(event);
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }
                }
            }
        }
    }

    class LoadHistoryRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            ((LoadHistoryAction) workbenchActions.get("loadhistory")).loadHistoryFromServer(obtainR(),
                    (String) action.getAttributes().get(RActionConst.FILENAME));
        }
    }

    class SaveHistoryRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            ((SaveHistoryAction) workbenchActions.get("savehistory")).
                    saveCommandHistory((String) action.getAttributes().get(RActionConst.FILENAME));
        }
    }

    class UpdateFileTreeRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            final FileNode root = (FileNode) action.
                    getAttributes().get(RActionConst.ROOTNODE);

            new Thread(new Runnable(){
                public void run() {
                    fileBrowser.updateTreeRoot(root);
                }
            }).start();
        }
    }

    class UpdateFileNodeRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            final FileNode node = (FileNode) action.
                    getAttributes().get(RActionConst.FILENODE);

            new Thread(new Runnable(){
                public void run() {
                    fileBrowser.updateTreeNode(node);
                }
            }).start();
        }
    }

    class BusyRActionHandler extends RActionHandlerAbstract {
        public void actionPerformed(RAction action) {
            int busy = (Integer) action.getAttributes().get(RActionConst.BUSY);

            if (busy == 0) {
                updateConsoleIcon(_connectedIcon);
            } else if (busy == 1) {
                updateConsoleIcon(_busyIcon);
            } else {
                log.error("busy=" + busy);
            }
        }
    }

    class DeflatedRActionHandler extends RActionHandlerAbstract {
        private Object inflate(byte[] data) {

            Inflater decompressor = new Inflater();
            decompressor.setInput(data);

            try {

                ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

                byte[] buf = new byte[1024];
                while (!decompressor.finished()) {
                    try {
                        int count = decompressor.inflate(buf);
                        bos.write(buf, 0, count);
                    } catch (DataFormatException e) {
                    }
                }

                ObjectInputStream objectinput =
                        new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

                return objectinput.readObject();

            } catch (IOException ioe) {
                log.error("Error!",ioe);
            } catch (ClassNotFoundException cnfe) {
                log.error("Error!",cnfe);
            }

            return null;
        }

        public void actionPerformed(RAction action) {
            //log.info("got data");

            final byte[] data = (byte[]) action.getAttributes().get(RActionConst.DATA);

            Vector readyactions = (Vector) inflate(data);

            //log.info("---------- deflated ----------");
            /*
            for (int i=0;i<20;i++) {
                if (i < readyactions.size()) {
                    RAction action = (RAction) readyactions.get(i);
                    if (action != null) {
                        //log.info("i="+i+" action="+action.toString());
                    }
                }
            }
            */

            try {
                genericRActionListenerImpl.pushActions(readyactions);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }
    }


    private HashMap<String, Vector<RActionHandlerInterface>> rActionHandlerMap =
            new HashMap<String, Vector<RActionHandlerInterface>>();


    public void addRActionHandler(String actionname, RActionHandlerInterface handler) {
        Vector handlerlist = rActionHandlerMap.get(actionname);

        if (handlerlist == null) {
            handlerlist = new Vector<RActionHandlerInterface>();
            rActionHandlerMap.put(actionname, handlerlist);
        }

        handlerlist.add(handler);
    }

    public void removeRActionHandler(String actionname, RActionHandlerInterface handler) {
        Vector handlerlist = rActionHandlerMap.get(actionname);

        if (handlerlist != null) {
            handlerlist.remove(handler);
        }
    }

    public void initRActionHandlers() {

        addRActionHandler(RActionType.HELP, new HelpRActionHandler());
        addRActionHandler(RActionType.BROWSE, new BrowseRActionHandler());
        addRActionHandler(RActionType.QUIT, new QuitRActionHandler());
        addRActionHandler(RActionType.EDIT, new EditRActionHandler());
        addRActionHandler(RActionType.COMPLETE, new AsyncSubmitRActionHandler());
        addRActionHandler(RActionType.USER_INPUT, new UserInputRActionHandler());
        addRActionHandler(RActionType.CONSOLE, new ConsoleLogRActionHandler());
        addRActionHandler(RActionType.CONTINUE, new ConsoleContRActionHandler());
        addRActionHandler(RActionType.ADMIN_MESSAGE, new UserMessageRActionHandler());
        addRActionHandler(RActionType.VARIABLES_CHANGE, new VarChangeRActionHandler());
        addRActionHandler(RActionType.LOAD_HISTORY, new LoadHistoryRActionHandler());
        addRActionHandler(RActionType.SAVE_HISTORY, new SaveHistoryRActionHandler());
        addRActionHandler(RActionType.UPDATE_FILETREE, new UpdateFileTreeRActionHandler());
        addRActionHandler(RActionType.UPDATE_FILENODE, new UpdateFileNodeRActionHandler());
        addRActionHandler(RActionType.BUSY_ACTION, new BusyRActionHandler());
        addRActionHandler(RActionType.LOG, new ServerLogRActionHandler());
        addRActionHandler(RActionType.DEFLATED, new DeflatedRActionHandler());
        addRActionHandler(RActionType.PROMPT, new PromptRActionHandler());
        addRActionHandler(RActionType.SEARCH, new SearchRActionHandler());
    }


	Vector<VariablesChangeListener> varsListners = new Vector<VariablesChangeListener>();

	public void removeAllVariablesChangeListeners() {
		varsListners.removeAllElements();
	}

	public void removeVariablesChangeListener(VariablesChangeListener listener) {
		varsListners.remove(listener);
	}

	public void addVariablesChangeListener(VariablesChangeListener listener) {
		varsListners.add(listener);
	}

    HashMap<String, RConsole> consoleList = new HashMap<String, RConsole>();

    public void addConsole(String id, RConsole console) {
        consoleList.put(id, console);
    }

    public void removeConsole(String id) {
        consoleList.remove(id);
    }

    public HashMap<String, AbstractAction> getActions() {
        return workbenchActions;
    }

    public JFrame getRootFrame() {
        return this;
    }

    public RuntimeEnvironment getRuntime(){
        return runtime;
    }

    public ProjectDataDAO getProject(){
        return (runtime != null ? runtime.getProject() : null);
    }

    public UserDataDAO getUser(){
        return (runtime != null ? runtime.getUser() : null);
    }

    public WorkbenchRConsole getConsole() {
        return consolePanel;
    }

    public OperationManager getOpManager() {
        return opMan;
    }

    public ViewManager getViewManager() {
        return viewMan;
    }

    public WorkbenchLogContainer getLogContainer() {
        return logContainer;
    }
    
    public TabWindow getMainTabWindow() {
        return mainTabWindow;
    }

    public RootWindow getBenchRootWindow() {
        return benchRootWindow;
    }

    public RConsoleProvider getBenchRConsoleProvider() {
        return consoleProvider;
    }

	static public void main(String[] args) throws Exception {

	}
}
