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

package org.structr.web.entity.html;

import org.structr.common.PropertyView;
import org.structr.core.property.Property;
import org.structr.web.common.HtmlProperty;
import org.structr.web.entity.dom.DOMElement;

/**
 * @author Axel Morgner
 */
public class Audio extends DOMElement {

	public static final Property<String> _src		= new HtmlProperty("src");
	public static final Property<String> _crossorigin	= new HtmlProperty("crossorigin");
	public static final Property<String> _preload		= new HtmlProperty("preload");
	public static final Property<String> _autoplay		= new HtmlProperty("autoplay");
	public static final Property<String> _mediagroup		= new HtmlProperty("mediagroup");
	public static final Property<String> _loop		= new HtmlProperty("loop");
	public static final Property<String> _muted		= new HtmlProperty("muted");
	public static final Property<String> _controls		= new HtmlProperty("controls");

	public static final org.structr.common.View htmlView	= new org.structr.common.View(Audio.class, PropertyView.Html,
		_src, _crossorigin, _preload, _autoplay, _mediagroup, _loop, _muted, _controls
	);

}
