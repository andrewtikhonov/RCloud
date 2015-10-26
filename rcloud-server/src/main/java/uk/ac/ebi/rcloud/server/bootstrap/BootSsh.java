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
package uk.ac.ebi.rcloud.server.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class BootSsh {
    final private static Logger log = LoggerFactory.getLogger(BootSsh.class);

	public static final String STUB_BEGIN_MARKER = "#STUBBEGIN#";
	public static final String STUB_END_MARKER = "#STUBEND#";

	public static final String PROCESS_ID_BEGIN_MARKER = "#PROCESSIDBEGIN#";
	public static final String PROCESS_ID_END_MARKER = "#PROCESSIDEND#";

	public static final String R_PROCESS_ID_BEGIN_MARKER = "#RPROCESSIDBEGIN#";
	public static final String R_PROCESS_ID_END_MARKER = "#RPROCESSIDEND#";
	
	public static final String NO_NAME="NULLNAME";

	public static void main(String[] args) throws Exception {

		try {
			
			String logFileName = args[7];
			
			String name = args[8].equals(NO_NAME) ? null : args[8];


			PrintStream bw = null;
			if (logFileName.equals("System.out")) {
				bw = System.out;
			} else {
				bw = new PrintStream(new File(logFileName));
			}

			Vector<URL> codeUrls = new Vector<URL>();
			if (args.length > 9) {
				for (int i = 9; i < args.length; ++i) {
					codeUrls.add(new URL(args[i]));
				}
			}

			try {

				URL classServerUrl = new URL("http://" + args[1] + ":" + args[2] + "/classes/");

				bw.println(classServerUrl);

                URLClassLoader cl = new URLClassLoader(new URL[] { classServerUrl }, BootSsh.class.getClassLoader());

                cl.loadClass("uk.ac.ebi.rcloud.server.manager.ServerManager").
                        getMethod("startPortInUseDogwatcher", new Class<?>[] { String.class, int.class, int.class, int.class })
                        .invoke(null, args[1], Integer.decode(args[2]), 3, 3);

                Class<?> ServerLauncherClass = cl.loadClass("uk.ac.ebi.rcloud.server.manager.ServerManager");

                Remote r = (Remote) ServerLauncherClass.getMethod(
						"createR",
						new Class<?>[] { boolean.class, String.class, int.class,  Properties.class,  int.class, int.class, String.class, boolean.class,
								URL[].class, String.class }).invoke(
						null,
						new Object[] { new Boolean(args[0]).booleanValue(), args[1], Integer.decode(args[2]).intValue(), stringToProperties(args[3]), Integer.decode(args[5]).intValue(), Integer.decode(args[6]).intValue(), name, false,
								(URL[]) codeUrls.toArray(new URL[0]), args[4] });


                Class<?> poolUtilsClass = cl.loadClass("uk.ac.ebi.rcloud.rpf.PoolUtils");

                String processId = (String) poolUtilsClass.getMethod("getProcessId", new Class<?>[0]).invoke(null, new Object[0]);

                bw.println(PROCESS_ID_BEGIN_MARKER + processId + PROCESS_ID_END_MARKER);

                bw.println(R_PROCESS_ID_BEGIN_MARKER + (String) r.getClass().getMethod("getProcessId", new Class<?>[0]).invoke(r, new Object[0])
						+ R_PROCESS_ID_END_MARKER);

                bw.println(STUB_BEGIN_MARKER + stubToHex(r) + STUB_END_MARKER);
			} catch (Exception e) {
                log.error("Error!", e);

			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}

			}

		} catch (Throwable e) {
            log.error("Error!", e);
            log.info("Throwable");new BufferedReader(new InputStreamReader(System.in)).readLine();
			
		} finally {
			System.exit(0);
		}
	}

	public static String stubToHex(Remote obj) throws NoSuchObjectException {
		if (obj instanceof UnicastRemoteObject) {
			obj = java.rmi.server.RemoteObject.toStub(obj);
		}
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baoStream).writeObject(obj);
		} catch (Exception e) {
            log.error("Error!", e);
		}
		String stub_hex = bytesToHex(baoStream.toByteArray());
		return stub_hex;
	}

	public static String bytesToHex(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;
		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);
		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(pseudo[(int) ch]);
			ch = (byte) (in[i] & 0x0F);
			out.append(pseudo[(int) ch]);
			i++;
		}
		String rslt = new String(out);
		return rslt;
	}
	
	
	public static Properties stringToProperties(String parametersStr) {		
		StringTokenizer st = new StringTokenizer(parametersStr, "~/~"); 
		Properties result = new Properties();	
		while (st.hasMoreElements()) {
			try {
				String element=(String)st.nextElement();
				int p=element.indexOf('=');
				if (p==-1) {
					result.put(element,null);
				}
				else {
					result.put(element.substring(0,p).trim(), element.substring(p+1, element.length()).trim());
				}
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
		return result;
	}
	
	public static String propertiesToString(Properties props) {
		String result="";
		for (Object k:props.keySet()) {
			result=result+k+"="+props.getProperty((String)k)+"~/~";
		}
		
		if (result.length()>0) {
			result=result.substring(0,result.length()-"~/~".length());
		}
	
		return result;
	}

}
