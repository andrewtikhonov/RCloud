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
package workbench.views.extraconsole;

import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import workbench.views.extraconsole.ConsolePanelBase;
import workbench.RGui;
import workbench.views.DynamicView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 15, 2010
 * Time: 4:14:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class RConsoleView extends DynamicView {

    private ConsolePanelFeatured consolePanel;
    private SearchPanel searchPanel = null;

    public RConsoleView(String title, Icon icon, int id, RGui rgui) {
        super(title, icon, new JPanel(), id);

        consolePanel = new ConsolePanelFeatured(rgui, id);
        searchPanel = new SearchPanel(consolePanel);

        JScrollPane scrollPane = new JScrollPane(consolePanel);
        scrollPane.setViewportBorder(new EmptyBorder(5,5,5,5));

        ((JPanel) getComponent()).setLayout(new BorderLayout());
        ((JPanel) getComponent()).add(scrollPane, BorderLayout.CENTER);
        ((JPanel) getComponent()).add(searchPanel, BorderLayout.SOUTH);
    }

    public ConsolePanelBase getConsolePanel() {
        return consolePanel;
    }

    public void dispose() {
        consolePanel.dispose();
        //searchPanel.stopthreads();
    }
}
