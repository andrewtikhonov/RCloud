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
package uk.ac.ebi.rcloud.server.manager;

import ch.ethz.ssh2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.*;
import uk.ac.ebi.rcloud.rpf.exception.ServantCreationTimeout;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.bootstrap.BootSsh;
import uk.ac.ebi.rcloud.server.exception.BadSshHostException;
import uk.ac.ebi.rcloud.server.exception.BadSshLoginPwdException;
import uk.ac.ebi.rcloud.server.exception.ServantCreationFailed;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static uk.ac.ebi.rcloud.rpf.PoolUtils.isWindowsOs;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.unzip;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 16, 2010
 * Time: 1:27:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerFactory {

    private static final  Logger log = LoggerFactory.getLogger(ServerFactory.class);

    public static String INSTALL_DIR = null;
    public static final String EMBEDDED_R = "R-version-2.8.0";
    public static final int ENTRIES_NUMBER = 4832;
    private static String BOOTSTRAPPATH = "/uk/ac/ebi/rcloud/server/bootstrap";
    private static String MANAGERPATH = "/uk/ac/ebi/rcloud/server/manager";

    public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 5;
    public static int BUFFER_SIZE = 8192 * 5;
    private static final String RHOMESTART = "R$HOME$START";
    private static final String RHOMEEND = "R$HOME$END";
    private static final String RVERSTART = "R$VER$START";
    private static final String RVEREND = "R$VER$END";

	public static void main(String[] args) throws Exception {
	    log.info("started");
    }

	public static String[] namingVars = new String[] {
		"naming.mode", "registry.host", "registry.port",
		"db.type", "db.host", "db.port", "db.dir", "db.name", "db.user", "db.password",
		"httpregistry.url", "httpregistry.login", "httpregistry.password",
		"rmi.port.start",
		"job.id", "job.name", "notify.email",
		"node",
		"http.port",
		"cloud"};

	public static Properties getRegistryNamingInfo(String registryHost, int registryPort) {
		Properties result = new Properties();
		result.put("registry.host", registryHost);
		result.put("registry.port", new Integer(registryPort).toString());
		return result;
	}

	public static Properties getNamingInfo() {
		Properties result = new Properties();
		for (int i = 0; i < namingVars.length; ++i) {
			String var = namingVars[i];
			if (System.getProperty(var) != null && !System.getProperty(var).equals("")) {
				result.put(var, System.getProperty(var));
			}
		}
		return result;
	}

	/*
    public static RServices createR(String name) throws Exception {
		return createR(null, false, PoolUtils.getHostIp(),
                        0, //FIXME: LocalHttpServer.getLocalHttpServerPort(),
                        getRegistryNamingInfo(PoolUtils.getHostIp(),
                                LocalRmiRegistry.getLocalRmiRegistryPort()),
                ServerDefaults._memoryMin,
                ServerDefaults._memoryMax, name, false, null, null);
	}
	*/

	private interface ProgessLoggerInterface {
		void logProgress(String message);
	}

	synchronized public static RServices createR(int memoryMinMegabytes, int memoryMaxMegabytes, String name,
                                                 String logFile, Properties namingInfo) throws Exception {

		ProgessLoggerInterface progressLogger = new ProgessLoggerInterface() {
			public void logProgress(String message) {
                log.info(message);
			}
		};

		try {

			String[] requiredPackages = null;

			if ((System.getenv("BIOCEP_USE_DEFAULT_LIBS") != null && System.getenv("BIOCEP_USE_DEFAULT_LIBS").equalsIgnoreCase("false"))
					|| (System.getProperty("use.default.libs") != null && System.getProperty("use.default.libs").equalsIgnoreCase("true"))) {
				requiredPackages = new String[0];
			} else {
				if (isWindowsOs()) {
					requiredPackages = new String[] { "rJava", "JavaGD", "iplots", "TypeInfo", "Cairo", "svMisc" };
				} else {
					requiredPackages = new String[] { "rJava", "JavaGD", "iplots", "TypeInfo", "svMisc" };
				}
			}

			progressLogger.logProgress("Generating Bootstrap Classes..");

			String bootstrap = (INSTALL_DIR + "classes/org/kchine/r/server/manager/bootstrap").replace('\\', '/');
            log.info(bootstrap);
			if (!new File(bootstrap).exists())
				new File(bootstrap).mkdirs();
			InputStream is = ServerManager.class.getResourceAsStream("/uk/ac/ebi/rcloud/server/bootstrap/Boot.class");
			byte[] buffer = new byte[is.available()];
			try {
				for (int i = 0; i < buffer.length; ++i) {
					int b = is.read();
					buffer[i] = (byte) b;
				}
			} catch (Exception e) {
                log.error("Error!", e);
			}
			RandomAccessFile raf = new RandomAccessFile(bootstrap + "/Boot.class", "rw");
			raf.setLength(0);
			raf.write(buffer);
			raf.close();
			progressLogger.logProgress("Bootstrap Classes Generated.");

			// ---------------------------------------

			if (!isWindowsOs() && !new File(INSTALL_DIR + "VRWorkbench.sh").exists()) {
				try {

					progressLogger.logProgress("Generating Launcher Batch..");

					String launcherFile = INSTALL_DIR + "VRWorkbench.sh";
					FileWriter fw = new FileWriter(launcherFile);
					PrintWriter pw = new PrintWriter(fw);
					pw.println("javaws http://biocep-distrib.r-forge.r-project.org/rworkbench.jnlp");
					fw.close();

					progressLogger.logProgress("Launcher Batch generated..");
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}

			// ---------------------------------------

			// String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";

            String jripath = null;//rlibs + "/rJava/jri/";
            log.info("jripath:" + jripath + "\n");

			String cp = INSTALL_DIR + "classes";

			try {

				/*
				 * if (keepAlive) { try
				 * {downloadBiocepCore(PoolUtils.LOG_PRGRESS_TO_LOGGER |
				 * (showProgress ? PoolUtils.LOG_PRGRESS_TO_DIALOG : 0) );}
				 * catch (Exception e) {e.printStackTrace(); } }
				 */

				File[] extraJarFiles = new File(INSTALL_DIR).listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				});

				Arrays.sort(extraJarFiles);
                log.info("Insiders Extra Jars:" + Arrays.toString(extraJarFiles));
				for (int i = 0; i < extraJarFiles.length; ++i) {
					cp = cp + System.getProperty("path.separator") + extraJarFiles[i];
				}

				if (System.getenv().get("BIOCEP_EXTRA_JARS_LOCATION") != null) {
					extraJarFiles = new File(System.getenv().get("BIOCEP_EXTRA_JARS_LOCATION")).listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".jar");
						}
					});

					Arrays.sort(extraJarFiles);
                    log.info("Outsiders Extra Jars:" + Arrays.toString(extraJarFiles));
					for (int i = 0; i < extraJarFiles.length; ++i) {
						cp = cp + System.getProperty("path.separator") + extraJarFiles[i];
					}
				}

			} catch (Exception e) {
                log.error("Error!", e);
			}

			/*
			 * if (new File(INSTALL_DIR + "biocep-core.jar").exists()) { cp = cp
			 * + System.getProperty("path.separator") + new File(INSTALL_DIR +
			 * "biocep-core.jar").getAbsolutePath(); } else if (new
			 * File(INSTALL_DIR + "biocep.jar").exists()) { cp = cp +
			 * System.getProperty("path.separator") + new File(INSTALL_DIR +
			 * "biocep.jar").getAbsolutePath(); } if (new File(INSTALL_DIR +
			 * "groovy-all-1.5.4").exists()) { cp = cp +
			 * System.getProperty("path.separator") + new File(INSTALL_DIR +
			 * "groovy-all-1.5.4").getAbsolutePath(); }
			 */

			ManagedServant[] servantHolder = new ManagedServant[1];
			RemoteException[] exceptionHolder = new RemoteException[1];

			CreationCallBack callBack = null;

			progressLogger.logProgress("Creating R Server..");

			try {
				callBack = new CreationCallBack(servantHolder, exceptionHolder);
				String listenerStub = PoolUtils.stubToHex(callBack);

				String uid = null;

				if (name != null && !name.equals("") && name.contains("%{uid}")) {
					if (uid == null)
						uid = UUID.randomUUID().toString();
					name = PoolUtils.replaceAll(name, "%{uid}", uid);
				}

				if (logFile != null && !logFile.equals("") && logFile.contains("%{uid}")) {
					if (uid == null)
						uid = UUID.randomUUID().toString();
					logFile = PoolUtils.replaceAll(logFile, "%{uid}", uid);
				}

				Vector<String> command = new Vector<String>();

				command.add((isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-DXms" + memoryMinMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-DXmx" + memoryMaxMegabytes + "m" + (isWindowsOs() ? "\"" : ""));

				command.add("-cp");
				command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));


				//command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=" + codeBase + (isWindowsOs() ? "\"" : ""));

				//if (keepAlive) {
				//	command.add((isWindowsOs() ? "\"" : "") + "-Dpreloadall=true" + (isWindowsOs() ? "\"" : ""));
				//}

				command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=uk.ac.ebi.rcloud.server.RServantImpl" + (isWindowsOs() ? "\"" : ""));

				if (name == null || name.equals("")) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
				} else {
					command.add((isWindowsOs() ? "\"" : "") + "-Dname=" + name + (isWindowsOs() ? "\"" : ""));
				}

				command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub=" + listenerStub + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dpreprocess.help=true" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=false" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + INSTALL_DIR + "wdir" + (isWindowsOs() ? "\"" : ""));

				for (int i = 0; i < namingVars.length; ++i) {
					String var = namingVars[i];
					if (namingInfo.getProperty(var) != null && !namingInfo.getProperty(var).equals("")) {
						command.add((isWindowsOs() ? "\"" : "") + "-D" + var + "=" + namingInfo.get(var) + (isWindowsOs() ? "\"" : ""));
					}
				}

				if (logFile != null && !logFile.equals("")) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger"
							+ (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=DEBUG,A1,A2,A3" + (isWindowsOs() ? "\"" : ""));

					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));

					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=uk.ac.ebi.rcloud.rpf.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));

					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3=org.apache.log4j.FileAppender" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3.file=" + logFile + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
				}

				command.add("uk.ac.ebi.rcloud.server.bootstrap.Boot");
				//command.add(new Boolean(keepAlive).toString());
				//command.add(codeServerHostIp);
				//command.add("" + codeServerPort);

				//if (codeUrls != null && codeUrls.length > 0) {
				//	for (int i = 0; i < codeUrls.length; ++i) {
				//		command.add(codeUrls[i].toString());
				//	}
				//}

				final Process proc = null;
                //Runtime.getRuntime().exec(command.toArray(new String[0]), envVector.toArray(new String[0]));
				//final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), envVector.toArray(new String[0]));

				final Vector<String> outPrint = new Vector<String>();
				final Vector<String> errorPrint = new Vector<String>();

                log.info(" command : " + command);

				new Thread(new Runnable() {
					public void run() {
						try {
							BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
							String line = null;
							while ((line = br.readLine()) != null) {
                                log.info(line);
								errorPrint.add(line);
							}
						} catch (Exception e) {
                            log.error("Error!", e);
						}
					}
				}).start();

				new Thread(new Runnable() {
					public void run() {
						try {
							BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
							String line = null;
							while ((line = br.readLine()) != null) {
                                log.info(line);
								outPrint.add(line);
							}
						} catch (Exception e) {
                            log.error("Error!", e);
						}
					}
				}).start();

				long t1 = System.currentTimeMillis();
				while (servantHolder[0] == null && exceptionHolder[0] == null) {
					if (System.currentTimeMillis() - t1 >= SERVANT_CREATION_TIMEOUT_MILLISEC)
						throw new ServantCreationTimeout();
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				if (exceptionHolder[0] != null) {
					throw exceptionHolder[0];
				}

				progressLogger.logProgress("R Server Created.");

				return (RServices) servantHolder[0];
			} finally {
				if (callBack != null) {
					UnicastRemoteObject.unexportObject(callBack, true);
				}
			}
		} finally {
		}
	}

	// incomplete
	public static String getLibraryPath(String libName, String rpath, String rlibs) {
		if (!rpath.endsWith("/") && !rpath.endsWith("\\")) {
			rpath += "/";
		}
		if (rlibs != null && !rlibs.equals("") && !rlibs.endsWith("/") && !rlibs.endsWith("\\")) {
			rlibs += "/";
		}
		if (rlibs != null && !rlibs.equals("") && new File(rlibs + libName).exists()) {
			return rlibs + libName + "/";
		} else if (new File(rpath + "library/" + libName).exists()) {
			return rpath + "library/" + libName + "/";
		} else {
			return null;
		}
	}

	public static void killLocalUnixProcess(String processId, boolean isKILLSIG) throws Exception {
		PoolUtils.killLocalUnixProcess(processId, isKILLSIG);
	}

	public static void killLocalWinProcess(String processId, boolean isKILLSIG) throws Exception {
		PoolUtils.killLocalWinProcess(processId, isKILLSIG);
	}

	public static void killLocalProcess(String processId, boolean isKILLSIG) throws Exception {
		if (isWindowsOs())
			PoolUtils.killLocalWinProcess(processId, isKILLSIG);
		else
			PoolUtils.killLocalUnixProcess(processId, isKILLSIG);
	}

	public static void killSshProcess(String processId, String sshHostIp, String sshLogin, String sshPwd, boolean forcedKill) throws Exception {
		SSHUtils.killSshProcess(processId, sshHostIp, sshLogin, sshPwd, forcedKill);
	}

	public static String[] getRInfo(String rbin) {

		File getInfoFile = new File(INSTALL_DIR + "getInfo.R");

		File getInfoOutputFile = new File(INSTALL_DIR + "getInfo.Rout");

		try {
			getInfoOutputFile.delete();
		} catch (Exception e) {
            log.error("Error!", e);
		}

		String rversion = null;

		String rlibraypath = null;

		try {

			FileWriter fw = new FileWriter(getInfoFile);

			PrintWriter pw = new PrintWriter(fw);

			pw.println("paste('" + RHOMESTART + "',R.home(), '" + RHOMEEND + "',sep='%')");

			pw.println("paste('" + RVERSTART + "', R.version.string , '" + RVEREND + "', sep='%')");

			fw.close();

			Vector<String> getInfoCommand = new Vector<String>();

			if (rbin != null && !rbin.equals("")) {
                log.info("trying to execute :" + rbin);
				getInfoCommand.add(rbin);
				getInfoCommand.add("CMD");
				getInfoCommand.add("BATCH");
				getInfoCommand.add("--no-save");
				getInfoCommand.add(getInfoFile.getAbsolutePath());
				getInfoCommand.add(getInfoOutputFile.getAbsolutePath());

			} else {

				if (isWindowsOs()) {

					getInfoCommand.add(System.getenv().get("ComSpec"));
					getInfoCommand.add("/C");
					getInfoCommand.add("R");
					getInfoCommand.add("CMD");
					getInfoCommand.add("BATCH");
					getInfoCommand.add("--no-save");
					getInfoCommand.add(getInfoFile.getAbsolutePath());
					getInfoCommand.add(getInfoOutputFile.getAbsolutePath());

				} else {
					getInfoCommand.add(/* System.getenv().get("SHELL") */"/bin/sh");
					getInfoCommand.add("-c");
					getInfoCommand.add("R CMD BATCH --no-save " + getInfoFile.getAbsolutePath() + " " + getInfoOutputFile.getAbsolutePath());
				}
			}

			Vector<String> systemEnvVector = new Vector<String>();
			{

				Map<String, String> osenv = System.getenv();

				Map<String, String> env = new HashMap<String, String>(osenv);

				for (String k : env.keySet()) {

					systemEnvVector.add(k + "=" + env.get(k));

				}

			}

            log.info("exec->" + getInfoCommand);

            log.info(systemEnvVector.toString());

			final Process getInfoProc = Runtime.getRuntime().exec(getInfoCommand.toArray(new String[0]),

			systemEnvVector.toArray(new String[0]));

			new Thread(new Runnable() {

				public void run() {

					try {

						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getErrorStream()));

						String line = null;

						while ((line = br.readLine()) != null) {

                            log.info(line);

						}

					} catch (Exception e) {

                        log.error("Error!", e);

					}

				}

			}).start();

			new Thread(new Runnable() {

				public void run() {

					try {

						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getInputStream()));

						String line = null;

						while ((line = br.readLine()) != null) {

                            log.info(line);

						}

					} catch (Exception e) {

                        log.error("Error!", e);

					}

				}

			}).start();

			getInfoProc.waitFor();

			if (getInfoOutputFile.exists()) {

				BufferedReader br = new BufferedReader(new FileReader(getInfoOutputFile));

				String line = null;

				while ((line = br.readLine()) != null) {

					// log.info(line);

					if (line.contains(RHOMESTART + "%")) {

						rlibraypath = line.substring(line.indexOf(RHOMESTART + "%") + (RHOMESTART + "%").length(), (line.indexOf("%" + RHOMEEND) > 0 ? line
								.indexOf("%" + RHOMEEND) : line.length()));

					}

					if (line.contains(RVERSTART + "%")) {

						rversion = line.substring(line.indexOf(RVERSTART + "%") + (RVERSTART + "%").length(), line.indexOf("%" + RVEREND));

					}

				}

			} else {
                log.info(getInfoOutputFile.toString() + " not found ");
			}

		} catch (Exception e) {

            log.error("Error!", e);

		}

        log.info("+rversion:" + rversion);
        log.info("+rlibraypath:" + rlibraypath);
		if (rlibraypath != null) {
			return new String[] { rlibraypath, rversion };
		} else {

			return null;

		}

	}

	public static boolean isPortInUse(String hostIp, int port) {
		Socket s = null;
		try {
			s = new Socket(hostIp, port);
		} catch (Exception e) {
			return false;
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (Exception ex) {
				}
		}
		return true;
	}

	public static void startPortInUseDogwatcher(final String hostIp, final int port, final int periodicitySec, final int maxFailure) {

		new Thread(new Runnable() {
			int failureCounter = maxFailure;

			public void run() {
				while (true) {
					if (!isPortInUse(hostIp, port))
						--failureCounter;
					if (failureCounter == 0) {
                        log.info("The Creator Process doesn't respond, going to die");
						System.exit(0);
					}
					try {
						Thread.sleep(1000 * periodicitySec);
					} catch (Exception e) {
					}
				}
			}
		}).start();

	}

}
