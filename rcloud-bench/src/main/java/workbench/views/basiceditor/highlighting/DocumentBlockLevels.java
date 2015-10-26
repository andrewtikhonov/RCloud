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
 * Date: 17/02/2012
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
public class DocumentBlockLevels {
    public int insideCodeBlock  = 0;
    public int insideArgsBlock  = 0;
    public int insideArrayBlock = 0;

    public static DocumentBlockLevels analyzeNestedLevels(String text, int pos) {

        int start = pos;

        int i_code_b = 0;
        int i_args_b = 0;
        int i_arra_b = 0;

        char i_am_in_text = EditorUtil.EMPTY;

        DocumentBlockLevels structure = new DocumentBlockLevels();

        while (start >= 0) {
            char c = text.charAt(start);

            if (i_am_in_text == EditorUtil.EMPTY) {
                if (c == EditorUtil.CODEOPEN) {
                    i_code_b++;
                } else if (c == EditorUtil.CODECLOSE) {
                    i_code_b--;
                } else if (c == EditorUtil.ARGSOPEN) {
                    i_args_b++;
                } else if (c == EditorUtil.ARGSCLOSE) {
                    i_args_b--;
                }  else if (c == EditorUtil.ARRAYOPEN) {
                    i_arra_b++;
                }  else if (c == EditorUtil.ARRAYCLOSE) {
                    i_arra_b--;
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

            if (c == EditorUtil.COMMENT) {
                i_code_b = 0;
                i_args_b = 0;
                i_arra_b = 0;
            }

            if (c == EditorUtil.NEWLINE || start == 0) {
                structure.insideCodeBlock += i_code_b;
                structure.insideArgsBlock += i_args_b;
                structure.insideArrayBlock += i_arra_b;

                i_code_b = 0;
                i_args_b = 0;
                i_arra_b = 0;
            }


            start--;
        }

        return structure;
    }

}
