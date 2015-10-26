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
package uk.ac.ebi.rcloud.http.proxy;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 14, 2011
 * Time: 3:09:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxySettings {
    public String PROXY_HOST = "";
    public int PROXY_PORT    = 8080;
    public String USERNAME   = "";
    public String PASSWORD   = "";

    public ProxySettings() {

    }

    public ProxySettings(String PROXY_HOST, int PROXY_PORT, String USERNAME, String PASSWORD) {
        this.PROXY_HOST = PROXY_HOST;
        this.PROXY_PORT = PROXY_PORT;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;

    }

    public String toSring() {
        return("PROXY host: " + PROXY_HOST + " port: " + PROXY_PORT +
               " user: " + USERNAME + " pswd: " + PASSWORD);

    }
}
