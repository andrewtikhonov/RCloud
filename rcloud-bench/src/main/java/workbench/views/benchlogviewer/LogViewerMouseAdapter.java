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
package workbench.views.benchlogviewer;

import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.RGui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 5, 2010
 * Time: 3:51:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogViewerMouseAdapter extends MouseAdapter {

    private JTextPane logarea;
    private RGui rgui;

    public LogViewerMouseAdapter(RGui rgui, JTextPane logarea){
        this.logarea = logarea;
        this.rgui = rgui;
    }

    public void mousePressed(MouseEvent event) {
        checkPopup(event);
    }

    public void mouseClicked(MouseEvent event) {
        checkPopup(event);
    }

    public void mouseReleased(MouseEvent event) {
        checkPopup(event);
    }

    private void checkPopup(MouseEvent event) {
        if (event.isPopupTrigger()) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem cleanItem = new JMenuItem();
            cleanItem.setAction(new AbstractAction("Clear") {

                public void actionPerformed(ActionEvent e) {
                    rgui.getLogContainer().clearLog();
                    logarea.setText("");
                }

                public boolean isEnabled() {
                    return !logarea.getText().equals("");
                }
            });

            cleanItem.setIcon(new ImageIcon(
                    ImageLoader.load("/views/images/logviewer/edit-clear.png")));


            popupMenu.add(cleanItem);

            popupMenu.show(logarea, event.getX(), event.getY());
        }
    }


}
