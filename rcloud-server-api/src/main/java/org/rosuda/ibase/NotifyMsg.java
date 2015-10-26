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
package org.rosuda.ibase;

import java.io.Serializable;

/** messages passed with NotifyAllDep
    @version $Id: NotifyMsg.java 445 2003-07-29 21:54:22Z starsoft $ */
public class NotifyMsg implements Serializable{
    Object source;
    int    messageID;
    String cmd;
    Object[] par;

    public NotifyMsg(Object src, int msgid, String command, Object[] params) {
        source=null; messageID=msgid; cmd=command; par=params;	
    }

    public NotifyMsg(Object src, int msgid, String command) {
	this(src, msgid, command, null);
    }

    public NotifyMsg(Object src, int msgid) {
	this(src, msgid, null, null);
    }

    public NotifyMsg(Object src) {
        this(src, 0, null, null);
    }

    /*
    public Object getSource() {
        return source;
    }
    */

    public int getMessageID() {
        return messageID;
    }

    public String getCommand() {
	return cmd;
    }

    public Object[] getParams() {
	return par;
    }

    public int parCount() {
	return (par==null)?0:par.length;
    }

    public Object parAt(int pos) {
	return (par==null||pos<0||pos>=par.length)?null:par[pos];
    }

    public int parI(int pos) {
	return (par==null||pos<0||pos>=par.length)?0:(((Number)par[pos]).intValue());
    }

    public double parD(int pos) {
	return (par==null||pos<0||pos>=par.length)?0:(((Number)par[pos]).doubleValue());
    }

    public String parS(int pos) {
	return (par==null||pos<0||pos>=par.length)?null:(par[pos].toString());
    }

    public String toString() {
        return "NotifyMsg["+messageID+"]from["+source+"]";
    }
}
