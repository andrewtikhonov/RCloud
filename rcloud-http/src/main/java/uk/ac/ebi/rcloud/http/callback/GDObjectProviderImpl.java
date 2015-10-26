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
package uk.ac.ebi.rcloud.http.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.graphics.GDObjectProvider;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;

import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 8, 2009
 * Time: 4:09:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class GDObjectProviderImpl implements GDObjectProvider {

    private Logger log = LoggerFactory.getLogger(getClass());

    private int timeout = 0;
    private ArrayBlockingQueue<Vector<GDObject>> queue = null;

    public GDObjectProviderImpl(ArrayBlockingQueue<Vector<GDObject>> queue, int timeout) {
        this.queue = queue;
        this.timeout = timeout;
    }

    public Vector<GDObject> popObjects() {
        Vector<GDObject> objects = new Vector<GDObject>();

        try {
            objects = queue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            log.info("timeout");
        } catch (Exception e) {
            log.error("Error!", e);
        }

        /*
        if (objects != null) {
            while (queue.peek() != null) {
                objects.addAll(queue.poll());
            }
        }
        */

        return objects;
    }

    public void dispose() {
    }
}
