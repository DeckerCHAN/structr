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
package org.structr.core.entity;

import java.util.Date;
import org.structr.core.property.Property;
import org.structr.common.PropertyView;
import org.structr.common.RelType;
import org.structr.common.View;
import org.structr.core.property.DoubleProperty;
import org.structr.core.property.ISO8601DateProperty;
import org.structr.core.property.IntProperty;
import org.structr.core.property.LongProperty;
import org.structr.core.property.EntityProperty;
import org.structr.core.property.StringProperty;

/**
 * A simple entity for the most basic tests.
 * 
 * 
 * @author Axel Morgner
 */
public class TestOne extends AbstractNode {
	
	public static final Property<Integer> anInt	= new IntProperty("anInt").indexed().indexedWhenEmpty();
	public static final Property<Double> aDouble	= new DoubleProperty("aDouble").indexed().indexedWhenEmpty();
	public static final Property<Long> aLong	= new LongProperty("aLong").indexed().indexedWhenEmpty();
	public static final Property<Date> aDate	= new ISO8601DateProperty("aDate").indexed().indexedWhenEmpty();
	public static final Property<String>  aString	= new StringProperty("aString").indexed().indexedWhenEmpty();
	
	public static final EntityProperty<TestTwo>   testTwo	= new EntityProperty<TestTwo>("testTwo", TestTwo.class, RelType.IS_AT, false, Relation.DELETE_IF_CONSTRAINT_WOULD_BE_VIOLATED);
	public static final EntityProperty<TestThree> testThree	= new EntityProperty<TestThree>("testThree", TestThree.class, RelType.OWNS, false, Relation.DELETE_IF_CONSTRAINT_WOULD_BE_VIOLATED);
	public static final EntityProperty<TestFour>  testFour	= new EntityProperty<TestFour>("testFour", TestFour.class, RelType.IS_AT, false, Relation.DELETE_IF_CONSTRAINT_WOULD_BE_VIOLATED);

	public static final View publicView = new View(TestOne.class, PropertyView.Public,
		name, anInt, aDouble, aLong, aDate, createdDate, aString
	);
}
