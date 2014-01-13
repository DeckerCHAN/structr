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

package org.structr.websocket.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;
import org.eclipse.jetty.websocket.WebSocketFactory.Acceptor;

import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.WebSocketDataGSONAdapter;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.neo4j.graphdb.GraphDatabaseService;
import org.structr.core.property.PropertyKey;
import org.structr.core.Services;
import org.structr.core.graph.NodeService;
import org.structr.rest.ResourceProvider;
import org.structr.websocket.SynchronizationController;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class WebSocketServlet extends HttpServlet {

	private static final String STRUCTR_PROTOCOL              = "structr";
	private static ServletConfig config                       = null;
	private static WebSocketFactory factory                   = null;
	private static final Logger logger                        = Logger.getLogger(WebSocketServlet.class.getName());

	private SynchronizationController syncController          = null;
	private PropertyKey idProperty                            = null;
	private ResourceProvider resourceProvider                 = null;
	
	public WebSocketServlet(final PropertyKey idProperty) {
		this.idProperty = idProperty;
	}

	public WebSocketServlet(final ResourceProvider resourceProvider) {
		this.resourceProvider    = resourceProvider;
	}

	public WebSocketServlet(final ResourceProvider resourceProvider, final PropertyKey idProperty) {
		this.idProperty = idProperty;
		this.resourceProvider    = resourceProvider;
	}

	//~--- methods --------------------------------------------------------

	@Override
	public void init() {

		// servlet config
		config = this.getServletConfig();

		// create GSON serializer
		final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(WebSocketMessage.class, new WebSocketDataGSONAdapter(idProperty)).create();

		syncController = new SynchronizationController(gson);
		syncController.setResourceProvider(resourceProvider);

		GraphDatabaseService graphDb = Services.getService(NodeService.class).getGraphDb();
		graphDb.registerTransactionEventHandler(syncController);

		// create web socket factory
		factory = new WebSocketFactory(new Acceptor() {

			@Override
			public WebSocket doWebSocketConnect(final HttpServletRequest request, final String protocol) {

				if (STRUCTR_PROTOCOL.equals(protocol)) {

					return new StructrWebSocket(syncController, config, request, gson, idProperty);

				} else {

					logger.log(Level.INFO, "Protocol {0} not accepted", protocol);

				}

				return null;
			}
			@Override
			public boolean checkOrigin(final HttpServletRequest request, final String origin) {

				// TODO: check origin
				return true;
			}

		});
	}

	@Override
	public void destroy() {
		
		GraphDatabaseService graphDb = Services.getService(NodeService.class).getGraphDb();
		graphDb.unregisterTransactionEventHandler(syncController);
	}

	@Override
	protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws IOException {

		// accept connection
		if (!factory.acceptWebSocket(request, response)) {

			logger.log(Level.INFO, "Request rejected.");
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

		} else {

			logger.log(Level.INFO, "Request accepted.");

		}
	}

	public void setResourceProvider(final ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
	}
	
}
