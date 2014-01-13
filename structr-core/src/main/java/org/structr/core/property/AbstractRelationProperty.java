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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.*;
import org.structr.common.FactoryDefinition;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.common.error.IdNotFoundToken;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.entity.Relation.Cardinality;
import org.structr.core.graph.*;
import org.structr.core.graph.search.Search;
import org.structr.core.notion.Notion;

/**
 * Abstract base class for all related node properties.
 *
 * @author Christian Morgner
 */
public abstract class AbstractRelationProperty<T> extends Property<T> {
	
	private static final Logger logger = Logger.getLogger(AbstractRelationProperty.class.getName());
	protected Class destType           = null;
	protected RelationshipType relType = null;
	protected Direction direction      = null;
	protected Cardinality cardinality  = null;
	protected int cascadeDelete        = 0;
	
	public abstract Notion getNotion();
	
	public AbstractRelationProperty(String name, Class destType, RelationshipType relType, Direction direction, Cardinality cardinality, int cascadeDelete) {

		super(name);
		
		this.destType      = destType;
		this.relType       = relType;
		this.direction     = direction;
		this.cardinality   = cardinality;
		this.cascadeDelete = cascadeDelete;
	}

	@Override
	public Property<T> indexed() {
		return this;
	}

	@Override
	public Property<T> indexed(NodeService.NodeIndex nodeIndex) {
		return this;
	}
	
	@Override
	public Property<T> indexed(NodeService.RelationshipIndex relIndex) {
		return this;
	}
	
	@Override
	public Property<T> passivelyIndexed() {
		return this;
	}
	
	@Override
	public Property<T> passivelyIndexed(NodeService.NodeIndex nodeIndex) {
		return this;
	}
	
	@Override
	public Property<T> passivelyIndexed(NodeService.RelationshipIndex relIndex) {
		return this;
	}
	
	@Override
	public Object fixDatabaseProperty(Object value) {
		return null;
	}
	
	@Override
	public boolean isSearchable() {
		return false;
	}

	public AbstractRelationship createRelationship(final SecurityContext securityContext, final AbstractNode sourceNode, final AbstractNode targetNode) throws FrameworkException {
		return createRelationship(securityContext, sourceNode, targetNode, new PropertyMap());
	}

	public AbstractRelationship createRelationship(final SecurityContext securityContext, final AbstractNode sourceNode, final AbstractNode targetNode, final PropertyMap properties) throws FrameworkException {

		// create relationship if it does not already exist
		final CreateRelationshipCommand<?> createRel = Services.command(securityContext, CreateRelationshipCommand.class);
		final DeleteRelationshipCommand deleteRel    = Services.command(securityContext, DeleteRelationshipCommand.class);

		if ((sourceNode != null) && (targetNode != null)) {

			final AbstractNode finalTargetNode = targetNode;
			final AbstractNode finalSourceNode = (AbstractNode) sourceNode;
                        
			StructrTransaction<AbstractRelationship> transaction = new StructrTransaction<AbstractRelationship>() {

				@Override
				public AbstractRelationship execute() throws FrameworkException {

                                        PropertyMap props           = new PropertyMap(properties);
					AbstractRelationship newRel = null;

					// set cascade delete value
					if (getCascadeDelete() > 0) {

						props.put(AbstractRelationship.cascadeDelete, new Integer(getCascadeDelete()));

					}
					
					
					if (getDirection().equals(Direction.OUTGOING)) {

						// set combined type
						String tripleKey = EntityContext.createCombinedRelationshipType(declaringClass.getSimpleName(), relType.name(), destType.getSimpleName());
						props.put(AbstractRelationship.combinedType, Search.clean(tripleKey));

						newRel = createRel.execute(sourceNode, finalTargetNode, getRelType(), props, false);
//						newRel = createRel.execute(sourceNode, finalTargetNode, getRelType(), props, true);

					} else {

						// set combined type
						String tripleKey = EntityContext.createCombinedRelationshipType(destType.getSimpleName(), relType.name(), declaringClass.getSimpleName());
						props.put(AbstractRelationship.combinedType, Search.clean(tripleKey));

						newRel = createRel.execute(finalTargetNode, sourceNode, getRelType(), props, false);
//						newRel = createRel.execute(finalTargetNode, sourceNode, getRelType(), props, true);

					}

					if (newRel != null) {

						FactoryDefinition factoryDefinition = EntityContext.getFactoryDefinition();
						
						switch (getCardinality()) {

							case OneToOne:

								ensureOneToMany(finalSourceNode, finalTargetNode, newRel, factoryDefinition, deleteRel);
								ensureManyToOne(finalSourceNode, finalTargetNode, newRel, factoryDefinition, deleteRel);
								break;
								
							case OneToMany:

								ensureOneToMany(finalSourceNode, finalTargetNode, newRel, factoryDefinition, deleteRel);
								break;

							case ManyToOne:
							
								ensureManyToOne(finalSourceNode, finalTargetNode, newRel, factoryDefinition, deleteRel);
								break;

						}

					}

					return newRel;
				}
			};

			// execute transaction
			return Services.command(securityContext, TransactionCommand.class).execute(transaction);

		} else {

			String type = "unknown";

			if (sourceNode != null) {

				type = sourceNode.getType();

			} else if (targetNode != null) {

				type = targetNode.getType();

			}

			if (sourceNode == null) {
				logger.log(Level.WARNING, "No source node!");
				throw new FrameworkException(type, new IdNotFoundToken(sourceNode));
			}
			
			if (targetNode == null) {
				logger.log(Level.WARNING, "No target node!");
				throw new FrameworkException(type, new IdNotFoundToken(targetNode));
			}

		}
		
		return null;
	}

