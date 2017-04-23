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

package org.ggolden.expensey.auth;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.ggolden.expensey.auth.impl.SimpleAuthService;
import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the AuthenticationService.
 */
public class AuthenticationServiceTest
{
	// the service
	private static final AuthenticationService service = new SimpleAuthService();

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
		// authenticate as an unknown user
		Optional<Authentication> auth = service.authenticateByCredentials(new Credentials("Welcome1234", "user@mac.com"));
		Assertions.assertThat(auth).isEmpty();

		auth = service.authenticateByCredentials(new Credentials("Welcome123", "user2@mac.com"));
		Assertions.assertThat(auth).isEmpty();

		// authenticate as a known user
		auth = service.authenticateByCredentials(new Credentials("Welcome123", "user@mac.com"));
		Assertions.assertThat(auth).isNotEmpty();

		auth = service.authenticateByCredentials(new Credentials("Welcome123", "user@gmail.com"));
		Assertions.assertThat(auth).isNotEmpty();
	}

	@Test
	public void testNewUser()
	{
		// create a new user
		Credentials newCredentials = new Credentials("Welcome123", "user@icloud.com");

		// verify it is not already registered
		Optional<Authentication> auth = service.authenticateByCredentials(newCredentials);
		Assertions.assertThat(auth).isEmpty();

		// register it
		auth = service.registerUser(newCredentials);
		Assertions.assertThat(auth).isNotEmpty();

		// verify it can then authenticate
		auth = service.authenticateByCredentials(newCredentials);
		Assertions.assertThat(auth).isNotEmpty();

		// verify the password is working
		auth = service.authenticateByCredentials(new Credentials("Welcome1234", newCredentials.getUserId()));
		Assertions.assertThat(auth).isEmpty();

		// verify we cannot register another with the same user ID
		auth = service.registerUser(newCredentials);
		Assertions.assertThat(auth).isEmpty();

		// remove the user
		service.removeUser(newCredentials.getUserId());

		// verify that authentication is no longer possible
		auth = service.authenticateByCredentials(newCredentials);
		Assertions.assertThat(auth).isEmpty();

		// verify that we can now re-register with this id
		newCredentials.setPassword("Welcome321");
		auth = service.registerUser(newCredentials);
		Assertions.assertThat(auth).isNotEmpty();
		auth = service.authenticateByCredentials(newCredentials);
		Assertions.assertThat(auth).isNotEmpty();
	}

	@Test
	public void testTokens()
	{
		// authenticate
		Optional<Authentication> auth = service.authenticateByCredentials(new Credentials("Welcome123", "user@mac.com"));
		Assertions.assertThat(auth).isNotEmpty();

		// connect to the authentication by token
		Optional<Authentication> auth2 = service.authenticateByToken(auth.get().get_id());
		Assertions.assertThat(auth2).isNotEmpty();

		// verify that we get back the same auth
		Assertions.assertThat(auth2.get()).isEqualTo(auth.get());

		// verify the token is being checked
		auth2 = service.authenticateByToken("10000");
		Assertions.assertThat(auth2).isEmpty();

		// remove the auth
		service.removeAuthentication(auth.get());

		// verify it cannot be used to authenticate
		auth2 = service.authenticateByToken(auth.get().get_id());
		Assertions.assertThat(auth2).isEmpty();
	}
}
