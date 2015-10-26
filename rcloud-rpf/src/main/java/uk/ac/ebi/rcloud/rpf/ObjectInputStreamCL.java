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
package uk.ac.ebi.rcloud.rpf;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public  class ObjectInputStreamCL extends ObjectInputStream {

	private ClassLoader _classLoader;

	public ObjectInputStreamCL(InputStream in, ClassLoader loader) throws IOException {
		super(in);
		_classLoader = loader;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass v)

	throws IOException, ClassNotFoundException {

		return Class.forName(v.getName(), false, _classLoader);
	}

	@Override
	protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {

		ClassLoader latestLoader = _classLoader;
		ClassLoader nonPublicLoader = null;
		boolean hasNonPublicInterface = false;

		// define proxy in class loader of non-public interface(s), if any
		Class<?>[] classObjs = new Class[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> cl = Class.forName(interfaces[i], false, latestLoader);
			if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
				if (hasNonPublicInterface) {
					if (nonPublicLoader != cl.getClassLoader()) {
						throw new IllegalAccessError("conflicting non-public interface class loaders");
					}
				} else {
					nonPublicLoader = cl.getClassLoader();
					hasNonPublicInterface = true;
				}
			}
			classObjs[i] = cl;
		}
		try {
			return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader : latestLoader, classObjs);
		} catch (IllegalArgumentException e) {
			throw new ClassNotFoundException(null, e);
		}
	}
}