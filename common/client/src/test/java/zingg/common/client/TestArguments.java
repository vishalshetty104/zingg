/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

public class TestArguments {

	private static final String KEY_HEADER = "header";
	private static final String KEY_FORMAT = "format";
	private static final String KEY_MODEL_ID = "modelId";

	public static final Log LOG = LogFactory.getLog(TestArguments.class);

	@Test
	public void testSubstituteVariablesWithAllEnvVarSet() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "400");
	
			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));
			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			assertEquals(args.getData()[0].getProps().get(KEY_HEADER), env.get(KEY_HEADER));
			assertEquals(args.getData()[0].getFormat(), env.get(KEY_FORMAT));
			assertEquals(args.getModelId(), env.get(KEY_MODEL_ID));
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception " + e.getMessage());
		}
	}

	@Test
	public void testSubstituteVariablesWithMissingEnvVar() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
			fail("Exception was expected due to missing environment variable");
 		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to missing environment variable");
 		}
	}

	@Test
	public void testSubstituteVariablesWithBlankEnvVar() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for blank value for an environment variable");
 		} catch (IOException | ZinggClientException e) {
 			LOG.warn("Expected exception received due to blank value for an environment variable");
		}
	}

	@Test
	public void testInvalidEnvVarBooleanType() {
		try {

			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "someValue");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments.createArgumentsFromJSONString(json, "");
 
			fail("Exception was expected for invalid value for a Boolean variable");
 		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to invalid value for a Boolean variable");
 		}
	}

	@Test
	public void testBooleanType() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "400");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");
 
			assertEquals(args.getOutput()[0].getProps().get(KEY_HEADER), env.get(KEY_HEADER));
		} catch (IOException | ZinggClientException e) {
			fail("Exception was not expected for valid value for a Boolean variable within quotes");

		}
	}

	@Test
	public void testInvalidEnvVarNumericType() {
		try {
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "ONEHUNDRED");

			byte[] encoded = Files
					.readAllBytes(Paths.get(getClass().getResource("../../../testArguments/testConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for invalid value for a Numeric variable");
		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to invalid value for a Numeric variable");
		}
	}

	@Test
	public void testNumericWithinQuotes() {
		try {
			
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "500");

			byte[] encoded = Files.readAllBytes(
					Paths.get(getClass().getResource("../../../testArguments/testNumericWithinQuotesTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			//Numeric within quotes are allowed
			assertEquals(args.getModelId(), env.get(KEY_MODEL_ID));
		} catch (IOException | ZinggClientException e) {
			fail("Unexpected exception in testNumericWithinQuotes()" + e.getMessage());
		}
	}

	@Test
	public void testMalformedVariable() {
		try {
			
			Map<String, String> env = new HashMap<String, String>();
			env.put(KEY_HEADER, "true");
			env.put(KEY_FORMAT, "csv");
			env.put(KEY_MODEL_ID, "500");

			byte[] encoded = Files.readAllBytes(
					Paths.get(getClass().getResource("../../../testArguments/testMalformedConfigTemplate.json.env").getFile()));

			String template = new String(encoded, StandardCharsets.UTF_8);
			String json = Arguments.substituteVariables(template, env);
			Arguments args = Arguments.createArgumentsFromJSONString(json, "");

			fail("Exception was expected for malformed variable in json");
		} catch (IOException | ZinggClientException e) {
			LOG.warn("Expected exception received due to malformed variable in json");
		}
	}

	@Test
	public void testInvalidFilePath() {
		String filePath = "../dummyFilename";
		try {
			Arguments.createArgumentsFromJSONTemplate(filePath, "");
			fail("Exception was expected for invalid filepath or name");
		} catch (ZinggClientException e) {
			LOG.warn("Expected exception received: NoSuchFileException");
		}
	}

	@Test
	public void testMatchTypeMultiple() {
			Arguments args;
            try {
                args = Arguments.createArgumentsFromJSON(getClass().getResource("../../../testArguments/configWithMultipleMatchTypes.json").getFile(), "test");
				List<MatchType> fNameMatchType = args.getFieldDefinition().get(0).getMatchType();
				assertEquals(2, fNameMatchType.size());
				assertEquals(MatchType.FUZZY, fNameMatchType.get(0));
				assertEquals(MatchType.NULL_OR_BLANK, fNameMatchType.get(1));

				
            } catch (Exception | ZinggClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
				fail("Could not read config");
            }
		
	}

	@Test
	public void testMatchTypeWrong() {
			Arguments args;
            try {
                args = Arguments.createArgumentsFromJSON(getClass().getResource("../../../testArguments/configWithMultipleMatchTypesUnsupported.json").getFile(), "test");
				//List<MatchType> fNameMatchType = args.getFieldDefinition().get(0).getMatchType();
				fail("config had error, should have flagged");
				
            } catch (Exception | ZinggClientException e) {
//                e.printStackTrace();
            }
			
			
		
	}
	
}
