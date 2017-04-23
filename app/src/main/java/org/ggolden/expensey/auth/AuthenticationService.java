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

import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface AuthenticationService
{
	/** cookie name for authentication token from browser */
	final String TOKEN = "AUTH";

	/**
	 * Create an authentication based on credentials.
	 * 
	 * @param credentials
	 *            The authentication credentials (user id, password).
	 * @return The Authentication if authenticated, or not.
	 */
	Optional<Authentication> authenticateByCredentials(Credentials credentials);

	/**
	 * Authenticate by token.
	 * 
	 * @param token
	 *            The authentication token.
	 * @return The Authentication if found and valid, or not.
	 */
	Optional<Authentication> authenticateByToken(String token);

	/**
	 * Change a user's password. The user must already be authenticated.
	 * 
	 * @param authentication
	 *            The authentication, identifying a valid user.
	 * @param newPassword
	 *            The new password (clear text).
	 * @return true if the change was accepted, false if not. May be rejected if the password is not sufficiently secure.
	 */
	boolean changePassword(Authentication authentication, String newPassword);

	/**
	 * Register a new user, and create their first authentication.
	 * 
	 * @param credentials
	 *            The new user's credentials.
	 * @return The authentication, or not. May fail if the user ID in the credentials is already known, or the PW is insufficiently secure.
	 */
	Optional<Authentication> registerUser(Credentials credentials);

	/**
	 * End the validity of this authentication, so that it cannot be used to authentication (token).
	 * 
	 * @param authentication
	 *            The authentication to remove.
	 */
	void removeAuthentication(Authentication authentication);

	/**
	 * Remove a user. After removal, the user ID can no longer be used for authentication, unless a new user is registered with this ID.
	 * 
	 * @param userID
	 *            The user ID
	 */
	void removeUser(String userID);
}
