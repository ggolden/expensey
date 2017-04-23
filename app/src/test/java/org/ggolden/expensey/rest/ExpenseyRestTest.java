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

package org.ggolden.expensey.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.ggolden.expensey.auth.AuthenticationService;
import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.ggolden.expensey.dw.Configuration;
import org.ggolden.expensey.expense.ExpenseService;
import org.ggolden.expensey.expense.model.Expense;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import io.dropwizard.testing.junit.ResourceTestRule;

/**
 * Test the ExpenseyRest REST endpoints.
 */
public class ExpenseyRestTest
{
	/** mock authentication service */
	public static final AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);

	/** mock config */
	public static Configuration config = new Configuration("environment");

	/** mock expense service */
	public static final ExpenseService expenseService = Mockito.mock(ExpenseService.class);

	/** the resource we are testing */
	@ClassRule
	public static final ResourceTestRule z_rest = ResourceTestRule.builder().addResource(new ExpenseyRest(config, authenticationService, expenseService))
			.build();

	/** some credentials used in tests */
	protected static Credentials credentials_bad = new Credentials("Welcome1234", "user@mac.com");
	protected static Credentials credentials_good = new Credentials("Welcome123", "user@mac.com");

	protected static Expense expense = new Expense("id", 85.0f, new Date(), "Parking at airport", "user@mac.com");

	/**
	 * Before each test, setup the mock services to return the mock models, if asked for nicely ;-)
	 */
	@Before
	public void setup()
	{
		// setup authenticationService
		Authentication auth = new Authentication("id", new Date(), "ip", "user");
		Mockito.when(authenticationService.authenticateByToken(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(authenticationService.authenticateByToken(Mockito.eq("authorized"), Mockito.anyString())).thenReturn(Optional.of(auth));
		Mockito.when(authenticationService.authenticateByCredentials(Mockito.eq(credentials_good), Mockito.anyString())).thenReturn(Optional.of(auth));
		Mockito.when(authenticationService.authenticateByCredentials(Mockito.eq(credentials_bad), Mockito.anyString())).thenReturn(Optional.empty());

		// setup expenseService
		Mockito.when(expenseService.addExpense(Mockito.anyFloat(), Mockito.any(Date.class), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Optional.of(expense));

		List<Expense> expenses = new ArrayList<>();
		expenses.add(expense);
		expenses.add(expense);
		Mockito.when(expenseService.getExpensesForUser(Mockito.anyString())).thenReturn(expenses);
	}

	/**
	 * Cleanup after the test, resetting the mocked services.
	 */
	@After
	public void tearDown()
	{
		Mockito.reset(authenticationService);
		Mockito.reset(expenseService);
	}

	/**
	 * Test the /login path
	 */
	@Test
	public void testLogin()
	{
		Response rv = z_rest.client().target("/data/login").request().post(Entity.entity(credentials_bad, MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertThat(rv).isNotNull();
		Assertions.assertThat(rv.getStatus()).isEqualTo(403);

		rv = z_rest.client().target("/data/login").request().post(Entity.entity(credentials_good, MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertThat(rv).isNotNull();
		Assertions.assertThat(rv.getStatus()).isEqualTo(200);
	}

	/**
	 * test the /expenses path
	 */
	@Test
	public void testExpense()
	{
		// not valid auth token
		Expense rv = z_rest.client().target("/data/expenses").request().cookie(AuthenticationService.TOKEN, "auth")
				.post(Entity.entity(expense, MediaType.APPLICATION_JSON_TYPE), Expense.class);
		Assertions.assertThat(rv).isNull();

		// valid auth token
		rv = z_rest.client().target("/data/expenses").request().cookie(AuthenticationService.TOKEN, "authorized")
				.post(Entity.entity(expense, MediaType.APPLICATION_JSON_TYPE), Expense.class);
		Assertions.assertThat(rv).isNotNull();
		Assertions.assertThat(rv.get_id()).isNotNull();
		Assertions.assertThat(rv.getAmount()).isEqualTo(expense.getAmount());
		Assertions.assertThat(rv.getDescription()).isEqualTo(expense.getDescription());
		Assertions.assertThat(rv.getDate()).isEqualTo(expense.getDate());
	}

	/**
	 * test the "/hello/{id : \\d+} path
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testHello()
	{
		List<String> rv = new ArrayList<>();

		// token not valid
		List<String> value = z_rest.client().target("/data/hello/1").request().cookie(AuthenticationService.TOKEN, "auth").get(rv.getClass());
		Assertions.assertThat(value).isNull();

		// token is valid
		value = z_rest.client().target("/data/hello/1").request().cookie(AuthenticationService.TOKEN, "authorized").get(rv.getClass());
		Assertions.assertThat(value).isNotNull();
		Assertions.assertThat(value).isNotEmpty();
		Assertions.assertThat(value).hasSize(4);
		Assertions.assertThat(value).contains("Hello", "Gigsters", "(1)", "!");
	}

	/**
	 * test the "/expenses path
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testExpenses()
	{
		List<String> rv = new ArrayList<>();

		// token not valid
		List<Expense> value = z_rest.client().target("/data/expenses").request().cookie(AuthenticationService.TOKEN, "auth").get(rv.getClass());
		Assertions.assertThat(value).isNull();

		// token is valid
		value = z_rest.client().target("/data/expenses").request().cookie(AuthenticationService.TOKEN, "authorized").get(rv.getClass());
		Assertions.assertThat(value).isNotNull();
		Assertions.assertThat(value).isNotEmpty();
		Assertions.assertThat(value).hasSize(2);
	}
}
