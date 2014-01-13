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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.structr.core.entity.AbstractNode;

/**
 *
 * @author Christian Morgner
 */
public class TypeSearchAttribute extends PropertySearchAttribute<String> {

	public TypeSearchAttribute(Class type, Occur occur, boolean isExactMatch) {
		super(AbstractNode.type, type.getSimpleName(), occur, isExactMatch);
	}
	
	@Override
	public Query getQuery() {

		String value = getStringValue();
		if (isExactMatch()) {
			
			return new TermQuery(new Term(getKey().dbName(), value));
			
		} else {
			
			return new TermQuery(new Term(getKey().dbName(), value.toLowerCase()));
		}
	}
	
}
