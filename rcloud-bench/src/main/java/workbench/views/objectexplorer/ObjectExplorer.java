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

import java.awt.event.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel ;
import javax.swing.JTable ;


import javax.swing.event.ListSelectionListener ;
import javax.swing.event.ListSelectionEvent ;

import javax.swing.table.TableColumn ;


import uk.ac.ebi.rcloud.server.RType.RChar;
import workbench.RGui;

import javax.swing.BoxLayout ;
import javax.swing.* ;
import javax.swing.border.EmptyBorder ;
import java.awt.* ;


/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 29, 2009
 * Time: 2:17:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectExplorer extends JPanel {

		//private View view;

    private RGui rgui;
    private JTable content;
    private ObjectsExplorerTableModel model ;

    private JLabel envLabel ;
    public  JComboBox envCombo ;


    // author: Romain Francois
    // updated: Andrew T

	public ObjectExplorer(RGui gui)  {

        super(new BorderLayout());

        this.rgui = gui;

		Box topBox = new Box(BoxLayout.Y_AXIS);

        GridBagLayout layout = new GridBagLayout();

		JPanel envAndFilterPanel = new JPanel(layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridwidth = cons.gridheight = 1;
		cons.gridx = cons.gridy = 0;
		cons.fill = GridBagConstraints.BOTH;
		cons.anchor = GridBagConstraints.EAST;
		envLabel = new JLabel("Env",SwingConstants.RIGHT);
		envLabel.setToolTipText( "R Environment to browse" ) ;
		envLabel.setBorder(new EmptyBorder(0,0,0,12));
		layout.setConstraints(envLabel,cons);
		envAndFilterPanel.add(envLabel);


        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refresh();
            }
        };

		envCombo = new JComboBox( searchpath() ) ;
		envCombo.addActionListener(listener);

		Dimension prefSize = envCombo.getPreferredSize();
		prefSize.width = 0;
		envCombo.setPreferredSize(prefSize);
		envCombo.addActionListener(listener);
		cons.gridx = 1;
		cons.weightx = 1.0;
		cons.gridwidth = GridBagConstraints.REMAINDER;

		layout.setConstraints(envCombo,cons);

		envAndFilterPanel.add(envCombo);

		topBox.add(envAndFilterPanel);
		this.add( topBox, BorderLayout.NORTH ) ;

        model = new ObjectsExplorerTableModel(rgui) ;

        content = new JTable(model) ;
        content.addMouseListener(new ObjectExplorerMouseAdapter(this));
        //content.getTableHeader().addMouseListener(new HeaderListener(content));

        initColumnSize( content );

        ListSelectionListener selectionListener = new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e){ }
        };

        content.getSelectionModel().addListSelectionListener( selectionListener ) ;
        JScrollPane scrollPane = new JScrollPane(content);

		this.add( scrollPane, BorderLayout.CENTER ) ;

        setVisible( true ) ;
		//addNotify( ) ;
        refresh();
	}

    public JTable getContent() {
        return content;
    }

    public JComboBox getEnvCombo() {
        return envCombo;
    }

    public RGui getRGui() {
        return rgui;
    }

    private void initColumnSize( JTable table){

        // icon
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth( 20 ) ;
    	column.setMaxWidth( 20 );

		// name
        column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth( 200 ) ;

        // dimension
        column = table.getColumnModel().getColumn(2);
        column.setPreferredWidth( 60 ) ;
    	column.setMaxWidth( 100 );

        // group
        column = table.getColumnModel().getColumn(3);
        column.setPreferredWidth( 100 ) ;
		column.setMaxWidth( 150 );
    }

    public String[] searchpath( ){
	    String[] out = null ;
        try {
            rgui.getRLock().lock() ;
            out = ( (RChar) ( rgui.obtainR().getObject( "search()" ) ) ).getValue() ;
        } catch( Exception e ){
        } finally{
            rgui.getRLock().unlock() ;
        }
        return out ;
    }

    // invoked when the user changes the filter text
    private void newFilter() {
      //  RowFilter<ObjectsExplorerTableModel, Object> rf = null;
      //  //If current expression doesn't parse, don't update.
      //  try {
      //    rf = RowFilter.regexFilter("(?i)"+filter.getText());
      //  } catch (java.util.regex.PatternSyntaxException e) {
      //    return;
      //  }
      //  sorter.setRowFilter(rf);
    }

    public void refresh( String text) {
        ObjectsExplorerTableModel hmodel = ((ObjectsExplorerTableModel)content.getModel()) ;
        model.refresh( text ) ;
    }

	public void refresh( ){
		Object ob = envCombo.getSelectedItem() ;
		refresh( ob == null ? ".GlobalEnv" : (String)ob  ) ;
	}


}

