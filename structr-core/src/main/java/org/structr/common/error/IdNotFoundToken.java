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
package org.structr.common.error;

import org.structr.core.property.PropertyKey;
import org.structr.core.property.StringProperty;

/**
 * Indicates that a given ID could not be found.
 *
 * @author Christian Morgner
 */
public class IdNotFoundToken extends PropertyNotFoundToken {

	// FIXME: this needs to me modified when the uuid/id mess is resolved
	private static final PropertyKey<String> id = new StringProperty("id");
	
	public IdNotFoundToken(Object value) {
		super(id, value);
	}
}
