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

package org.structr.core.graph;

import org.neo4j.graphdb.GraphDatabaseService;

import org.structr.common.RelType;
import org.structr.common.error.FrameworkException;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;
import org.structr.core.Services;
import org.structr.core.Transformation;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Principal;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.structr.common.Permission;
import org.structr.core.entity.SecurityRelationship;
import org.structr.core.property.PropertyKey;
import org.structr.core.property.PropertyMap;

//~--- classes ----------------------------------------------------------------

/**
 * Creates a new node in the database with the given properties.
 *
 * @author Christian Morgner
 */
public class CreateNodeCommand<T extends AbstractNode> extends NodeServiceCommand {

	private static final Logger logger = Logger.getLogger(CreateNodeCommand.class.getName());

	public T execute(Collection<NodeAttribute> attributes) throws FrameworkException {
		
		PropertyMap properties = new PropertyMap();
		for (NodeAttribute attribute : attributes) {
			
			properties.put(attribute.getKey(), attribute.getValue());
		}
		
		return execute(properties);
		
	}
	
	public T execute(NodeAttribute... attributes) throws FrameworkException {
		
		PropertyMap properties = new PropertyMap();
		for (NodeAttribute attribute : attributes) {
			
			properties.put(attribute.getKey(), attribute.getValue());
		}
		
		return execute(properties);
	}
	
	public T execute(PropertyMap attributes) throws FrameworkException {

		GraphDatabaseService graphDb = (GraphDatabaseService) arguments.get("graphDb");
		Principal user               = securityContext.getUser(false);
		T node	                     = null;

		if (graphDb != null) {

			CreateRelationshipCommand createRel = Services.command(securityContext, CreateRelationshipCommand.class);
			Date now                            = new Date();

			// Determine node type
			PropertyMap properties     = new PropertyMap(attributes);
			Object typeObject          = properties.get(AbstractNode.type);
			String nodeType            = (typeObject != null) ? typeObject.toString() : EntityContext.getFactoryDefinition().getGenericNodeType();
			NodeFactory<T> nodeFactory = new NodeFactory<T>(securityContext);
			boolean isCreation         = true;

			// Create node with type
			node = nodeFactory.instantiateWithType(graphDb.createNode(), nodeType, isCreation);
			if(node != null) {
				
				TransactionCommand.nodeCreated(node);
				
				if ((user != null) && user instanceof AbstractNode) {

					// Create new relationship to user and grant permissions to user or group
					AbstractNode owner = (AbstractNode)user;
					createRel.execute(owner, node, RelType.OWNS, false);
					
					SecurityRelationship securityRel = (SecurityRelationship) createRel.execute(owner, node, RelType.SECURITY, false);
					securityRel.setAllowed(Permission.values());

					node.unlockReadOnlyPropertiesOnce();
					node.setProperty(AbstractNode.createdBy, user.getProperty(AbstractNode.uuid));
				}
				
				node.unlockReadOnlyPropertiesOnce();
				node.setProperty(AbstractNode.createdDate, now);

				node.unlockReadOnlyPropertiesOnce();
				node.setProperty(AbstractNode.lastModifiedDate, now);

				// properties.remove(AbstractNode.type);

				for (Entry<PropertyKey, Object> attr : properties.entrySet()) {

					Object value = attr.getValue();
					PropertyKey key = attr.getKey();
					if (key.isReadOnly()) {
						node.unlockReadOnlyPropertiesOnce();
					}
					node.setProperty(key, value);

				}

				properties.clear();
			}

		}

		if (node != null) {
			
			// notify node of its creation
			node.onNodeCreation();

			// iterate post creation transformations
			for (Transformation<GraphObject> transformation : EntityContext.getEntityCreationTransformations(node.getClass())) {

				transformation.apply(securityContext, node);

			}
		}

		return node;
	}
}
