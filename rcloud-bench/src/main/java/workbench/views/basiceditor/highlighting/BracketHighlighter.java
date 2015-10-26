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
package workbench.views.basiceditor.highlighting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.util.EditorUtil;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 17, 2010
 * Time: 5:43:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class BracketHighlighter implements CaretListener {

    final static private Logger log = LoggerFactory.getLogger(BracketHighlighter.class);

    private StyledDocument doc;

    public BracketHighlighter(StyledDocument doc) {
        this.doc = doc;
    }

    class Mark {

        private int pos;
        private Color background;
        private StyledDocument doc;
        private Color mark = new Color(220, 220, 200, 255);

        public Mark(StyledDocument doc, int pos) {
            this.doc = doc;
            this.pos = pos;
        }

        public void setMark() {
            SimpleAttributeSet attrSet =
                    new SimpleAttributeSet(doc.getCharacterElement(pos).getAttributes());

            background = (Color) attrSet.getAttribute(StyleConstants.Background);

            StyleConstants.setBackground(attrSet, mark);
            doc.setCharacterAttributes(pos, 1, attrSet, false);
        }

        public void removeMark() {
            SimpleAttributeSet attrSet =
                    new SimpleAttributeSet(doc.getCharacterElement(pos).getAttributes());

            if (background != null) {
                StyleConstants.setBackground(attrSet, background);
            } else {
                attrSet.removeAttribute(StyleConstants.Background);
            }

            doc.setCharacterAttributes(pos, 1, attrSet, true);
        }
    }

    private Vector<Mark> marksToSet = new Vector<Mark>();
    private Vector<Mark> marksToRemove = new Vector<Mark>();

    String CALLBACK = "CALLBACK";

    private Runnable updateMarksRunnable = new Runnable() {
        public void run() {
            if (marksToRemove.size() > 0) {
                for (Mark m : marksToRemove) {
                    m.removeMark();
                }
            }
            if (marksToSet.size() > 0) {
                for (Mark m : marksToSet) {
                    m.setMark();
                }
                marksToRemove = marksToSet;
                marksToSet = new Vector<Mark>();
            } else {
                marksToRemove = new Vector<Mark>();
            }
        }
    };


    public void caretUpdate(CaretEvent event) {
        Object obj = doc.getProperty(CALLBACK);

        if (obj == null) {
            doc.putProperty(CALLBACK, updateMarksRunnable);
        }

        int dot = event.getDot();
        int mark = event.getMark();

        if (dot == mark) {
            try {
                if (dot < doc.getLength()) {
                    char open = doc.getText(dot, 1).charAt(0);
                    char close = 0;

                    if (open == EditorUtil.CODEOPEN) {
                        close = EditorUtil.CODECLOSE;
                    } else if (open == EditorUtil.ARGSOPEN) {
                        close = EditorUtil.ARGSCLOSE;
                    } else if (open == EditorUtil.ARRAYOPEN) {
                        close = EditorUtil.ARRAYCLOSE;
                    }

                    if (close != 0) {
                        String text = doc.getText(dot, doc.getLength() - dot);
                        Integer end = EditorUtil.findBlockEndChar(text, 1, open, close);

                        if (end != null) {
                            marksToSet.add(new Mark(doc, dot));
                            marksToSet.add(new Mark(doc, dot + end));
                        }
                    }
                }

                if (dot > 0) {
                    int index = dot - 1;

                    char close = doc.getText(index, 1).charAt(0);
                    char open = 0;

                    if (close == EditorUtil.CODECLOSE) {
                        open = EditorUtil.CODEOPEN;
                    } else if (close == EditorUtil.ARGSCLOSE) {
                        open = EditorUtil.ARGSOPEN;
                    } else if (close == EditorUtil.ARRAYCLOSE) {
                        open = EditorUtil.ARRAYOPEN;
                    }

                    if (open != 0) {
                        String text = doc.getText(0, index);
                        Integer end = EditorUtil.findBlockStartChar(text, index - 1, open, close);

                        if (end != null) {
                            marksToSet.add(new Mark(doc, index));
                            marksToSet.add(new Mark(doc, end));
                        }
                    }
                }

                if (marksToSet.size() > 0 || marksToRemove.size() > 0) {
                    updateMarks();
                }

            } catch (BadLocationException ble) {
                log.error("Error!", ble);
            }
        }
    }

    public void updateMarks() {
        EventQueue.invokeLater(updateMarksRunnable);
    }

}
