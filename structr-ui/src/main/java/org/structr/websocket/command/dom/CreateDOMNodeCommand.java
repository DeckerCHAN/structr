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
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.command.AbstractCommand;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;
import org.w3c.dom.Document;

/**
 *
 * @author Christian Morgner
 */
public class CreateDOMNodeCommand extends AbstractCommand {

	static {
		
		StructrWebSocket.addCommand(CreateDOMNodeCommand.class);
	}
	
	@Override
	public void processMessage(WebSocketMessage webSocketData) {

		Map<String, Object> nodeData = webSocketData.getNodeData();

		String pageId                = webSocketData.getPageId();
		if (pageId != null) {
			
			Document document = getPage(pageId);
			if (document != null) {

				String tagName    = (String) nodeData.get("tagName");
				if (tagName != null && !tagName.isEmpty()) {

					document.createElement(tagName);

				} else {

					document.createTextNode("");
				}
				
			} else {
		
				getWebSocket().send(MessageBuilder.status().code(404).message("Page not found").build(), true);		
			}
			
		} else {
		
			getWebSocket().send(MessageBuilder.status().code(422).message("Cannot create node without pageId").build(), true);		
		}
	}

	@Override
	public String getCommand() {
		return "CREATE_DOM_NODE";
	}
}
