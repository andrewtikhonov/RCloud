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
package uk.ac.ebi.rcloud.common.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SymbolPopDialog extends JDialog {

	String symbol_str = null;
	String file_str;

	public String getSymbolName() {
		return symbol_str;
	}

	public String getFileName() {
		return file_str;
	}

	public SymbolPopDialog(final Container aFrame, String servantName, String[] symbolsList, final boolean toFile) {
		super((Frame) null, true);

		if (servantName != null) {
			setTitle("Pop Symbol From <" + servantName + ">");
		}
		setLocationRelativeTo(aFrame);

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		p1.add(new JLabel("  Symbol Name"));
		if (toFile) {
			p1.add(new JLabel("  Destination File"));
		}
		p1.add(new JLabel(""));

		if (symbolsList == null)
			symbolsList = new String[0];
		final JComboBox symbolName = new JComboBox(symbolsList);

		final JFileChooser chooser = new JFileChooser();
		final JTextField fileTF = new JTextField();
		fileTF.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int returnVal = chooser.showOpenDialog(aFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						fileTF.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		});
		p2.add(symbolName);

		JButton browse = new JButton("Browse");
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = chooser.showOpenDialog(aFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileTF.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		JPanel field_button = new JPanel();
		field_button.setLayout(new BorderLayout());
		field_button.add(fileTF, BorderLayout.CENTER);
		field_button.add(browse, BorderLayout.EAST);

		if (toFile) {
			p2.add(field_button);
		}

		p2.add(new JLabel(""));

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				symbol_str = (String) symbolName.getSelectedItem();
				file_str = (toFile ? fileTF.getText() : null);
				SymbolPopDialog.this.setVisible(false);
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SymbolPopDialog.this.setVisible(false);
			}
		});

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(420, 130));

	}

}
