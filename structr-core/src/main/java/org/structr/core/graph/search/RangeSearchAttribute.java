/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause.Occur;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.neo4j.index.impl.lucene.LuceneUtil;
import org.structr.core.GraphObject;

import org.structr.core.property.PropertyKey;

/**
 * Search attribute for range queries
 *
 * @author Christian Morgner
 * @author Axel Morgner
 */
public class RangeSearchAttribute<T> extends SearchAttribute {
	
	private static final Logger logger = Logger.getLogger(RangeSearchAttribute.class.getName());

	private PropertyKey<T> searchKey = null;
	private T rangeStart = null;
	private T rangeEnd = null;

	public RangeSearchAttribute(final PropertyKey<T> searchKey, final T rangeStart, final T rangeEnd, final Occur occur) {

		super(occur);

		this.searchKey  = searchKey;
		this.rangeStart = rangeStart;
		this.rangeEnd   = rangeEnd;
	}

	@Override
	public Query getQuery() {

		Query q;
		
		if (rangeStart == null && rangeEnd == null) {
			return null;
		}
		
		if ((rangeStart != null && rangeStart instanceof Date) || (rangeEnd != null && rangeEnd instanceof Date)) {
			
			q = LuceneUtil.rangeQuery(searchKey.dbName(), rangeStart == null ? null : ((Date) rangeStart).getTime(), rangeEnd == null ? null : ((Date) rangeEnd).getTime(), true, true);
			
		} else if ((rangeStart != null && rangeStart instanceof Number) || (rangeEnd != null && rangeEnd instanceof Number)) {
			
			q = LuceneUtil.rangeQuery(searchKey.dbName(), rangeStart == null ? null : (Number) rangeStart, rangeEnd == null ? null : (Number) rangeEnd, true, true);
			
		} else {
			
			q = new TermRangeQuery(searchKey.dbName(), rangeStart == null ? null : rangeStart.toString(), rangeEnd == null ? null : rangeEnd.toString(), true, true);
			
		}
		
		logger.log(Level.FINE, "Range query: {0}", q);
		
		return q;
	}
	
	@Override
	public PropertyKey getKey() {
		return searchKey;
	}

	@Override
	public String getValue() {
		
		Query query = getQuery();
		if (query != null) {
			
			return getQuery().toString();
		}
		
		return "";
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
		
		T nodeValue          = (T) entity.getProperty(getKey());
		Occur occur          = getOccur();
		
		if (nodeValue == null) {
			
			return occur.equals(Occur.MUST_NOT);
			
		}
		
		boolean isInRange = (compare(nodeValue, rangeStart) >= 0 && compare(nodeValue, rangeEnd) <= 0);
		
		return isInRange && !occur.equals(Occur.MUST_NOT);
	}

	private int compare(T nodeValue, T searchValue) {
		
		if (nodeValue instanceof Comparable && searchValue instanceof Comparable) {
			
			Comparable n = (Comparable)nodeValue;
			Comparable s = (Comparable)searchValue;
			
			return n.compareTo(s);
		}
		
		return 0;
	}

	@Override
	public String getValueForEmptyField() {
		return null;
	}
}
