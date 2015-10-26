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

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by andrew on 13/10/15.
 */
public class IOUtil {

    public static void redirectIO() {
   		final JTextArea area = new JTextArea();
   		JFrame f = new JFrame("out/err");
   		f.add(new JScrollPane(area), BorderLayout.CENTER);
   		f.pack();
   		f.setVisible(true);
   		f.setSize(500, 500);
   		f.setLocation(100, 100);

   		PrintStream ps = new PrintStream(new OutputStream() {
   			public void write(final int b) throws IOException {
   				SwingUtilities.invokeLater(new Runnable() {
   					public void run() {
   						area.setText(area.getText() + new String(new byte[] { (byte) b }));
   					}
   				});
   				SwingUtilities.invokeLater(new Runnable() {
   					public void run() {
   						area.setCaretPosition(area.getText().length());
   						area.repaint();
   					}
   				});
   			}

   			public void write(final byte[] b) throws IOException {

   				SwingUtilities.invokeLater(new Runnable() {
   					public void run() {
   						area.setText(area.getText() + new String(b));
   					}
   				});
   				SwingUtilities.invokeLater(new Runnable() {
   					public void run() {
   						area.setCaretPosition(area.getText().length());
   						area.repaint();
   					}
   				});
   			}

   			public void write(byte[] b, int off, int len) throws IOException {
   				final byte[] r = new byte[len];
   				for (int i = 0; i < len; ++i)
   					r[i] = b[off + i];

   				SwingUtilities.invokeLater(new Runnable() {
   					public void run() {
   						area.setText(area.getText() + new String(r));
   					}
   				});
   				SwingUtilities.invokeLater(new Runnable() {
   					public void run() {
   						area.setCaretPosition(area.getText().length());
   						area.repaint();
   					}
   				});
   			}
   		});
   		System.setOut(ps);
   		System.setErr(ps);
   	}

}
