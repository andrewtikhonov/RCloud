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
package workbench.views.filebrowser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.generic.GenericPopupMenu;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 22, 2010
 * Time: 5:52:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileBrowserPopupMenu extends GenericPopupMenu {

    final static private Logger log = LoggerFactory.getLogger(FileBrowserPopupMenu.class);

    public FileBrowserPopupMenu(HashMap<String, AbstractAction> browserActions) {

        add(makeMenuItem(browserActions.get(FileBrowserActionType.OPEN), null, null));
        addSeparator();
        add(makeMenuItem(browserActions.get(FileBrowserActionType.NEWFILE),
                "/views/images/filebrowser/menu/page_white_add.png", null));

        add(makeMenuItem(browserActions.get(FileBrowserActionType.NEWFOLDER),
                "/views/images/filebrowser/menu/folder_add.png", null));

        add(makeMenuItem(browserActions.get(FileBrowserActionType.RENAME), null, null));

        addSeparator();
        add(makeMenuItem(browserActions.get(FileBrowserActionType.REMOVE), null, null));

        JMenu devMenu = new JMenu("Developer");

        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.FIND),
                "/views/images/filebrowser/menu/zoom.png", null));
        devMenu.addSeparator();
        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.INSTALL),
                "/views/images/filebrowser/menu/brick.png", null));
        devMenu.addSeparator();
        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.CHECK),
                "/views/images/filebrowser/menu/brick_error.png", null));
        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.CONFIG),
                "/views/images/filebrowser/menu/brick_edit.png", null));

        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.BUILD),
                "/views/images/filebrowser/menu/brick_link.png", null));

        devMenu.addSeparator();
        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.TEMPLATE),
                "/views/images/filebrowser/menu/brick_add.png", null));

        devMenu.addSeparator();
        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.COMPRESS),
                "/views/images/filebrowser/menu/page_white_zip.png", null));

        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.UNCOMPRESS),
                "/views/images/filebrowser/menu/page_white_zip.png", null));


        devMenu.addSeparator();
        devMenu.add(makeMenuItem(browserActions.get(FileBrowserActionType.COMMAND),
                "/views/images/filebrowser/menu/application_xp_terminal.png", null));

        addSeparator();
        add(devMenu);

        addSeparator();
        add(makeMenuItem(browserActions.get(FileBrowserActionType.IMPORT), null, null));
        add(makeMenuItem(browserActions.get(FileBrowserActionType.EXPORT), null, null));

        addSeparator();
        add(makeMenuItem(browserActions.get(FileBrowserActionType.MAKELINK),
                "/views/images/filebrowser/menu/link.png", null));
        add(makeMenuItem(browserActions.get(FileBrowserActionType.IMPORTLINK),
                "/views/images/filebrowser/menu/link_add.png", null));

        addSeparator();
        add(makeMenuItem(browserActions.get(FileBrowserActionType.PREVIEW), null,
                KeyUtil.getKeyStroke(KeyEvent.VK_SPACE, 0)));
    }

}



