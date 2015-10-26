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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.RType.RChar;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;
import workbench.RGui;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 15, 2010
 * Time: 5:50:57 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CompletionInterfaceAbstract implements CompletionInterfaceExtended {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private String currentPref = "";
    private String originalSuff = "";
    private String originalPref = "";
    private String currentWord = "";

    private RGui rgui;

    public CompletionInterfaceAbstract(RGui rgui) {
        this.rgui = rgui;
    }

    public int countQuotes(String text) {
        int b0 = 0;
        int b1 = 0;
        int lastQuoteIndex = -1;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '"') {
                b0 = (b0 == 0 ? 1 : 0);
                lastQuoteIndex = i;
            }

            if (c == '\'') {
                b1 = (b1 == 0 ? 1 : 0);
                lastQuoteIndex = i;
            }
        }

        return ((b0 != 0 || b1 != 0) ? lastQuoteIndex : -1);
    }

    private CompletionResult getCompletionList(String x) {

        CompletionResult result = new CompletionResult();

        try {
            if (rgui != null && rgui.obtainR() != null) {

                x = x.replaceAll("\"", "\\\\\"");

                String cmd = ".PrivateEnv$doComplete(\"" + x + "\")";

                RObject object = rgui.obtainR().getObject(cmd);

                if (object != null && object instanceof RList) {

                    //RChar rchar = (RChar) object;
                    RList listObject = (RList) object;

                    RChar addition = (RChar) listObject.getValueByName("addition");

                    if (addition != null && addition.getValue() != null) {
                        result.setAddition(addition.getValue()[0]);
                    }

                    RChar names = (RChar) listObject.getValueByName("names");

                    String[] values = names.getNames();
                    String[] classes = names.getValue();

                    if (values.length != classes.length) {
                        throw new RuntimeException("values.length=" + values.length +
                                " classes.lengt=" + classes.length);
                    }

                    for (int i=0;i<values.length;i++) {
                        result.add(new CompletionItem(values[i], classes[i]));
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Error!", ex);
        }

        return result;
    }

    public CompletionResult provideResult() {

        String text = getText();
        String patternStr;
        String stringToMatch;

        // get caret position
        int caretPosition = getCaretPosition();

        originalPref = text.substring(0,caretPosition);
        originalSuff = text.substring(caretPosition);

        // find the word being completed
        int quotes = countQuotes(originalPref);

        if (quotes != -1) {

            stringToMatch = originalPref.substring(quotes);
            patternStr = "([\\S]*)$";

            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(stringToMatch);

            matcher.find();

            int foundWordIndex = matcher.start();

            int offsetindex = originalPref.length() - stringToMatch.length();

            String start = stringToMatch.substring(foundWordIndex,foundWordIndex + 1);

            String prefixQuotes = "\"";

            if (start.equals("\"") || start.equals("\'")) {
                offsetindex++;
                prefixQuotes = "";
            }

            // figure the actual prefix string
            currentPref = originalPref.substring(0,offsetindex + foundWordIndex);
            currentWord = stringToMatch.substring(foundWordIndex);
            currentWord = prefixQuotes + currentWord;

        } else {
            stringToMatch = originalPref;
            patternStr = "([\\w\\.\\:\\$\\#\\@\\]\\[]*)$";

            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(stringToMatch);

            matcher.find();

            int foundWordIndex = matcher.start();

            // figure the actual prefix string
            currentPref = stringToMatch.substring(0,foundWordIndex);
            currentWord = originalPref;
        }

        CompletionResult list = getCompletionList(currentWord);

        return list;
    }

    public String providePattern() {

        // get text
        String text = getText();

        // get caret position
        int caretPosition = getCaretPosition();

        // split the text into 2 parts
        // before and after the caret
        text = text.substring(0, caretPosition);

        // if the text is being removed
        // close the completion popup list
        //
        // can behave differently though, e.g.
        // auto-complete in real-time as the text is being edited
        //
        if (text.length() < originalPref.length())
        {
            closePopup();
            return "";
        }

        // get the patter from the text it should
        // begin right after the "prefix" string
        String pattern = text.substring(currentPref.length());

        return (pattern);
    }

    public void makeAddition(final String string) {
        String text = getText();
        int cater = getCaretPosition();

        String startstr = text.substring(0, cater);
        String endstr = text.substring(cater);

        setText( startstr + string + endstr );
        setCaretPosition(startstr.length() + string.length());
    }

    public void acceptResult(final String string, final int offset) {
        setText( currentPref + string + originalSuff );
        setCaretPosition(currentPref.length() + string.length() + offset);
        currentPref = "";
        originalSuff = "";
        originalPref = "";
    }

    public void handlePopupShowed(){
    }
    public void handlePopupClosed(){
    }

}


