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
package workbench.manager.logman;

import org.apache.log4j.WriterAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 5, 2010
 * Time: 2:24:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkbenchLogAppender extends WriterAppender {

    final private static Logger log = LoggerFactory.getLogger(WorkbenchLogAppender.class);

    private static Vector<WorkbenchLogListener> logListeners = new Vector<WorkbenchLogListener>();

    public WorkbenchLogAppender() {
        setWriter(createWriter(new WorkbenchLogStream(logListeners)));
    }

    public static void addLogListener(WorkbenchLogListener listener) {
        if (listener != null) {
            logListeners.add(listener);
        }
    }

    public static void removeLogListener(WorkbenchLogListener listener) {
        logListeners.remove(listener);
    }

    public static void removeAllLogListeners() {
        logListeners.removeAllElements();
    }

}
