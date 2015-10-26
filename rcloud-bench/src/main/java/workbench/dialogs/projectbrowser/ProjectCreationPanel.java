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

import uk.ac.ebi.rcloud.common.components.textfield.JHintTextField;
import uk.ac.ebi.rcloud.common.components.textfield.JHintTextPane;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import workbench.util.ButtonUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 06/07/2011
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCreationPanel extends ProjectRecordBase {

    // accessible fields
    public JHintTextField titleField;
    public JHintTextPane descriptionField;

    // buttons
    protected JButton create;

    // undo managers
    private FieldUndoManager titleUndo;
    private FieldUndoManager descreUndo;


    //
    //   C O N S T R U C T O R
    //
    public ProjectCreationPanel(ProjectListItem item, ActionHandlers handlers) {
        initProjectCreationPanel();

        if (item != null) {
            setProjectListItem(item);
        }

        if (handlers != null) {
            setActionHandlers(handlers);
        }
    }

    //
    //   A C C E S S O R S
    //
    public String getDescription() {
        return descriptionField.getText();
    }

    public void setDescription(String text) {
        descriptionField.setText(text);
    }

    public String getFolder() {

        String text0 = getTitle();

        while (text0.contains(" ")) {
            text0 = text0.replaceAll(" ", "");
        }

        return text0;
    }

    public String getTitle() {
        return titleField.getText();
    }

    public void setTitle(String text) {
        titleField.setText(text);
    }

    public void setProjectListItem(ProjectListItem item) {
        super.setProjectListItem(item);

        if (item != null) {

            ProjectDataDAO project  = item.getProject();

            setTitle(project.getTitle());

            setDescription(project.getDescription());
        }
    }

    public ProjectListItem getProjectListItem() {

        ProjectListItem item = super.getProjectListItem();

        item.getProject().getMap().put(ProjectDataDAO.identifier,
                item.getProject().getOwner() + "/" + getFolder());

        item.getProject().getMap().put(ProjectDataDAO.title, getTitle());
        item.getProject().getMap().put(ProjectDataDAO.description, getDescription());

        return super.getProjectListItem();
    }

    private ProjectCreationPanel getPanel(){
        return ProjectCreationPanel.this;
    }


    //
    //   D I A L O G
    //

    public void initProjectCreationPanel() {

        int iconsize = 20;

        create = ButtonUtil.makeButton("Create & Launch", "/views/images/projectlist/page_lightning.png", null);

        Font f0 = getFont();
        Font f1 = new Font(f0.getFamily(), Font.BOLD, f0.getSize() + 1);
        Font f2 = new Font(f0.getFamily(), Font.PLAIN, f0.getSize() - 4);


        titleField = new JHintTextField("Title");

        //titleField.setOpaque(false);
        titleField.setFont(f1);

        //JPanel titlePanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        //titlePanelLeft.setOpaque(false);

        JPanel titlePanelRight = new JPanel(new BorderLayout()); //new FlowLayout(FlowLayout.RIGHT, 6, 0)
        titlePanelRight.setOpaque(false);
        titlePanelRight.setBorder(new EmptyBorder(2, 5, 2, 5));

        titlePanelRight.add(create, BorderLayout.CENTER);

        JPanel titlePanelCenter = new JPanel(new BorderLayout());
        titlePanelCenter.setOpaque(false);
        titlePanelCenter.setBorder(new EmptyBorder(2, 5, 2, 5));

        titlePanelCenter.add(titleField, BorderLayout.CENTER);

        titleContainer.setBorder(new EmptyBorder(2,5,2,5));
        titleContainer.setOpaque(false);
        //titleContainer.add(titlePanelLeft, BorderLayout.WEST);
        titleContainer.add(titlePanelCenter, BorderLayout.CENTER);
        titleContainer.add(titlePanelRight, BorderLayout.EAST);
        //titleContainer.add(new JLabel(" "), BorderLayout.SOUTH);

        descriptionField = new JHintTextPane("Comment");
        descriptionField.setMargin(new Insets(2,2,2,2));

        expandableContainer.setOpaque(false);
        expandableContainer.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5,10,5,10)));

        JScrollPane descrScroll = new JScrollPane(descriptionField);
        descrScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        descrScroll.setBorder(new LineBorder(Color.LIGHT_GRAY));

        expandableContainer.add(descrScroll, BorderLayout.CENTER);

        setPreferredTitleSize(new Dimension(739, 40));
        setPreferredContainerSize(new Dimension(739, 150));

        initUndoManagers();

        initActionListeners();
    }

    //
    //   H A N D L E R S
    //

    private void initActionListeners() {
        create.addMouseListener(new CreateProjectMouseAdapter());
    }

    class CreateProjectMouseAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
            getActionHandlers().createProject(getPanel());
        }
    }

    public void initUndoManagers() {
        titleUndo = new FieldUndoManager(titleField);
        descreUndo = new FieldUndoManager(descriptionField);
    }

    private static ActionHandlers dummyHandlers = new ActionHandlers(){
        public void createProject(ProjectCreationPanel panel) {}
        public void launchProject(ProjectRecordPanel panel) {}
        public void deleteProject(ProjectRecordPanel panel) {}
        public void updateDescription(ProjectRecordPanel panel){}
        public void refreshProject(ProjectRecordPanel panel) {}
        public void stopServer(ProjectRecordPanel panel) {}
    };

    public static ProjectCreationPanel createDummyCreationPanel(String title) {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(ProjectDataDAO.title, title);
        options.put(ProjectDataDAO.identifier, "owner/folder");
        options.put(ProjectDataDAO.description, "A dummy description");
        options.put(ProjectDataDAO.owner, "owner");

        ProjectCreationPanel panel = new ProjectCreationPanel(
                new ProjectListItem(new ProjectDataDAO(options)),
                dummyHandlers);

        panel.setLinked(true);

        return panel;
    }


    public static void createFrame() {
        final JFrame frame = new JFrame();
        final JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        //gbc.anchor = GridBagConstraints.NORTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        //gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;



        container.add(createDummyCreationPanel("Bio Research Project"), gbc);
        container.add(createDummyPanel("My yet another test project"), gbc);
        container.add(createDummyPanel("Project MARS"), gbc);
        container.add(createDummyPanel("Jeta"), gbc);
        container.add(createDummyPanel("Influenza sequencing analysis"), gbc);
        container.add(createDummyPanel("Project 6"), gbc);
        container.add(createDummyPanel("Project 7"), gbc);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);


        frame.add(scroll, BorderLayout.CENTER);

        frame.setSize(new Dimension(800,600));
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
