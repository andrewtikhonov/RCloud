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
package uk.ac.ebi.rcloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.NoSuchObjectException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Apr 6, 2010
 * Time: 1:40:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class HexUtil {

    final private static Logger log = LoggerFactory.getLogger(HexUtil.class);

    public static String bytesToHex(byte in[]) {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0)
            return null;
        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
        StringBuffer out = new StringBuffer(in.length * 2);
        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0);
            ch = (byte) (ch >>> 4);
            ch = (byte) (ch & 0x0F);
            out.append(pseudo[(int) ch]);
            ch = (byte) (in[i] & 0x0F);
            out.append(pseudo[(int) ch]);
            i++;
        }
        String rslt = new String(out);
        return rslt;
    }

    public static final byte[] hexToBytes(String s) throws NumberFormatException, IndexOutOfBoundsException {
        int slen = s.length();
        if ((slen % 2) != 0) {
            s = '0' + s;
        }

        byte[] out = new byte[slen / 2];

        byte b1, b2;
        for (int i = 0; i < slen; i += 2) {
            b1 = (byte) Character.digit(s.charAt(i), 16);
            b2 = (byte) Character.digit(s.charAt(i + 1), 16);
            if ((b1 < 0) || (b2 < 0)) {
                throw new NumberFormatException();
            }
            out[i / 2] = (byte) (b1 << 4 | b2);
        }
        return out;
    }

    public static byte[] objectToBytes(Object obj) throws NoSuchObjectException {
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baoStream);
            oos.writeObject(obj);
            oos.flush();
        } catch (Exception e) {
            log.error("Error!", e);
        }
        return baoStream.toByteArray();
    }

    public static Object bytesToObject(byte[] b) {
        try {
            return (Object) new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
        } catch (Exception e) {
            log.error("Error!", e);
            return null;
        }
    }

    public static String objectToHex(Object obj) {

        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(baoStream).writeObject(obj);
        } catch (Exception e) {
            log.error("Error!", e);
        }
        return bytesToHex(baoStream.toByteArray());
    }

    public static Object hexToObject(String hex) {
        if (hex == null || hex.equals(""))
            return null;
        try {
            return (Object) new ObjectInputStream(new ByteArrayInputStream(hexToBytes(hex))).readObject();
        } catch (Exception e) {
            log.error("Error!", e);
            return null;
        }
    }



}
