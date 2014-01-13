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
package org.structr.core.property;

import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.notion.PropertyNotion;

/**
 * A property that wraps a {@link PropertyNotion} with the entity's UUID around an {@link EntityProperty}.
 *
 * @author Christian Morgner
 */
public class EntityIdProperty<S extends AbstractNode> extends EntityNotionProperty<S, String> {
	
	public EntityIdProperty(String name, Property<S> base) {
		this(name, base, false);
	}
	
	public EntityIdProperty(String name, Property<S> base, boolean createIfNotExisting) {
		
		super(name, base, new PropertyNotion(GraphObject.uuid, createIfNotExisting));
	}
}
