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
package workbench.views.benchlogviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.manager.logman.WorkbenchLogListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 5, 2010
 * Time: 3:40:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogViewerListener implements WorkbenchLogListener {

    private static Logger log = LoggerFactory.getLogger(LogViewerListener.class);

    private JTextPane logarea;

    public LogViewerListener(JTextPane logarea) {
        this.logarea = logarea;
    }

    public void flush() throws Exception {
    }

    class TextWriteRunnable implements Runnable {
        private String text;
        public TextWriteRunnable(String text) {
            this.text = text;
        }
        public void run(){
            if (logarea != null) {
                try {
                    Document doc = logarea.getDocument();
                    doc.insertString(doc.getLength(), text, null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void write(String text) {
        EventQueue.invokeLater(new TextWriteRunnable(text));
    }
    
}