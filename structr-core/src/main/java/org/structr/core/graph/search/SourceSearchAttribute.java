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
package org.structr.core.graph.search;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.structr.core.GraphObject;

/**
 *
 * @author Christian Morgner
 */
public class SourceSearchAttribute<T> extends SearchAttribute<T> {

	public SourceSearchAttribute(Occur occur) {
		super(occur);
	}
	
	@Override
	public Query getQuery() {
		return null;
	}

	@Override
	public boolean isExactMatch() {
		return true;
	}

	@Override
	public String getStringValue() {
		return null;
	}

	@Override
	public String getInexactValue() {
		return null;
	}

	@Override
	public boolean includeInResult(GraphObject entity) {
		return true;
	}

	@Override
	public String getValueForEmptyField() {
		return null;
	}
}
