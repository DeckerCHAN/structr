/*
 *  Copyright (C) 2012 Axel Morgner
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */



package org.structr.web.resource;

import org.apache.commons.collections.ListUtils;


import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Command;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.node.*;
import org.structr.core.node.search.Search;
import org.structr.core.node.search.SearchAttribute;
import org.structr.core.node.search.SearchNodeCommand;
import org.structr.rest.RestMethodResult;
import org.structr.rest.exception.IllegalPathException;
import org.structr.rest.exception.NotFoundException;
import org.structr.rest.resource.Resource;
import org.structr.rest.resource.TypeResource;
import org.structr.rest.resource.UuidResource;
import org.structr.web.entity.Component;
import org.structr.web.entity.Content;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import org.neo4j.graphdb.Direction;
import org.structr.common.RelType;
import org.structr.web.common.RelationshipHelper;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class DynamicTypeResource extends TypeResource {

	private static final Logger logger = Logger.getLogger(DynamicTypeResource.class.getName());

	//~--- fields ---------------------------------------------------------

	private List<DynamicTypeResource> nestedResources = new ArrayList<DynamicTypeResource>();
	private UuidResource uuidResource                 = null;
	private boolean parentResults                     = false;

	//~--- methods --------------------------------------------------------

	@Override
	public boolean checkAndConfigure(String part, SecurityContext securityContext, HttpServletRequest request) throws FrameworkException {

		this.securityContext = securityContext;
		this.request         = request;
		this.rawType         = part;

		super.checkAndConfigure(part, securityContext, request);

		// FIXME: do type check on existing dynamic resources here..
		return rawType != null;
	}

	@Override
	public String toString() {
		return "DynamicTypeResource(".concat(this.rawType).concat(")");
	}

	@Override
	public List<GraphObject> doGet() throws FrameworkException {

		List<GraphObject> uuidResults = null;

		// REST path contained uuid, return result of UuidResource
		if (uuidResource != null) {

			uuidResults = (List<GraphObject>) uuidResource.doGet();

		}

		// check for dynamic type, use super class otherwise
		List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
		AbstractNode topNode                   = null;
		boolean includeDeleted                 = false;
		boolean publicOnly                     = false;

		if (rawType != null) {

			searchAttributes.add(Search.andExactProperty(Component.UiKey.structrclass.name(), EntityContext.normalizeEntityName(rawType)));
			searchAttributes.add(Search.andExactType(Component.class.getSimpleName()));

			// searchable attributes from EntityContext
			hasSearchableAttributes(rawType, request, searchAttributes);

			// do search
			List<GraphObject> results = (List<GraphObject>) Services.command(securityContext, SearchNodeCommand.class).execute(topNode, includeDeleted, publicOnly, searchAttributes);

			if (!results.isEmpty()) {

				// intersect results with uuid result
				if (uuidResults != null) {

					results = ListUtils.intersection(results, uuidResults);

				}

				// check if nested DynamicTypeResources have valid results
				for (DynamicTypeResource res : nestedResources) {

					if (res.doGet().isEmpty()) {

						throw new NotFoundException();

					}

				}

				return results;
			}

		}

		parentResults = true;

		return super.doGet();
	}

	private long getMaxPosition(final List<GraphObject> templates, final String resourceId) {

		long pos = 0;

		for (GraphObject template : templates) {
			if (template instanceof Component) {
				Component component = (Component) template;

				List<AbstractRelationship> rels = component.getRelationships(RelType.CONTAINS, Direction.INCOMING);
				for (AbstractRelationship rel : rels) {
					pos = Math.max(pos, rel.getLongProperty(resourceId));

				}
				
			}
		}

		return pos;

	}

	@Override
	public RestMethodResult doPost(final Map<String, Object> propertySet) throws FrameworkException {

		// REST path contained uuid, POST not allowed here
		if (uuidResource != null) {

			throw new IllegalPathException();

		}

		List<GraphObject> templates = doGet();

		if (parentResults) {

			return super.doPost(propertySet);

		} else if (!templates.isEmpty()) {

			final Command createNodeCommand              = Services.command(securityContext, CreateNodeCommand.class);
			final Map<String, Object> templateProperties = new LinkedHashMap<String, Object>();
			final String componentId                     = UUID.randomUUID().toString().replaceAll("[\\-]+", "");
			final Component template                     = (Component) templates.get(0);

			// copy properties to map
			templateProperties.put(AbstractNode.Key.type.name(), Component.class.getSimpleName());
			templateProperties.put("structrclass", template.getStringProperty("structrclass"));
			templateProperties.put("uuid", componentId);

			// use parentId from template
			String parentComponentId = template.getComponentId();
			String parentResourceId  = template.getResourceId();

			final long position                           = getMaxPosition(templates, parentResourceId) + 1;

			if ((wrappedResource != null) && (wrappedResource instanceof UuidResource)) {

				parentComponentId = ((UuidResource) wrappedResource).getUuid();

			} else if (!nestedResources.isEmpty()) {

				DynamicTypeResource nested = nestedResources.get(nestedResources.size() - 1);

				if (nested.uuidResource != null) {

					parentComponentId = nested.uuidResource.getUuid();

				}

			}

			final String finalParentComponentId = parentComponentId;
			final String finalParentResourceId  = parentResourceId;
			Component newComponent              = (Component) Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {

					Component comp = (Component) createNodeCommand.execute(templateProperties);

					RelationshipHelper.copyRelationships(securityContext, template, comp, finalParentResourceId, finalParentComponentId, position);

					Map<String, Object> contentTemplateProperties = new LinkedHashMap<String, Object>();

					for (AbstractNode node : template.getContentNodes().values()) {

						// copy content properties
						if (node instanceof Content) {

							Content contentTemplate = (Content) node;
							String dataKey          = contentTemplate.getStringProperty("data-key");

							// create new content node with content from property set
							contentTemplateProperties.clear();
							contentTemplateProperties.put(AbstractNode.Key.type.name(), "Content");
							contentTemplateProperties.put("data-key", dataKey);
							contentTemplateProperties.put("content", propertySet.get(dataKey));

							Content newContent = (Content) createNodeCommand.execute(contentTemplateProperties);

							// remove non-local data key from set
							propertySet.remove(dataKey);
							RelationshipHelper.copyRelationships(securityContext, contentTemplate, newContent, finalParentResourceId, componentId, position);

						}
					}

					return comp;
				}

			});

			if (newComponent != null) {

				for (String key : propertySet.keySet()) {

					newComponent.setProperty(key, propertySet.get(key));

				}

			}

			RestMethodResult result = new RestMethodResult(201);

			if (newComponent != null) {

				result.addHeader("Location", buildLocationHeader(newComponent));

			}

			return result;

		} else {

			return super.doPost(propertySet);

		}
	}

	@Override
	public RestMethodResult doDelete() throws FrameworkException {

		final Command deleteCommand      = Services.command(securityContext, DeleteNodeCommand.class);
		final List<GraphObject> toDelete = doGet();
		final boolean cascade            = true;

		for (GraphObject obj : toDelete) {

			if (obj instanceof Component) {

				Set<AbstractNode> contentNodes = new LinkedHashSet<AbstractNode>();

				contentNodes.addAll(((Component) obj).getContentNodes().values());

				for (AbstractNode contentNode : contentNodes) {

					deleteCommand.execute(contentNode, cascade);

				}

			}

			deleteCommand.execute(obj, cascade);

		}

		return new RestMethodResult(200);
	}

	@Override
	public Resource tryCombineWith(Resource next) throws FrameworkException {

		int x = 0;

		if (next instanceof UuidResource) {

			this.uuidResource = (UuidResource) next;

			return this;

		} else if (next instanceof DynamicTypeResource) {

			((DynamicTypeResource) next).nestedResources.add(this);

			return next;

		} else if (next instanceof TypeResource) {

			throw new IllegalPathException();

		}

		return super.tryCombineWith(next);
	}

	// ----- private methods -----


	//~--- get methods ----------------------------------------------------

	@Override
	public boolean isCollectionResource() {

		if (uuidResource != null) {

			return uuidResource.isCollectionResource();

		}

		return true;
	}
}