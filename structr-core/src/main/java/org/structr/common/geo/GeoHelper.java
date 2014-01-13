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
package org.structr.common.geo;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Location;
import org.structr.core.graph.CreateNodeCommand;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.TransactionCommand;
import org.structr.core.property.PropertyMap;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Logger;
import org.apache.commons.collections.map.LRUMap;
import org.structr.core.graph.search.DistanceSearchAttribute;

//~--- classes ----------------------------------------------------------------

/**
 * Helper class to create location nodes from coordinates or by using
 * online geocoding service.
 *
 * @author Axel Morgner
 */
public class GeoHelper {

	private static final Logger logger                    = Logger.getLogger(GeoHelper.class.getName());
	
	private static Map<String, GeoCodingResult> geoCache  = Collections.synchronizedMap(new LRUMap(10000));
	private static Class<GeoCodingProvider> providerClass = null;
	private static GeoCodingProvider providerInstance     = null;

	/**
	 * Creates a Location entity for the given geocoding result and returns it.
	 * 
	 * @param coords
	 * @return a Location entity for the given geocoding result
	 * 
	 * @throws FrameworkException 
	 */
	public static Location createLocation(final GeoCodingResult coords) throws FrameworkException {

		final PropertyMap props = new PropertyMap();
		double latitude         = coords.getLatitude();
		double longitude        = coords.getLongitude();
		String type             = Location.class.getSimpleName();

		props.put(AbstractNode.type,  type);
		props.put(Location.latitude,  latitude);
		props.put(Location.longitude, longitude);

		StructrTransaction transaction = new StructrTransaction<AbstractNode>() {

			@Override
			public AbstractNode execute() throws FrameworkException {
				return Services.command(SecurityContext.getSuperUserInstance(), CreateNodeCommand.class).execute(props);
			}
		};

		return (Location) Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(transaction);
	}

	public static GeoCodingResult geocode(DistanceSearchAttribute distanceSearch) throws FrameworkException {
		
		String street     = distanceSearch.getStreet();
		String house      = distanceSearch.getHouse();
		String postalCode = distanceSearch.getPostalCode();
		String city       = distanceSearch.getCity();
		String state      = distanceSearch.getState();
		String country    = distanceSearch.getCountry();
		
		return geocode(street, house, postalCode, city, state, country);
	}
	
	/**
	 * Tries do find a geo location for the given address using the GeoCodingProvider
	 * specified in the configuration file. 

	 * @param country the country to search for, may be null
	 * @param state the state to search for, may be null
	 * @param city the city to search for, may be null
	 * @param street the street to search for, may be null
	 * @param house the house to search for, may be null
	 *
	 * @return the geolocation of the given address, or null
	 * 
	 * @throws FrameworkException 
	 */
	public static GeoCodingResult geocode(final String street, final String house, String postalCode, final String city, final String state, final String country) throws FrameworkException {
		
		String language        = Services.getConfigurationValue(Services.GEOCODING_LANGUAGE, "de");
		String cacheKey        = cacheKey(street, house, postalCode, city, state, country, language);
		GeoCodingResult result = geoCache.get(cacheKey);
		
		if (result == null) {

			GeoCodingProvider provider = getGeoCodingProvider();
			if (provider != null) {

				result = provider.geocode(street, house, postalCode, city, state, country, language);
				if (result != null) {
					
					// store in cache
					geoCache.put(cacheKey, result);
					
				} else {
					
					geoCache.put(cacheKey, new NullResult());
				}
			}
			
		}

		// do not try to geocode failed results again
		if (result instanceof NullResult) {
			return null;
		}
		
		return result;
	}
	
	private static String cacheKey(final String street, final String house, String postalCode, final String city, final String state, final String country, final String language) {
		
		StringBuilder keyBuffer = new StringBuilder();

		if (street != null) {
			keyBuffer.append(street);
		}
		
		if (house != null) {
			keyBuffer.append(house);
		}
		
		if (postalCode != null) {
			keyBuffer.append(postalCode);
		}
		
		if (city != null) {
			keyBuffer.append(city);
		}
		
		if (state != null) {
			keyBuffer.append(state);
		}
		
		if (country != null) {
			keyBuffer.append(country);
		}
		
		if (language !=  null) {
			keyBuffer.append(language);
		}
		
		return keyBuffer.toString();
	}
	
	private static GeoCodingProvider getGeoCodingProvider() {

		if (providerInstance == null) {

			try {

				if (providerClass == null) {

					String geocodingProvider = Services.getConfigurationValue(Services.GEOCODING_PROVIDER, GoogleGeoCodingProvider.class.getName());
					providerClass = (Class<GeoCodingProvider>)Class.forName(geocodingProvider);
				}

				providerInstance = providerClass.newInstance();

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Unable to instantiate geocoding provider: {0}", t.getMessage() );
			}
		}
		
		return providerInstance;
	}
	
	private static class NullResult implements GeoCodingResult {

		@Override
		public String getAddress() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public AddressComponent getAddressComponent(Type... types) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public List<AddressComponent> getAddressComponents() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public double getLatitude() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public double getLongitude() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void setAddress(String address) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void setLatitude(double latitude) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void setLongitude(double longitude) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public Double[] toArray() {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
	}
}
