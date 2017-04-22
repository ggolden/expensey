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
	// add the "Login" controller to the module
	angular.module("Expensey").controller("Login", Login);

	// the Login controller function
	function Login($log, $http, $q, $location)
	{
		// refer to the controller as 'ctrl', matching how we refer to it in the html
		var ctrl = this;

		ctrl.invalidCredentials = invalidCredentials;
		ctrl.login = login;

		ctrl.password = "";
		ctrl.email = "";

		/** ******************************************************************************************************** */

		function invalidCredentials()
		{
			return ((ctrl.email.trim().length == 0) || (ctrl.password.trim().length == 0));
		}

		function login()
		{
			$log.log("login", ctrl.email);
			$location.path("/nutshells");
		}
	}

})();
