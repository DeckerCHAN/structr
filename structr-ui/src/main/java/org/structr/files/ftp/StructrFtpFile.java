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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.ftpserver.ftplet.FtpFile;
import org.structr.web.entity.File;

/**
 *
 * @author Axel Morgner
 */
public class StructrFtpFile extends AbstractStructrFtpFile {

	private static final Logger logger = Logger.getLogger(StructrFtpFile.class.getName());

	public StructrFtpFile(final File file) {
		super(file);
	}

//	public StructrFtpFile(final String newPath, final StructrFtpUser user) {
//		super(newPath, user);
//	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public long getSize() {
		Long size = ((File) structrFile).getSize();
		return size == null ? 0 : size;
	}

	@Override
	public boolean mkdir() {
		logger.log(Level.INFO, "mkdir()");
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public OutputStream createOutputStream(final long l) throws IOException {
		return ((File) structrFile).getOutputStream();
	}

	@Override
	public InputStream createInputStream(final long l) throws IOException {
		return ((File) structrFile).getInputStream();
	}		

	@Override
	public List<FtpFile> listFiles() {
		logger.log(Level.INFO, "listFiles()");
		return null;
	}
	
}

