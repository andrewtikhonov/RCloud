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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHUtils {
    final private static Logger log = LoggerFactory.getLogger(SSHUtils.class);    

	public static void killSshProcess(String processId, String sshHostIp, String sshLogin, String sshPwd, boolean forcedKill) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			sess.execCommand("kill" + " " + (forcedKill ? "-9" : "") + " " + processId);

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null)
								break;
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
                            log.info(line);
						}
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);

		} finally {
			try {
				conn.close();
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}
	
	
	public static void execSsh(String command, String sshHostIp, int port, String sshLogin, String sshPwd) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp, port);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			
			sess.execCommand(command);

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
                            log.info(line);
						}
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);

		} finally {
			try {
				conn.close();
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}
	
	public static String execSshBatch(String command, String uid, String prefix,  String sshHostIp, int port, String sshLogin, String sshPwd, String remoteTargetDirectory) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp,port);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			new SCPClient(conn).put(PoolUtils.replaceAll(command, "%{uid}", uid).getBytes(), "launcher_"+uid+".sh", remoteTargetDirectory);			
			sess.close();
			
			sess = conn.openSession();		
			sess.execCommand("chmod a+x "+remoteTargetDirectory+"/launcher_"+uid+".sh");
			sess.close();
			
			sess = conn.openSession();			
			sess.execCommand(prefix+" "+remoteTargetDirectory+"/launcher_"+uid+".sh");
			
			
			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			
			final StringBuffer outputBuffer=new StringBuffer();
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;
							outputBuffer.append(line.trim());
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
                            log.info(line);
						}
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
			
			sess.close();
			sess = conn.openSession();
			sess.execCommand("rm "+remoteTargetDirectory+"/launcher_"+uid+".sh");
			sess.close();
			return outputBuffer.toString();

		} finally {
			try {
				conn.close();
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}

	
	public static void putFileSsh(String localFile, String remoteTargetDirectory, String sshHostIp, String sshLogin, String sshPwd) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			Session sess = null;
			sess = conn.openSession();			
			new SCPClient(conn).put(localFile, remoteTargetDirectory);			
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}
	
	public static void getFileSsh(String remoteFile, String localFile, String sshHostIp, String sshLogin, String sshPwd) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			sess.execCommand("cat "+remoteFile);

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			final Vector<String> out=new Vector<String>();
			
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;
							out.add(line);
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
                            log.info(line);
						}
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}).start();
			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);			
			
			PrintWriter pw=new PrintWriter(localFile);
			for (int i=0; i<out.size(); ++i) pw.println(out.elementAt(i));
			pw.close();
			
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}	
		

}
