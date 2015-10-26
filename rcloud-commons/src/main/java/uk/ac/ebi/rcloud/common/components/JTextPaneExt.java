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
package uk.ac.ebi.rcloud.common.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.util.KeyUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 29, 2009
 * Time: 5:23:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class JTextPaneExt extends JTextPane {

    final private Logger log = LoggerFactory.getLogger(getClass());

    public static final int MIN_FONT_SIZE = 4;
    public static final int MAX_FONT_SIZE = 25;
    public static final int FONT_CHANGE_STEP = 1;

    private LimitingDocumentFilter limiter = new LimitingDocumentFilter();

    public JTextPaneExt(){

        //ActionMap actionMap = getActionMap();
        InputMap inputMap = getInputMap();

        IncreaseFontSizeAction increaseAction = new IncreaseFontSizeAction();
        DecreaseFontSizeAction decreaseAction = new DecreaseFontSizeAction();

        /* Press enter to evaluate the input */
        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.META_MASK),
            increaseAction);

        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.META_MASK),
            decreaseAction);

        /* Press enter to evaluate the input */
        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.META_MASK),
            increaseAction);

        inputMap.put(KeyUtil.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.META_MASK),
            decreaseAction);

        ((AbstractDocument) getDocument()).setDocumentFilter(limiter);

    }

    public void appendSafe(String text) {
        try {
            Document doc = this.getDocument();
            doc.insertString(doc.getLength(), text, null);
            this.setCaretPosition(doc.getLength());
        } catch (BadLocationException ble) {
            throw new RuntimeException(ble);
        }
    }

	public void append(final String text) {
		if (EventQueue.isDispatchThread()) {
            appendSafe(text);
        } else {
            EventQueue.invokeLater(new Runnable(){
                public void run(){
                    appendSafe(text);
                }
            });
        }
	}

    class IncreaseFontSizeAction extends AbstractAction {

        private void increaseFontSize() {
            Font font1 = JTextPaneExt.this.getFont();
            int size = font1.getSize();
            if (size < MAX_FONT_SIZE) {
                size+=FONT_CHANGE_STEP;

                Font font2 = new Font(font1.getName(), font1.getStyle(), size);
                JTextPaneExt.this.setFont(font2);
                JTextPaneExt.this.repaint();
            }
        }

        public void actionPerformed(ActionEvent evt) {
            if (EventQueue.isDispatchThread()) {
                increaseFontSize();
            } else {
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        increaseFontSize();
                    }
                });
            }
        }
    }

    class DecreaseFontSizeAction extends AbstractAction {

        private void decreaseFontSize() {
            Font font1 = JTextPaneExt.this.getFont();
            int size = font1.getSize();
            if (size > MIN_FONT_SIZE) {
                size-=FONT_CHANGE_STEP;

                Font font2 = new Font(font1.getName(), font1.getStyle(), size);
                JTextPaneExt.this.setFont(font2);
                JTextPaneExt.this.repaint();
            }
        }

        public void actionPerformed(ActionEvent evt) {
            if (EventQueue.isDispatchThread()) {
                decreaseFontSize();
            } else {
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        decreaseFontSize();
                    }
                });
            }
        }
    }

    public void registerKeyAction(KeyStroke keyStroke, String actionName, Action action) {
        getInputMap().put(keyStroke, actionName);
        getActionMap().put(actionName, action);
    }

    class LimitingDocumentFilter extends DocumentFilter {

        public void insertString(DocumentFilter.FilterBypass fb, int offset,
                                 String text, AttributeSet attr) throws BadLocationException {
            fb.insertString(offset, text, attr);

            if (MAX_LINES_IN_BUFFER > 0) {
                Document doc = JTextPaneExt.this.getDocument();
                Element root = doc.getDefaultRootElement();
                int count = root.getElementCount();

                if (count > MAX_LINES_IN_BUFFER) {

                    Element el0 = root.getElement(0);
                    Element el1 = root.getElement(CUT_NUMBER_OF_LINES);

                    int start = el0.getStartOffset();
                    int end = el1.getEndOffset();

                    try {
                        doc.remove(start, end - start);
                    } catch (BadLocationException ble){
                        log.error("Error!", ble);
                    }
                }
            }
            
        }
    }

    private int MAX_LINES_IN_BUFFER = 0;
    private int CUT_NUMBER_OF_LINES = 1000;

    public void setLineLimit(int maxLinesInBuffer, int cutNumerOfLines) {
        this.MAX_LINES_IN_BUFFER = maxLinesInBuffer;
        this.CUT_NUMBER_OF_LINES = cutNumerOfLines;
    }


}
