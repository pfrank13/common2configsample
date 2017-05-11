package com.github.pfrank13.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.tree.OverrideCombiner;

/**
 * This only works on String values, should be clear how to flesh it out for other objects.
 */
public class Environment {
  private final PropertiesConfiguration runtimeConfigurationProperties;
  private final CombinedConfiguration combinedConfiguration; //Idiomatic Commons Configuration should use a Builder here

  public Environment(final String propertiesFileClasspathResourceId){
    //For Runtime Properties, necessary because we are using a CombinedConfiguration
    final Parameters parameters = new Parameters();
    final BasicConfigurationBuilder<PropertiesConfiguration> runtimePropertiesBuilder = new BasicConfigurationBuilder<>(PropertiesConfiguration.class).configure(parameters.basic().setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
    try {
      runtimeConfigurationProperties = runtimePropertiesBuilder.getConfiguration(); //Hold a reference to use for runtime properties
    }catch(ConfigurationException ce){
      throw new IllegalStateException(ce);
    }

    combinedConfiguration = new CombinedConfiguration(new OverrideCombiner());
    combinedConfiguration.addConfiguration(runtimeConfigurationProperties); //Runtime Properties First
    combinedConfiguration.addConfiguration(new SystemConfiguration()); //System.getProperty 2nd
    combinedConfiguration.addConfiguration(new EnvironmentConfiguration()); //System.getenv 3rd

    final FileBasedConfigurationBuilder<FileBasedConfiguration> propertiesFilePropertiesBuilder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class).configure(parameters.fileBased().setLocationStrategy(new ClasspathLocationStrategy()).setPath(propertiesFileClasspathResourceId));
    try {
      combinedConfiguration.addConfiguration(propertiesFilePropertiesBuilder.getConfiguration()); //properties file 4th
    }catch (ConfigurationException ce){
      throw new IllegalStateException(ce);
    }
  }

  /**
   * Resolution Order
   *
   * Runtime Properties
   * Java Command Line Properties (System.getProperty)
   * OS Environment Variables
   * Properties File Properties
   */
  public String getString(final String propertyName){
    return combinedConfiguration.getString(propertyName);
  }

  public void addProperty(final String propertyName, final String propertyValue){
    runtimeConfigurationProperties.addProperty(propertyName, propertyValue);
  }
}
