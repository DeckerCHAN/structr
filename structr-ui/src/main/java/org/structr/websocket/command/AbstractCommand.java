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

package org.structr.websocket.command;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.graph.FindNodeCommand;
import org.structr.core.graph.FindRelationshipCommand;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchAttribute;
import org.structr.core.graph.search.SearchNodeCommand;
import org.structr.core.graph.search.SearchRelationshipCommand;
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.core.property.PropertyKey;
import org.structr.core.Result;
import org.structr.core.graph.CreateNodeCommand;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.TransactionCommand;
import org.structr.core.property.PropertyMap;
import org.structr.web.entity.Widget;
import org.structr.web.entity.dom.DOMNode;
import org.structr.web.entity.dom.Page;
import org.structr.web.entity.dom.ShadowDocument;

//~--- classes ----------------------------------------------------------------

/**
 * Base class for all WebSocket messages in structr.
 *
 * @author Christian Morgner
 */
public abstract class AbstractCommand {

	public static final String COMMAND_KEY = "command";
	public static final String ID_KEY      = "id";
	private static final Logger logger     = Logger.getLogger(AbstractCommand.class.getName());

	//~--- fields ---------------------------------------------------------

	private Connection connection          = null;
	private PropertyKey<String> idProperty = null;
	private StructrWebSocket webSocket     = null;

	//~--- methods --------------------------------------------------------

	public abstract void processMessage(final WebSocketMessage webSocketData);

	//~--- get methods ----------------------------------------------------

	public abstract String getCommand();

	public Connection getConnection() {
		return connection;
	}

	public PropertyKey<String> getIdProperty() {
		return idProperty;
	}

	public StructrWebSocket getWebSocket() {
		return webSocket;
	}

	public Page getPage(final String id) {
		
		AbstractNode node = getNode(id);
		
		if (node != null && node instanceof Page) {
			
			return (Page) node;
		}
		
		return null;
	}

	public DOMNode getDOMNode(final String id) {
		
		AbstractNode node = getNode(id);
		
		if (node != null && node instanceof DOMNode) {
			
			return (DOMNode) node;
		}
		
		return null;
	}

	public Widget getWidget(final String id) {
		
		AbstractNode node = getNode(id);
		
		if (node != null && node instanceof Widget) {
			
			return (Widget) node;
		}
		
		return null;
	}
	
	/**
	 * Returns the node to which the uuid parameter
	 * of this command refers to.
	 *
	 * @return the node
	 */
	public AbstractNode getNode(final String id) {

		final SecurityContext securityContext = getWebSocket().getSecurityContext();

		try {

			if (idProperty != null) {

				List<SearchAttribute> attrs = new LinkedList<SearchAttribute>();

				attrs.add(Search.andExactProperty(securityContext, idProperty, id));

				Result results = Services.command(securityContext, SearchNodeCommand.class).execute(true, false, attrs);

				if (!results.isEmpty()) {

					return (AbstractNode) results.get(0);

				}

			} else {

				List<AbstractNode> results = (List<AbstractNode>) Services.command(securityContext, FindNodeCommand.class).execute(id);

				if (!results.isEmpty()) {

					return results.get(0);

				}

			}

		} catch (FrameworkException fex) {
			logger.log(Level.WARNING, "Unable to get node", fex);
		}

		return null;
	}

	/**
	 * Returns the relationship to which the uuid parameter
	 * of this command refers to.
	 *
	 * @return the node
	 */
	public AbstractRelationship getRelationship(final String id) {
	
		if (id == null) {
			return null;
		}

		final SecurityContext securityContext = getWebSocket().getSecurityContext();

		try {

			if (idProperty != null) {

				List<SearchAttribute> attrs = new LinkedList<SearchAttribute>();

				attrs.add(Search.andExactProperty(securityContext, idProperty, id));

				List<AbstractRelationship> results = Services.command(securityContext, SearchRelationshipCommand.class).execute(attrs).getResults();

				if (!results.isEmpty()) {

					return results.get(0);

				}

			} else {

				// FIXME: does this ever get called?
				List<AbstractRelationship> results = (List<AbstractRelationship>)Services.command(securityContext,
									     FindRelationshipCommand.class).execute(id);

				if (!results.isEmpty()) {

					return results.get(0);

				}

			}

		} catch (FrameworkException fex) {
			logger.log(Level.WARNING, "Unable to get relationship", fex);
		}

		return null;
	}

	// ----- protected methods -----
	protected String getIdFromNode(final AbstractNode node) {

		if (idProperty != null) {

			return node.getProperty(idProperty);

		} else {

			return node.getIdString();

		}
	}

	/**
	 * Make child nodes of the source nodes child nodes of the target node.
	 * 
	 * @param sourceNode
	 * @param targetNode 
	 */
	protected void moveChildNodes(final DOMNode sourceNode, final DOMNode targetNode) {
		
		DOMNode child = (DOMNode) sourceNode.getFirstChild();
		
		while (child != null) {
			
			DOMNode next = (DOMNode) child.getNextSibling();
			
			targetNode.appendChild(child);
			
			child = next;
			
		}
		
	}
	/**
	 * Search for a hidden page named __ShadowDocument__ of type {@see ShadowDocument.class}.
	 * 
	 * If found, return it, if not, create it.
	 * The shadow page is the DOM document all reusable components are connected to.
	 * It is necessary to comply with DOM standards.
	 * 
	 * @return
	 * @throws FrameworkException 
	 */
	protected ShadowDocument getOrCreateHiddenDocument() throws FrameworkException {
		
		SecurityContext securityContext = SecurityContext.getSuperUserInstance();

		Result result = (Result) Services.command(securityContext, SearchNodeCommand.class).execute(
			Search.andExactType(ShadowDocument.class)
		);

		if (result.isEmpty()) {

			final CreateNodeCommand cmd  = Services.command(securityContext, CreateNodeCommand.class);
			final PropertyMap properties = new PropertyMap();
			properties.put(AbstractNode.type, ShadowDocument.class.getSimpleName());
			properties.put(AbstractNode.name, "__ShadowDocument__");
			properties.put(AbstractNode.hidden, true);
			properties.put(AbstractNode.visibleToAuthenticatedUsers, true);
		
			ShadowDocument doc = Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction<ShadowDocument>() {

				@Override
				public ShadowDocument execute() throws FrameworkException {

					return (ShadowDocument) cmd.execute(properties);
				}		
			});
	
			return doc;

		}
		
		return (ShadowDocument) result.get(0);
		
		
	}
	
	//~--- set methods ----------------------------------------------------

	public void setConnection(final Connection connection) {
		this.connection = connection;
	}

	public void setIdProperty(final PropertyKey<String> idProperty) {
		this.idProperty = idProperty;
	}

	public void setWebSocket(final StructrWebSocket webSocket) {
		this.webSocket = webSocket;
	}
}
