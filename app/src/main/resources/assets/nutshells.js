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

(function()
{
	// add the "People" controller to the module
	angular.module("Expensey").controller("Nutshells", Nutshells);

	// the Nutshells controller function
	function Nutshells($log, $http, $q)
	{
		// refer to the controller as 'ctrl', matching how we refer to it in the html
		var ctrl = this;

		// the people read
		ctrl.allPeople = [];

		// the people processed: selected and sorted and count-limited
		ctrl.people = [];

		/** ******************************************************************************************************** */

		// these are private, not used by the UI
		// get all courses for this term: page - the page number to read, options - various declarations used when the API call is done
		function get_people(page, options)
		{
			// the REST API call, creating a promise we will act upon when it succeeds
			var peoplePromise = $http.get("http://join.nutshell.com/people/" + page);

			// when they are ALL done (note, in this case, we have a single call to make, but in general, it may be many concurrent)
			$q.all([ peoplePromise ]).then(function(results)
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
							people : results[0].data,
							page : page
						});
					}
				}
				else
				{
					// process failure if defined in the options
					if (options.failure !== undefined)
					{
						options.failure(results);
					}
				}
			});
		}

		// read all the people from the server: page - the page to start with
		function load(page)
		{
			if (page === undefined)
				page = 1;

			// an empty page returned indicates we have run out of pages to read
			// guard against server being crazy and constantly returning non-empty pages
			if (page > 100)
			{
				$log.log("> 100 pages");
				return;
			}

			// start a page read
			get_people(page,
			{
				success : function(data)
				{
					// add these to the total
					var peopleRead = data.people;
					ctrl.allPeople = ctrl.allPeople.concat(peopleRead);

					// if there's more (we read a non-empty page), start another read
					if (peopleRead.length > 0)
					{
						load(page + 1);
					}
					else
					{
						// when all read, process the data
						ctrl.people = process(ctrl.allPeople);
					}
				},
				failure : function()
				{
					$log.log("failure");
				}
			});
		}

		// process the read people, applying our search, ordering, and count limiting criteria:
		// "Write some code that prints the 5 newest people (by signup date) that have a non-null email address."
		function process(people)
		{
			// set a real Date from the string read
			for (var i = 0; i < people.length; i++)
			{
				people[i].signup_date = moment(people[i].signup_date, "YYYY-MM-DD").toDate();
			}

			// sort by Date, desc
			people = people.sort(function(a, b)
			{
				return b.signup_date - a.signup_date;
			});

			// filter by email set
			people = people.filter(function(a)
			{
				return a.email != null;
			});

			// first 5
			people = people.slice(0, 5);

			return people;
		}

		load();
	}

})();