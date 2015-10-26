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
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 12, 2009
 * Time: 4:34:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompletionVisualList extends JList {

    private String text = "No Data";
    private ImageIcon icon = null;

    public CompletionVisualList(){
        super();
    }

    public CompletionVisualList(ListModel listModel){
        super(listModel);
    }

    public void setMessage(String message){
        this.text = message;
    }

    public void setIcon(ImageIcon icon){
        this.icon = icon;
    }

    public void drawIcon(Graphics g) {
        int h = icon.getIconHeight();
        int w = icon.getIconWidth();

        g.drawImage(icon.getImage(),(getWidth()-w)/2,(getHeight()-h)/2,null);
    }

    public void drawText(Graphics g) {
        FontMetrics metrics = getFontMetrics(getFont());
        int width = metrics.stringWidth(text);
        int height = metrics.getHeight();

        int x = (getWidth() - width)/2;
        int y = (getHeight() - height)/2;

        g.clearRect(0,0,getWidth(),getHeight());
        g.drawString(text, x, y);
    }

    public void paintComponent(Graphics g) {
        if (this.getModel().getSize() == 0) {

            if (icon != null) {
                drawIcon(g);
            } else {
                drawText(g);
            }
        } else {
            super.paintComponent(g);
        }
    }

}
