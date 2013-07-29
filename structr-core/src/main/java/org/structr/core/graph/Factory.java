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
package org.structr.core.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.gis.spatial.indexprovider.SpatialRecordHits;
import org.neo4j.graphdb.index.IndexHits;
import org.structr.common.FactoryDefinition;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.common.error.IdNotFoundToken;
import org.structr.core.Adapter;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;
import org.structr.core.Result;

/**
 *
 * @author Christian Morgner
 */

public abstract class Factory<S, T extends GraphObject> implements Adapter<S, T> {

	private static final Logger logger = Logger.getLogger(Factory.class.getName());
	
	public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
	public static final int DEFAULT_PAGE      = 1;
	
	// encapsulates all criteria for node creation
	protected FactoryDefinition factoryDefinition = EntityContext.getFactoryDefinition();
	protected FactoryProfile factoryProfile       = null;
	
	public Factory(final SecurityContext securityContext) {

		factoryProfile = new FactoryProfile(securityContext);
	}

	public Factory(final SecurityContext securityContext, final boolean includeDeletedAndHidden, final boolean publicOnly) {

		factoryProfile = new FactoryProfile(securityContext, includeDeletedAndHidden, publicOnly);
	}

	public Factory(final SecurityContext securityContext, final int pageSize, final int page, final String offsetId) {

		factoryProfile = new FactoryProfile(securityContext);

		factoryProfile.setPageSize(pageSize);
		factoryProfile.setPage(page);
		factoryProfile.setOffsetId(offsetId);
	}

	public Factory(final SecurityContext securityContext, final boolean includeDeletedAndHidden, final boolean publicOnly, final int pageSize, final int page, final String offsetId) {
		factoryProfile = new FactoryProfile(securityContext, includeDeletedAndHidden, publicOnly, pageSize, page, offsetId);
	}
	
	public abstract T instantiate(final S obj) throws FrameworkException;
	
	public abstract T instantiateWithType(final S obj, final String nodeType, boolean isCreation) throws FrameworkException;
	
	public abstract T instantiate(final S obj, final boolean includeDeletedAndHidden, final boolean publicOnly) throws FrameworkException;
	

	/**
	 * Create structr nodes from all given underlying database nodes
	 * No paging, but security check
	 *
	 * @param securityContext
	 * @param input
	 * @return
	 */
	public Result instantiateAll(final Iterable<S> input) throws FrameworkException {

		List<T> objects = bulkInstantiate(input);

		return new Result(objects, objects.size(), true, false);
	}

	/**
	 * Create structr nodes from the underlying database nodes
	 *
	 * Include only nodes which are readable in the given security context.
	 * If includeDeletedAndHidden is true, include nodes with 'deleted' flag
	 * If publicOnly is true, filter by 'visibleToPublicUsers' flag
	 *
	 * @param input
	 * @return
	 */
	public Result instantiate(final IndexHits<S> input) throws FrameworkException {

		if (input != null) {

			if (factoryProfile.getOffsetId() != null) {

				return resultWithOffsetId(input);

			} else {

				return resultWithoutOffsetId(input);
			}

		}

		return Result.EMPTY_RESULT;

	}
	
	/**
	 * Create structr nodes from all given underlying database nodes
	 * No paging, but security check
	 *
	 * @param securityContext
	 * @param input
	 * @return
	 */
	public List<T> bulkInstantiate(final Iterable<S> input) throws FrameworkException {

		List<T> nodes = new LinkedList<T>();

		if ((input != null) && input.iterator().hasNext()) {

			for (S node : input) {

				T n = instantiate(node);
				if (n != null) {

					nodes.add(n);
				}
			}
		}

		return nodes;
	}

	@Override
	public T adapt(S s) {

		try {
			return instantiate(s);
			
		} catch (FrameworkException fex) {
			
			logger.log(Level.WARNING, "Unable to adapt", fex);
		}

		return null;
	}


	// <editor-fold defaultstate="collapsed" desc="private methods">
	protected List<S> read(final Iterable<S> it) {

		List<S> nodes = new LinkedList();

		while (it.iterator().hasNext()) {

			nodes.add(it.iterator().next());
		}

		return nodes;

	}

	protected Result resultWithOffsetId(final IndexHits<S> input) throws FrameworkException {

		int size                 = input.size();
		final int pageSize       = Math.min(size, factoryProfile.getPageSize());
		final int page           = factoryProfile.getPage();
		final String offsetId    = factoryProfile.getOffsetId();
		List<T> allObjects       = this.bulkInstantiate(input);
		
		List<T> result           = new LinkedList<T>();
		
		int count                = 0;
		int offset               = 0;

		// We have an offsetId, so first we need to
		// find the object with this uuid to get the offset
		List<T> objectsUpToOffset = new LinkedList();
		int i                   = 0;
		boolean gotOffset        = false;

		for (T obj : allObjects) {
				
			objectsUpToOffset.add(obj);

			if (!gotOffset) {

				if (!offsetId.equals(obj.getUuid())) {

					i++;

					continue;

				}

				gotOffset = true;
				offset    = page > 0
					    ? i
					    : i + (page * pageSize);

				break;

			}

		}

		if (!gotOffset) {

			throw new FrameworkException("offsetId", new IdNotFoundToken(offsetId));
		}
		
		int position = 0;
		
		if (offset < 0) {
			
			// Remove last item
			objectsUpToOffset.remove(objectsUpToOffset.size()-1);
			
			return new Result(objectsUpToOffset, allObjects.size(), true, false);
			
		}

		for (T obj : allObjects) {
			
			if (++position <= offset) {
				
				continue;
				
			}

			// stop if we got enough objects
			if (++count > pageSize) {

				return new Result(result, allObjects.size(), true, false);
			}

			result.add(obj);

		}

		return new Result(result, size, true, false);

	}

