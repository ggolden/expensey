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

package org.ggolden.expensey.auth.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.ggolden.expensey.auth.AuthenticationService;
import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the AuthenticationService.
 */
@Service
public class SimpleAuthService implements AuthenticationService {
	final static private Logger logger = LoggerFactory.getLogger(SimpleAuthService.class);

	// may show up as the IP address when testing
	protected static byte[] noAddress = { 0, 0, 0, 0 };

	// authentications: mapped by token to the Authentication
	Map<String, Authentication> auths = new HashMap<>();

	// pre-defined users
	List<Credentials> credentials = new ArrayList<>();

	// protected UserService userService;

	/**
	 * Create the authentication service
	 * 
	 * @param config
	 *            The configuration
	 */
	@Inject
	public SimpleAuthService(/* UserService userService */) {
		// this.userService = userService;
		logger.info("SimpleAuthService()");

		// fill out predefined credentials TODO: from config
		this.credentials.add(new Credentials("Welcome123", "ggolden22@mac.com"));
		this.credentials.add(new Credentials("Welcome123", "ggolden22@gmail.com"));
	}

	@Override
	public Optional<Authentication> authenticateByCredentials(Credentials credentials, String ipAddress) {

		// check credentials
		String user = "user";

		// generate a new ID
		String id = "id";

		// create and record the authentication
		Authentication auth = new Authentication(id, new Date(), ipAddress, user);
		this.auths.put("id", auth);

		// return the authentication
		return Optional.of(auth);
	}

	@Override
	public Optional<Authentication> authenticateByToken(String token, HttpServletRequest req) {

		// check that the token is to a valid authentication
		Authentication auth = this.auths.get(token);
		if (auth == null) {
			return Optional.empty();
		}
		// further check that the IP address is the same as when the authentication was created
		if (auth.getIpAddress().equals(remoteAddr(req))) {
			return Optional.empty();
		}

		// all is well
		return Optional.of(auth);
	}

	/**
	 * Testing may not provide a request object... safe way to get the remote IP
	 * 
	 * @param req
	 *            The request.
	 * @return The remote IP, or null.
	 */
	protected String remoteAddr(HttpServletRequest req) {
		if (req == null)
			return null;
		return req.getRemoteAddr();
	}
}
