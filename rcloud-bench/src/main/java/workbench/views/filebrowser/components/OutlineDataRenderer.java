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
package workbench.views.filebrowser.components;

import uk.ac.ebi.rcloud.server.file.FileNode;
import org.netbeans.swing.outline.RenderDataProvider;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 28, 2009
 * Time: 5:46:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class OutlineDataRenderer implements RenderDataProvider {

    //@Override
    public Color getBackground(Object o) {
        return null;
    }

    //@Override
    public String getDisplayName(Object o) {
        return ((FileNode)o).getName();
    }

    //@Override
    public Color getForeground(Object o) {
        FileNode node = (FileNode) o;
        if (!node.isDirectory()) {
            return UIManager.getColor("controlShadow");
        }
        return null;
    }

    //@Override
    public Icon getIcon(Object o) {
        return null;

    }

    //@Override
    public String getTooltipText(Object o) {
        FileNode node = (FileNode) o;
        return node.getPath();
    }

    //@Override
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }
}

