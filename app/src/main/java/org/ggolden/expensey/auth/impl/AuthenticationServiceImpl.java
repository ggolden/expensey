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
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.ggolden.expensey.auth.AuthenticationService;
import org.ggolden.expensey.auth.model.Authentication;
import org.ggolden.expensey.auth.model.Credentials;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the AuthenticationService.
 * 
 * In a real service,
 * 
 * - the known credentials would be persisted in a database
 * 
 * - the password in the know credentials would be stored as a hash, then checked by hashing the pw in the presented credentials (so even if hacked, the stored
 * password would not be exposed)
 * 
 * - the authentications would be logged, probably to a database, for user access and usage tracking
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService
{
	final static private Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	/** To generate the next authentication id. TODO: usually this would be done by the database with an auto-increment column. */
	protected AtomicInteger nextId = new AtomicInteger(1);

	/** authentications: mapped by token to the Authentication */
	Map<String, Authentication> auths = new HashMap<>();

	/** known users */
	List<Credentials> credentials = new ArrayList<>();

	/**
	 * Create the authentication service
	 * 
	 * @param config
	 *            The configuration
	 */
	@Inject
	public AuthenticationServiceImpl()
	{
		logger.info("SimpleAuthService()");

		// fill out predefined credentials TODO: from config
		this.credentials.add(new Credentials("Welcome123", "user@mac.com"));
		this.credentials.add(new Credentials("Welcome123", "user@gmail.com"));
	}

	@Override
	public Optional<Authentication> authenticateByCredentials(Credentials credentials)
	{
		// check credentials
		int foundAt = this.credentials.indexOf(credentials);
		if (foundAt == -1)
		{
			return Optional.empty();
		}

		// use the stored credentials for any values we need -
		// the find process above might be less than fully strict (such as allowing case insensitive user id checks)
		Credentials found = this.credentials.get(foundAt);

		// generate a new ID
		String id = Integer.toString(this.nextId.getAndIncrement());

		// create and record the authentication
		Authentication auth = new Authentication(id, new Date(), found.getUserId());
		this.auths.put(id, auth);

		// return the authentication
		return Optional.of(auth);
	}

	@Override
	public Optional<Authentication> authenticateByToken(String token)
	{
		// check that the token is to a valid authentication
		Authentication auth = this.auths.get(token);
		if (auth == null)
		{
			return Optional.empty();
		}

		// all is well
		return Optional.of(auth);
	}

	@Override
	public boolean changePassword(Authentication authentication, String newPassword)
	{
		// get the credentials
		Optional<Credentials> found = this.credentials.stream().filter(c -> c.getUserId().equals(authentication.getUser())).findFirst();
		if (!found.isPresent())
		{
			return false;
		}

		// we will also need the index of this so we can replace it later
		int foundAt = this.credentials.indexOf(found.get());
		if (foundAt == -1)
		{
			// this should not happen
			logger.warn("changePassword(): could not find index of found credentials: " + found.get().getUserId());
			return false;
		}

		// validate the new password TODO:

		// create new credentials
		Credentials replacement = new Credentials(newPassword, found.get().getUserId());

		// replace
		this.credentials.set(foundAt, replacement);

		return true;
	}

	@Override
	public Optional<Authentication> registerUser(Credentials credentials)
	{
		// validate that the user ID has not already been registered
		Optional<Credentials> found = this.credentials.stream().filter(c -> c.getUserId().equals(credentials.getUserId())).findFirst();
		if (found.isPresent())
		{
			return Optional.empty();
		}

		// validate the new password TODO:

		// store the new credentials
		this.credentials.add(credentials);

		// return the authentication
		return this.authenticateByCredentials(credentials);
	}

	@Override
	public void removeAuthentication(Authentication authentication)
	{
		// TODO: if storing authentications in the database, you might want to keep the record, but mark it closed.

		// remove the authentication
		this.auths.remove(authentication.get_id());
	}

	@Override
	public void removeUser(String userID)
	{
		// find the existing credentials
		Optional<Credentials> found = this.credentials.stream().filter(c -> c.getUserId().equals(userID)).findFirst();
		if (!found.isPresent())
		{
			return;
		}

		int foundAt = this.credentials.indexOf(found.get());
		if (foundAt == -1)
		{
			// this should not happen
			logger.warn("removeUser(): could not find index of found credentials: " + found.get().getUserId());
			return;
		}

		// remove
		this.credentials.remove(foundAt);

		// TODO: if we have records of authentications, we might want to clear them

		// TODO: if currently authenticated, end it
	}
}
