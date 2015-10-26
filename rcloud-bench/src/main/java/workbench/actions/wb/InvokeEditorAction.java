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
package workbench.actions.wb;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.OperationAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.RGui;
import workbench.util.AbstractDockingWindowListener;
import workbench.views.DynamicView;
import workbench.views.basiceditor.BasicEditorView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 2, 2010
 * Time: 11:17:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class InvokeEditorAction extends AbstractAction {

    final private static Logger log = LoggerFactory.getLogger(InvokeEditorAction.class);
    final private String untitled_title = "Untitled";

    private RGui rgui;

    public InvokeEditorAction(RGui rgui, String name){
        super(name);
        this.rgui = rgui;
    }

    public void actionPerformed(ActionEvent event) {
        invokeBasicEditor(untitled_title, null, null);
    }

    public boolean isEnabled() {
        return true;
    }

    public void invokeBasicEditor(String title, String function, String filename, int linenum) {
        int id = rgui.getViewManager().getDynamicViewId();

        if (!title.equals(untitled_title)) {

            Vector<DynamicView> editorviews =
                    rgui.getViewManager().getAllViewsOfClass(BasicEditorView.class);

            for (DynamicView wiew : editorviews) {

                if (wiew instanceof BasicEditorView) {

                    String name0 = ((BasicEditorView) wiew).getEditor().getName();

                    if ((filename != null && filename.equals(name0)) ||
                            (function != null && function.equals(name0)) ) {

                        //if (title0.startsWith(title) &&
                        //        title0.length() - title.length() <= 1)

                        // found it, make visible
                        wiew.makeVisible();

                        // set the line
                        ((BasicEditorView) wiew).getEditor().positionAt(linenum);

                        return;
                    }
                }
            }
        }

        BasicEditorView edview =
                new BasicEditorView(title, null, id, rgui, function, filename, linenum);

        rgui.getMainTabWindow().addTab(edview);

        edview.addListener(new EditorDockingWindowListener(edview));
    }

    public void invokeBasicEditor(String title, String function, String filenam) {
        invokeBasicEditor(title, function, filenam, -1);
    }


    class EditorDockingWindowListener extends AbstractDockingWindowListener {
        BasicEditorView view;

        public EditorDockingWindowListener(BasicEditorView view){
            this.view = view;
        }

        public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
            view.dispose();
        }
    }

}
