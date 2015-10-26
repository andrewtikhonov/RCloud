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
package uk.ac.ebi.rcloud.rpf.util;

import uk.ac.ebi.rcloud.rpf.PoolUtils;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 23/05/2011
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class ServerUtil {

    /**
     * Obtain the name this servant is registered under
     *
     * @return Servant's name, if any.
     * @throws java.rmi.RemoteException
     */
    public static String makeName(String servantPoolPrefix, Registry rmiRegistry) throws RemoteException {

        String servantName = null;
        String[] servantNames = rmiRegistry.list();

        Vector<Integer> idsVector = new Vector<Integer>();
        for (int i = 0; i < servantNames.length; ++i) {
            String name = PoolUtils.shortRmiName(servantNames[i]);
            if (name.startsWith(servantPoolPrefix)) {
                String prefix = name.substring(servantPoolPrefix.length());
                try {
                    idsVector.add(Integer.decode(prefix));
                } catch (Exception e) {
                }
            }
        }

        if (idsVector.size() == 0) {
            servantName = servantPoolPrefix + "1";
        } else {
            idsVector.add(0);
            int[] ids = new int[idsVector.size()];
            for (int i = 0; i < ids.length; ++i) {
                ids[i] = idsVector.elementAt(i);
            }
            Arrays.sort(ids);

            for (int i = 0; i < ids.length - 1; ++i) {
                if (ids[i + 1] > (ids[i] + 1)) {
                    servantName = servantPoolPrefix + (ids[i] + 1);
                    break;
                }
            }
            if (servantName == null) {
                servantName = servantPoolPrefix + (ids[ids.length - 1] + 1);
            }
        }
        return servantName;

    }

}
