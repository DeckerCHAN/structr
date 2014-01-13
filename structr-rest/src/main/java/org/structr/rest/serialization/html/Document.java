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
package org.structr.rest.serialization.html;

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Christian Morgner
 */
public class Document extends Tag {

	private PrintWriter writer = null;
	
	public Document(final PrintWriter writer) {
		super(null, "html", false, true);
		
		this.writer = writer;
	}
	
	public void setIndent(final String indent) {
		
	}
	
	public void flush() {
		writer.flush();
	}
	
	public void render() throws IOException {
		
		writer.println("<!DOCTYPE html>");
		
		render(writer, 0);
	}
}
