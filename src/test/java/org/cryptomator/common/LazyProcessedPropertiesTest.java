package org.cryptomator.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Properties;

public class LazyProcessedPropertiesTest {

	LazyProcessedProperties inTest;

	@Nested
	public class Processing {

		@ParameterizedTest
		@DisplayName("Test template replacement")
		@CsvSource(value = {"unknown.@{testToken}.test, unknown.@{testToken}.test", //
				"@{only*words*digits*under_score\\},@{only*words*digits*under_score\\}", //
				"C:\\Users\\@{appdir}\\dir, C:\\Users\\foobar\\dir", //
				"@{@{appdir}},@{foobar}", //
				"Longer @{appdir} text with @{appdir}., Longer foobar text with foobar."})
		public void test(String propertyValue, String expected) {
			LazyProcessedProperties inTest = new LazyProcessedProperties(new Properties(), Map.of("APPDIR", "foobar"));
			var result = inTest.process(propertyValue);
			Assertions.assertEquals(result, expected);
		}

		@Test
		@DisplayName("@{userhome} is replaced with the user home directory")
		public void testPropSubstitutions() {
			var props = new Properties();
			props.setProperty("user.home", "OneUponABit");

			inTest = new LazyProcessedProperties(props, Map.of());
			var result = inTest.process("@{userhome}");
			Assertions.assertEquals(result, "OneUponABit");
		}

		@DisplayName("Other keywords are replaced accordingly")
		@ParameterizedTest(name = "Token \"{0}\" replaced with content of {1}")
		@CsvSource(value = {"appdir, APPDIR, foobar", "appdata, APPDATA, bazbaz", "localappdata, LOCALAPPDATA, boboAlice"})
		public void testEnvSubstitutions(String token, String envName, String expected) {
			inTest = new LazyProcessedProperties(new Properties(), Map.of(envName, expected));
			var result = inTest.process("@{" + token + "}");
			Assertions.assertEquals(result, expected);
		}

	}


	@Nested
	public class GetProperty {

		@Test
		@DisplayName("Undefined properties are not processed")
		public void testNoProcessingOnNull() {
			inTest = Mockito.spy(new LazyProcessedProperties(new Properties(), Map.of()));

			var result = inTest.getProperty("some.prop");
			Assertions.assertNull(result);
			Mockito.verify(inTest, Mockito.never()).process(Mockito.anyString());
		}

		@ParameterizedTest
		@DisplayName("Properties not starting with \"cryptomator.\" are not processed")
		@ValueSource(strings = {"example.foo","cryptomatorSomething.foo","org.cryptomator.foo","cryPtoMAtor.foo"})
		public void testNoProcessingOnNotCryptomator(String propKey) {
			var props = new Properties();
			props.setProperty(propKey, "someValue");
			inTest = Mockito.spy(new LazyProcessedProperties(props, Map.of()));

			var result = inTest.getProperty("some.prop");
			Assertions.assertNull(result);
			Mockito.verify(inTest, Mockito.never()).process(Mockito.anyString());
		}

		@Test
		@DisplayName("Non-null property starting with \"cryptomator.\" is processed")
		public void testProcessing() {
			var props = new Properties();
			props.setProperty("cryptomator.prop", "someValue");
			inTest = Mockito.spy(new LazyProcessedProperties(props, Map.of()));
			Mockito.doReturn("someValue").when(inTest).process(Mockito.anyString());

			inTest.getProperty("cryptomator.prop");
			Mockito.verify(inTest).process("someValue");
		}
	}

}
