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
package org.structr.websocket.command.dom;

import java.util.Map;

import org.structr.web.entity.dom.DOMNode;
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.command.AbstractCommand;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;

import org.w3c.dom.DOMException;

/**
 *
 * @author Axel Morgner
 */
public class InsertBeforeCommand extends AbstractCommand {

	static {

		StructrWebSocket.addCommand(InsertBeforeCommand.class);
	}

	@Override
	public void processMessage(WebSocketMessage webSocketData) {

		String id                    = webSocketData.getId();
		Map<String, Object> nodeData = webSocketData.getNodeData();
		String refId                 = (String) nodeData.get("refId");
		String parentId              = (String) nodeData.get("parentId");

		// check node to append
		if (id == null) {

			getWebSocket().send(MessageBuilder.status().code(422).message("No node to append").build(), true);

			return;

		}

		// check for parent ID
		if (parentId == null) {

			getWebSocket().send(MessageBuilder.status().code(422).message("Cannot add node without parentId").build(), true);

			return;

		}

		DOMNode refNode = getDOMNode(refId );

		// check if parent node with given ID exists
		DOMNode parentNode = getDOMNode(parentId);

		if (parentNode == null) {

			getWebSocket().send(MessageBuilder.status().code(404).message("Parent node not found").build(), true);

			return;

		}

		DOMNode node = getDOMNode(id);

		try {

			// append node to parent
			if (node != null) {

				parentNode.insertBefore(node, refNode);
				
			}
		} catch (DOMException dex) {

			// send DOM exception
			getWebSocket().send(MessageBuilder.status().code(422).message(dex.getMessage()).build(), true);
		}

	}

	@Override
	public String getCommand() {

		return "INSERT_BEFORE";

	}

}
