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
package workbench.dialogs.packageinstaller;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 3, 2009
 * Time: 12:47:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstallerTableView {

    private Vector<InstallerTableItem> packageList = new Vector<InstallerTableItem>();
    private HashMap<String, Integer> indexMap0   = new HashMap<String, Integer>();
    private HashMap<String, Integer> indexMap1   = new HashMap<String, Integer>();
    private String                 currentLetter = "";

    public InstallerTableView() {
        indexMap1.put(currentLetter, 0);
        indexMap0.put(currentLetter, 0);
    }

    // insertion into this view is performed only once
    // the view is maintained sorted in alphabetical order
    // indexMap is maintained to facilitate quicker search
    // of records in the view

    public void add(InstallerTableItem item) {

        String letter = item.getName().substring(0, 1);
        if (letter.compareTo(currentLetter) > 0) {
            indexMap1.put(currentLetter, packageList.size());
            indexMap0.put(letter, packageList.size());
            currentLetter = letter;
        }

        packageList.add(item);
    }

    public InstallerTableItem get(int index) {
        return packageList.get(index);
    }

    public Vector<InstallerTableItem> getList() {
        return packageList;
    }

    private int getIndex0(String letter) {
        Integer i = indexMap0.get(letter);
        return (i != null ? i.intValue() : 0);
    }

    private int getIndex1(String letter) {
        Integer i = indexMap1.get(letter);
        return (i != null ? i.intValue() : packageList.size());
    }

    public String getStatistics() {
        StringBuilder sb = new StringBuilder();

        sb.append(packageList.size()+" items in list\n");

        Iterator i = indexMap0.keySet().iterator();

        while (i.hasNext()) {
            String letter = (String) i.next();
            int v0 = getIndex0(letter);
            int v1 = getIndex1(letter);

            sb.append(letter+" range "+v0+"-"+v1+" items: "+(v1-v0)+"\n");
        }

        return sb.toString();
    }


    public InstallerTableItem find(String name) {

        String letter = name.substring(0, 1);
        int index0     = getIndex0(letter);
        int index1     = getIndex1(letter);

        for (int j=index0;j<index1;j++) {
            InstallerTableItem item = packageList.get(j);
            if (item.getName().equals(name)) {
                return item;
            }
        }

        return null;
    }
}

