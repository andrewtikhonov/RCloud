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
package workbench.manager.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.manager.opman.OperationCancelledException;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 1, 2010
 * Time: 3:10:38 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class RunHelper implements Runnable {

    final private static Logger log = LoggerFactory.getLogger(RunHelper.class);

    private boolean completed = false;
    private Thread currentthread = null;

    public RunHelper() {
    }

    public void actionCompleted() {
        setCompleted(true);
        if (currentthread != null) currentthread.interrupt();
    }

    public void preAction() {}

    public void postAction() {}

    public void mainAction() {}

    public void cancellationAction() {}

    public void completionAction() {}

    public void trackProgress() throws OperationCancelledException {}

    public void run() {

        setCompleted(false);

        currentthread = Thread.currentThread();

        try {
            preAction();

            mainAction();

            while(!isCompleted()) {

                try {
                    trackProgress();
                    Thread.sleep(1000);

                } catch (InterruptedException ie) {
                } catch(OperationCancelledException oce) {
                    break;
                }
            }

            if (isCompleted()) {
                completionAction();
            } else {
                cancellationAction();
            }

        } catch (Exception ex) {
            log.error("Error!", ex);
        } finally {
            postAction();
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

}
