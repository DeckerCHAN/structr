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

import java.util.LinkedList;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.tooling.GlobalGraphOperations;

import org.structr.common.error.FrameworkException;
import org.structr.core.Services;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.SecurityContext;
import org.structr.core.EntityContext;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchAttribute;
import org.structr.core.graph.search.SearchRelationshipCommand;
import org.structr.core.property.PropertyKey;

//~--- classes ----------------------------------------------------------------

/**
 * Sets the properties found in the property set on all relationships matching the type.
 * If no type property is found, set the properties on all relationships.
 *
 * @author Axel Morgner
 */
public class BulkSetRelationshipPropertiesCommand extends NodeServiceCommand implements MaintenanceCommand {

	private static final Logger logger = Logger.getLogger(BulkSetRelationshipPropertiesCommand.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void execute(final Map<String, Object> properties) throws FrameworkException {

		final GraphDatabaseService graphDb            = (GraphDatabaseService) arguments.get("graphDb");
		final RelationshipFactory relationshipFactory = new RelationshipFactory(securityContext);
                final SearchRelationshipCommand searchRel     = Services.command(SecurityContext.getSuperUserInstance(), SearchRelationshipCommand.class);
                
		if (graphDb != null) {

			List<AbstractRelationship> rels = null;

			if (properties.containsKey(AbstractRelationship.combinedType.dbName())) {

				List<SearchAttribute> attrs = new LinkedList<SearchAttribute>();
				attrs.add(Search.andExactProperty(securityContext, AbstractRelationship.type, (String)properties.get(AbstractRelationship.combinedType.dbName())));

				rels = searchRel.execute(attrs).getResults();
				properties.remove(AbstractRelationship.combinedType.dbName());

			} else {

				rels = (List<AbstractRelationship>) relationshipFactory.instantiate(GlobalGraphOperations.at(graphDb).getAllRelationships());
			}

			long count = NodeServiceCommand.bulkGraphOperation(securityContext, rels, 1000, "SetRelationshipProperties", new BulkGraphOperation<AbstractRelationship>() {

				@Override
				public void handleGraphObject(SecurityContext securityContext, AbstractRelationship rel) {

					// Treat only "our" nodes
					if (rel.getProperty(AbstractRelationship.uuid) != null) {

						for (Entry entry : properties.entrySet()) {

							String key = (String) entry.getKey();
							Object val = entry.getValue();

							PropertyKey propertyKey = EntityContext.getPropertyKeyForDatabaseName(rel.getClass(), key);
							if (propertyKey != null) {

								try {
									rel.setProperty(propertyKey, val);
									
							
								} catch (FrameworkException fex) {

									logger.log(Level.WARNING, "Unable to set relationship property {0} of relationship {1} to {2}: {3}", new Object[] { propertyKey, rel.getUuid(), val, fex.getMessage() } );
								}
							}
						}
					}
				}

				@Override
				public void handleThrowable(SecurityContext securityContext, Throwable t, AbstractRelationship rel) {
					logger.log(Level.WARNING, "Unable to set properties of relationship {0}: {1}", new Object[] { rel.getUuid(), t.getMessage() } );
				}

				@Override
				public void handleTransactionFailure(SecurityContext securityContext, Throwable t) {
					logger.log(Level.WARNING, "Unable to set relationship properties: {0}", t.getMessage() );
				}

			});

			logger.log(Level.INFO, "Finished setting properties on {0} relationships", count);
		}
	}
}
