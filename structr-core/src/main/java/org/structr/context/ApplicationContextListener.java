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

package org.structr.context;

import org.structr.core.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileInputStream;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

//~--- classes ----------------------------------------------------------------

/**
 * Web application lifecycle listener.
 *
 * @author Christian Morgner
 */
public class ApplicationContextListener implements ServletContextListener, HttpSessionListener {

	private static final Logger logger = Logger.getLogger(ApplicationContextListener.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		logger.log(Level.INFO, "Servlet context created");

		Map<String, String> context   = new ConcurrentHashMap<String, String>(20, 0.9f, 8);
		ServletContext servletContext = sce.getServletContext();
		String configFilePath         = servletContext.getInitParameter(Services.CONFIG_FILE_PATH);

		context.put(Services.CONFIG_FILE_PATH, configFilePath);
		context.put(Services.SERVLET_REAL_ROOT_PATH, servletContext.getRealPath("/"));

		try {

			// load config file
			Properties properties = new Properties();

			properties.load(new FileInputStream(configFilePath));

			String configuredServices = properties.getProperty(Services.CONFIGURED_SERVICES);

			logger.log(Level.INFO, "Config file configured services: {0}", configuredServices);

			String appTitle = properties.getProperty(Services.APPLICATION_TITLE);

			logger.log(Level.INFO, "Config file application title: {0}", appTitle);

			String tmpPath = properties.getProperty(Services.TMP_PATH);

			logger.log(Level.INFO, "Config file temp path: {0}", tmpPath);

			String basePath = properties.getProperty(Services.BASE_PATH);

			logger.log(Level.INFO, "Config file base path: {0}", basePath);

			String databasePath = properties.getProperty(Services.DATABASE_PATH);

			logger.log(Level.INFO, "Config file database path: {0}", databasePath);

			String filesPath = properties.getProperty(Services.FILES_PATH);

			logger.log(Level.INFO, "Config file files path: {0}", filesPath);

			String serverIp = properties.getProperty(Services.SERVER_IP);

			logger.log(Level.INFO, "Config file server IP: {0}", serverIp);

			String tcpPort = properties.getProperty(Services.TCP_PORT);

			logger.log(Level.INFO, "Config file TCP port: {0}", tcpPort);

			String udpPort = properties.getProperty(Services.UDP_PORT);

			logger.log(Level.INFO, "Config file UDP port: {0}", udpPort);

			String superuserUsername = properties.getProperty(Services.SUPERUSER_USERNAME);

			logger.log(Level.INFO, "Config file superuser username: {0}", superuserUsername);

			String superuserPassword = properties.getProperty(Services.SUPERUSER_PASSWORD);

			logger.log(Level.INFO, "Config file superuser password: {0}", superuserPassword);

			if (configuredServices != null) {

				context.put(Services.CONFIGURED_SERVICES, configuredServices);
			}

			if (appTitle != null) {

				context.put(Services.APPLICATION_TITLE, appTitle);
			}

			if (tmpPath != null) {

				context.put(Services.TMP_PATH, tmpPath);
			}

			if (basePath != null) {

				context.put(Services.BASE_PATH, basePath);
			}

			if (databasePath != null) {

				context.put(Services.DATABASE_PATH, databasePath);
			}

			if (filesPath != null) {

				context.put(Services.FILES_PATH, filesPath);
			}

			if (tcpPort != null) {

				context.put(Services.TCP_PORT, tcpPort);
			}

			if (serverIp != null) {

				context.put(Services.SERVER_IP, serverIp);
			}

			if (udpPort != null) {

				context.put(Services.UDP_PORT, udpPort);
			}

			if (superuserUsername != null) {

				context.put(Services.SUPERUSER_USERNAME, superuserUsername);
			}

			if (superuserPassword != null) {

				context.put(Services.SUPERUSER_PASSWORD, superuserPassword);
			}

			// load all properties into context
			for (String name : properties.stringPropertyNames()) {

				if (!context.containsKey(name)) {

					context.put(name, properties.getProperty(name));
				}

			}
		} catch (Throwable t) {

			// handle error
			logger.log(Level.WARNING, "Could not inititialize all values");
		}

		Services.initialize(context);

		// Services.setContext(context);

		// Initialize cloud service
		// not needed any more: Services.command(securityContext, StartCloudService.class);
		logger.log(Level.INFO, "structr application context initialized (structr started successfully)");

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

		logger.log(Level.INFO, "Servlet context destroyed");

		// TODO: remove servlet context attributes? Is it necessary?
		Services.shutdown();

	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {

		logger.log(Level.FINE, "HTTP session created", se.getSession().getId());

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {

		// clean session..
		HttpSession session = se.getSession();

		if (session != null) {

			String servletSessionId = se.getSession().getId();

			logger.log(Level.FINE, "HTTP session destroyed, cleaning ", servletSessionId);

		}
	}

}
