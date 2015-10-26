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
package workbench.views.basiceditor;


import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.properties.ViewProperties;
import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import workbench.RGui;
import workbench.views.basiceditor.BasicEditorPanel;
import workbench.views.DynamicView;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 14, 2009
 * Time: 11:29:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class BasicEditorView extends DynamicView {

    BasicEditorPanel editor = null;
    SearchPanel search = null;

    public BasicEditorView(String title, Icon icon, int id, RGui rgui, String function, String filename, int linenum) {

        super(title, icon, new JPanel(), id);

        BasicEditorTitleSetter setter = new BasicEditorTitleSetter(){

            public void set(String title) {
                BasicEditorView.this.getViewProperties().setTitle(title);
            }

            public String get() {
                return BasicEditorView.this.getViewProperties().getTitle();
            }
        };

        editor = new BasicEditorPanel(rgui, function, filename, setter, linenum);
        search = new SearchPanel(editor.getTextComponent());

        ((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).add(editor, BorderLayout.CENTER);
        ((JPanel) getComponent()).add(search, BorderLayout.SOUTH);

        editor.setSearchPanel(search);

        this.addListener(new DockingWindowAdapter() {
            public void windowClosing(DockingWindow dockingWindow) throws OperationAbortedException {
                if (!editor.handleWindowClosing()) {
                    throw new OperationAbortedException();
                }
            }
        });

        //editor.positionAt();
    }

    public BasicEditorView(String title, Icon icon, int id, RGui rgui, String function, String filename) {
        this(title, icon, id, rgui, function, filename, -1);
    }

    public BasicEditorPanel getEditor(){
        return editor;
    }

    public SearchPanel getSearchPanel(){
        return search;
    }

    public void dispose() {
        editor.dispose();
        //search.stopthreads();
    }
}
