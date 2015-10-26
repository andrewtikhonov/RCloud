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

import uk.ac.ebi.rcloud.common.components.indicator.ProgressIndicator;
import uk.ac.ebi.rcloud.common.components.textfield.JHintTextPane;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.ServerDataDAO;
import workbench.AliasMap;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 10, 2009
 * Time: 3:39:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectRecordPanel extends ProjectRecordBase {

    // accessible fields
    public JLabel titleLabel;
    public JLabel statusTitleLabel;

    public JLabel statusTitleLabel2;
    public JLabel statusTitleLabel3;

    //
    public JHintTextPane commentField;

    // buttons
    protected JButton launch;
    protected JButton delete;
    protected JButton shutdown;
    protected JButton refresh;
    protected JButton update;

    // labels
    protected JLabel lastActivity;
    protected JLabel overall;
    protected JLabel server;
    protected JLabel serverStatus;

    // progres indicator
    public ProgressIndicator progress;

    // undo managers
    private FieldUndoManager descriUndo;

    // panels
    JPanel statisticPanel;
    //JPanel shotPanel;
    JPanel buttonPanel;


    //
    //   C O N S T R U C T O R
    //
    public ProjectRecordPanel(ProjectListItem item, ActionHandlers handlers) {
        initProjectRecordPanel(handlers);

        if (item != null) {
            setProjectListItem(item);
        }
    }


    //
    //   D I A L O G
    //

    public void initUndoManagers() {
        descriUndo = new FieldUndoManager(commentField);
    }

    public void initProjectRecordPanel(ActionHandlers handlers) {

        if (handlers != null) {
            setActionHandlers(handlers);
        }

        int iconsize = 20;

        launch   = ButtonUtil.makeButton("Launch", "/views/images/projectlist/page_lightning.png", null);
        delete   = ButtonUtil.makeButton("Delete Project", "/views/images/projectlist/bin_closed.png", null);
        shutdown = ButtonUtil.makeButton("Shutdown Server", "/views/images/projectlist/cross_octagon_fram.png", null);

        update  = ButtonUtil.makeButton("Update Comment", "/views/images/projectlist/comment.png", null);
        refresh = ButtonUtil.makeButton("Refresh Status", "/views/images/projectlist/view-refresh.png", null);

        Font f0 = getFont();
        Font f1 = new Font(f0.getFamily(), Font.BOLD, f0.getSize() + 1);
        Font f2 = new Font(f0.getFamily(), Font.PLAIN, f0.getSize() - 4);

        titleLabel = new JLabel("");
        titleLabel.setOpaque(false);
        titleLabel.setFont(f1);

        statusTitleLabel = new JLabel("");
        statusTitleLabel.setOpaque(false);
        //statusTitleLabel.setForeground(Color.WHITE);
        statusTitleLabel.setFont(f2);

        statusTitleLabel2 = new JLabel("");
        statusTitleLabel2.setOpaque(false);
        //statusTitleLabel2.setForeground(Color.WHITE);
        statusTitleLabel2.setFont(f2);

        statusTitleLabel3 = new JLabel("");
        statusTitleLabel3.setOpaque(false);
        //statusTitleLabel3.setForeground(Color.WHITE);
        statusTitleLabel3.setFont(f2);

        //JPanel titlePanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        //titlePanelLeft.setOpaque(false);
        //titlePanelLeft.setBorder(new EmptyBorder(2,5,2,5));

        JPanel titlePanelRight = new JPanel(new BorderLayout());
        titlePanelRight.setOpaque(false);
        titlePanelRight.setBorder(new EmptyBorder(2,5,2,5));

        JPanel titlePanelCenter = new JPanel(new BorderLayout());
        titlePanelCenter.setOpaque(false);
        titlePanelCenter.setBorder(new EmptyBorder(2,5,2,5));

        JPanel titlePanelCenterRight = new JPanel(new BorderLayout());
        titlePanelCenterRight.setOpaque(false);
        titlePanelCenterRight.setPreferredSize(new Dimension(200, 10));

        titlePanelCenterRight.add(statusTitleLabel2, BorderLayout.NORTH);
        titlePanelCenterRight.add(statusTitleLabel3, BorderLayout.CENTER);

        titlePanelCenter.add(titleLabel, BorderLayout.CENTER);
        titlePanelCenter.add(statusTitleLabel, BorderLayout.SOUTH);
        titlePanelCenter.add(titlePanelCenterRight, BorderLayout.EAST);

        titlePanelRight.add(launch);

        titleContainer.setBorder(new EmptyBorder(2,5,2,5));
        titleContainer.setOpaque(false);
        //titleContainer.add(titlePanelLeft, BorderLayout.WEST);
        titleContainer.add(titlePanelCenter, BorderLayout.CENTER);
        titleContainer.add(titlePanelRight, BorderLayout.EAST);
        //titleContainer.add(new JLabel(" "), BorderLayout.SOUTH);

        commentField = new JHintTextPane("Comment");
        commentField.setMargin(new Insets(2,2,2,2));

        lastActivity = new JLabelSmallFont(" ");
        overall = new JLabelSmallFont(" ");
        server = new JLabelSmallFont(" ");
        serverStatus = new JLabelSmallFont(" ");

        progress = new ProgressIndicator();
        progress.setPreferredSize(new Dimension(delete.getPreferredSize().height,
                delete.getPreferredSize().height));
        progress.setVisible(false);

        buttonPanel = new JPanel(new BorderLayout()); //new FlowLayout()
        buttonPanel.setOpaque(false);

        JPanel buttonPanelInner = new JPanel(new GridLayout(0, 1, 2, 2));
        buttonPanelInner.setOpaque(false);

        buttonPanelInner.add(update);
        buttonPanelInner.add(refresh);
        buttonPanelInner.add(shutdown);
        buttonPanelInner.add(delete);

        buttonPanel.add(buttonPanelInner, BorderLayout.NORTH);

        expandableContainer.setOpaque(false);
        expandableContainer.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5,10,5,10)));

        statisticPanel = new JPanel(new BorderLayout());
        statisticPanel.setOpaque(false);

        JPanel statisticPanelLeft = new JPanel(new GridLayout(0,1,5,2));
        statisticPanelLeft.setOpaque(false);
        //statisticPanelLeft.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(5,5,5,5)));
        statisticPanelLeft.setBorder(new EmptyBorder(5,5,5,5));

        JPanel statisticPanelRight = new JPanel(new GridLayout(0,1,5,2));
        statisticPanelRight.setOpaque(false);
        //statisticPanelRight.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(5,5,5,5)));
        statisticPanelRight.setBorder(new EmptyBorder(5,5,5,5));

        statisticPanel.add(statisticPanelLeft, BorderLayout.WEST);
        statisticPanel.add(statisticPanelRight, BorderLayout.CENTER);

        statisticPanelLeft.add(new JLabelSmallFont("Last Activity "));
        statisticPanelLeft.add(new JLabelSmallFont("Overall Time "));
        statisticPanelLeft.add(new JLabelSmallFont("Server "));
        statisticPanelLeft.add(new JLabelSmallFont("Server Status "));

        statisticPanelRight.add(lastActivity);
        statisticPanelRight.add(overall);
        statisticPanelRight.add(server);
        statisticPanelRight.add(serverStatus);


        JPanel innerContainer = new JPanel(new BorderLayout());
        innerContainer.setOpaque(false);

        JScrollPane commentScroll = new JScrollPane(commentField);
        commentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        commentScroll.setBorder(new LineBorder(Color.LIGHT_GRAY));

        //innerContainer.add(fieldPanel, BorderLayout.NORTH);
        innerContainer.add(commentScroll, BorderLayout.CENTER);
        innerContainer.add(statisticPanel, BorderLayout.SOUTH);

        //shotPanel = new JPanel(new BorderLayout());
        //shotPanel.setOpaque(false);

        //JLabelQ shotLabel = new JLabelQ("last state",
        //        new ImageIcon(ImageLoader.load("/images/workbench_snap.jpg")), JLabel.CENTER);
        //shotLabel.setVerticalTextPosition(JLabel.BOTTOM);
        //shotLabel.setHorizontalTextPosition(JLabel.CENTER);

        //shotPanel.add(shotLabel, BorderLayout.CENTER);

        expandableContainer.add(innerContainer, BorderLayout.CENTER);
        //expandableContainer.add(shotPanel, BorderLayout.WEST);
        expandableContainer.add(buttonPanel, BorderLayout.EAST);

        //titleField.getDocument().addDocumentListener(new CopyTextHandler(titleField, titleLabel));

        setPreferredTitleSize(new Dimension(739, 40));
        setPreferredContainerSize(new Dimension(739, 180));

        initUndoManagers();

        initActionListeners();

    }


    //
    //   A C C E S S O R S
    //

    public String getComment() {
        return commentField.getText();
    }

    public void setComment(String text) {
        commentField.setText(text);
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String text) {
        titleLabel.setText(text);
    }

    public void setTitleStatus(String text) {
        statusTitleLabel.setText(text);
    }

    public void setStatus(String text) {
        statusTitleLabel2.setText("Status: " + text);
    }

    public void setCreated(String text) {
        statusTitleLabel3.setText("Created:" + text);
    }

    public void setLastActivity(String text) {
        lastActivity.setText(text);
    }

    public void setOverallTime(String text) {
        overall.setText(text);
    }

    private ProjectRecordPanel getPanel() {
        return ProjectRecordPanel.this;
    }

    //
    //   H A N D L E R S
    //

    private void initActionListeners() {

        launch.addActionListener(new LaunchProjectAction());
        update.addActionListener(new UpdateDesriptionAction());
        refresh.addActionListener(new RefreshProjectAction());
        shutdown.addActionListener(new ShutdownServerAction());
        delete.addActionListener(new DeleteProjectAction());
    }

    class LaunchProjectAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (getActionHandlers() != null) {
                getActionHandlers().launchProject(getPanel());
            }
        }
    }

    class UpdateDesriptionAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (getActionHandlers() != null) {
                getActionHandlers().updateDescription(getPanel());
            }
        }
    }


    class RefreshProjectAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (getActionHandlers() != null) {
                getActionHandlers().refreshProject(getPanel());
            }
        }
    }

    class ShutdownServerAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (getActionHandlers() != null) {
                getActionHandlers().stopServer(getPanel());
            }
        }
    }

    class DeleteProjectAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (getActionHandlers() != null) {
                getActionHandlers().deleteProject(getPanel());
            }
        }
    }

    public String formatOverallTime(String time) {
        String[] s0 = time.split(" ");
        String[] s1 = s0[1].split(":");
        return (s0[0] + " days " + s1[0] + " hours " + s1[1] + " minutes");
    }

    public void updatePanelFromProjectListItem() {
        if (projectListItem != null) {

            String NAText = "NA";

            ProjectDataDAO project  = projectListItem.getProject();

            setTitle(project.getTitle());

            setComment(project.getDescription());

            setStatus(project.getStatus());

            Timestamp created = project.getCreatedTime();
            if (created != null) {
                setCreated(DateFormat.getDateTimeInstance().format(created));
            }

            Timestamp lastactivity = project.getLastActivity();
            if (lastactivity != null) {
                setLastActivity(DateFormat.getDateTimeInstance().format(lastactivity));
            }

            String overalltime = project.getOverallTime();
            if (overalltime != null) {
                String overalFormatted = overalltime;
                if (!overalltime.contains("days")) {
                    overalFormatted = formatOverallTime(overalltime);
                }
                setOverallTime(overalFormatted);
                //setOverallTime(DateFormat.getDateTimeInstance().format(overalltime));
            }

            ServerDataDAO server0 = projectListItem.getServer();

            if (server0 != null) {

                String serverNameAndAlias = server0.getServerName() + " " +
                        AliasMap.getServerAliasName(server0.getServerName()) + " (" +
                        server0.getOsName()+")";

                server.setText(serverNameAndAlias);

                String serverStatusText = " ";

                switch (projectListItem.getServerStatus()) {
                    case ProjectListItem.SERVER_ALIVE :
                        serverStatusText = "Running";
                        break;

                    case ProjectListItem.SERVER_CHECKING :
                        serverStatusText = "Checking...";
                        break;

                    case ProjectListItem.SERVER_UNRESPONSIVE :
                        serverStatusText = "Not Responding";
                        break;
                }

                serverStatus.setText(serverStatusText);
                setTitleStatus(serverNameAndAlias + " " +serverStatusText);

                shutdown.setEnabled(true);

            } else {
                server.setText(NAText);
                serverStatus.setText(NAText);
                shutdown.setEnabled(false);
                shutdown.setEnabled(false);
            }
        }
    }

    // set / get project data
    //
    public void setProjectListItem(ProjectListItem item) {
        super.setProjectListItem(item);

        updatePanelFromProjectListItem();
    }

    public static ProjectDataDAO getADummyProject(String title) {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(ProjectDataDAO.title, title);
        options.put(ProjectDataDAO.identifier, "dummy/folder");
        options.put(ProjectDataDAO.description, "A dummy description");

        return new ProjectDataDAO(options);
    }

    private static ActionHandlers dummyHandlers = new ActionHandlers(){
        public void launchProject(ProjectRecordPanel panel) {}
        public void createProject(ProjectCreationPanel panel) {}
        public void deleteProject(ProjectRecordPanel panel) {}
        public void updateDescription(ProjectRecordPanel panel){}
        public void refreshProject(ProjectRecordPanel panel) {}
        public void stopServer(ProjectRecordPanel panel) {}
    };

    public static ProjectRecordPanel createDummyPanel(String title) {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(ProjectDataDAO.title, title);
        options.put(ProjectDataDAO.identifier, "owner/folder");
        options.put(ProjectDataDAO.description, "A dummy description");
        options.put(ProjectDataDAO.owner, "owner");

        ProjectRecordPanel panel = new ProjectRecordPanel(
                new ProjectListItem(new ProjectDataDAO(options)),
                dummyHandlers);

        //panel.setAlignmentY(Component.TOP_ALIGNMENT);
        //panel.setLinked(true);

        return panel;
    }

    public static void createFrame() {
        final JFrame frame = new JFrame();

        final JPanel container = new JPanel(new ViewVerticalLayout());

        //container.setAlignmentY(Component.TOP_ALIGNMENT);
        //container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS)); // Y_AXIS
        //container.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Y_AXIS

        container.add(createDummyPanel("Bio Research Project"));
        container.add(createDummyPanel("My yet another test project"));
        container.add(createDummyPanel("Project MARS"));
        container.add(createDummyPanel("Jeta"));
        container.add(createDummyPanel("Influenza sequencing analysis"));
        container.add(createDummyPanel("Project 6"));
        container.add(createDummyPanel("Project 7"));

        JScrollPane scroll = new JScrollPane(container);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(scroll, BorderLayout.CENTER);

        JButton butt = ButtonUtil.makeButton("new");
        butt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ProjectRecordPanel panel = createDummyPanel("New Project");
                container.add(panel);
                container.revalidate();
                panel.expand();
                panel.scrollRectToVisible(panel.getVisibleRect());
                
            }
        });

        frame.add(butt, BorderLayout.SOUTH);


        frame.setSize(new Dimension(800,400));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run() {
                createFrame();
            }
        });
    }

}


