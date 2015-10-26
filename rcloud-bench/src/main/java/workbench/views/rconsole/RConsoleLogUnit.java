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
package workbench.views.rconsole;

import workbench.generic.ImageContainer;

import javax.swing.text.SimpleAttributeSet;
import java.awt.image.BufferedImage;

public class RConsoleLogUnit {
	private String cmd = null;
	private String log = null;
	private ImageContainer container = null;
	private SimpleAttributeSet logAttributeSet = null;
	
	
	public RConsoleLogUnit(String cmd, String log, ImageContainer container, SimpleAttributeSet logAttributeSet) {
		super();
		this.cmd = cmd;
		this.log = log;
		this.container = container;
		this.logAttributeSet = logAttributeSet;
	}

    public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public ImageContainer getContainer() {
		return container;
	}

	public void setContainer(ImageContainer container) {
		this.container = container;
	}

	public SimpleAttributeSet getLogAttributeSet() {
		return logAttributeSet;
	}

	public void setLogAttributeSet(SimpleAttributeSet logAttributeSet) {
		this.logAttributeSet = logAttributeSet;
	}
	
}
