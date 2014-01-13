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
package org.structr.core.converter;

import org.structr.common.SecurityContext;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;

/**
 * Returns the "type" property of the end node when evaluated.
 *
 * @author Christian Morgner
 */
public class RelationshipEndNodeTypeConverter extends PropertyConverter<Object, String> {

	public RelationshipEndNodeTypeConverter(SecurityContext securityContext, GraphObject entity) {
		super(securityContext, entity);
	}
	
	@Override
	public Object revert(String source) {
		
		if (currentObject instanceof AbstractRelationship) {
			
			AbstractRelationship rel = (AbstractRelationship) currentObject;
			if (rel != null) {
				
				AbstractNode endNode = rel.getEndNode();
				if (endNode != null) {
					
					return endNode.getType();
				}
			}
		}
		
		return null;
	}

	@Override
	public String convert(Object source) {
		return source != null ? source.toString() : null;
	}
}
