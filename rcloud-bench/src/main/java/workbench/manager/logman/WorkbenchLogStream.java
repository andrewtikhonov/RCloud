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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 5, 2010
 * Time: 3:07:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkbenchLogStream extends OutputStream {

    private static Logger log = LoggerFactory.getLogger(WorkbenchLogStream.class);

    private Vector<WorkbenchLogListener> listeners;

    public WorkbenchLogStream(Vector<WorkbenchLogListener> listeners) {
        this.listeners = listeners;
    }

    public void close() {
    }

    public void flush() {}

    private void notifyListeners(String text) {
        if (listeners.size() == 0)
            return;
        Vector<WorkbenchLogListener> listenersToRemove = new Vector<WorkbenchLogListener>();
        for (int i = 0; i < listeners.size(); ++i) {
            try {
                listeners.elementAt(i).write(text);
            } catch (Exception e) {
                listenersToRemove.add(listeners.elementAt(i));
            }
        }
        listeners.removeAll(listenersToRemove);
    }

    public void write(final byte[] b) throws IOException {
        notifyListeners(new String(b));
    }

    public void write(final byte[] b, final int off, final int len) throws IOException {
        notifyListeners(new String(b, off, len));
    }

    public void write(final int b) throws IOException {
        notifyListeners(new String(new byte[] { (byte) b, (byte) (b >> 8) }));
    }

}
