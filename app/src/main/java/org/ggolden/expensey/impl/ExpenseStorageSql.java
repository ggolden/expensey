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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.ggolden.expensey.db.Transactor;
import org.ggolden.expensey.expense.ExpenseStorage;
import org.ggolden.expensey.expense.model.Expense;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.LongColumnMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL based storage for Expense
 */
public class ExpenseStorageSql implements ExpenseStorage
{
	/**
	 * Make an Expense from results.
	 */
	protected class ExpenseMapper implements ResultSetMapper<Expense>
	{
		@Override
		public Expense map(int index, ResultSet r, StatementContext ctx) throws SQLException
		{
			Expense rv = new Expense(Long.toString(r.getLong("id")), r.getFloat("amount"), Transactor.toDate(r.getLong("date")), r.getString("description"),
					r.getString("user"));
			return rv;
		}
	}

	final static private Logger logger = LoggerFactory.getLogger(ExpenseServiceImpl.class);

	/** DB transaction access. */
	protected Transactor db;

	@Inject
	public ExpenseStorageSql(Transactor db)
	{
		this.db = db;

		createTables();
	}

	@Override
	public Optional<Expense> createExpense(Float amount, Date date, String description, String userId)
	{
		Transactor.Holder<Expense> rv = new Transactor.Holder<>();
		rv.value = Optional.of(new Expense(null, amount, date, description, userId));

		db.transact(h ->
		{
			long id = h.createStatement("insert into expense (user, date, amount, description) values (:user, :date, :amount, :description)") //
					.bind("user", userId) //
					.bind("date", Transactor.fromDate(date)) //
					.bind("amount", amount) //
					.bind("description", description) //
					.executeAndReturnGeneratedKeys(LongColumnMapper.PRIMITIVE) //
					.first();

			// set the generated id
			rv.value.get().set_id(Long.toString(id));
		});

		return rv.value;
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
		Transactor.Holder<List<Expense>> rv = new Transactor.Holder<>();

		db.transact(h ->
		{
			List<Expense> expenses = h.createQuery("select id, user, date, amount, description from expense where user=:user") //
					.bind("user", user) //
					.map(new ExpenseMapper()) //
					.list();

			rv.value = Optional.ofNullable(expenses);
		});

		return rv.value.orElse(new ArrayList<>());
	}

	@Override
	public void updateExpense(Expense expense)
	{
		// TODO Auto-generated method stub

	}

	protected void createTables()
	{
		db.transact(h ->
		{
			h.execute("create table if not exists expense (" //
					+ "id bigint unsigned auto_increment not null primary key," //
					+ "user varchar (255) not null," //
					+ "date bigint not null," //
					+ "amount float not null," //
					+ "description longtext," //
					+ "key expense_u (user)" //
					+ ")");
		});
	}
}
