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
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 12, 2009
 * Time: 4:39:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompletionVisualListModel extends AbstractListModel {

    public static final int FORWARD_ORDER = 0;
    public static final int INVERTED_ORDER = 1;

    public static final int FREETEXT_FILTER = 0;
    public static final int EXACT_FILTER    = 1;

    protected int order = FORWARD_ORDER;
    protected int filter = EXACT_FILTER;

    protected String largeststring = "";

    protected Vector<CompletionItem> data = null;
    protected HashMap<Integer, Boolean> map = null;

    private String pattern = "";

    public CompletionVisualListModel(int filter) {
        data = new Vector<CompletionItem>();
        map  = new HashMap<Integer, Boolean>();
        this.filter = filter;
    }

    public int getSize() {
        return data.size();
    }

    public Object getElementAt(int i) {
        switch (order) {
            case FORWARD_ORDER: return data.get(i);
            case INVERTED_ORDER: return data.get(data.size() - 1 - i);
            default: return data.get(i);
        }
    }

    public boolean isFilterPassed(String item, String p) {
        switch (filter) {
            case FREETEXT_FILTER: return item.contains(p);
            case EXACT_FILTER: return item.startsWith(p);
            default: return item.startsWith(p);
        }
    }

    public void addElements(CompletionResult list) {
        if (list != null && list.size() > 0) {

            int startindex = data.size();

            for (CompletionItem item : list) {
                int code = item.hashCode();

                if(!map.containsKey(code)) {
                    map.put(code, true);

                    if (isFilterPassed(item.getValue(), this.pattern)) {
                        data.add(item);

                        if (item.getValue().length() > largeststring.length()) {
                            largeststring = item.getValue();
                        }
                    }
                }
            }

            int stopindex = data.size() - 1;
            if (stopindex >= startindex) {
                fireIntervalAdded(this, startindex, stopindex);
            }
        }
    }

    public void clear() {
        if (data.size() > 0) {
            int lastIndex = data.size() - 1;
            data.removeAllElements();
            fireIntervalRemoved(this, 0, lastIndex);
        }
        largeststring = "";
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void replaceItems(CompletionResult list) {
        this.clear();
        this.map.clear();
        addElements(list);
    }

    public void addItems(CompletionResult list) {
        addElements(list);
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public String getLargestString() {
        return largeststring;
    }
    
}
