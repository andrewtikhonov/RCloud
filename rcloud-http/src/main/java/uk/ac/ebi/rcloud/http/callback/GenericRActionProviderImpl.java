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
import uk.ac.ebi.rcloud.server.callback.GenericRActionProvider;
import uk.ac.ebi.rcloud.server.callback.RAction;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 9, 2009
 * Time: 11:48:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenericRActionProviderImpl implements GenericRActionProvider {

    private Logger log = LoggerFactory.getLogger(getClass());

    private int timeout = 0;
    private ArrayBlockingQueue<Vector<RAction>> queue = null;

    public GenericRActionProviderImpl(ArrayBlockingQueue<Vector<RAction>> queue, int timeout) {
        this.queue = queue;
        this.timeout = timeout;
    }

    public Vector<RAction> popActions() {
        Vector<RAction> actions = new Vector<RAction>();

        try {
            actions = queue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            log.info("timeout");
        } catch (Exception e) {
            log.error("Error!", e);
        }

        /*
        if (actions != null) {
            while (queue.peek() != null) {
                actions.addAll(queue.poll());
            }
        }
        */

        return actions;
    }

    public void dispose() {
    }
}
