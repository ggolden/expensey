/**********************************************************************************
 *
 * Copyright 2017 Glenn R. Golden 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.ggolden.expensey.dw;

import javax.inject.Singleton;

import org.eclipse.jetty.server.Server;
import org.ggolden.expensey.auth.AuthenticationService;
import org.ggolden.expensey.auth.impl.SimpleAuthService;
import org.ggolden.expensey.rest.ExpenseyRsrc;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Application extends io.dropwizard.Application<Configuration> {
	final static private Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		new Application().run(args);
	}

	@Override
	public String getName() {
		return "expensey";
	}

	@Override
	public void initialize(Bootstrap<Configuration> bootstrap) {

		// enable Java 8 object support in jackson json mapping
		// see: https://github.com/FasterXML/jackson-datatype-jdk8
		bootstrap.getObjectMapper().registerModule(new Jdk8Module());

		// serve our static assets from /, serving "index.html" as the default
		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
	}

	@Override
	public void run(Configuration configuration, Environment environment) {
		logger.info("run: configuration:" + configuration);

		// environment.lifecycle().manage(new NotifyManager(configuration.getHostname(), configuration.getAuth().getVersion()));

		// create our db connection, and make it available for injection into services
		DataSourceFactory database = new DataSourceFactory();
		database.setDriverClass("org.h2.Driver");
		database.setUrl("jdbc:h2:mem:apps;mode=mysql");
		database.setUser("u");
		database.setPassword("p");
		DBI dbi = new DBIFactory().build(environment, database, "db");

		// our wrapper around the dbi
		// DB db = new DB(dbi, autoDDL);

		// add our services, etc. to the component system
		environment.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {

				// make the configuration available
				bind(configuration).to(Configuration.class);

				// bind the DBI and DB
				bind(dbi).to(DBI.class);
				// bind(db).to(DB.class);

				// make our services available for injection - as singletons

				// authentication
				bind(SimpleAuthService.class).to(AuthenticationService.class).in(Singleton.class);

//				// user & data
//				bind(configuration.getUser()).to(UserConfiguration.class);
//				bind(UserDataJDBIImpl.class).to(UserData.class).in(Singleton.class);
//				bind(UserServiceImpl.class).to(UserService.class).in(Singleton.class);

				// make our resources singleton
				bind(ExpenseyRsrc.class).to(ExpenseyRsrc.class).in(Singleton.class);
			}
		});

		// register a custom jetty request log factory
//		((DefaultServerFactory) configuration.getServerFactory()).setRequestLogFactory(
//				new RequestLogJDBIFactory(db, configuration.isMock_data(), configuration.getHostname(), configuration.getAuth().getVersion()));

		// register our resources
		environment.jersey().register(ExpenseyRsrc.class);

		environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
			@Override
			public void serverStarted(Server server) {
				logger.info("ServerLifecycleListener.serverStarted()");

				ServiceLocator locator = ((ServletContainer) environment.getJerseyServletContainer()).getApplicationHandler().getServiceLocator();

				// start our singletons
//				locator.getService(AuthenticationService.class);
//				locator.getService(UserService.class);

				// start our resources
				locator.getService(ExpenseyRsrc.class);
			}
		});
	}
}
