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

import org.ggolden.expensey.expense.ExpenseStorage;
import org.ggolden.expensey.expense.model.Expense;

/**
 * Memory (test) based storage for Expense
 */
public class ExpenseStorageMem implements ExpenseStorage
{
	/** To generate the next expense id. TODO: usually this would be done by the database with an autoincrement column. */
	protected AtomicInteger nextId = new AtomicInteger(1);

	/** authentications: mapped by token to the Authentication */
	Map<String, Expense> expenses = new HashMap<>();

	@Override
	public Optional<Expense> createExpense(Float amount, Date date, String description, String userId)
	{
		// generate a new ID
		String id = Integer.toString(nextId.getAndIncrement());

		// create the expense
		Expense ex = new Expense(id, amount, date, description, userId);

		// remember it
		expenses.put(ex.get_id(), ex);

		return Optional.of(ex);
	}

	@Override
	public void deleteExpense(Expense expense)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<Expense> readExpense(String id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Expense> readExpensesForUser(String user)
	{
		// the expenses for this user
		List<Expense> rv = expenses.values().stream() //
				.filter(e -> e.getUserId().equals(user)) //
				.collect(Collectors.toList());

		return rv;
	}

	@Override
	public void updateExpense(Expense expense)
	{
		// TODO Auto-generated method stub

	}
}
