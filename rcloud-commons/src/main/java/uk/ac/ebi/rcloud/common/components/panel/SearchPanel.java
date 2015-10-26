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
package uk.ac.ebi.rcloud.common.components.panel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.textfield.JSearchField;
import uk.ac.ebi.rcloud.common.util.KeyUtil;

import javax.management.Attribute;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.*;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 2, 2009
 * Time: 11:40:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchPanel extends JPanel {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private boolean searchActivated = false;

    private JButton nextButton;
    private JButton prevButton;
    private JSearchField searchField;
    private JCheckBox matchCaseCB;
    private JTextPane textComponent;

    private SimpleAttributeSet defaultSet = null;
    private SimpleAttributeSet highlightSet = null;
    private SimpleAttributeSet selectedSet = null;
    private int selectionstart = -1;
    private int selectionlength = -1;

    public DeactivateAction deactivateAction = new DeactivateAction("Quit Find");
    public ActivateAction activateAction = new ActivateAction("Find");
    public FindNextAction findNextAction = new FindNextAction("Find Next");
    public FindPreviousAction findPreviousAction = new FindPreviousAction("Find Previous");

    public SearchPanel(JTextPane text) {

        this.textComponent = text;
        this.defaultSet = new SimpleAttributeSet(textComponent.
                getStyledDocument().getParagraphElement(0).getAttributes());
        //StyleConstants.setBackground(this.defaultSet, textComponent.getBackground());

        this.selectedSet = new SimpleAttributeSet(defaultSet.copyAttributes());
        StyleConstants.setBackground(this.selectedSet, Color.LIGHT_GRAY);

        this.highlightSet = new SimpleAttributeSet(defaultSet.copyAttributes());
        StyleConstants.setBackground(this.highlightSet, Color.ORANGE);

        this.setOpaque(false);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(layout);
        setOpaque(false);

        Cursor hand = new Cursor(Cursor.HAND_CURSOR);
        
        nextButton  = new JButton("Next");
        prevButton  = new JButton("Previous");
        searchField = new JSearchField();
        matchCaseCB = new JCheckBox("Match case");
        matchCaseCB.setSelected(true);

        //add(closeButton);
        add(searchField);
        add(nextButton);
        add(prevButton);
        add(matchCaseCB);

        c.weightx = 0.1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;

        layout.setConstraints(searchField, c);

        nextButton.addActionListener(findNextAction);
        prevButton.addActionListener(findPreviousAction);

        searchField.getDocument().addDocumentListener(new SearchFieldTracker());

        InputMap inputMap = searchField.getInputMap();
        ActionMap actionMap = searchField.getActionMap();

        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_ENTER, 0), "search-next");
        actionMap.put("search-next", findNextAction);

        matchCaseCB.addActionListener(activateAction);

        textComponent.addMouseListener(new TextComponentMouseListener());

    }

    class TextComponentMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent event) {
            resetMarker(selectionstart, selectionlength);
        }
    }

    public Action getDeactivateAction() {
        return deactivateAction;
    }

    public Action getActivateAction() {
        return activateAction;
    }

    public Action getFindNextAction() {
        return findNextAction;
    }

    public Action getFindPreviousAction() {
        return findPreviousAction;
    }

    private void resetMarkup() {
        StyledDocument doc = textComponent.getStyledDocument();

        int length = doc.getLength();
        int offset = 0;
        int lastEnd = Integer.MAX_VALUE;

        for (int pos = offset; pos < (offset + length); pos = lastEnd) {

            Element run = doc.getCharacterElement(pos);
            lastEnd = run.getEndOffset();

            if (pos == lastEnd) {
                break;
            }

            SimpleAttributeSet attrCopy = new SimpleAttributeSet(run.getAttributes());

            Object backColor = attrCopy.getAttribute(originalBackKey);

            if (backColor != null) {
                //log.info("backColor="+backColor);

                Object backgroundKey = StyleConstants.Background;

                if (doc instanceof HTMLDocument) {
                    backgroundKey = CSS.Attribute.BACKGROUND_COLOR;
                }

                if (backColor == nullColorValue) {
                    attrCopy.removeAttribute(originalBackKey);
                    attrCopy.removeAttribute(backgroundKey);
                } else {
                    attrCopy.removeAttribute(originalBackKey);
                    StyleConstants.setBackground(attrCopy, (Color) backColor);
                }

                doc.setCharacterAttributes(pos, lastEnd - pos, attrCopy, true);
            }
        }
    }

    static class MyCustomAttribute {
        private String name;
        public MyCustomAttribute(String name) {
            this.name = name;
        }
    }

    private static final Object originalBackKey = new MyCustomAttribute("original-background");
    private static final Object nullColorValue = new Color(0,0,0,0);

    private void markup() {

        resetMarkup();

        String patt = searchField.getText();

        if (patt != null && patt.length() > 1) {
            StyledDocument doc = textComponent.getStyledDocument();

            try {
                String text = doc.getText(0, doc.getLength());

                if (!matchCaseCB.isSelected()) {
                    text = text.toUpperCase();
                    patt = patt.toUpperCase();
                }

                int index = 0;

                while((index = text.indexOf(patt, index)) != -1) {

                    SimpleAttributeSet highlightAttributeSet =
                            new SimpleAttributeSet(doc.getCharacterElement(index).getAttributes());

                    Object backcolor = highlightAttributeSet.getAttribute(StyleConstants.Background);

                    if (backcolor != null) {
                        //log.info("backcolor="+backcolor);
                    } else {
                        backcolor = nullColorValue;
                    }

                    highlightAttributeSet.addAttribute(originalBackKey, backcolor);
                    StyleConstants.setBackground(highlightAttributeSet, Color.ORANGE);
                    doc.setCharacterAttributes(index, patt.length(), highlightAttributeSet, false);

                    index += patt.length();
                }

            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        } else {

        }

    }

    private void searchFirst() {
        Caret caret = textComponent.getCaret();
        textComponent.setCaretPosition(Math.min(caret.getDot(), caret.getMark()));
        searchForward();
    }

    private void searchForward() {

        String patt = searchField.getText();
        Caret caret = textComponent.getCaret();

        if (patt != null && patt.length() > 0) {
            Document doc = textComponent.getDocument();

            try {
                String text = doc.getText(0, doc.getLength());

                if (!matchCaseCB.isSelected()) {
                    text = text.toUpperCase();
                    patt = patt.toUpperCase();
                }

                int start = Math.max(caret.getDot(), caret.getMark());
                int index = text.indexOf(patt, start);
                if (index == -1 && start != 0) {
                    index = text.indexOf(patt, 0);
                }

                updateSelection(index, patt.length());
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        } else {

        }
    }

    private void searchBackward() {

        String patt = searchField.getText();
        Caret caret = textComponent.getCaret();

        if (patt != null && patt.length() > 0) {
            Document doc = textComponent.getDocument();

            try {
                String text = doc.getText(0, doc.getLength());

                if (!matchCaseCB.isSelected()) {
                    text = text.toUpperCase();
                    patt = patt.toUpperCase();
                }

                int start = Math.min(caret.getDot(), caret.getMark()) - 1;
                int index = text.lastIndexOf(patt, start);
                if (index == -1 && start != text.length()) {
                    index = text.lastIndexOf(patt, text.length());
                }

                updateSelection(index, patt.length());
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        } else {

        }
    }

    class DeactivateAction extends AbstractAction implements ActionListener {
        public DeactivateAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            if (searchActivated) {
                setVisible(false);
                searchActivated = false;

                resetMarker(selectionstart, selectionlength);

                resetMarkup();

                selectionstart = 0;
                selectionlength = 0;
            }
        }
    }

    class ActivateAction extends AbstractAction implements ActionListener {
        public ActivateAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            if (!searchActivated) {
                setVisible(true);
                searchActivated = true;

                selectionstart = 0;
                selectionlength = 0;
            }

            final String selected = textComponent.getSelectedText();

            if (selected != null && !selected.equals("")) {
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        searchField.setText(selected);
                    }
                });
            }

            markup();
            searchField.requestFocus();
        }
    }

    class FindNextAction extends AbstractAction implements ActionListener {
        public FindNextAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            searchForward();
        }
    }

    class FindPreviousAction extends AbstractAction implements ActionListener {
        public FindPreviousAction(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            searchBackward();
        }
    }


    class SearchFieldTracker implements DocumentListener {
        public void insertUpdate(DocumentEvent ev) {
            markup();
            searchFirst();
        }

        public void removeUpdate(DocumentEvent ev) {
            markup();
            searchFirst();
        }

        public void changedUpdate(DocumentEvent ev) {
        }
    }


    @Override
    public void addNotify() {
        super.addNotify();
        initializeComponent();
    }

    public boolean isActive() {
        return searchActivated;
    }


    public void deactivateSearch() {
    }

    public void initializeComponent() {

        JComponent root = (JComponent)this.getParent();

        root.registerKeyboardAction(deactivateAction,
                KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        root.registerKeyboardAction(activateAction,
                KeyUtil.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        root.registerKeyboardAction(findNextAction,
                KeyUtil.getKeyStroke(KeyEvent.VK_F3, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        root.registerKeyboardAction(findPreviousAction,
                KeyUtil.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        root.registerKeyboardAction(findNextAction,
                KeyUtil.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        root.registerKeyboardAction(findPreviousAction,
                KeyUtil.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_MASK + KeyEvent.SHIFT_MASK),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.setVisible(false);
        searchActivated = false;
    }

    private void resetMarker(int start, int length) {
        //StyledDocument doc = textComponent.getStyledDocument();
        //doc.setCharacterAttributes(start, length, defaultSet, true);
    }

    private void resetSelection(int start, int length) {
        textComponent.setSelectionStart(start);
        textComponent.setSelectionEnd(start);
    }

    private void imposeMarker(int start, int length) {
        //StyledDocument doc = textComponent.getStyledDocument();
        //doc.setCharacterAttributes(start, length, selectedSet, true);
    }

    private void imposeSelection(int start, int length) {
        textComponent.setSelectionStart(start);
        textComponent.setSelectionEnd(start + length);
    }

    private void updateSelection(int start, int length) {

        if (selectionstart != -1) {
            resetMarker(selectionstart, selectionlength);
            resetSelection(selectionstart, selectionlength);
        }

        selectionstart = start;
        selectionlength = length;

        if (start != -1) {

            imposeMarker(selectionstart, selectionlength);
            imposeSelection(selectionstart, selectionlength);
        }
    }

    public static void setupGUI(){
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        final JTextPane text      = new JTextPane();
        final JScrollPane scroll  = new JScrollPane(text);
        final SearchPanel search  = new SearchPanel(text);

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(search, BorderLayout.SOUTH);

        frame.setPreferredSize(new Dimension(500, 200));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable (){
            public void run(){
                setupGUI();
            }
        });
    }

}
