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
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.ggolden.expensey.expense.model.Expense;
import org.ggolden.expensey.impl.SimpleExpenseService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ExpenseService.
 */
public class ExpenseServiceTest
{
	// the service
	private static final ExpenseService service = new SimpleExpenseService();

	/**
	 * Setup each test.
	 */
	@Before
	public void setup()
	{
	}

	/**
	 * Cleanup after each test
	 */
	@After
	public void tearDown()
	{
	}

	@Test
	public void test()
	{
		// add an expense
		Date now = new Date();
		Float amount = 22.22f;
		String description = "Parking at airport";
		String userId = "user@mac.com";
		Optional<Expense> expense = service.addExpense(amount, now, description, userId);
		Assertions.assertThat(expense).isNotEmpty();
		Assertions.assertThat(expense.get().getDate().equals(now));
		Assertions.assertThat(expense.get().getDescription().equals(description));
		Assertions.assertThat(expense.get().getUserId().equals(userId));
	}
}
