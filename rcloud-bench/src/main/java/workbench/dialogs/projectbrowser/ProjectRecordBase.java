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

import uk.ac.ebi.rcloud.common.components.panel.JExpandablePanel;
import uk.ac.ebi.rcloud.common.util.KeyUtil;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 07/07/2011
 * Time: 12:14
 * To change this template use File | Settings | File Templates.
 */
public class ProjectRecordBase extends JExpandablePanel {

    protected ProjectListItem projectListItem = new ProjectListItem();
    protected ActionHandlers actionHandlers   = null;

    public ProjectListItem getProjectListItem() {
        return projectListItem;
    }

    public void setProjectListItem(ProjectListItem theItem) {
        this.projectListItem = theItem;
    }

    public ActionHandlers getActionHandlers() {
        return actionHandlers;
    }

    public void setActionHandlers(ActionHandlers actionHandlers) {
        this.actionHandlers = actionHandlers;
    }

    public ActionHandlers getUsableActionHandlers() {
        return actionHandlers;
    }

    class JLabelSmallFont extends JLabel {
        public JLabelSmallFont(){
            super();
            init();
        }
        public JLabelSmallFont(String text){
            super(text);
            init();
        }
        public void init(){
            Font font = getFont();
            Font newfont = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
            setFont(newfont);
        }
    }

    public class FieldUndoManager extends UndoManager implements UndoableEditListener {

        AbstractAction redoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                FieldUndoManager um = FieldUndoManager.this;
                if (um.canRedo()) {
                    um.redo();
                }
            }
        };

        AbstractAction undoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                FieldUndoManager um = FieldUndoManager.this;
                if (um.canUndo()) {
                    um.undo();
                }
            }
        };

        public void registerKeyAction(JTextComponent c, KeyStroke keyStroke,
                                      String actionName, AbstractAction action) {
            c.getInputMap().put(keyStroke, actionName);
            c.getActionMap().put(actionName, action);
        }

        public FieldUndoManager(JTextComponent c) {
            c.getDocument().addUndoableEditListener(this);

            registerKeyAction(c, KeyUtil.getKeyStroke(KeyEvent.VK_Z,
                    KeyEvent.META_MASK),
                    "undo", undoAction);

            registerKeyAction(c, KeyUtil.getKeyStroke(KeyEvent.VK_Z,
                    KeyEvent.META_MASK + KeyEvent.SHIFT_MASK),
                    "redo", redoAction);
        }

        public void undoableEditHappened(UndoableEditEvent undoableEditEvent) {
            addEdit(undoableEditEvent.getEdit());
        }
    }

}
