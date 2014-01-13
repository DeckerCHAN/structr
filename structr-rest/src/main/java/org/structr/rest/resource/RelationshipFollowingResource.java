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

package org.structr.rest.resource;

import org.structr.core.Result;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

import org.structr.common.SecurityContext;
import org.structr.core.entity.AbstractNode;
import org.structr.core.graph.NodeFactory;
import org.structr.rest.RestMethodResult;
import org.structr.rest.exception.IllegalPathException;
import org.structr.rest.exception.NotFoundException;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.structr.core.property.PropertyKey;
import org.structr.common.error.FrameworkException;
import org.structr.core.property.AbstractRelationProperty;

//~--- classes ----------------------------------------------------------------

/**
 * A constraint that collects TypedIdConstraints and tries
 * to verify that a relationship exists between the nodes
 * returned by the collected constraints.
 *
 * @author Christian Morgner
 */
public class RelationshipFollowingResource extends SortableResource implements Evaluator {

	private static final Logger logger = Logger.getLogger(RelationshipFollowingResource.class.getName());

	//~--- fields ---------------------------------------------------------

	private TypedIdResource firstResource                      = null;
	private Set<Object> idSet                                  = null;
	private TypedIdResource lastResource                       = null;
	private int pathLength                                     = 0;
	private TraversalDescription traversalDescription          = null;
	private List<String> uriParts                              = null;
	private Set<AbstractRelationProperty> visitedRelationships = null;

	//~--- constructors ---------------------------------------------------

	public RelationshipFollowingResource(SecurityContext securityContext, TypedIdResource typedIdResource) {

		this.traversalDescription = Traversal.description().depthFirst().uniqueness(Uniqueness.NODE_GLOBAL).evaluator(Evaluators.excludeStartPosition());
		this.visitedRelationships = new LinkedHashSet<AbstractRelationProperty>();
		this.securityContext      = securityContext;
		this.idSet                = new LinkedHashSet<Object>();
		this.uriParts             = new LinkedList<String>();

		// add TypedIdResource to list of evaluators
		traversalDescription = traversalDescription.evaluator(this);

		// store first and last constraint separately
		// to be able to access them faster afterwards
		firstResource = typedIdResource;
		lastResource  = typedIdResource;

		UuidResource idResource = typedIdResource.getIdResource();

		if (idResource instanceof UuidResource) {

			logger.log(Level.FINE, "Adding id {0} to id set", idResource.getUriPart());

			// add uuid from TypedIdResource to idSet
			idSet.add(((UuidResource) idResource).getUriPart());

		} else {

			logger.log(Level.FINE, "Adding id {0} to id set", idResource.getUriPart());

			// add id from TypedIdResource to idSet
			idSet.add(idResource.getUuid());

		}
	}

	//~--- methods --------------------------------------------------------

	public void addTypedIdResource(TypedIdResource typedIdResource) throws FrameworkException {

		logger.log(Level.FINE, "Adding id {0} to id set", typedIdResource.getIdResource().getUriPart());

		// we need to differentiate between UuidResource and UuidResource
		UuidResource idResource = typedIdResource.getIdResource();

		if (idResource instanceof UuidResource) {

			// add uuid from TypedIdResource to idSet
			if (!idSet.add(((UuidResource) idResource).getUriPart())) {

				// id alread in set, this is an illegal path!
				throw new IllegalPathException();
			}
		} else {

			// add id from TypedIdResource to idSet
			if (!idSet.add(idResource.getUuid())) {

				// id alread in set, this is an illegal path!
				throw new IllegalPathException();
			}
		}

		// add id from TypedIdResource to idSet

		uriParts.add(typedIdResource.getUriPart());

		// find static relationship between the two types
		PropertyKey key = findPropertyKey(lastResource, typedIdResource.getTypeResource());
		if (key != null && key instanceof AbstractRelationProperty) {

			AbstractRelationProperty rel = (AbstractRelationProperty)key;
			
			if (!visitedRelationships.contains(rel)) {

				traversalDescription = traversalDescription.relationships(rel.getRelType(), rel.getDirection());
				visitedRelationships.add(rel);
			}

		} else {

			String rawType1    = lastResource.getTypeResource().getRawType();
			String rawType2    = typedIdResource.getTypeResource().getRawType();

			logger.log(Level.INFO, "No relationship defined between {0} and {1}, illegal path", new Object[] { rawType1, rawType2 });

			// no relationship defined, illegal path
			throw new IllegalPathException();

		}

		// store last constraint separately
		lastResource = typedIdResource;
	}

	@Override
	public boolean checkAndConfigure(String part, SecurityContext securityContext, HttpServletRequest request) {
		return false;
	}

