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
package workbench.completion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 26, 2010
 * Time: 12:00:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompletionVisualListRenderer extends JPanel implements ListCellRenderer {

    CompletionItem item = null;

    private JPanel textPanel;
    private JPanel toolPanel;
    private JLabel titleLabel;
    private JLabel detailLabel;
    private JLabel iconLabel;

    public CompletionVisualListRenderer() {

        super();

        setLayout(new BorderLayout(2,2));
        setBorder(new EmptyBorder(2,2,2,2));
        setOpaque(true);

        toolPanel = new JPanel(new BorderLayout());
        toolPanel.setOpaque(false);

        textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);

        titleLabel = new JLabel();
        titleLabel.setOpaque(false);

        detailLabel = new JLabel();
        detailLabel.setOpaque(false);

        iconLabel = new JLabel();
        iconLabel.setOpaque(false);

        Font f = detailLabel.getFont();
        detailLabel.setFont(new Font(f.getFamily(), Font.PLAIN, f.getSize() - 2));
        detailLabel.setForeground(Color.DARK_GRAY);

        textPanel.add(titleLabel, BorderLayout.CENTER);
        textPanel.add(detailLabel, BorderLayout.SOUTH);

        toolPanel.add(iconLabel, BorderLayout.WEST);

        add(toolPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    /**
     * Return the renderers fixed size here.
     */
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    /**
     * Completely bypass all of the standard JComponent painting machinery.
     * This is a special case: the renderer is guaranteed to be opaque,
     * it has no children, and it's only a child of the JList while
     * it's being used to rubber stamp cells.
     * <p>
     * Clear the background and then draw the text.
     */
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        //((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        //        RenderingHints.VALUE_ANTIALIAS_ON);
        //g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,5,5);
        g.fillRect(0,0,getWidth()-1,getHeight()-1);
    }

    /* This is is the ListCellRenderer method.  It just sets
     * the foreground and background properties and updates the
     * local text field.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

        item = (CompletionItem) value;

        titleLabel.setText(item.getValue());
        //detailLabel.setText("help text for " + item.getValue());
        iconLabel.setIcon(item.getIcon());

        titleLabel.setForeground(getForeground());
        detailLabel.setForeground(getForeground());

        return this;
    }


}
