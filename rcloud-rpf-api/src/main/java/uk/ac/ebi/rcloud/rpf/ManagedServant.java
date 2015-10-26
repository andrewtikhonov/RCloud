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
package uk.ac.ebi.rcloud.rpf;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

public interface ManagedServant extends java.rmi.Remote {

    public String ping() throws java.rmi.RemoteException;

    public void die() throws java.rmi.RemoteException;

    public void shutdown() throws java.rmi.RemoteException;

    public void saveAndShutdown(String path) throws java.rmi.RemoteException;

    // server reset

	public void reset() throws java.rmi.RemoteException;

    public boolean isResetEnabled() throws java.rmi.RemoteException;

    public void setResetEnabled(boolean enable) throws java.rmi.RemoteException;

    // work space
    public void loadWorkspace(String path) throws java.rmi.RemoteException;

    public void saveWorkspace(String path) throws java.rmi.RemoteException;


    // logs

	public String getLogs() throws java.rmi.RemoteException;

    public void addLogListener(RemoteLogListener listener) throws java.rmi.RemoteException;

    public void removeLogListener(RemoteLogListener listener) throws java.rmi.RemoteException;

    public void removeAllLogListeners() throws java.rmi.RemoteException;

    public void logInfo(String message) throws java.rmi.RemoteException;

    // parameters

	public String getServantName() throws java.rmi.RemoteException;

    // console mode

	public boolean hasConsoleMode() throws java.rmi.RemoteException;

	public String consoleSubmit(String cmd) throws java.rmi.RemoteException;

    public String consoleSubmit(String cmd, HashMap<String, Object> attributes) throws java.rmi.RemoteException;

	public void asynchronousConsoleSubmit(String cmd) throws java.rmi.RemoteException;

    public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> attributes) throws java.rmi.RemoteException;


    // info
	public boolean isBusy() throws java.rmi.RemoteException;

    // push/pop mode

    public boolean hasPushPopMode() throws java.rmi.RemoteException;

	public Serializable pop(String symbol) throws java.rmi.RemoteException;

	public void push(String symbol, Serializable object) throws java.rmi.RemoteException;

	public String[] listSymbols() throws java.rmi.RemoteException;

	// graphic mode

    public boolean hasGraphicMode() throws java.rmi.RemoteException;

	public RemotePanel getPanel(int w, int h) throws java.rmi.RemoteException;

    // server system properties

	public String getProcessId() throws java.rmi.RemoteException;

	public String getHostIp() throws java.rmi.RemoteException;

    public String getHostname() throws java.rmi.RemoteException;

	public String getJobId() throws java.rmi.RemoteException;
		
	public void setJobId(String jobId) throws java.rmi.RemoteException;
	
	public String getStub() throws java.rmi.RemoteException;
	
	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException ;

    // properties

    public String getProperty(String name) throws RemoteException ;

    public Hashtable<Object, Object> getProperties() throws RemoteException ;

    public void setProperty(String name, String value) throws RemoteException ;

}
