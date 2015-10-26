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
package workbench.views.extraconsole;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JTextPaneExt;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.generic.ImageContainer;
import workbench.generic.RConsole;
import workbench.views.rconsole.RConsoleImageContainer;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 28, 2009
 * Time: 2:57:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsolePanelBase extends JTextPaneExt implements RConsole {
    final private Logger log = LoggerFactory.getLogger(getClass());

    public static final String INPUTSTART = "INPUTSTART";

    public String consolePrompt = "#";

    //public static final Object Input = new Object();
    //public static final Object Actions = new Object();
    //private static final Cursor MoveCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    //private static final Cursor DefaultCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

    private EventListenerList listenerList;
    private DocumentHandler documentHandler;
    private ConsoleHistoryText history;

    public ConsolePanelBase(){

        history = new ConsoleHistoryText(this);

        listenerList = new EventListenerList();

        initActions();

        addMouseListener(new ConsoleMouseAdapter());

        documentHandler = new DocumentHandler();

        setDocument(getDocument());
        
        setInputStart(0);
    }

    public void setDocument(Document doc){
        if(documentHandler != null && getDocument() != null)
            getDocument().removeDocumentListener(documentHandler);

        super.setDocument(doc);
        doc.addDocumentListener(documentHandler);
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class,l);
    }

    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class,l);
    }

    public void fireActionEvent(String code) {
        lockConsole();

        ActionEvent evt = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED,code);

        Object[] listeners = listenerList.getListenerList();
        for(int i = 0; i < listeners.length; i++) {
            if(listeners[i] == ActionListener.class) {
                ActionListener l = (ActionListener)
                    listeners[i+1];
                l.actionPerformed(evt);
            }
        }
    }

    public String getInput() {
        try {
            Document doc = getDocument();
            int cmdStart = getInputStart();
            String line = doc.getText(cmdStart,doc.getLength() - cmdStart);
            if(line.endsWith("\n"))
                return line.substring(0,line.length() - 1);
            else
                return line;
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInput(String line) {
        try {
            Document doc = getDocument();
            int cmdStart = getInputStart();
            doc.remove(cmdStart,doc.getLength() - cmdStart);
            doc.insertString(cmdStart,line,null);
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public int getInputStart() {
        return (Integer) getDocument().getProperty(INPUTSTART);
    }

    public void setInputStart(int cmdStart) {
        getDocument().putProperty(INPUTSTART, cmdStart);
    }

    public String getInputBeforeCaret() {
        try {
            return getDocument().getText(getInputStart(),
                getCaretPosition() - getInputStart());
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void printPrompt() {

        //log.error("printPrompt");

        //writeAttrs(ConsolePanelBase.colorAttributes(Color.RED.darker().darker()), consolePrompt);
        writeAttrs(ConsolePanelBase.colorAttributes(Color.BLACK), consolePrompt);
        //writeAttrs(ConsolePanelBase.colorAttributes(Color.BLACK), " ");

        unlockConsole();
    }

    public void printPrompt(String prompt) {
        setPrompt(prompt);
        printPrompt();
    }

    public void print(String msg) {
        writeAttrs(ConsolePanelBase.colorAttributes(Color.BLACK),
            msg);
    }

    public void print(Color color, String msg) {
        writeAttrs(ConsolePanelBase.colorAttributes(color),
            msg);
    }


    public void printImage(ImageContainer container) {

        //log.error("printImage");

        final RConsoleImageContainer imagePanel = new RConsoleImageContainer(container);

        EventQueue.invokeLater(new Runnable(){
            public void run(){

                //log.error("printImage-run");

                Document doc = getDocument();

                int insertAt = isConsoleLocked() ?
                        doc.getLength() : getInputStart() - getPrompt().length() - 1;

                try {

                    MutableAttributeSet attributes = new SimpleAttributeSet();

                    StyleConstants.setComponent(attributes, imagePanel);

                    //insertComponent(imagePanel);
                    doc.insertString(insertAt, "\n", null);
                    doc.insertString(insertAt + 1, "\n", attributes);

                    //doc.insertAt
                    //doc.insertString(insertAt, "\n", null);
                } catch (BadLocationException ble){
                    log.error("Error!", ble);
                }

                int len = doc.getLength();

                setCaretPosition(len);
                setInputStart(len);

                /*
                */
            }
        });
    }

    public void lockConsole() {
        if(SwingUtilities.isEventDispatchThread()) {
            setEditable(false);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setEditable(false);
                }
            });
        }
    }

    public void unlockConsole() {
        if(SwingUtilities.isEventDispatchThread()) {
            setEditable(true);
            setCaretPosition(getDocument().getLength());
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setEditable(true);
                    setCaretPosition(getDocument().getLength());
                }
            });
        }
    }

    public boolean isConsoleLocked() {
        return !isEditable();
    }

    public void writeAttrs(final AttributeSet attrs, final String msg) {
        if(SwingUtilities.isEventDispatchThread())
            writeSafely(attrs,msg);
        else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    writeSafely(attrs,msg);
                }
            });
        }
    }

    public void writeAttrsBeforeInput(final AttributeSet attrs, final String msg) {
        if(SwingUtilities.isEventDispatchThread())
            writeSafelyBeforeInput(attrs,msg);
        else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    writeSafelyBeforeInput(attrs,msg);
                }
            });
        }
    }

    private void writeSafely(AttributeSet attrs, String msg) {

        //log.error("writeSafely");

        Document scrollback = getDocument();
        try {
            if(attrs != null && StyleConstants.getIcon(attrs) != null)
                msg = " ";
            scrollback.insertString(scrollback.getLength(),
                msg,attrs);
        } catch(BadLocationException bl) {
            throw new RuntimeException(bl);
        }

        setInputStart(scrollback.getLength());
    }

    private void writeSafelyBeforeInput(AttributeSet attrs, String msg) {
        Document scrollback = getDocument();
        try {
            if(attrs != null && StyleConstants.getIcon(attrs) != null)
                msg = " ";
            scrollback.insertString(getInputStart() - getPrompt().length(),
                msg,attrs);
        } catch(BadLocationException bl) {
            throw new RuntimeException(bl);
        }
    }

    public void setPrompt(String prompt) {
        consolePrompt = prompt;
    }

    public String getPrompt() {
        return consolePrompt;
    }

    public static AttributeSet colorAttributes(Color color) {
        SimpleAttributeSet style = new SimpleAttributeSet();

        if(color != null)
            style.addAttribute(StyleConstants.Foreground,color);

        return style;
    }

    class DocumentHandler implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {

            int offset = e.getOffset();
            int length = e.getLength();

            int cmdStart = getInputStart();
            if(offset < cmdStart)
                cmdStart += length;
            setInputStart(cmdStart);

        }

        public void removeUpdate(DocumentEvent e) {
            int offset = e.getOffset();
            int length = e.getLength();

            int cmdStart = getInputStart();
            if(offset < cmdStart) {
                if(offset + length > cmdStart)
                    cmdStart = offset;
                else
                    cmdStart -= length;
            }
            setInputStart(cmdStart);
        }

        public void changedUpdate(DocumentEvent e) {}
    }

    //   P O P U P   M E N U   S U P P O R T
    //

    class ConsoleMouseAdapter extends MouseAdapter {
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
                JPopupMenu popupMenu = initConsolePopupMenu();
                popupMenu.show(ConsolePanelBase.this, e.getX(), e.getY());
            }
        }
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

    public JPopupMenu initConsolePopupMenu() {

        JPopupMenu menu = new JPopupMenu();

        addMenuItem(menu, COPY);
        addMenuItem(menu, CUT);
        addMenuItem(menu, PASTE);
        addMenuItem(menu, EXPORT2PDF);
        addMenuItem(menu, CLEAR);
        menu.addSeparator();

        addMenuItem(menu, NEXT);
        addMenuItem(menu, PREV);
        addMenuItem(menu, SEARCH);

        return menu;
    }

    //   A C T I O N   R O U T I N E S
    //

    private static String COPY = "console-copy";
    private static String CUT = "console-cut";
    private static String PASTE = "console-paste";
    private static String CLEAR = "console-clear";
    private static String ENTER = "console-enter";
    private static String BACKSPACE = "console-backspace";
    private static String DELETE = "console-delete";
    private static String BEGINLINE = "console-home";
    private static String BEGINLINESELECT = "console-home-select";
    private static String NEXT = "next-command";
    private static String PREV = "prev-command";
    private static String SEARCHNEXT = "search-next-command";
    private static String SEARCHPREV = "search-prev-command";
    private static String SEARCH = "search-command";
    private static String BACKWARD = "console-backward";
    private static String BACKWARDSELECT = "console-backward-select";
    private static String EXPORT2PDF = "export-to-pdf";

    public void initActions() {

        initAction(COPY, new CopyAction("Copy",
                KeyUtil.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK)),
                "/views/images/extraconsole/edit-copy.png");

        initAction(CUT, new CutAction("Cut",
                KeyUtil.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK)),
                "/views/images/extraconsole/edit-cut.png");

        initAction(PASTE, new PasteAction("Paste",
                KeyUtil.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK)),
                "/views/images/extraconsole/edit-paste.png");

        initAction(CLEAR, new ClearScreenAction("Clear Screen",
                null),
                "/views/images/extraconsole/edit-clear.png");

        initAction(ENTER, new EnterAction("Enter",
                KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0)),
                null);

        initAction(BACKSPACE, new BackspaceAction("Backspace",
                KeyUtil.getKeyStroke(KeyEvent.VK_BACK_SPACE,0)),
                null);

        initAction(DELETE, new DeleteAction("Delete",
                KeyUtil.getKeyStroke(KeyEvent.VK_DELETE,0)),
                null);

        initAction(BEGINLINE, new BeginLineAction("Begin Line",
                KeyUtil.getKeyStroke(KeyEvent.VK_HOME, 0), false),
                null);

        initAction(BEGINLINESELECT, new BeginLineAction("Begin Line & Select",
                KeyUtil.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK), true),
                null);

        initAction(NEXT, new NextCommandAction("Next Command",
                KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0)),
                null);

        initAction(PREV, new PrevCommandAction("Prev Command",
                KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0)),
                null);

        initAction(SEARCHNEXT, new SearchNextAction("Search Next",
                KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_MASK)),
                null);

        initAction(SEARCHPREV, new SearchPrevAction("Search Prev",
                KeyUtil.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_MASK)),
                null);

        initAction(SEARCH, new SearchPrevAction("Search Command",
                KeyUtil.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK)),
                null);

        initAction(BACKWARD, new CaretBackwardAction("Backward",
                KeyUtil.getKeyStroke(KeyEvent.VK_LEFT, 0)),
                null);

        initAction(BACKWARDSELECT, new CaretBackwardSelectAction("Backward Select",
                KeyUtil.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK)),
                null);

        initAction(EXPORT2PDF, new ExportToPdfAction("Save As PDF",
                null), null);

        String CONSOLE_KEYMAP = "extra-console-keymap";
        Keymap binding = addKeymap(CONSOLE_KEYMAP, getKeymap());
        binding.setDefaultAction(new NewDefaultKeyTypedAction("Default", null));
        setKeymap(binding);

    }

    private void createMenuItem(String actionname, String iconPath) {

        ConsoleAction action = consoleActions.get(actionname);

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

    public void registerKeyAction(String actionname) {
        ConsoleAction action = consoleActions.get(actionname);
        KeyStroke ks = action.getKeystroke();

        if (ks != null) {
            getInputMap().put(ks, actionname);
            getActionMap().put(actionname, action);
        }
    }

    public void initAction(String name, ConsoleAction action, String iconPath) {

        consoleActions.put(name, action);

        createMenuItem(name, iconPath);

        registerKeyAction(name);
    }


    //   A C T I O N S
    //
    //

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

    class ClearScreenAction extends ConsoleAction {
        public ClearScreenAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            Document doc = getDocument();

            try {
                getDocument().remove(0, doc.getLength());
            } catch (BadLocationException ble) {
            }

            printPrompt();
        }

        public boolean isEnabled() {
            return getDocument().getLength() > 0;
        }
    }

    class BackspaceAction extends ConsoleAction {
        private Action delegate = getActionMap().get(DefaultEditorKit.deletePrevCharAction);

        public BackspaceAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            int inputStart = getInputStart();

            if(getCaretPosition() > inputStart &&
               getSelectionStart() > inputStart) {
                delegate.actionPerformed(evt);
            }
        }
    }

    class DeleteAction extends ConsoleAction {
        private Action delegate = getActionMap().get(DefaultEditorKit.deleteNextCharAction);

        public DeleteAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            int inputStart = getInputStart();

            if(getCaretPosition() >= inputStart &&
               getSelectionStart() >= inputStart) {
                delegate.actionPerformed(evt);
            }
        }
    }

    class EnterAction extends ConsoleAction {
        public EnterAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            setCaretPosition(getDocument().getLength());
            replaceSelection("\n");

            String input = getInput();

            if (input.length() > 0) {
                history.addCurrentToHistory();
            }
            history.setIndex(-1);

            fireActionEvent(getInput());
        }
    }

    class NewDefaultKeyTypedAction extends ConsoleAction {
        private Action delegate = getKeymap().getDefaultAction();

        public NewDefaultKeyTypedAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            int inputStart = getInputStart();

            if(getCaretPosition() >= inputStart &&
               getSelectionStart() >= inputStart) {
                delegate.actionPerformed(evt);
            }
        }
    }

    class CaretBackwardAction extends ConsoleAction {
        private Action delegate = getActionMap().get(DefaultEditorKit.backwardAction);

        public CaretBackwardAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            int inputStart = getInputStart();

            if(getCaretPosition() != inputStart) {
                delegate.actionPerformed(evt);
            }
        }
    }

    class CaretBackwardSelectAction extends ConsoleAction {
        private Action delegate = getActionMap().get(DefaultEditorKit.selectionBackwardAction);

        public CaretBackwardSelectAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            int inputStart = getInputStart();

            if(getCaretPosition() != inputStart) {
                delegate.actionPerformed(evt);
            }
        }
    }

    class BeginLineAction extends ConsoleAction {
        private boolean select;

        public BeginLineAction(String name, KeyStroke keystroke, boolean select) {
            super(name, keystroke);
            this.select = select;
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent target = ConsolePanelBase.this;
            if (target != null) {
                try {
                    int offs = target.getCaretPosition();
                    int begOffs = Utilities.getRowStart(target, offs);
                    int input = getInputStart(); 

                    if (begOffs < input) {
                        begOffs = input;
                    }

                    if (select) {
                        target.moveCaretPosition(begOffs);
                    } else {
                        target.setCaretPosition(begOffs);
                    }
                } catch (BadLocationException bl) {
                    //UIManager.getLookAndFeel().provideErrorFeedback(
                    //        target);
                }
            }
        }
    }

    class PrevCommandAction extends ConsoleAction {
        private Action delegate = getActionMap().get(DefaultEditorKit.upAction);

        PrevCommandAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            if(getCaretPosition() >= getInputStart()) {
                history.historyPrevious();
            } else {
                delegate.actionPerformed(evt);
            }
        }
    }

    class NextCommandAction extends ConsoleAction {
        private Action delegate = getActionMap().get(DefaultEditorKit.downAction);

        NextCommandAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            if(getCaretPosition() >= getInputStart()) {
                history.historyNext();
            } else {
                delegate.actionPerformed(evt);
            }
        }
    }

    class SearchPrevAction extends ConsoleAction {
        public SearchPrevAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            history.doBackwardSearch();
        }
    }

    class SearchNextAction extends ConsoleAction {
        public SearchNextAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent evt) {
            history.doForwardSearch();
        }
    }

    class DummyAction extends ConsoleAction {
        public DummyAction(String name, KeyStroke keystroke){
            super(name, keystroke);
        }
        public void actionPerformed(ActionEvent evt) {
        }
    }

    class CopyAction extends ConsoleAction {
        Action delegate = getActionMap().get(DefaultEditorKit.copyAction);
        public CopyAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            delegate.actionPerformed(event);
        }

        public boolean isEnabled() {
            Caret caret = getCaret();
            return caret.getMark() != caret.getDot();
        }
    }


    class CutAction extends ConsoleAction {
        Action copydelegate = getActionMap().get(DefaultEditorKit.copyAction);
        Action cutdelegate = getActionMap().get(DefaultEditorKit.cutAction);
        public CutAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            Caret caret = getCaret();

            int start = Math.min(caret.getDot(), caret.getMark());

            if (start >= getInputStart()) {
                cutdelegate.actionPerformed(event);
            } else {
                copydelegate.actionPerformed(event);
            }
        }

        public boolean isEnabled() {
            Caret caret = getCaret();
            return caret.getMark() != caret.getDot();
        }
    }

    class PasteAction extends ConsoleAction {
        Action delegate = getActionMap().get(DefaultEditorKit.pasteAction);
        public PasteAction(String name, KeyStroke keystroke) {
            super(name, keystroke);
        }

        public void actionPerformed(ActionEvent event) {
            Caret caret = getCaret();

            int start = Math.min(caret.getDot(), caret.getMark());

            if (start >= getInputStart()) {
                delegate.actionPerformed(event);
            }
        }

        public boolean isEnabled() {
            return delegate.isEnabled();
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

            DefaultStyledDocument doc = (DefaultStyledDocument) getDocument();

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

                    int returnVal = chooser.showSaveDialog(ConsolePanelBase.this);

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


    private HashMap<String, JMenuItem> menuItemsMap = new HashMap<String, JMenuItem>();
    private HashMap<String, ConsoleAction> consoleActions = new HashMap<String, ConsoleAction>();

    //   T E S T   G U I
    //

    static class TestConsoleThread extends Thread {
        ConsolePanelBase console;
        public TestConsoleThread(ConsolePanelBase console) {
            this.console = console;
        }
        public void run(){
            int i = 10;
            while(i-- > 0) {
                console.print("tread cnt="+i);
                try { Thread.sleep(1000); } catch (Exception ex) {}
            }
            console.printPrompt();
        }
    }

    static void setupGUI() {
        JFrame frame = new JFrame();

        final ConsolePanelBase console = new ConsolePanelBase();

        console.printPrompt();

        ActionListener listener = new ActionListener(){
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().equals("start")) {
                    console.print("started");

                    new TestConsoleThread(console).start();
                } else {
                    console.print("processing command "+actionEvent.getActionCommand()+"\nresult.. OK\n");
                    console.printPrompt();
                }
            }
        };

        console.addActionListener(listener);

        frame.add(new JScrollPane(console));
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
