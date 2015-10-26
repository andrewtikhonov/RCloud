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

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import uk.ac.ebi.rcloud.common.components.JTextPaneExt;
import uk.ac.ebi.rcloud.common.util.FontUtil;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.generic.ImageContainer;
import workbench.generic.RConsole;

import java.awt.*;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.undo.UndoManager;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 26, 2009
 * Time: 10:44:33 AM
 * To change this template use File | Settings | File Templates.
 */

public class RConsoleBase extends JPanel implements ClipboardOwner, RConsole {

    final private static Logger log = LoggerFactory.getLogger(RConsoleBase.class);

    public RConsoleSubmitInterface sInterface;

	public JTextPaneExt logArea;
	public JTextPane 	commandInputField;
	public JScrollPane  scrollPane = null;
    public JPanel       centerPanel;
    public JPanel       bottomPanel;
    public JPanel       topPanel;
    private String      consoleLabel;

	private UndoManager      um = new UndoManager();

    private Integer actionsLock = new Integer(0);
    private Vector<RConsoleLogUnit>  logActions = new Vector<RConsoleLogUnit>();

    private volatile boolean stopLogThread = false;

    public RConsoleBase(RConsoleSubmitInterface sInterface, String textFieldLabel) {
        this.sInterface     = sInterface;
        this.consoleLabel   = textFieldLabel;

        setupUI();
    }

    public RConsoleBase(String textFieldLabel) {
        this.consoleLabel   = textFieldLabel;
        setupUI();
    }

    class LogAppenderSignal {
        private Object monitorObject = new Object();
        private boolean signalled = false;

        public void doWait(){
            synchronized(monitorObject){
                while(!signalled){
                    try{
                        monitorObject.wait();
                    } catch(InterruptedException e){
                    }
                }
                signalled = false;
            }
        }

        public void doNotify(){
            synchronized(monitorObject){
                signalled = true;
                monitorObject.notify();
            }
        }
    }

