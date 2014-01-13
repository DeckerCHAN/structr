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

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.RelationshipType;
import org.structr.common.RelType;
import org.structr.common.SecurityContext;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.error.FrameworkException;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.property.PropertyKey;

/**
 *
 * @author Christian Morgner
 */
	
public class ModificationQueue {

	private static final Logger logger = Logger.getLogger(ModificationQueue.class.getName());
	
	private ConcurrentSkipListMap<String, GraphObjectModificationState> modifications = new ConcurrentSkipListMap<String, GraphObjectModificationState>();
	private Set<String> alreadyPropagated                                             = new LinkedHashSet<String>();
	private Set<String> synchronizationKeys                                           = new TreeSet<String>();
	
	/**
	 * Returns a set containing the different entity types of
	 * nodes modified in this queue.
	 * 
	 * @return the types
	 */
	public Set<String> getSynchronizationKeys() {
		return synchronizationKeys;
	}
	
	public boolean doInnerCallbacks(SecurityContext securityContext, ErrorBuffer errorBuffer) throws FrameworkException {

		long t0                  = System.currentTimeMillis();
		boolean hasModifications = true;
		boolean valid = true;
		
		// collect all modified nodes
		while (hasModifications) {

			hasModifications = false;

			for (GraphObjectModificationState state : modifications.values()) {

				if (state.wasModified()) {

					// do callback according to entry state
					valid &= state.doInnerCallback(this, securityContext, errorBuffer);
					hasModifications = true;
				}
			}
		}

		long t = System.currentTimeMillis() - t0;
		if (t > 1000) {
			logger.log(Level.INFO, "{0} ms", t);
		}

		return valid;
	}
	
	public boolean doValidation(SecurityContext securityContext, ErrorBuffer errorBuffer, boolean doValidation) throws FrameworkException {
		
		long t0       = System.currentTimeMillis();
		boolean valid = true;
		
		// do validation and indexing
		for (Entry<String, GraphObjectModificationState> entry : modifications.entrySet()) {

			// do callback according to entry state
			valid &= entry.getValue().doValidationAndIndexing(this, securityContext, errorBuffer, doValidation);
		}

		long t = System.currentTimeMillis() - t0;
		if (t > 1000) {
			logger.log(Level.INFO, "{0} ms", t);
		}

		return valid;
	}

	public void doOuterCallbacks(SecurityContext securityContext) {

		long t0 = System.currentTimeMillis();
		
		// copy modifications, do after transaction callbacks
		for (GraphObjectModificationState state : modifications.values()) {

			if (!state.isDeleted()) {
				
				state.doOuterCallback(securityContext);
			}
		}

		long t = System.currentTimeMillis() - t0;
		if (t > 1000) {
			logger.log(Level.INFO, "{0} ms", t);
		}
	}
	
	public void clear() {
		
		// clear collections afterwards
		alreadyPropagated.clear();
		modifications.clear();
	}

	public void create(AbstractNode node) {
		getState(node).create();
		
//		synchronizationKeys.add(node.getType());
	}

	public void create(AbstractRelationship relationship) {

		getState(relationship).create();

		modifyEndNodes(relationship.getStartNode(), relationship.getEndNode(), relationship.getRelType());
		
		String combinedType = relationship.getProperty(AbstractRelationship.combinedType);
		if (combinedType != null) {
			synchronizationKeys.add(combinedType);
		}
	}

	public void modifyOwner(AbstractNode node) {
		getState(node).modifyOwner();
	}
	
	public void modifySecurity(AbstractNode node) {
		getState(node).modifySecurity();
	}
	
	public void modifyLocation(AbstractNode node) {
		getState(node).modifyLocation();
	}
	
	public void modify(AbstractNode node, PropertyKey key, Object previousValue) {
		getState(node).modify(key, previousValue);
		
		if (key != null&& key.requiresSynchronization()) {
			synchronizationKeys.add(node.getClass().getSimpleName().concat(".").concat(key.getSynchronizationKey()));
		}
	}

	public void modify(AbstractRelationship relationship, PropertyKey key, Object previousValue) {
		getState(relationship).modify(key, previousValue);
		
		if (key != null && key.requiresSynchronization()) {
			synchronizationKeys.add(relationship.getClass().getSimpleName().concat(".").concat(key.getSynchronizationKey()));
		}
	}
	
	public void propagatedModification(AbstractNode node) {

		if (node != null) {
		
			GraphObjectModificationState state = getState(node, true);
			if (state != null) {

				state.propagatedModification();

				// save hash to avoid repeated propagation
				alreadyPropagated.add(hash(node));
			}
	}
	}

	public void delete(AbstractNode node) {
		getState(node).delete(false);
	}

	public void delete(AbstractRelationship relationship, boolean passive) {

		getState(relationship).delete(passive);

		modifyEndNodes(relationship.getStartNode(), relationship.getEndNode(), relationship.getRelType());
	}

	private void modifyEndNodes(AbstractNode startNode, AbstractNode endNode, RelationshipType relType) {
		
//		synchronizationKeys.add(relType.name());

		if (RelType.OWNS.equals(relType)) {

			modifyOwner(startNode);
			modifyOwner(endNode);
			return;
		}

		if (RelType.SECURITY.equals(relType)) {

			modifySecurity(startNode);
			modifySecurity(endNode);
			return;
		}

		if (RelType.IS_AT.equals(relType)) {

			modifyLocation(startNode);
			modifyLocation(endNode);
			return;
		}

		modify(startNode, null, null);
		modify(endNode, null, null);
	}

	private GraphObjectModificationState getState(AbstractNode node) {
		return getState(node, false);
	}
	
	private GraphObjectModificationState getState(AbstractNode node, boolean checkPropagation) {

		String hash = hash(node);
		GraphObjectModificationState state = modifications.get(hash);

		if (state == null && !(checkPropagation && alreadyPropagated.contains(hash))) {

			state = new GraphObjectModificationState(node);
			modifications.put(hash, state);
		}

		return state;
	}

	private GraphObjectModificationState getState(AbstractRelationship rel) {

		String hash = hash(rel);
		GraphObjectModificationState state = modifications.get(hash);

		if (state == null) {

			state = new GraphObjectModificationState(rel);
			modifications.put(hash, state);
		}

		return state;
	}

	private String hash(AbstractNode node) {
		return "N" + node.getId();
	}

	private String hash(AbstractRelationship rel) {
		return "R" + rel.getId();
	}
}
