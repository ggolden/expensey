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
public class AuthenticationServiceTest {

	// the service
	private static final AuthenticationService service = new SimpleAuthService();

	/**
	 * Setup each test.
	 */
	@Before
	public void setup() {
	}

	/**
	 * Cleanup after each test
	 */
	@After
	public void tearDown() {
	}

	@Test
	public void test() {

		// authenticate as an unknown user
		Optional<Authentication> auth = service.authenticateByCredentials(new Credentials("Welcome1234", "ggolden22@mac.com"), "127.0.0.1");
		Assertions.assertThat(auth).isEmpty();

		auth = service.authenticateByCredentials(new Credentials("Welcome123", "ggolden222@mac.com"), "127.0.0.1");
		Assertions.assertThat(auth).isEmpty();

		// authenticate as a known user
		auth = service.authenticateByCredentials(new Credentials("Welcome123", "ggolden22@mac.com"), "127.0.0.1");
		Assertions.assertThat(auth).isNotEmpty();

		auth = service.authenticateByCredentials(new Credentials("Welcome123", "ggolden22@gmail.com"), "127.0.0.1");
		Assertions.assertThat(auth).isNotEmpty();
	}
}
