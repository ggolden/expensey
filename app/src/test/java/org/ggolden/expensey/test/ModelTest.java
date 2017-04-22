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

package org.ggolden.expensey.test;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

public abstract class ModelTest<T>
{
	protected static final ObjectMapper MAPPER = Jackson.newObjectMapper().registerModule(new Jdk8Module());

	/**
	 * Test that the JSON read model object matches the constructed model object.
	 * 
	 * @throws Exception
	 */
	@Test
	public void modelFromJSON() throws Exception
	{
		final T o = mockObjectAsFixture();
		Assertions.assertThat(MAPPER.readValue(FixtureHelpers.fixture(getFixtureName()), o.getClass())).isEqualTo(o);
	}

	/**
	 * Test that the model object serialized to JSON matches the JSON in the file (which passes through de-serialization and serialization).
	 * 
	 * @throws Exception
	 */
	@Test
	public void modelToJSON() throws Exception
	{
		final T o = mockObjectAsFixture();
		final String json = MAPPER.writeValueAsString(o);
		final String expected = MAPPER.writeValueAsString(MAPPER.readValue(FixtureHelpers.fixture(getFixtureName()), o.getClass()));
		Assertions.assertThat(json).isEqualTo(expected);
	}

	/** Provide the JSON file name for this class. */
	protected abstract String getFixtureName();

	/** Provide an object of the class that matches the JSON in the file. */
	protected abstract T mockObjectAsFixture();
}
