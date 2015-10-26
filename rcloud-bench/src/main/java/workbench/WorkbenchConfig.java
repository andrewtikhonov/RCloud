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
package workbench;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 18, 2009
 * Time: 5:40:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkbenchConfig extends XMLConfiguration {

    final private static Logger log = LoggerFactory.getLogger(WorkbenchConfig.class);

    private static String configFile = "config/Workbench.xml";

    private static Integer singletonLock = new Integer(0);

    private static WorkbenchConfig instance = null;

    public static WorkbenchConfig getInstance() {
        if (instance == null) {
            synchronized (singletonLock) {
                instance = new WorkbenchConfig(configFile);
                return instance;
            }
        } else {
            return instance;
        }
    }

    private WorkbenchConfig(String fileName) {
        setFileName(fileName);
        try {
            load();
            readAliasNames();
        } catch (ConfigurationException ex) {
            log.error("Error!", ex);
        }
    }

    private String[] aliasNames;

    private void readAliasNames() {
        Object namesObj = getProperty("aliasMap.names");
        aliasNames = ((String)namesObj).split("\\s+");
    }

    public String[] getAliasNames() {
        return aliasNames;
    }

    public static void main(String args[]) {
        WorkbenchConfig config = WorkbenchConfig.getInstance();

        String[] names = config.getAliasNames();
        for(String s : names) {
            log.info(s);
        }
    }

}
