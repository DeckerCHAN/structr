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
package org.structr.core.cron;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.core.agent.Agent;
import org.structr.core.agent.ReturnValue;
import org.structr.core.agent.Task;

/**
 * An agent to test the cron service. This agent just logs the execution of the
 * test task and returns a success value.
 *
 * @author Christian Morgner
 */
public class CronTestAgent extends Agent {

	private static final Logger logger = Logger.getLogger(CronTestAgent.class.getName());

	@Override
	public Class getSupportedTaskType() {
		return CronTestTask.class;
	}

	@Override
	public ReturnValue processTask(Task task) throws Throwable {

		logger.log(Level.INFO, "Processing test task {0}", task.getClass().getName());

		return ReturnValue.Success;
	}

}
