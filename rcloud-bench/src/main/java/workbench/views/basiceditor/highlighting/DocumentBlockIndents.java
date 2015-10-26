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

import workbench.util.EditorUtil;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 20/02/2012
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
public class DocumentBlockIndents {
    public DocumentBlockLevels l = new DocumentBlockLevels();
    public String baseindent    = "";
    public String currentindent = "";
    public String nextindent    = "";

    public static void updateBlockIndents(String s, DocumentBlockIndents indents) {
        int start = 0;
        int length = s.length();
        boolean allSpaces = true;

        indents.currentindent = indents.nextindent;

        int i_code_b = 0;
        int i_arg_b = 0;
        int i_arr_b = 0;
        int i_comment = 0;
        char i_am_in_text = EditorUtil.EMPTY;

        while (start < length) {
            char c = s.charAt(start);

            if (i_am_in_text == EditorUtil.EMPTY) {

                if (c == EditorUtil.CODEOPEN) {
                    //indents.l.insideCodeBlock++;
                    i_code_b++;

                } else if (c == EditorUtil.CODECLOSE) {
                    //indents.l.insideCodeBlock--;
                    i_code_b--;

                    // all spaces before the block closure sign
                    //
                    if (allSpaces) {
                        // remove one level
                        if (indents.currentindent.length() > EditorUtil.TABSPACE.length()) {
                            indents.currentindent = indents.currentindent.
                                    substring(EditorUtil.TABSPACE.length());
                        } else {
                            indents.currentindent = "";
                        }
                    }

                } else if (c == EditorUtil.ARGSOPEN) {
                    //indents.l.insideArgsBlock++;
                    i_arg_b++;
                } else if (c == EditorUtil.ARGSCLOSE) {
                    //indents.l.insideArgsBlock--;
                    i_arg_b--;
                } else if (c == EditorUtil.ARRAYOPEN) {
                    //indents.l.insideArrayBlock++;
                    i_arr_b++;
                } else if (c == EditorUtil.ARRAYCLOSE) {
                    //indents.l.insideArrayBlock--;
                    i_arr_b--;
                }
            }


            if (c == EditorUtil.QUOTE1) {
                if (i_am_in_text == EditorUtil.EMPTY) {
                    // entering text
                    i_am_in_text = EditorUtil.QUOTE1;
                } else if (i_am_in_text == EditorUtil.QUOTE1) {
                    // exiting text
                    i_am_in_text = EditorUtil.EMPTY;
                }
            }

            if (c == EditorUtil.QUOTE2) {
                if (i_am_in_text == EditorUtil.EMPTY) {
                    // entering text
                    i_am_in_text = EditorUtil.QUOTE2;
                } else if (i_am_in_text == EditorUtil.QUOTE2) {
                    // exiting text
                    i_am_in_text = EditorUtil.EMPTY;
                }
            }


            if (c == EditorUtil.COMMENT || start == length - 1) {
                indents.l.insideCodeBlock += i_code_b;
                indents.l.insideArgsBlock += i_arg_b;
                indents.l.insideArrayBlock += i_arr_b;

                i_code_b = 0;
                i_arg_b = 0;
                i_arr_b = 0;

                i_comment++;
            }

            if (c == EditorUtil.NEWLINE || start == length - 1) {

                if (i_comment == 0) {
                    indents.l.insideCodeBlock += i_code_b;
                    indents.l.insideArgsBlock += i_arg_b;
                    indents.l.insideArrayBlock += i_arr_b;
                }

                i_code_b = 0;
                i_arg_b = 0;
                i_arr_b = 0;
                i_comment = 0;
            }

            // at the very end, check the symbol
            // is not a whitespace. if checked before,
            // the logic of "all spaces" would be
            // affected by the current symbol
            //
            if (c != ' ') {
                allSpaces = false;
            }

            start++;
        }

        indents.nextindent = computeIndentFromLevels(indents.l);
    }
    
    public static String computeIndentFromLevels(DocumentBlockLevels levels) {
        int indentlength = levels.insideCodeBlock * EditorUtil.TABSPACE.length() +
                           levels.insideArgsBlock * EditorUtil.TABSPACE.length() * 2 +
                           levels.insideArrayBlock * EditorUtil.TABSPACE.length() * 2;

        return indentlength > 0 ? EditorUtil.buildWhiteSpaceString(indentlength) : "";

    }

}
