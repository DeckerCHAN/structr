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

package org.structr.web.entity;

import org.neo4j.graphdb.Direction;
import org.structr.common.View;
import org.structr.common.PropertyView;
import org.structr.common.error.ErrorBuffer;
import org.structr.core.entity.AbstractNode;
import org.structr.core.notion.PropertySetNotion;
import org.structr.core.property.CollectionProperty;
import org.structr.core.property.EntityProperty;
import org.structr.core.property.IntProperty;
import org.structr.core.property.Property;
import org.structr.web.common.RelType;


//~--- classes ----------------------------------------------------------------

/**
 * The Folder entity.
 *
 * @author Axel Morgner
 *
 */
public class Folder extends AbstractFile {

	public static final CollectionProperty<Folder>	folders      = new CollectionProperty<>("folders", Folder.class, RelType.CONTAINS, Direction.OUTGOING, new PropertySetNotion(uuid, name), true);
	public static final CollectionProperty<File>	files        = new CollectionProperty<>("files", File.class, RelType.CONTAINS, Direction.OUTGOING, new PropertySetNotion(uuid, name), true);
	public static final CollectionProperty<Image>	images       = new CollectionProperty<>("images", Image.class, RelType.CONTAINS, Direction.OUTGOING, new PropertySetNotion(uuid, name), true);
	
	public static final Property<Integer>		position     = new IntProperty("position").indexed();

	public static final View defaultView = new View(Folder.class, PropertyView.Public, uuid, type, name);
	
	public static final View uiView = new View(Folder.class, PropertyView.Ui,
		parent, folders, files, images
	);
	
	@Override
	public boolean isValid(ErrorBuffer errorBuffer) {
		
		boolean valid = true;
		
		valid &= nonEmpty(AbstractNode.name, errorBuffer);
		valid &= super.isValid(errorBuffer);
		
		return valid;
	}
}
