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

package org.structr.web.entity;

import java.util.List;
import org.neo4j.graphdb.Direction;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.notion.PropertyNotion;
import org.structr.core.property.CollectionNotionProperty;
import org.structr.core.property.CollectionProperty;
import org.structr.core.property.Property;
import org.structr.web.common.RelType;
import org.structr.web.property.UiNotion;

/**
 *
 * @author Christian Morgner
 */
public interface Taggable extends GraphObject {
	
	public static final CollectionProperty<Tag> tags = new CollectionProperty<>("tags", Tag.class, RelType.TAG, Direction.INCOMING, new UiNotion(), false);
	public static final Property<List<String>> tag_names = new CollectionNotionProperty("tag_names", tags, new PropertyNotion(AbstractNode.name));
	
}
