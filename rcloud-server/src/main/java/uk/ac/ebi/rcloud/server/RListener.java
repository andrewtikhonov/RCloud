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

import uk.ac.ebi.rcloud.server.callback.RAction;
import uk.ac.ebi.rcloud.server.callback.RActionConst;
import uk.ac.ebi.rcloud.server.callback.RActionType;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.cluster.impl.RClusterImpl;
import uk.ac.ebi.rcloud.server.cluster.impl.RSnowClusterImpl;

public abstract class RListener {
    
    final private static Logger log = LoggerFactory.getLogger(RListener.class);
    
	public static String[] _forbiddenSymbols = null;

	public static String[] forbiddenSymbols(String voidStr) {
		if (_forbiddenSymbols == null) {
			Vector<String> v = new Vector<String>();
			v.addAll(DirectJNI.getInstance().getBootStrapRObjects());
			v.add("ls");
			v.add("objects");
			v.add("q");
			v.add("win.graph");
			v.add("x11");
			v.add("X11");
			v.add("dev.off");
			v.add("graphics.off");
			v.add("dev.set");
			v.add("help");
			v.add("setwd");
			_forbiddenSymbols = (String[]) v.toArray(new String[0]);
		}
		return _forbiddenSymbols;
	}

	public static String[] help(String path) {
        try {
            DirectJNI.getInstance().notifyRActionListeners(new RAction(RActionType.HELP).
                    put(RActionConst.URL, DirectJNI.getInstance().cutRHomeFromPath(path)));

        } catch (Exception e) {
            log.error("Error!", e);
        }

		return null;
	}

	public static String[] browse(String url) {
        try {
            DirectJNI.getInstance().notifyRActionListeners(new RAction(RActionType.BROWSE).
                    put(RActionConst.URL, url));

        } catch (Exception e) {
            log.error("Error!", e);
        }

		return null;
	}

	public static String[] q(String save, String status, String runLast) {
		DirectJNI.getInstance().notifyRActionListeners(
                new RAction(RActionType.QUIT).
                        put(RActionConst.SAVE, save).
                        put(RActionConst.STATUS, status).
                        put(RActionConst.RUNLAST, runLast));
		return null;
	}

    public static String[] edit(String function) {
        DirectJNI.getInstance().notifyRActionListeners(new RAction(RActionType.EDIT).
                put(RActionConst.FUNCTION, function));
        return null;
    }

    public static String[] exec(String command, String interpret) {

        log.info("RListener - calling DirectJNI.getInstance().exec(" + command + ")");

        String[] result = DirectJNI.getInstance().exec(command, Boolean.parseBoolean(interpret),
                !Boolean.parseBoolean(interpret));

        log.info("RListener - DirectJNI returned");

        return result;
    }

    public static String[] getProperty(String propery) {
        return new String[] { System.getProperty(propery) };
    }

	private static Vector<String> list = null;

	public static String[] listLightPacks(String v) {

		if (list == null) {

			list = new Vector<String>();
			URL jarURL = null;
			StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
			while (st.hasMoreTokens()) {
				String pathElement = st.nextToken();
				if (pathElement.endsWith("RJB.jar")) {
					try {
						jarURL = (new URL("jar:file:" + pathElement.replace('\\', '/') + "!/"));
					} catch (Exception e) {
                        log.error("Error!", e);
					}
					break;
				}
			}

			if (jarURL != null) {
				try {
					JarURLConnection jarConnection = (JarURLConnection) jarURL.openConnection();
					JarFile jarfile = jarConnection.getJarFile();
					Enumeration<JarEntry> enu = jarfile.entries();
					while (enu.hasMoreElements()) {
						String entry = enu.nextElement().toString();
						if (entry.startsWith("monoscriptpackage") && entry.endsWith(".r"))
							list.add(entry.substring("monoscriptpackage".length() + 1, entry.length() - 2));
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}

		}
		return new String[] { "OK", convertToPrintCommand(list.toString()) };

	}

	public static void stopAllClusters() {
        RClusterImpl.stopAllClusters();
        RSnowClusterImpl.stopAllClusters();
	}

	public static String convertToPrintCommand(String s) {
		if (s.length() == 0)
			return "";
		StringBuffer result = new StringBuffer();
		result.append("print(\"");
		for (int i = 0; i < s.length(); ++i) {
			char si = s.charAt(i);
			if (si == '\n') {
				if (i == s.length() - 1) {

				} else {
					result.append("\",quote=FALSE);print(\"");
				}
			} else if (si == '\"') {
				result.append("\\'");
			} else if (si == '\t') {
				result.append("    ");
			} else if (si == '\r') {
				result.append("");
			} else if (si == '\\') {
				result.append("/");
			} else {
				result.append(si);
			}
		}
		result.append("\",quote=FALSE);");
		return result.toString();
	}

	public static void pager(String fileName, String header, String title, String deleteFile) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();

		byte[] buffer = null;
		try {
			RandomAccessFile raf = new RandomAccessFile(fileName, "r");
			buffer = new byte[(int) raf.length()];
			raf.readFully(buffer);
			raf.close();
		} catch (Exception e) {
            log.error("Error!", e);
		}

		DirectJNI.getInstance().notifyRActionListeners(new RAction(RActionType.PAGER)
                .put(RActionConst.FILENAME, new File(fileName).getName())
                .put(RActionConst.CONTENT, buffer)
                .put(RActionConst.HEADER, header)
                .put(RActionConst.TITLE, title)
                .put(RActionConst.DELETEFILE, new Boolean(deleteFile)));
	}

    public static void systemexit(int status) {
        System.exit(status);
    }
    
    // SERVER INFO
    //
    //
    public static String[] getServerName() {
        return new String[] { DirectJNI.getInstanceName() };
    }


	//   S N O W   C L U S T E R
    //
    //
    public static String[] makeClusterNode(String nametemplate, String shortpoolname,
                                           String masterhostname, long port, String outfile) {
        return RSnowClusterImpl.makeClusterNode(nametemplate,
                shortpoolname, masterhostname, port, outfile);
    }

    public static String[] releaseClusterNode(String nodename) {
        return RSnowClusterImpl.releaseClusterNode(nodename);
    }

    public static String[] getClusterNodes(String foo) {
        return RSnowClusterImpl.getClusterNodes(foo);
    }

    //   O L D   C L U S T E R
    //
    //

    public static String[] makeCluster(long n, String poolName) {
        return RClusterImpl.makeCluster(n, poolName);
    }

    public static String[] setClusterProperties(String gprops) {
        return RClusterImpl.setClusterProperties(gprops);
    }

    public static String[] clusterApply(String cl, String varName, String functionName) {
        return RClusterImpl.clusterApply(cl, varName, functionName);
    }

    public static String[] clusterEvalQ(String cl, String expression) {
        return RClusterImpl.clusterEvalQ(cl, expression);
    }

    public static String[] clusterExport(String cl, String exp, String ato) {
        return RClusterImpl.clusterExport(cl, exp, ato);
    }

}
