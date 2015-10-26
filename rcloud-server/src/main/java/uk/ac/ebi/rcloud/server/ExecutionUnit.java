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
package uk.ac.ebi.rcloud.server;

import org.rosuda.JRI.Rengine;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class ExecutionUnit {

	public abstract void run(Rengine e);

    private String input = null;
    private StringBuffer result = new StringBuffer();
    private HashMap<String, Object> attributes = null;
    private ArrayBlockingQueue<String> notifyQueue = null;

    public ExecutionUnit() {
        this(null, null, null);
    }

    public ExecutionUnit(String input) {
        this(input, null, null);
    }

    public ExecutionUnit(HashMap<String, Object> attributes) {
        this(null, attributes, null);
    }

    public ExecutionUnit(String input, HashMap<String, Object> attributes) {
        this(input, attributes, null);
    }

    public ExecutionUnit(ArrayBlockingQueue<String> notifyQueue) {
        this(null, null, notifyQueue);
    }

    public ExecutionUnit(String input, HashMap<String, Object> attributes,
                         ArrayBlockingQueue<String> notifyQueue) {
        this.input = input;
        this.attributes = attributes;
        this.notifyQueue = notifyQueue;
    }

    public String getInput() {
        return input;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public ArrayBlockingQueue<String> getNotifyQueue() {
        return notifyQueue;
    }

    public StringBuffer getResult() {
        return result;
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setNotifyQueue(ArrayBlockingQueue<String> notifyQueue) {
        this.notifyQueue = notifyQueue;
    }

    public void setResult(StringBuffer result) {
        this.result = result;
    }


}
