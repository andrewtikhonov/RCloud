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
package uk.ac.ebi.rcloud.rpf;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.LogLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoteAppender extends WriterAppender {

    final private static Logger log = LoggerFactory.getLogger(RemoteAppender.class);

	private static Vector<RemoteLogListener> logListeners = new Vector<RemoteLogListener>();

	public static void addLogListener(RemoteLogListener listener) {
        if (listener != null) {
            logListeners.add(listener);
        }
	}

	public static void removeLogListener(RemoteLogListener listener) {
		logListeners.remove(listener);
	}

	public static void removeAllLogListeners() {
		logListeners.removeAllElements();
    }

    public RemoteAppender() {
        super();
	    setWriter(createWriter(new RemoteLoggerStream()));
    }

	private static class RemoteLoggerStream extends OutputStream {
		public RemoteLoggerStream() {
		}

		public void close() {
		}

        private void notifyListeners(String text) {
            if (logListeners.size() == 0)
                return;
            Vector<RemoteLogListener> logListenersToRemove = new Vector<RemoteLogListener>();
            for (int i = 0; i < logListeners.size(); ++i) {
                try {
                    logListeners.elementAt(i).write(text);
                } catch (RemoteException e) {
                    logListenersToRemove.add(logListeners.elementAt(i));
                }
            }
            logListeners.removeAll(logListenersToRemove);

        }

		public void flush() {}

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
}
