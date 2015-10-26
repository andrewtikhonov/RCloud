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
package workbench.views.objectexplorer;

import uk.ac.ebi.rcloud.common.util.ImageLoader;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 8, 2010
 * Time: 4:36:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectExplorerMouseAdapter extends MouseAdapter {

    private ObjectExplorer explorer;
    private JPopupMenu menu;

    public ObjectExplorerMouseAdapter(ObjectExplorer explorer) {
        this.explorer = explorer;
        this.menu = initPopupMenu();
    }

    private JMenuItem makeMenuItem(Action action, String iconPath, KeyStroke ks) {
        JMenuItem menuItem = new JMenuItem();

        if (action != null)   menuItem.setAction(action);
        if (iconPath != null) menuItem.setIcon(new ImageIcon(ImageLoader.load(iconPath)));
        if (ks != null)       menuItem.setAccelerator(ks);

        return menuItem;
    }

    public JPopupMenu initPopupMenu() {

        JPopupMenu menu = new JPopupMenu();


        JMenuItem viewHelpItem = makeMenuItem(new ViewHelpAction("View Help"),
                "/views/images/explorer/information.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_F1, 0));

        JMenuItem viewSourceItem = makeMenuItem(new ViewSourceAction("View Source"),
                "/views/images/explorer/page_white_edit.png",
                KeyUtil.getKeyStroke(KeyEvent.VK_F2, 0));

        menu.add(viewHelpItem);
        menu.add(viewSourceItem);

        return menu;

    }

    public void mouseClicked(MouseEvent event) {
        checkPopup(event);

        if (event.getClickCount() == 2) {
            //JComboBox combo = explorer.getEnvCombo();
            //int   col = content.columnAtPoint(event.getPoint());
            //col = content.convertColumnIndexToModel(col);
            openSelectedFunctions();
        }
    }

    private void checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {

            JTable content = explorer.getContent();

            int row    = content.rowAtPoint( e.getPoint() );
            int rows[] = content.getSelectedRows();

            if (row == -1) {

                content.clearSelection();

            } else {

                boolean found = false;

                for(int i : rows) {
                    if (i == row) found = true;
                }

                if (!found) {
                    content.getSelectionModel().setSelectionInterval(row, row);
                }
            }

            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void mousePressed(MouseEvent event) {
        checkPopup(event);
    }

    public void mouseReleased(MouseEvent event) {
        checkPopup(event);
    }

    class ViewHelpAction extends AbstractAction {
        public ViewHelpAction(String name){
            super(name);
        }
        public void actionPerformed(ActionEvent event) {
            JTable content = explorer.getContent();

            int[] rows = content.getSelectedRows();

            for (int r : rows) {
                requestKeywordHelp((String) content.getModel().getValueAt(r, 1));
            }
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class ViewSourceAction extends AbstractAction {
        public ViewSourceAction(String name){
            super(name);
        }
        public void actionPerformed(ActionEvent event) {
            openSelectedFunctions();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public void openSelectedFunctions() {
        JTable content = explorer.getContent();
        String env = (String) explorer.getEnvCombo().getSelectedItem();

        int[] rows = content.getSelectedRows();

        int index = env.indexOf(":");
        if (index != -1) {
            env = env.substring(index + 1) + "::";
        } else {
            env = "";
        }

        for (int r : rows) {
            String name = env + content.getModel().getValueAt(r, 1);
            viewSourceCode(name);
        }
    }

    private Action requesthelpAction = null;
    private void requestKeywordHelp(String keyword) {

        if (keyword != null) {
            if (requesthelpAction == null) {
                requesthelpAction = explorer.getRGui().getActions().get("requesthelp");
            }
            requesthelpAction.actionPerformed(new ActionEvent(this, 0, keyword));
        }
    }

    private Action openfunctionAction = null;
    private void viewSourceCode(String keyword) {

        if (keyword != null) {
            if (openfunctionAction == null) {
                openfunctionAction = explorer.getRGui().getActions().get("openfunction");
            }
            openfunctionAction.actionPerformed(new ActionEvent(this, 0, keyword));
        }
    }

}
