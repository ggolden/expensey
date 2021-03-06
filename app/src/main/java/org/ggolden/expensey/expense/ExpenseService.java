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

package org.ggolden.expensey.expense;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.ggolden.expensey.expense.model.Expense;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ExpenseService
{
	/**
	 * Record a new expense
	 * 
	 * @param amount
	 *            The expense amount.
	 * @param date
	 *            The expense date.
	 * @param description
	 *            The expense description.
	 * @param userId
	 *            The user ID of the user claiming the expense.
	 * @return The recorded expense, or not if any fields are missing or invalid.
	 */
	Optional<Expense> addExpense(Float amount, Date date, String description, String userId);

	/**
	 * Get all the expenses for this user.
	 * 
	 * @param user
	 *            The user ID.
	 * @return The List of Expense, possibly empty.
	 */
	List<Expense> getExpensesForUser(String user);
}