	public void removeRelationship(final SecurityContext securityContext, final AbstractNode sourceNode, final AbstractNode targetNode) throws FrameworkException {

		final DeleteRelationshipCommand deleteRel = Services.command(securityContext, DeleteRelationshipCommand.class);

		if ((sourceNode != null) && (targetNode != null)) {

			final AbstractNode finalTargetNode = targetNode;
			StructrTransaction transaction     = new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {

					switch (getCardinality()) {

						case ManyToOne :
						case OneToOne : {

							String destType = finalTargetNode.getType();

							// delete previous relationships to nodes of the same destination combinedType and direction
							for (AbstractRelationship rel : sourceNode.getRelationships(getRelType(), getDirection())) {

								if (rel.getOtherNode(sourceNode).getType().equals(destType)) {

									deleteRel.execute(rel);

								}

							}

							break;

						}

						case OneToMany : {

							String sourceType = sourceNode.getType();
							
							// Here, we have a OneToMany with OUTGOING Rel, so we need to remove all relationships
							// of the same combinedType incoming to the target node (which should be exaclty one relationship!)
							for (AbstractRelationship rel : finalTargetNode.getRelationships(getRelType(), Direction.INCOMING)) {

								if (rel.getOtherNode(finalTargetNode).getType().equals(sourceType)) {

									deleteRel.execute(rel);

								}

							}
						}

						case ManyToMany : {

							// In this case, remove exact the relationship of the given combinedType
							// between source and target node
							for (AbstractRelationship rel : finalTargetNode.getRelationships(getRelType(), Direction.BOTH)) {

								if (rel.getOtherNode(finalTargetNode).equals(sourceNode)) {

									deleteRel.execute(rel);

								}

							}
						}

					}

					return null;
				}
			};

			// execute transaction
			Services.command(securityContext, TransactionCommand.class).execute(transaction);

		} else {

			String type = "unknown";

			if (sourceNode != null) {

				type = sourceNode.getType();

			} else if (targetNode != null) {

				type = targetNode.getType();

			}

			throw new FrameworkException(type, new IdNotFoundToken(targetNode));

		}
	}

	public Class getDestType() {
		return destType;
	}

	public RelationshipType getRelType() {
		return relType;
	}

	public Direction getDirection() {
		return direction;
	}

	public Cardinality getCardinality() {
		return cardinality;
	}

	public int getCascadeDelete() {
		return cascadeDelete;
	}
	
	@Override
	public void index(GraphObject entity, Object value) {
		// no indexing
	}

	@Override
	public Object getValueForEmptyFields() {
		return null;
	}
	
	// ----- protected methods -----
	
	public List<T> getRelatedNodesReverse(SecurityContext securityContext, GraphObject obj, Class destinationType) {
		
		List<T> relatedNodes = new LinkedList<T>();
		
		if (obj instanceof AbstractNode) {

			AbstractNode node = (AbstractNode)obj;

			NodeFactory nodeFactory = new NodeFactory(securityContext);
			Node dbNode             = node.getNode();
			AbstractNode value      = null;

			try {

				for (Relationship rel : dbNode.getRelationships(getRelType(), getDirection().reverse())) {

					value = nodeFactory.instantiate(rel.getOtherNode(dbNode));

					// break on first hit of desired type
					if (value != null && destinationType.isInstance(value)) {
						relatedNodes.add((T)value);
					}
				}

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Unable to fetch related node: {0}", t.getMessage());
			}

		} else {

			logger.log(Level.WARNING, "Property {0} is registered on illegal type {1}", new Object[] { this, obj.getClass() } );
		}

		return relatedNodes;
	}

	
	// ----- private methods -----
	private void ensureManyToOne(AbstractNode sourceNode, AbstractNode targetNode, AbstractRelationship newRel, FactoryDefinition factoryDefinition, DeleteRelationshipCommand deleteRel) throws FrameworkException {
		
		Class newRelationshipClass = newRel.getClass();
		Class targetType           = targetNode.getClass();

		// ManyToOne: sourceNode may not have relationships to other nodes of the same type!
		
		for (AbstractRelationship rel : sourceNode.getRelationships(getRelType(), getDirection())) {

			if (rel.equals(newRel)) {
				continue;
			}

			Class relationshipClass = rel.getClass();
			boolean isGeneric = factoryDefinition.isGeneric(relationshipClass);

			AbstractNode otherNode = rel.getOtherNode(sourceNode);
			Class otherClass = otherNode.getClass();
			boolean removeRel = targetType.isAssignableFrom(otherClass) || (!isGeneric && newRelationshipClass.isAssignableFrom(relationshipClass));
			
//			if (!removeRel) {
//				
//				// Check interfaces
//				for (Class iface : EntityContext.getInterfacesForType(targetType)) {
//
//					removeRel |= iface.isAssignableFrom(otherClass);
//				}
//			}
			
			if (removeRel) {

				deleteRel.execute(rel);
			}
		}

	}
	
	private void ensureOneToMany(AbstractNode sourceNode, AbstractNode targetNode, AbstractRelationship newRel, FactoryDefinition factoryDefinition, DeleteRelationshipCommand deleteRel) throws FrameworkException {
		
		Class newRelationshipClass = newRel.getClass();
		Class sourceType           = sourceNode.getClass();

		// ManyToOne: targetNode may not have relationships to other nodes of the same type!
		
		for (AbstractRelationship rel : targetNode.getRelationships(getRelType(), getDirection().reverse())) {

			if (rel.equals(newRel)) {
				continue;
			}

			Class relationshipClass = rel.getClass();
			boolean isGeneric = factoryDefinition.isGeneric(relationshipClass);

			AbstractNode otherNode = rel.getOtherNode(targetNode);
			Class otherClass = otherNode.getClass();
			boolean removeRel = sourceType.isAssignableFrom(otherClass) || (!isGeneric && newRelationshipClass.isAssignableFrom(relationshipClass));
			
//			if (!removeRel) {
//				
//				// Check interfaces
//				for (Class iface : EntityContext.getInterfacesForType(sourceType)) {
//
//					removeRel |= iface.isAssignableFrom(otherClass);
//				}
//			}
			
			if (removeRel) {

				deleteRel.execute(rel);
			}
		}

	}
}
