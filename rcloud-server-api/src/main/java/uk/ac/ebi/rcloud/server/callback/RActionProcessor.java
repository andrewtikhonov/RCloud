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
package uk.ac.ebi.rcloud.server.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Aug 4, 2010
 * Time: 4:04:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class RActionProcessor implements Runnable {

    final private static Logger log = LoggerFactory.getLogger(RActionProcessor.class);

    public static int MAX_QUEUE_CAPACITY = 1000;
    public static int WARNING_QUEUE_CAPACITY = 30;
    public static int NOTIFY_TIMEOUT = 30000;

    private RActionListener listener = null;

    private Thread processor = new Thread(this);

    private ArrayBlockingQueue<RAction> queue =
            new ArrayBlockingQueue<RAction>(MAX_QUEUE_CAPACITY);

    private boolean _shutdownRequested = false;

    public RActionProcessor(RActionListener listener0){
        this.listener = listener0;
        this.processor.start();
    }

    public void notifyListeners(RAction action) {
        //log.info("RActionProcessor-notifyListeners-action="+action.toString());

        if (queue.remainingCapacity() < WARNING_QUEUE_CAPACITY) {
            log.info("RActionProcessor-notifyListeners-queue.remainingCapacity()=" +
                    queue.remainingCapacity());
        }

        try {
            queue.offer(action, NOTIFY_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            log.error("Error!", ie);
        }
    }

    public void run() {

        while(!_shutdownRequested) {
            try {
                RAction action = queue.poll(NOTIFY_TIMEOUT, TimeUnit.MILLISECONDS);

                if (action != null) {

                    try {
                        listener.notify(action);
                    } catch (Exception ex) {
                        log.info("RActionProcessor-run CLEANING ACTION LISTENER");
                        //log.debug("Debug!", ex);

                        this._shutdownRequested = true;

                        break;
                    }
                }
            } catch (InterruptedException ie) {
            }
        }

        // cleanup everything
        //
        log.info("RActionProcessor-run-nullout everything");

        this.listener = null;
        this.processor = null;
        this.queue = null;
    }


    // INTERFACES
    //

    public void setShutdownRequested(boolean _shutdownRequested) {
        log.info("RActionProcessor-setShutdownRequested");

        this._shutdownRequested = _shutdownRequested;
        this.processor.interrupt();
    }

    public RActionListener getListener() {
        return listener;
    }

    public Thread getProcessor() {
        return processor;
    }

    public ArrayBlockingQueue<RAction> getQueue() {
        return queue;
    }

    public boolean isShutdownRequested() {
        return _shutdownRequested;
    }

}
