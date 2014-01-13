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
package org.structr.files.ftp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

/**
 *
 * @author Axel Morgner
 */
public class StructrFileSystemFactory implements FileSystemFactory {

	private static final Logger logger = Logger.getLogger(StructrFileSystemFactory.class.getName());

	@Override
	public FileSystemView createFileSystemView(final User user) throws FtpException {
		FileSystemView fileSystemView = new StructrFileSystemView(user);
		logger.log(Level.INFO, "Created Structr File System View [user, homeDir, workingDir]: {0}, {1}, {2}", new Object[]{user.getName(), fileSystemView.getHomeDirectory().getAbsolutePath(), fileSystemView.getWorkingDirectory().getAbsolutePath()});
		return fileSystemView;
	}
}
