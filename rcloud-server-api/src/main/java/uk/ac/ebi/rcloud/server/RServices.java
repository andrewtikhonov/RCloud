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
package uk.ac.ebi.rcloud.server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import uk.ac.ebi.rcloud.server.RType.RObject;
import uk.ac.ebi.rcloud.server.callback.GenericCallbackDevice;
import uk.ac.ebi.rcloud.server.callback.RActionListener;
import uk.ac.ebi.rcloud.server.callback.RCollaborationListener;
import uk.ac.ebi.rcloud.server.file.FileDescription;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.search.SearchRequest;
import uk.ac.ebi.rcloud.server.search.SearchResult;

public interface RServices extends ManagedRServer {

    // R operations
	public String evaluate(String expression) throws RemoteException;
	public String evaluate(String expression, int n) throws RemoteException;
	public RObject call(String methodName, Object... args) throws RemoteException;
    public void callAndAssign(String varName, String methodName, Object... args) throws RemoteException;
    public void putAndAssign(Object obj, String name) throws RemoteException;
    public RObject getObject(String expression) throws RemoteException;
    public Object getObjectConverted(String expression) throws RemoteException;
    public Object callAndConvert(String methodName, Object... args) throws RemoteException;
    public Object convert(RObject obj) throws RemoteException;

    public RObject callAndGetReference(String methodName, Object... args) throws RemoteException;
    public RObject putAndGetReference(Object obj) throws RemoteException;
    public boolean isReference(RObject obj) throws RemoteException;
    public RObject referenceToObject(RObject refObj) throws RemoteException;
    public void assignReference(String name, RObject refObj) throws RemoteException;
    public RObject getReference(String expression) throws RemoteException;


    public RObject callAndGetObjectName(String methodName, Object... args) throws RemoteException;
    public RObject getObjectName(String expression) throws RemoteException;
    public RObject realizeObjectName(RObject objectName) throws RemoteException;
    public Object realizeObjectNameConverted(RObject objectName) throws RemoteException;
    public void freeReference(RObject refObj) throws RemoteException;
    public void freeAllReferences() throws RemoteException;


    // ...
    public void stop() throws RemoteException;
    void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException;


    // source to R
	public String sourceFromResource(String resource) throws RemoteException;
	public String sourceFromBuffer(String buffer) throws RemoteException;
	public String sourceFromResource(String resource, HashMap<String, Object> attributes) throws RemoteException;
	public String sourceFromBuffer(String buffer, HashMap<String, Object> attributes) throws RemoteException;

    // R status
	public String getStatus() throws RemoteException;

    // packages
	public String[] listPackages() throws RemoteException;
	public RPackage getPackage(String packageName) throws RemoteException;
	public boolean symbolExists(String symbol) throws RemoteException;
	
	// collaboration
    public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException;
	public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException;	
	public void removeAllRCollaborationListeners() throws RemoteException;
	public boolean hasRCollaborationListeners() throws RemoteException;

    // R console listeners
	public void addRConsoleActionListener(RActionListener ractionListener) throws RemoteException;
	public void removeRConsoleActionListener(RActionListener ractionListener) throws RemoteException;
	public void removeAllRConsoleActionListeners() throws RemoteException;

    // generic callback
    public GenericCallbackDevice newGenericCallbackDevice() throws RemoteException;
    public GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException;

    // R user
	public void  registerUser(String sourceUID,String user) throws RemoteException;
	public void  unregisterUser(String sourceUID) throws RemoteException;
	public void  updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException;
	public UserStatus[] getUserStatusTable() throws RemoteException;

	public void setUserInput(String userInput) throws RemoteException;

    // devices
    public GDDevice newDevice(int w, int h) throws java.rmi.RemoteException;
    public GDDevice newBroadcastedDevice(int w, int h) throws java.rmi.RemoteException;
    public GDDevice[] listDevices() throws java.rmi.RemoteException;

    //
	void chat(String sourceUID,String user, String message) throws RemoteException;
    public String unsafeGetObjectAsString( String cmd ) throws RemoteException;
	public RNI getRNI() throws RemoteException;

    // access random files
    public void createRandomAccessFile(String fileName) throws java.rmi.RemoteException;
    public void createRandomAccessDir(String  dirName) throws java.rmi.RemoteException;
    public void renameRandomAccessFile(String fileName1, String fileName2) throws java.rmi.RemoteException;
    public void removeRandomAccessFile(String fileName) throws java.rmi.RemoteException;

    public void appendBlockToRandomAccessFile(String fileName, byte[] block) throws java.rmi.RemoteException;

    public void copyRandomAccessFile(String srcFileName, String dstFileName) throws java.rmi.RemoteException;
    public void copyRandomAccessDir(String srcPath, String dstPath) throws java.rmi.RemoteException;
    public void moveRandomAccessFile(String srcFileName, String dstFileName) throws java.rmi.RemoteException;
    public void moveRandomAccessDir(String srcPath, String dstPath) throws java.rmi.RemoteException;

    public byte[] readRandomAccessFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException;
    public FileDescription getRandomAccessFileDescription(String fileName) throws java.rmi.RemoteException;

    // working directory
    public String getWorkingDirectory() throws java.rmi.RemoteException;
    public void setWorkingDirectory(String dir) throws java.rmi.RemoteException;
	public FileDescription[] getWorkingDirectoryFileDescriptions() throws java.rmi.RemoteException;

    // accessing working-directory files
    public void createWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException;
    public void removeWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException;
    public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws java.rmi.RemoteException;
    public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException;
    public FileDescription getWorkingDirectoryFileDescription(String fileName) throws java.rmi.RemoteException;

    // reinit
    public void reinitServer() throws java.rmi.RemoteException;

    //
	public String[] listDemos() throws java.rmi.RemoteException;
	public String getDemoSource(String demoName) throws java.rmi.RemoteException;

    // help
    //
	public byte[] getRHelpFile(String uri) throws java.rmi.RemoteException;
	public String getRHelpFileUri(String topic, String pack) throws java.rmi.RemoteException;

    // history
    public void saveHistory(Vector<String> history, String filename, boolean append) throws RemoteException;
    public Vector<String> loadHistory(String filename) throws RemoteException;

    // util
	public Vector<String> getSvg(String script, int width, int height) throws RemoteException;	
	public byte[] getPdf(String script, int width, int height) throws RemoteException;	
	
    // probed variables
    public void addProbeOnVariables(String[] variables) throws RemoteException;
    public void removeProbeOnVariables(String[] variables) throws RemoteException;
    public String[] getProbedVariables() throws RemoteException;
    public void setProbedVariables(String[] variables) throws RemoteException;

    // libraries
    public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException;

    // exec
    public String[] exec(String command) throws RemoteException;
    public String[] exec(String command, HashMap<String, Object> attributes) throws RemoteException;

    // search
    public Vector<SearchResult> search(SearchRequest request) throws RemoteException;
    public Vector<SearchResult> search(SearchRequest request, HashMap<String, Object> attributes) throws RemoteException;
    public void searchAsync(SearchRequest request) throws RemoteException;
    public void searchAsync(final SearchRequest request, final HashMap<String, Object> attributes) throws RemoteException;


}
