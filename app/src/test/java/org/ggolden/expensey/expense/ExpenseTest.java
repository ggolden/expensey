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

import org.ggolden.expensey.expense.model.Expense;
import org.ggolden.expensey.test.ModelTest;

public class ExpenseTest extends ModelTest<Expense>
{
	@Override
	protected String getFixtureName()
	{
		return "fixtures/expense.json";
	}

	@Override
	protected Expense mockObjectAsFixture()
	{
		final Expense expense = new Expense("ID", 100.50f, new Date(1L), "DESCRIPTION", "USER");
		return expense;
	}
}
