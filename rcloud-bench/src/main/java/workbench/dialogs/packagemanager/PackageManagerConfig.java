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
package workbench.dialogs.packagemanager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 17, 2009
 * Time: 2:49:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageManagerConfig extends XMLConfiguration {

    final private static Logger log = LoggerFactory.getLogger(PackageManagerConfig.class);

    private static String configFile = "config/PackageManager.xml";

    private static Integer singletonLock = new Integer(0);

    private static PackageManagerConfig instance = null;

    public static String LOAD_PACKAGE = "commands.load-package";
    public static String UNLOAD_PACKAGE = "commands.unload-package";


    public static PackageManagerConfig getInstance() {
        if (instance == null) {
            synchronized (singletonLock) {
                instance = new PackageManagerConfig(configFile);
                return instance;
            }
        } else {
            return instance;
        }
    }

    private PackageManagerConfig(String fileName) {
        setFileName(fileName);
        try {
            load();
        } catch (ConfigurationException ex) {
            log.error("Error!", ex);
        }
    }

    public static void main(String args[]) {
        PackageManagerConfig config = PackageManagerConfig.getInstance();
        log.info(config.getString(LOAD_PACKAGE));
        log.info(config.getString(UNLOAD_PACKAGE));
    }

}
