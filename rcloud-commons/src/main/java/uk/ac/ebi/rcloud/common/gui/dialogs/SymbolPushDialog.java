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


public class SymbolPushDialog extends JDialog {

	Symbol symbol_str = null;
	String pushAs_str = null;
	String file_str = null;

	JComboBox symbolName = null;

	public Symbol getSymbol() {
		if (symbol_str == null && pushAs_str == null && file_str == null)
			return null;
		else
			return symbol_str;
	}

	public String getPushAs() {
		if (symbol_str == null && pushAs_str == null && file_str == null)
			return null;
		else
			return pushAs_str;
	}

	public String getFileName() {
		if (symbol_str == null && pushAs_str == null && file_str == null)
			return null;
		else
			return file_str;
	}

	public SymbolPushDialog(final Container aFrame, String servantName, Symbol[] symbolsList, boolean fromFile) {
		super((Frame) null, true);

		if (servantName != null) {
			setTitle("Push Symbol to <" + servantName + ">");
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

		if (!fromFile) {
			p1.add(new JLabel("  Symbol Name"));
		} else {
			p1.add(new JLabel("  Push From File"));
		}

		p1.add(new JLabel("  Push As"));
		p1.add(new JLabel(""));

		final JFileChooser chooser = new JFileChooser();
		final JTextField pushFromFile = new JTextField();
		pushFromFile.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int returnVal = chooser.showOpenDialog(aFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						pushFromFile.setText(chooser.getSelectedFile().getAbsolutePath());
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
		final JTextField pushAs = new JTextField();
		if (!fromFile) {
			symbolName = new JComboBox(symbolsList);
			symbolName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pushAs.setText((String) ((Symbol) symbolName.getSelectedItem()).getName());
				}
			});
			if (symbolsList.length > 0)
				symbolName.setSelectedIndex(0);
			p2.add(symbolName);
		} else {
			JButton browse = new JButton("Browse");
			browse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int returnVal = chooser.showOpenDialog(aFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						pushFromFile.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			JPanel field_button = new JPanel();
			field_button.setLayout(new BorderLayout());
			field_button.add(pushFromFile, BorderLayout.CENTER);
			field_button.add(browse, BorderLayout.EAST);

			p2.add(field_button);
		}

		p2.add(pushAs);

		p2.add(new JLabel(""));

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (symbolName != null) {
					symbol_str = (Symbol) symbolName.getSelectedItem();
				}
				pushAs_str = pushAs.getText();
				file_str = pushFromFile.getText();
				SymbolPushDialog.this.setVisible(false);
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SymbolPushDialog.this.setVisible(false);
			}
		});

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(500, 140));

	}

}