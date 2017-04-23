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
import org.ggolden.expensey.auth.impl.AuthenticationServiceImpl;
import org.ggolden.expensey.db.Transactor;
import org.ggolden.expensey.expense.ExpenseService;
import org.ggolden.expensey.expense.ExpenseStorage;
import org.ggolden.expensey.impl.ExpenseServiceImpl;
import org.ggolden.expensey.impl.ExpenseStorageMem;
import org.ggolden.expensey.impl.ExpenseStorageSql;
import org.ggolden.expensey.rest.ExpenseyRest;
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

public class Application extends io.dropwizard.Application<Configuration>
{
	final static private Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception
	{
		new Application().run(args);
	}

	@Override
	public String getName()
	{
		return "expensey";
	}

	@Override
	public void initialize(Bootstrap<Configuration> bootstrap)
	{
		// enable Java 8 object support in jackson json mapping
		// see: https://github.com/FasterXML/jackson-datatype-jdk8
		bootstrap.getObjectMapper().registerModule(new Jdk8Module());

		// serve our static assets from /, serving "index.html" as the default
		bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
	}

	@Override
	public void run(Configuration configuration, Environment environment)
	{
		logger.info("run: configuration:" + configuration);

		// create our db connection, and make it available for injection into services
		// TODO: this is for test, with a file based H2 db
		DataSourceFactory database = new DataSourceFactory();
		database.setDriverClass("org.h2.Driver");
		database.setUrl("jdbc:h2:file:~/expensey;mode=mysql");
		database.setUser("u");
		database.setPassword("p");
		DBI dbi = new DBIFactory().build(environment, database, "db");

		// our wrapper around the dbi for transactions
		Transactor transactor = new Transactor(dbi);

		// add our services, etc. to the component system
		environment.jersey().register(new AbstractBinder()
		{
			@Override
			protected void configure()
			{
				// make the configuration available
				bind(configuration).to(Configuration.class);

				// bind the DBI and Transactor
				bind(dbi).to(DBI.class);
				bind(transactor).to(Transactor.class);

				// make our services available for injection - as singletons

				// authentication
				bind(AuthenticationServiceImpl.class).to(AuthenticationService.class).in(Singleton.class);

				// expenses - using the test/mem storage
				// bind(ExpenseStorageMem.class).to(ExpenseStorage.class).in(Singleton.class);
				
				// expenses - using the sql storage
				bind(ExpenseStorageSql.class).to(ExpenseStorage.class).in(Singleton.class);
				bind(ExpenseServiceImpl.class).to(ExpenseService.class).in(Singleton.class);

				// make our resources singleton
				bind(ExpenseyRest.class).to(ExpenseyRest.class).in(Singleton.class);
			}
		});

		// register our resources
		environment.jersey().register(ExpenseyRest.class);

		environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener()
		{
			@Override
			public void serverStarted(Server server)
			{
				logger.info("ServerLifecycleListener.serverStarted()");

				ServiceLocator locator = ((ServletContainer) environment.getJerseyServletContainer()).getApplicationHandler().getServiceLocator();

				// start our singletons
				locator.getService(AuthenticationService.class);
				locator.getService(ExpenseService.class);

				// start our resources
				locator.getService(ExpenseyRest.class);
			}
		});
	}
}
