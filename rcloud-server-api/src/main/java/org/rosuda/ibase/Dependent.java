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
package org.rosuda.ibase;

import java.rmi.Remote;
import java.util.Vector;

/**
   Simple dependency interface.
   Any class that implements this interface can be notified upon changes.
   @version $Id: Dependent.java 445 2003-07-29 21:54:22Z starsoft $
*/
public interface Dependent{
    /**
     * This method will be called when an even occured.
     * Currently {@link SMarker} and {@link Axis} use this method of notification.
     * There's no generic class for implemention the notification-list yet.
     *
     * @param src Object that sent the notification. The actual content is implementation-dependent.
     * @param path This parameter is <code>null<code> for non-cascaded notify - in that case further calls to NotifyAll are not allowed. Otherwise it contains a Vector with all objects notified so far during cascaded notify. To aviod cyclic notifications every instance must either reject cascaded notifications (i.e. no calls to NotifyAll at all) or check for occurence of itself in the chain before calling NotifyAll.
     * The only valid recursive calls in Notifying are NotifyAll(path) and NotifyAll(..,path), because only these two pass the "path" parameter to avoid cyclic loops. */
    public void Notifying(NotifyMsg msg, Object src, Vector path);
}
