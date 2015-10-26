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

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.components.SideNumberingPane;
import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import uk.ac.ebi.rcloud.server.RType.RChar;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;
import workbench.completion.CompletionInterfaceAbstract;
import workbench.completion.CompletionSupport;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.generic.ImageContainer;
import workbench.generic.RConsole;
import workbench.manager.tooltipman.WorkbenchToolTip;
import workbench.util.*;
import uk.ac.ebi.rcloud.common.util.FontUtil;
import workbench.dialogs.SaveDialog;
import workbench.views.basiceditor.highlighting.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoManager;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;
import java.rmi.RemoteException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 14, 2009
 * Time: 10:07:23 AM
 * To change this template use File | Settings | File Templates.
 */

public class BasicEditorPanel extends JPanel implements RConsole {

    final private Logger log = LoggerFactory.getLogger(getClass());

    RGui rgui;

    private JButton executeButton;
    private JButton sourceBufferButton;
    private JButton sourceSelectionButton;
    private JButton saveButton;
    private JButton saveAsButton;
    private JButton helpButton;
    private JButton duplicateButton;
    private JButton formatButton;

    private JTextField filenameField;

    public NonWrappingTextPane getEditBuffer() {
        return editBuffer;
    }

    private BasicEditorTextPane editBuffer = new BasicEditorTextPane();
    private JScrollPane scrollPane         = new JScrollPane(editBuffer);

    SideNumberingPane numberPane   = null;
    private StatusLabel statusBar  = new StatusLabel(" ");
    private CompletionSupport editComplete = null;

    private String UNTITLED_NAME        = "Untitled";
    private String UNDEFINED_NAMESPACE  = "";
    private String namespace        = UNDEFINED_NAMESPACE;
    private String resourcename     = UNTITLED_NAME;
    private int    mode             = SaveDialog.FILE;
    private boolean newDoc          = true;
    private int linenum             = -1;

    private BasicEditorTitleSetter titleSetter = null;

    private UndoManager dummyUndoManager = new UndoManager();
    
    private HashMap<String, EditorAction> editorActions = new HashMap<String, EditorAction>();

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    private JPopupMenu popupMenu    = null;

    private JMenuItem helpItem;
    private JMenuItem viewSourceItem;
    private JMenuItem sbufferItem;
    private JMenuItem sselectionItem;
    private JMenuItem undoItem;
    private JMenuItem redoItem;
    private JMenuItem cutTextItem;
    private JMenuItem copyTextItem;
    private JMenuItem pasteTextItem;
    private JMenuItem findTextItem;
    private JMenuItem replaceTextItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;

    private SearchPanel search = null;

    private String addAsterisk(String text) {
        //log.info("text="+text);

        String result = text;
        String asterisk = "*";
        if (text.lastIndexOf(asterisk) == -1) {
            result = result + asterisk;
        }

        //log.info("result="+result);

        return result;
    }

    private String removeAsterisk(String text) {
        //log.info("text="+text);

        String result = text;
        String asterisk = "*";
        int i = text.lastIndexOf(asterisk);

        if (i != -1) {
            result = text.substring(0, i);
        }

        //log.info("result="+result);

        return result;
    }

    private WorkbenchToolTip messagetip = new WorkbenchToolTip();

    private long messageTipTimeout = 20 * 1000;

    public void printPrompt(String prompt) {
    }

    public void printImage(ImageContainer container) {
    }

    //private String EDITOR_ID = "editor-" + Long.toString(System.currentTimeMillis());
    private String EDITOR_ID = "default";

    private HashMap<String, Object> editorAttributes = initAttributes();

    public HashMap<String, Object> initAttributes() {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("originator", EDITOR_ID);
        return attributes;
    }

    public void dispose() {
        //rgui.removeConsole(EDITOR_ID);
    }

    //@Override
    //public void finalize() throws Throwable {
    //    log.info("finalizing");
    //    super.finalize();
    //}

    public void print(String output) {
        Dimension editorSize = BasicEditorPanel.this.getSize();
        int tipheight = 200;

        messagetip.setPreferredSize(new Dimension(editorSize.width - 40, tipheight));
        messagetip.setTipLocationRelativeTo(BasicEditorPanel.this, new Point(20, editorSize.height - tipheight - 20));
        messagetip.showToolTip("R Output", output, messageTipTimeout);
    }

    private boolean contentready = false;
    private boolean contentchanged = false;

    public void setContentReady(boolean ready) {
        contentready = ready;
    }

    public boolean isContentReady() {
        return contentready;
    }

    public boolean isContentChanged() {
        return contentchanged;
    }

    public void markChanged(boolean changed) {
        if (!isContentReady()) return;

        if (changed) {
            if (!contentchanged) {
                if (titleSetter != null)
                    titleSetter.set(addAsterisk(titleSetter.get()));
                contentchanged = true;
            }
        } else {
            if (titleSetter != null)
                titleSetter.set(removeAsterisk(titleSetter.get()));
            contentchanged = false;
        }
    }


    class StatusLabel extends JLabel implements CaretListener {
        public StatusLabel(String label) {
            super(label);
        }

        public void caretUpdate(CaretEvent e) {
            displaySelectionInfo(e.getDot(), e.getMark());
        }

        public void displaySelectionInfo(final int dot, final int mark) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    Document doc = editBuffer.getDocument();

                    int linenum = doc.getDefaultRootElement().getElementIndex(dot);

                    //int selectionStart = editBuffer.getSelectionStart();
                    //int selectionEnd = editBuffer.getSelectionEnd();
                    int caret = editBuffer.getCaretPosition();
                    int linestart = doc.getDefaultRootElement().getElement(linenum).getStartOffset();

