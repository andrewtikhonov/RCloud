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
package workbench.manager.viewman;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.views.DynamicView;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 1, 2009
 * Time: 5:21:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewManager {

    final private static Logger log = LoggerFactory.getLogger(ViewManager.class);

    private HashMap<Integer, DynamicView> dynamicViews = new HashMap<Integer, DynamicView>();

    private HashMap<Class<?>, Vector<DynamicView>> classMap = new HashMap<Class<?>, Vector<DynamicView>>();

    public static final String WINDOW_OPERATION = "window-operation";
    public static final String CLOSEWINDOW = "close";
    public static final String HIDEWINDOW = "hide";

    public ViewManager() {
    }

    private void addView(DockingWindow window){
        //log.info("ViewManager-addView");

        DynamicView view = (DynamicView) window;

        Class class0 = view.getClass();

        if (!dynamicViews.containsKey(view.getId())) {

            dynamicViews.put(view.getId(), view);

            Vector<DynamicView> views = classMap.get(class0);

            if (views == null) {
                views = new Vector<DynamicView>();
                classMap.put(class0, views);
            }

            views.add(view);
        }
    }

    private void removeView(DockingWindow window){
        //log.info("ViewManager-removeView");

        DynamicView view = (DynamicView) window;

        Class class0 = view.getClass();

        dynamicViews.remove(view.getId());

        Vector<DynamicView> views = classMap.get(class0);

        if (views != null) {
            views.remove(window);
        }
    }

    public int getDynamicViewId() {
        int id = 0;
        while (dynamicViews.containsKey(new Integer(id)))
            id++;
        return id;
    }

    public void updateViews(DockingWindow window, boolean added) {
        //log.info("ViewManager-updateViews-added="+added);

        if (window instanceof View) {
            if (window instanceof DynamicView) {
                if (added) {
                    addView(window);
                } else {
                    if (!HIDEWINDOW.equals(window.getClientProperty(WINDOW_OPERATION))) {
                        removeView(window);
                    }
                }
            } else {
            }
        } else {
            for (int i = 0; i < window.getChildWindowCount(); i++)
                updateViews(window.getChildWindow(i), added);
        }
    }

    public HashMap<Integer, DynamicView> getAllViews(){
        return dynamicViews;
    }

    public DynamicView getFirstViewOfClass(Class viewClass) {
        Vector<DynamicView> views = classMap.get(viewClass);
        if (views != null) {
            for (DynamicView view : views) {
                return view;
            }
        }
        return null;
    }

    public Vector<DynamicView> getAllViewsOfClass(Class viewClass) {
        Vector<DynamicView> views = classMap.get(viewClass);
        if (views != null)
            return views;
        else return new Vector<DynamicView>();
    }


}
