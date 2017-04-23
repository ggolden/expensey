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
	// add the "Login" controller to the module
	angular.module("Expensey").controller("Login", Login);

	// the Login controller function
	function Login($log, $http, $q, $location)
	{
		// refer to the controller as 'ctrl', matching how we refer to it in the html
		var ctrl = this;

		ctrl.invalidCredentials = invalidCredentials;
		ctrl.login = login;

		ctrl.password = null;
		ctrl.email = null;

		ctrl.invalidLogin = false;
		ctrl.loggingIn = false;

		/** ******************************************************************************************************** */

		function invalidCredentials()
		{
			return ((ctrl.email == null) || (ctrl.password == null) || (ctrl.email.trim().length == 0) || (ctrl.password.trim().length == 0));
		}

		function login()
		{
			// send the login to the server
			post_login(
			{
				password : ctrl.password,
				userId : ctrl.email
			},
			{
				// if successful
				success : function()
				{
					// switch to the next view
					$location.replace();
					$location.path("/expenses");
				},
				failure : function()
				{
					// if login was unsuccessful
					ctrl.invalidLogin = true;
					$log.log("failed");
				},
				before : function()
				{
					ctrl.invalidLogin = false;
					ctrl.loggingIn = true;
				},
				after : function()
				{
					ctrl.loggingIn = false;
				}
			})
		}

		// post the login
		function post_login(credentials, options)
		{
			if (options.before !== undefined)
			{
				options.before();
			}

			$http.post("/api/data/login", credentials).then(function(response)
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
	}

})();
