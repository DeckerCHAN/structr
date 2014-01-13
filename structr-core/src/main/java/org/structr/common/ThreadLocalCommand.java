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
package org.structr.common;

import org.structr.core.Command;
import org.structr.core.Services;

/**
 * A thread-local version of a structr service command. Use this class when you
 * need a static, re-usable instance of a command that you call very often.
 * 
 * @author Christian Morgner
 */
public class ThreadLocalCommand<T extends Command> extends ThreadLocal<T> {
	
	private SecurityContext securityContext = SecurityContext.getSuperUserInstance();
	private Class<T> type                   = null;
	
	public ThreadLocalCommand(SecurityContext securityContext, Class<T> type) {
		this.securityContext = securityContext;
		this.type = type;
	}
	
	public ThreadLocalCommand(Class<T> type) {
		this.type = type;
	}
	
	@Override
	protected T initialValue() {
		return Services.command(securityContext, type);
	}
}
