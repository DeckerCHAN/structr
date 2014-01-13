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

import org.apache.commons.lang.StringUtils;

import org.structr.web.common.RelType;
import org.structr.common.error.FrameworkException;
import org.structr.core.property.PropertyKey;
import org.structr.core.property.PropertyMap;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.Permission;
import org.structr.core.Services;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.TransactionCommand;
import org.structr.websocket.StructrWebSocket;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class UpdateCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(UpdateCommand.class.getName());
	
	static {
		
		StructrWebSocket.addCommand(UpdateCommand.class);

	}

	//~--- methods --------------------------------------------------------

	@Override
	public void processMessage(WebSocketMessage webSocketData) {
		

		GraphObject obj  = getNode(webSocketData.getId());
		Boolean recValue = (Boolean) webSocketData.getNodeData().get("recursive");
		
		boolean rec = recValue != null ? recValue : false;

		webSocketData.getNodeData().remove("recursive");

		if (obj != null) {

			if (!getWebSocket().getSecurityContext().isAllowed(((AbstractNode) obj), Permission.write)) {

				logger.log(Level.WARNING, "No write permission for {0} on {1}", new Object[] {getWebSocket().getCurrentUser().toString(), obj.toString()});
				getWebSocket().send(MessageBuilder.status().message("No write permission").code(400).build(), true);
				return;
				
			}
			
		}
		
		if (obj == null) {

			// No node? Try to find relationship
			obj = getRelationship(webSocketData.getId());
		}

		if (obj != null) {
			
			try {

				setProperties(obj, PropertyMap.inputTypeToJavaType(this.getWebSocket().getSecurityContext(), obj.getClass(), webSocketData.getNodeData()), rec);

			} catch (FrameworkException ex) {

				logger.log(Level.SEVERE, "Unable to set properties: {0}", ((FrameworkException) ex).toString());
				getWebSocket().send(MessageBuilder.status().code(400).build(), true);

			}

		} else {

			logger.log(Level.WARNING, "Graph object with uuid {0} not found.", webSocketData.getId());
			getWebSocket().send(MessageBuilder.status().code(404).build(), true);

		}

	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getCommand() {

		return "UPDATE";

	}

	//~--- set methods ----------------------------------------------------

	private void setProperties(final GraphObject obj, final PropertyMap properties, final boolean rec) throws FrameworkException {

		Services.command(getWebSocket().getSecurityContext(), TransactionCommand.class).execute(new StructrTransaction() {

			@Override
			public Object execute() throws FrameworkException {

				for (Entry<PropertyKey, Object> entry : properties.entrySet()) {

					PropertyKey key = entry.getKey();
					Object value    = entry.getValue();

					obj.setProperty(key, value);

					if (rec && obj instanceof AbstractNode) {

						AbstractNode node = (AbstractNode) obj;

						for (AbstractRelationship rel : node.getOutgoingRelationships(RelType.CONTAINS)) {

							AbstractNode endNode = rel.getEndNode();
							if (endNode != null) {

								setProperties(endNode, properties, rec);
							}
						}
					}
				}
				
				return null;
			}
		});
	}
}
