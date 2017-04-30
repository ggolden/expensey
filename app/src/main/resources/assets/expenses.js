/***************************************************************************************************************************************************************
 * 
 * Copyright 2017 Glenn R. Golden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * 
 **************************************************************************************************************************************************************/

(function()
{
	// add the "Expenses" controller to the module
	angular.module("Expensey").controller("Expenses", Expenses);

	// the Expenses controller function
	function Expenses($log, $http, $q, $location)
	{
		// refer to the controller as 'ctrl', matching how we refer to it in the html
		var ctrl = this;

		// the expenses read
		ctrl.allExpenses = [];

		// the expenses processed: selected and sorted and count-limited
		ctrl.expenses = [];
		ctrl.noExpenses = false;

		ctrl.expense = newExpense();
		ctrl.adding = false;

		ctrl.addExpense = addExpense;
		ctrl.invalidExpense = invalidExpense;
		ctrl.logout = logout;

		ctrl.datePickerFormat = "MMM dd, yyyy";
		ctrl.datePickerOpen = false;

		ctrl.loading = false;

		/** ******************************************************************************************************** */

		// get all expenses for the current user
		function get_expenses(options)
		{
			if (options.before !== undefined)
			{
				options.before();
			}

			// the REST API call, creating a promise we will act upon when it succeeds
			var expensesPromise = $http.get("/api/data/expenses");

			// when they are ALL done (note, in this case, we have a single call to make, but in general, it may be many concurrent)
			$q.all([ expensesPromise ]).then(function(results)
			{
				// check success (again, with the idea we might have to make multiple concurrent calls)
				var success = results.every(function(element, index, array)
				{
					return element.status == 200
				});

				if (success)
				{
					// process success if defined in the options
					if (options.success !== undefined)
					{
						options.success(
						{
							expenses : results[0].data
						});
					}
					if (options.after !== undefined)
					{
						options.after();
					}
				}
				else
				{
					// process failure if defined in the options
					if (options.failure !== undefined)
					{
						options.failure(results);
					}
					if (options.after !== undefined)
					{
						options.after();
					}
				}
			});
		}

		// post an expense
		function post_expense(expense, options)
		{
			if (options.before !== undefined)
			{
				options.before();
			}

			$http.post("/api/data/expenses", expense).then(function(response)
			{
				var success = (response.status == 200);

				if (success)
				{
					if (options.success !== undefined)
					{
						options.success(response);
					}
				}
				else
				{
					if (options.failure !== undefined)
					{
						options.failure(response);
					}
				}
				if (options.after !== undefined)
				{
					options.after();
				}
			}, function(response)
			{
				if (options.failure !== undefined)
				{
					options.failure(response);
				}
				if (options.after !== undefined)
				{
					options.after();
				}
			});
		}

		// read all expenses for the current user
		function load()
		{
			get_expenses(
			{
				success : function(data)
				{
					ctrl.allExpenses = data.expenses;
					ctrl.expenses = process(ctrl.allExpenses);
					ctrl.noExpenses = (ctrl.expenses.length == 0);
				},
				failure : function()
				{
					$log.log("failure");
				},
				before : function()
				{
					ctrl.loading = true;
				},
				after : function()
				{
					ctrl.loading = false;
				}
			});
		}

		// process the expenses, selecting those to display, and their order
		function process(expenses)
		{
			// TODO: all of them
			var rv = expenses;

			// sort by Date, desc
			expenses = expenses.sort(function(a, b)
			{
				return b.date - a.date;
			});

			return expenses;
		}

		function addExpense()
		{
			post_expense(ctrl.expense,
			{
				success : function(data)
				{
					ctrl.expense = newExpense();
					load();
				},
				failure : function()
				{
					$log.log("failure");
				},
				before : function()
				{
					ctrl.adding = true;
					ctrl.loading = true;
				},
				after : function()
				{
					ctrl.adding = false;
					ctrl.loading = false;
				}
			});
		}

		function invalidExpense()
		{
			return ((ctrl.expense.amount == null) || //
			(ctrl.expense.date == null) || //
			(ctrl.expense.description == null));
		}

		function newExpense()
		{
			var rv =
			{
				id : null,
				amount : null,
				date : new Date(),
				description : null,
				userId : null
			}

			return rv;
		}

		function logout()
		{
			// TODO: logout on the server

			$location.replace();
			$location.path("/login");
		}

		load();
	}

})();