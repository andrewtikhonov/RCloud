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
package workbench.dialogs;

import workbench.util.ButtonUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class InDialog extends JDialog {
	String[] save;
	String expr_str = null;

	private boolean _closedOnOK = false;
	final JTextField exprs;

	public String getExpr() {
		if (_closedOnOK)
			try {
				return expr_str;
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	public InDialog(Component father, String label, String[] expr_save) {
		super(new JFrame(), true);
		
		
		setUndecorated(true);
		
		JPanel p = ((JPanel) getContentPane());
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createLineBorder(Color.black,3));
		/*
		createRProgressArea[0].setForeground(Color.white);
		createRProgressArea[0].setBackground(new Color(0x00,0x80,0x80));
		createRProgressArea[0].setBorder(BorderFactory.createLineBorder(new Color(0x00,0x80,0x80),3));
		createRProgressArea[0].setEditable(false);		
		createRProgressBar[0].setForeground(Color.white);
		createRProgressBar[0].setBackground(new Color(0x00,0x80,0x80));
		createRProgressBar[0].setIndeterminate(true);
		*/
		
		setLocationRelativeTo(father);
		
		
		save = expr_save;
		
		
		p.add(new JLabel(label), BorderLayout.WEST);

		exprs = new JTextField();
		exprs.setText(save[0]);

		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					okMethod();
				} 
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		};
		exprs.addKeyListener(keyListener);

		p.add(exprs, BorderLayout.CENTER );

		JButton ok = ButtonUtil.makeButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okMethod();
			}
		});

		p.add(ok,BorderLayout.EAST);
		setSize(new Dimension(320, 38));
        setLocationRelativeTo(father);
	}

	private void okMethod() {
		expr_str = exprs.getText();
		save[0] = expr_str;
		_closedOnOK = true;
		setVisible(false);
	}
	
	
}