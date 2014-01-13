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
import org.neo4j.tooling.GlobalGraphOperations;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.EntityContext;
import org.structr.core.Result;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- classes ----------------------------------------------------------------

/**
 * Sets a new UUID on each graph object of the given type. For nodes, set type,
 * for relationships relType.
 *
 * @author Axel Morgner
 */
public class BulkSetUuidCommand extends NodeServiceCommand implements MaintenanceCommand {

	private static final Logger logger = Logger.getLogger(BulkSetUuidCommand.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void execute(Map<String, Object> attributes) throws FrameworkException {

		final String entityType                = (String) attributes.get("type");
		final String relType                   = (String) attributes.get("relType");
		final GraphDatabaseService graphDb     = (GraphDatabaseService) arguments.get("graphDb");
		final SecurityContext superUserContext = SecurityContext.getSuperUserInstance();
		final NodeFactory nodeFactory          = new NodeFactory(superUserContext);
		final RelationshipFactory relFactory   = new RelationshipFactory(superUserContext);

		if (entityType != null) {

			final Class type = EntityContext.getEntityClassForRawType(entityType);

			if (type != null) {

				// final Result<AbstractNode> result = Services.command(securityContext, SearchNodeCommand.class).execute(true, false, Search.andExactType(type.getSimpleName()));
				final Result<AbstractNode> result = nodeFactory.instantiateAll(GlobalGraphOperations.at(graphDb).getAllNodes());
				final List<AbstractNode> nodes    = result.getResults();

				logger.log(Level.INFO, "Start setting UUID on all nodes of type {0}", new Object[] { type.getSimpleName() });

				long count = bulkGraphOperation(securityContext, nodes, 1000, "SetNodeProperties", new BulkGraphOperation<AbstractNode>() {

					@Override
					public void handleGraphObject(SecurityContext securityContext, AbstractNode node) {

						if (!node.getClass().equals(type)) {

							return;
						}

						try {

							node.setProperty(AbstractNode.uuid, UUID.randomUUID().toString().replaceAll("[\\-]+", ""));

						} catch (FrameworkException fex) {

							logger.log(Level.WARNING, "Unable to set UUID of node {0}: {1}", new Object[] { node, fex.getMessage() });

						}

					}
					@Override
					public void handleThrowable(SecurityContext securityContext, Throwable t, AbstractNode node) {

						logger.log(Level.WARNING, "Unable to set UUID of node {0}: {1}", new Object[] { node, t.getMessage() });

					}
					@Override
					public void handleTransactionFailure(SecurityContext securityContext, Throwable t) {

						logger.log(Level.WARNING, "Unable to set UUID on node: {0}", t.getMessage());

					}

				});

				logger.log(Level.INFO, "Done with setting UUID on {0} nodes", count);

				return;
			}

		} else if (relType != null) {

			// final Result<AbstractNode> result = Services.command(securityContext, SearchNodeCommand.class).execute(true, false, Search.andExactType(type.getSimpleName()));
			final List<AbstractRelationship> rels = relFactory.instantiate(GlobalGraphOperations.at(graphDb).getAllRelationships());

			logger.log(Level.INFO, "Start setting UUID on all rels of type {0}", new Object[] { relType });

			long count = bulkGraphOperation(securityContext, rels, 1000, "SetRelationshipUuid", new BulkGraphOperation<AbstractRelationship>() {

				@Override
				public void handleGraphObject(SecurityContext securityContext, AbstractRelationship rel) {

					if (!rel.getType().equals(relType)) {

						return;
					}

					try {

						rel.setProperty(AbstractRelationship.uuid, UUID.randomUUID().toString().replaceAll("[\\-]+", ""));

					} catch (FrameworkException fex) {

						logger.log(Level.WARNING, "Unable to set UUID of relationship {0}: {1}", new Object[] { rel, fex.getMessage() });

					}

				}
				@Override
				public void handleThrowable(SecurityContext securityContext, Throwable t, AbstractRelationship rel) {

					logger.log(Level.WARNING, "Unable to set UUID of relationship {0}: {1}", new Object[] { rel, t.getMessage() });

				}
				@Override
				public void handleTransactionFailure(SecurityContext securityContext, Throwable t) {

					logger.log(Level.WARNING, "Unable to set UUID on relationship: {0}", t.getMessage());

				}

			});

			logger.log(Level.INFO, "Done with setting UUID on {0} relationships", count);

			return;
		}

		logger.log(Level.INFO, "Unable to determine entity type to set UUID.");

	}

}
