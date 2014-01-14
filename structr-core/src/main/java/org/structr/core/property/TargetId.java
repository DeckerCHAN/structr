package org.structr.core.property;

import org.neo4j.graphdb.Relationship;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.graph.NodeFactory;
import org.structr.core.graph.NodeInterface;
import org.structr.core.graph.RelationshipInterface;

/**
 *
 * @author Christian Morgner
 */
public class TargetId extends Property<String> {

	public TargetId(final String name) {
		super(name);
	}
	
	@Override
	public Class relatedType() {
		return null;
	}

	@Override
	public String getProperty(final SecurityContext securityContext, final GraphObject obj, final boolean applyConverter) {
		
		if (obj instanceof RelationshipInterface) {
		
			try {
				final Relationship relationship = ((RelationshipInterface)obj).getRelationship();
				final NodeInterface endNode     = new NodeFactory<>(securityContext).instantiate(relationship.getEndNode());
				
				return endNode.getUuid();
				
			} catch (Throwable t) {
				
				t.printStackTrace();
			}
		}
		
		return null;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public Integer getSortType() {
		return null;
	}

	@Override
	public void setProperty(SecurityContext securityContext, GraphObject obj, String value) throws FrameworkException {
		
		if (obj instanceof RelationshipInterface) {
		
			try {
				((RelationshipInterface)obj).setTargetNodeId(value);
				
			} catch (Throwable t) {
				
				t.printStackTrace();
			}
		}
	}

	@Override
	public Object fixDatabaseProperty(Object value) {
		return null;
	}

	@Override
	public Object getValueForEmptyFields() {
		return null;
	}

	@Override
	public String typeName() {
		return null;
	}

	@Override
	public PropertyConverter<String, ?> databaseConverter(SecurityContext securityContext) {
		return null;
	}

	@Override
	public PropertyConverter<String, ?> databaseConverter(SecurityContext securityContext, GraphObject entity) {
		return null;
	}

	@Override
	public PropertyConverter<?, String> inputConverter(SecurityContext securityContext) {
		return null;
	}
}
