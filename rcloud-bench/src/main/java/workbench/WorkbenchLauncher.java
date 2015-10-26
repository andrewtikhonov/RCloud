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

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.swing.JFrame;


import workbench.splashscreen.SplashWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchLauncher {
    final private static Logger log = LoggerFactory.getLogger(WorkbenchLauncher.class);
    static Workbench workbench = null;

	static public void createDesktopApplication() {

		try {
            Workbench.setupLookAndFeel();

            Workbench workbenchTmp = new Workbench();

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					workbench.destroy();
				}
			}));

			workbenchTmp.setPreferredSize(new Dimension(1024, 768));
			workbenchTmp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//EXIT_ON_CLOSE);

			workbenchTmp.pack();
            workbenchTmp.setLocationRelativeTo(null);

            workbench = workbenchTmp;

            workbench.setVisible(true);

		} catch (Exception e) {
            log.error("Error!", e);
		}
	}

    static class WarmupRunnable implements Runnable {
        private String getClassPath(){
            return System.getProperty("java.class.path");
        }
        private void locateJarClasses(Vector<String> classCache, File file) {
            try {
                JarInputStream jis = new JarInputStream(new FileInputStream(file));

                while(true) {
                    JarEntry jarentry = jis.getNextJarEntry();
                    if (jarentry == null) {
                        break;
                    }

                    String name = jarentry.getName();
                    if (!name.contains("springframework")) {

                        int index = name.lastIndexOf(".class");
                        if (index != -1) {
                            String classname = name.substring(0, index).replace('/', '.');
                            classCache.add(classname);
                        }
                    }
                }

            } catch(Exception ex) {
                log.error("Error!", ex);
            }
        }

        private void locateCompiledClasses(Vector<String> classCache, File[] files, String path) {
            String classsuffix = ".class";

            for (File f : files) {
                if (f.isDirectory()) {
                    String path0 = (path.length() == 0 ? path : path + ".");

                    locateCompiledClasses(classCache, f.listFiles(),  path0 + f.getName());
                } else {
                    String filename = f.getName();

                    if (filename.endsWith(classsuffix)) {

                        String classname = path + "." +
                                filename.substring(0, filename.length() - classsuffix.length());

                        classCache.add(classname);
                    }
                }
            }

        }


        private void warmup(ClassLoader classloader) {

            log.info("warning up...");

            String classpath = getClassPath();

            String[] resources = classpath.split(":");

            long ct0 = System.currentTimeMillis();

            //ClassLoader classloader = WarmupTest.class.getClassLoader();

            Vector<String> classCache = new Vector<String>();

            for (String resource : resources) {

                if (resource.contains("rcloud")) {
                    File resfile = new File(resource);

                    if (!resfile.isDirectory()) {
                        locateJarClasses(classCache, resfile);
                    } else {
                        locateCompiledClasses(classCache, resfile.listFiles(), "");
                    }
                }
            }

            log.info("loading " + classCache.size() + " classes");

            for(String cl : classCache) {

                try {
                    if (!cl.contains("remoting")) {
                        classloader.loadClass(cl);
                    }
                } catch (Exception ex) {
                }
            }

            log.info("done");

            long ct1 = System.currentTimeMillis();
            log.info("warm took " + ((ct1 - ct0)/1000) + " seconds" );
        }

        public void run(){
            warmup(Workbench.class.getClassLoader());
        }
    }


	public static void main(String[] args) throws Exception {
        new SplashWindow().start(3000);
        createDesktopApplication();

	}
}
