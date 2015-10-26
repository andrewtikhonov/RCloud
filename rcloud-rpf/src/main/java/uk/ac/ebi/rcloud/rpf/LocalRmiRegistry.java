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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class LocalRmiRegistry {
    final private static Logger log = LoggerFactory.getLogger(LocalRmiRegistry.class);

	private static Registry _server = null;
	private static Integer _lock = new Integer(0);
	private static int _port; 

	public static Registry getInstance() {
		if (_server != null)
			return _server;
		synchronized (_lock) {
			if (_server == null) {
				try {
					if (System.getProperty("localrmiregistry.port") == null || System.getProperty("localrmiregistry.port").equals("")) {
						ServerSocket ss = new ServerSocket(0);
						_port = ss.getLocalPort();
						ss.close();

						Runtime.getRuntime().gc();
						
						Random rnd=new Random();rnd.setSeed(System.currentTimeMillis());
						while (true) {
							try {
								_server = LocateRegistry.createRegistry(_port);
								break;
							} catch (Exception e) {
								_port=2000+rnd.nextInt(1000);
							}
						}

                        log.info("local port :" + _port);
							
					} else {
						_server = LocateRegistry.createRegistry(Integer.decode(System.getProperty("localrmiregistry.port")));
					}
				} catch (Exception e) {
                    log.error("Error!", e);
				}
			}
			return _server;
		}
	}


	public static Integer getLocalRmiRegistryPort() {
		getInstance();
		return _port;
	}
	

}
