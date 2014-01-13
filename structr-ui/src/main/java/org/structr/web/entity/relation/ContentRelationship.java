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

package org.structr.web.entity.relation;

import org.structr.core.property.PropertyKey;
import org.structr.web.common.RelType;
import org.structr.core.EntityContext;
import org.structr.core.entity.AbstractRelationship;
import org.structr.web.entity.dom.Content;
import org.structr.web.entity.dom.DOMElement;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedHashSet;
import java.util.Set;
import org.structr.core.property.Property;
import org.structr.core.property.StringProperty;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class ContentRelationship extends AbstractRelationship {

	public static final Property<String> parentId    = new StringProperty("parentId");
	public static final Property<String> contentId   = new StringProperty("contentId");
	public static final Property<String> componentId = new StringProperty("componentId");
	public static final Property<String> pageId      = new StringProperty("pageId");
	public static final Property<String> foo         = new StringProperty("foo");

	static {

		EntityContext.registerNamedRelation("data", ContentRelationship.class, DOMElement.class, Content.class, RelType.CONTAINS);

		// not needed, overridden below
		// EntityContext.registerPropertySet(ContentRelationship.class, PropertyView.All,    ContentRelationship.Key.values());
		// EntityContext.registerPropertySet(ContentRelationship.class, PropertyView.Public, ContentRelationship.Key.values());

	}

	//~--- get methods ----------------------------------------------------

	@Override
	public PropertyKey getStartNodeIdKey() {
		return parentId;
	}

	@Override
	public PropertyKey getEndNodeIdKey() {
		return contentId;
	}

	@Override
	public Iterable<PropertyKey> getPropertyKeys(String propertyView) {

		Set<PropertyKey> keys = new LinkedHashSet<PropertyKey>();

		keys.add(parentId);
		keys.add(contentId);
		keys.add(componentId);
		keys.add(pageId);

		if (dbRelationship != null) {

			for (String key : dbRelationship.getPropertyKeys()) {

				keys.add(EntityContext.getPropertyKeyForDatabaseName(entityType, key));
			}

		}

		return keys;

	}

}
