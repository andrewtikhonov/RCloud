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
package workbench.dialogs.projectbrowser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.dialog.JDialogA;
import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.indicator.ProgressIndicator;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.http.proxy.DAOHttpProxy;
import uk.ac.ebi.rcloud.http.proxy.ServerRuntimeImpl;
import uk.ac.ebi.rcloud.rpf.db.DAOLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.ServerDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import workbench.AliasMap;
import workbench.report.BugReport;
import workbench.runtime.RuntimeEnvironment;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 12, 2009
 * Time: 3:41:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectBrowserDialog extends JDialogA { // {  JFrame //  //JDialogExt
    final private static Logger log = LoggerFactory.getLogger(ProjectBrowserDialog.class);

    private JPanel browserContainer;

    public static final int OK = 0;
    public static final int CANCEL = 1;
    private int result = CANCEL;

    private RuntimeEnvironment runtime;
    private ProgressIndicator indic;

    private JPanel scrollPanel;
    private JScrollPane scroll;
    
    private static Integer singletonLock = new Integer(0);
    private static ProjectBrowserDialog dialog = null;

    private Runnable refreshList = new RefreshListRunnable();

    private JButton create;
    private JButton updateList;
    private JButton sort;

    public static ProjectBrowserDialog getInstance(Component c, RuntimeEnvironment runtime) {
        if (dialog == null) {
            synchronized (singletonLock) {
                dialog = new ProjectBrowserDialog(c, runtime);
                //dialog.resetUI();
                return dialog;
            }
        } else {
            //dialog.resetUI();
            return dialog;
        }
    }

    private void addNewProject() {

        String username = runtime.getUser().getUsername();

        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(ProjectDataDAO.title,        "New Project");
        options.put(ProjectDataDAO.identifier,   username + "/NewProject");
        options.put(ProjectDataDAO.description,  "");
        options.put(ProjectDataDAO.owner,        username);
        options.put(ProjectDataDAO.status,       ProjectDataDAO.PROJECT_NEW);
        options.put(ProjectDataDAO.baseFolder,   runtime.getUser().getBaseFolder());

        final ProjectCreationPanel panel = new ProjectCreationPanel(
                new ProjectListItem(new ProjectDataDAO(options)), browserActionHandlers);

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                browserContainer.add(panel, 0);
                browserContainer.validate();
                panel.expand();
                panel.titleField.requestFocus();
                panel.scrollRectToVisible(panel.getVisibleRect());
                //collapseAllButOne(panel);
            }
        });
    }

    private void removeProjectPanel(final ProjectRecordPanel panel) {
        panel.collapse();

        try { Thread.sleep(1000); } catch(Exception ex) {
        }

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                browserContainer.remove(panel);
                browserContainer.validate();
                scroll.repaint();
            }
        });
    }

    public void lockControlls() {
        //create.setEnabled(false);
        //updateList.setEnabled(false);
    }

    public void unlockControlls() {
        //create.setEnabled(true);
        //updateList.setEnabled(true);
    }

    public void startProgressIndicator() {
        lockControlls();
        indic.start();
    }

    public void stopProgressIndicator() {
        indic.stop();
        unlockControlls();
    }

    /*
    private BufferedImage loadScreenshot(ProjectDataDAO project) {

        try {
            byte[] data = FileLoad.read(project.getAbsolutePath() + "/.RConsole", obtainR());

            if (data.length > 0) {
                Object obj = HexUtil.bytesToObject(data);

                if (obj instanceof BufferedImage) {
                    return (BufferedImage) obj;
                }
            }

        } catch (Exception ex) {
            log.info("Error!", ex);
        }

        return ImageLoader.load("/views/images/projectlist/flickering-screen-noice.jpg");
    }
    */

    class ServerStatusRunnable implements Runnable {
        private ProjectRecordPanel panelToUpdate;

        public ServerStatusRunnable(ProjectRecordPanel panel) {
            panelToUpdate = panel;
        }

        public void run() {
            ProjectListItem item = panelToUpdate.getProjectListItem();

            if (item.getServer() != null) {
                
                //log.info("Checking Server "+ item.getServer().getName());
    
                boolean alive = checkServerIsAlive(item.getServer().getServerName());
    
                //log.info("Server "+ item.getServer().getName() + " alive: " + alive);
    
                item.setServerStatus(alive ?
                        ProjectListItem.SERVER_ALIVE : ProjectListItem.SERVER_UNRESPONSIVE);
    
                // trigger update of panel fields
                //
                panelToUpdate.updatePanelFromProjectListItem();
    
                performGUIUpdate(new Runnable(){
                    public void run(){
                        browserContainer.revalidate();
                        browserContainer.repaint();
                    }
                });
            }
        }
    }

    class ProjectActionHandlers implements ActionHandlers {

        class CreateProjectRunnable implements Runnable {
            private ProjectCreationPanel panel;
            public CreateProjectRunnable(ProjectCreationPanel panel) {
                this.panel = panel;
            }

            public void run() {
                ProjectDataDAO project = panel.getProjectListItem().getProject();

                try {
                    startProgressIndicator();

                    ProjectDataDAO project0 = runtime.
                            getDAOLayer().getProject(project.getIdentifier());

                    if (project0 != null) {
                        warningMessage("Such or very similar project already exists");
                        return;
                    }

                    DAOLayerInterface daoLayer = runtime.getDAOLayer();

                    daoLayer.createProject(project);
                    runtime.setProject(project);
                    runtime.setServer(null);

                    okMethod();

                } catch (AlreadyBoundException ex) {
                    warningMessage("Project already exists");
                } catch (Exception ex) {
                    boolean report = BugReport.reportAProblem(getOwner(),
                            "Sorry, we encountered a problem creating the project.\n"+
                            "Would you like to send a report to the R Cloud support ?");

                    if (report) {
                        BugReport.showReportDialog(getOwner(), runtime,
                                "problem creating project",
                                "Project: " + project.getTitle() + "\n" +
                                BugReport.stackTraceToString(ex));

                    }

                    log.error("Error!", ex);
                } finally {
                    stopProgressIndicator();
                }
            }
        }


        public void createProject(ProjectCreationPanel panel) {
            new Thread(new CreateProjectRunnable(panel)).start();
        }


        class LaunchProjectRunnable implements Runnable {
            private ProjectRecordPanel panel;
            public LaunchProjectRunnable(ProjectRecordPanel panel) {
                this.panel = panel;
            }

            public void run() {
                // continue project
                //
                ProjectListItem item = panel.getProjectListItem();

                ServerDataDAO server = item.getServer();

                if (server != null && panel.getProjectListItem().getServerStatus() != ProjectListItem.SERVER_ALIVE) {

                    Object[] options = { "Launch Project", "Restart Project", "Cancel" };

                    int reply = JOptionPaneExt.showOptionDialog(ProjectBrowserDialog.this,
                            "<html><h2>The server is not responding.</h2>\n" +
                            "Would you like to try to launch the project \n" + 
                            "or shutdown the server and restart the project ?\n" +
                            "(unsaved data will likely be lost)" ,
                            "Confirm Actions",
                            JOptionPaneExt.DEFAULT_OPTION,
                            JOptionPaneExt.QUESTION_MESSAGE,
                            null, options, options[0]);

                    if (reply == 2) {
                        return;
                    }
                    
                    if (reply == 1) {
                        try {
                            runtime.getDAOLayer().setProject(server.getServerName(), null);
                            runtime.getDAOLayer().registerPingFailure(server.getServerName());

                            server = null;
                        } catch(RemoteException re){
                        } catch(NotBoundException re){
                        }
                    }
                }

                runtime.setProject(item.getProject());
                runtime.setServer((server != null ? server.getServerName() : null));

                okMethod();
            }
        }

        public void launchProject(ProjectRecordPanel panel) {
            new Thread(new LaunchProjectRunnable(panel)).start();
        }

        class DeleteProjectRunnable implements Runnable {
            private ProjectRecordPanel panel;
            public DeleteProjectRunnable(ProjectRecordPanel panel) {
                this.panel = panel;
            }

            public void run() {
                boolean projectsDeleted = false;

                ProjectDataDAO project = panel.getProjectListItem().getProject();
                Vector<ServerDataDAO> servers = null;

                try {
                    servers = runtime.getDAOLayer().getServerByProjectId(project.getIdentifier());
                } catch ( RemoteException re ) {
                    //log.error("Error!", re);
                }

                if (servers != null && servers.size() > 0) {

                    Object[] options = { "Shutdown & Delete", "Cancel" };

                    int reply = JOptionPaneExt.showOptionDialog(ProjectBrowserDialog.this,
                            project.getTitle() + " is running. \n" +
                            "Shut down the server and delete the project ?" ,
                            "Confirm Deletion",
                            JOptionPaneExt.DEFAULT_OPTION,
                            JOptionPaneExt.QUESTION_MESSAGE,
                            null, options, options[0]);

                    if (reply == 0) {

                        // delete project
                        DAOLayerInterface daoLayer = runtime.getDAOLayer();

                        try {
                            startProgressIndicator();
                            ServerDataDAO server = servers.get(0);
                            daoLayer.setProject(server.getServerName(), null); //deleteProject(project);
                            daoLayer.registerPingFailure(server.getServerName());

                        } catch (Exception ex) {

                            boolean report = BugReport.reportAProblem(getOwner(),
                                    "Sorry, we encountered a problem deleting the server.\n" +
                                    "Would you like to report it to the R Cloud support ?");

                            if (report) {
                                BugReport.showReportDialog(getOwner(), runtime,
                                        "problem unlinking server from project",
                                        "Project: " + project.getTitle() + "\n" +
                                        "Server: " + servers.get(0).getServerName() + "\n" +
                                        BugReport.stackTraceToString(ex));
                            }

                            log.error("Error!", ex);

                        } finally {
                            stopProgressIndicator();
                        }

                    } else {
                        return;
                    }

                } else {

                    Object[] options = { "Delete", "Cancel" };

                    int reply = JOptionPaneExt.showOptionDialog(ProjectBrowserDialog.this,
                            "Do you want to delete " + project.getTitle() + " ?",
                            "Confirm Deletion",
                            JOptionPaneExt.DEFAULT_OPTION,
                            JOptionPaneExt.QUESTION_MESSAGE,
                            null, options, options[0]);

                    if (reply != 0) {
                        return;
                    }
                }

                try {
                    startProgressIndicator();

                    // delete project
                    DAOLayerInterface daoLayer = runtime.getDAOLayer();

                    try {
                        daoLayer.deleteProject(project);
                        projectsDeleted = true;

                    } catch (NotBoundException ex) {
                        warningMessage(project.getTitle() + " does not exist");

                    } catch (Exception ex) {

                        boolean report = BugReport.reportAProblem(getOwner(),
                                "Sorry, we encountered a problem deleting "+ project.getTitle() + "\n" +
                                "Would you like to report it to the R Cloud support ?");

                        if (report) {
                            BugReport.showReportDialog(getOwner(), runtime,
                                    "problem deleting project",
                                    "Project: " + project.getTitle() + "\n" +
                                    BugReport.stackTraceToString(ex));

                        }

                        log.error("Error!", ex);
                    }

                    removeProjectPanel(panel);

                } finally {
                    stopProgressIndicator();
                }
            }

        }

        public void deleteProject(ProjectRecordPanel panel) {
            new Thread(new DeleteProjectRunnable(panel)).start();
        }

        class UpdateProjectDescriptionRunnable implements Runnable {
            private ProjectRecordPanel panel;
            public UpdateProjectDescriptionRunnable(ProjectRecordPanel panel) {
                this.panel = panel;
            }

            public void run() {
                try {
                    startProgressIndicator();
                    DAOLayerInterface daoLayer = runtime.getDAOLayer();

                    ProjectDataDAO project = panel.getProjectListItem().getProject();

                    project.getMap().put(ProjectDataDAO.description, panel.getComment());

                    try {
                        daoLayer.updateProjectDescription(project);
                    } catch (Exception ex) {

                        boolean report = BugReport.reportAProblem(getOwner(),
                                "Sorry, we encountered a problem updating the project.\n"+
                                "Would you like to report it to the R Cloud support ?");

                        if (report) {
                            BugReport.showReportDialog(getOwner(), runtime,
                                    "problem updating project",
                                    "Project: " + project.getTitle() + "\n" +
                                    BugReport.stackTraceToString(ex));
                        }

                        log.error("Error!", ex);
                    }
                } finally {
                    stopProgressIndicator();
                }
            }
        }

        public void updateDescription(ProjectRecordPanel panel) {
            new Thread(new UpdateProjectDescriptionRunnable(panel)).start();
        }

        class RefreshProjectRunnable implements Runnable {
            private ProjectRecordPanel panel;
            public RefreshProjectRunnable(ProjectRecordPanel panel) {
                this.panel = panel;
            }

            public void run() {
                try {
                    startProgressIndicator();
                    DAOLayerInterface daoLayer = runtime.getDAOLayer();
                    ProjectListItem item = panel.getProjectListItem();

                    ProjectDataDAO project = item.getProject();

                    try {

                        String proejctid = project.getIdentifier();

                        ProjectDataDAO newproject = daoLayer.getProject(proejctid);

                        Vector<ServerDataDAO> servers = daoLayer.getServerByProjectId(proejctid);

                        item.setProject(newproject);

                        if (servers != null && servers.size() > 0) {
                            item.setServer(servers.elementAt(0));
                            item.setServerStatus(ProjectListItem.SERVER_CHECKING);
                        }

                        panel.setProjectListItem(item);

                        if (servers != null) {
                            new Thread(
                                    new ServerStatusRunnable(panel)).start();
                        }

                        performGUIUpdate(new Runnable(){
                            public void run(){
                                browserContainer.revalidate();
                                browserContainer.repaint();
                            }
                        });


                    } catch (Exception ex) {

                        boolean report = BugReport.reportAProblem(getOwner(),
                                "Sorry, we encountered a problem refreshing the project data.\n"+
                                "Would you like to report it to the R Cloud support ?");

                        if (report) {
                            BugReport.showReportDialog(getOwner(), runtime,
                                    "problem refreshing project data",
                                    "Project: " + project.getTitle() + "\n" +
                                    BugReport.stackTraceToString(ex));
                        }
                        log.error("Error!", ex);
                    }
                } finally {
                    stopProgressIndicator();
                }
            }
        }

        public void refreshProject(ProjectRecordPanel panel) {
            new Thread(new RefreshProjectRunnable(panel)).start();
        }


        class StopServerRunnable implements Runnable {
            private ProjectRecordPanel panel;
            public StopServerRunnable(ProjectRecordPanel panel) {
                this.panel = panel;
            }

            public void run() {
                ProjectListItem item = panel.getProjectListItem();

                ServerDataDAO server = item.getServer();

                if (server != null) {

                    Object[] options = { "Shutdown Server", "Cancel" };

                    String servername = server.getServerName() + " " + AliasMap.getServerAliasName(server.getServerName());

                    String message = panel.getProjectListItem().getServerStatus() == ProjectListItem.SERVER_ALIVE ?
                            "Server " + servername + " is running.\n" +
                            "Do you want to stop the running server ?\n" +
                            "(unsaved data will likely be lost)" :

                            "Server " + servername + " is not responding.\n" +
                            "Do you want to stop the running server ?";

                    int reply = JOptionPaneExt.showOptionDialog(ProjectBrowserDialog.this,
                            message,
                            "Confirm Shutdown",
                            JOptionPaneExt.DEFAULT_OPTION,
                            JOptionPaneExt.QUESTION_MESSAGE,
                            null, options, options[0]);

                    if (reply == 1) {
                        return;
                    }

                    try {
                        log.info("shutting server down");

                        startProgressIndicator();

                        runtime.getDAOLayer().setProject(server.getServerName(), null);
                        runtime.getDAOLayer().registerPingFailure(server.getServerName());

                    } catch(RemoteException re){
                    } catch(NotBoundException re){
                    } finally {
                        stopProgressIndicator();
                    }

                    refreshList.run();
                
                } else {

                    JOptionPaneExt.showMessageDialog(ProjectBrowserDialog.this,
                            item.getProject().getTitle() + " is not running.");
                }
            }
        }

        public void stopServer(ProjectRecordPanel panel) {
            new Thread(new StopServerRunnable(panel)).start();
        }

    }

    private boolean checkServerIsAlive(String servername) {

        HashMap<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("server", servername);

        boolean alive = true;

        try {
            alive = ServerRuntimeImpl.
                    isServerAlive(runtime.getSession(), parameters);

        } catch (Exception ex) {
            alive = false;
        }

        return alive;
    }


    private ActionHandlers browserActionHandlers = new ProjectActionHandlers();

    class CreateNewProjectAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            addNewProject();
        }
    }

    private static final int BYNAME = 0;
    private static final int BYDATE = 1;
    private static final int BYSTATUS = 2;

    class SortProjectsBy extends AbstractAction {
        private int type;
        public SortProjectsBy(String name, int type) {
            super(name);
            this.type = type;
        }

        private boolean GT (ProjectListItem i0, ProjectListItem i1, int type) {
            if (type == BYNAME) {
                return (i0.getProject().getTitle().compareToIgnoreCase(i1.getProject().getTitle()) > 0);
            } else if (type == BYDATE) {
                return (i0.getProject().getCreatedTime().getTime() - i1.getProject().getCreatedTime().getTime() < 0);
            } else if (type == BYSTATUS) {
                return (i0.getProject().getStatus().compareToIgnoreCase(i1.getProject().getStatus()) > 0);
            }
            return false;
        }

        public void actionPerformed(ActionEvent e) {
            final Vector<ProjectRecordBase> tosort = new Vector<ProjectRecordBase>();
            final Vector<ProjectRecordBase> sorted = new Vector<ProjectRecordBase>();

            int count = browserContainer.getComponentCount();

            for (int i = 0; i < count;i++) {
                Component c = browserContainer.getComponent(i);

                if(c instanceof ProjectRecordBase) {
                    tosort.add((ProjectRecordBase)c);
                }
            }

            count = tosort.size();

            for (int i = 0; i < count; i++) {
                ProjectRecordBase c0 = tosort.elementAt(0);

                for (int j = 1; j < tosort.size(); j++) {
                    //
                    ProjectRecordBase c1 = tosort.elementAt(j);

                    if (GT(c0.getProjectListItem(), c1.getProjectListItem(), type)) {
                        c0 = c1;
                    }
                }

                sorted.add(c0);
                tosort.remove(c0);
            }

            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    browserContainer.removeAll();
                    for (ProjectRecordBase c : sorted) {
                        browserContainer.add(c);
                    }
                    browserContainer.validate();
                    scroll.repaint();
                }
            });
        }

        public boolean isEnabled() {
            return true;
        }
    }

    class SortProjectsAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {

            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem sortByNameItem = new JMenuItem();
            sortByNameItem.setAction(new SortProjectsBy("Name", BYNAME));

            JMenuItem sortByDateItem = new JMenuItem();
            sortByDateItem.setAction(new SortProjectsBy("Date", BYDATE));

            JMenuItem sortByStatusItem = new JMenuItem();
            sortByStatusItem.setAction(new SortProjectsBy("Status", BYSTATUS));

            //cleanItem.setIcon(new ImageIcon(
            //        ImageLoader.load("/views/images/logviewer/edit-clear.png")));


            popupMenu.add(sortByNameItem);
            popupMenu.add(sortByDateItem);
            popupMenu.add(sortByStatusItem);

            Point l0 = MouseInfo.getPointerInfo().getLocation();
            Point l1 = ((JComponent)event.getSource()).getLocationOnScreen();

            popupMenu.show((JComponent)event.getSource(), l0.x - l1.x, l0.y - l1.y);
        }
    }

    class UpdateListAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            new Thread(refreshList).start();
        }
    }


    public ProjectBrowserDialog(Component c, RuntimeEnvironment runtime) {

        //super((Frame) c, true);
        super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, c), true);

        this.runtime = runtime;

        setupUI();

        setTitle("Project Browser");
        
        setPreferredSize(new Dimension(836, 650));
        setLocationRelativeTo(c);
        setResizable(false);
    }

    private void performGUIUpdate(final Runnable update) {
        if(SwingUtilities.isEventDispatchThread())
            update.run();
        else {
            EventQueue.invokeLater(update);
        }
    }

    class RefreshListRunnable implements Runnable {
        public void run(){

            performGUIUpdate(new Runnable(){
                public void run(){
                    browserContainer.removeAll();
                    browserContainer.revalidate();
                    browserContainer.repaint();
                }
            });

            try {
                startProgressIndicator();

                HashMap<String, ServerDataDAO> serverMap = new HashMap<String, ServerDataDAO>();

                Vector<ServerDataDAO> servers = null;

                try {
                    servers = runtime.getDAOLayer().getServersByOwner(
                            runtime.getUser().getUsername());

                } catch ( RemoteException re ) {

                    boolean report = BugReport.reportAProblem(getOwner(),
                            "Sorry, we cant get the list of running servers.\n"+
                            "Would you like to report it to the R Cloud support ?");

                    if (report) {
                        BugReport.showReportDialog(getOwner(), runtime,
                                "problem getting list of running servers",
                                BugReport.stackTraceToString(re));
                    }

                    log.error("Error!", re);
                    return;
                }

                for(ServerDataDAO s : servers) {
                    serverMap.put(s.getProjectId(), s);
                }

                // see if there are projects owned
                //
                Vector<ProjectDataDAO> projects = null;

                try {
                    projects = runtime.getDAOLayer().getProjectsOwnedByUser(
                            runtime.getUser().getUsername());

                } catch ( RemoteException re ) {
                    //log.error("Error!", re);
                }

                if (projects == null || projects.isEmpty()) {
                    return;
                }

                try {

                    // if there are open projects
                    // show them in the list
                    //
                    for ( ProjectDataDAO p : projects ) {

                        ProjectListItem item = new ProjectListItem(p);

                        ServerDataDAO server = serverMap.get(p.getIdentifier());

                        if (server != null) {

                            item.setServer(server);
                            item.setServerStatus(ProjectListItem.SERVER_CHECKING);
                        }

                        final ProjectRecordPanel panel =
                                new ProjectRecordPanel(item, browserActionHandlers);
                        panel.setPreferredSize(new Dimension(scroll.getViewport().getWidth(), 500));
                        panel.setHighlighted(true);

                        if (server != null) {
                            new Thread(
                                    new ServerStatusRunnable(panel)).start();
                        }

                        performGUIUpdate(new Runnable(){
                            public void run(){
                                browserContainer.add(panel);
                                browserContainer.revalidate();
                                browserContainer.repaint();
                            }
                        });
                    }

                } finally {
                    performGUIUpdate(new Runnable(){
                        public void run(){
                            browserContainer.revalidate();
                            browserContainer.repaint();
                        }
                    });
                }

            } finally {
                stopProgressIndicator();

            }
        }
    }

    private Runnable repainter = new Runnable() {
        public void run() {
            scroll.repaint();
        }
    };

    private void setupUI(){

        browserContainer = new JPanel(new ViewVerticalLayout()); //

        //browserContainer.setBorder(new EmptyBorder(2,2,2,2));
        browserContainer.setOpaque(true);

        JPanel toolPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolPanelLeft.setOpaque(false);

        JPanel toolPanelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toolPanelRight.setOpaque(false);

        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setOpaque(false);

        toolPanel.add(toolPanelLeft, BorderLayout.WEST);
        toolPanel.add(toolPanelRight, BorderLayout.EAST);

        scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.setBorder(new EmptyBorder(10,10,10,10));

        scroll = new JScrollPane(browserContainer);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scrollPanel.add(scroll, BorderLayout.CENTER);

        create = ButtonUtil.makeButton("New Project", "/views/images/projectlist/page_add.png", "Create new project");
        create.addActionListener(new CreateNewProjectAction());

        updateList = ButtonUtil.makeButton("Refresh", "/views/images/projectlist/page_refresh.png", "Refresh project list");
        updateList.addActionListener(new UpdateListAction());

        sort = ButtonUtil.makeButton("Sort", "/views/images/projectlist/page_find.png", "Sort By");
        sort.addActionListener(new SortProjectsAction());

        indic = new ProgressIndicator(Color.DARK_GRAY); //Color.BLUE.darker()
        indic.setPreferredSize(new Dimension(create.getPreferredSize().height,
                create.getPreferredSize().height));
        indic.setVisible(false);

        toolPanelLeft.add(create);
        toolPanelLeft.add(updateList);
        toolPanelLeft.add(sort);
        toolPanelRight.add(indic);

        add(toolPanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);

        ActionListener cancelDialogListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelMethod();
            }
        };

        ((JPanel)getContentPane()).registerKeyboardAction(cancelDialogListener,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            resetUI();
        }
        super.setVisible(visible);
    }

    public void resetUI(){
        result = CANCEL;
        new Thread(refreshList).start();
    }

    public void okMethod() {
        result = OK;
        setVisible(false);
    }

    public void cancelMethod() {
        result = CANCEL;
        setVisible(false);
    }

    public int getResult() {
        return result;
    }

    public void errorMessage(String message) {
        JOptionPaneExt.showMessageDialog(getOwner(),
                message, "", JOptionPaneExt.ERROR_MESSAGE);
    }

    public void warningMessage(final String message) {
        JOptionPaneExt.showMessageDialog(getOwner(),
                message, "", JOptionPaneExt.WARNING_MESSAGE);
    }

    public static void main(String[] args) {

        try {
            HashMap<String, Object> options = new HashMap<String, Object>();
            options.put(UserDataDAO.username, System.getProperty("login"));
            options.put(UserDataDAO.userFolder, System.getProperty("userfolder"));

            UserDataDAO user = new UserDataDAO(options);

            RuntimeEnvironment runtime = new RuntimeEnvironment();

            runtime.setBaseUrl(System.getProperty("baseurl"));

            runtime.setDAOLayer(DAOHttpProxy.getDAOLayer(null, runtime.getSession()));

            runtime.setUser(user);

            ProjectBrowserDialog dialog = new ProjectBrowserDialog(new JFrame().getContentPane(), runtime);

            dialog.setModal(true);
            dialog.setVisible(true);
            System.exit(0);

        } catch (Exception ex) {
            log.error("Error! ", ex);
        }
    }
}
