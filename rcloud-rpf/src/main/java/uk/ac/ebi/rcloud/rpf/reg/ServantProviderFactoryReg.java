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
package uk.ac.ebi.rcloud.rpf.reg;

import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static uk.ac.ebi.rcloud.rpf.PoolUtils.DEFAULT_TIMEOUT;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.DEFAULT_PREFIX;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.DEFAULT_REGISTRY_HOST;
import static uk.ac.ebi.rcloud.rpf.PoolUtils.DEFAULT_REGISTRY_PORT;

import uk.ac.ebi.rcloud.rpf.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.exception.TimeoutException;


public class ServantProviderFactoryReg extends ServantProviderFactory {
    final private Logger log = LoggerFactory.getLogger(getClass());

	private HashMap<String, PoolData> _poolHashMap = new HashMap<String, PoolData>();
	private Hashtable<ManagedServant, PoolNode> _borrowedServants = new Hashtable<ManagedServant, PoolNode>();
	private String _defaultPoolName = null;

	private Registry _rmiRegistry = null;

	private ServantProvider _servantProvider = null;

	public ServantProviderFactoryReg() {

		if (System.getProperty("pools.regmode.configuration.file") != null &&
                !System.getProperty("pools.regmode.configuration.file").equals("")) {
			try {
				initPoolsHashMap(new FileInputStream(System.getProperty("pools.regmode.configuration.file")));
			} catch (Exception e) {
                log.error("Error!", e);
			}
		} else if (System.getProperty("pools.regmode.configuration.resource") != null &&
                !System.getProperty("pools.regmode.configuration.resource").equals("")) {
			try {
				initPoolsHashMap(this.getClass().getResourceAsStream(System.getProperty("pools.regmode.configuration.resource")));
			} catch (Exception e) {
                log.error("Error!", e);
			}
		} else {

			
			String poolPrefix = ServerDefaults._servantPoolPrefix;
			String registryHost = ServerDefaults._registryHost;
			int registryPort = ServerDefaults._registryPort;
			
			int timeOutMillisec = System.getProperty("timeout") != null && !System.getProperty("timeout").equals("") ? Integer.decode(System
					.getProperty("timeout")) : DEFAULT_TIMEOUT;

			
			_defaultPoolName = "DEFAULT";
			PoolData poolData = new PoolData(_defaultPoolName, timeOutMillisec, new Vector<PoolNode>());
			poolData.getNodes().add(new PoolNode(poolPrefix, registryHost, registryPort));
			_poolHashMap.put(poolData.getPoolName(), poolData);

			try {
				_rmiRegistry = LocateRegistry.getRegistry(registryHost, registryPort);
			} catch (Exception e) {
                log.error("Error!", e);
			}

		}

        log.info(_poolHashMap.toString());

		_servantProvider = new ServantProvider() {

			public ManagedServant borrowServantProxy(String poolName) throws TimeoutException {
				ManagedServant proxy = null;
				int nodesSize = _poolHashMap.get(poolName).getNodes().size();
				Vector<Integer> order = PoolUtils.getRandomOrder(nodesSize);
				int nodeIndex = 0;
				PoolNode node = null;
				long tstart = System.currentTimeMillis();
				do {
					try {
						node = _poolHashMap.get(poolName).getNodes().elementAt(order.elementAt(nodeIndex));
						proxy = (ManagedServant) ServantProxyPoolSingletonReg.getInstance(node.getRegistryHost(),
                                node.getRegistryPort(),
								node.getServantPoolPrefix()).borrowObject();
					} catch (NoSuchElementException e) {
					} catch (Exception ex) {
                        log.error("Error!", ex);
					}

					if (proxy != null) {
						try {
							log.info("<" + Thread.currentThread().getName() + "> obtained resource : " +
                                    proxy.getServantName());
						} catch (Exception e) {
						}
						break;
					} else {
						nodeIndex = (nodeIndex + 1) % nodesSize;
					}

					if (System.currentTimeMillis() - tstart > _poolHashMap.get(poolName).getBorrowTimeout())
						throw new TimeoutException();
					try {
						Thread.sleep(20);
					} catch (Exception e) {
					}

					log.info("<" + Thread.currentThread().getName() + "> thread waiting for resource for  : " +
                            ((System.currentTimeMillis() - tstart) / 1000)
							+ " seconds");

				} while (true);

				_borrowedServants.put(proxy, node);
				return proxy;
			}

			public ManagedServant borrowServantProxyNoWait(String poolName) {
				ManagedServant proxy = null;
				int nodesSize = _poolHashMap.get(poolName).getNodes().size();
				Vector<Integer> order = PoolUtils.getRandomOrder(nodesSize);
				int nodeIndex = 0;
				PoolNode node = null;
				do {
					try {
						node = _poolHashMap.get(poolName).getNodes().elementAt(order.elementAt(nodeIndex));
						proxy = (ManagedServant) ServantProxyPoolSingletonReg.getInstance(node.getRegistryHost(), node.getRegistryPort(),
								node.getServantPoolPrefix()).borrowObject();
					} catch (NoSuchElementException e) {
					} catch (Exception ex) {
                        log.error("Error!", ex);
					}

					if (proxy != null) {
						try {
							log.info("<" + Thread.currentThread().getName() + "> obtained resource : " + proxy.getServantName());
						} catch (Exception e) {
						}
						break;
					} else {
						nodeIndex = (nodeIndex + 1);
						if (nodeIndex >= nodesSize)
							break;
					}

				} while (true);

				if (proxy != null)
					_borrowedServants.put(proxy, node);
				return proxy;
			}

			public void returnServantProxy(ManagedServant proxy) {
				if (proxy == null)
					return;
				try {
					PoolNode node = _borrowedServants.get(proxy);
					_borrowedServants.remove(proxy);
					ServantProxyPoolSingletonReg.getInstance(node.getRegistryHost(),
                            node.getRegistryPort(), node.getServantPoolPrefix()).returnObject(proxy);
				} catch (Exception e) {
                    log.error("Error!", e);
					System.exit(0);
				}
			}
			
			public void returnServantProxy(String poolName, ManagedServant proxy) {
				if (proxy == null)
					return;
				try {
					PoolNode node = _borrowedServants.get(proxy);
					_borrowedServants.remove(proxy);
					ServantProxyPoolSingletonReg.getInstance(node.getRegistryHost(),
                            node.getRegistryPort(), node.getServantPoolPrefix()).returnObject(proxy);
				} catch (Exception e) {
                    log.error("Error!", e);
					System.exit(0);
				}
			}

			public ManagedServant borrowServantProxy() throws TimeoutException {
				return borrowServantProxy(_defaultPoolName);
			}

			public ManagedServant borrowServantProxyNoWait() {
				return borrowServantProxyNoWait(_defaultPoolName);
			}

			public String getDefaultPoolName() {
				return _defaultPoolName;
			}

			public Registry getRegistry() {
				return _rmiRegistry;
			}

		};

	}

