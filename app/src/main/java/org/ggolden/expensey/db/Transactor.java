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

package org.ggolden.expensey.db;

import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB Transaction support
 */
public class Transactor
{
	/**
	 * Represents an operation that accepts a single input argument and returns no result. Unlike most other functional interfaces, {@code Consumer} is expected
	 * to operate via side-effects.
	 *
	 * <p>
	 * This is a <a href="package-summary.html">functional interface</a> whose functional method is {@link #accept(Object)}.
	 *
	 * @param <T>
	 *            the type of the input to the operation
	 *
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface ConsumerThrowing<T>
	{

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param t
		 *            the input argument
		 */
		void accept(T t) throws DBIException;
	}

	final static private Logger logger = LoggerFactory.getLogger(Transactor.class);

	/** How many retries we attempt before giving up, in transact(), in case of db down or SQLException deadlock. */
	protected final static int MAX_RETRIES = 5;

	/**
	 * Safe way to prepare a boolean for writing to the database (as a mysql tinyint 0/1).
	 * 
	 * @param b
	 *            The boolean.
	 * @return The value to write to the database.
	 */
	public static Integer fromBoolean(Boolean b)
	{
		return (b == null) ? 0 : (b ? 1 : 0);
	}

	/**
	 * Safe way to prepare a date for writing to the database (as a mysql bigint).
	 * 
	 * @param d
	 *            The date.
	 * @return The value to write to the database.
	 */
	public static Long fromDate(Date d)
	{
		if (d == null)
			return null;
		return d.getTime();
	}

	/**
	 * Read a value from the db into a Boolean.
	 * 
	 * @param value
	 *            The (int 0:false, 1:true) value.
	 * @return The Boolean value.
	 */
	public static Boolean toBoolean(int value)
	{
		return value == 0 ? Boolean.FALSE : Boolean.TRUE;
	}

	/**
	 * Read a value from the db into a Date. A 0 is null.
	 * 
	 * @param value
	 *            The (Long) value.
	 * @return The Date value.
	 */
	public static Date toDate(long value)
	{
		if (value == 0)
			return null;
		return new Date(value);
	}

	/**
	 * Read a value from the db into a Long. 0 is treated as null.
	 * 
	 * @param value
	 *            The (Long) value.
	 * @return The Date value.
	 */
	public static Long toLong(long value)
	{
		if (value == 0)
			return null;
		return Long.valueOf(value);
	}

	/** The wrapped DBI. */
	protected final DBI dbi;

	public Transactor(DBI dbi)
	{
		this.dbi = dbi;
	}

	/**
	 * Run this job now, if we can get a database handle, and in case of deadlock error, retry a few times before giving up.
	 * 
	 * @param job
	 *            The job. All transaction code is added to the basic statements in the job, and should NOT be in the job. Job may run multiple times.
	 * @return true if run successfully, false if not.
	 */
	public boolean transact(ConsumerThrowing<Handle> job)
	{
		boolean success = false;

		// we will do MAX_RETRIES retries in case of deadlock or db not available
		int retries = MAX_RETRIES;
		while (retries > 0)
		{
			Optional<Handle> handle = open();
			if (handle.isPresent())
			{
				try (Handle h = handle.get())
				{
					try
					{
						h.begin();
						job.accept(h);
						h.commit();
						retries = 0;
						success = true;
					} catch (DBIException e)
					{
						logger.info("transact: retry: " + (MAX_RETRIES - retries) + " " + e.toString());
						if (e.getCause() instanceof SQLException)
						{
							SQLException s = (SQLException) e.getCause();

							if (s.getErrorCode() == 1213)
							{
								// a mysql deadlock killed the transaction, so we might retry
								retries--;
							} else
							{
								// some other error, we are done
								retries = 0;
							}
						}
						h.rollback();
					} catch (Throwable t)
					{
						logger.info("transact: fatal: " + (MAX_RETRIES - retries) + " " + t.toString());
						h.rollback();

						// we don't try again
						retries = 0;
					}
				}
			} else
			{
				// try again like a deadlock
				logger.info("transact: retry: " + (MAX_RETRIES - retries) + " DB Down");
				retries--;
			}

			// if we are retrying, take a quick break first
			if (retries > 0)
			{
				// sleep 1 .. 4 seconds, depending on how many retries we've done.
				try
				{
					Thread.sleep((5 - retries) * 1000);
				} catch (InterruptedException ie)
				{
				}
			}
		}

		return success;
	}

	/**
	 * Open a handle, deal with exceptions.
	 * 
	 * @return The handle, or not.
	 */
	protected Optional<Handle> open()
	{
		try
		{
			Handle h = dbi.open();
			return Optional.of(h);
		} catch (UnableToObtainConnectionException e)
		{
			return Optional.empty();
		}
	}
}
