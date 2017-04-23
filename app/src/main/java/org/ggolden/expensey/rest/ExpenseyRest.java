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
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ggolden.expensey.auth.AuthenticationService;
import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.ggolden.expensey.dw.Configuration;
import org.ggolden.expensey.expense.ExpenseService;
import org.ggolden.expensey.expense.model.Expense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expensey REST endpoints.
 */
@Path("/data")
public class ExpenseyRest
{
	final static private Logger logger = LoggerFactory.getLogger(ExpenseyRest.class);

	protected final AuthenticationService authService;
	protected final Configuration config;
	protected final ExpenseService expenseService;

	@Inject
	public ExpenseyRest(Configuration config, AuthenticationService authService, ExpenseService expenseService)
	{
		this.config = config;
		this.authService = authService;
		this.expenseService = expenseService;

		logger.info("ExpenseyRsrc()");
	}

	@GET
	@Path("/expenses")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Expense> getExpenses( //
			@CookieParam(AuthenticationService.TOKEN) String authenticationToken, //
			@Context HttpServletRequest req)
	{
		// authenticate based on the cookie delivered token
		Optional<Authentication> authentication = authService.authenticateByToken(authenticationToken, req == null ? "" : req.getRemoteAddr());
		if (!authentication.isPresent())
			return null;

		// TODO: do other security checks before satisfying the request

		// get the expenses for this user
		List<Expense> rv = expenseService.getExpensesForUser(authentication.get().getUser());

		return rv;
	}

	/**
	 * Get the hello.
	 * 
	 * @param id
	 *            The hello id.
	 * @param authenticationToken
	 * @param req
	 * @return The array of Strings making up the hello.
	 */
	@GET
	@Path("/hello/{id : \\d+}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getPathId(@PathParam("id") Long id, //
			@CookieParam(AuthenticationService.TOKEN) String authenticationToken, //
			@Context HttpServletRequest req)
	{
		// authenticate based on the cookie delivered token
		Optional<Authentication> authentication = authService.authenticateByToken(authenticationToken, req == null ? "" : req.getRemoteAddr());
		if (!authentication.isPresent())
			return null;

		// TODO: do other security checks before satisfying the request

		List<String> rv = new ArrayList<>();

		// satisfy the request
		rv.add("Hello");
		rv.add("Gigsters");
		rv.add("(" + id.toString() + ")");
		rv.add("!");

		return rv;
	}

	/**
	 * Post a new expense.
	 * 
	 * @param authenticationToken
	 * @param req
	 * @param expense
	 *            The expense to post.
	 * @return The expense posted.
	 */
	@POST
	@Path("/expenses")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Expense postExpense( //
			@CookieParam(AuthenticationService.TOKEN) String authenticationToken, //
			@Context HttpServletRequest req, //
			Expense expense)
	{
		// authenticate based on the cookie delivered token
		Optional<Authentication> authentication = authService.authenticateByToken(authenticationToken, req == null ? "" : req.getRemoteAddr());
		if (!authentication.isPresent())
			return null;

		// TODO: do other security checks before satisfying the request

		// add the expense for the authenticated user
		Optional<Expense> added = expenseService.addExpense(expense.getAmount(), expense.getDate(), expense.getDescription(), authentication.get().getUser());
		if (!added.isPresent())
		{
			return null;
		}

		return added.get();
	}

	/**
	 * Respond to login.
	 * 
	 * @param userAgent
	 *            The user's browser agent.
	 * @param req
	 *            The request.
	 * @param credentials
	 *            The user's login credentials.
	 * @return An OK Response with authentication token cookie if successful, or a FORBIDDEN if not.
	 */
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postLogin( //
			@HeaderParam("user-agent") String userAgent, //
			@Context HttpServletRequest req, //
			Credentials credentials)
	{
		// authenticate these credentials - records the authentication if successful
		Optional<Authentication> auth = authService.authenticateByCredentials(credentials, req == null ? "" : req.getRemoteAddr());
		if (!auth.isPresent())
		{
			return Response.status(Status.FORBIDDEN).build();
		}

		// return OK with the authentication's id for a token
		return Response.ok().entity(auth.get().get_id()).cookie(new NewCookie(AuthenticationService.TOKEN, auth.get().get_id(), "/", null, "", -1, false))
				.build();
	}
}
