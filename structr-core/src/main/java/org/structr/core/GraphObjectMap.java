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
package org.structr.core;

import java.util.*;
import org.neo4j.graphdb.PropertyContainer;
import org.structr.core.property.PropertyKey;
import org.structr.core.property.PropertyMap;
import org.structr.common.SecurityContext;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.error.FrameworkException;
import org.structr.core.entity.AbstractNode;

/**
 * A dummy graph object that uses a map as its data store.
 * 
 * @author Christian Morgner
 */
public class GraphObjectMap extends PropertyMap implements GraphObject {

	@Override
	public long getId() {
		return -1;
	}

	@Override
	public String getUuid() {
		return getProperty(AbstractNode.uuid);
	}

	@Override
	public String getType() {
		return getProperty(AbstractNode.uuid);
	}

	@Override
	public Iterable<PropertyKey> getPropertyKeys(String propertyView) {
		return properties.keySet();
	}

	@Override
	public void setProperty(PropertyKey key, Object value) throws FrameworkException {
		properties.put(key, value);
	}

	@Override
	public <T> T getProperty(PropertyKey<T> propertyKey) {
		return (T)properties.get(propertyKey);
	}

	@Override
	public <T> Comparable getComparableProperty(PropertyKey<T> key) {
		return (Comparable)getProperty(key);
	}

	@Override
	public void removeProperty(PropertyKey key) throws FrameworkException {
		properties.remove(key);
	}

	@Override
	public PropertyKey getDefaultSortKey() {
		return null;
	}

	@Override
	public String getDefaultSortOrder() {
		return "asc";
	}

	@Override
	public void unlockReadOnlyPropertiesOnce() {
	}

	@Override
	public boolean onCreation(SecurityContext securityContext, ErrorBuffer errorBuffer) throws FrameworkException {
		return true;
	}

	@Override
	public boolean onModification(SecurityContext securityContext, ErrorBuffer errorBuffer) throws FrameworkException {
		return true;
	}

	@Override
	public boolean onDeletion(SecurityContext securityContext, ErrorBuffer errorBuffer, PropertyMap properties) throws FrameworkException {
		return true;
	}

	@Override
	public void afterCreation(SecurityContext securityContext) {
	}

	@Override
	public void afterModification(SecurityContext securityContext) {
	}

	@Override
	public void ownerModified(SecurityContext securityContext) {
	}

	@Override
	public void securityModified(SecurityContext securityContext) {
	}

	@Override
	public void locationModified(SecurityContext securityContext) {
	}

	@Override
	public void propagatedModification(SecurityContext securityContext) {
	}

	// ----- interface map -----
	@Override
	public int size() {
		return properties.size();
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	@Override
	public Object put(PropertyKey key, Object value) {
		return properties.put(key, value);
	}

	@Override
	public void clear() {
		properties.clear();
	}

	@Override
	public Set keySet() {
		return properties.keySet();
	}

	@Override
	public Collection values() {
		return properties.values();
	}

	@Override
	public Set entrySet() {
		return properties.entrySet();
	}

	@Override
	public Object getPropertyForIndexing(PropertyKey key) {
		return null;
	}

	@Override
	public PropertyContainer getPropertyContainer() {
		return null;
	}

	@Override
	public void addToIndex() {
	}

	@Override
	public void updateInIndex() {
	}

	@Override
	public void removeFromIndex() {
	}
	
	@Override
	public void indexPassiveProperties() {
	}
}
