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
package workbench.actions.wb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.file.FileNode;
import workbench.RGui;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 2, 2010
 * Time: 1:11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpenFileAction extends InvokeEditorAction {

    final private static Logger log = LoggerFactory.getLogger(OpenFileAction.class);

    public OpenFileAction(RGui rgui, String name){
        super(rgui, name);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        openWithLinenum(event.getActionCommand(), -1);
    }

    public void openWithLinenum(String filename, int linenum) {
        String title = filename;

        if (filename.contains(FileNode.separator)) {
            title = title.substring(title.lastIndexOf(FileNode.separator) + 1);
        }

        invokeBasicEditor(title, null, filename, linenum);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
