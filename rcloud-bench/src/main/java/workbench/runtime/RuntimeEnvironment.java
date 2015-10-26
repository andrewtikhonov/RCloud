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
package workbench.runtime;

import uk.ac.ebi.rcloud.http.proxy.ProxySettings;
import uk.ac.ebi.rcloud.http.proxy.ServerSession;
import uk.ac.ebi.rcloud.rpf.db.DAOLayerInterface;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 30, 2009
 * Time: 12:56:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeEnvironment {

    // running Modes
	public static final int NEW_R_MODE = 0;
	public static final int RMI_MODE = 1;
	public static final int HTTP_MODE = 2;

    private String baseUrl = "http://127.0.0.1:8080/rcloud";

    private ProjectDataDAO project = null;

    private UserDataDAO user = null;

    private String servername = null;

    private DAOLayerInterface daoLayer = null;

    private ServerSession session = new ServerSession(baseUrl, null);

    private boolean wait = false;

    private int mode = HTTP_MODE;

    private ProxySettings settings = null;

    private boolean proxyauto = true;

    public RuntimeEnvironment() {
    }

    public ServerSession getSession() {
        return session;
    }

    public void setSession(ServerSession session) {
        this.session = session;
    }

    public String getServer() {
        return servername;
    }

    public void setServer(String servername) {
        this.servername = servername;
    }

    public UserDataDAO getUser() {
        return user;
    }

    public void setUser(UserDataDAO user) {
        this.user = user;
    }

    public ProjectDataDAO getProject() {
        return project;
    }

    public void setProject(ProjectDataDAO project) {
        this.project = project;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getCommandUrl() {
        return baseUrl + "/cmd";
    }

    public String getHelpUrl() {
        return baseUrl + "/helpme";
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        session.setCommandurl(getCommandUrl());
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isWait() {
        return wait;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    public DAOLayerInterface getDAOLayer() {
        return this.daoLayer;
    }

    public void setDAOLayer(DAOLayerInterface daoLayer) {
        this.daoLayer = daoLayer;
    }

    public ProxySettings getProxySettings() {
        return settings;
    }

    public void setProxySettings(ProxySettings settings) {
        this.settings = settings;
    }

    public boolean getProxyAuto() {
        return proxyauto;
    }

    public void setProxyAuto(boolean proxyauto) {
        this.proxyauto = proxyauto;
    }

}