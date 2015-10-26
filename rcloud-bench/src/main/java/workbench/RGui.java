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

import java.awt.Component;
import java.util.HashMap;

import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import uk.ac.ebi.rcloud.http.proxy.ProxySettings;
import uk.ac.ebi.rcloud.server.RKit;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.rpf.db.dao.UserDataDAO;
import workbench.events.VariablesChangeListener;
import workbench.generic.RConsole;
import workbench.graphics.GDContainerPanel;
import workbench.graphics.RConsoleProvider;
import workbench.manager.logman.WorkbenchLogContainer;
import workbench.manager.runner.RActionHandlerInterface;
import workbench.manager.viewman.ViewManager;
import workbench.runtime.RuntimeEnvironment;
import workbench.views.rconsole.WorkbenchRConsole;
import workbench.manager.opman.OperationManager;
import uk.ac.ebi.rcloud.rpf.db.dao.ProjectDataDAO;
import net.infonode.docking.View;

import javax.swing.*;

public interface RGui extends RKit {

	public ConsoleLogger getConsoleLogger();

	public View createView(Component panel, String title);

	public void setCurrentDevice(GDDevice device);

	public Component getRootComponent();

	public GDDevice getCurrentDevice();

	public GDContainerPanel getCurrentJGPanelPop();

	public String getUserName();

	public String getUID();
	
	public String getSessionId();

	public String getInstallDir();
		
	//void pushTask(Runnable task);
	
	public void addVariablesChangeListener(VariablesChangeListener listener);

	public void removeVariablesChangeListener(VariablesChangeListener listener);
	
    // get workbench actions
    public HashMap<String, AbstractAction> getActions();

    // get root frame
    public JFrame getRootFrame();

    // get project info
    public ProjectDataDAO getProject();

    // get user info
    public UserDataDAO getUser();

    // get workbench runtime info
    public RuntimeEnvironment getRuntime();

    // get operation manager
    public OperationManager getOpManager();

    // get view manager
    public ViewManager getViewManager();

    // get main tab window
    public TabWindow getMainTabWindow();

    // get console panel
    public WorkbenchRConsole getConsole();

    // set hel url
    public void setHelpBrowserURL(String url);

    // get urls
	public String getHelpRootUrl();
    //public String getCommandServletUrl();

    // no session
    public void noSession();

    // property
    public String getProperty(String name);

    public void setProperty(String name, String value);

    // console
    public void addConsole(String id, RConsole console);

    public void removeConsole(String id);

    // logs
    public WorkbenchLogContainer getLogContainer();

    // custom action handlers
    public void addRActionHandler(String actionname, RActionHandlerInterface handler);

    public void removeRActionHandler(String actionname, RActionHandlerInterface handler);

    public RootWindow getBenchRootWindow();

    public RConsoleProvider getBenchRConsoleProvider();

    // proxy settings
    public void loadProxySettings();

    public void persistProxySettings(boolean auto, ProxySettings settings);

    // closure handler
    // 1 - close
    // 0 cancel
    public int handleWorkbenchClosure();

}
