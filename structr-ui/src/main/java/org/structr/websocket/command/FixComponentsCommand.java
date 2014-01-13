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


import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.Direction;
import org.structr.web.common.RelType;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Result;
import org.structr.core.Services;
import org.structr.core.graph.CreateRelationshipCommand;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.TransactionCommand;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchNodeCommand;
import org.structr.web.entity.dom.DOMNode;
import org.structr.web.entity.dom.Page;
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;


/**
 * Fix 'lost' components
 *
 * @author Axel Morgner
 */
public class FixComponentsCommand extends AbstractCommand {

	private static final Logger logger                                  = Logger.getLogger(FixComponentsCommand.class.getName());
	
	static {

		StructrWebSocket.addCommand(FixComponentsCommand.class);
	}

	@Override
	public void processMessage(WebSocketMessage webSocketData) {

		final SecurityContext securityContext = getWebSocket().getSecurityContext();

		try {


			StructrTransaction transaction               = new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {

					fixLostComponents();

					return null;

				}

			};

			Services.command(securityContext, TransactionCommand.class).execute(transaction);

		} catch (Exception ex) {

			// send DOM exception
			getWebSocket().send(MessageBuilder.status().code(422).message(ex.getMessage()).build(), true);
		}

	}

	@Override
	public String getCommand() {

		return "FIX_LOST_COMPONENTS";

	}
	
	/**
	 * Iterate over all DOM nodes and connect all nodes to the shadow document which
	 * fulfill the following criteria:
	 * 
	 * - has child nodes
	 * - has at least one SYNC relationship (out or in)
	 * 
	 * @return
	 * @throws FrameworkException 
	 */
	private void fixLostComponents() throws FrameworkException {
		
		Page hiddenDoc = getOrCreateHiddenDocument();
		
		SecurityContext securityContext = SecurityContext.getSuperUserInstance();

		Result<DOMNode> result = (Result<DOMNode>) Services.command(securityContext, SearchNodeCommand.class).execute(
			Search.andExactTypeAndSubtypes(DOMNode.class)
		);

		final CreateRelationshipCommand<?> createRel = Services.command(securityContext, CreateRelationshipCommand.class);
		
		for (DOMNode node : result.getResults()) {
			
			if (node.hasChildNodes()
				&& (node.hasRelationship(RelType.SYNC, Direction.INCOMING) || node.hasRelationship(RelType.SYNC, Direction.OUTGOING))
				&& (!hiddenDoc.equals(node.getOwnerDocument()))
				) {
				
				try {
				
					DOMNode clonedNode = (DOMNode) node.cloneNode(false);

					moveChildNodes(node, clonedNode);
					clonedNode.setProperty(DOMNode.ownerDocument, hiddenDoc);

					createRel.execute(node, clonedNode, RelType.SYNC, true);
					createRel.execute(clonedNode, node, RelType.SYNC, true);
					
				} catch (Exception ex) {
					
					logger.log(Level.SEVERE, "Could not fix component " + node, ex);
					
				}
				
			}
			
		}

	}

}
