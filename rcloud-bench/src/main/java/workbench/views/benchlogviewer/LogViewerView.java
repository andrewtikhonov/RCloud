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

import uk.ac.ebi.rcloud.common.components.JTextPaneExt;
import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import workbench.RGui;
import workbench.manager.logman.WorkbenchLogAppender;
import workbench.views.DynamicView;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 5, 2010
 * Time: 3:38:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogViewerView extends DynamicView {

    private JTextPaneExt logarea = new JTextPaneExt();
    private JScrollPane scrollPane = new JScrollPane(logarea);
    private SearchPanel search = new SearchPanel(logarea);
    private LogViewerListener listener = new LogViewerListener(logarea);
    private RGui rgui;

    public LogViewerView(String title, Icon icon, int id, RGui rgui) {
        super(title, icon, new JPanel(), id);

        this.rgui = rgui;

        ((JPanel) getComponent()).setLayout(new BorderLayout());
        ((JPanel) getComponent()).add(scrollPane, BorderLayout.CENTER);
        ((JPanel) getComponent()).add(search, BorderLayout.SOUTH);

        logarea.addMouseListener(new LogViewerMouseAdapter(rgui, logarea));
        logarea.setLineLimit(8000, 1000);


        StringBuilder builder = new StringBuilder();

        for (String s : rgui.getLogContainer().getLog()) {
            builder.append(s); //insertString(doc.getLength(), s, null)
        }

        listener.write(builder.toString());

        WorkbenchLogAppender.addLogListener(listener);
    }

    public void dispose() {
        WorkbenchLogAppender.removeLogListener(listener);
    }
}
