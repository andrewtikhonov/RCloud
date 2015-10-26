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
package uk.ac.ebi.rcloud.http.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.util.HexUtil;
import uk.ac.ebi.rcloud.version.SoftwareVersion;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 6, 2010
 * Time: 2:55:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpgradeServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    final private static Logger log = LoggerFactory.getLogger(UpgradeServlet.class);

	public UpgradeServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            doAny(request, response);
        } catch (Exception ex) {
            log.error("Unexpected Exception ", ex);
        }
	}

	protected HashMap<String, Object> getRequestParameters(HttpServletRequest request) {
        return (HashMap<String, Object>) HexUtil.hexToObject(request.getParameter("parameters"));
    }

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String command = request.getParameter("method");

        if (command.equals("getversion")) {

            Properties p = new Properties();
            p.put("version", SoftwareVersion.getVersion());
            p.storeToXML(response.getOutputStream(),"version");
            response.flushBuffer();

            return;

        } else if (command.equals("othercommand")) {
            //
        }
    }


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            doAny(request, response);
        } catch (Exception ex) {
            log.error("Unexpected exception ", ex);
        }
	}

	public void init(ServletConfig sConfig) throws ServletException {
		super.init(sConfig);
		log.info("command servlet init");
	}

}
