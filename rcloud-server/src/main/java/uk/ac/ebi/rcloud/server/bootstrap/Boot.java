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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Vector;

public class Boot {
    final private static Logger log = LoggerFactory.getLogger(Boot.class);

	public static void main(final String[] args) throws Exception {
		try {
			boolean keepAlive = Boolean.parseBoolean(args[0]);

            URLClassLoader cl = null;

            String host = args[1];
            String port = args[2];

            log.info("Boot Args:" + Arrays.toString(args));

            Vector<URL> codeUrls = new Vector<URL>();

            codeUrls.add(new URL("http://" + host + ":" + port + "/classes/"));
			
			if (args.length > 3) {
				for (int i = 3;i < args.length; ++i) {
					codeUrls.add(new URL(args[i]));
				}
			}
			
			cl = new URLClassLoader((URL[])codeUrls.toArray(new URL[0]), Boot.class.getClassLoader());

			if (!keepAlive) {
				cl.loadClass("uk.ac.ebi.rcloud.server.manager.ServerManager")
                        .getMethod("startPortInUseDogwatcher", new Class<?>[] { String.class, int.class, int.class, int.class })
						.invoke(null, host, Integer.decode(port), 3, 3);
			}

			cl.loadClass("uk.ac.ebi.rcloud.server.MainRServer")
                    .getMethod("main", new Class<?>[] { String[].class })
                    .invoke(null, new Object[] { new String[] {} });
			
		} catch (Throwable e) {
            log.error("Error!", e);
		}

	}
}
