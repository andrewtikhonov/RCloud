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
package workbench.completion;

import workbench.views.extraconsole.ConsolePanelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.util.KeyUtil;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 12, 2009
 * Time: 4:40:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompletionSupport {
    // pop up components
    final private Logger log = LoggerFactory.getLogger(getClass());

    private boolean		list_visible    = false;
    private boolean 	list_activated  = false;

    private CompletionVisualListModel model       = null;
    private CompletionVisualList list             = null;
    private CompletionVisualListRenderer renderer = null;
    private JScrollPane listScroll		= null;
    private JPanel 		listPanel		= null;
    private Popup 		listPopup   	= null;

    private Runnable 	showListPopup		= null;
    private Runnable 	updateListPopup		= null;
    private Runnable 	listProcessorAction = null;
    private Runnable 	closeListPopup		= null;

    public  final static int GET_DATA_ON_ACTIVATION		= 0;
    public  final static int GET_DATA_ON_EACH_KEYPRESS	= 1;
    public  final static int DATAMINER_DEFAULT_MODE		= GET_DATA_ON_ACTIVATION;

    public  final static int SELECT_FROM_TOP    		= 0;
    public  final static int SELECT_FROM_BOTTOM     	= 1;
    public  final static int DEFAULT_SELECTION_MODE		= SELECT_FROM_TOP;

    private Dimension defaultPreferredSize = new Dimension(300, 176);
    private Container rootContainer = null;


    // panel actions

    public  final static int COMPLETE        = 0;
    public  final static int CANCEL          = 1;
    public  final static int SELECT          = 2;
    public  final static int SCROLL_UP       = 3;
    public  final static int SCROLL_DOWN     = 4;

    // panel constants

    public  final static int APPEARS_BELOW    = 0;
    public  final static int APPEARS_ABOVE    = 1;
    public  final static int DEFAULT_LOCATION = APPEARS_BELOW;

    public  final static int STATIC_DOCKING   = 0;
    public  final static int FLOATING_DOCKING = 1;
    public  final static int DEFAULT_DOCKING  = STATIC_DOCKING;

    // matching criteria

    public  final static int FREETEXT_FILTER  = 0;
    public  final static int EXACT_FILTER     = 1;
    public  final static int DEFAULT_FILTER = EXACT_FILTER;

    private int location  = DEFAULT_LOCATION;
    private int docking   = DEFAULT_DOCKING;

    // internal stuff

    private JTextComponent textComponent    = null;
    private CompletionResult completionResult    = null;
    private CompletionInterface cInterface  = null;

    private int	listSelection = DEFAULT_SELECTION_MODE;

    public CompletionSupport(String title, JTextComponent textComponent,
                           CompletionInterface channel, int location, int docking) {

        this.textComponent = textComponent;
        this.cInterface	= channel;
        this.location   = location;
        this.docking    = docking;

        initPopupComponents();
        initActions();

        setAppearanceOption(location);
    }

    public CompletionSupport(String title, JTextComponent textComponent, CompletionInterface channel) {
        this(title, textComponent, channel, DEFAULT_LOCATION, DEFAULT_DOCKING);
    }

    //
    // POPUP ROUTINES
    //
    public void initPopupComponents() {
        
        // popup panels
        listPanel	= new JPanel(new BorderLayout());

        // popup list

        renderer    = new CompletionVisualListRenderer(); 
        model		= new CompletionVisualListModel(DEFAULT_FILTER); //DefaultListModel();
        list 		= new CompletionVisualList(model);
        list.setSelectionForeground(Color.RED.darker().darker().darker());
        list.setSelectionBackground(Color.LIGHT_GRAY);
        list.setMessage("No Suggestions");
        list.setCellRenderer(renderer);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);

        listScroll	= new JScrollPane(list);

        listPanel.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 5, Color.DARK_GRAY));
        //listPanel.setBorder(BorderFactory.createEmptyBorder());
        listPanel.setLayout(new BorderLayout());
        listPanel.add(listScroll, BorderLayout.SOUTH);

        listScroll.setPreferredSize(getDefaultPreferredSize());
        listPanel.setPreferredSize(getDefaultPreferredSize());

        //
        // focus listener
        //

        textComponent.addFocusListener(new FocusAdapter(){
            public void focusLost(FocusEvent event){
                closePopups();
            }
        });


        /*
        // key listener
        textComponent.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentevent) {
                listProcessorAction.run();
                //EventQueue.invokeLater();
            }

            public void removeUpdate(DocumentEvent documentevent) {
                listProcessorAction.run();
                //EventQueue.invokeLater(listProcessorAction);
            }

            public void changedUpdate(DocumentEvent documentevent) {
                listProcessorAction.run();
                //EventQueue.invokeLater(listProcessorAction);
            }
        });
        */


        // key listener
        textComponent.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent e) {
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
                EventQueue.invokeLater(listProcessorAction);
            }
        });
    }

    public void bindKeyAction(KeyStroke keyStroke, int actionIndex) {
        InputMap inputMap = textComponent.getInputMap();
        ActionMap actionMap = textComponent.getActionMap();

        String name = (String) inputMap.get(keyStroke);

        if (name == null) {
            name = keyStroke.toString();
            inputMap.put(keyStroke, name);
        }

        Action action = actionMap.get(name);

        Action newAction = null;

        switch(actionIndex) {

            case SELECT: newAction = new SelectItemAction(action); break;
            case COMPLETE: newAction = new CompleteAction(action); break;
            case CANCEL: newAction = new CancelAction(action); break;
            case SCROLL_UP: newAction = new ScrollUpAction(action); break;
            case SCROLL_DOWN: newAction = new ScrollDownAction(action); break;
        }

        if (newAction != null) {
            actionMap.put(name, newAction);
        } else {
            log.error("CompletionPopup2-bindKeyAction-actionIndex=" + actionIndex);
        }
    }

    private void initActions() {
        //
        // list pop up runnables
        //

        closeListPopup = new Runnable() {
            public void run() {
                //log.info("closeListPopup");

                //model.clear();
                listPopup.hide();

                //listPanel.stop();
                //listPanel.resetAnimation();

                list_visible = false;
                list_activated = false;
                cInterface.handlePopupClosed();
            }
        };

        updateListPopup = new Runnable() {
            public void run() {
                //log.info("updateListPopup");

                switch(listSelection) {

                    case SELECT_FROM_TOP:
                        list.setSelectedIndex(0);
                        list.ensureIndexIsVisible(0);
                        break;

                    case SELECT_FROM_BOTTOM:
                        int index = model.getSize() - 1;
                        list.setSelectedIndex(index);
                        list.ensureIndexIsVisible(index);
                        break;

                    default:
                        list.setSelectedIndex(0);
                        list.ensureIndexIsVisible(0);
                        break;
                }
            }
        };

        listProcessorAction = new Runnable() {
            public void run() {

                // process list
                //
                //if (textComponent.getText().equals(textstring)) return;
                //textstring = textComponent.getText();

                if(list_activated && list_visible) {
                    //log.info("listProcessorAction");

                    String p = cInterface.providePattern();

                    if (!model.getPattern().equals(p)) {

                        model.setPattern(p);
                        model.replaceItems(completionResult);

                        // if we're in the search mode, we want
                        // the list to be updated on keypress
                        updateListPopup.run();
                    }

                }
            }
        };


        showListPopup = new Runnable() {
            public void run() {
                //log.info("showListPopup");

                if (list_visible) {
                    log.error("--list already popped--");

                } else {
                    showPopupList();
                    list_visible = true;
                }

                cInterface.handlePopupShowed();
            }
        };
    }

    //
    // ACTION CLASSES
    //

    // "select item" action
    class SelectItemAction extends AbstractAction {
        Action delegate;
        public SelectItemAction(Action delegate) {
            this.delegate = delegate;
        }

        public boolean isAllowed(){
            return ( list_activated && list_visible );
        }

        public void actionPerformed(ActionEvent event) {
            //log.info("selectItemAction");
            if (isAllowed()) {
                CompletionItem item = (CompletionItem) list.getSelectedValue();
                
                if (item != null) {
                    String value = item.getValue();
                    String suffix = item.getSuffix();

                    cInterface.acceptResult(value + suffix,
                            (suffix.length() > 0 ? -1 : 0));
                }

                if (list_visible) { closeListPopup.run(); }
                list_activated = false;
            } else {
                if (delegate != null) delegate.actionPerformed(event);
            }
        }
    }

    // "activate list" action
    class CompleteAction extends AbstractAction {
        Action delegate;
        UpdateListRunnable updateListRunnable = new UpdateListRunnable();

        public CompleteAction(Action delegate) {
            this.delegate = delegate;
        }

        public boolean isAllowed() {
            return ( cInterface.canShow() );
        }

        class UpdateListRunnable implements Runnable {
            public void run() {

                model.setPattern(cInterface.providePattern());

                model.replaceItems(completionResult);

                //model.replaceItems(completionResult);

                String addition = completionResult.getAddition();

                if (model.getSize() > 1) {
                    if( !list_visible ) {
                        showListPopup.run();
                    }

                    updateListPopup.run();
                } else if (model.getSize() == 1) {

                    if (addition == null || addition.length() == 0) {
                        CompletionItem item = (CompletionItem) model.getElementAt(0);

                        String value = item.getValue();
                        String suffix = item.getSuffix();

                        cInterface.acceptResult(value + suffix,
                                (suffix.length() > 0 ? - suffix.length()/2 : 0));
                    }
                } else {
                    if (list_visible) { closeListPopup.run(); }
                    list_activated = false;
                }

                if (addition != null && addition.length() > 0) {
                    cInterface.makeAddition(addition);
                }
            }
        }

        public void actionPerformed(ActionEvent event) {

            if (isAllowed()) {

                list_activated = true;

                //log.info("activateListAction");
                new Thread(new Runnable(){
                    public void run(){
                        // request data
                        completionResult = cInterface.provideResult();

                        EventQueue.invokeLater(updateListRunnable);
                    }
                }).start();
            } else {
                if (delegate != null) delegate.actionPerformed(event);
            }
        }
    }

    class CancelAction extends AbstractAction {
        Action delegate;
        public CancelAction(Action delegate) {
            this.delegate = delegate;
        }

        public boolean isAllowed() {
            return list_activated;
        }

        public void actionPerformed(ActionEvent event) {
            if (isAllowed()) {
                // disable the search mode
                list_activated = false;

                if(list_visible) { closeListPopup.run(); }
            } else {
                if (delegate != null) delegate.actionPerformed(event);
            }
        }
    }

    class ScrollUpAction extends AbstractAction {
        Action delegate;
        public ScrollUpAction(Action delegate) {
            this.delegate = delegate;
        }

        public boolean isAllowed(){ return list_visible; }
        public void actionPerformed(ActionEvent event) {
            if (isAllowed()) {
                int index = list.getSelectedIndex() - 1;
                if (index < 0) { index = model.getSize() - 1; }

                // select it
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            } else {
                if (delegate != null) delegate.actionPerformed(event);
            }
        }
    }

    // "scroll down" action

    class ScrollDownAction extends AbstractAction {
        Action delegate;
        public ScrollDownAction(Action delegate) {
            this.delegate = delegate;
        }

        public boolean isAllowed(){ return list_visible; }
        public void actionPerformed(ActionEvent event) {
            if (isAllowed()) {
                int index = list.getSelectedIndex() + 1;
                if (index >= model.getSize()) { index = 0; }

                // select it
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            } else {
                if (delegate != null) delegate.actionPerformed(event);
            }
        }
    }

    //
    // INTERNAL METHODS
    //

    private void showPopupList() {

        int x, y;
        int maxindent = 40;

        FontMetrics metrics = listPanel.getFontMetrics(listPanel.getFont());
        String largestString = model.getLargestString();

        if (this.docking == FLOATING_DOCKING) {
            Point 		areaLoc = textComponent.getLocationOnScreen();
            Point 		loc     = textComponent.getCaret().getMagicCaretPosition();
            int         height	= metrics.getHeight();
            int         width	= metrics.stringWidth(model.getPattern()) + 5;
            Dimension 	popDim	= listPanel.getPreferredSize();

            switch(this.location) {
                case APPEARS_ABOVE:
                    x = areaLoc.x + loc.x - width;
                    y = areaLoc.y + loc.y - popDim.height - 3;
                    break;

                default:
                    x = areaLoc.x + loc.x - width;
                    y = areaLoc.y + loc.y + height + 3;
                    break;
            }

        } else {
            Point 		loc     = textComponent.getLocationOnScreen();
            Dimension 	popDim	= listPanel.getPreferredSize();
            Dimension 	ctrlDim	= textComponent.getSize();

            switch(this.location) {
                case APPEARS_ABOVE:
                    x = loc.x;
                    y = loc.y - popDim.height - 3;
                    break;

                default:
                    x = loc.x;
                    y = loc.y + ctrlDim.height + 3;
                    break;
            }
        }

        Dimension preferrd = getDefaultPreferredSize();

        int popupwidth =
                //Math.min(
                //Math.min(getMaximumPreferredSize().width, Toolkit.getDefaultToolkit().getScreenSize().width - x - 10),
                //Math.max(metrics.stringWidth(largestString) + maxindent, preferrd.width));
                Math.min(getMaximumPreferredSize().width, Math.max(metrics.stringWidth(largestString) + maxindent, preferrd.width));

        listScroll.setPreferredSize(new Dimension(popupwidth, preferrd.height));
        listPanel.setPreferredSize(new Dimension(popupwidth, preferrd.height));

        PopupFactory popupFactory = PopupFactory.getSharedInstance();

        // figure out the list location
        listPopup = popupFactory.getPopup(textComponent, listPanel, x, y);
        listPopup.show();
    }

    //
    //
    //

    public void setSelectionOption(int option) {
        listSelection = option;
        switch (option) {
            case SELECT_FROM_BOTTOM:
                model.setOrder(CompletionVisualListModel.INVERTED_ORDER); break;

            case SELECT_FROM_TOP:
                model.setOrder(CompletionVisualListModel.FORWARD_ORDER); break;

        }
    }

    public void setDockingOption(int option) {
        docking = option;
    }

    public void setAppearanceOption(int option) {
        switch (option) {
            case APPEARS_ABOVE:
                setSelectionOption(SELECT_FROM_BOTTOM); break;

            case APPEARS_BELOW:
                setSelectionOption(SELECT_FROM_TOP); break;
        }

        location = option;
    }

    public void setFilter(int filter) {
        model.setFilter(filter);
    }

    public void handleComponentMoved() {
        closePopups();
    }

    public void handleComponentResized() {
        closePopups();
    }

    public void closePopups() {
        if(list_visible) { EventQueue.invokeLater(closeListPopup); }
    }

    public boolean isActive() {
        return list_visible;
    }

    public Dimension getDefaultPreferredSize() {
        return defaultPreferredSize;
    }

    public Dimension getMaximumPreferredSize() {
        if (rootContainer == null) {
            rootContainer = SwingUtilities.getAncestorOfClass(JFrame.class, textComponent);
        }

        return rootContainer.getSize();
    }
    
    public static void setupGUI(){

        JFrame frame = new JFrame();

        final ConsolePanelBase console = new ConsolePanelBase();
        final CompletionResult history = new CompletionResult();

        ActionListener actionListener = new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                String cmd = event.getActionCommand();
                if (cmd != null && cmd.length() > 0) {
                    console.print("processing cmd=" + cmd + "\n");
                    history.add(new CompletionItem(cmd, "command"));
                }

                console.printPrompt();
            }
        };

        console.setPrompt("%>");
        console.printPrompt();
        console.addActionListener(actionListener);

        CompletionInterface completionInterface = new CompletionInterface(){
            public CompletionResult provideResult() {
                return history;
            }

            public String providePattern() {
                return (console.getInput());
            }

            public void makeAddition(final String s) {
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        console.setInput(console.getInput() + s);
                    }
                });
            }

            public void acceptResult(final String string, final int offset) {
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        console.setInput(string);
                        console.setCaretPosition(console.getCaretPosition() + offset);
                    }
                });
            }

            public boolean canShow() {
                return true;
            }

            public void handlePopupShowed(){
            }

            public void handlePopupClosed(){
            }
        };

        CompletionSupport cmdSearch = new CompletionSupport("Command Search", console, completionInterface);

        cmdSearch.setDockingOption(CompletionSupport.FLOATING_DOCKING);

        // bind in keys

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0),
                CompletionSupport.SELECT);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_TAB, 0),
                CompletionSupport.COMPLETE);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK),
                CompletionSupport.COMPLETE);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CompletionSupport.CANCEL);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0),
                CompletionSupport.SCROLL_UP);

        cmdSearch.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0),
                CompletionSupport.SCROLL_DOWN);

        frame.add(new JScrollPane(console));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(new Dimension(500, 400));
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run(){
                setupGUI();
            }
        });
    }

}
