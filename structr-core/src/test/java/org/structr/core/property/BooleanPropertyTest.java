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
package org.structr.core.property;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import org.structr.common.RelType;
import org.structr.common.StructrTest;
import org.structr.common.error.FrameworkException;
import org.structr.core.EntityContext;
import org.structr.core.Result;
import org.structr.core.Services;
import org.structr.core.entity.TestFour;
import org.structr.core.entity.TestOne;
import org.structr.core.entity.TestRelationship;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.TransactionCommand;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchNodeCommand;
import org.structr.core.graph.search.SearchRelationshipCommand;

/**
 *
 * @author Christian Morgner
 */
public class BooleanPropertyTest extends StructrTest {
	
	public void test() {
		
		try {

			final Property<Boolean> key = TestFour.booleanProperty;
			final TestFour testEntity        = createTestNode(TestFour.class);
			
			assertNotNull(testEntity);

			// store boolean in the test entitiy
			final Boolean value = Boolean.TRUE;

			Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
					
					testEntity.setProperty(key, value);
					return null;
				}
			});

			// check value from database
			assertEquals(value, testEntity.getProperty(key));
			
		} catch (FrameworkException fex) {
			
			fail("Unable to store array");
		}
	}
	
	public void testSimpleSearchOnNode() {
		
		try {
			final PropertyMap properties  = new PropertyMap();
			final PropertyKey<Boolean> key = TestFour.booleanProperty;
			
			properties.put(key, true);
			
			final TestFour testEntity     = createTestNode(TestFour.class, properties);
			
			assertNotNull(testEntity);

			// check value from database
			assertEquals((Boolean)true, (Boolean)testEntity.getProperty(key));
			
			
			Result<TestFour> result = Services.command(securityContext, SearchNodeCommand.class).execute(
				Search.andExactType(TestFour.class),
				Search.andExactProperty(securityContext, key, true)
			);
			
			assertEquals(result.size(), 1);
			assertEquals(result.get(0), testEntity);
		
		} catch (FrameworkException fex) {
			
			fail("Unable to store array");
		}
		
	}
	
	public void testSimpleSearchOnRelationship() {
		
		try {
			final TestOne testOne        = createTestNode(TestOne.class);
			final TestFour testFour      = createTestNode(TestFour.class);
			final Property<Boolean> key = TestRelationship.booleanProperty;
			
			assertNotNull(testOne);
			assertNotNull(testFour);
			
			final TestRelationship testEntity = (TestRelationship)createTestRelationship(testOne, testFour, RelType.IS_AT);
			
			assertNotNull(testEntity);

			Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
					
					// set property
					testEntity.setProperty(key, true);
					
					return null;
				}
				
			});
			
			// check value from database
			assertEquals((Boolean)true, (Boolean)testEntity.getProperty(key));
			
			Result<TestFour> result = Services.command(securityContext, SearchRelationshipCommand.class).execute(
				Search.andExactRelType(EntityContext.getNamedRelation(TestRelationship.Relation.test_relationships.name())),
				Search.andExactProperty(securityContext, key, true)
			);
			
			assertEquals(result.size(), 1);
			assertEquals(result.get(0), testEntity);
		
		} catch (FrameworkException fex) {
			
			fail("Unable to store array");
		}
	}
}
