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
package org.structr.common;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.graph.NodeFactory;
import org.structr.core.graph.RelationshipFactory;

/**
 * Defines methods that are used by {@link NodeFactory} and
 * {@link RelationshipFactory} when creating nodes.
 *
 * @author Christian Morgner
 */
public interface FactoryDefinition {
	
	/**
	 * @return an uninitialized instance of a generic relationship
	 */
	public AbstractRelationship createGenericRelationship();
	public String getGenericRelationshiType();
	
	/**
	 * @return an uninitialized instance of a generic node
	 */
	public AbstractNode createGenericNode();
	public String getGenericNodeType();
	
	/**
	 * Indicates whether the given class is a generic type according to
	 * this class.
	 * 
	 * @param entityClass the type to check
	 * @return whether the given type is a generic type
	 */
	public boolean isGeneric(Class<?> entityClass);
	
	/**
	 * Returns an entity name for the given node. A node type can be defined
	 * by the node's surroundings or by a given type property. Its up to the
	 * user of structr to specify this.
	 * 
	 * @param node
	 * @return the entity name as returned by Class.getSimpleName()
	 */
	public String determineNodeType(Node node);
}
