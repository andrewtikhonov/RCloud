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
package workbench.manager.opman;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 14, 2009
 * Time: 11:19:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class Operation {

    private int state;
    private int progress;
    private long id;
    private String title; 
    private OperationManager opMan;
    private boolean abort = false;
    private boolean indeterminate = false;
    private boolean eventUnsafe = false;

    public Operation(String title, OperationManager opMan) {
        this(title, opMan, false);
    }

    public Operation(String title, OperationManager opMan, boolean indeterminate) {
        this.title   = title;
        this.state   = OperationState.NONE;
        this.progress = 0;
        this.opMan    = opMan;
        this.id       = System.currentTimeMillis() + title.hashCode();
        this.indeterminate = indeterminate;
    }

    public int getState() {
        return state;
    }

    public boolean getAbort() {
        return abort;
    }

    public int getProgress() {
        return progress;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public void setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
    }

    public boolean isEventUnsafe() {
        return eventUnsafe;
    }

    public void setEventUnsafe(boolean eventUnsafe) {
        this.eventUnsafe = eventUnsafe;
    }

    public void setProgress(int progress) throws OperationCancelledException {
        if (state == OperationState.STARTED) {
            this.progress = progress;
            opMan.updateOperationProgress(this);

        } else if (state == OperationState.ABORTED) {
            throw new OperationCancelledException();
        }
    }

    public void setProgress(int progress, String tip) throws OperationCancelledException {
        opMan.updateToolTip(tip);
        setProgress(progress);
    }

    public void startOperation() {
        if (state != OperationState.STARTED) {
            progress = 0;
            state = OperationState.STARTED;
            opMan.updateOperationState(this);
        }
    }

    public void startOperation(String tip) {
        opMan.updateToolTip(tip);
        startOperation();
    }

    public void abortOperation() {
        if (state == OperationState.STARTED) {
            state = OperationState.ABORTED;
            opMan.updateOperationState(this);
        }
    }

    public void completeOperation() {
        if (state == OperationState.STARTED) {
            state = OperationState.COMPLETED;
            opMan.updateOperationState(this);
        }
    }

}
