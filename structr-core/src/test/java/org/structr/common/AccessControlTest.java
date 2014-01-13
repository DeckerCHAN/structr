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
/*
*  Copyright (C) 2010-2013 Axel Morgner
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



package org.structr.common;

import org.structr.common.error.FrameworkException;
import org.structr.core.Result;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.TestOne;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchAttribute;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.structr.core.Services;
import org.structr.core.entity.Person;
import org.structr.core.entity.Principal;
import org.structr.core.entity.User;
import org.structr.core.graph.CreateNodeCommand;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.search.SearchNodeCommand;
import org.structr.core.property.PropertyKey;
import org.structr.core.property.PropertyMap;

//~--- classes ----------------------------------------------------------------

/**
 * Test access control with different permission levels.
 *
 * @author Axel Morgner
 */
public class AccessControlTest extends StructrTest {

	private static final Logger logger = Logger.getLogger(AccessControlTest.class.getName());
	
	//~--- methods --------------------------------------------------------

	@Override
	public void test00DbAvailable() {

		super.test00DbAvailable();

	}

	public void test01PublicAccessToNonPublicNode() {

		try {

			List<AbstractNode> users = createTestNodes(User.class, 1);
			User user = (User) users.get(0);
			
			// Create node with user context
			Class type = TestOne.class;
			TestOne t1 = createTestNode(TestOne.class, user);
			
			SecurityContext publicContext = SecurityContext.getInstance(null, AccessMode.Frontend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(publicContext, SearchNodeCommand.class).execute(searchAttributes);

			// Node should not be visible in public context (no user logged in)
			assertTrue(result.isEmpty());
			
		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	
	public void test02PublicAccessToPublicNode() {

		try {

			List<AbstractNode> users = createTestNodes(User.class, 1);
			User user = (User) users.get(0);
			
			PropertyMap props = new PropertyMap();
			props.put(AbstractNode.visibleToPublicUsers, true);
			
			// Create two nodes with user context, one of them is visible to public users
			Class type = TestOne.class;
			TestOne t1 = createTestNode(TestOne.class, props, user);
			TestOne t2 = createTestNode(TestOne.class, user);
			
			SecurityContext publicContext = SecurityContext.getInstance(null, AccessMode.Frontend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(publicContext, SearchNodeCommand.class).execute(searchAttributes);
			
			assertEquals(1, result.size());
			assertEquals(t1.getUuid(), result.get(0).getUuid());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test03PublicAccessToProtectedNode() {

		try {

			List<AbstractNode> users = createTestNodes(User.class, 1);
			User user = (User) users.get(0);
			
			PropertyMap props = new PropertyMap();
			props.put(AbstractNode.visibleToPublicUsers, true);
			
			// Create two nodes with user context, one of them is visible to public users
			Class type = TestOne.class;
			TestOne t1 = createTestNode(TestOne.class, props, user);
			
			props = new PropertyMap();
			props.put(AbstractNode.visibleToAuthenticatedUsers, true);
			
			TestOne t2 = createTestNode(TestOne.class, props, user);
			
			SecurityContext publicContext = SecurityContext.getInstance(null, AccessMode.Frontend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(publicContext, SearchNodeCommand.class).execute(searchAttributes);
			
			assertEquals(1, result.size());
			assertEquals(t1.getUuid(), result.get(0).getUuid());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test04BackendUserAccessToProtectedNode() {

		try {

			List<AbstractNode> users = createTestNodes(User.class, 2);
			User user1 = (User) users.get(0);
			User user2 = (User) users.get(1);

			PropertyMap props = new PropertyMap();
			props.put(AbstractNode.visibleToPublicUsers, true);
			
			// Create two nodes with user context, one of them is visible to public users
			Class type = TestOne.class;
			TestOne t1 = createTestNode(TestOne.class, props, user1);
			
			props = new PropertyMap();
			props.put(AbstractNode.visibleToAuthenticatedUsers, true);
			
			TestOne t2 = createTestNode(TestOne.class, props, user1);
			
			// Let another user search
			SecurityContext user2Context = SecurityContext.getInstance(user2, AccessMode.Backend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(user2Context, SearchNodeCommand.class).execute(searchAttributes);
			
			assertEquals(2, result.size());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test05FrontendUserAccessToProtectedNode() {

		try {

			List<AbstractNode> users = createTestNodes(User.class, 2);
			User user1 = (User) users.get(0);
			User user2 = (User) users.get(1);
			
			PropertyMap props = new PropertyMap();
			props.put(AbstractNode.visibleToPublicUsers, true);
			
			// Create two nodes with user context, one of them is visible to public users
			Class type = TestOne.class;
			TestOne t1 = createTestNode(TestOne.class, props, user1);
			
			props = new PropertyMap();
			props.put(AbstractNode.visibleToAuthenticatedUsers, true);
			
			TestOne t2 = createTestNode(TestOne.class, props, user1);
			
			// Let another user search
			SecurityContext user2Context = SecurityContext.getInstance(user2, AccessMode.Frontend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(user2Context, SearchNodeCommand.class).execute(searchAttributes);
			
			assertEquals(2, result.size());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}
		
	}

	public void test06GrantReadPermission() {

		try {

			List<AbstractNode> users = createTestNodes(User.class, 2);
			User user1 = (User) users.get(0);
			final User user2 = (User) users.get(1);
			
			// Let user 1 create a node
			Class type = TestOne.class;
			final TestOne t1 = createTestNode(TestOne.class, user1);

			transactionCommand.execute(new StructrTransaction<Object>() {
				@Override
				public Object execute() throws FrameworkException {

					// Grant read permission to user 2
					user2.grant(Permission.read, t1);
					return null;
				}
			});
			
			// Let user 2 search
			SecurityContext user2Context = SecurityContext.getInstance(user2, AccessMode.Backend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(user2Context, SearchNodeCommand.class).execute(searchAttributes);
			
			assertEquals(1, result.size());
			assertEquals(t1.getUuid(), result.get(0).getUuid());

			transactionCommand.execute(new StructrTransaction<Object>() {
				@Override
				public Object execute() throws FrameworkException {

					// Revoke permission again
					user2.revoke(Permission.read, t1);
					return null;
				}
			});
			
			result = Services.command(user2Context, SearchNodeCommand.class).execute(searchAttributes);
			
			assertTrue(result.isEmpty());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test07ResultCount() {

		try {

			List<AbstractNode> persons = createTestNodes(Person.class, 1);
			Person user = (Person) persons.get(0);
			
			Class type = TestOne.class;
			
			final List<AbstractNode> nodes = createTestNodes(type, 10);
			
			transactionCommand.execute(new StructrTransaction<Object>() {
				@Override
				public Object execute() throws FrameworkException {

					nodes.get(3).setProperty(AbstractNode.visibleToPublicUsers, true);
					nodes.get(5).setProperty(AbstractNode.visibleToPublicUsers, true);
					nodes.get(7).setProperty(AbstractNode.visibleToPublicUsers, true);
					
					return null;
				}
			});

			SecurityContext publicContext = SecurityContext.getInstance(null, AccessMode.Frontend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = Services.command(publicContext, SearchNodeCommand.class).execute(searchAttributes);
			
			assertEquals(3, result.size());
			assertEquals(3, (int) result.getRawResultCount());

			assertEquals(nodes.get(3).getUuid(), result.get(0).getUuid());
			assertEquals(nodes.get(5).getUuid(), result.get(1).getUuid());
			assertEquals(nodes.get(7).getUuid(), result.get(2).getUuid());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}
	
	public void test07ResultCountWithPaging() {

		try {

			List<AbstractNode> persons = createTestNodes(Person.class, 1);
			Person user = (Person) persons.get(0);
			
			Class type = TestOne.class;
			
			final List<AbstractNode> nodes = createTestNodes(type, 10);
			
			transactionCommand.execute(new StructrTransaction<Object>() {
				@Override
				public Object execute() throws FrameworkException {

					nodes.get(3).setProperty(AbstractNode.visibleToPublicUsers, true);
					nodes.get(5).setProperty(AbstractNode.visibleToPublicUsers, true);
					nodes.get(7).setProperty(AbstractNode.visibleToPublicUsers, true);
					nodes.get(9).setProperty(AbstractNode.visibleToPublicUsers, true);
					
					return null;
				}
			});

			SecurityContext publicContext = SecurityContext.getInstance(null, AccessMode.Frontend);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			PropertyKey sortKey = AbstractNode.name;
			boolean sortDesc    = false;
			int pageSize        = 2;
			int page            = 1;
			
			Result result = Services.command(publicContext, SearchNodeCommand.class).execute(false, false, searchAttributes, sortKey, sortDesc, pageSize, page);
			
			assertEquals(2, result.size());
			assertEquals(4, (int) result.getRawResultCount());

			assertEquals(nodes.get(3).getUuid(), result.get(0).getUuid());
			assertEquals(nodes.get(5).getUuid(), result.get(1).getUuid());

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	protected <T extends AbstractNode> T createTestNode(final Class<T> type, final Principal user) throws FrameworkException {
		return (T)createTestNode(type, new PropertyMap(), user);
	}

	protected <T extends AbstractNode> T createTestNode(final Class<T> type, final PropertyMap props, final Principal user) throws FrameworkException {

		SecurityContext context = SecurityContext.getInstance(user, AccessMode.Backend);
		final CreateNodeCommand create = Services.command(context, CreateNodeCommand.class);			
		
		props.put(AbstractNode.type, type.getSimpleName());

		return transactionCommand.execute(new StructrTransaction<T>() {

			@Override
			public T execute() throws FrameworkException {

				return (T)create.execute(props);

			}

		});

	}
	
	
}
