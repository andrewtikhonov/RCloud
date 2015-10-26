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
import workbench.util.DocumentLine;
import workbench.util.EditorUtil;
import workbench.util.LineIterator;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;
import java.awt.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 17, 2009
 * Time: 4:18:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSyntaxDocument extends DefaultStyledDocument implements FormattedDocument {

    final static private Logger log = LoggerFactory.getLogger(RSyntaxDocument.class);

    private DefaultStyledDocument doc;
    private Element rootElement;

    private boolean multiLineComment;
    private MutableAttributeSet normal;
    private MutableAttributeSet keyword;
    private MutableAttributeSet comment;
    private MutableAttributeSet operator;
    private MutableAttributeSet digit;
    private MutableAttributeSet quote;

    private HashSet keywords = null;
    private JTextPane textpane;

    private BracketHighlighter bracketLighter = new BracketHighlighter(this);

    private UndoableEditSupport undoSupport = new UndoableEditSupport();
    private UndoManager undoManager       = new UndoManager();

    private boolean enableChangesRecording = false;

    private boolean requestFormatting = false;
    private String CALLBACK = "CALLBACK";

    public RSyntaxDocument(JTextPane textpane) {
        this.doc = this;

        this.textpane = textpane;
        this.textpane.addCaretListener(bracketLighter);

        this.rootElement = doc.getDefaultRootElement();
        putProperty( DefaultEditorKit.EndOfLineStringProperty, "\n" );

        this.normal = new SimpleAttributeSet();
        StyleConstants.setForeground(normal, Color.black);

        this.comment = new SimpleAttributeSet();
        StyleConstants.setForeground(comment, Color.gray);
        StyleConstants.setItalic(comment, true);

        this.keyword = new SimpleAttributeSet();
        StyleConstants.setForeground(keyword, Color.blue.darker().darker());
        StyleConstants.setBold(keyword, true);

        this.quote = new SimpleAttributeSet();
        StyleConstants.setForeground(quote, Color.red.darker().darker());

        this.operator = new SimpleAttributeSet();
        StyleConstants.setForeground(operator, Color.blue.darker().darker());

        this.digit = new SimpleAttributeSet();
        StyleConstants.setForeground(digit, Color.green.darker().darker());

        this.keywords = RSyntax.keywords();

        undoSupport.addUndoableEditListener(new UndoableSupportListener());
        addUndoableEditListener(new UndoableDocumentEditListener());

    }

    class UndoableDocumentEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent undoableeditevent) {
            if (enableChangesRecording &&
                    undoableeditevent.getEdit().isSignificant()) {
                undoSupport.postEdit(undoableeditevent.getEdit());
            }
        }
    }

    class UndoableSupportListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent undoableeditevent) {
            undoManager.addEdit(undoableeditevent.getEdit());
        }
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void requestFormatting() {
        requestFormatting = true;
    }

    /*
     *  Override to apply syntax highlighting after the document has been updated
     */

    public void setEnableChangesRecording(boolean enableChangesRecording) {
        this.enableChangesRecording = enableChangesRecording;

        //new Exception().printStackTrace();
        //log.info("setEnableChangesRecording-enableChangesRecording="+enableChangesRecording);
    }

    public void processChanges(final Vector<RSyntaxDocumentChange> changes, final AttributeSet a)
            throws BadLocationException {

        Object obj = getProperty(CALLBACK);

        if (obj != null) {
            ((Runnable) obj).run();
        }

        int startoffset = RSyntaxDocument.this.getLength();
        int totallength = 0;

        setEnableChangesRecording(true);
        undoSupport.beginUpdate();

        for(RSyntaxDocumentChange ch: changes) {
            switch (ch.op) {
                case RSyntaxDocumentChange.INSERT:
                    RSyntaxDocument.super.insertString(ch.offset, ch.str, a);
                    totallength += ch.str.length();

                    break;
                case RSyntaxDocumentChange.REMOVE:
                    RSyntaxDocument.super.remove(ch.offset, ch.length);
                    totallength -= ch.length;

                    break;
                default:
                    log.error("Error! Unsupported RSyntaxDocumentChange Operation ", ch.op);
            }

            if (startoffset > ch.offset) {
                startoffset = ch.offset;
            }
        }

        undoSupport.endUpdate();
        setEnableChangesRecording(false);

        processChangedLines(startoffset, totallength);
    }

    public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException {

        Integer removeOffset = null;

        Object obj = getProperty(CALLBACK);

        if (obj != null) {
            ((Runnable) obj).run();
        }

        str = str.replaceAll("\t", "    ");

        if (requestFormatting) {

            String beginningOfLine = "";

            Element el = rootElement.getElement(
                    rootElement.getElementIndex( offset ) );

            int removeOffsetLocal = el.getStartOffset();

            if (removeOffsetLocal != offset) {
                removeOffset = removeOffsetLocal;

                try {
                    beginningOfLine = doc.getText(removeOffset, offset - removeOffset);
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }

                //str = beginningOfLine + str;
            }


            str = formatLines(offset, str, beginningOfLine);
        }

        setEnableChangesRecording(true);
        undoSupport.beginUpdate();

        //super.insertString(offset, str, a);
        

        if (removeOffset != null) {
            super.remove(removeOffset, offset - removeOffset);
            super.insertString(removeOffset, str, a);
        } else {
            super.insertString(offset, str, a);
        }


        undoSupport.endUpdate();
        setEnableChangesRecording(false);

        processChangedLines(offset, str.length());

        if (requestFormatting) {
            requestFormatting = false;
        }
    }

    /*
     *  Override to apply syntax highlighting after the document has been updated
     */
    public void replace(int offset, int length, String str, AttributeSet a)
            throws BadLocationException {

        Integer removeOffset = null;

        Object obj = getProperty(CALLBACK);

        if (obj != null) {
            ((Runnable) obj).run();
        }

        str = str.replaceAll("\t", "    ");

        if (requestFormatting) {
            String beginningOfLine = "";

            Element el  = rootElement.getElement(
                    rootElement.getElementIndex( offset ) );

            int removeOffsetLocal = el.getStartOffset();

            if (removeOffsetLocal != offset) {
                removeOffset = removeOffsetLocal;

                try {
                    beginningOfLine = doc.getText(removeOffset, offset - removeOffset);
                } catch (BadLocationException ble) {
                    log.error("Error!", ble);
                }

                //str = beginningOfLine + str;
            }

            str = formatLines(offset, str, beginningOfLine);
        }

        setEnableChangesRecording(true);
        undoSupport.beginUpdate();

        super.remove(offset, length);
        //super.insertString(offset, str, a);



        if (removeOffset != null) {
            super.remove(removeOffset, offset - removeOffset);
            super.insertString(removeOffset, str, a);
        } else {
            super.insertString(offset, str, a);
        }


        undoSupport.endUpdate();
        setEnableChangesRecording(false);

        processChangedLines(offset, str.length());

        if (requestFormatting) {
            requestFormatting = false;
        }
    }

    /*
     *  Override to apply syntax highlighting after the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {

        Object obj = getProperty(CALLBACK);

        if (obj != null) {
            ((Runnable) obj).run();
        }

        setEnableChangesRecording(true);
        undoSupport.beginUpdate();

        super.remove(offset, length);

        undoSupport.endUpdate();
        setEnableChangesRecording(false);

        processChangedLines(offset, 0);
    }

    /*
     *  Determine how many lines have been changed,
     *  then apply highlighting to each line
     */
    public void processChangedLines(int offset, int length)
        throws BadLocationException {
        String content = doc.getText(0, doc.getLength());

        //  The lines affected by the latest document update

        int startLine = rootElement.getElementIndex( offset );
        int endLine = rootElement.getElementIndex( offset + length );

        //  Make sure all comment lines prior to the start line are commented
        //  and determine if the start line is still in a multi line comment

        setMultiLineComment( commentLinesBefore( content, startLine ) );

        //  Do the actual highlighting

        for (int i = startLine; i <= endLine; i++) {
            applyHighlighting(content, i);
        }

        //  Resolve highlighting to the next end multi line delimiter

        if (isMultiLineComment())
            commentLinesAfter(content, endLine);
        else
            highlightLinesAfter(content, endLine);
    }

    /*
     *  Highlight lines when a multi line comment is still 'open'
     *  (ie. matching end delimiter has not yet been encountered)
     */
    private boolean commentLinesBefore(String content, int line) {

        int offset = rootElement.getElement( line ).getStartOffset();

        //  Start of comment not found, nothing to do

        int startDelimiter = lastIndexOf( content, getStartDelimiter(), offset - 2 );

        if (startDelimiter < 0)
            return false;

        //  Matching start/end of comment found, nothing to do

        int endDelimiter = indexOf( content, getEndDelimiter(), startDelimiter );

        if (endDelimiter < offset & endDelimiter != -1)
            return false;

        //  End of comment not found, highlight the lines

        doc.setCharacterAttributes(startDelimiter, offset - startDelimiter + 1, comment, false);
        return true;
    }

    /*
     *  Highlight comment lines to matching end delimiter
     */
    private void commentLinesAfter(String content, int line) {

        int offset = rootElement.getElement( line ).getEndOffset();

        //  End of comment not found, nothing to do

        int endDelimiter = indexOf( content, getEndDelimiter(), offset );

        if (endDelimiter < 0)
            return;

        //  Matching start/end of comment found, comment the lines

        int startDelimiter = lastIndexOf( content, getStartDelimiter(), endDelimiter );

        if (startDelimiter < 0 || startDelimiter <= offset)
        {
            doc.setCharacterAttributes(offset, endDelimiter - offset + 1, comment, false);
        }
    }

    /*
     *  Highlight lines to start or end delimiter
     */
    private void highlightLinesAfter(String content, int line)
        throws BadLocationException {

        int offset = rootElement.getElement( line ).getEndOffset();

        //  Start/End delimiter not found, nothing to do

        int startDelimiter = indexOf( content, getStartDelimiter(), offset );
        int endDelimiter = indexOf( content, getEndDelimiter(), offset );

        if (startDelimiter < 0)
            startDelimiter = content.length();

        if (endDelimiter < 0)
            endDelimiter = content.length();

        int delimiter = Math.min(startDelimiter, endDelimiter);

        if (delimiter < offset)
            return;

        //	Start/End delimiter found, reapply highlighting

        int endLine = rootElement.getElementIndex( delimiter );

        for (int i = line + 1; i < endLine; i++) {

            Element branch = rootElement.getElement( i );
            Element leaf = doc.getCharacterElement( branch.getStartOffset() );
            AttributeSet as = leaf.getAttributes();

            if ( as.isEqual(comment) )
                applyHighlighting(content, i);
        }
    }

    /*
     *  Parse the line to determine the appropriate highlighting
     */
    private void applyHighlighting(String content, int line)
        throws BadLocationException {

        int startOffset = rootElement.getElement( line ).getStartOffset();
        int endOffset = rootElement.getElement( line ).getEndOffset() - 1;

        int lineLength = endOffset - startOffset;
        int contentLength = content.length();

        if (endOffset >= contentLength)
            endOffset = contentLength - 1;

        //  check for multi line comments
        //  (always set the comment attribute for the entire line)

        if (endingMultiLineComment(content, startOffset, endOffset)
            ||  isMultiLineComment()
            ||  startingMultiLineComment(content, startOffset, endOffset) ) {

            doc.setCharacterAttributes(startOffset, endOffset - startOffset + 1, comment, false);
            return;
        }

        //  set normal attributes for the line

        doc.setCharacterAttributes(startOffset, lineLength, normal, true);

        //  check for single line comment

        int index = content.indexOf(getSingleLineDelimiter(), startOffset);

        if ( (index > -1) && (index < endOffset) ) {
            doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
            endOffset = index - 1;
        }

        //  check for tokens

        checkForTokens(content, startOffset, endOffset);
    }

    /*
     *  Does this line contain the start delimiter
     */
    private boolean startingMultiLineComment(String content, int startOffset, int endOffset)
        throws BadLocationException {

        int index = indexOf( content, getStartDelimiter(), startOffset );

        if ( (index < 0) || (index > endOffset) )
            return false;
        else {
            setMultiLineComment( true );
            return true;
        }
    }

    /*
     *  Does this line contain the end delimiter
     */
    private boolean endingMultiLineComment(String content, int startOffset, int endOffset)
        throws BadLocationException {
        int index = indexOf( content, getEndDelimiter(), startOffset );

        if ( (index < 0) || (index > endOffset) )
            return false;
        else
        {
            setMultiLineComment( false );
            return true;
        }
    }

    /*
     *  We have found a start delimiter
     *  and are still searching for the end delimiter
     */
    private boolean isMultiLineComment() {
        return multiLineComment;
    }

    private void setMultiLineComment(boolean value) {
        multiLineComment = value;
    }

    /*
     *	Parse the line for tokens to highlight
     */
    private void checkForTokens(String content, int startOffset, int endOffset) {
        while (startOffset <= endOffset) {
            //  skip the delimiters to find the start of a new token

            char c = content.charAt(startOffset);

            if (isCharDelimiter( c )) {
                startOffset++;
                continue;
            }

            if (isCharOperator( c )) {
                startOffset = getOperator(startOffset);
                continue;
            }

            //  Extract and process the entire token
            if (Character.isDigit( c )) {
                startOffset = getDigitToken(content, startOffset, endOffset);
                continue;
            }

            if (isCharQuoteDelimiter( c )) {
                startOffset = getQuoteToken(content, startOffset, endOffset);
                continue;
            }
            else {
                startOffset = getOtherToken(content, startOffset, endOffset);
            }
        }
    }

    /*
     *
     */
    private int getOperator(int startOffset) {
        doc.setCharacterAttributes(startOffset, 1, operator, false);

        return startOffset + 1;
    }

    /*
     *
     */
    private int getDigitToken(String content, int startOffset, int endOffset) {
        int endOfDigit = startOffset;

        while( endOfDigit < endOffset) {
            char c = content.charAt(endOfDigit + 1);

            if (Character.isDigit(c)) {
                endOfDigit++;
            } else {
                break;
            }
        }

        doc.setCharacterAttributes(startOffset, endOfDigit - startOffset + 1, digit, false);

        return endOfDigit + 1;
    }

    private int getQuoteToken(String content, int startOffset, int endOffset) {
        char quoteDelimiter = content.charAt(startOffset);

        int endOfQuote = startOffset;

        while( endOfQuote < endOffset) {
            char c = content.charAt(endOfQuote + 1);

            if (c  == quoteDelimiter){
                endOfQuote++;
                break;
            } else if (c  == EditorUtil.ESCAPE) {
                endOfQuote += 2;
            } else {
                endOfQuote++;
            }
        }

        doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);

        return endOfQuote + 1;
    }


    /*
     *
     */
    private int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset;
        char c;

        while( endOfToken < endOffset) {
            c = content.charAt(endOfToken + 1);

            if (Character.isLetterOrDigit( c ) || c == '_') {
                endOfToken++;
            } else {
                break;
            }
        }

        String token = content.substring(startOffset, endOfToken+1);

        if ( isKeyword( token ) ) {
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset + 1, keyword, false);
        }

        return endOfToken + 1;
    }

    /*
     *  Assume the needle will the found at the start/end of the line
     */
    private int indexOf(String content, String needle, int offset) {
        int index;

        while ( (index = content.indexOf(needle, offset)) != -1 ) {
            String text = getLine( content, index ).trim();

            if (text.startsWith(needle) || text.endsWith(needle))
                break;
            else
                offset = index + 1;
        }

        return index;
    }

    /*
     *  Assume the needle will the found at the start/end of the line
     */
    private int lastIndexOf(String content, String needle, int offset) {
        int index;

        while ( (index = content.lastIndexOf(needle, offset)) != -1 ) {
            String text = getLine( content, index ).trim();

            if (text.startsWith(needle) || text.endsWith(needle))
                break;
            else
                offset = index - 1;
        }

        return index;
    }

    private String getLine(String content, int offset) {
        int line = rootElement.getElementIndex( offset );
        Element lineElement = rootElement.getElement( line );
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();
        return content.substring(start, end - 1);
    }

    /*
     *  Override for other languages
     */
    protected boolean isDelimiter(String character) {
        String operands = ",;";

        if (Character.isWhitespace( character.charAt(0) ) ||
            operands.indexOf(character) != -1)
            return true;
        else
            return false;
    }

    /*
     *  Override for other languages
     */
    protected boolean isCharDelimiter(char c) {
        return (Character.isWhitespace( c ) || c == ',' || c == ';');
    }

    /*
     *  Override for other languages
     */
    protected boolean isOperator(String character) {
        String operands = ":{}()[]+-/%<=>!&|^~*";

        if (operands.indexOf(character) != -1)
            return true;
        else
            return false;
    }

    protected boolean isCharOperator(char c) {
        String operands = ":{}()[]+-/%<=>!&|^~*";
        return (operands.indexOf(c) != -1);
    }


    /*
     *  Override for other languages
     */
    protected boolean isDigit(String character) {
        if (Character.isDigit( character.charAt(0) ))
            return true;
        else
            return false;
    }


    /*
     *  Override for other languages
     */
    protected boolean isQuoteDelimiter(String character) {
        String quoteDelimiters = "\"'";

        if (quoteDelimiters.indexOf(character) < 0)
            return false;
        else
            return true;
    }

    /*
     *  Override for other languages
     */
    protected boolean isCharQuoteDelimiter(char c) {
        return (c == '"' || c == '\'');
    }

    /*
     *  Override for other languages
     */
    protected boolean isKeyword(String token) {
        return keywords.contains( token );
    }

    /*
     *  Override for other languages
     */
    protected String getStartDelimiter() {
        return "/*";
    }

    /*
     *  Override for other languages
     */
    protected String getEndDelimiter() {
        return "*/";
    }

    /*
     *  Override for other languages
     */
    protected String getSingleLineDelimiter() {
        return "#";
    }

    /*
     *  Override for other languages
     */
    protected String getEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }

    /*
     *
     */
    protected String addMatchingBrace(int offset) throws BadLocationException {

        StringBuffer whiteSpace = new StringBuffer();
        int line = rootElement.getElementIndex( offset );
        int i = rootElement.getElement(line).getStartOffset();

        while (true) {
            String temp = doc.getText(i, 1);

            if (temp.equals(" ") || temp.equals("\t")) {
                whiteSpace.append(temp);
                i++;
            }
            else
                break;
        }

        return "{\n" + whiteSpace.toString() + "\t\n" + whiteSpace.toString() + "}";
    }

    public void highlightKeyword(final Color col,final int begin,
                                 final int length,final boolean flag,final boolean bold) {
            /*
            if (SwingUtilities.isEventDispatchThread()) {
            	setCharacterAttributes(begin, length, style, flag);
            } else {
            	SwingUtilities.invokeLater(new Runnable(){
            		public void run() {
            			setCharacterAttributes(begin, length, style, flag);
            		}
            	});
            }
            */
    }

    public String stripOffNonFunctionalChars(String line) {

        int index = 0;
        //int length = line.length();
        char removalchar = '!';
        int removalstart = -1;

        for(index = 0;index < line.length();index++) {
            char c = line.charAt(index);

            if (removalstart != -1) {
                // removal mode

                if (c == EditorUtil.ESCAPE) {
                    // skip next symbol
                    index++;
                    continue;
                }

                if (c == removalchar) {
                    // found end of removal
                    String s1 = line.substring(0, removalstart + 1);
                    String s2 = line.substring(index);

                    line = s1.concat(s2);

                    // shift reset
                    index = removalstart + 1;

                    // reset removal start
                    removalstart = -1;
                }


            } else {

                if (c == EditorUtil.QUOTE1 || c == EditorUtil.QUOTE2) {
                    removalchar = c;
                    removalstart = index;
                    continue;
                }

                if (c == EditorUtil.COMMENT) {
                    // remove the rest
                    line = line.substring(0, index).concat(line.substring(line.length()-1));
                    break;
                }
            }
        }

        return line;
    }

    public String formatLines(int offset, String s, String beginningOfLine) throws BadLocationException {

        //log.info("formatLines");

        boolean contentPresentOnLine;

        StringBuilder builder = new StringBuilder();

        // allocate text indents
        DocumentBlockIndents indents = new DocumentBlockIndents();

        String alltext = "";

        try {
            alltext = doc.getText(0, offset);
        } catch (BadLocationException ble) {
            log.error("Error!", ble);
        }

        // levels
        indents.l = DocumentBlockLevels.analyzeNestedLevels(alltext, offset - 1);

        // "-1" because otherwise, it will find "\n" instead of spaces
        contentPresentOnLine = !EditorUtil.allWhiteSpaces(beginningOfLine);

        // next indent is used to indent next
        // line (first line in our case)
        indents.nextindent = DocumentBlockIndents.computeIndentFromLevels(indents.l);

        Vector<DocumentLine> lines = new LineIterator(s).getLines();
        for (DocumentLine myLine : lines) {

            String funcLine = stripOffNonFunctionalChars(myLine.text);

            if (contentPresentOnLine) {
                contentPresentOnLine = false;

                DocumentBlockIndents.updateBlockIndents(funcLine, indents);
                builder.append(beginningOfLine);
                builder.append(myLine.text);

            } else {
                String lineindent = EditorUtil.getLineIndent(myLine.text);

                DocumentBlockIndents.updateBlockIndents(funcLine, indents);
                builder.append(indents.currentindent);
                builder.append(myLine.text.substring(lineindent.length()));
            }
        }

        return builder.toString();
    }
}
