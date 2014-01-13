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
 * A value holder that can be fetched and set in the presence of a
 * {@link SecurityContext}.
 * 
 * @author Christian Morgner
 */
public interface Value<T> {

	/**
	 * Sets the current value of this value holder.
	 * 
	 * @param securityContext the security context
	 * @param value the value to be set
	 * @throws FrameworkException 
	 */
	public void set(SecurityContext securityContext, T value) throws FrameworkException;
	
	/**
	 * Gets the current value of this value holder.
	 * 
	 * @param securityContext the security context
	 * @return the current value
	 */
	public T get(SecurityContext securityContext);
}
