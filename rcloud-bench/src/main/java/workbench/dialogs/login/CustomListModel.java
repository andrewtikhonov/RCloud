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
package workbench.dialogs.login;

import javax.swing.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 29, 2009
 * Time: 1:31:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomListModel extends AbstractListModel {

    private Vector<Object> items;

    public CustomListModel(){
       items = new Vector<Object>();
    }

    public int getSize() {
        return items.size();
    }

    public Object getElementAt(int i) {
        return items.get(i);
    }

    public void addElement(Object o) {
        int index = items.size();

        items.add(o);

        fireIntervalAdded(this, index, index);
    }

    public void insertElementAt(Object o, int index) {
        items.insertElementAt(o, index);

        fireIntervalAdded(this, index, index);
    }


    public void removeElementAt(int i) {
        items.removeElementAt(i);

        fireIntervalRemoved(this, i, i);
    }

    public void removeAllElements() {
        int index = items.size() - 1;

        if (index >= 0)
        {
            items.removeAllElements();

            fireIntervalRemoved(this, 0, index);
        }
    }
    
}
