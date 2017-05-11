package com.github.pfrank13.config;

import com.google.common.collect.ImmutableMap;

import com.github.pfrank13.config.Environment;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * This suite's test idea is that all keys are actually present in myProperties.properties and proves out that overriding
 * occurs properly.  Also, there are keys added in System properties to verify the fallback works for System.getProperty
 * related calls and also it's overriden properly, e.g. the RUNTIME property retrieved isn't from system properties.
 * Environment properties from System.getenv() are immutable so I'm guessing JAVA_HOME is there, this is a smell
 */
public class EnvironmentTest {
  private static final Map<PropertyKey, String> EXPECTED;
  private Environment environment;

  static{
    EXPECTED = ImmutableMap.of(PropertyKey.RUNTIME, "I am runtime.property set at runtime",
        PropertyKey.SYSTEM, "I am system.property in System Properties",
        PropertyKey.ENVIRONMENT, System.getenv("JAVA_HOME"),
        PropertyKey.PROPERTIES_FILE, "I am propertiesFile.property in myProperties.properties");
  }

  @BeforeClass
  public static void setUpClass(){
    final String javaHomeValue = System.getenv(PropertyKey.ENVIRONMENT.getValue());
    if(javaHomeValue == null || javaHomeValue.length() ==  0){
      throw new IllegalStateException("For this test to work a JAVA_HOME needs to be set");
    }

    System.setProperty(PropertyKey.RUNTIME.getValue(), "I am runtime.property in System Properties");
    System.setProperty(PropertyKey.SYSTEM.getValue(), "I am system.property in System Properties");
  }

  @Before
  public void setUp(){
    environment = new Environment("/myProperties.properties");
  }

  @Test
  public void testGetRuntimePropertyOverridesPropertiesFile(){
    //GIVEN
    environment.addProperty(PropertyKey.RUNTIME.getValue(), "I am runtime.property set at runtime");

    //WHEN THEN
    assertPropertyKey(PropertyKey.RUNTIME);
  }

  @Test
  public void testGetSystemPropertyOverridePropertiesFile(){
    assertPropertyKey(PropertyKey.SYSTEM);
  }

  @Test
  public void testGetEnvironmentVariablePropertyOverridesPropertiesFile(){
    assertPropertyKey(PropertyKey.ENVIRONMENT);
  }

  @Test
  public void testGetPropertiesFilePropertyKeyFound(){
    assertPropertyKey(PropertyKey.PROPERTIES_FILE);
  }

  @Test
  public void testRuntimeEnvironmentVariableOverride(){
    //GIVEN
    final String expected = "I'm overriding the environment variable";
    environment.addProperty(PropertyKey.ENVIRONMENT.getValue(), expected);

    //WHEN THEN
    assertPropertyKey(PropertyKey.ENVIRONMENT, expected);
  }

  @Test
  public void testRuntimeSystemPropertyVariableOverride(){
    //GIVEN
    final String expected = "I'm overriding the system variable";
    environment.addProperty(PropertyKey.SYSTEM.getValue(), expected);

    //WHEN THEN
    assertPropertyKey(PropertyKey.SYSTEM, expected);
  }

  void assertPropertyKey(final PropertyKey propertyKey){
    assertPropertyKey(propertyKey, EXPECTED.get(propertyKey));
  }

  void assertPropertyKey(final PropertyKey propertyKey, final String expectedValue){
    Assertions.assertThat(environment.getString(propertyKey.getValue())).isEqualTo(expectedValue);
  }

  enum PropertyKey{
    RUNTIME("runtime.property"),
    SYSTEM("system.property"),
    ENVIRONMENT("JAVA_HOME"),
    PROPERTIES_FILE("propertiesFile.property");

    private final String value;

    PropertyKey(final String value){
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