	@Override
	public ServantProvider getServantProvider() {
		return _servantProvider;
	}

	void initPoolsHashMap(InputStream poolsXmlStream) throws Exception {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();

		Document document = documentBuilder.parse(poolsXmlStream);
		NodeList pools = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < pools.getLength(); ++i) {
			Node p = pools.item(i);
			if (p.getNodeName().equals("pool")) {
				String name = p.getAttributes().getNamedItem("name").getNodeValue();
				int timeout = p.getAttributes().getNamedItem("borrowTimeout") == null ? DEFAULT_TIMEOUT : Integer.decode(p.getAttributes().getNamedItem(
						"borrowTimeout").getNodeValue());
				PoolData poolData = new PoolData(name, timeout, new Vector<PoolNode>());
				_poolHashMap.put(poolData.getPoolName(), poolData);

				NodeList pnodes = p.getChildNodes();
				for (int j = 0; j < pnodes.getLength(); ++j) {
					Node pn = pnodes.item(j);
					if (pn.getNodeName().equals("node")) {
						String prefix = pn.getAttributes().getNamedItem("prefix") == null ? DEFAULT_PREFIX : pn.getAttributes().getNamedItem("prefix")
								.getNodeValue();
						String host = pn.getAttributes().getNamedItem("registryHost") == null ? DEFAULT_REGISTRY_HOST : pn.getAttributes().getNamedItem(
								"registryHost").getNodeValue();
						int port = pn.getAttributes().getNamedItem("registryPort") == null ? DEFAULT_REGISTRY_PORT : Integer.decode(pn.getAttributes()
								.getNamedItem("registryPort").getNodeValue());
						poolData.getNodes().add(new PoolNode(prefix, host, port));
					}
				}
			}
		}

		_defaultPoolName = document.getDocumentElement().getAttribute("default");
		if (_poolHashMap.get(_defaultPoolName) == null)
			throw new Exception("bad default pool name");
	}

	private static class PoolNode {
		private String _servantPoolPrefix;
		private String _registryHost;
		private int _registryPort;

		public PoolNode(String poolPrefix, String host, int port) {
			_servantPoolPrefix = poolPrefix;
			_registryHost = host;
			_registryPort = port;
		}

		public String toString() {
			return "PoolNode[prefic=" + _servantPoolPrefix + " host=" + _registryHost + " port=" + _registryPort + "]";
		}

		public String getRegistryHost() {
			return _registryHost;
		}

		public int getRegistryPort() {
			return _registryPort;
		}

		public String getServantPoolPrefix() {
			return _servantPoolPrefix;
		}
	}

	private static class PoolData {
		private String _poolName;
		private Vector<PoolNode> _nodes;
		private int _borrowTimeout;

		public PoolData(String name, int timeout, Vector<PoolNode> nodes) {
			super();
			_poolName = name;
			this._nodes = nodes;
			_borrowTimeout = timeout;
		}

		public int getBorrowTimeout() {
			return _borrowTimeout;
		}

		public Vector<PoolNode> getNodes() {
			return _nodes;
		}

		public String getPoolName() {
			return _poolName;
		}

		public String toString() {
			return "PoolData[name=" + _poolName + " bto=" + _borrowTimeout + " nodes=" + _nodes + "]";
		}
	}

}
