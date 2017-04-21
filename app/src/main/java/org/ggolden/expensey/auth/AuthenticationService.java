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

import javax.servlet.http.HttpServletRequest;

import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface AuthenticationService {

	/** cookie name for authentication token from browser */
	final String TOKEN = "AUTH";

	/**
	 * Create an authentication based on credentials.
	 * 
	 * @param credentials
	 *            The authentication credentials (user id, password).
	 * @param ipAddress
	 *            The originating IP address, which may be used for further authentication requests.
	 * @return The Authentication if authenticated, or not.
	 */
	Optional<Authentication> authenticateByCredentials(Credentials credentials, String ipAddress);

	/**
	 * Authenticate by token. This must match the IP address of the initial authentication
	 * 
	 * @param token
	 *            The authentication token.
	 * @param req
	 *            The request.
	 * @return The Authentication if found and valid, or not.
	 */
	Optional<Authentication> authenticateByToken(String token, HttpServletRequest req);
}
