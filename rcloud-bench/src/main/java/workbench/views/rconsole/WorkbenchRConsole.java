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
package workbench.views.rconsole;

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import uk.ac.ebi.rcloud.server.callback.RActionConst;
import workbench.actions.WorkbenchActionType;
import workbench.completion.*;
import workbench.RGui;
import workbench.manager.tooltipman.WorkbenchToolTip;
import workbench.util.EditorUtil;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 16, 2009
 * Time: 4:52:41 PM
 * To change this template use File | Settings | File Templates.
 */

public class WorkbenchRConsole extends RConsoleBase {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private RGui rgui = null;
    private CompletionSupport cmdSearch	= null;
    private CompletionSupport cmdComplete = null;
    private SearchPanel search   = new SearchPanel(logArea);

    private static String CONSOLE_ID = "default";

    public WorkbenchRConsole(RGui rgui) {

        super("> ");

        super.setSubmitInterface(new ConsoleSubmitInterface());

        this.rgui = rgui;

        initConsoleActions();
        initCommandSearch();
        initCommandCompletion();

        search.setVisible(false);

        centerPanel.add(search, BorderLayout.SOUTH);

        rgui.addConsole(CONSOLE_ID, this);
    }

    class ConsoleSubmitInterface implements RConsoleSubmitInterface {

        private WorkbenchToolTip consoletip = new WorkbenchToolTip();
        private long tiptimeout = 10 * 1000;
        private long ct = 0;

        private void showCommandQueueingTip() {
            long ct0 = System.currentTimeMillis();
            if (ct0 - ct > tiptimeout) {
                ct = ct0;

                consoletip.setTipLocationRelativeTo(
                        getCommandInputField(), new Point(20,-20));

                consoletip.showToolTip("Commands queued",
                        "commands you enter are being queued for execution "+
                                "and will be executed as soon as previous tasks are finished",
                        consoletip.getDefaultTimeout());
            }
        }

        public String submit(String expression) {

            if (rgui.obtainR() == null) {
                JOptionPaneExt.showMessageDialog(WorkbenchRConsole.this,
                        "Sorry, you're not connected to R server", "",
                        JOptionPaneExt.WARNING_MESSAGE);

                return null;
            }

            expression = expression.trim();

            if (rgui.getRLock().isLocked()) {
                showCommandQueueingTip();
            }

            try {

                print(expression, null);

                HashMap<String, Object> attributes
                        = new HashMap<String, Object>();

                attributes.put(RActionConst.ORIGINATOR, CONSOLE_ID);

                rgui.obtainR().asynchronousConsoleSubmit(expression, attributes);

            } catch (Exception ex) {
            }

            // push history to server
            rgui.getActions().get(WorkbenchActionType.SAVEHISTORY).
                    actionPerformed(new ActionEvent(this, 0, expression));

            return null;
        }
    }


    //  popup-related routines
    //
    public void handleComponentMoved() {
        cmdSearch.handleComponentMoved();
        cmdComplete.handleComponentMoved();
    }

    public void handleComponentResized() {
        cmdSearch.handleComponentResized();
        cmdComplete.handleComponentResized();
    }

