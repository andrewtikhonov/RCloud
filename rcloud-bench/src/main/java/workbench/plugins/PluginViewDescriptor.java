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
package workbench.plugins;

public class PluginViewDescriptor {

	
	private String name;
	private String className;
	private String pluginName;
	private ClassLoader pluginClassLoader;
	
	
	public PluginViewDescriptor(String name, String className, String pluginName, ClassLoader pluginClassLoader) {
		super();
		this.name = name;
		this.className = className;
		this.pluginName = pluginName;
		this.pluginClassLoader=pluginClassLoader;
	}
	
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getPluginName() {
		return pluginName;
	}
	
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}
	
	public String toString() {
		return "{name="+name+",classname="+className+",plugin name="+pluginName+"}";
		
	}

	public ClassLoader getPluginClassLoader() {
		return pluginClassLoader;
	}

	public void setPluginClassLoader(ClassLoader pluginCodeBase) {
		this.pluginClassLoader = pluginCodeBase;
	}

}
