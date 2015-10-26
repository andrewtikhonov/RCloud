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
package workbench.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.views.basiceditor.highlighting.DocumentBlockIndents;
import workbench.views.basiceditor.highlighting.DocumentBlockLevels;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 8, 2010
 * Time: 3:31:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditorUtil {

    private static Logger log = LoggerFactory.getLogger(EditorUtil.class);

    public static char EMPTY = 0x255;
    public static char NEWLINE = '\n';
    public static char SPACEBAR = ' ';
    public static char CODEOPEN = '{';
    public static char CODECLOSE = '}';
    public static char ARGSOPEN = '(';
    public static char ARGSCLOSE = ')';
    public static char ARRAYOPEN = '[';
    public static char ARRAYCLOSE = ']';
    public static char QUOTE1 = '"';
    public static char QUOTE2 = '\'';
    public static char ESCAPE = '\\';
    public static char COMMENT = '#';
    public static String TABSPACE = "    ";

    public static String buildWhiteSpaceString(int num) {
        char[] spaces = new char[num];
        Arrays.fill(spaces, SPACEBAR);
        return new String(spaces);
    }

    public static boolean allWhiteSpacesLineBeforePos(String text, int pos) {
        int start = pos;

        while (start >= 0) {
            char c = text.charAt(start);

            if (c == NEWLINE) {
                return true;
            } else if (c == SPACEBAR) {
            } else {
                return false;
            }

            start--;
        }

        return true;
    }

    public static boolean allWhiteSpaces(String text) {
        int start = text.length() - 1;

        while (start >= 0) {
            char c = text.charAt(start);

            if (c == SPACEBAR) {
            } else {
                return false;
            }

            start--;
        }

        return true;
    }

    public static boolean isLetter(String str) {
        Character c = str.charAt(0);
        return (Character.isLetterOrDigit( c ) ||
                str.equals("_") || str.equals("$") || str.equals(".") );
    }

    public static TextSelection findKeywordSelection(String str, int position) {

        TextSelection selection = null;

        if (str == null || str.length() == 0) return selection;
        if (position >= str.length()) return selection;

        String c        = str.substring(position, position + 1);
        int lastidx     = str.length() - 1;
        int start       = position;
        int stop        = position;

        if (isLetter( c )) {
            selection = new TextSelection();

            while (start > 0 && isLetter(str.substring(start - 1, start))) {
                start--;
            }

            while (stop < lastidx && isLetter(str.substring(stop + 1, stop + 2))) {
                stop++;
            }

            selection.start = start;
            selection.length = stop - start + 1;
        }
        return selection;
    }


    public static String getSelectedkeyword(JTextPane textpane) {
        String keyword = null;
        String s = textpane.getText();

        int start = textpane.getSelectionStart();
        int end   = textpane.getSelectionEnd();

        if (start != end) {
            keyword = s.substring(start, end);

        } else {
            TextSelection sel = findKeywordSelection(s, textpane.getCaretPosition());

            if (sel != null) {
                keyword = s.substring(sel.start, sel.start + sel.length);
            }
        }
        return keyword;
    }

    public static Integer findBlockStart(String text, int pos) {
        return findBlockStartChar(text, pos, CODEOPEN, CODECLOSE);
    }

    public static Integer findBlockStartChar(String text, int pos, char open, char close) {
        int start = pos;
        int block = 0;
        Integer index = null;

        while (start >= 0) {
            char c = text.charAt(start);

            if (c == open) {
                block++;
                if (block > 0) {
                    index = start;
                    break;
                }
            } else if (c == close) {
                if (block <= 0) {
                    block--;
                }
            }

            start--;
        }
        return index;
    }

    public static Integer findBlockEndChar(String text, int pos, char open, char close) {
        int start = pos;
        int block = 0;
        int length = text.length();
        Integer index = null;

        while (start < length) {
            char c = text.charAt(start);

            if (c == open) {
                if (block >= 0) {
                    block++;
                }
            } else if (c == close) {
                block--;
                if (block < 0) {
                    index = start;
                    break;
                }
            }

            start++;
        }
        return index;
    }

    public static Integer findBlockEnd(String text, int pos) {
        return findBlockEndChar(text, pos, CODEOPEN, CODECLOSE);
    }

    public static String findWhiteSpaces(String text, int pos) {
        String space = "";

        int start = pos;
        int end = pos;

        while (start >= 0) {
            char c = text.charAt(start);

            if (c <= SPACEBAR || c == NEWLINE) {
            } else {
                end = start;
            }

            if (start == 0) {
                space = text.substring(start, end);
                break;
            } else if (c == NEWLINE) {
                space = text.substring(start + 1, end);
                break;
            }

            start--;
        }

        return space;
    }

    public static String getLineIndent(String s) {
        String indent = "";
        int start = 0;
        int length = s.length();

        while (start < length) {
            char c = s.charAt(start);

            if (c == NEWLINE) {
                break;
            } if (c <= SPACEBAR) {
                start++;
            } else {
                break;
            }
        } 

        indent = s.substring(0, start);
        return indent;
    }

    public static Integer hasCharBeforePosition(String text, int pos, char ch) {
        int start = pos;
        Integer index = null;

        while (start >= 0) {
            char c = text.charAt(start);

            if (c == SPACEBAR) {
            } else if (c == ch) {
                index = start;
                break;
            } else {
                break;
            }

            start--;
        }
        return index;
    }

    public static Integer hasBlockOpeningBeforePosition(String text, int pos) {
        return hasCharBeforePosition(text, pos, CODEOPEN);
    }


    public static Integer hasCharAfterPosition(String text, int pos, char ch) {
        int start = pos;
        int length = text.length();
        Integer index = null;

        while (start < length) {
            char c = text.charAt(start);

            if (c == SPACEBAR) {
            } else if (c == ch) {
                index = start;
                break;
            } else {
                break;
            }

            start++;
        }
        return index;
    }

    public static Integer hasBlockClosureAfterPosition(String text, int pos) {
        return hasCharAfterPosition(text, pos, CODECLOSE);
    }


    public static int countBlockCharsBackwards(String text, int pos, char open, char close, Character endchar) {
        int start = pos;
        int block = 0;

        while (start >= 0) {
            char c = text.charAt(start);

            if (c == open) {
                block++;
            } else if (c == close) {
                block--;
            } else if (endchar != null && c == endchar) {
                break;
            }

            start--;
        }
        return block;
    }


    public static boolean hasOpenCodeBlocks(String text, int pos) {
        return (countBlockCharsBackwards(text, pos, CODEOPEN, CODECLOSE, null) > 0);
    }

    public static boolean insideArgumentBlock(String text, int pos) {
        return (countBlockCharsBackwards(text, pos, ARGSOPEN, ARGSCLOSE, CODEOPEN) > 0);
    }

    public static boolean insideArrayBlock(String text, int pos) {
        return (countBlockCharsBackwards(text, pos, ARRAYOPEN, ARRAYCLOSE, CODEOPEN) > 0);
    }

    public static String findBlockInitialIndent(String text, int pos, char open1, char close1, char open2, char close2) {
        int start = pos;
        int end   = pos;
        int block1 = 0;
        int block2 = 0;

        String space = "";

        while (start >= 0) {
            char c = text.charAt(start);

            if (c == close1) {
                block1--;
            } else if (c == open1) {
                block1++;
            } else if (c == close2) {
                block2--;
            } else if (c == open2) {
                block2++;
            }

            if (block1 != 0 || block2 != 0) {
                end = start;
            }

            if (c <= SPACEBAR || c == NEWLINE) {
            } else {
                end = start;
            }

            if (start == 0) {
                space = text.substring(start, end);
                break;
            } else if (c == NEWLINE && block1 == 0 && block2 == 0) {
                space = text.substring(start + 1, end);
                break;
            }

            start--;
        }

        return space;
    }

    public static String findCurrentBlockIndent(String text, int pos) {

        String indent = null;

        Integer blockstart = EditorUtil.findBlockStart( text, pos );
        if (blockstart != null) {
            indent = EditorUtil.findBlockInitialIndent(text, blockstart - 1,
                    ARGSOPEN, ARGSCLOSE, ARRAYOPEN, ARRAYCLOSE);
        }

        return indent;
    }

    public static String findIndentSimple(String text, int pos) {

        String indent = findCurrentBlockIndent(text, pos);

        if (indent != null) {
            return  indent + TABSPACE;
        }

        return indent;
    }

    public static String findBlockInnerIndent(String text, int pos) {

        DocumentBlockLevels l = DocumentBlockLevels.analyzeNestedLevels(text, pos);
        return DocumentBlockIndents.computeIndentFromLevels(l);
    }

    public static DocumentLine getLineAtPosition(Document doc, int pos){
        Element root  = doc.getDefaultRootElement();
        return getLineAtIndex(doc, root.getElementIndex(pos));
    }

    public static DocumentLine getLineAtIndex(Document doc, int linenum){
        Element root  = doc.getDefaultRootElement();
        Element el    = root.getElement(linenum);
        int start     = el.getStartOffset();
        //int end       = el.getEndOffset() - 1;
        int end       = el.getEndOffset();

        try {
            return new DocumentLine(doc.getText(start, end - start), start, end);
        } catch (BadLocationException ble) {
            log.error("Error!", ble);
            return null;
        }
    }


}