                    setText(" " + (linenum + 1) + " : " + (caret - linestart + 1));
                }
            });
        }
    }

    class MyUndoableEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent event) {
            if (event.getEdit().isSignificant() &&
                    !event.getEdit().getPresentationName().contains("style")) {
                markChanged(true);
            }
        }
    }

    public UndoManager getUndoManager() {
        Document doc = editBuffer.getDocument();
        if (doc instanceof RSyntaxDocument) {
            return ((RSyntaxDocument) doc).getUndoManager();
        } else {
            return dummyUndoManager;
        }
    }

    public void setSearchPanel(SearchPanel search) {
        this.search = search;
    }

    public boolean handleWindowClosing() {
        return handleWindowClosing(true);
    }

    public boolean handleWindowClosing(boolean allowcancel) {

        boolean close = true;

        if (isContentChanged()) {
            boolean save = false;

            Object[] options = ( allowcancel ?
                    new String[] { "Save", "Discard", "Cancel" } :
                    new String[] { "Save", "Discard" } );

            int reply = JOptionPaneExt.showOptionDialog(BasicEditorPanel.this,
                    "Do you want to save changes ?\n",
                    "Save?",
                    JOptionPaneExt.DEFAULT_OPTION,
                    JOptionPaneExt.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch(reply) {
                case 0: save = true; break;
                case 1: save = false; break;
                case 2: close = false; break;
                default: break;
            }

            if (save) {
                saveCurrentBuffer();
            }
        }

        return close;
    }

    static final String COPY = "copy";
    static final String CUT = "cut";
    static final String PASTE = "paste";
    static final String REPLACE = "replace";
    static final String FIND = "find";
    static final String FINDNEXT = "findnext";
    static final String FINDPREVIOUS = "findprevious";
    static final String VIEWHELP = "viewhelp";
    static final String VIEWSOURCE = "viewsource";
    static final String SOURCEBUFFER = "sourcebuffer";
    static final String SOURCESELECTION = "sourceselection";
    static final String UNDO = "undo";
    static final String REDO = "redo";
    static final String ENTER = "enter";
    static final String TAB = "tab";
    static final String SHIFT_TAB = "shifttab";
    static final String EXECUTE = "execute";
    static final String DUPLICATE = "duplicate";
    static final String SAVE = "save";
    static final String SAVEAS = "saveas";
    static final String HOME = "home";
    static final String HOMESELECT = "homeselect";
    static final String END = "end";
    static final String ENDSELECT = "endselect";
    static final String FORMAT = "format";
    static final String BACKSPACE = "backspace";
    static final String DEL = "delete";

    private void initEditorActions() {

        editorActions.put(COPY, new CopyAction("Copy",
                KeyUtil.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK)));

        editorActions.put(CUT, new CutAction("Cut",
                KeyUtil.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK)));

        editorActions.put(PASTE, new PasteAction("Paste",
                KeyUtil.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK)));

        editorActions.put(REPLACE, new DummyAction("Replace",
                KeyUtil.getKeyStroke(KeyEvent.VK_R, KeyEvent.META_MASK)));

        editorActions.put(FIND, new FindAction("Find",
                KeyUtil.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_MASK)));

        editorActions.put(FINDNEXT, new FindNextAction("Find Next",
                KeyUtil.getKeyStroke(KeyEvent.VK_F3, 0)));

        editorActions.put(FINDPREVIOUS, new FindPreviousAction("Find Previous",
                KeyUtil.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK)));

        editorActions.put(VIEWHELP, new ViewHelpAction("View Help",
                KeyUtil.getKeyStroke(KeyEvent.VK_F1, 0)));

        editorActions.put(VIEWSOURCE, new ViewSourceAction("View Source",
                KeyUtil.getKeyStroke(KeyEvent.VK_F2, 0)));

        editorActions.put(SOURCEBUFFER, new SourceAllAction("Source Buffer",
                KeyUtil.getKeyStroke(KeyEvent.VK_F5, 0)));

        editorActions.put(SOURCESELECTION, new SourceSelectionAction("Source Selection",
                KeyUtil.getKeyStroke(KeyEvent.VK_F6, 0)));

        editorActions.put(UNDO, new UndoAction("Undo",
                KeyUtil.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK)));

        editorActions.put(REDO, new RedoAction("Redo",
                KeyUtil.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK + KeyEvent.SHIFT_MASK)));

        editorActions.put(ENTER, new EnterAction("Enter",
                KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0)));

        editorActions.put(TAB, new FormatAction("Tab",
                KeyUtil.getKeyStroke(KeyEvent.VK_TAB, 0), TAB_ACTION));

        editorActions.put(SHIFT_TAB, new FormatAction("Shift-Tab",
                KeyUtil.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK), SHIFT_TAB_ACTION));

        editorActions.put(EXECUTE, new ExecuteLineAction("Execute",
                KeyUtil.getKeyStroke(KeyEvent.VK_F8, 0)));

        editorActions.put(DUPLICATE, new DuplicateAction("Duplicate",
                KeyUtil.getKeyStroke(KeyEvent.VK_D, KeyEvent.META_MASK)));

        editorActions.put(SAVE, new SaveAction("Save",
                KeyUtil.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK)));

        editorActions.put(SAVEAS, new SaveAsAction("Save As",
                KeyUtil.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK + KeyEvent.SHIFT_MASK)));

        editorActions.put(HOME, new HomeAction("Home",
                KeyUtil.getKeyStroke(KeyEvent.VK_HOME, 0), false));

        editorActions.put(HOMESELECT, new HomeAction("Home Select",
                KeyUtil.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_MASK), true));

        editorActions.put(END, new EndAction("End",
                KeyUtil.getKeyStroke(KeyEvent.VK_END, 0), false));

        editorActions.put(ENDSELECT, new EndAction("End Select",
                KeyUtil.getKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_MASK), true));

        editorActions.put(FORMAT, new FormatAction("Format",
                KeyUtil.getKeyStroke(KeyEvent.VK_T, KeyEvent.META_MASK), FORMAT_ACTION));

        editorActions.put(BACKSPACE, new BackspaceAction("Backspace",
                KeyUtil.getKeyStroke(KeyEvent.VK_BACK_SPACE,0)));

        editorActions.put(DEL, new DelAction("Delete",
                KeyUtil.getKeyStroke(KeyEvent.VK_DELETE,0)));

    }

    // E D I T O R   M E N U
    //
    //


    private JMenuItem makeMenuItem(String actionname, String iconPath) {
        EditorAction action = editorActions.get(actionname);

        JMenuItem menuItem = new JMenuItem();

        if (action != null) {
            menuItem.setAction(action);

            if (action.getKeystroke() != null) {
                menuItem.setAccelerator(action.getKeystroke());
            }
        }
        if (iconPath != null) {
            menuItem.setIcon(new ImageIcon(ImageLoader.load(iconPath)));
        }

        return menuItem;
    }

    public void updateEditorPopupMenus() {
        undoItem.setEnabled(undoItem.getAction().isEnabled());
        redoItem.setEnabled(redoItem.getAction().isEnabled());
    }

    private void initEditorPopupMenus() {
        popupMenu = new JPopupMenu();

        helpItem = makeMenuItem(VIEWHELP, "/views/images/editor/menu/information.png");

        viewSourceItem = makeMenuItem(VIEWSOURCE, "/views/images/editor/menu/page_white_edit.png");

        sbufferItem = makeMenuItem(SOURCEBUFFER, "/views/images/editor/menu/script_go.png");

        sselectionItem = makeMenuItem(SOURCESELECTION, "/views/images/editor/menu/script_edit.png");

        undoItem = makeMenuItem(UNDO, null);

        redoItem = makeMenuItem(REDO, null);

        cutTextItem = makeMenuItem(CUT, "/views/images/editor/menu/cut.png");

        copyTextItem = makeMenuItem(COPY, "/views/images/editor/menu/page_copy.png");

        pasteTextItem = makeMenuItem(PASTE, "/views/images/editor/menu/editpaste.png");

        findTextItem = makeMenuItem(FIND, "/views/images/editor/menu/zoom.png");

        replaceTextItem = makeMenuItem(REPLACE, "/views/images/editor/menu/text_replace.png");

        findNextItem = makeMenuItem(FINDNEXT, null);

        findPreviousItem = makeMenuItem(FINDPREVIOUS, null);

        popupMenu.add(helpItem);
        popupMenu.add(viewSourceItem);
        popupMenu.addSeparator();

        popupMenu.add(copyTextItem);
        popupMenu.add(cutTextItem);
        popupMenu.add(pasteTextItem);
        popupMenu.addSeparator();

        popupMenu.add(findTextItem);
        popupMenu.add(replaceTextItem);
        popupMenu.addSeparator();

        popupMenu.add(findNextItem);
        popupMenu.add(findPreviousItem);
        popupMenu.addSeparator();

        popupMenu.add(sbufferItem);
        popupMenu.add(sselectionItem);
        popupMenu.addSeparator();

        popupMenu.add(undoItem);
        popupMenu.add(redoItem);
    }

    private String getAcceleratorText(KeyStroke ks) {
        String acceleratorText = "";
        String acceleratorDelimiter = "+";
        int modifiers = ks.getModifiers();
        int keyCode = ks.getKeyCode();
        if (modifiers > 0) {
            acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
            acceleratorText += acceleratorDelimiter;
        }
        if (keyCode != 0) {
            acceleratorText += KeyEvent.getKeyText(keyCode);
        }

        return acceleratorText;
    }

    private JButton makeEditorButton(String actionname, String imagePath) {

        EditorAction action = editorActions.get(actionname);
        String name = (String) action.getValue(Action.NAME);

        JButton b = ButtonUtil.makeButton("", imagePath,
                name + " (" + getAcceleratorText(action.getKeystroke()) +")");

        b.addActionListener(action);

        return(b);
    }


    private void initEditorButtons() {

        duplicateButton    = makeEditorButton(DUPLICATE, "/views/images/editor/mainwindow/text_horizontalrule.png");
        formatButton    = makeEditorButton(FORMAT, "/views/images/editor/mainwindow/text_indent.png");
        executeButton    = makeEditorButton(EXECUTE, "/views/images/editor/mainwindow/script_lightning.png");
        sourceBufferButton    = makeEditorButton(SOURCEBUFFER, "/views/images/editor/mainwindow/script_go.png");
        sourceSelectionButton = makeEditorButton(SOURCESELECTION, "/views/images/editor/mainwindow/script_edit.png");
        saveButton     = makeEditorButton(SAVE, "/views/images/editor/mainwindow/script_save.png");
        saveAsButton   = makeEditorButton(SAVEAS, "/views/images/editor/mainwindow/disk.png");
        helpButton     = makeEditorButton(VIEWHELP, "/views/images/editor/mainwindow/information.png");

    }


    // C O N S T R U C T O R
    //
    //

    public BasicEditorPanel(RGui rgui, String function, String filename,
                            BasicEditorTitleSetter setter) {
        this(rgui, function, filename, setter, -1);
    }

    public BasicEditorPanel(RGui rgui, String function, String filename,
                            BasicEditorTitleSetter setter, int linenum) {

        this.rgui      = rgui;
        titleSetter    = setter;

        if(function != null && !function.equals("")) {
            newDoc     = false;
            mode       = SaveDialog.FUNCTION;
            namespace  = getNamespace(function);
            resourcename = getFunctionName(function);
        }

        if(filename != null && !filename.equals("")) {
            newDoc     = false;
            resourcename = filename;
            mode       = SaveDialog.FILE;
        }

        editBuffer.setFont(new Font(FontUtil.getInstance().getCommonMonospaceFontFailyName(),
                Font.PLAIN, FontUtil.NORMAL_FONT_SIZE));

        //editBuffer.setBackground(new Color(0xfffff9));
        //editBuffer.setBackground(new Color(0xfffff0));
        editBuffer.setMargin(new Insets(5,5,5,5));
        editBuffer.addCaretListener(statusBar);

        numberPane = new SideNumberingPane(editBuffer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setRowHeaderView(numberPane);


        //scrollPane.getVerticalScrollBar().setUnitIncrement(4);

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel toolsContainer   = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2)); //GradientPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JPanel toolsPanel   = new JPanel(new BorderLayout());
        JPanel editorPanel  = new JPanel(new BorderLayout());
        JPanel statusPanel  = new JPanel(new BorderLayout());

        toolsContainer.setOpaque(false);
        toolsPanel.setOpaque(false);
        editorPanel.setOpaque(false);
        statusPanel.setOpaque(false);

        initEditorActions();
        initEditorPopupMenus();
        initEditorButtons();

        filenameField = new JTextField(resourcename);
        filenameField.setEditable(false);

        toolsPanel.add(toolsContainer, BorderLayout.EAST);
        toolsPanel.add(filenameField, BorderLayout.SOUTH);

        toolsContainer.add(duplicateButton);
        toolsContainer.add(formatButton);
        toolsContainer.add(new JLabel(" | "));
        toolsContainer.add(executeButton);
        toolsContainer.add(sourceBufferButton);
        toolsContainer.add(sourceSelectionButton);
        toolsContainer.add(new JLabel(" | "));
        toolsContainer.add(saveButton);
        toolsContainer.add(saveAsButton);
        toolsContainer.add(helpButton);

        editorPanel.add(scrollPane, BorderLayout.CENTER);
        statusPanel.add(statusBar, BorderLayout.SOUTH);

        add(toolsPanel, BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        editBuffer.setEditorKitForContentType("text/R", new REditorKit(editBuffer));
        editBuffer.setContentType("text/R");

        bindKeyActions();
        bindCompletion();

        EditorBrouseAdapter freeBrowseAdapter = new EditorBrouseAdapter(rgui, this);

        editBuffer.addMouseListener(freeBrowseAdapter);
        editBuffer.addMouseMotionListener(freeBrowseAdapter);
        editBuffer.addKeyListener(freeBrowseAdapter);
        editBuffer.addFocusListener(freeBrowseAdapter);
        editBuffer.getDocument().addUndoableEditListener(new MyUndoableEditListener());

        //rgui.addConsole(EDITOR_ID, BasicEditorPanel.this);

        if(!newDoc) {

            if (mode == SaveDialog.FUNCTION) {
                // load in function
                loadFunctionIntoBuffer();
            }

            else if (mode == SaveDialog.FILE) {
                // load in from file
                loadFileIntoBuffer(linenum);
            }
        } else {
            setContentReady(true);
        }
    }


    private void bindCompletion() {
        //
        // init: Command Completion
        //
        CompletionInterfaceAbstract completeInterface = new CompletionInterfaceAbstract(this.rgui) {

            private int start = 0;
            private int end = 0;

            private String gettext = null;
            private int getcaretposition = 0;

            public String getText() {

                Document doc = editBuffer.getDocument();
                int caretpos = editBuffer.getCaretPosition();
                Element root = doc.getDefaultRootElement();
                int linenum  = root.getElementIndex(caretpos);
                Element el   = root.getElement(linenum);

                start = el.getStartOffset();
                end   = el.getEndOffset() - 1;

                try {
                    gettext = doc.getText(start, end - start );
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }

                getcaretposition = caretpos - start;

                return gettext;
            }

            public int getCaretPosition() {
                return getcaretposition;
            }

            public void setText(String text) {
                Document doc = editBuffer.getDocument();

                try {
                    ((AbstractDocument) doc).replace(start, end - start, text, null);
                    end = start + text.length();
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }
            }

            public void setCaretPosition(int pos) {
                editBuffer.setCaretPosition(start + pos);
            }

            public void closePopup() {
                editComplete.closePopups();
            }

            public boolean canShow() {
                return true;
            }
        };

        // init popup components
        editComplete = new CompletionSupport("Completion", editBuffer, completeInterface);

        editComplete.setAppearanceOption(CompletionSupport.APPEARS_BELOW);

        editComplete.setDockingOption(CompletionSupport.FLOATING_DOCKING);

        editComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0),
                CompletionSupport.SELECT);
        editComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK),
                CompletionSupport.COMPLETE);
        editComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CompletionSupport.CANCEL);
        editComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0),
                CompletionSupport.SCROLL_UP);
        editComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0), 
                CompletionSupport.SCROLL_DOWN);


        // assign control hotkeys
        //
    }

    public void registerKeyAction(String actionname) {

        EditorAction action = editorActions.get(actionname);
        KeyStroke ks = action.getKeystroke();

        if (ks != null) {
            editBuffer.getInputMap().put(ks, actionname);
            editBuffer.getActionMap().put(actionname, action);
        }
    }

    private void bindKeyActions() {

        registerKeyAction(COPY);
        registerKeyAction(CUT);
        registerKeyAction(PASTE);
        registerKeyAction(REPLACE);

        registerKeyAction(ENTER);
        registerKeyAction(TAB);
        registerKeyAction(SHIFT_TAB);
        registerKeyAction(VIEWHELP);
        registerKeyAction(VIEWSOURCE);
        registerKeyAction(SOURCEBUFFER);
        registerKeyAction(SOURCESELECTION);
        registerKeyAction(EXECUTE);
        registerKeyAction(DUPLICATE);
        registerKeyAction(SAVE);
        registerKeyAction(SAVEAS);
        registerKeyAction(UNDO);
        registerKeyAction(REDO);
        registerKeyAction(HOME);
        registerKeyAction(HOMESELECT);
        registerKeyAction(END);
        registerKeyAction(ENDSELECT);
        registerKeyAction(BACKSPACE);
        registerKeyAction(DEL);
        registerKeyAction(FORMAT);

        //registerKeyAction(FIND);
        //registerKeyAction(FINDNEXT);
        //registerKeyAction(FINDPREVIOUS);

    }

    public JTextPane getTextComponent() {
        return editBuffer;
    }

    private String getNamespace(String arg) {
        String ns = "";

        int idx = arg.indexOf(":");

        if (idx != -1){
            ns = arg.substring(0, idx);
        }
        return ns;
    }

    private String getFunctionName(String arg) {
        String function = arg;

        int idx = arg.lastIndexOf(":");

        if (idx != -1) {
            function = arg.substring(idx+1);
        }
        return function;
    }

    //
    private boolean checkRisAvailable() {
        if (rgui == null) {
            JOptionPaneExt.showMessageDialog(rgui.getRootFrame(), "Sorry, no R Gui available");
            return false;
        }

        if (!rgui.isRAvailable()) {
            JOptionPaneExt.showMessageDialog(rgui.getRootFrame(), "Sorry, no R available");
            return false;
        }

        if (rgui.getRLock().isLocked()) {
            JOptionPaneExt.showMessageDialog(rgui.getRootFrame(), "Sorry, R server is busy");
            return false;
        }

        return true;
    }

    public String getRFunction(String function, String namespace) {

        try{
            rgui.getRLock().lock();

            String arg = namespace.length() > 0 ? (namespace + ":::" + function) : function;

            RChar result = (RChar) rgui.obtainR().getObject(".PrivateEnv$deparseArgument(" + arg + ")");

            if (result == null || result.getValue()[0].equals("NULL")) {
                return null;
            } else {
                String arr[] = result.getValue();
                StringBuilder build = new StringBuilder("");
                for(String s : arr) {
                    build.append(s);
                    build.append("\n");
                }

                return (build.toString());
            }

            /*
            String v = RUtils.newTemporaryVariableName();

            String status = rgui.obtainR().consoleSubmit(v + " <- " + arg);

            if (status != null && status.contains("Error")) {
                return null;
            }

            result = (RChar)rgui.obtainR().getObject("attr(" + v + ", 'source')");

            if (result == null || result.getValue()[0].equals("NULL")) {

                result = (RChar)rgui.obtainR().getObject("deparse(" + v + ")");

                if (result == null && result.getValue()[0].equals("NULL")) {
                    result = null;
                }
            }

            rgui.obtainR().consoleSubmit("rm(" + v + ")");
            */

        } catch(RemoteException e) {
            e.printStackTrace();
            return null;
        } finally {
            rgui.getRLock().unlock();
        }
    }

    public String setRFunction(String function, String namespace, String text) {

        try{
            rgui.getRLock().lock();

            String result = "";

            if (namespace.equals("")) {
                String command = "try ({ " + function + " <- " + text + " })";
                //String command = "try ({ assign('" + function + "', " + text + ", envir=.GlobalEnv); })";

                result = rgui.obtainR().sourceFromBuffer(command, editorAttributes);

                //result = rgui.obtainR().consoleSubmit(
                //        ,
                //        editorAttributes);
            }
            else {
                String command = "assignInNamespace(x='" + function + "', value=" + text + ", ns='"+ namespace +"')";

                //result = rgui.obtainR().consoleSubmit(cmd, editorAttributes);
                result = rgui.obtainR().sourceFromBuffer(command, editorAttributes);

                /*
                String v = RUtils.newTemporaryVariableName();

                result = rgui.obtainR().consoleSubmit(v + " <- " + text);

                if (!result.contains("Error")) {

                    String s = "assignInNamespace(x='" + function + "', value=" + v + ", ns='"+ namespace +"')";

                    result = rgui.obtainR().consoleSubmit(s);

                    rgui.obtainR().consoleSubmit("rm(" + v + ")");
                }
                */
            }

            return result;


        } catch(java.rmi.RemoteException e) {
            e.printStackTrace();
            return null;
        } finally {
            rgui.getRLock().unlock();
        }
    }

    // EDITOR ACTIONS
    //
    //

    private Action requesthelpAction = null;
    private void requestKeywordHelp() {
        if (!checkRisAvailable()) { return; }

        String keyword = EditorUtil.getSelectedkeyword(editBuffer);

        if (keyword != null) {
            if (requesthelpAction == null) {
                requesthelpAction = rgui.getActions().get("requesthelp");
            }
            requesthelpAction.actionPerformed(new ActionEvent(this, 0, keyword));
        }
    }

    private Action openfunctionAction = null;
    private void viewSourceCode() {
        if (!checkRisAvailable()) { return; }

        String keyword = EditorUtil.getSelectedkeyword(editBuffer);

        if (keyword != null) {
            if (openfunctionAction == null) {
                openfunctionAction = rgui.getActions().get("openfunction");
            }
            openfunctionAction.actionPerformed(new ActionEvent(this, 0, keyword));
        }
    }

    private void saveCurrentBufferAs() {
        saveCurrentBuffer(true);
    }

    private void saveCurrentBuffer() {
        saveCurrentBuffer(false);
    }

    private boolean requestNewName() {
        SaveDialog dialog = SaveDialog.getInstance(this);

        dialog.setOption(mode);

        if (mode == SaveDialog.FUNCTION) {
            dialog.setFName(resourcename);
        }

        //dialog.setVisible(true);
        dialog.visialize();

        if (dialog.getResult() == SaveDialog.OK) {
            mode = dialog.getOption();


            if (mode == SaveDialog.FUNCTION || mode == SaveDialog.FILE) {
                resourcename = dialog.getName();
                filenameField.setText(resourcename);
            }

            newDoc = false;

            // set title
            //
            if (titleSetter != null) {

                if (mode == SaveDialog.FILE) {
                    titleSetter.set(cuffOffFileNameFromPath(resourcename));
                } else {
                    titleSetter.set(resourcename);
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    public String cuffOffFileNameFromPath(String path) {
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }

        return path;
    }

    //
    //
    private void saveCurrentBuffer(boolean rename) {

        if(newDoc || rename) {
            if (!requestNewName()) {
                return;
            }
        }

        new Thread(new Runnable(){
            public void run(){
                if (mode == SaveDialog.FUNCTION) {
                    saveBufferAsFunction();
                }
                if (mode == SaveDialog.FILE) {
                    saveBufferAsAFile();
                }
            }
        }).start();
    }


    //
    //
    private void saveBufferAsAFile() {
        try{
            FileLoad.write(editBuffer.getText().getBytes(), resourcename, rgui.obtainR());

            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    String status = "saving file " + resourcename;
                    statusBar.setText(status);
                }
            });

            markChanged(false);

        } catch(java.rmi.RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return resourcename;
    }

    //
    //
    private void saveBufferAsFunction() {

        if (!checkRisAvailable()) { return; }

        final String[] resultContainer = new String[1];

        resultContainer[0] = setRFunction(resourcename, namespace, editBuffer.getText());

        EventQueue.invokeLater(new Runnable(){
            public void run(){
                rgui.getConsoleLogger().printAsOutput(resultContainer[0]);

                String status = "saving function " + ((namespace.equals("")) ? ("") : ( namespace + ":::" )) +
                        resourcename;

                statusBar.setText(status);
            }
        });

        markChanged(false);
    }


    //
    //
    private void setBufferText(final String text) {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                try {

                    Document doc = editBuffer.getDocument();

                    ((AbstractDocument)doc).replace(0, doc.getLength(), text, null);

                    getUndoManager().discardAllEdits();
                    setContentReady(true);


                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        });
    }

    //
    //
    private void loadFunctionIntoBuffer() {

        //if (!checkRisAvailable()) { return; }

        new Thread(new Runnable(){
            public void run(){

                String buffer = getRFunction(resourcename, namespace);

                if(buffer == null) {
                    buffer = "function() {\n\n}";
                }

                setBufferText(buffer);
            }
        }).start();
    }

    //
    //
    private void loadFileIntoBuffer(final int linenum) {

        new Thread(new Runnable(){
            public void run(){
                try{
                    byte[] data = FileLoad.read(resourcename, rgui.obtainRForFiles());
                    if (data != null) {
                        setBufferText(new String(data));

                        positionAt(linenum);
                    }
                    else {
                        setBufferText("");
                    }
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void positionAt(final int linenum) {

        EventQueue.invokeLater(new Runnable() {
            public void run(){

                if (linenum > 0) {
                    try {
                        Document doc = editBuffer.getDocument();

                        Element root  = doc.getDefaultRootElement();
                        Element el    = root.getElement(Math.min(linenum, root.getElementCount()));
                        int start     = el.getStartOffset();

                        editBuffer.setCaretPosition(start);
                        editBuffer.setSelectionStart(start);
                        editBuffer.setSelectionEnd(start);
                    } catch (Exception ex) {
                    }
                }
            }
        });
    }

    //
    //
    private void sourceSelection2R()
    {
        if (!checkRisAvailable()) { return; }

        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringSelection data = new StringSelection("");

        clipboard.setContents(data, data);

        editBuffer.getActionMap().get(DefaultEditorKit.copyAction).actionPerformed(null);

        new Thread(new Runnable() {
            public void run() {
                try {
                    rgui.getRLock().lock();

                    Transferable clipData = clipboard.getContents(clipboard);
                    if (clipData != null) {
                        if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {

                            String s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));

                            rgui.obtainR().sourceFromBuffer(s, editorAttributes);

                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    rgui.getConsoleLogger().printAsOutput("selection sourced to R\n");
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    rgui.getRLock().unlock();
                }
            }
        }).start();
    }

    //
    //
    private void sourceBuffer2R()
    {
        if (!checkRisAvailable()) { return; }

        final String data = editBuffer.getText();

        if (data != null && !data.equals(""))
        {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        rgui.getRLock().lock();
                        rgui.obtainR().sourceFromBuffer(data, editorAttributes);

                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                rgui.getConsoleLogger().printAsOutput(resourcename + " sourced\n");
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        rgui.getRLock().unlock();
                    }
                }
            }).start();
        }
    }

    // A C T I O N S
    //
    //
    //

    abstract class EditorAction extends AbstractAction {
        private KeyStroke keystroke;
        public EditorAction(String name, KeyStroke keystroke){
            super(name);
            this.keystroke = keystroke;
        }
        public KeyStroke getKeystroke() {
            return keystroke;
        }

        public void setKeystroke(KeyStroke keystroke) {
            this.keystroke = keystroke;
        }
    }

    class DummyAction extends EditorAction {
        public DummyAction(String name, KeyStroke ks) {
            super(name, ks);
        }

        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class CopyAction extends EditorAction {
        public CopyAction(String name, KeyStroke ks){
            super(name, ks);
        }

        public void actionPerformed(ActionEvent e) {
            TransferHandler.getCopyAction().
                    actionPerformed(new ActionEvent(editBuffer, ActionEvent.ACTION_PERFORMED, "copy"));
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class CutAction extends EditorAction {
        public CutAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            TransferHandler.getCutAction().
                    actionPerformed(new ActionEvent(editBuffer, ActionEvent.ACTION_PERFORMED, "cut"));
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class PasteAction extends EditorAction {
        public PasteAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            ((FormattedDocument) editBuffer.getStyledDocument()).requestFormatting();
            TransferHandler.getPasteAction().
                    actionPerformed(new ActionEvent(editBuffer, ActionEvent.ACTION_PERFORMED, "paste"));
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class ViewHelpAction extends EditorAction {
        public ViewHelpAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            requestKeywordHelp();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class ViewSourceAction extends EditorAction {
        public ViewSourceAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            viewSourceCode();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class SourceAllAction extends EditorAction {
        public SourceAllAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            sourceBuffer2R();
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class SourceSelectionAction extends EditorAction {
        public SourceSelectionAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            sourceSelection2R();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

    }

    class UndoRunnable implements Runnable {
        public void run(){
            getUndoManager().undo();
            //markChanged(false);
        }
    }

    private UndoRunnable undoRunnable = new UndoRunnable();

    class RedoRunnable implements Runnable {
        public void run(){
            getUndoManager().redo();
        }
    }

    private RedoRunnable redoRunnable = new RedoRunnable();

    class UndoAction extends EditorAction {
        public UndoAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(undoRunnable);
        }

        @Override
        public boolean isEnabled() {
            return getUndoManager().canUndo();
        }
    }

    class RedoAction extends EditorAction {
        public RedoAction(String name, KeyStroke ks){
            super(name, ks);
        }
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(redoRunnable);
        }
        @Override
        public boolean isEnabled() {
            return getUndoManager().canRedo();
        }

    }

    class FindAction extends EditorAction {
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

    class FindNextAction extends EditorAction {
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

    class FindPreviousAction extends EditorAction {
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


    class EnterAction extends EditorAction {
        public EnterAction(String name, KeyStroke ks){
            super(name, ks);
        }

        private String NEWLINE = "\n";

        public void actionPerformed(ActionEvent event) {
            int caretpos = editBuffer.getCaretPosition();
            Document doc = editBuffer.getDocument();
            DocumentLine line = EditorUtil.getLineAtPosition(doc, caretpos);

            String indent = null;
            Integer newcaret = null;

            int searchindex = caretpos - line.start;

            Vector<RSyntaxDocumentChange> changes = new Vector<RSyntaxDocumentChange>();

            // check if there's block closure after caret
            Integer followingblockclosure = EditorUtil.hasBlockClosureAfterPosition(line.text, searchindex);

            if (followingblockclosure != null) {

                String alltext = "";

                try {
                    alltext = doc.getText(0, caretpos);
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }

                indent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);
                /*
                indent = EditorUtil.findIndentSimple(alltext, caretpos - 1);
                if (indent == null) {
                    indent = EditorUtil.getLineIndent(line.text);
                }
                */


                if (indent.length() > EditorUtil.TABSPACE.length()) {
                    indent = indent.
                            substring(EditorUtil.TABSPACE.length());
                } else {
                    indent = "";
                }

                // "-1" is because if not, instead of "{" it will find first "}"
                Integer precedingblockopening = EditorUtil.hasBlockOpeningBeforePosition(line.text, searchindex - 1);

                if (followingblockclosure != searchindex) {
                    changes.add(new
                            RSyntaxDocumentChange(RSyntaxDocumentChange.REMOVE,
                            caretpos, followingblockclosure - searchindex, null));
                }

                changes.add(new
                        RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                        caretpos, 0,
                        NEWLINE + indent ));


                if (precedingblockopening != null) {
                    String newlineinsert = NEWLINE + indent + EditorUtil.TABSPACE;

                    changes.add(new
                            RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                            caretpos, 0,
                            newlineinsert));

                    newcaret = caretpos + newlineinsert.length();
                }

            } else {
                // check if there are arguments
                boolean opencodeblock = EditorUtil.hasOpenCodeBlocks(line.text, searchindex);

                if (opencodeblock) {
                    String alltext = "";

                    try {
                        alltext = doc.getText(0, caretpos);
                    } catch (BadLocationException ble) {
                      log.error("Error!", ble);
                    }


                    indent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);

                    /*
                    indent = EditorUtil.findIndentSimple(alltext, caretpos - 1);
                    if (indent == null) {
                        indent = EditorUtil.getLineIndent(line.text);
                    }
                    */

                    changes.add(new
                          RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                          caretpos, 0,
                          NEWLINE + indent ));

                } else {
                    String alltext = "";

                    try {
                        alltext = doc.getText(0, caretpos);

                    } catch (BadLocationException ble) {
                      log.error("Error!", ble);
                    }

                    indent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);

                    String lineindent = EditorUtil.getLineIndent(line.text);
                    /*
                    indent = EditorUtil.findIndentSimple(alltext, caretpos - 1);

                    if (indent == null) {
                        indent = lineindent;
                    }
                    */

                    // we use line indent, since it's the current
                    // line whitespace indent, if caret is located on
                    // it, shift the line down, otherwise insert a line break
                    //boolean indentarea = caretpos < line.start + lineindent.length();

                    boolean shiftline = (lineindent.length() == indent.length())
                            && ((caretpos - line.start) < lineindent.length()); //caretpos < line.start + lineindent.length();

                    changes.add(new
                            RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                            shiftline ? line.start : caretpos, 0,
                            shiftline ? (indent + NEWLINE) : (NEWLINE + indent)));
                }
            }

            try{
                ((RSyntaxDocument)editBuffer.getStyledDocument()).processChanges(changes, null);

                if (newcaret != null) {
                    editBuffer.setCaretPosition(newcaret);
                }
            } catch (javax.swing.text.BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        public void actionPerformedOld(ActionEvent event) {
            int caretpos = editBuffer.getCaretPosition();
            Document doc = editBuffer.getDocument();
            DocumentLine line = EditorUtil.getLineAtPosition(doc, caretpos);

            String indent = null;
            Integer newcaret = null;

            int searchindex = caretpos - line.start;

            Vector<RSyntaxDocumentChange> changes = new Vector<RSyntaxDocumentChange>();

            // check if there's block closure after caret
            Integer followingblockclosure = EditorUtil.hasBlockClosureAfterPosition(line.text, searchindex);

            if (followingblockclosure != null) {

                String alltext = "";

                try {
                    alltext = doc.getText(0, caretpos);
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }

                //indent = EditorUtil.findBlockOriginalIndent(alltext, caretpos - 1);
                indent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);


                if (indent.length() > EditorUtil.TABSPACE.length()) {
                    indent = indent.
                            substring(EditorUtil.TABSPACE.length());
                } else {
                    indent = "";
                }

                // "-1" is because if not, instead of "{" it will find first "}"
                Integer precedingblockopening = EditorUtil.hasBlockOpeningBeforePosition(line.text, searchindex - 1);

                if (followingblockclosure != searchindex) {
                    changes.add(new
                            RSyntaxDocumentChange(RSyntaxDocumentChange.REMOVE,
                            caretpos, followingblockclosure - searchindex, null));
                }

                changes.add(new
                        RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                        caretpos, 0,
                        NEWLINE + indent ));


                if (precedingblockopening != null) {
                    String newlineinsert = NEWLINE + indent + EditorUtil.TABSPACE;

                    changes.add(new
                            RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                            caretpos, 0,
                            newlineinsert));

                    newcaret = caretpos + newlineinsert.length();
                }

            } else {
                // check if there are arguments
                boolean opencodeblock = EditorUtil.hasOpenCodeBlocks(line.text, searchindex);

                if (opencodeblock) {
                    String alltext = "";

                    try {
                        alltext = doc.getText(0, caretpos);
                    } catch (BadLocationException ble) {
                      log.error("Error!", ble);
                    }

                    indent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);

                    changes.add(new
                          RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                          caretpos, 0,
                          NEWLINE + indent ));

                } else {
                    String alltext = "";

                    try {
                        alltext = doc.getText(0, caretpos);

                    } catch (BadLocationException ble) {
                      log.error("Error!", ble);
                    }

                    String lineindent = EditorUtil.getLineIndent(line.text);

                    indent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);

                    // we use line indent, since it's the current
                    // line whitespace indent, if caret is located on
                    // it, shift the line down, otherwise insert a line break
                    //boolean indentarea = caretpos < line.start + lineindent.length();
                    boolean shifline = (lineindent.length() == indent.length())
                            && ((caretpos - line.start) < lineindent.length()); //caretpos < line.start + lineindent.length();

                    changes.add(new
                            RSyntaxDocumentChange(RSyntaxDocumentChange.INSERT,
                            shifline ? line.start : caretpos, 0,
                            shifline ? (indent + NEWLINE) : (NEWLINE + indent)));
                }
            }

            try{
                ((RSyntaxDocument)editBuffer.getStyledDocument()).processChanges(changes, null);

                if (newcaret != null) {
                    editBuffer.setCaretPosition(newcaret);
                }
            } catch (javax.swing.text.BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    class DuplicateAction extends EditorAction {
        public DuplicateAction(String name, KeyStroke ks){
            super(name, ks);
        }

        public void actionPerformed(ActionEvent event) {
            int start = editBuffer.getSelectionStart();
            int end   = editBuffer.getSelectionEnd();

            if (start != end) {
                // close selection

                try {
                    Document doc  = editBuffer.getDocument();
                    doc.insertString(start, doc.getText(start, end-start), null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }

            } else {
                // clone line

                Document doc  = editBuffer.getDocument();
                int caret_pos = editBuffer.getCaretPosition();
                int linenum   = doc.getDefaultRootElement().getElementIndex(caret_pos);
                Element el    = doc.getDefaultRootElement().getElement(linenum);
                start         = el.getStartOffset();
                end           = el.getEndOffset();
                try {
                    doc.insertString(start, doc.getText(start, end-start), null);

                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
    }

    static int FORMAT_ACTION    = 0;
    static int TAB_ACTION       = 1;
    static int SHIFT_TAB_ACTION = 2;

    class FormatAction extends EditorAction {
        private int formataction;

        public FormatAction(String name, KeyStroke ks, int formataction){
            super(name, ks);
            this.formataction = formataction;
        }

        Runnable tabRunnable = new Runnable(){

            public void run() {

                Document doc = editBuffer.getDocument();
                Element root  = doc.getDefaultRootElement();

                StringBuilder builder = new StringBuilder();

                int caretpos = editBuffer.getCaretPosition();
                int selectionstart = editBuffer.getSelectionStart();
                int selectionend = editBuffer.getSelectionEnd();
                int newcaretpos = caretpos;

                int startoffset = 0;
                int endoffset = 0;

                boolean singleline = (selectionstart == selectionend);

                int firstline = root.getElementIndex(selectionstart);
                int lastline = firstline;

                if (!singleline) {
                    lastline = root.getElementIndex(selectionend);

                    if (root.getElement(lastline).getStartOffset() == selectionend) {
                        lastline --;
                    }
                }

                if (firstline != lastline) {
                    Element el0 = root.getElement(firstline);
                    Element el1 = root.getElement(lastline);
                    startoffset = el0.getStartOffset();
                    endoffset = Math.min(doc.getLength(), el1.getEndOffset());
                } else {
                    Element el0 = root.getElement(firstline);
                    startoffset = el0.getStartOffset();
                    endoffset = Math.min(doc.getLength(), el0.getEndOffset());
                }


                try {
                    String alltext = doc.getText(0, doc.getLength());
                    String formattedtext = alltext.substring(startoffset, endoffset);

                    DocumentBlockIndents indents = new DocumentBlockIndents();
                    indents.l = DocumentBlockLevels.analyzeNestedLevels(alltext, selectionstart - 1);

                    String blockindent = DocumentBlockIndents.computeIndentFromLevels(indents.l);
                    //String blockindent = EditorUtil.findBlockInnerIndent(alltext, selectionstart - 1);

                    //indents.indentMap.put(indents.blockcount, blockindent);

                    indents.currentindent = blockindent;
                    indents.nextindent = blockindent;

                    Vector<DocumentLine> lines = new LineIterator(formattedtext).getLines();

                    for (DocumentLine myLine : lines) {

                        String lineindent = EditorUtil.getLineIndent(myLine.text);

                        DocumentBlockIndents.updateBlockIndents(myLine.text, indents);

                        if (formataction == TAB_ACTION) {
                            String newindent = null;

                            if (singleline) {

                                // check caret position
                                if (caretpos <= startoffset + lineindent.length()) {

                                    // formatting
                                    if (lineindent.length() >= indents.currentindent.length()) {

                                        if (caretpos < startoffset + lineindent.length() &&
                                                myLine.text.trim().length() == 0) {

                                            newindent = indents.currentindent;
                                        } else {
                                            newindent = lineindent + EditorUtil.TABSPACE;
                                        }


                                    } else {
                                        newindent = indents.currentindent;
                                    }

                                    builder.append(newindent);
                                    builder.append(myLine.text.substring(lineindent.length()));

                                    // new caret position
                                    newcaretpos = startoffset + newindent.length();
                                } else {
                                    // inserting TAB

                                    builder.append(myLine.text.substring(0, caretpos - startoffset));
                                    builder.append(EditorUtil.TABSPACE);
                                    builder.append(myLine.text.substring(caretpos - startoffset));

                                    // new caret position
                                    newcaretpos = caretpos + EditorUtil.TABSPACE.length();
                                }

                            } else {

                                if (lineindent.length() >= indents.currentindent.length()) {
                                    newindent = lineindent + EditorUtil.TABSPACE;
                                } else {
                                    newindent = indents.currentindent;
                                }

                                builder.append(newindent);
                                builder.append(myLine.text.substring(lineindent.length()));
                            }

                        } else if (formataction == SHIFT_TAB_ACTION) {
                            if (lineindent.length() <= indents.currentindent.length()) {
                                if (lineindent.length() > EditorUtil.TABSPACE.length()) {
                                    builder.append(lineindent.substring(EditorUtil.TABSPACE.length()));
                                    if (singleline) {
                                        newcaretpos = Math.max(startoffset,
                                                caretpos - EditorUtil.TABSPACE.length());
                                    }
                                } else {
                                    if (singleline) {
                                        newcaretpos = Math.max(startoffset,
                                                caretpos - lineindent.length());
                                    }
                                }
                            } else {
                                builder.append(indents.currentindent);

                                if (singleline) {
                                    newcaretpos = Math.max(startoffset, caretpos -
                                            (lineindent.length() - indents.currentindent.length()));
                                }
                            }

                            builder.append(myLine.text.substring(lineindent.length()));

                        } else if (formataction == FORMAT_ACTION) {
                            builder.append(indents.currentindent);
                            builder.append(myLine.text.substring(lineindent.length()));
                        }
                    }

                    String newstring = builder.toString();

                    //((AbstractDocument)doc).replace(
                    ((RSyntaxDocument)doc).replace(
                            startoffset, endoffset - startoffset, newstring, null);

                    editBuffer.setCaretPosition(newcaretpos);

                    if (!singleline) {
                        editBuffer.setSelectionStart(startoffset);
                        editBuffer.setSelectionEnd(startoffset + newstring.length());
                    }

                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                    return;
                }
            }
        };

        public void actionPerformed(ActionEvent event) {
            EventQueue.invokeLater(tabRunnable);
        }
    }

    class SaveAction extends EditorAction {
        public SaveAction(String name, KeyStroke ks){
            super(name, ks);
        }

        public void actionPerformed(ActionEvent event) {
            saveCurrentBuffer();
        }
    }

    class SaveAsAction extends EditorAction {
        public SaveAsAction(String name, KeyStroke ks){
            super(name, ks);
        }

        public void actionPerformed(ActionEvent event) {
            saveCurrentBufferAs();
        }
    }

    class ExecuteLineAction extends EditorAction {

        Runnable execLinesRunnable = new Runnable(){
            public void run(){
                int caretpos = editBuffer.getCaretPosition();
                int selectionstart = editBuffer.getSelectionStart();
                int selectionend = editBuffer.getSelectionEnd();

                Document doc = editBuffer.getDocument();
                Element root  = doc.getDefaultRootElement();

                int firstline = root.getElementIndex(selectionstart);

                int lastline = (selectionstart == selectionend) ?
                        firstline : root.getElementIndex(selectionend);

                int caretline = (caretpos == selectionstart) ? firstline : lastline;

                int caretoffset = caretpos - root.getElement(caretline).getStartOffset();

                for (int lidx = firstline; lidx <= lastline; lidx++) {
                    Element curel = root.getElement(lidx);
                    int curstart  = curel.getStartOffset();
                    int curend    = curel.getEndOffset() - 1;

                    try {
                        String line = doc.getText(curstart, curend - curstart).trim();

                        if (line.length() != 0 && !line.startsWith("#")) {
                            //log.info("executing line="+line);

                            if (rgui != null) {
                                rgui.getConsole().play(line, false);
                            }

                            if (lidx < lastline) {
                                try { Thread.sleep(200); } catch (InterruptedException ie) {}
                            }

                        }
                    } catch (BadLocationException ble) {
                        log.info("Error!",ble);
                    }

                    Element nextel = root.getElement(lidx + 1);

                    if (nextel != null) {
                        int nextstart     = nextel.getStartOffset();
                        int nextend       = nextel.getEndOffset() - 1;

                        int nextoffset = Math.min(nextend, nextstart + caretoffset);
                        editBuffer.setCaretPosition(nextoffset);
                    }
                }
            }
        };

        public ExecuteLineAction(String name, KeyStroke ks){
            super(name, ks);
        }

        public void actionPerformed(ActionEvent event) {
            new Thread(execLinesRunnable).start();
        }
    }

    class HomeAction extends EditorAction {
        private boolean select = false;
        public HomeAction(String name, KeyStroke ks, boolean select){
            super(name, ks);

            this.select = select;
        }

        public void actionPerformed(ActionEvent event) {
            int caret = editBuffer.getCaretPosition();
            DocumentLine line = EditorUtil.getLineAtPosition(editBuffer.getDocument(), caret);

            // get the leading whitespaces
            int linestart = line.start + EditorUtil.getLineIndent(line.text).length();
            int newcaret;

            if (caret > line.start && caret <= linestart) {
                newcaret = line.start;
            } else {
                newcaret = linestart;
            }

            if (this.select) {
                editBuffer.moveCaretPosition(newcaret);
            } else {
                editBuffer.setCaretPosition(newcaret);
            }
        }
    }

    class EndAction extends EditorAction {
        private boolean select = false;

        public EndAction(String name, KeyStroke ks, boolean select){
            super(name, ks);
            this.select = select;
        }

        public void actionPerformed(ActionEvent event) {
            try {
                int caretpos = editBuffer.getCaretPosition();
                Document doc = editBuffer.getDocument();

                DocumentLine line = EditorUtil.getLineAtPosition(doc, caretpos);

                int reallength = line.end - line.start - 1;

                if (line.text.trim().length() == 0) {
                    String alltext = doc.getText(0, doc.getLength());
                    String blockindent = EditorUtil.findBlockInnerIndent(alltext, caretpos - 1);

                    if (reallength < blockindent.length()) {
                        String toinsert = blockindent.substring(reallength);
                        doc.insertString(line.end - 1, toinsert, null);
                        reallength = blockindent.length();
                    }
                }

                int newposition = line.start + reallength;

                if (select) {
                    editBuffer.moveCaretPosition(newposition);
                } else {
                    editBuffer.setCaretPosition(newposition);
                }

            } catch (BadLocationException ble) {
                log.error("Error!", ble);
            }
        }
    }

    class BackspaceAction extends EditorAction {
        private Action delegate;

        public BackspaceAction(String name, KeyStroke ks){
            super(name, ks);
            delegate = editBuffer.getActionMap().
                    get(DefaultEditorKit.deletePrevCharAction);
        }

        public void actionPerformed(ActionEvent event) {
            try {
                Caret caret = editBuffer.getCaret();
                Document doc = editBuffer.getDocument();

                int dot = caret.getDot();
                int mark = caret.getMark();

                if (dot != mark) {
                    //doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                    delegate.actionPerformed(event);
                } else {

                    Element root = doc.getDefaultRootElement();
                    int linenum = root.getElementIndex(dot);
                    Element el0 = root.getElement(linenum);
                    int start   = el0.getStartOffset();

                    if (start == dot && linenum > 0) {
                        DocumentLine previousline = EditorUtil.getLineAtIndex(doc, linenum - 1);

                        if (previousline.text.trim().length() == 0) {
                            doc.remove(previousline.start, dot - previousline.start);
                        } else {
                            delegate.actionPerformed(event);
                        }
                    } else {
                        delegate.actionPerformed(event);
                    }
                }
            } catch (BadLocationException ble) {
                log.error("Error!", ble);
            }
        }
    }

    class DelAction extends EditorAction {
        private Action delegate;

        public DelAction(String name, KeyStroke ks){
            super(name, ks);
            delegate = editBuffer.getActionMap().
                    get(DefaultEditorKit.deleteNextCharAction);
        }

        public void actionPerformed(ActionEvent event) {
            try {
                Caret caret = editBuffer.getCaret();
                Document doc = editBuffer.getDocument();

                int dot = caret.getDot();
                int mark = caret.getMark();

                if (dot != mark) {
                    //doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                    delegate.actionPerformed(event);
                } else {

                    Element root = doc.getDefaultRootElement();
                    int linenum = root.getElementIndex(dot);
                    DocumentLine line = EditorUtil.getLineAtIndex(doc, linenum);

                    if (line.text.trim().length() == 0) {
                        doc.remove(line.start, line.end - line.start);
                    } else {
                        delegate.actionPerformed(event);
                    }
                }
            } catch (BadLocationException ble) {
                log.error("Error!", ble);
            }
        }
    }

    // test GUI
    //
    //
    //


    public static void createGUI(){

        JFrame frame = new JFrame();
        BasicEditorPanel editor = new BasicEditorPanel(null, "", "", null);

        frame.add(editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(new Dimension(500, 400));
        frame.setVisible(true);
    }


    public static void main(String args[]){
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                createGUI();
            }
        });
    }

}



