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
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.structr.common.error.FrameworkException;
import org.structr.web.common.FtpTest;

/**
 * Tests for FTP directories.
 * 
 * @author Axel Morgner
 */
public class FtpDirectoriesTest extends FtpTest {

	private static final Logger logger = Logger.getLogger(FtpDirectoriesTest.class.getName());
	
	public void test01ListDirectories() {
		
		FTPClient ftp = setupFTPClient();
		try {
			
			FTPFile[] dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(0, dirs.length);
			
			String name1 = "FTPdir1";
			
			// Create folder by API methods
			createFTPDirectory(null, name1);
			dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(1, dirs.length);
			assertEquals(name1, dirs[0].getName());

			String name2 = "FTPdir2";
			
			// Create second folder in /
			createFTPDirectory(null, name2);
			dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(2, dirs.length);
			assertEquals(name1, dirs[0].getName());
			assertEquals(name2, dirs[1].getName());
			
			ftp.disconnect();
			
		} catch (IOException | FrameworkException ex) {
			logger.log(Level.SEVERE, "Error while listing FTP directories", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}
	
	public void test02MkDir() {
		
		FTPClient ftp = setupFTPClient();
		
		try {

			assertEmptyDirectory(ftp);
			
			String name1 = "FTPdir1";
			
			// Create folder by mkdir FTP command
			boolean success = ftp.makeDirectory(name1);
			assertTrue(success);

			FTPFile[] dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(1, dirs.length);
			assertEquals(name1, dirs[0].getName());

			String name2 = "FTPdir2";
			
			// Create second folder in /
			success = ftp.makeDirectory(name2);
			assertTrue(success);
			
			dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(2, dirs.length);
			assertEquals(name1, dirs[0].getName());
			assertEquals(name2, dirs[1].getName());
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error while making FTP directories", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}

	public void test03MkdirCd() {

		FTPClient ftp = setupFTPClient();
		
		try {
			
			FTPFile[] dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(0, dirs.length);
			
			String name1 = "/FTPdir1";
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name1);
			
			ftp.changeWorkingDirectory(name1);
			
			assertEmptyDirectory(ftp);
			
			String newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name1, newWorkingDirectory);
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error while changing FTP directories", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}
	
	public void test04MkdirCdMkdirCd() {

		FTPClient ftp = setupFTPClient();
		
		try {
			
			assertEmptyDirectory(ftp);
			
			String name1 = "/FTPdir1";
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name1);
			
			ftp.changeWorkingDirectory(name1);
			
			String newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name1, newWorkingDirectory);

			assertEmptyDirectory(ftp);
			
			String name2 = name1.concat("/").concat("FTPdir2");
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name2);
			
			ftp.changeWorkingDirectory(name2);
			
			newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name2, newWorkingDirectory);
			
			assertEmptyDirectory(ftp);
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}

	public void test05CdUp() {

		FTPClient ftp = setupFTPClient();
		
		try {
			
			assertEmptyDirectory(ftp);
			
			String name1 = "/FTPdir1";
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name1);
			
			ftp.changeWorkingDirectory(name1);
			
			String name2 = name1.concat("/").concat("FTPdir2");
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name2);
			ftp.changeWorkingDirectory(name2);
			
			boolean success = ftp.changeToParentDirectory();
			assertTrue(success);
			
			String newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name1, newWorkingDirectory);
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}

	public void test06CdTwoUp() {

		FTPClient ftp = setupFTPClient();
		
		try {
			
			assertEmptyDirectory(ftp);
			
			String name1 = "/FTPdir1";
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name1);
			
			ftp.changeWorkingDirectory(name1);
			
			String name2 = name1.concat("/").concat("FTPdir2");
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name2);
			
			ftp.changeWorkingDirectory(name2);
			
			String name3 = name2.concat("/").concat("FTPdir3");
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name3);
			
			ftp.changeWorkingDirectory(name3);
			
			ftp.changeToParentDirectory();
			ftp.changeToParentDirectory();
			
			String newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name1, newWorkingDirectory);
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}

	public void test07CdToSiblingDirectory() {

		FTPClient ftp = setupFTPClient();
		
		try {
			
			FTPFile[] dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(0, dirs.length);
			
			String name1 = "/FTPdir1";
			String name2 = "/FTPdir2";
			
			// Create folders by mkdir FTP command
			ftp.makeDirectory(name1);
			ftp.makeDirectory(name2);
			
			ftp.changeWorkingDirectory(name1);

			String newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name1, newWorkingDirectory);
			
			ftp.changeWorkingDirectory("../" + name2);
			
			newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name2, newWorkingDirectory);
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error while changing FTP directories", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}
	
	public void test08CdRoot() {

		FTPClient ftp = setupFTPClient();
		
		try {
			
			FTPFile[] dirs = ftp.listDirectories();
			
			assertNotNull(dirs);
			assertEquals(0, dirs.length);
			
			String name1 = "/FTPdir1";
			
			// Create folder by mkdir FTP command
			ftp.makeDirectory(name1);
			
			ftp.changeWorkingDirectory(name1);
			
			assertEmptyDirectory(ftp);
			
			String newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals(name1, newWorkingDirectory);
			
			ftp.changeWorkingDirectory("/");
			
			newWorkingDirectory = ftp.printWorkingDirectory();
			assertEquals("/", newWorkingDirectory);

			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error while changing FTP directories", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
	}
	
}
