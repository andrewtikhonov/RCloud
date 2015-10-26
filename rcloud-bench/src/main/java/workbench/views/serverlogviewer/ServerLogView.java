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
package workbench.views.serverlogviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.components.JTextPaneExt;
import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.views.DynamicView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public  class ServerLogView extends DynamicView {

    private static final Logger log = LoggerFactory.getLogger(ServerLogView.class);

    private JTextPaneExt area = new JTextPaneExt();
    private SearchPanel search = new SearchPanel(area);
    private ServerLogListener listener = new ServerLogListener();

    private Vector<String> logdata;

	public ServerLogView(String title, Icon icon, int id, Vector<String> logdata) {
		super(title, icon, new JPanel(), id);

        this.logdata = logdata;

		JScrollPane scrollPane = new JScrollPane(area);

		((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).add(scrollPane, BorderLayout.CENTER);
		((JPanel) getComponent()).add(search, BorderLayout.SOUTH);

        area.addMouseListener(new ServerLogMouseAdapter());
        area.setLineLimit(8000, 1000);

        StringBuilder sb = new StringBuilder();
        for (String s : logdata) {
            sb.append(s);
        }

        try {
            area.getDocument().insertString(0, sb.toString(), null);
        } catch (BadLocationException ble) {
        }

	}

    class ServerLogMouseAdapter extends MouseAdapter {
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
                        area.setText("");
                        logdata.removeAllElements();
                    }

                    public boolean isEnabled() {
                        return !area.getText().equals("");
                    }
                });

                cleanItem.setIcon(new ImageIcon(
                        ImageLoader.load("/views/images/logviewer/edit-clear.png")));


                popupMenu.add(cleanItem);

                popupMenu.show(area, event.getX(), event.getY());
            }
        }
    }

    class ServerLogListener implements LogListener {
        class PrintRunnable implements Runnable {
            private String text;
            public PrintRunnable(String text) {
                this.text = text;
            }

            public void run(){
                try {
                    Document doc = area.getDocument();
                    doc.insertString(doc.getLength(), text, null);
                } catch (BadLocationException e) {
                    log.error("Error!", e);
                }
            }
        }

        public void write(String text) {
            EventQueue.invokeLater(new PrintRunnable(text));
        }
    }

    public ServerLogListener getListener() {
        return listener;
    }

    public void dispose() {
        //log.info("dispose");
        //search.stopthreads();
    }

	public void clearLogArea() {
		EventQueue.invokeLater(new Runnable(){
            public void run(){
                area.setText("");
            }
        });
	}

}

