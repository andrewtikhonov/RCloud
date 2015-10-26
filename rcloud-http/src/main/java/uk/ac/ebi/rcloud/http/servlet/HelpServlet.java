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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.rpf.PoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.RType.RChar;
import uk.ac.ebi.rcloud.server.RType.RList;
import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.util.HexUtil;


public class HelpServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    final private Logger log = LoggerFactory.getLogger(this.getClass());
    
	public HelpServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		doAny(request, response);
	}

    protected HashMap<String, Object> getRequestParameters(HttpServletRequest request) {
        return (HashMap<String, Object>) HexUtil.hexToObject(request.getParameter("parameters"));
    }

    private HttpSession findSession(String sessionid) {
        HttpSession session;

        Collection<HttpSession> sessions =
                ((HashMap<String, HttpSession>) getServletContext()
                        .getAttribute("SESSIONS_MAP")).values();

        if (sessions.size() > 0) {
            while(sessions.iterator().hasNext()) {
                session = sessions.iterator().next();

                if (session.getId().equals(sessionid)) {
                    return session;
                }
            }
        }

        return null;
    }

    private class ResponseCotainer {
        public byte[] data = null;
        public String redirectURL = null;
    }

    private ResponseCotainer requestHelpContent(RServices r,  String helpuri, ResponseCotainer container) {

        int cnt = 50;

        String requestres;

        try {
            container.data = r.getRHelpFile(helpuri);
            return container;
        } catch (Exception ex) {
        }

        try {
            while( cnt > 0 ) {

                String requestcmd = "tools:::httpd(\"" + helpuri +"\")"; // [["payload"]]

                RChar   rchar;
                RObject robjvalue;

                RObject robj = r.getObject(requestcmd);

                if (robj != null && robj instanceof RList) {

                    //response.encodeRedirectURL()
                    //
                    // encode redirect from help servlet
                    //

                    while(robj instanceof RList) {
                        robjvalue = ((RList) robj).getValueByName("payload");

                        if (robjvalue != null) {

                            if (robjvalue instanceof RList) {
                                robj = robjvalue;
                                continue;
                            }

                            rchar = (RChar) robjvalue;

                            requestres = ((String[])rchar.getValue())[0];

                            if (requestres.startsWith("Redirect to")) {

                                String opentag = "href=\"";
                                String closetag = "\"";

                                String redirecturl;
                                redirecturl = requestres.substring(requestres.indexOf(opentag) + opentag.length(), requestres.length());
                                redirecturl = redirecturl.substring(0, redirecturl.indexOf(closetag));

                                container.data = requestres.getBytes();
                                container.redirectURL = redirecturl;
                                return container;

                            } else {
                                container.data = requestres.getBytes();
                                return container;
                            }

                        } else {

                            robjvalue = ((RList) robj).getValueByName("file");

                            if (robjvalue instanceof RList) {
                                robj = robjvalue;
                                continue;
                            }

                            rchar = (RChar) robjvalue;

                            if (rchar != null) {
                                String filename = ((String[])rchar.getValue())[0];

                                container.data = r.readRandomAccessFileBlock(filename, 0, 6553500);
                                return container;
                            }
                        }
                    }

                } else {
                    return container;
                }

                cnt--;

                if (cnt == 0) {
                    log.error("Error! Infinite Loop in requestHelpContent(" + helpuri + ")");
                    throw new Exception();
                }
            }
        } catch (Exception ex) {
        }

        return container;
    }

    private String prepareRedirectURL(String redirectFromURL, String redirectToURL) {
        String backtag = "../";
        String fwslash = "/";

        redirectFromURL = redirectFromURL.substring(0, redirectFromURL.lastIndexOf(fwslash));

        while(redirectToURL.startsWith(backtag)) {
            redirectFromURL = redirectFromURL.substring(0, redirectFromURL.lastIndexOf(fwslash));
            redirectToURL   = redirectToURL.substring(backtag.length(), redirectToURL.length());
        }

        redirectFromURL = redirectFromURL + fwslash + redirectToURL;

        return redirectFromURL;
    }

	protected void doAny(final HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ResponseCotainer container = new ResponseCotainer();
		HttpSession session = null;
		//Object result = null;

		try {

			do {

				session = request.getSession(false);

                String uri = request.getRequestURI();

                String helpmetag = "/helpme";
                int indexOfhelpme = uri.indexOf(helpmetag);

                String prefix = uri.substring(0, indexOfhelpme + helpmetag.length());
                String helpuri = uri.substring(indexOfhelpme + helpmetag.length());

                response.setContentType("text/html; charset=utf-8");

                if (helpuri.endsWith("R.css")) {
                    container.data = composeRcss();
                    break;
                }

                if (session == null) {
                    Collection<HttpSession> sessions =
                            ((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).values();

                    if (sessions.size() > 0) {
						session = sessions.iterator().next();
					} else {
                        container.data = composeNoRServerFound();
                        break;
					}
                }

                RServices r = (RServices) session.getAttribute("R");

                if (r == null) {
                    container.data = composeNoRServerFound();
                    break;
                }

                if (uri.toLowerCase().endsWith(".jpg")) {
                    response.setContentType("image/jpeg");
                }

                container = requestHelpContent(r, helpuri, container);

                if (container.redirectURL != null) {

                }

                break;

			} while (true);

		} catch (Exception e) {
            log.error("Error!", e);
            container.data = composeHelpPageNotFound();
		}

        if (container.data != null && container.data.getClass().equals(byte[].class)) {
            response.getOutputStream().write((byte[]) container.data);
        } else {
            new ObjectOutputStream(response.getOutputStream()).writeObject(container.data);
        }

        if (container.redirectURL != null) {

            response.sendRedirect(response.encodeRedirectURL(
                    prepareRedirectURL(request.getRequestURI(), container.redirectURL)));
        }

            /*
            if (container.data != null && container.data instanceof Throwable) {
                log.error("Error!", ((Throwable) result));
                response.getOutputStream().print(PoolUtils.getStackTraceAsString((Throwable) result));
            } else {
                new ObjectOutputStream(response.getOutputStream()).writeObject(result);
            }
            */


		response.flushBuffer();
	}

    private byte[] composeResonse(String path, String message) {
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    HelpServlet.class.getResourceAsStream(path)));

            String l;
            while ((l = br.readLine()) != null) {
                //l = l.trim();
                builder.append(l);
                //builder.append("\n");
            }
        } catch (Exception ex) {
            builder.append("<html><body><h1><center>" + message + "</center><h1></body></html>");
        }

        return builder.toString().getBytes();

    }

    private byte[] composeHelpPageNotFound() {
        return composeResonse("/html/help-page-not-found.html", "Requested help page not found");
    }

    private byte[] composeRcss() {
        return composeResonse("/html/copy-of-R.css", "");
    }

    private byte[] composeNoRServerFound() {
        return composeResonse("/html/no-r-server-found.html", "No valid R server found");
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		doAny(request, response);
	}

}