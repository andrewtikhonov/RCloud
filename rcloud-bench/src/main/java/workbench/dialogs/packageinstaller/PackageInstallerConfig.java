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
package workbench.dialogs.packageinstaller;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 17, 2009
 * Time: 3:21:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageInstallerConfig extends XMLConfiguration {

    final private static Logger log = LoggerFactory.getLogger(PackageInstallerConfig.class);

    private static String configFile = "config/PackageInstaller.xml";

    private static Integer singletonLock = new Integer(0);

    private static PackageInstallerConfig instance = null;

    public static PackageInstallerConfig getInstance() {
        if (instance == null) {
            synchronized (singletonLock) {
                instance = new PackageInstallerConfig(configFile);
                return instance;
            }
        } else {
            return instance;
        }
    }

    private PackageInstallerConfig(String fileName) {
        setFileName(fileName);
        try {
            load();
        } catch (ConfigurationException ex) {
            log.error("Error!", ex);
        }

        readInRepositoryData();
    }

    public static final String REPOSITORY = "repositories.repository";

    public String[] getInitCommands()  {
        String[] commands = null;

        Object obj = getProperty("init.command");
        if (obj instanceof Collection) {
            int size = ((Collection) obj).size();
            commands = new String[size];
            for (int i=0;i<size;i++) {
                commands[i] = (String) ((ArrayList) obj).get(i);
            }

        } else if (obj instanceof String) {
            commands = new String[1];
            commands[0] = (String)obj;
        }
        return commands;
    }

    private String resolve(String value) {
        try {
            String obj = getString(value);
            if (obj == null) {
                return value;
            } else {
                return resolve(obj);
            }
        } catch (Exception ex) {
            return value;
        }
    }

    public String getCollectionProperty(String p, int i, String s) {
        Object obj = instance.getProperty(p + s);
        if (obj instanceof Collection) {
            return resolve((String)instance.getProperty(p + "(" + i + ")" + s));

        } else {
            return resolve((String)instance.getProperty(p + s));
        }
    }

    public String getRepositoryName(int i) {
        return repositoryData[i][NAME];
    }

    public String getRepositoryUrl(int i) {
        return repositoryData[i][URL];
    }

    public String getRepositoryInitCmd(int i) {
        return repositoryData[i][INIT_CMD];
    }

    public String getRepositoryInstallCmd(int i) {
        return repositoryData[i][INST_CMD];
    }

    public int getRepositoryCount() {
        return repositoryData.length;
    }

    private static final int NAME = 0;
    private static final int URL = 1;
    private static final int INIT_CMD = 2;
    private static final int INST_CMD = 3;
    private static final int NUM = INST_CMD + 1;

    private String[][] repositoryData;

    private void readInRepositoryData() {

        Object nameObj = getProperty(REPOSITORY + ".name");
        Object urlObj = getProperty(REPOSITORY + ".url");
        Object initObj = getProperty(REPOSITORY + ".repository-init-cmd");
        Object instObj = getProperty(REPOSITORY + ".package-install-cmd");

        if (nameObj instanceof Collection) {
            int size = ((Collection) nameObj).size();
            repositoryData = new String[size][NUM];

            for(int i=0;i<size;i++) {
                repositoryData[i][NAME] = resolve((String) ((ArrayList) nameObj).get(i));
                repositoryData[i][URL] = resolve((String) ((ArrayList) urlObj).get(i));
                repositoryData[i][INIT_CMD] = resolve((String) ((ArrayList) initObj).get(i));
                repositoryData[i][INST_CMD] = resolve((String) ((ArrayList) instObj).get(i));
            }

        } else if (nameObj instanceof String) {
            repositoryData = new String[1][NUM];

            repositoryData[0][NAME] = resolve((String) nameObj);
            repositoryData[0][URL] = resolve((String) urlObj);
            repositoryData[0][INIT_CMD] = resolve((String)initObj);
            repositoryData[0][INST_CMD] = resolve((String)instObj);
        }
    }


    public static void main(String args[]) {
        PackageInstallerConfig config = PackageInstallerConfig.getInstance();
        //log.info(config.getString(""));
        //log.info(config.getString(""));

        int cnt = config.getRepositoryCount();

        for (int i = 0; i < cnt; i++) {
            log.info(config.getRepositoryName(i));
            log.info(config.getRepositoryUrl(i));
            log.info(config.getRepositoryInitCmd(i));
            log.info(config.getRepositoryInstallCmd(i));
            log.info("----");
        }

        String[] cmd = config.getInitCommands();

        for (int i=0;i<cmd.length;i++) {
            log.info("cmd=" + cmd[i]);
        }

    }
}