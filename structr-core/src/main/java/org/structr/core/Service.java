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

package org.structr.core;

import java.util.Map;

//~--- interfaces -------------------------------------------------------------

/**
 * The base class for services in structr.
 *
 * @author cmorgner
 */
public interface Service {

	/**
	 * Called by <code>Services.createCommand()</code> before the command is returned to
	 * the user. Use this method to inject service-specific resources into your command
	 * objects so you can access them later in the <code>execute()</code> method.
	 *
	 * @param command
	 */
	public void injectArguments(Command command);

	/**
	 * Called by <code>Serivces</code> after the service is instantiated to initialize
	 * service-specific resources etc.
	 *
	 * @param context the context
	 */
	public void initialize(Map<String, String> context);

	/**
	 * Called before the service is discarded. Note that this method will not be called
	 * for instances of <code>PrototypeService</code>.
	 */
	public void shutdown();

	//~--- get methods ----------------------------------------------------

	/**
	 * Return name of service
	 * @return
	 */
	public String getName();

	/**
	 * Return true if Service is running.
	 * @return
	 */
	public boolean isRunning();

}
