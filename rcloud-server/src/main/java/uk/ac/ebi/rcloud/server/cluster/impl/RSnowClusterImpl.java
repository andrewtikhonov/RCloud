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
package uk.ac.ebi.rcloud.server.cluster.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.DirectJNI;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.SiteOptions;
import uk.ac.ebi.rcloud.server.cluster.RSnowClusterInterface;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 16, 2010
 * Time: 1:43:56 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class RSnowClusterImpl {

    //   S N O W   C L U S T E R
    //
    //

    final private static Logger log = LoggerFactory.getLogger(RSnowClusterImpl.class);

    private static HashMap<String, RServices> snowServerHolder = new HashMap<String, RServices>();
    
    private static RSnowClusterInterface rShowClusterInterface = new RSnowClusterInterfaceImpl();

    private static HashMap<String, String> poolMapping = null;

    private static long NODE_COUNTER = 0;

    private static long REFRESH = 0;
    private static long STALETIMEOUT = 1000 * 15;

    private static HashMap<String, String> initPoolMapping(String mastername) {

        HashMap<String, String> mapping = new HashMap<String, String>();

        String sitename = makeSiteNameFromServerName(mastername);

        if (sitename != null) {

            SiteOptions.getInstance(sitename).refresh();

            String mappings = SiteOptions.getInstance(sitename)
                    .getSiteData().getClusterPoolMap();

            if (mappings != null && mappings.length() > 0) {

                String[] pairs = mappings.split(",");

                for(String pair : pairs) {
                    pair = pair.trim();

                    if (pair.length() > 0) {
                        int index0 = pair.indexOf('=');
                        if (index0 != -1) {
                            mapping.put(pair.substring(0, index0).trim(),
                                    pair.substring(index0 + 1).trim());
                        }
                    }
                }
            }
        }

        return mapping;
    }

    public static String makeSiteNameFromServerName(String servername) {
        int index = servername.indexOf("_");

        if (index != -1) {
            return servername.substring(0, index);
        }

        return null;
    }


    public static String[] makeClusterNode(String nametemplate, String shortpoolname,
                                           String masterhostname, long port, String outfile) {

        if (poolMapping == null || System.currentTimeMillis() - REFRESH > STALETIMEOUT) {
            poolMapping = initPoolMapping(DirectJNI.getInstanceName());

            // set the refresh time
            REFRESH = System.currentTimeMillis();
        }

        if (poolMapping == null) {
            return new String[] { "NOK",
                    "cannot read pool mappings for master node " + DirectJNI.getInstanceName()};
        }

        try {
            String realpoolname = poolMapping.get(shortpoolname);

            if (realpoolname == null) {
                return new String[] { "NOK",
                        "cluster pool unknown " + shortpoolname };
            }

            nametemplate = nametemplate + "-" + NODE_COUNTER++;

            log.info("nodename=" + nametemplate +
                     " poolname=" + shortpoolname +
                     " realpoolname=" + realpoolname +
                     " master=" + masterhostname + ":" + port +
                     " outfile=" + outfile);

            RServices r = rShowClusterInterface.createR(realpoolname);

            log.info("DirectJNI.getInstanceName() = "+DirectJNI.getInstanceName());

            r.getJobId();

            r.setMaster(DirectJNI.getInstanceName());

            snowServerHolder.put(nametemplate, r);

            String hostname = r.getHostname();

            String command = "Sys.sleep(0.3); require(\"snow\"); startRCLOUDNodeLoop(\"" +
                    masterhostname + "\"," + port + ",\"" + outfile + "\")";

            r.asynchronousConsoleSubmit(command);   // server will not be
                                                    // responsive after this

            return new String[] { "OK", hostname, nametemplate };

        } catch (Exception e) {
            return new String[] { "NOK",
                    "couldn't allocate cluster node " + nametemplate + " from pool " + shortpoolname };
        }
    }

    public static String[] getClusterNodes(String foo) {

        String[] names = new String[snowServerHolder.size()];

        int cnt = 0;

        for (String name : snowServerHolder.keySet()) {
            names[cnt++] = name;
        }

        return names;
    }

    public static String[] releaseClusterNode(String nodename) {
        try {
            RServices r = snowServerHolder.get(nodename);

            if (r != null) {
                r.setMaster(null);

                snowServerHolder.remove(nodename);

                rShowClusterInterface.releaseR(r);

                return new String[] { "OK", "" };

            } else {
                String result = "cluster node " + nodename + " not found";
                log.error(result);
                return new String[] { "NOK", result };
            }

        } catch (Exception e) {
            return new String[] { "NOK",
                    "problem releasing node "+nodename };
        }
    }

    public static void stopAllClusters() {
        log.info("Stop All Snow Clusters");
        Vector<String> s = new Vector<String>(snowServerHolder.keySet());
        for (String server : s) {
            RServices r = snowServerHolder.get(server);
            try {
                rShowClusterInterface.releaseR(r);
            } catch (Exception ex) {
                log.error("Error!", ex);
            }
        }
    }

}
