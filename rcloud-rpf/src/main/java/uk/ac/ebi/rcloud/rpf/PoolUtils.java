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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import interruptiblermi.InterruptibleRMISocketFactory;
import interruptiblermi.InterruptibleRMIThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.exception.*;
import uk.ac.ebi.rcloud.util.HexUtil;

public class PoolUtils {
    final private static Logger log = LoggerFactory.getLogger(PoolUtils.class);

    public static final String DEFAULT_NAMING_MODE = "registry";
    public static final String DEFAULT_PREFIX = "POOL_";
    public static final String DEFAULT_REGISTRY_HOST = "localhost";
    public static final int DEFAULT_REGISTRY_PORT = 1099;
    public static final int DEFAULT_MEMORY_MIN = 256;
    public static final int DEFAULT_MEMORY_MAX = 256;
    public static final String DEFAULT_DB_TYPE = "oracle"; // postgres, mysql
    public static final String DEFAULT_DB_HOST = "localhost";
    public static final int DEFAULT_DB_PORT = 1531;
    public static final String DEFAULT_DB_NAME = "";
    public static final String DEFAULT_DB_USER = "";
    public static final String DEFAULT_DB_PASSWORD = "";
    public static final String DEFAULT_DB_DIR = "";

	public static final int DEFAULT_TIMEOUT = 40000;

	public static final int PING_FAILURES_NBR_MAX = 1;
	public static final long LOOKUP_TIMEOUT_MILLISEC = 8000;
	public static final long PING_TIMEOUT_MILLISEC = 8000;
	public static final long RESET_TIMEOUT_MILLISEC = 10000;
	public static final long DIE_TIMEOUT_MILLISEC = 20000;

	public static final String TEMP_DIR = ".";
	public static final String UNKOWN = "Unknown";
	private static String _hostName = null;
	private static String _hostIp = null;
	private static String _processId = null;
	private static final Integer PING_DONE = new Integer(0);
	private static final Integer RESET_DONE = new Integer(0);
	private static final Integer DIE_DONE = new Integer(0);
	private static final int GET_PROCESS_ID_RETRY_MAX = 3;

	private static boolean _propertiesInjected = false;
	private static boolean _rmiSocketFactoryInitialized = false;

	public static final int LOG_PRGRESS_TO_SYSTEM_OUT = 1;
	public static final int LOG_PRGRESS_TO_LOGGER = 2;
	public static final int LOG_PRGRESS_TO_DIALOG = 4;

	public static int BUFFER_SIZE = 1024 * 32;

	public static String getHostName() {
		if (_hostName == null) {
			try {
				_hostName = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				_hostName = UNKOWN;
			}
		}
		return _hostName;
	}

