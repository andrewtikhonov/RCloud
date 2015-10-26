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

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 28, 2009
 * Time: 3:25:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleHistoryModel {

    private Vector<String> data = new Vector<String>();

    public ConsoleHistoryModel() {
        this.data = new Vector<String>();
    }

    public void addItem(String item) {
        data.add(item);
    }

    public String getItem(int i) {
        return data.get(data.size() - 1 - i);
    }

    public void clear() {
        data.clear();
    }

    public int getSize() {
        return data.size();
    }

    public static void loadHistory() { }

    public static void saveHistory() { }

    public static void propertiesChanged() { }

}
