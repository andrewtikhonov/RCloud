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

import static uk.ac.ebi.rcloud.rpf.PoolUtils.getDBType;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;

import uk.ac.ebi.rcloud.rpf.PoolUtils;
import uk.ac.ebi.rcloud.rpf.ServantProviderFactory;
import uk.ac.ebi.rcloud.rpf.ServerDefaults;
import uk.ac.ebi.rcloud.rpf.db.ConnectionProvider;
import uk.ac.ebi.rcloud.rpf.db.DBLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.rpf.exception.SSHTunnelingException;
import uk.ac.ebi.rcloud.util.HexUtil;


public class SSHTunnelingWorker {
    final private static Logger log = LoggerFactory.getLogger(SSHTunnelingWorker.class);

	public static HashMap<String, Object> servantMap = new HashMap<String, Object>();

	public static Object invoke(String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		Object servant = servantMap.get(servantName);
		if (servant == null) {
			throw new SSHTunnelingException("Bad Servant Name :" + servantName);
		}
		Method m = null;
		try {
			m = servant.getClass().getMethod(methodName, methodSignature);
		} catch (NoSuchMethodException e) {
			throw new SSHTunnelingException("Bad Method Name :" + methodName);
		}
		Object result = null;
		try {
			result = m.invoke(servant, methodParameters);
		} catch (InvocationTargetException ite) {
			throw new SSHTunnelingException(PoolUtils.getStackTraceAsString(ite.getCause()));
		} catch (Exception e) {
			throw new SSHTunnelingException(PoolUtils.getStackTraceAsString(e));
		}
		return result;
	}

	public static void init() throws Exception {
		Class.forName(ServerDefaults._dbDriver);
		DBLayer registry = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
			public Connection newConnection() throws java.sql.SQLException {
				return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
			};
		});
		servantMap.put("db", registry);
		servantMap.put("servant.provider", ServantProviderFactory.getFactory().getServantProvider());
	}

	public static void main(String[] args) {
		String fileIn = args[0];
		Object result = null;
		try {
			init();
			Properties invokationProps = new Properties();
			invokationProps.loadFromXML(new FileInputStream(fileIn));
			result = invoke(invokationProps.getProperty("servantName"), invokationProps.getProperty("methodName"), (Class<?>[]) HexUtil
					.hexToObject(invokationProps.getProperty("methodSignature")), (Object[]) HexUtil.hexToObject(invokationProps
					.getProperty("methodParameters")));
		} catch (SSHTunnelingException e) {
			result = e;
		} catch (Exception e) {
			result = new SSHTunnelingException(PoolUtils.getStackTraceAsString(e));
		}

		try {
			String resultBuffer=HexUtil.objectToHex(result);
            log.info("->Result Start");
			
			int b=300;
			int d=resultBuffer.length()/b;
			int m=resultBuffer.length()%b;
			
			for (int i=0; i<d;++i) {
                log.info(resultBuffer.substring(i * b, i * b + b));
			}
			if (m>0) log.info(resultBuffer.substring(d * b, resultBuffer.length()));

            log.info("->Result End");
			System.out.flush();
			new File(fileIn).delete();
		} catch (Exception e) {
            log.error("Error!", e);
		}
	}

}