    private void setupUI() {

        //Color background = new Color(0xfffff0);

        Font commonFont = new Font(FontUtil.getInstance().getCommonMonospaceFontFailyName(),
                Font.PLAIN, FontUtil.NORMAL_FONT_SIZE);

        commandInputField = new JTextPane();
        commandInputField.setFont(commonFont);
        commandInputField.setFocusTraversalKeysEnabled(false);
        //commandInputField.setBackground(background);

        logArea = new JTextPaneExt();
        logArea.setEditable(false);
        logArea.setBorder(null);
        logArea.setFont(commonFont);
        logArea.setLineLimit(8000, 1000);

        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(new EmptyBorder(5,5,5,5));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        // panel layout
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.GRAY));

        topPanel    = new JPanel(new BorderLayout());
        centerPanel = new JPanel(new BorderLayout());
        bottomPanel = new JPanel(new BorderLayout());

        topPanel.setOpaque(false);
        centerPanel.setOpaque(false);
        bottomPanel.setOpaque(false);

        add(topPanel,    BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel label = new JLabel(consoleLabel);
        label.setOpaque(true);
        //label.setBackground(background);
        label.setVerticalAlignment(JLabel.TOP);
        label.setFont(commonFont);

        bottomPanel.add(label, BorderLayout.WEST);
        bottomPanel.add(commandInputField, BorderLayout.CENTER);

        commandInputField.getDocument().addUndoableEditListener(new InputUndoableEditListener());
        commandInputField.addMouseListener(new InputMouseAdapter());

        logArea.addMouseListener(new LogviewMouseAdapter());

        initActions();
        bindKeyActions();

        commandInputField.requestFocus();
        consoleLogThread.start();
    }

    //   H I S T O R Y
    //

    class CommandHistory {

        private int displayIndex = 1;
        private String currentCommand = "";
        public Vector<String> history = new Vector<String>();

        private void updateHistoryIndex(int index) {
            if (index < 0) { index = 0; }
            displayIndex = index + 1;
        }

        public Vector<String> getHistory() {
            return history;
        }

        public void setHistory(Vector<String> history) {
            this.history = new Vector<String>();

            for (String s : history) {
                if (s.length() > 0) {
                    this.history.add(s);
                }
            }
            this.displayIndex = this.history.size();
        }

        public void setCurrentCommand(String currentCommand) {
            this.currentCommand = currentCommand;
        }

        public void add(String command) {
            history.add(command);
            updateHistoryIndex(history.size() - 1);
        }

        public String getPrevCommand(String current) {
            int cmdHistorySize = history.size();

            if (displayIndex == 0 || cmdHistorySize == 0)
                return null;

            if (displayIndex >= cmdHistorySize) {
                 currentCommand = current;
            } else {
                history.set(displayIndex, current);
            }

            --displayIndex;

            return history.elementAt(displayIndex);
        }

        public String getNextCommand(String current) {
            int cmdHistorySize = history.size();

            if (displayIndex >= cmdHistorySize)
                return null;

            history.set(displayIndex, current);

            displayIndex++;

            return (displayIndex == cmdHistorySize) ?
                    currentCommand : history.elementAt(displayIndex);
        }
    }

    private CommandHistory commandHistory = new CommandHistory();


    //   C O N S O L E   L O G G E R
    //


    class ConsoleLogWriter implements Runnable {

        private LogAppenderSignal signal;

        private Vector<RConsoleLogUnit> actions;

        public ConsoleLogWriter(Vector<RConsoleLogUnit> actions, LogAppenderSignal signal) {
            this.actions = actions;
            this.signal = signal;
        }

        public void insertImageContainer(ImageContainer container) {

            RConsoleImageContainer imagePanel = new RConsoleImageContainer(container);

            Document doc = logArea.getDocument();

            int insertAt = doc.getLength();

            try {
                MutableAttributeSet attributes = new SimpleAttributeSet();

                StyleConstants.setComponent(attributes, imagePanel);

                //doc.insertString(insertAt, "\n", null);
                doc.insertString(insertAt, "\n", attributes);

            } catch (BadLocationException ble){
                log.error("Error!", ble);
            }
        }

        public void run() {

            StringBuilder builder = null;

            for (RConsoleLogUnit unit : actions) {

                ImageContainer container = unit.getContainer();
                String cmd = unit.getCmd();
                String log = unit.getLog();

                if (cmd != null) {
                    if (builder == null) {
                        builder = new StringBuilder();
                    }

                    //builder.append("> ");
                    builder.append(cmd);
                    builder.append("\n");
                }

                if (log != null) {
                    if (builder == null) {
                        builder = new StringBuilder();
                    }

                    builder.append(log);
                }

                if (container != null) {
                    if (builder != null) {
                        logArea.append(builder.toString());
                        builder = null;
                    }
                    insertImageContainer(container);
                }
            }

            if (builder != null) {
                logArea.append(builder.toString());
                builder = null;
            }

            signal.doNotify();
        }
    }

    class ConsoleLogRunnable implements Runnable {

        private boolean dirty = false;

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public void printActionsSafe(Vector<RConsoleLogUnit> actions) {
            LogAppenderSignal signal = new LogAppenderSignal();

            EventQueue.invokeLater(new ConsoleLogWriter(actions, signal));

            setDirty(true);

            signal.doWait();
        }

        public void run() {
            while (!stopLogThread) {

                boolean keepProcessingEvents = true;
                while(keepProcessingEvents) {

                    Vector<RConsoleLogUnit> actions = popAllLogActions();

                    if (actions != null && actions.size() > 0) {

                        printActionsSafe(actions);

                    } else {
                        keepProcessingEvents = false;
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            }
        }
    }

    public boolean isConsoleDirty() {
        return consoleLogRunnable.isDirty();
    }

    public void resetConsoleDirty() {
        consoleLogRunnable.setDirty(false);
    }

    private ConsoleLogRunnable consoleLogRunnable = new ConsoleLogRunnable();

    private Thread consoleLogThread = new Thread(consoleLogRunnable);

    //   M O U S E   A D A P T E R S
    //
    //

    class InputMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu popupMenu = initInputPopupMenu();
                popupMenu.show(commandInputField, e.getX(), e.getY());
            }
        }
    }

    class LogviewMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
            if (e.getClickCount() == 1) {
                commandInputField.requestFocus();
            }
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu popupMenu = initLogviewPopupMenu();
                popupMenu.show(logArea, e.getX(), e.getY());
            }
        }
    }

    //   C H A N G E   L I S T E N E R S
    //
    //

    class InputUndoableEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            if (commandInputField.getText().length() > 200 * 10) {
                e.getEdit().undo();
                Toolkit.getDefaultToolkit().beep();
            } else {
                um.addEdit(e.getEdit());
            }
        }
    }

    //   G E T T E R S   &   S E T T E R S
    //
    //
    
    public void setSubmitInterface(RConsoleSubmitInterface sInterface) {
        this.sInterface = sInterface;
    }

    public Vector<String> getCommandHistory() {
        return commandHistory.getHistory();
	}

	public void setCommandHistory(Vector<String> history) {
		if (history != null) {
            commandHistory = new CommandHistory();
            commandHistory.setHistory(history);
        }
	}

    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        logArea.setCursor(cursor);
    }

    public void stopLogThread() {
        stopLogThread = true;
    }

    public JTextPane getCommandInputField() {
        return commandInputField;
    }

    public void pasteToConsoleEditor() {
        commandInputField.requestFocus();
        commandInputField.getActionMap().get(DefaultEditorKit.pasteAction).actionPerformed(null);
    }

    public void clearScreen() {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                logArea.setText("");
            }
        });
	}

    public void print(String cmd, String log, ImageContainer container, SimpleAttributeSet logAttributeSet) {
        synchronized (actionsLock) {
            logActions.add(new RConsoleLogUnit(cmd, log, container, logAttributeSet));
        }
        consoleLogThread.interrupt();
    }

    public void print(String cmd, String log) {
        print(cmd, log, null, null);
    }

    public void print(String log) {
        print(null, log, null, null);
    }

    public void printPrompt(String prompt) {
        print(null, prompt, null, null);
    }

    public void printImage(ImageContainer container) {
        print(null, "\n", container, null);
    }

	public Vector<RConsoleLogUnit> popAllLogActions() {

        Vector<RConsoleLogUnit> result = null;

		synchronized (actionsLock) {
            if (logActions.size() > 0) {
                result = logActions;
                logActions = new Vector<RConsoleLogUnit>();
            }
        }
        return result;

	}

    public void play(final String command, boolean demo) {

        if (!demo) {
            commandInputField.setText(command);
        } else {
            for (int i = 0; i <= command.length(); ++i) {
                commandInputField.setText(command.substring(0, i));
                commandInputField.setCaretPosition(commandInputField.getText().length());
                try {
                    Thread.sleep(30);
                } catch (Exception e) {
                }
            }
        }

        playCommand(command);
    }

    class PlayCommandRunnable implements Runnable {

        private String command;
        public PlayCommandRunnable(String command) {
            this.command = command;
        }

        class PreExecuteRunnable implements Runnable {
            public void run() {
                commandInputField.setEnabled(false);
            }
        }

        class PostExecuteRunnable implements Runnable {
            public void run() {
                commandInputField.setText("");
                commandInputField.setEnabled(true);
                commandInputField.requestFocus();
                um = new UndoManager();
            }
        }

        private PreExecuteRunnable preRunnable = new PreExecuteRunnable();
        private PostExecuteRunnable postRunnable = new PostExecuteRunnable();

        public void run() {
            try {
                EventQueue.invokeAndWait(preRunnable);

                if(!command.equals("")) {
                    commandHistory.add(command);
                }

                String log = sInterface.submit(command);

                if (log != null) {
                    print(command, log);
                }

            } catch (Exception ex) {
                log.error("Error!", ex);
            } finally {
                EventQueue.invokeLater(postRunnable);
            }
        }
    }

    private void playCommand(String command) {
        new Thread(new PlayCommandRunnable(command)).start();
    }

    private int countCR() {
        int result = 0;
        for (int i = 0; i < commandInputField.getText().length(); ++i)
            if (commandInputField.getText().charAt(i) == '\n')
                ++result;
        return result;
    }

    public boolean isInputLocked() {
        return false;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    public Vector<Object> getObjectModel() {

        Vector<Object> model = new Vector<Object>();

        DefaultStyledDocument doc = (DefaultStyledDocument) logArea.getDocument();

        StringBuilder builder = new StringBuilder();

        int length = doc.getLength();

        for (int i=0;i<length;i++) {

            Element el = doc.getCharacterElement(i);

            AttributeSet attributes = el.getAttributes();

            int start = el.getStartOffset();
            int end = el.getEndOffset();

            Component c = (Component) attributes.getAttribute(StyleConstants.ComponentAttribute);

            if (c != null && c instanceof RConsoleImageContainer) {
                try {
                    model.add(builder.toString());
                    builder = new StringBuilder();
                    model.add(((RConsoleImageContainer) c).getContainer().getRawdata());

                } catch (Exception ex) {
                    log.error("Error!", ex);
                }
            } else {
                try {
                    builder.append(doc.getText(start, end - start));
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }
            }

            i = end - 1;
        }

        model.add(builder.toString());

        return model;
    }

    public void setObjectModel(Vector<Object> model) {

        for (Object o : model) {
            if (o instanceof String) {
                this.print(o.toString());
            } else if (o instanceof byte[]) {
                this.print(null, null, new ImageContainer((byte[])o, null), null);
            } else {
                log.error("setObjectModel-unknown object=" + o.toString());
            }
        }
    }

    //   K E Y B O A R D   A C T I O N S
    //
    //

    public void registerAction(JComponent c, String actionname) {

        ConsoleAction action = consoleActions.get(actionname);

        KeyStroke ks = action.getKeystroke();

        if (ks != null) {
            c.getInputMap().put(ks, actionname);
            c.getActionMap().put(actionname, action);
        }
    }

    public void registerInputAction(String actionname) {
        registerAction(commandInputField, actionname);
    }

    public void registerLogviewAction(String actionname) {
        registerAction(logArea, actionname);
    }

    public void bindKeyActions() {

        registerInputAction(INPUTCOPY);
        registerInputAction(INPUTCUT);
        registerInputAction(INPUTPASTE);
        registerInputAction(INSERTLINE);
        registerInputAction(NEXTCOMMANDC);
        registerInputAction(PREVCOMMANDC);
        registerInputAction(NEXTCOMMAND);
        registerInputAction(PREVCOMMAND);
        registerInputAction(UNDO);
        registerInputAction(REDO);
        registerInputAction(BEGINLINE);
        registerInputAction(ENDLINE);
        registerInputAction(CUTREST);
        registerInputAction(CUTALL);
        registerInputAction(ENTER);

        registerLogviewAction(LOGVIEWCLEAR);
        registerLogviewAction(LOGVIEWCOPY);
        registerLogviewAction(COPYHISTORY);
        registerLogviewAction(PASTEHISTORY);
        registerLogviewAction(CLEARHISTORY);
        registerLogviewAction(EXPORT2PDF);

        String CONSOLE_KEYMAP = "simple-console-keymap";
        Keymap binding = logArea.addKeymap(CONSOLE_KEYMAP, logArea.getKeymap());
        binding.setDefaultAction(new DefaultLogviewAction(commandInputField));
        logArea.setKeymap(binding);
    }

    public boolean addMenuItem(JComponent menu, String actionname) {

        JMenuItem item = menuItemsMap.get(actionname);

        if (item == null) {
            throw new RuntimeException("Menu item not defined " + actionname);
        }

        if (item.getAction().isEnabled()) {
            item.setEnabled(true);
            menu.add(item);
            return true;
        }

        return false;
    }

    public static final String HISTORYMENU = "historymenu";

    public JPopupMenu initLogviewPopupMenu() {

        JPopupMenu menu = new JPopupMenu();

        boolean separator = false;

        separator = addMenuItem(menu, LOGVIEWCOPY) || separator;
        separator = addMenuItem(menu, LOGVIEWCLEAR) || separator;
        separator = addMenuItem(menu, EXPORT2PDF) || separator;
        if (separator) {
            menu.addSeparator();
        }

        JMenu historymenu = new JMenu("Command History");

        addMenuItem(historymenu, COPYHISTORY);
        addMenuItem(historymenu, PASTEHISTORY);
        addMenuItem(historymenu, CLEARHISTORY);

        menu.add(historymenu);
        menu.putClientProperty(HISTORYMENU, historymenu);

        return menu;
    }

    public JPopupMenu initInputPopupMenu() {

        JPopupMenu menu = new JPopupMenu();

        addMenuItem(menu, INSERTLINE);
        menu.addSeparator();

        addMenuItem(menu, INPUTCOPY);
        addMenuItem(menu, INPUTCUT);
        addMenuItem(menu, INPUTPASTE);

        menu.addSeparator();
        addMenuItem(menu, NEXTCOMMAND);
        addMenuItem(menu, PREVCOMMAND);
        addMenuItem(menu, UNDO);
        addMenuItem(menu, REDO);

        return menu;
    }

    private HashMap<String, JMenuItem> menuItemsMap = new HashMap<String, JMenuItem>();
    private HashMap<String, ConsoleAction> consoleActions = new HashMap<String, ConsoleAction>();

    private void createMenuItem(String actionname, ConsoleAction action, String iconPath) {

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

        menuItemsMap.put(actionname, menuItem);
    }


    public void initAction(String name, ConsoleAction action, String iconPath) {
        consoleActions.put(name, action);
        createMenuItem(name, action, iconPath);
    }

    private void initActions() {

        // log view

        initAction(LOGVIEWCOPY, new LogviewCopyAction("Copy",
                KeyUtil.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK)),
                "/images/console/edit-copy.png");

        initAction(LOGVIEWCLEAR, new LogviewClearAction("Clear Screen",
                null), "/images/console/edit-clear.png");

        initAction(EXPORT2PDF, new ExportToPdfAction("Save as PDF",
                null), "/views/images/rconsole/page_white_acrobat.png");

        initAction(COPYHISTORY, new CopyHistoryAction("Copy History",
                null), null);

        initAction(PASTEHISTORY, new PasteHistoryAction("Paste History",
                null), null);

        initAction(CLEARHISTORY, new ClearHistoryAction("Clear History",
                null), null);

        // input

        initAction(INPUTCOPY, new InputCopyAction("Copy",
                KeyUtil.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK)),
                "/images/console/edit-copy.png");

        initAction(INPUTCUT, new InputCutAction("Cut",
                KeyUtil.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK)),
                "/images/console/edit-cut.png");

        initAction(INPUTPASTE, new InputPasteAction("Paste",
                KeyUtil.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK)),
                "/images/console/edit-paste.png");

        initAction(INSERTLINE, new InsertBlankLine("Insert Blank Line",
                KeyUtil.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK)),
                "/images/console/list-add.png");

        initAction(UNDO, new UndoAction("Undo",
                KeyUtil.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK)),
                "/images/console/edit-undo.png");

        initAction(REDO, new RedoAction("Redo",
                KeyUtil.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK + KeyEvent.SHIFT_MASK)),
                "/images/console/edit-redo.png");

        initAction(NEXTCOMMANDC, new NextCommandAction("Next Command keyboard",
                KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0), false),
                null);

        initAction(PREVCOMMANDC, new PrevCommandAction("Prev Command keyboard",
                KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0), false),
                null);

        initAction(NEXTCOMMAND, new NextCommandAction("Next Command",
                KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.META_MASK), true),
                null);

        initAction(PREVCOMMAND, new PrevCommandAction("Prev Command",
                KeyUtil.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_MASK), true),
                null);

        initAction(BEGINLINE, new BeginLineAction("Begin Line",
                KeyUtil.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK)),
                null);

        initAction(ENDLINE, new EndLineAction("End Line",
                KeyUtil.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK)),
                null);

        initAction(CUTREST, new CutRestAction("Cut Rest",
                KeyUtil.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK)),
                null);

        initAction(CUTALL, new CutAllAction("Cut All",
                KeyUtil.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK)),
                null);

        initAction(ENTER, new EnterAction("Enter",
                KeyUtil.getKeyStroke(KeyEvent.VK_ENTER, 0)),
                null);
    }

    //   A C T I O N S
    //
    //

    private static String LOGVIEWCLEAR = "logviewclear";
    private static String LOGVIEWCOPY = "logviewcopy";
    private static String COPYHISTORY = "copyhistory";
    private static String PASTEHISTORY = "pastehistory";
    private static String CLEARHISTORY = "clearhistory";

    private static String INPUTCOPY = "inputcopy";
    private static String INPUTCUT = "inputcut";
    private static String INPUTPASTE = "inputpaste";
    private static String INSERTLINE = "insertline";
    private static String NEXTCOMMAND = "nextcommand";
    private static String PREVCOMMAND = "prevcommand";
    private static String NEXTCOMMANDC = "nextcommandc";
    private static String PREVCOMMANDC = "prevcommandc";
    private static String UNDO = "undo";
    private static String REDO = "redo";

    private static String BEGINLINE = "beginline";
    private static String ENDLINE = "endline";
    private static String CUTREST = "cutrest";
    private static String CUTALL = "cutall";
    private static String ENTER = "enter";

    private static String EXPORT2PDF = "toPDF";


    public abstract class ConsoleAction extends AbstractAction {
        private KeyStroke keystroke = null;

        public ConsoleAction(String name, KeyStroke keystroke){
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

    //   L O G   V I E W
    //
    //

    class LogviewCopyAction extends ConsoleAction {
        Action delegate = logArea.getActionMap().get(DefaultEditorKit.copyAction);
        public LogviewCopyAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }

        public boolean isEnabled() {
            Caret caret = logArea.getCaret();
            return caret.getMark() != caret.getDot();
        }
    }

    class LogviewClearAction extends ConsoleAction {
        public LogviewClearAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            logArea.setText("");
        }

        public boolean isEnabled() {
            return logArea.getText().length() > 0;
        }
    }

    class CopyHistoryAction extends ConsoleAction {
        public CopyHistoryAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            StringBuffer sb = new StringBuffer();

            Vector<String> history = commandHistory.getHistory();
            for (int i = 0; i < history.size(); ++i) {
                sb.append(history.elementAt(i));
                sb.append("\n");
            }

            StringSelection stringSelection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, RConsoleBase.this);
        }
    }

    class PasteHistoryAction extends ConsoleAction {
        public PasteHistoryAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            try {
                BufferedReader in = new BufferedReader(new StringReader((String)
                        Toolkit.getDefaultToolkit().getSystemClipboard().getContents(
                        RConsoleBase.this).getTransferData(DataFlavor.stringFlavor)));

                String line;
                while ((line = in.readLine()) != null) {
                    commandHistory.add(line);
                }
            } catch (Exception ex) {
                log.error("Error!", ex);
            }

        }
    }

    class ClearHistoryAction extends ConsoleAction {
        public ClearHistoryAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            commandHistory = new CommandHistory();
        }
    }

    class DefaultLogviewAction extends AbstractAction {
        private  JTextPane commandpane;
        private Action commandpaneAction;

        public DefaultLogviewAction(JTextPane commandpane) {
            super("default");
            this.commandpane = commandpane;
            this.commandpaneAction = commandpane.getKeymap().getDefaultAction();
        }

        public void actionPerformed(ActionEvent event) {
            if (!commandpane.hasFocus()) {
                commandpane.requestFocus();
            }

            ActionEvent newevent = new ActionEvent(commandpane,
                    event.getID(),
                    event.getActionCommand(),
                    event.getWhen(),
                    event.getModifiers());

            commandpaneAction.actionPerformed(newevent);
        }
    }

    //   I N P U T
    //
    //

    class InputCopyAction extends ConsoleAction {
        Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.copyAction);
        public InputCopyAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }

        public boolean isEnabled() {
            Caret caret = commandInputField.getCaret();
            return caret.getMark() != caret.getDot();
        }
    }


    class InputCutAction extends ConsoleAction {
        Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.cutAction);
        public InputCutAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }

        public boolean isEnabled() {
            Caret caret = commandInputField.getCaret();
            return caret.getMark() != caret.getDot();
        }
    }

    class InputPasteAction extends ConsoleAction {
        Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.pasteAction);
        public InputPasteAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }

        public boolean isEnabled() {
            return delegate.isEnabled();
        }
    }

    class UndoAction extends ConsoleAction {
        public UndoAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            if (um.canUndo()) {
                um.undo();
            }
        }

        public boolean isEnabled() {
            return um.canUndo();
        }
    }

    class RedoAction extends ConsoleAction {
        public RedoAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            if (um.canRedo()) {
                um.redo();
            }
        }

        public boolean isEnabled() {
            return um.canRedo();
        }
    }

    class InsertBlankLine extends ConsoleAction {
        public InsertBlankLine(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            String text 		= commandInputField.getText();
            int caretPosition 	= commandInputField.getCaretPosition();
            commandInputField.setText(text.substring(0, caretPosition) +
                    "\n" + text.substring(caretPosition));
            commandInputField.setCaretPosition(caretPosition + 1);
        }
    }

    class NextCommandAction extends ConsoleAction {
        private boolean force = false;
        private Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.downAction);
        public NextCommandAction(String name, KeyStroke keystroke, boolean force) {
            super(name, keystroke);
            this.force = force;
        }

        public void actionPerformed(ActionEvent event) {
            if (countCR() == 0 || this.force) {
                String cmd = commandHistory.getNextCommand(commandInputField.getText());
                if (cmd != null) {
                    commandInputField.setText(cmd);
                    commandInputField.setCaretPosition(cmd.length());
                }
            } else {
                delegate.actionPerformed(event);
            }
        }
    }

    class PrevCommandAction extends ConsoleAction {
        private boolean force = false;
        private Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.upAction);
        public PrevCommandAction(String name, KeyStroke keystroke, boolean force) {
            super(name, keystroke);
            this.force = force;
        }

        public void actionPerformed(ActionEvent event) {
            if (countCR() == 0 || force) {
                String cmd = commandHistory.getPrevCommand(commandInputField.getText());
                if (cmd != null) {
                    commandInputField.setText(cmd);
                    commandInputField.setCaretPosition(cmd.length());
                }
            } else {
                delegate.actionPerformed(event);
            }
        }
    }

    class BeginLineAction extends ConsoleAction {
        Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.beginLineAction);
        public BeginLineAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }
    }

    class EndLineAction extends ConsoleAction {
        Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.endLineAction);
        public EndLineAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }
    }


    class CutRestAction extends ConsoleAction {
        public CutRestAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            int pos = commandInputField.getCaretPosition();
            commandInputField.setText(commandInputField.getText().substring(0, pos));
        }
    }

    class CutAllAction extends ConsoleAction {
        Action delegate = commandInputField.getActionMap().get(DefaultEditorKit.cutAction);
        public CutAllAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(final ActionEvent event) {
            commandInputField.select(0, commandInputField.getText().length());
            delegate.actionPerformed(event);
        }
    }

    class EnterAction extends ConsoleAction {
        public EnterAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            playCommand(commandInputField.getText());
            commandHistory.setCurrentCommand("");
        }
    }

    class ExportToPdfAction extends ConsoleAction {
        private String familyName = "Times New Roman";

        private com.itextpdf.text.Font catFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.getFamily(familyName),
                        18, com.itextpdf.text.Font.BOLD);

        private com.itextpdf.text.Font redFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.getFamily(familyName),
                        12, com.itextpdf.text.Font.NORMAL, BaseColor.RED);

        private com.itextpdf.text.Font subFont =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.getFamily(familyName),
                        16, com.itextpdf.text.Font.BOLD);

        private com.itextpdf.text.Font smallBold =
                new com.itextpdf.text.Font(com.itextpdf.text.Font.getFamily(familyName),
                        12, com.itextpdf.text.Font.BOLD);

        private JFileChooser chooser = null;

        public ExportToPdfAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        // iText allows to add metadata to the PDF which can be viewed in your Adobe
        // Reader
        // under File -> Properties
        private void addMetaData(com.itextpdf.text.Document document) {
            document.addTitle("Console Output");
            document.addSubject("Console Output");
            document.addKeywords("R, Cloud, RCloud, R-Cloud, Java, PDF, Console, Output");
            document.addAuthor("user");  // FIXME
            document.addCreator("user"); // FIXME
        }


        private void addTitlePage(com.itextpdf.text.Document document)
                throws DocumentException {
            Paragraph preface = new Paragraph();
            // We add one empty line
            addEmptyLine(preface, 1);
            // Lets write a big header
            preface.add(new Paragraph("Workbench console report", catFont));

            addEmptyLine(preface, 1);
            // Will create: Report generated by: _name, _date
            preface.add(new Paragraph("Generated by: " +
                    System.getProperty("user.name") + ", " + new Date(), smallBold));

            DefaultStyledDocument doc = (DefaultStyledDocument) logArea.getDocument();

            StringBuilder builder = new StringBuilder();

            int length = doc.getLength();

            for (int i=0;i<length;i++) {

                Element el = doc.getCharacterElement(i);

                AttributeSet attributes = el.getAttributes();

                Component c = (Component) attributes.getAttribute(StyleConstants.ComponentAttribute);

                if (c != null && c instanceof RConsoleImageContainer) {
                    try {
                        preface.add(new Paragraph(builder.toString()));

                        builder = new StringBuilder();

                        com.itextpdf.text.Image image = com.itextpdf.text.Image.
                                    getInstance(((RConsoleImageContainer) c).getContainer().getImage(), null, false);

                        preface.add(image);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                int start = el.getStartOffset();
                int end = el.getEndOffset();

                String text;

                try {
                    text = doc.getText(start, end - start);
                } catch (BadLocationException ble) {
                    text = "BadLocationException";
                }

                builder.append(text);

                i = end - 1;
            }

            preface.add(new Paragraph(builder.toString()));

            document.add(preface);

        }


        private void addEmptyLine(Paragraph paragraph, int number) {
            for (int i = 0; i < number; i++) {
                paragraph.add(new Paragraph(" "));
            }
        }

        public void exportConsoleToPdf(File file) {

            try {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                addMetaData(document);
                addTitlePage(document);
                //addContent(document);
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void actionPerformed(final ActionEvent e) {

            if (chooser == null) {
                chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(true);
                chooser.setFileHidingEnabled(true);

                FileFilter filter = new FileFilter() {
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        } else {
                            String name = f.getName().toLowerCase();

                            boolean accept = name.endsWith("pdf");

                            //log.info("filter-accept-accept="+accept);

                            return accept;
                        }
                    }

                    public String getDescription() {
                        return "PDF files";
                    }
                };

                chooser.setFileFilter(filter);
            }

            new Thread(new Runnable() {
                public void run() {
                    chooser.setSelectedFile(new File("output.pdf"));

                    int returnVal = chooser.showSaveDialog(RConsoleBase.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File selectedfile = chooser.getSelectedFile();

                        String name = selectedfile.getName();

                        if (name.endsWith("pdf")) {
                            exportConsoleToPdf(selectedfile);
                        }
                    }

                }
            }).start();

        }

    }

    //   T E S T   G U I
    //
    //

    static void setupGUI() {
        JFrame frame = new JFrame();

        RConsoleSubmitInterface iterface = new RConsoleSubmitInterface() {
            public String submit(String expression) {
                log.info("RConsoleSubmitInterface-submit-expression=" + expression);
                return "executed\n";
            }
        };

        RConsoleBase console = new RConsoleBase(iterface, "> ");

        frame.add(console);
        frame.setPreferredSize(new Dimension(500,500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                setupGUI();
            }
        });
    }

}


