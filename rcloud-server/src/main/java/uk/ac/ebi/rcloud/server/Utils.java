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
package uk.ac.ebi.rcloud.server;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class Utils {
    final private static Logger log = LoggerFactory.getLogger(Utils.class);

	public static Vector<String> getVectorFromSet(Set<String> s) {
		Vector<String> result = new Vector<String>();
		for (Iterator<String> iter = s.iterator(); iter.hasNext();) {
			result.add((String) iter.next());
		}
		return result;
	}

	public static String captalizeFirstChar(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	public static String retro(Registry registry, Remote servant) throws Exception {
		String[] names = registry.list();
		for (int i = 0; i < names.length; ++i)
			if (registry.lookup(names[i]).equals(servant))
				return names[i];
		return null;
	}

	public static String retro(String registryHost, int registryPort, Remote servant) throws Exception {
		return retro(LocateRegistry.getRegistry(registryHost, registryPort), servant);
	}

	public static StringBuffer getFileAsStringBuffer(String fileName) throws Exception {
		StringBuffer result = new StringBuffer();
		BufferedReader breader = new BufferedReader(new FileReader(fileName));
		String line;
		do {
			line = breader.readLine();
			if (line != null) {
				result.append(line);
				result.append('\n');
			}
		} while (line != null);
		breader.close();
		return result;
	}

	public static void catchNodes(Node node, String nameToCatch, Vector<Node> result) throws DOMException {
		if (node == null)
			return;
		if (node.getNodeName().equals(nameToCatch)) {
			result.add(node);
		}
		if (node.hasChildNodes())
			for (int i = 0; i < node.getChildNodes().getLength(); i++)
				catchNodes(node.getChildNodes().item(i), nameToCatch, result);
	}

	public static Node catchNode(Node node, String nameToCatch) throws DOMException {
		if (node.getNodeName().equals(nameToCatch)) {
			return node;
		}
		if (node.hasChildNodes())
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				Node result = catchNode(node.getChildNodes().item(i), nameToCatch);
				if (result != null)
					return result;
			}
		return null;
	}

	public static String getGetterName(Field field) {

		return (field.getClass().equals(Boolean.class) ? "is" + Utils.captalizeFirstChar(field.getName()) : "get") + Utils.captalizeFirstChar(field.getName());

	}

	public static String indentS4Print(String p) {

		BufferedReader br = new BufferedReader(new StringReader(p));

		StringBuffer result = new StringBuffer();
		int indent = -1;
		String line = null;
		String linePrec = null;

		do {
			try {
				line = br.readLine();
			} catch (Exception e) {
                log.error("Error!", e);
			}
			if (line != null) {
				if (line.startsWith("An object of class"))
					++indent;
				if (line.equals("") && linePrec != null && linePrec.equals(""))
					--indent;
				for (int i = 0; i < indent; ++i)
					result.append("\t");
				result.append(line);
				result.append("\n");
				linePrec = line;
			}

		} while (line != null);
		return result.toString();

	}

	public static String indent(String p, int indent) {
		if (p == null)
			return p;
		BufferedReader br = new BufferedReader(new StringReader(p));
		StringBuffer result = new StringBuffer();
		String line = null;
		do {
			try {
				line = br.readLine();
			} catch (Exception e) {
                log.error("Error!", e);
			}
			if (line != null) {
				for (int i = 0; i < indent; ++i)
					result.append("\t");
				result.append(line);
				result.append("\n");
			}

		} while (line != null);
		return result.toString();
	}

	static class StreamGobbler extends Thread {
		InputStream is;

		String type;

		OutputStream os;

		StreamGobbler(InputStream is, String type) {
			this(is, type, null);
		}

		StreamGobbler(InputStream is, String type, OutputStream redirect) {
			this.is = is;
			this.type = type;
			this.os = redirect;
		}

		public void run() {
			try {
				PrintWriter pw = null;
				if (os != null)
					pw = new PrintWriter(os);

				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (pw != null)
						pw.println(line);
                    log.info(type + ">" + line);
				}
				if (pw != null)
					pw.flush();
			} catch (IOException ioe) {
                log.error("Error!", ioe);
			}
		}
	}

	public static void exec(String command[], String fileNameOut, String fileNameErr) {
		try {
			FileOutputStream errf = fileNameErr == null ? null : new FileOutputStream(fileNameErr);
			FileOutputStream outf = fileNameOut == null ? null : new FileOutputStream(fileNameOut);
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);

			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", errf);

			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", outf);

			errorGobbler.start();
			outputGobbler.start();

			int exitVal = proc.waitFor();
            log.info("ExitValue: " + exitVal);

			if (errf != null) {
				errf.flush();
				errf.close();
			}
			if (outf != null) {
				outf.flush();
				outf.close();
			}

		} catch (Throwable t) {
            log.error("Error!", t);
		}
	}

}
