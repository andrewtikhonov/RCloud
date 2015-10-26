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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.UUID;

import uk.ac.ebi.rcloud.rpf.PoolUtils;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.exception.SSHTunnelingException;
import uk.ac.ebi.rcloud.util.HexUtil;

public class SSHTunnelingProxy {
    final private static Logger log = LoggerFactory.getLogger(SSHTunnelingProxy.class);	
	
	public static Object invoke(String sshHostIp, int port, String sshLogin, String sshPwd, String homedir, String invokeCommand, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		String uid=UUID.randomUUID().toString();								
		
		String fileIn=System.getProperty("java.io.tmpdir")+"/invoke"+uid+".in";
		String fileOut=System.getProperty("java.io.tmpdir")+"/invoke"+uid+".out";

		if (new File(fileIn).exists()) new File(fileIn).delete();
		if (new File(fileOut).exists()) new File(fileOut).delete();
		
		Connection conn = null;
		try {
			
			conn = new Connection(sshHostIp,port);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			Session sess = null;
			sess = conn.openSession();		
			
			Properties invokationProps=new Properties();
			invokationProps.put("servantName", servantName);
			invokationProps.put("methodName", methodName);
			invokationProps.put("methodSignature", HexUtil.objectToHex(methodSignature));
			invokationProps.put("methodParameters", HexUtil.objectToHex(methodParameters));
			
			
			FileOutputStream fos=new FileOutputStream(fileIn);
			invokationProps.storeToXML(fos, "");
			fos.close();
			new SCPClient(conn).put(fileIn, homedir);			
			String cmd=PoolUtils.replaceAll(invokeCommand, "%{file}", homedir+"/invoke"+uid+".in");
			cmd=PoolUtils.replaceAll(cmd, "%{install.dir}", homedir);

            log.info("cmd:" + cmd);
			sess.execCommand( cmd );

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			final StringBuffer buffer=new StringBuffer();
			final boolean[] startReadingAnswer=new boolean[]{false};
			new Thread(new Runnable() {
				
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;							
							
							if (line.equals("->Result End")) startReadingAnswer[0]=false;
							if (startReadingAnswer[0]) buffer.append(line.trim());
							if (line.equals("->Result Start")) startReadingAnswer[0]=true;							
							//log.info(line);
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
							if (line == null) break;
                            log.info("ERROR:" + line);
						}
					} catch (Exception e) {
                        log.error("Error!", e);
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
			
			while (startReadingAnswer[0]) {
				try {Thread.sleep(10);} catch (Exception e) {}
			}
		
			Object result=PoolUtils.hexToObject(buffer.toString(), SSHTunnelingProxy.class.getClassLoader());
			
			if (result instanceof SSHTunnelingException) throw (SSHTunnelingException)result;
			else return result;			
			
		} catch (SSHTunnelingException sshe) {			
			throw sshe;			
		} catch (Exception e) {
            log.error("Error!", e);
			throw new SSHTunnelingException(PoolUtils.getStackTraceAsString(e));			
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
                log.error("Error!", e);
			}
			
			if (new File(fileIn).exists()) new File(fileIn).delete();
			if (new File(fileOut).exists()) new File(fileOut).delete();

		}
	}
	
	public static Object getDynamicProxy(final String sshHostIp,final int port, final String sshLogin,final String sshPwd,final String homedir,final String invokeCommand, final String servantName, Class<?>[] c) {
		Object proxy = Proxy.newProxyInstance(SSHTunnelingProxy.class.getClassLoader(), c, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				//new Exception().printStackTrace();
				return SSHTunnelingProxy.invoke(sshHostIp,port, sshLogin, sshPwd,homedir,invokeCommand, servantName, method.getName(), method.getParameterTypes(), args);
			}
		});
		return proxy;
	}

}