	public static boolean isValidIPAddress(String ipAddress) {
		String[] parts = ipAddress.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		for (String s : parts) {
			int i = Integer.parseInt(s);
			if ((i < 0) || (i > 255)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLoopBackIP(String ipAddress) {
		return ipAddress.startsWith("127.");
	}
	
	public static String getIPAddressFromNetworkInterfaces() {
		Vector<String> IPs = new Vector<String>();
		try {
			NetworkInterface iface = null;
			for (Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				iface = (NetworkInterface) ifaces.nextElement();
				InetAddress ia = null;
				for (Enumeration<?> ips = iface.getInetAddresses(); ips.hasMoreElements();) {
					ia = (InetAddress) ips.nextElement();
					boolean matches = isValidIPAddress(ia.getHostAddress()) && !isLoopBackIP(ia.getHostAddress());
					if (matches) {
						IPs.add(ia.getHostAddress());
					}
				}
			}
		} catch (Exception e) {
            log.error("Error!", e);
		}
		if (IPs.size() == 1)
			return IPs.elementAt(0);
		return null;
	}

	public static boolean publicIPUnavailable() {
		try {
			return isLoopBackIP(InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e) {
            log.error("Error!", e);
			return true;
		}
	}

	public static String getHostIp() {
		if (_hostIp == null) {
			try {
                if (publicIPUnavailable()) {
                    String IPAddressFromNetworkInterfaces = getIPAddressFromNetworkInterfaces();
                    if (IPAddressFromNetworkInterfaces != null)
                        _hostIp = IPAddressFromNetworkInterfaces;
                    else
                        _hostIp = "127.0.0.1";
                } else {
                    _hostIp = InetAddress.getLocalHost().getHostAddress();
                }
			} catch (Exception e) {
                log.error("Error!", e);
				_hostIp = UNKOWN;
			}
		}
		return _hostIp;
	}

	public static String getProcessId() {
		if (_processId == null) {
			try {
				_processId = PoolUtils.currentProcessID();
			} catch (Exception e) {
				_processId = UNKOWN;
			}
		}
		return _processId;
	}

	public static String getBiocepHome() {
		String result=System.getProperty("biocep.src.home");
		return result;
	}
	
	public static String getOs() {
		return System.getProperty(PropertyConst.OSNAME);
	}

	public static boolean isWindowsOs() {
		return System.getProperty(PropertyConst.OSNAME).toLowerCase().contains("windows");
	}

	public static boolean isMacOs() {
		return System.getProperty(PropertyConst.OSNAME).toLowerCase().contains("mac");
	}

	public static String shortRmiName(String fullRmiName) {
		return fullRmiName.substring(fullRmiName.lastIndexOf('/') + 1);
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	public static Vector<Integer> getRandomOrder(int size) {
		Vector<Integer> randomIndexes = new Vector<Integer>();
		Vector<Integer> availableIndexes = new Vector<Integer>();
		for (int i = 0; i < size; ++i)
			availableIndexes.add(i);
		for (int i = 0; i < size - 1; ++i) {
			int randomIndex = (int) Math.round((availableIndexes.size() - 1) * Math.random());
			randomIndexes.add(availableIndexes.elementAt(randomIndex));
			availableIndexes.remove(randomIndex);
		}
		randomIndexes.add(availableIndexes.elementAt(0));
		return randomIndexes;
	}

	private static void injectSystemProperties(InputStream is, boolean override) {
		if (is != null) {
			try {
				Properties props = new Properties();

				props.loadFromXML(is);

				Enumeration<Object> keys = props.keys();
				while (keys.hasMoreElements()) {
					String key = (String) keys.nextElement();
					boolean setProp=override || System.getProperty(key)==null || System.getProperty(key).equals("");
					if (setProp) System.setProperty(key, props.getProperty(key));
				}

			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}

	public static void injectSystemProperties(boolean onlyOnce) {

		if (onlyOnce && _propertiesInjected)
			return;
		injectSystemProperties(ServerDefaults.class.getResourceAsStream("/global.properties"), false);
		if (System.getProperty("properties.extension") != null && !System.getProperty("properties.extension").equals("")) {
			if (new File(System.getProperty("properties.extension")).exists()) {
				try {
					injectSystemProperties(new FileInputStream(System.getProperty("properties.extension")), true);
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			} else {
                log.info("Invalid File Name in 'properties.extension' <" + System.getProperty(
                        "properties.extension") + ">");
			}
		}

		_propertiesInjected = true;

	}

	public static void initRmiSocketFactory() {
		if (!_rmiSocketFactoryInitialized) {
			try {
				RMISocketFactory.setSocketFactory(new InterruptibleRMISocketFactory());
                _rmiSocketFactoryInitialized = true;
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
	}

	public static void ensurePublicIPIsUsedForRMI() {
		String hostname = System.getProperty(PropertyConst.RMISERVERHOSTNAME);

        if (hostname == null || hostname.length() == 0) {
			if (publicIPUnavailable()) System.setProperty(PropertyConst.RMISERVERHOSTNAME, getHostIp());
		}		
	}
	
	public static void noGui() {
		System.setProperty(PropertyConst.HEADLESS, "true");
	}

	public static String flatArray(Object[] array) {
		String result = "{";
		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				result += array[i].toString() + (i == (array.length - 1) ? "" : ",");
			}
		} else {
			result += "null";
		}
		result += "}";
		return result;
	}

	public static String currentUnixProcessID() throws Exception {

		String outputFileName = TEMP_DIR + "/echoPPID_" + System.currentTimeMillis() + ".txt";
		String[] command = new String[] { "/bin/sh", "-c", "echo $PPID > " + outputFileName };

		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);
		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception(Arrays.toString(command) + " exit code : " + exitVal);

		BufferedReader br = new BufferedReader(new FileReader(outputFileName));
		String result = br.readLine();
		br.close();

		new File(outputFileName).delete();
		return result;
	}

	public static String currentWinProcessID() throws Exception {

		String pslistpath = System.getProperty("java.io.tmpdir") + "/rpf/WinTools/" + "ps.exe";
        log.info(pslistpath);
		File pslistFile = new File(pslistpath);
		if (!pslistFile.exists()) {
			pslistFile.getParentFile().mkdirs();
			InputStream is = PoolUtils.class.getResourceAsStream("/wintools/ps.exe");
			RandomAccessFile raf = new RandomAccessFile(pslistFile, "rw");
			raf.setLength(0);
			int b;
			while ((b = is.read()) != -1)
				raf.write((byte) b);
			raf.close();
		}
		String[] command = new String[] { pslistpath };
		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);
		final StringBuffer psPrint = new StringBuffer();
		final StringBuffer psError = new StringBuffer();
		new Thread(new Runnable() {
			public void run() {
				try {
					InputStream is = proc.getInputStream();
					int b;
					while ((b = is.read()) != -1) {
						psPrint.append((char) b);
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					InputStream is = proc.getErrorStream();
					int b;
					while ((b = is.read()) != -1) {
						psError.append((char) b);
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}
		}).start();

		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception("ps exit code : " + exitVal);

		BufferedReader reader = new BufferedReader(new StringReader(psPrint.toString()));
		String line;
		int i = 0;
		while (!(line = reader.readLine()).startsWith("PID  PPID  THR PR NAME"))
			++i;
		++i;
		while ((line = reader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " ");
			st.nextElement();
			String PPID = (String) st.nextElement();
			st.nextElement();
			st.nextElement();
			if (line.endsWith("\\ps.exe"))
				return PPID;
			++i;
		}
		return null;
	}

	public static String currentProcessID() throws Exception {
		for (int i = 0; i < GET_PROCESS_ID_RETRY_MAX; ++i) {
			try {
				String result = isWindowsOs() ? currentWinProcessID() : currentUnixProcessID();
				if (result != null) {
					return result;
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}

		}
		throw new Exception("Couldn't retrieve Process ID");
	}

    public static Object hexToObject(String hex, ClassLoader cl) {
        try {
            return (Object) new ObjectInputStreamCL(new ByteArrayInputStream(HexUtil.hexToBytes(hex)), cl).readObject();
        } catch (Exception e) {
            log.error("Error!", e);
            return null;
        }
    }

    public static Object bytesToObject(byte[] buffer, ClassLoader cl) {
        try {
            return new ObjectInputStreamCL(new ByteArrayInputStream(buffer), cl).readObject();
        } catch (Exception e) {
            log.error("Error!", e);
            return null;
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
		String stub_hex = HexUtil.bytesToHex(baoStream.toByteArray());
		return stub_hex;
	}

	public static Remote hexToStub(String stubHex, ClassLoader cl) {
		try {
			return (Remote) new ObjectInputStreamCL(new ByteArrayInputStream(HexUtil.hexToBytes(stubHex)), cl).readObject();
		} catch (Exception e) {
            log.error("Error!", e);
			return null;
		}
	}

	public static String charRepeat(char c, int rep) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < rep; ++i)
			sb.append(c);
		return sb.toString();
	}

	public static String getDBType(String jdbcUrl) {
		int p1 = jdbcUrl.indexOf(':');
		int p2 = jdbcUrl.indexOf(':', p1 + 1);
		return jdbcUrl.substring(p1 + 1, p2);
	}

	public static URL[] getURLS(String urlsStr) {
		StringTokenizer st = new StringTokenizer(urlsStr, " ");
		Vector<URL> result = new Vector<URL>();
		while (st.hasMoreElements()) {
			try {
				result.add(new URL((String) st.nextElement()));
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
		return (URL[]) result.toArray(new URL[0]);
	}

	public static HashMap<String, String> getParameters(String parametersStr) {
		StringTokenizer st = new StringTokenizer(parametersStr, "~/~");
		HashMap<String, String> result = new HashMap<String, String>();
		while (st.hasMoreElements()) {
			try {
				String element = (String) st.nextElement();
				int p = element.indexOf('=');
				if (p == -1) {
					result.put(element, null);
				} else {
					result.put(element.substring(0, p).trim(), element.substring(p + 1, element.length()).trim());
				}
			} catch (Exception e) {
                log.error("Error!", e);
			}
		}
		return result;
	}

	public static Point deriveLocation(Point origin, double radius) {
		return new Point((int) ((origin.getX() - radius) + (Math.random() * 2 * radius)), (int) ((origin.getY() - radius) + (Math.random() * 2 * radius)));
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static void ping(final ManagedServant servant, long pingTimeOut) throws RemoteException {

		final Object[] resultHolder = new Object[1];
		Runnable pingRunnable = new Runnable() {
			public void run() {
				try {
					servant.ping();
					resultHolder[0] = PING_DONE;
				} catch (Exception e) {
					final boolean wasInterrupted = Thread.interrupted();
					if (wasInterrupted) {
						resultHolder[0] = new PingInterrupted();
					} else {
						resultHolder[0] = e;
					}
				}
			}
		};

		Thread pingThread = InterruptibleRMIThreadFactory.getInstance().newThread(pingRunnable);
		pingThread.start();

		long t1 = System.currentTimeMillis();
		while (resultHolder[0] == null) {
			if ((System.currentTimeMillis() - t1) > pingTimeOut) {
				pingThread.interrupt();
				resultHolder[0] = new PingTimeout();
				break;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}

		if (resultHolder[0] instanceof Throwable) {
			throw (RemoteException) resultHolder[0];
		}

	}

	public static void ping(final ManagedServant servant) throws RemoteException {
		ping(servant,PING_TIMEOUT_MILLISEC);
	}
	
	public static void reset(final ManagedServant servant,long resetTimeOut) throws RemoteException {
		final Object[] resultHolder = new Object[1];
		Runnable resetRunnable = new Runnable() {
			public void run() {
				try {
					servant.reset();
					resultHolder[0] = RESET_DONE;
				} catch (Exception e) {
					final boolean wasInterrupted = Thread.interrupted();
					if (wasInterrupted) {
						resultHolder[0] = new ResetInterrupted();
					} else {
						resultHolder[0] = e;
					}
				}
			}
		};

		Thread resetThread = InterruptibleRMIThreadFactory.getInstance().newThread(resetRunnable);
		resetThread.start();

		long t1 = System.currentTimeMillis();
		while (resultHolder[0] == null) {
			if ((System.currentTimeMillis() - t1) > resetTimeOut) {
				resetThread.interrupt();
				resultHolder[0] = new ResetTimeout();
				break;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}

		if (resultHolder[0] instanceof Throwable) {
			throw (RemoteException) resultHolder[0];
		}
	}
	
	public static void reset(final ManagedServant servant) throws RemoteException {
		reset(servant,RESET_TIMEOUT_MILLISEC);
	}

	public static void die(final ManagedServant servant,long dieTimeOut) throws RemoteException {
		final Object[] resultHolder = new Object[1];
		Runnable dieRunnable = new Runnable() {
			public void run() {
				try {
					servant.die();
					resultHolder[0] = DIE_DONE;
				} catch (UnmarshalException ue) {
					resultHolder[0] = DIE_DONE;
				} catch (Exception e) {
					final boolean wasInterrupted = Thread.interrupted();
					if (wasInterrupted) {
						resultHolder[0] = new DieInterrupted();
					} else {
						resultHolder[0] = e;
					}
				}
			}
		};

		Thread dieThread = InterruptibleRMIThreadFactory.getInstance().newThread(dieRunnable);
		dieThread.start();

		long t1 = System.currentTimeMillis();
		while (resultHolder[0] == null) {
			if ((System.currentTimeMillis() - t1) > dieTimeOut) {
				dieThread.interrupt();
				resultHolder[0] = new DieTimeout();
				break;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}

		if (resultHolder[0] instanceof Throwable) {
			throw (RemoteException) resultHolder[0];
		}
	}
	
	public static void die(final ManagedServant servant) throws RemoteException {
		die(servant,DIE_TIMEOUT_MILLISEC);
	}

	public static String replaceAll(String input, String replaceWhat, String replaceWith) throws Exception {
		int p;
		int bindex = 0;
		while ((p = input.indexOf(replaceWhat, bindex)) != -1) {
			input = input.substring(0, p) + replaceWith + input.substring(p + replaceWhat.length());
			bindex = p + replaceWith.length();
		}
		return input;
	}

	public static void locateInScreenCenter(Component c) {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		c.setLocation((screenDim.width - c.getWidth()) / 2, (screenDim.height - c.getHeight()) / 2);
	}

	public static void unzip(InputStream is, String destination, NameFilter nameFilter, int bufferSize, boolean showProgress, String taskName,
			int estimatedFilesNumber) {

		destination.replace('\\', '/');
		if (!destination.endsWith("/")) destination=destination+"/";
		
		final JTextArea area = showProgress ? new JTextArea() : null;
		final JProgressBar jpb = showProgress ? new JProgressBar(0, 100) : null;
		final JFrame f = showProgress ? new JFrame(taskName) : null;

		if (showProgress) {
			Runnable runnable = new Runnable() {
				public void run() {
					area.setFocusable(false);
					jpb.setIndeterminate(true);
					JPanel p = new JPanel(new BorderLayout());
					p.add(jpb, BorderLayout.SOUTH);
					p.add(new JScrollPane(area), BorderLayout.CENTER);
					f.add(p);
					f.pack();
					f.setSize(300, 90);
					f.setVisible(true);
					locateInScreenCenter(f);
				}
			};

			if (SwingUtilities.isEventDispatchThread())
				runnable.run();
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}

		try {

			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			int entriesNumber = 0;
			int currentPercentage = 0;
			int count;
			byte data[] = new byte[bufferSize];
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory() && (nameFilter == null || nameFilter.accept(entry.getName()))) {
					String entryName = entry.getName();
					prepareFileDirectories(destination, entryName);
					String destFN = destination + File.separator + entry.getName();

					FileOutputStream fos = new FileOutputStream(destFN);
					BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
					while ((count = zis.read(data, 0, bufferSize)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();

					if (showProgress) {
						++entriesNumber;
						final int p = (int) (100 * entriesNumber / estimatedFilesNumber);
						if (p > currentPercentage) {
							currentPercentage = p;
							final JTextArea fa = area;
							final JProgressBar fjpb = jpb;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fjpb.setIndeterminate(false);
									fjpb.setValue(p);
									fa.setText("\n" + p + "%" + " Done ");
								}
							});

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fa.setCaretPosition(fa.getText().length());
									fa.repaint();
									fjpb.repaint();
								}
							});

						}
					}
				}
			}
			zis.close();
			if (showProgress) {
				f.dispose();
			}
		} catch (Exception e) {
            log.error("Error!", e);
		}

	}

	
	private static final int RECONNECTION_RETRIAL_NBR=4;
	public static void cacheJar(URL url, String location, int logInfo, boolean forced) throws Exception {
		final String jarName = url.toString().substring(url.toString().lastIndexOf("/") + 1);
		if (!location.endsWith("/") && !location.endsWith("\\"))
			location += "/";
		String fileName = location + jarName;
		new File(location).mkdirs();

		final JTextArea area = ((logInfo & LOG_PRGRESS_TO_DIALOG) != 0) ? new JTextArea() : null;
		final JProgressBar jpb = ((logInfo & LOG_PRGRESS_TO_DIALOG) != 0) ? new JProgressBar(0, 100) : null;
		final JFrame f = ((logInfo & LOG_PRGRESS_TO_DIALOG) != 0) ? new JFrame("copying " + jarName + " ...") : null;

		try {
			
			URLConnection urlC = null;
			Exception connectionException=null;
			for (int i=0; i<RECONNECTION_RETRIAL_NBR; ++i) {
				try {
					urlC = url.openConnection();
					connectionException=null;break;
				} catch (Exception e) {
					connectionException=e;
				}
			}
			if (connectionException!=null) throw connectionException;
			
			InputStream is = url.openStream();
			File file = new File(fileName);
			
			long urlLastModified=urlC.getLastModified();
			if (!forced) {
				boolean somethingToDo=!file.exists() 
								||  file.lastModified() < urlLastModified
								|| (file.length() != urlC.getContentLength() && !isValidJar(fileName)); 
				if (!somethingToDo) return;
			}

			if ((logInfo & LOG_PRGRESS_TO_DIALOG) != 0) {

				Runnable runnable = new Runnable() {
					public void run() {
						try {
						f.setUndecorated(true);
						f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						area.setEditable(false);
						
						area.setForeground(Color.white);
						area.setBackground(new Color(0x00,0x80,0x80));
						
						jpb.setIndeterminate(true);
						jpb.setForeground(Color.white);
						jpb.setBackground(new Color(0x00,0x80,0x80));
						
						JPanel p = new JPanel(new BorderLayout());
						p.setBorder(BorderFactory.createLineBorder(Color.black,3));
						p.setBackground(new Color(0x00,0x80,0x80));
						p.add(jpb, BorderLayout.SOUTH);
						p.add(area, BorderLayout.CENTER);
						f.add(p);
						f.pack();
						f.setSize(300, 80);
						locateInScreenCenter(f);
						f.setVisible(true);
                            log.info("here");
						} catch (Exception e) {
                            log.error("Error!", e);
						}
					}
				};

				if (SwingUtilities.isEventDispatchThread())
					runnable.run();
				else {
					SwingUtilities.invokeLater(runnable);
				}
			}

			if ((logInfo & LOG_PRGRESS_TO_SYSTEM_OUT) != 0) {
                log.info("Downloading " + jarName + ":");
                log.info("expected:==================================================\ndone    :");
			}

			if ((logInfo & LOG_PRGRESS_TO_LOGGER) != 0) {
				log.info("Downloading " + jarName + ":");
			}

			int jarSize = urlC.getContentLength();
			int currentPercentage = 0;

			
			
			
			FileOutputStream fos = null;
			fos = new FileOutputStream(fileName);
			
			int count = 0;
			int printcounter = 0;

			byte data[] = new byte[BUFFER_SIZE];
			int co = 0;
			while ((co = is.read(data, 0, BUFFER_SIZE)) != -1) {
				fos.write(data, 0, co);
				
				count = count + co;
				int expected = (50 * count / jarSize);
				while (printcounter < expected) {
					if ((logInfo & LOG_PRGRESS_TO_SYSTEM_OUT) != 0) {
                        log.info("=");
					}
					if ((logInfo & LOG_PRGRESS_TO_LOGGER) != 0) {
						log.info((int) (100 * count / jarSize) + "% done.");
					}

					++printcounter;
				}

				if ((logInfo & LOG_PRGRESS_TO_DIALOG) != 0) {
					final int p = (int) (100 * count / jarSize);
					if (p > currentPercentage) {
						currentPercentage = p;

						final JTextArea fa = area;
						final JProgressBar fjpb = jpb;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fjpb.setIndeterminate(false);
								fjpb.setValue(p);
								fa.setText("Copying " + jarName + " ..."+"\n" + p + "%" + " Done. ");
							}
						});

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fa.setCaretPosition(fa.getText().length());
								fa.repaint();
								fjpb.repaint();
							}
						});

					}
				}

			}

			is.close();
			fos.close();
			
			

		} catch (MalformedURLException e) {
            log.error(e.toString());
			throw e;
		} catch (IOException e) {
            log.error(e.toString());
			
		} finally {
			if ((logInfo & LOG_PRGRESS_TO_DIALOG) != 0) {
				f.dispose();
			}
			if ((logInfo & LOG_PRGRESS_TO_SYSTEM_OUT) != 0) {
                log.info("\n 100% of " + jarName + " has been downloaded \n");
			}
			if ((logInfo & LOG_PRGRESS_TO_LOGGER) != 0) {
				log.info(" 100% of " + jarName + " has been downloaded");
			}
		}
	}

	public static void prepareFileDirectories(String destination, String entryName) {
		destination = destination.replace('\\', '/');
		String outputFileName = destination + entryName;
		new File(outputFileName.substring(0, outputFileName.lastIndexOf("/"))).mkdirs();
	}

	public interface NameFilter {
		public boolean accept(String name);
	}

	public static class EqualNameFilter implements NameFilter {
		HashSet<String> restrictToHashMap = new HashSet<String>();

		public EqualNameFilter(String... names) {
			if (names != null)
				for (int i = 0; i < names.length; ++i)
					restrictToHashMap.add(names[i]);
		}

		public boolean accept(String name) {
			return restrictToHashMap.contains(name);
		}
	}

	public static class StartsWithNameFilter implements NameFilter {
		String _startsWith;

		public StartsWithNameFilter(String startsWith) {
			_startsWith = startsWith;
		}

		public boolean accept(String name) {
			return name.startsWith(_startsWith);
		}
	}

	private static Vector<String> orderP(String[] keys) {
		Arrays.sort(keys);
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < keys.length; ++i)
			result.add((String) keys[i]);
		return result;
	}

	public static Vector<String> orderO(Collection<Object> c) {
		String[] keys = new String[c.size()];
		int i = 0;
		for (Object k : c)
			keys[i++] = (String) k;
		return orderP(keys);
	}

	public static Vector<String> orderS(Collection<String> c) {
		String[] keys = new String[c.size()];
		int i = 0;
		for (String k : c)
			keys[i++] = k;
		return orderP(keys);
	}

	public static void killLocalUnixProcess(String processId, boolean isKILLSIG) throws Exception {
		String[] command = isKILLSIG ? new String[] { "kill", "-9", processId } : new String[] { "kill", processId };
		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);
		final Vector<String> killPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

        log.info("Kill command : " + Arrays.toString(command));

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
						killPrint.add(line);
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}
		}).start();

		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception("kill exit code : " + exitVal + "\n" + errorPrint);
	}

	public static void killLocalWinProcess(String processId, boolean isKILLSIG) throws Exception {

		String killpath = System.getProperty("java.io.tmpdir") + "/rpf/WinTools/" + "kill.exe";
		File killFile = new File(killpath);
		if (!killFile.exists()) {
			killFile.getParentFile().mkdirs();
			InputStream is = PoolUtils.class.getResourceAsStream("/wintools/kill.exe");
			RandomAccessFile raf = new RandomAccessFile(killFile, "rw");
			raf.setLength(0);
			int b;
			while ((b = is.read()) != -1)
				raf.write((byte) b);
			raf.close();
		}
		String[] command = new String[] { killpath, processId };
		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);

		final StringBuffer killPrint = new StringBuffer();
		final StringBuffer errorPrint = new StringBuffer();
		new Thread(new Runnable() {
			public void run() {
				try {
					InputStream is = proc.getInputStream();
					int b;
					while ((b = is.read()) != -1) {
						killPrint.append((char) b);
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					InputStream is = proc.getErrorStream();
					int b;
					while ((b = is.read()) != -1) {
						errorPrint.append((char) b);
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}
		}).start();

		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception("kill exit code : " + exitVal + "\n" + errorPrint);
	}

	public static void callBack(final ServantCreationListener servantCreationListener,
                                final ManagedServant servant, final RemoteException exception) {
		try {

			final Object[] resultHolder = new Object[1];
			Runnable setServantStubRunnable = new Runnable() {
				public void run() {
					try {
						if (servant != null) {
							servantCreationListener.setServantStub(servant);
						} else {
							servantCreationListener.setRemoteException(exception);
						}
						resultHolder[0] = PoolUtils.SET_SERVANT_STUB_DONE;
					} catch (Exception e) {
						final boolean wasInterrupted = Thread.interrupted();
						if (wasInterrupted) {
							resultHolder[0] = new RmiCallInterrupted();
						} else {
							resultHolder[0] = e;
						}
					}
				}
			};

			Thread setServantStubThread = InterruptibleRMIThreadFactory.getInstance().
                    newThread(setServantStubRunnable);
			setServantStubThread.start();

			long t1 = System.currentTimeMillis();
			while (resultHolder[0] == null) {
				if ((System.currentTimeMillis() - t1) > PoolUtils.SET_SERVANT_STUB_TIMEOUT_MILLISEC) {
					setServantStubThread.interrupt();
					resultHolder[0] = new RmiCallTimeout();
					break;
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
			}

			if (resultHolder[0] instanceof Throwable) {
				throw (RemoteException) resultHolder[0];
			}

		} catch (Exception e) {
            log.error("Error!", e);
		}

	}

	public static final Integer SET_SERVANT_STUB_DONE = new Integer(0);
	public static final int SET_SERVANT_STUB_TIMEOUT_MILLISEC = 30000;

	public static int countLines(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		int counter = 0;
		while ((br.readLine()) != null)
			++counter;
		return counter;
	}

	public static void getFileNames(File path, Vector<String> result) throws Exception {
		File[] files = path.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".java")
                        || pathname.toString().toLowerCase().endsWith(".r")
						|| pathname.toString().toLowerCase().endsWith("build.xml");
			}
		});
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getFileNames(files[i], result);
			} else {
				String name = files[i].getAbsolutePath();
                log.info(name + " --> " + countLines(name));
				result.add(name);
			}
		}
	}

	public static void getClasses(File root, File path, Vector<String> result) throws Exception {
		if (path == null)
			path = root;
		File[] files = path.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".class");
			}
		});
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getClasses(root, files[i], result);
			} else {
				String absolutepath = files[i].getAbsolutePath();
				String name = absolutepath.substring(root.getAbsolutePath().length() + 1,
                        absolutepath.length() - ".class".length()).replace('/', '.').replace(
						'\\', '.');
				result.add(name);
			}
		}
	}

	public static void getResources(File root, File path, Vector<String> result) throws Exception {
		if (path == null)
			path = root;
		File[] files = path.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".xml") || pathname.toString().toLowerCase().endsWith(".props")
						|| pathname.toString().toLowerCase().endsWith(".properties") || pathname.toString().toLowerCase().endsWith(".r")
						|| pathname.toString().toLowerCase().endsWith(".png") || pathname.toString().toLowerCase().endsWith(".jpg")
						|| pathname.toString().toLowerCase().endsWith(".gif") || pathname.toString().toLowerCase().endsWith(".html")
						|| pathname.toString().toLowerCase().endsWith(".sql");
			}
		});
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getResources(root, files[i], result);
			} else {
				String absolutepath = files[i].getAbsolutePath();
				String name = absolutepath.substring(root.getAbsolutePath().length()).replace('\\', '/');
				result.add(name);
			}
		}
	}

	public static File createFileFromBuffer(String fileExtension, String buffer) throws Exception {
		File tempFile = null;
		tempFile = File.createTempFile("source", ".buffer");

		//if (tempFile.exists())
		//	tempFile.delete();

		BufferedReader breader = new BufferedReader(new StringReader(buffer));
		PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
		String line;
		do {
			line = breader.readLine();
			if (line != null) {
				pwriter.println(line);
			}
		} while (line != null);
		pwriter.close();
		return tempFile;
	}

	public static void main(String args[]) throws Exception {
        log.info(currentWinProcessID());
	}
	
	public static boolean isValidJar(String jarFileName) {
		try {
			URL jarUrl = new URL("jar:" + new File(jarFileName).toURI().toURL() + "!/");
			JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
			JarFile jarfile = jarConnection.getJarFile();
			return true;
		}  catch (Exception e) {
            log.error("Error!", e);
			return false;
		}		
	}
	
	
	public static Vector<String> tokenize(String command, String sep ) {
		Vector<String> result=new Vector<String>();
		StringTokenizer st = new StringTokenizer(command, sep);
		while (st.hasMoreElements())
			result.add(st.nextToken());
		return result;
	}
	
	private static final String pattern="%-&sd+-";	
	public static Vector<String> tokenizeWindowsCommand(String command) throws Exception {
		String command_t="";
		boolean changeSpaces=false;
		for (int i=0; i<command.length();++i) {
			if (command.charAt(i)=='\"') {
				changeSpaces=!changeSpaces;
			} else {
				if (command.charAt(i)==' ' && changeSpaces) command_t+=pattern;
				else command_t+=command.charAt(i);
			}
		}
			
		Vector<String> result=tokenize(command_t, " ");
		for (int i=0; i<result.size(); ++i) result.setElementAt(PoolUtils.replaceAll( result.elementAt(i).trim(), pattern, " "),i);
		return result;		
	}
	
	public static Properties extractProperties(String[] params ) throws Exception {
		Properties props = new Properties();
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				String element = params[i];
				int p = element.indexOf('=');
				if (p == -1) {
					props.put(element.toLowerCase(), "");
				} else {
					props.put(element.substring(0, p).trim().toLowerCase(), element.substring(p + 1, element.length()).trim());
				}
			}
		} 
		return props;
	}
	
	public static Properties extractProperties(String params_str) throws Exception {
		Properties props = new Properties();
		Vector<String> params=tokenizeWindowsCommand(params_str);
		if (params != null && params.size() > 0) {
			for (int i = 0; i < params.size(); i++) {				
				String element = params.elementAt(i);
				int p = element.indexOf('=');
				if (p == -1) {
					props.put(element.toLowerCase(), "");
				} else {
					props.put(element.substring(0, p).trim().toLowerCase(), element.substring(p + 1, element.length()).trim());
				}				
			}
		} 
		return props;
	}

}
