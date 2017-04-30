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
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.ggolden.expensey.expense.ExpenseService;
import org.ggolden.expensey.expense.ExpenseStorage;
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
public class ExpenseServiceImpl implements ExpenseService
{
	final static private Logger logger = LoggerFactory.getLogger(ExpenseServiceImpl.class);

	/** storage manager for expenses. */
	protected ExpenseStorage storage;

	/**
	 * Create the expense service
	 */
	@Inject
	public ExpenseServiceImpl(ExpenseStorage storage)
	{
		this.storage = storage;

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

		return storage.createExpense(amount, date, description, userId);
	}

	@Override
	public List<Expense> getExpensesForUser(String user)
	{
		return storage.readExpensesForUser(user);
	}
}
