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
package org.structr.core.converter;

import org.structr.core.Converter;
import org.structr.core.Value;

/**
 * Converts an Integer to a String and back.
 *
 * @author Christian Morgner
 */
public class StringToInt extends Converter<String, Integer> {
	
	public StringToInt(Value<String> source) {
		
		super(source, new PropertyConverter<String, Integer>(null, null) {

			@Override
			public String revert(Integer source) {
				
				if (source != null) {
					
					return source.toString();
				}
				
				return null;
			}

			@Override
			public Integer convert(String source) {
				
				if (source != null) {
					
					return Integer.parseInt(source);
				}
				
				return null;
			}
			
		});
	}
}
