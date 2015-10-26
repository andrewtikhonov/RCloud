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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.RGui;
import workbench.util.EditorUtil;
import workbench.util.TextSelection;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 19, 2010
 * Time: 4:26:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditorBrouseAdapter implements MouseInputListener, KeyListener, FocusListener {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private String word = "";
    private boolean metaPressed = false;
    private Point mousePosition = null;

    private TextSelection selection = null;
    private Action openFunction = null;

    private RGui rgui;
    private BasicEditorPanel editor;

    private Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private Cursor defa = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    public EditorBrouseAdapter(RGui rgui, BasicEditorPanel editor){
        this.rgui = rgui;
        this.editor = editor;
    }

    private boolean checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            editor.updateEditorPopupMenus();
            editor.getPopupMenu().show(editor.getEditBuffer(), e.getX(), e.getY());
            return true;
        }
        return false;
    }

    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseClicked(MouseEvent e) {
        if (!checkPopup(e)) {
            if (metaPressed && word.length() > 0) {

                if (openFunction == null) {
                    openFunction = rgui.getActions().get("openfunction");
                }
                openFunction.actionPerformed(new ActionEvent(this, 0, word));

                deactivateBrowsing();
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
        mousePosition = e.getPoint();

        if (metaPressed) {
            updateSelectedKeyword();
        }
    }

    private SimpleAttributeSet defaultAttributeSet = null;
    private SimpleAttributeSet underlineAttributeSet = null;


    private void setSelection(int start, int length, boolean underline) {

        StyledDocument doc = editor.getEditBuffer().getStyledDocument();

        if (defaultAttributeSet == null) {
            defaultAttributeSet = new SimpleAttributeSet(doc.getParagraphElement(0).getAttributes());
            StyleConstants.setUnderline(defaultAttributeSet, false);

            underlineAttributeSet = new SimpleAttributeSet(doc.getParagraphElement(0).getAttributes());
            StyleConstants.setUnderline(underlineAttributeSet, true);
        }

        if (underline) {
            doc.setCharacterAttributes(start, length, underlineAttributeSet, false);
        } else {
            doc.setCharacterAttributes(start, length, defaultAttributeSet, false);
        }

        if (underline) {
            editor.getEditBuffer().setCursor(hand);
        } else {
            editor.getEditBuffer().setCursor(defa);
        }
    }

    private void deactivateBrowsing(){
        word = "";
        metaPressed = false;
        if (selection != null) {
            setSelection(selection.start, selection.length, false);
            selection = null;
        }
    }

    private void updateSelectedKeyword() {
        if (mousePosition != null) {
            int index = editor.getEditBuffer().viewToModel(mousePosition);

            String text = editor.getEditBuffer().getText();
            TextSelection tmpSelection =
                    EditorUtil.findKeywordSelection(text, index);

            if (selection != null && tmpSelection != null &&
                    tmpSelection.start == selection.start &&
                    tmpSelection.length == selection.length) {

                return;
            }

            if (selection != null) {
                setSelection(selection.start, selection.length, false);
            }

            selection = tmpSelection;

            if (selection != null) {
                word = text.substring(selection.start, selection.start + selection.length);

                //log.info(word);
                setSelection(selection.start, selection.length, true);
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        boolean metaPressed0 = isPressed(e, KeyEvent.META_MASK) ||
                isPressed(e, KeyEvent.CTRL_MASK);

        if (metaPressed != metaPressed0 && metaPressed0) {
            metaPressed = metaPressed0;
            //updateSelectedKeyword();
            //log.info("META on " + metaPressed);
        }
    }

    public void keyReleased(KeyEvent e) {
        boolean metaPressed0 = isPressed(e, KeyEvent.META_MASK) ||
                isPressed(e, KeyEvent.CTRL_MASK);

        if (metaPressed0 != metaPressed && !metaPressed0) {
            metaPressed = metaPressed0;
            //log.info("META off " + metaPressed);
            deactivateBrowsing();
        }
    }

    public void focusGained(FocusEvent event) {
    }

    public void focusLost(FocusEvent event) {
        deactivateBrowsing();
    }

    private static boolean isPressed(KeyEvent e, int mask) {
        return ((e.getModifiers() & mask) == mask);
    }

}