	protected Result resultWithoutOffsetId(final IndexHits<S> input) throws FrameworkException {

		final int pageSize = factoryProfile.getPageSize();
		final int page     = factoryProfile.getPage();
		int fromIndex;

		if (page < 0) {

			List<S> rawNodes = read(input);
			int size         = rawNodes.size();

			fromIndex = Math.max(0, size + (page * pageSize));

			final List<T> nodes = new LinkedList<T>();
			int toIndex         = Math.min(size, fromIndex + pageSize);

			for (S n : rawNodes.subList(fromIndex, toIndex)) {

				nodes.add(instantiate(n));
			}

			// We've run completely through the iterator,
			// so the overall count from here is accurate.
			return new Result(nodes, size, true, false);

		} else {

			int size = input.size();

			fromIndex = pageSize == Integer.MAX_VALUE ? 0 : (page - 1) * pageSize;

			// The overall count may be inaccurate here, but
			// the page() method counts correctly now.
			return page(input, size, fromIndex, pageSize);
		}

	}

	protected Result page(final IndexHits<S> input, final int overallResultCount, final int offset, final int pageSize) throws FrameworkException {

		final List<T> nodes = new LinkedList<T>();
		int position        = 0;
		int count           = 0;
		int overallCount    = 0;
		
		Result result       = null;

		for (S node : input) {

			T n = instantiate(node);

			if (n != null) {

				overallCount++;

				if (++position > offset) {

					// stop to add to the list if we got enough nodes,
					// but continue to count
					if (++count <= pageSize) {

						nodes.add(n);
					}
					
				}

			}

		}

		// We've run completely through the iterator,
		// so the overall count from here is accurate.
		result = new Result(nodes, overallCount, true, false);
		
		return result;

	}

	//~--- inner classes --------------------------------------------------

	protected class FactoryProfile {

		private boolean includeDeletedAndHidden = true;
		private String offsetId                 = null;
		private boolean publicOnly              = false;
		private int pageSize                    = DEFAULT_PAGE_SIZE;
		private int page                        = DEFAULT_PAGE;
		private SecurityContext securityContext = null;

		//~--- constructors -------------------------------------------

		public FactoryProfile(final SecurityContext securityContext) {

			this.securityContext = securityContext;

		}

		public FactoryProfile(final SecurityContext securityContext, final boolean includeDeletedAndHidden, final boolean publicOnly) {

			this.securityContext         = securityContext;
			this.includeDeletedAndHidden = includeDeletedAndHidden;
			this.publicOnly              = publicOnly;

		}

		public FactoryProfile(final SecurityContext securityContext, final boolean includeDeletedAndHidden, final boolean publicOnly, final int pageSize, final int page,
				      final String offsetId) {

			this.securityContext         = securityContext;
			this.includeDeletedAndHidden = includeDeletedAndHidden;
			this.publicOnly              = publicOnly;
			this.pageSize                = pageSize;
			this.page                    = page;
			this.offsetId                = offsetId;

		}

		//~--- methods ------------------------------------------------

		/**
		 * @return the includeDeletedAndHidden
		 */
		public boolean includeDeletedAndHidden() {

			return includeDeletedAndHidden;

		}

		/**
		 * @return the publicOnly
		 */
		public boolean publicOnly() {

			return publicOnly;

		}

		//~--- get methods --------------------------------------------

		/**
		 * @return the offsetId
		 */
		public String getOffsetId() {

			return offsetId;

		}

		/**
		 * @return the pageSize
		 */
		public int getPageSize() {

			return pageSize;

		}

		/**
		 * @return the page
		 */
		public int getPage() {

			return page;

		}

		/**
		 * @return the securityContext
		 */
		public SecurityContext getSecurityContext() {

			return securityContext;

		}

		//~--- set methods --------------------------------------------

		/**
		 * @param includeDeletedAndHidden the includeDeletedAndHidden to set
		 */
		public void setIncludeDeletedAndHidden(boolean includeDeletedAndHidden) {

			this.includeDeletedAndHidden = includeDeletedAndHidden;

		}

		/**
		 * @param offsetId the offsetId to set
		 */
		public void setOffsetId(String offsetId) {

			this.offsetId = offsetId;

		}

		/**
		 * @param publicOnly the publicOnly to set
		 */
		public void setPublicOnly(boolean publicOnly) {

			this.publicOnly = publicOnly;

		}

		/**
		 * @param pageSize the pageSize to set
		 */
		public void setPageSize(int pageSize) {

			this.pageSize = pageSize;

		}

		/**
		 * @param page the page to set
		 */
		public void setPage(int page) {

			this.page = page;

		}

		/**
		 * @param securityContext the securityContext to set
		 */
		public void setSecurityContext(SecurityContext securityContext) {

			this.securityContext = securityContext;

		}

	}

	// </editor-fold>

}
