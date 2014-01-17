/*
 *  Copyright (C) 2013 Axel Morgner
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.structr.core.entity;

import org.structr.common.PropertyView;
import org.structr.common.View;
import static org.structr.core.entity.ResourceAccess.flags;
import static org.structr.core.entity.ResourceAccess.position;
import static org.structr.core.entity.ResourceAccess.signature;

/**
 *
 * @author Axel Morgner
 */
public class DynamicResourceAccess extends ResourceAccess {

	public static final View uiView = new View(ResourceAccess.class, PropertyView.Ui,
		signature, flags, position
	);
	
	public static final View publicView = new View(ResourceAccess.class, PropertyView.Public,
		signature, flags
	);

}