/**
 * Copyright (C) 2010-2014 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;

/**
 * A transformation that can be applied to an object in the
 * presence of a {@link SecurityContext}.
 *
 * @author Christian Morgner
 */
public abstract class Transformation<T> implements Comparable<Transformation<T>> {
	
	/**
	 * Transforms the given object.
	 * 
	 * @param securityContext the current security context
	 * @param obj the object to transform
	 * 
	 * @throws FrameworkException 
	 */
	public abstract void apply(SecurityContext securityContext, T obj) throws FrameworkException;
	
	/**
	 * Returns the desired position of this transformation in a list. Return
	 * a low value here to get called early, and a high value to get called
	 * late.
	 * 
	 * @return the desired position of this transformation in a list
	 */
	public abstract int getOrder();
	
	@Override
	public int compareTo(Transformation<T> t) {
		return Integer.valueOf(getOrder()).compareTo(Integer.valueOf(t.getOrder()));
	}
}