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
package workbench.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 4, 2010
 * Time: 11:00:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenericPopupMenu extends JPopupMenu {

    final private static Logger log = LoggerFactory.getLogger(GenericPopupMenu.class);

    public JMenuItem makeMenuItem(AbstractAction action, String iconPath, KeyStroke ks) {
        JMenuItem menuItem = new JMenuItem((String)action.getValue(Action.NAME));

        if (action != null)   menuItem.setAction(action);
        if (iconPath != null) {
            //log.info(iconPath);
            menuItem.setIcon(new ImageIcon(ImageLoader.load(iconPath)));
        }
        if (ks != null)       menuItem.setAccelerator(ks);

        return menuItem;
    }

    public JMenuItem makeMenuItem(Action action, String iconPath, KeyStroke ks) {
        JMenuItem menuItem = new JMenuItem();

        if (action != null)   menuItem.setAction(action);
        if (iconPath != null) menuItem.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        if (ks != null)       menuItem.setAccelerator(ks);

        return menuItem;
    }

    public void updateActions(Component[] items) {
        for (int i=0;i<items.length;i++){
            Component c = items[i];

            if (c instanceof JMenu) {
                updateActions(((JMenu)c).getMenuComponents());
            } else if (c instanceof JMenuItem) {
                JMenuItem mi = ((JMenuItem)c);
                Action action = mi.getAction();
                if (action != null) {
                    mi.setEnabled(action.isEnabled());
                }
            }
        }
    }

    public void updatePopupMenu() {
        updateActions(this.getComponents());
    }
}
