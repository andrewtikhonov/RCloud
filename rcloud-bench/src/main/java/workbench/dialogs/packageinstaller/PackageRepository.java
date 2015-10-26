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
package workbench.dialogs.packageinstaller;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 17, 2009
 * Time: 4:41:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageRepository {

    private String title;
    private String url;
    private String cmd;
    private String init;
    private InstallerTableView view;
    private boolean definedurl;
    private boolean warm = false;

    public PackageRepository(String title, String url, String cmd, String init, InstallerTableView view) {
        this.title = title;
        this.url   = url;
        this.cmd   = cmd;
        this.init  = init;
        this.view  = view;
        definedurl = (url != null && url.length() > 0);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public InstallerTableView getView() {
        return view;
    }

    public void setView(InstallerTableView view) {
        this.view = view;
    }

    public void setDefinedUrl(boolean definedurl) {
        this.definedurl = definedurl;
    }

    public boolean isDefinedUrl() {
        return definedurl;
    }

    public boolean isWarm() {
        return warm;
    }

    public void setWarm(boolean warm) {
        this.warm = warm;
    }
}