	@Override
	public Result doGet(PropertyKey sortKey, boolean sortDescending, int pageSize, int page, String offsetId) throws FrameworkException {

		Path path = getValidatedPath();

		if (path != null) {

			NodeFactory nodeFactory     = new NodeFactory<AbstractNode>(securityContext, pageSize, page, offsetId);

			// traverse path to force evaluation
			nodeFactory.instantiateAll(path.nodes());

			return lastResource.doGet(sortKey, sortDescending, pageSize, page, offsetId);

		} else {

			logger.log(Level.INFO, "No matching path with length {0}", pathLength);

		}

		throw new NotFoundException();
	}

	@Override
	public RestMethodResult doDelete() throws FrameworkException {

		Path path = getValidatedPath();
		if (path != null) {

			// traverse path to force evaluation and validation
			List nodeList     = new LinkedList();
			for (Node node : path.nodes()) {
				nodeList.add(node);
			}

			return lastResource.doDelete();

		}

		throw new NotFoundException();
	}

	@Override
	public RestMethodResult doPut(Map<String, Object> propertySet) throws FrameworkException {

		Path path = getValidatedPath();
		if (path != null) {

			// traverse path to force evaluation and validation
			List nodeList     = new LinkedList();
			for (Node node : path.nodes()) {
				nodeList.add(node);
			}

			return lastResource.doPut(propertySet);

		}

		throw new NotFoundException();
	}

	@Override
	public RestMethodResult doPost(Map<String, Object> propertySet) throws FrameworkException {

		Path path = getValidatedPath();
		if (path != null) {

			// traverse path to force evaluation and validation
			List nodeList     = new LinkedList();
			for (Node node : path.nodes()) {
				nodeList.add(node);
			}

			return lastResource.doPost(propertySet);

		}

		throw new NotFoundException();
	}

	@Override
	public RestMethodResult doHead() throws FrameworkException {
		return lastResource.doHead();
	}

	@Override
	public Resource tryCombineWith(Resource next) throws FrameworkException {

		if (next instanceof TypedIdResource) {

			addTypedIdResource((TypedIdResource) next);

			return this;

		} else if (next instanceof TypeResource) {

			// validate path before combining constraints
			if (getValidatedPath() != null) {

				return new StaticRelationshipResource(securityContext, lastResource, (TypeResource) next);

			} else {

				logger.log(Level.INFO, "No path found!");
				
				throw new NotFoundException();

			}
		}

		return super.tryCombineWith(next);
	}

	// ----- interface Evaluator -----
	@Override
	public Evaluation evaluate(Path path) {

		Node endNode = path.endNode();

		try {

			// only continue if we are on the right track :)
			if (idSet.contains(endNode.getProperty(AbstractNode.uuid.dbName()))) {

				if (path.length() == pathLength) {

					return Evaluation.INCLUDE_AND_PRUNE;

				} else {

					return Evaluation.INCLUDE_AND_CONTINUE;

				}

			}
		} catch (Throwable t) {

			// ignore
			t.printStackTrace();
		}

		// dead end, stop here
		return Evaluation.EXCLUDE_AND_PRUNE;
	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getUriPart() {

		StringBuilder uri = new StringBuilder();

		for (String part : uriParts) {

			uri.append(part);
			uri.append("/");

		}

		return uri.toString();
	}

	// ----- private methods -----
	private Path getValidatedPath() throws FrameworkException {

		// the nodes we want to find an existing path for.
		Node startNode = firstResource.getTypesafeNode().getNode();
		Node endNode   = lastResource.getTypesafeNode().getNode();

		// set desired path length we want to get
		pathLength = idSet.size();

		// traversal should return exactly one path
		Map<Integer, Path> paths = new HashMap<Integer, Path>();

		for (Iterator<Path> it = traversalDescription.traverse(startNode).iterator(); it.hasNext(); ) {

			Path path = it.next();

			// iterate
			for(Node node: path.nodes()) {}
			
			paths.put(path.length(), path);

		}

		Path path = paths.get(pathLength - 1);
		if ((path != null) && path.startNode().equals(startNode) && path.endNode().equals(endNode)) {
			return path;
		}

		return null;
	}

	@Override
	public boolean isCollectionResource() {
		return lastResource.isCollectionResource();
	}

        @Override
        public String getResourceSignature() {
 
                StringBuilder uri = new StringBuilder();
                
		for (String part : uriParts) {

                        if (part.contains("/")) {
                                
                                String[] parts = StringUtils.split(part, "/");
                                
                                for (String subPart : parts) {
                                        
                                        if (!subPart.matches("[a-zA-Z0-9]{32}")) {

                                                uri.append(subPart);
                                                uri.append("/");

                                        }
                                        
                                }
                                
                        } else {
                                
                                if (!part.matches("[a-zA-Z0-9]{32}")) {

                                        uri.append(part);
                                        uri.append("/");

                                }
                                
                        }
                        

		}

		return uri.toString();
        }
}
