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

import org.apache.ftpserver.ftplet.Authentication;

/**
 *
 * @author Axel Morgner
 */
public class StructrAuthentication implements Authentication {
	
    private final String username;
    private final String password;


    /**
     * Constructor with the minimal data for an authentication
     * 
     * @param username
     * @param password
     */
    public StructrAuthentication(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }

}
