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
import uk.ac.ebi.rcloud.rpf.PoolUtils;
import uk.ac.ebi.rcloud.rpf.ServantProviderFactory;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.RType.*;
import uk.ac.ebi.rcloud.server.cluster.Cluster;
import uk.ac.ebi.rcloud.server.cluster.RClusterInterface;
import uk.ac.ebi.rcloud.server.cluster.impl.RClusterInterfaceImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 16, 2010
 * Time: 1:36:20 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class RClusterImpl {

    final private static Logger log = LoggerFactory.getLogger(RClusterImpl.class);

    private static HashMap<String, Cluster> _clustersHash = new HashMap<String, Cluster>();

    private static RClusterInterface rClusterInterface = new RClusterInterfaceImpl();

    private static long CLUSTER_COUNTER = 0;

    //   C L U S T E R
    //
    //

    public static void setRClusterInterface(RClusterInterface clusterInterface) {
        rClusterInterface = clusterInterface;
    }

    public static String[] makeCluster(long n, String poolName) {
        Vector<RServices> workers = null;
        try {
            workers = rClusterInterface.createRs((int) n, poolName);
            String clusterName = "CL_" + (CLUSTER_COUNTER++);
            _clustersHash.put(clusterName, new Cluster(clusterName, workers, poolName));
            return new String[] { "OK", clusterName };

        } catch (Exception e) {

            log.error("Error!", e);
            return new String[] { "NOK", convertToPrintCommand("couldn't create cluster") };
        }

    }

    public static String[] setClusterProperties(String gprops) {
        if (!new File(gprops).exists()) {
            return new String[] { "NOK", "The file '" + gprops + "' doesn't exist" };
        } else {
            try {
                System.setProperty("properties.extension", gprops);
                ServantProviderFactory.init();
                return new String[] { "OK" };
            } catch (Exception e) {
                return new String[] { "NOK", PoolUtils.getStackTraceAsString(e) };
            }
        }
    }

    public interface VWrapper {
        public int getSize();

        public RObject getElementAt(int i);

        public Object gatherResults(RObject[] f);
    }

    public static RInteger nullObject = new RInteger(-11);

    public static String[] clusterApply(String cl, String varName, final String functionName) {
        try {

            Cluster cluster = _clustersHash.get(cl);
            if (cluster == null)
                return new String[] { "NOK", "Invalid cluster" };
            RObject v = DirectJNI.getInstance().getObjectFrom(varName, true);
            RObject vtemp = null;
            if (v.getClass() == RMatrix.class) {
                vtemp = ((RMatrix) v).getValue();
            } else if (v.getClass() == RArray.class) {
                vtemp = ((RArray) v).getValue();
            } else {
                vtemp = v;
            }

            final RObject var = vtemp;

            final VWrapper vwrapper = new VWrapper() {
                public int getSize() {
                    if (var.getClass() == RNumeric.class) {
                        return ((RNumeric) var).getValue().length;
                    } else if (var.getClass() == RInteger.class) {
                        return ((RInteger) var).getValue().length;
                    } else if (var.getClass() == RChar.class) {
                        return ((RChar) var).getValue().length;
                    } else if (var.getClass() == RLogical.class) {
                        return ((RLogical) var).getValue().length;
                    } else if (var.getClass() == RComplex.class) {
                        return ((RComplex) var).getReal().length;
                    } else if (var.getClass() == RList.class) {
                        return ((RList) var).getValue().length;
                    }
                    return 0;
                }

                public RObject getElementAt(int i) {
                    if (var.getClass() == RNumeric.class) {
                        return new RNumeric(((RNumeric) var).getValue()[i]);
                    } else if (var.getClass() == RInteger.class) {
                        return new RInteger(((RInteger) var).getValue()[i]);
                    } else if (var.getClass() == RChar.class) {
                        return new RChar(((RChar) var).getValue()[i]);
                    } else if (var.getClass() == RLogical.class) {
                        return new RLogical(((RLogical) var).getValue()[i]);
                    } else if (var.getClass() == RComplex.class) {
                        return new RComplex(new double[] { ((RComplex) var).getReal()[i] }, new double[] { ((RComplex) var).getImaginary()[i] },
                                ((RComplex) var).getIndexNA() != null ? new int[] { ((RComplex) var).getIndexNA()[i] } : null,
                                ((RComplex) var).getNames() != null ? new String[] { ((RComplex) var).getNames()[i] } : null);
                    }

                    else if (var.getClass() == RList.class) {
                        return (RObject) ((RList) var).getValue()[i];
                    }
                    return null;
                }

                public Object gatherResults(RObject[] f) {

                    if (var.getClass() == RList.class) {
                        return f;
                    } else {
                        Class<?> resultClass = f[0].getClass();
                        RObject result = null;
                        if (resultClass == RNumeric.class) {
                            double[] t = new double[f.length];
                            for (int i = 0; i < f.length; ++i)
                                t[i] = ((RNumeric) f[i]).getValue()[0];
                            result = new RNumeric(t);
                        } else if (resultClass == RInteger.class) {
                            int[] t = new int[f.length];
                            for (int i = 0; i < f.length; ++i)
                                t[i] = ((RInteger) f[i]).getValue()[0];
                            result = new RInteger(t);
                        } else if (resultClass == RChar.class) {
                            String[] t = new String[f.length];
                            for (int i = 0; i < f.length; ++i)
                                t[i] = ((RChar) f[i]).getValue()[0];
                            result = new RChar(t);
                        } else if (resultClass == RLogical.class) {
                            boolean[] t = new boolean[f.length];
                            for (int i = 0; i < f.length; ++i)
                                t[i] = ((RLogical) f[i]).getValue()[0];
                            result = new RLogical(t);
                        } else if (resultClass == RComplex.class) {
                            double[] real = new double[f.length];
                            double[] im = new double[f.length];

                            for (int i = 0; i < f.length; ++i) {
                                real[i] = ((RComplex) f[i]).getReal()[0];
                                im[i] = ((RComplex) f[i]).getImaginary()[0];
                            }

                            result = new RComplex(real, im, null, null);
                        } else {
                            throw new RuntimeException("Can't Handle this result type :" + resultClass.getName());
                        }
                        return result;
                    }

                }
            };

            if (vwrapper.getSize() == 0)
                return new String[] { "NOK", "0 elements in data" };

            Vector<RServices> workers = cluster.getWorkers();

            final ArrayBlockingQueue<Integer> indexesQueue = new ArrayBlockingQueue<Integer>(vwrapper.getSize());
            for (int i = 0; i < vwrapper.getSize(); ++i)
                indexesQueue.add(i);

            final ArrayBlockingQueue<RServices> workersQueue = new ArrayBlockingQueue<RServices>(workers.size());
            for (int i = 0; i < workers.size(); ++i)
                workersQueue.add(workers.elementAt(i));

            final RObject[] result = new RObject[vwrapper.getSize()];

            for (int i = 0; i < workers.size(); ++i) {
                new Thread(new Runnable() {
                    public void run() {
                        RServices r = workersQueue.poll();
                        while (indexesQueue.size() > 0) {
                            Integer idx = indexesQueue.poll();
                            if (idx != null) {
                                try {
                                    result[idx] = r.call(functionName, vwrapper.getElementAt(idx));
                                } catch (Exception e) {
                                    log.error("Error!", e);
                                    result[idx] = nullObject;
                                }
                            }
                        }
                    }
                }).start();
            }

            while (true) {
                int count = 0;
                for (int i = 0; i < result.length; ++i)
                    if (result[i] != null)
                        ++count;
                if (count == result.length)
                    break;
                Thread.sleep(100);
            }

            Object reconstituedObject = vwrapper.gatherResults(result);
            if (v.getClass() == RMatrix.class) {
                ((RArray) v).setValue((RVector) reconstituedObject);
            } else if (v.getClass() == RArray.class) {
                ((RArray) v).setValue((RVector) reconstituedObject);
            } else if (v.getClass() == RList.class) {
                ((RList) v).setValue((RObject[]) reconstituedObject);
            } else {
                v = (RObject) reconstituedObject;
            }

            DirectJNI.getInstance().putObjectAndAssignName(v, "clusterApplyResult", true);
            // DirectJNI.getInstance().putObjectAndAssignName(new RNumeric(12)
            // ,"clusterApplyResult" , true);

            return new String[] { "OK" };
        } catch (Exception e) {
            return new String[] { "NOK", PoolUtils.getStackTraceAsString(e) };
        }
    }

    public static String[] clusterEvalQ(String cl, String expression) {
        try {
            StringBuffer feedback = new StringBuffer();
            Cluster cluster = _clustersHash.get(cl);
            if (cluster == null)
                return new String[] { "NOK", "Invalid cluster" };
            for (int i = 0; i < cluster.getWorkers().size(); ++i) {
                RServices r = cluster.getWorkers().elementAt(i);
                r.consoleSubmit(expression);
                String s = r.getStatus();
                log.info("** submitted :" + expression + " to " + r.getServantName());
                if (s != null && !s.trim().equals(""))
                    feedback.append("worker<" + r.getServantName() + ">:\n" + s + "\n");
            }
            log.info("##<" + feedback + ">##");
            return new String[] { "OK", convertToPrintCommand(feedback.toString()) };
        } catch (Exception e) {
            return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
        }
    }

    public static String[] clusterExport(String cl, String exp, String ato) {
        try {
            StringBuffer feedback = new StringBuffer();
            Cluster cluster = _clustersHash.get(cl);
            if (cluster == null)
                return new String[] { "NOK", "Invalid cluster" };
            RObject v = DirectJNI.getInstance().getObjectFrom(exp, true);
            if (ato.equals("")) ato=exp;
            for (int i = 0; i < cluster.getWorkers().size(); ++i) {
                RServices r = cluster.getWorkers().elementAt(i);
                r.putAndAssign(v, ato);
                String s = r.getStatus();
                if (s != null && !s.trim().equals(""))
                    feedback.append("worker<" + r.getServantName() + ">:" + s + "\n");
            }
            return new String[] { "OK", convertToPrintCommand(feedback.toString()) };
        } catch (Exception e) {
            return new String[] { "NOK", PoolUtils.getStackTraceAsString(e) };
        }
    }

    public static String[] stopCluster(String cl) {
        Cluster cluster = _clustersHash.get(cl);
        if (cluster == null)
            return new String[] { "NOK", "Invalid cluster" };
        try {
             rClusterInterface.releaseRs(cluster.getWorkers());
        } catch (Exception e) {
            log.error("Error!", e);
        }
        _clustersHash.remove(cl);
        return new String[] { "OK" };
    }

    public static void stopAllClusters() {
        log.info("Stop All Clusters");
		Vector<String> v = new Vector<String>(_clustersHash.keySet());
		for (String cl : v) {
			Cluster cluster = _clustersHash.get(cl);
			stopCluster(cluster.getName());
		}
    }

    public static String convertToPrintCommand(String s) {
        if (s.length() == 0)
            return "";
        StringBuffer result = new StringBuffer();
        result.append("print(\"");
        for (int i = 0; i < s.length(); ++i) {
            char si = s.charAt(i);
            if (si == '\n') {
                if (i == s.length() - 1) {

                } else {
                    result.append("\",quote=FALSE);print(\"");
                }
            } else if (si == '\"') {
                result.append("\\'");
            } else if (si == '\t') {
                result.append("    ");
            } else if (si == '\r') {
                result.append("");
            } else if (si == '\\') {
                result.append("/");
            } else {
                result.append(si);
            }
        }
        result.append("\",quote=FALSE);");
        return result.toString();
    }

}