    private void initCommandSearch() {
        // init: Command History Search
        //
        CompletionInterface _cmdSearch_provider = new CompletionInterface(){
            public CompletionResult provideResult() {
                // provide command history
                Vector<String> history = getCommandHistory();

                int size = history.size();
                CompletionResult result = new CompletionResult();

                for (int i = size - 1; i >= 0; i--) {
                    result.add(new CompletionItem(history.get(i), "command"));
                }

                return result;
            }

            public String providePattern() {
                return (commandInputField.getText());
            }

            public void acceptResult(final String string, final int offset) {
                commandInputField.setText( string );

                if (offset != 0) {
                    commandInputField.setCaretPosition(
                            commandInputField.getCaretPosition() + offset);
                }
            }

            public void makeAddition(final String string) {
                commandInputField.setText( string );
            }

            public boolean canShow() {
                return (!cmdComplete.isActive());
            }

            public void handlePopupShowed(){
            }

            public void handlePopupClosed(){
            }
        };

        cmdSearch = new CompletionSupport("History Search", commandInputField, _cmdSearch_provider);

        cmdSearch.setAppearanceOption(CompletionSupport.APPEARS_ABOVE);

        cmdSearch.setFilter(CompletionSupport.FREETEXT_FILTER);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0),
                CompletionSupport.SELECT);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_TAB, 0),
                CompletionSupport.COMPLETE);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK),
                CompletionSupport.COMPLETE);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CompletionSupport.CANCEL);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0),
                CompletionSupport.SCROLL_UP);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0),
                CompletionSupport.SCROLL_DOWN);

    }

    private void initCommandCompletion() {

        //
        // init: Command Completion
        //
        CompletionInterfaceAbstract _cmdComplete_provider = new CompletionInterfaceAbstract(this.rgui) {

            public String getText() {
                return commandInputField.getText();
            }

            public int getCaretPosition() {
                return commandInputField.getCaretPosition();
            }

            public void setText(String text) {
                commandInputField.setText(text);
            }

            public void setCaretPosition(int pos) {
                commandInputField.setCaretPosition(pos);
            }

            public void closePopup() {
                cmdComplete.closePopups();
            }

            public boolean canShow() {
                return (!cmdSearch.isActive());
            }
        };

        // init popup components
        cmdComplete = new CompletionSupport("Completion", commandInputField, _cmdComplete_provider);

        cmdComplete.setAppearanceOption(CompletionSupport.APPEARS_ABOVE);

        // assign control hotkeys
        //

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0),
                CompletionSupport.SELECT);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_TAB, 0),
                CompletionSupport.COMPLETE);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK),
                CompletionSupport.COMPLETE);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CompletionSupport.CANCEL);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0),
                CompletionSupport.SCROLL_UP);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0),
                CompletionSupport.SCROLL_DOWN);
    }

    private static String FIND          = "console-find";
    private static String FINDNEXT      = "console-findnext";
    private static String FINDPREVIOUS  = "console-findprevious";
    private static String VIEWHELP      = "console-viewhelp";
    private static String VIEWSOURCE    = "console-viewsource";
    private static String STOPEVAL      = "console-stopeval";
    private static String LOADHISTORY   = "console-loadhistory";
    private static String SAVEHISTORY   = "console-savehistory";

    private void initConsoleActions(){

        initAction(VIEWHELP, new ViewHelpAction("View Help",
                KeyUtil.getKeyStroke(KeyEvent.VK_F1, 0)),
                "/views/images/rconsole/information.png");

        initAction(VIEWSOURCE, new ViewSourceAction("View Source",
                KeyUtil.getKeyStroke(KeyEvent.VK_F2, 0)),
                "/views/images/rconsole/page_white_edit.png");

        initAction(FIND, new FindAction("Find",
                KeyUtil.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_MASK)),
                "/views/images/rconsole/zoom.png");

        initAction(FINDNEXT, new FindNextAction("Find Next",
                KeyUtil.getKeyStroke(KeyEvent.VK_F3, 0)),
                null);

        initAction(FINDPREVIOUS, new FindPreviousAction("Find Previous",
                KeyUtil.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK)),
                null);

        initAction(STOPEVAL, new StopRAction("Stop R",
                null),
                "/views/images/rconsole/cross_octagon_fram.png");

        initAction(LOADHISTORY, new LoadHistoryAction("Load History",
                null),
                null);

        initAction(SAVEHISTORY, new SaveHistoryAction("Save History",
                null),
                null);

        registerLogviewAction(STOPEVAL);
        registerLogviewAction(VIEWHELP);
        registerLogviewAction(FIND);
        registerLogviewAction(FINDNEXT);
        registerLogviewAction(FINDPREVIOUS);
        registerLogviewAction(VIEWSOURCE);
        registerLogviewAction(LOADHISTORY);
        registerLogviewAction(SAVEHISTORY);
    }


    @Override
    public JPopupMenu initLogviewPopupMenu(){
        JPopupMenu menu = super.initLogviewPopupMenu();

        menu.addSeparator();
        addMenuItem(menu, FIND);
        addMenuItem(menu, FINDNEXT);
        addMenuItem(menu, FINDPREVIOUS);

        menu.addSeparator();
        addMenuItem(menu, VIEWHELP);
        addMenuItem(menu, VIEWSOURCE);

        menu.addSeparator();
        addMenuItem(menu, STOPEVAL);


        JMenu historymenu = (JMenu) menu.getClientProperty(HISTORYMENU);

        addMenuItem(historymenu, LOADHISTORY);
        addMenuItem(historymenu, SAVEHISTORY);

        return menu;
    }

    //   A C T I O N S
    //
    //

    class ViewHelpAction extends ConsoleAction {
        private Action requesthelpAction = rgui.getActions().get(WorkbenchActionType.REQUESTHELP);
        public ViewHelpAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }
        public void actionPerformed(ActionEvent event) {
            String keyword = EditorUtil.getSelectedkeyword(logArea);
            if (keyword != null) {
                requesthelpAction.actionPerformed(new ActionEvent(this, 0, keyword));
            }
        }
        public boolean isEnabled() {
            return requesthelpAction.isEnabled();
        }
    }

    class ViewSourceAction extends ConsoleAction {
        private Action openfunctionAction = rgui.getActions().get(WorkbenchActionType.OPENFUNCTION);
        public ViewSourceAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }
        public void actionPerformed(ActionEvent e) {
            String keyword = EditorUtil.getSelectedkeyword(logArea);
            if (keyword != null) {
                openfunctionAction.actionPerformed(new ActionEvent(this, 0, keyword));
            }
        }
        public boolean isEnabled() {
            return openfunctionAction.isEnabled();
        }
    }

    class StopRAction extends ConsoleAction {
        private Action stopRAction = rgui.getActions().get(WorkbenchActionType.STOPEVAL);
        public StopRAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }
        public void actionPerformed(ActionEvent event) {
            stopRAction.actionPerformed(event);
        }

        public boolean isEnabled() {
            return stopRAction.isEnabled();
        }
    }

    class LoadHistoryAction extends ConsoleAction {
        private Action loadHistoryAction = rgui.getActions().get(WorkbenchActionType.LOADHISTORY);
        public LoadHistoryAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }
        public void actionPerformed(ActionEvent event) {
            loadHistoryAction.actionPerformed(event);
        }

        public boolean isEnabled() {
            return loadHistoryAction.isEnabled();
        }
    }

    class SaveHistoryAction extends ConsoleAction {
        private Action saveHistoryAction = rgui.getActions().get(WorkbenchActionType.SAVEHISTORY);
        public SaveHistoryAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }
        public void actionPerformed(ActionEvent event) {
            saveHistoryAction.actionPerformed(event);
        }

        public boolean isEnabled() {
            return saveHistoryAction.isEnabled();
        }
    }


    class FindAction extends ConsoleAction {
        public FindAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            search.getActivateAction().actionPerformed(e);
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class FindNextAction extends ConsoleAction {
        public FindNextAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            search.getFindNextAction().actionPerformed(e);
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class FindPreviousAction extends ConsoleAction {
        public FindPreviousAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            search.getFindPreviousAction().actionPerformed(e);
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }


    @Override
    public boolean isInputLocked() {
        return (cmdSearch.isActive() || cmdComplete.isActive());
    }

}

