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

package org.ggolden.expensey.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ggolden.expensey.expense.ExpenseService;
import org.ggolden.expensey.expense.model.Expense;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the ExpenseService.
 * 
 * In a real service,
 * 
 * - the expenses would be persisted in a database
 */
@Service
public class SimpleExpenseService implements ExpenseService
{
	final static private Logger logger = LoggerFactory.getLogger(SimpleExpenseService.class);

	/** To generate the next expense id. TODO: usually this would be done by the database with an autoincrement column. */
	protected AtomicInteger nextId = new AtomicInteger(1);

	/** authentications: mapped by token to the Authentication */
	Map<String, Expense> expenses = new HashMap<>();

	/**
	 * Create the expense service
	 */
	@Inject
	public SimpleExpenseService()
	{
		// this.userService = userService;
		logger.info("SimpleExpenseService()");
	}

	@Override
	public Optional<Expense> addExpense(Float amount, Date date, String description, String userId)
	{
		// TODO: validate the fields
		if ((date == null) || (description == null) || (userId == null))
		{
			return Optional.empty();
		}

		// generate a new ID
		String id = Integer.toString(nextId.getAndIncrement());

		// create the expense
		Expense ex = new Expense(id, amount, date, description, userId);

		// remember it
		expenses.put(ex.get_id(), ex);

		return Optional.of(ex);
	}

	@Override
	public List<Expense> getExpensesForUser(String user)
	{
		// the expenses for this user
		List<Expense> rv = expenses.values().stream() //
				.filter(e -> e.getUserId().equals(user)) //
				.collect(Collectors.toList());

		return rv;
	}
}
