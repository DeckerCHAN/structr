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
import org.structr.core.entity.AbstractRelationship;

//~--- classes ----------------------------------------------------------------

/**
 * Returns the start node of a relationship when evaluated.
 *
 * @author Axel Morgner
 */
public class RelationshipStartNodeConverter extends PropertyConverter {

	public RelationshipStartNodeConverter(SecurityContext securityContext) {

		super(securityContext);

	}

	public RelationshipStartNodeConverter(SecurityContext securityContext, GraphObject entity) {

		super(securityContext, entity);

	}

	//~--- methods --------------------------------------------------------

	@Override
	public Object revert(Object source) {

		if (currentObject instanceof AbstractRelationship) {

			AbstractRelationship rel = (AbstractRelationship) currentObject;

			if (rel != null) {

				return rel.getStartNode();
			}

		}

		return null;

	}

	@Override
	public Object convert(Object source) {

		return null;

	}

}
