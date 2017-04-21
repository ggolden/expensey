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

package org.ggolden.expensey.auth.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication models a user authentication event to an app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authentication {

	/** The authentication id - can be used as a token to authenticate-by-token. */
	@JsonProperty("id")
	protected String _id;

	/** Date authenticated. */
	protected Date date;

	/** Originating IP address. */
	protected String ipAddress;

	/** The authenticated user. */
	protected String user;
}
