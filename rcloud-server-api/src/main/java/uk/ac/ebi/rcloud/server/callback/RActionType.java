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
package uk.ac.ebi.rcloud.server.callback;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 17, 2010
 * Time: 2:56:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class RActionType {

    public static final String HELP = "HELP";
    public static final String BROWSE = "BROWSE";
    public static final String QUIT = "QUIT";
    public static final String EDIT = "EDIT";
    public static final String ASYNC_SUBMIT = "COMPLETE";
    public static final String COMPLETE = "COMPLETE";
    public static final String USER_INPUT = "USER_INPUT";
    public static final String PAGER = "PAGER";
    public static final String CONSOLE = "CONSOLE"; 
    public static final String CONTINUE = "CONTINUE";
    public static final String UPDATE_USERS = "UPDATE_USERS";
    public static final String ADMIN_MESSAGE = "ADMIN_MESSAGE";
    public static final String VARIABLES_CHANGE = "VARIABLES_CHANGE";
    public static final String LOAD_HISTORY = "LOAD_HISTORY";
    public static final String SAVE_HISTORY = "SAVE_HISTORY";
    public static final String UPDATE_FILETREE = "UPDATE_FILETREE";
    public static final String UPDATE_FILENODE = "UPDATE_FILENODE";
    public static final String BUSY_ACTION = "BUSY_ACTION";
    public static final String CHAT = "CHAT";
    public static final String COLLABORATION_PRINT = "COLLABORATION_PRINT";
    public static final String LOG = "LOG";
    public static final String DEFLATED = "DEFLATED";
    public static final String PROMPT = "PROMPT";
    public static final String EXEC = "EXEC";
    public static final String SEARCH = "SEARCH";

}
