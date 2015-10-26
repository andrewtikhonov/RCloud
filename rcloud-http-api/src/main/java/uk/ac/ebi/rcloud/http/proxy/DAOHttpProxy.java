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
package uk.ac.ebi.rcloud.http.proxy;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import uk.ac.ebi.rcloud.rpf.db.DAOLayerInterface;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 14, 2011
 * Time: 3:10:26 PM
 * To change this template use File | Settings | File Templates.
 */

public class DAOHttpProxy {
	private static DAOLayerInterface db  = null;

    public static DAOLayerInterface getDAOLayer(ProxySettings proxySettings, ServerSession session) throws Exception {

        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

        if (proxySettings != null) {
            Credentials credentials = new
                    UsernamePasswordCredentials(proxySettings.USERNAME,
                    proxySettings.PASSWORD);

            AuthScope authScope = new AuthScope(proxySettings.PROXY_HOST, 
                    proxySettings.PROXY_PORT);

            httpClient.getHostConfiguration().setProxy(proxySettings.PROXY_HOST,
                    proxySettings.PROXY_PORT);

            httpClient.getState().setProxyCredentials(authScope, credentials);
        }


        try {
            db = (DAOLayerInterface) ServerRuntimeImpl.getDynamicProxy(
                    new ServerSession(session.getCommandurl(), null), "dbinvoke", // commandurl
                    "DAOLAYER", new Class<?>[]{DAOLayerInterface.class},
                    httpClient);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return db;
	}
}
