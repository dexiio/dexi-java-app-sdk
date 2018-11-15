package io.dexi.config;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *  This class supports reading "hierarchical" configuration files with sections like:
 *
 *  <b>YAML example</b>
 *  <code>
 *     dexi:
 *       baseUrl: http://localhost:3000/api/
 *       apiKey: super-secret-key
 *  </code>
 *
 *  <b>JSON example</b>
 *  <code>
 *      {
 *          "dexi": {
 *              "baseUrl": "http://localhost:3000/api/",
 *              "apiKey": "super-secret-key"
 *          }
 *      }
 *  </code>
 *
 *  It also supports reading environment variables with the following format: "DEXI_APP_&lt;section>_&lt;key> = &lt;value>".
 *
 *  Configuration is read in the following order:
 *  <ol>
 *      <li>On the local disk, read a file named "dexi.&lt;app-name>" placed in "~/.dexi"
 *          <ul>
 *              <li>Supported file formats are YAML (.yml), JSON (.json), XML (.xml) and INI (.ini).</li>
 *          </ul>
 *      </li>
 *      <li>If an environment variable or system property named "DEXI_APP_CONFIG_URL_YML" is set, read a YAML (.yml)
 *      file from the specified URL.</li>
 *      <li>Read any "DEXI_APP_" environment variable</li>
 *  </ol>
 *
 *  Values for duplicate keys within sections are overwritten by later keys.
 *
 */
public class Config {

    public static final String DEXI_APP_CONFIG_URL = "DEXI_APP_CONFIG_URL";

    private static final String ENVIRONMENT_VARIABLE_PREFIX = "DEXI_APP_";

    private static Properties properties = new Properties();

    private static void readEnvironment() {
        Map<String, String> env = System.getenv();
        Set<String> envKeys = env.keySet();
        if (envKeys.size() > 0) {
            for (String envKey : envKeys) {
                if (envKey.startsWith(ENVIRONMENT_VARIABLE_PREFIX) && !envKey.startsWith(DEXI_APP_CONFIG_URL)) {
                    String envKeyWithoutPrefix = envKey.substring(5).toLowerCase();
                    if (envKeyWithoutPrefix.indexOf("_") == -1) {
                        continue;
                    }

                    String section = envKeyWithoutPrefix.substring(0, envKeyWithoutPrefix.indexOf("_"));
                    String key = envKeyWithoutPrefix.substring(envKeyWithoutPrefix.indexOf("_") + 1);
                    String value = env.get(envKey);

                    String keyWithSection = String.format("%s.%s", section, key);
                    properties.setProperty(keyWithSection, value);
                }
            }
        }
    }

    // TODO: for non-tests, should only look in ~/.dexi/
    private static <T extends FileBasedConfiguration> Configuration getConfigurationFile(String fileLocation, Class<T> filedBasedClazz) throws ConfigurationException, URISyntaxException, MalformedURLException {
        T configuration = null;

        Parameters parameters = new Parameters();
        PropertiesBuilderParameters properties = parameters.properties();
        // TODO: use ReloadingFileBasedConfigurationBuilder
        FileBasedConfigurationBuilder<T> builder = new FileBasedConfigurationBuilder<>(filedBasedClazz);

        URI uri = new URI(fileLocation);
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme)) {
            builder = builder.configure(properties.setURL(uri.toURL()));
            configuration = builder.getConfiguration();
        } else {
            URL localFileURL = Config.class.getResource(fileLocation);
            if (localFileURL != null) {
                builder = builder.configure(properties.setFileName(localFileURL.getFile()));
                configuration = builder.getConfiguration();
            }
        }

        return configuration;
    }

    private static void addConfigurationToProperties(Configuration configuration) {
        if (configuration != null) {
            Iterator<String> keys = configuration.getKeys();
            while (keys.hasNext()) {
                String keyWithSection = keys.next();

                String existingValue = properties.getProperty(keyWithSection);
                if (existingValue == null) {
                    String value = configuration.getString(keyWithSection);
                    properties.put(keyWithSection, value);
                }
            }
        }
    }

    private static void getConfigurationFromURL() throws MalformedURLException, ConfigurationException, URISyntaxException {
        String dexiAppConfigUrl = System.getenv(DEXI_APP_CONFIG_URL);
        if (StringUtils.isEmpty(dexiAppConfigUrl)) {
            dexiAppConfigUrl = System.getProperty(DEXI_APP_CONFIG_URL);
        }

        if (StringUtils.isNotEmpty(dexiAppConfigUrl)) {
            Configuration ymlConfigurationURL = getConfigurationFile(dexiAppConfigUrl, YAMLConfiguration.class);
            addConfigurationToProperties(ymlConfigurationURL);
        }
    }

    private static void readLocalConfiguration(String appName) throws MalformedURLException, ConfigurationException, URISyntaxException {
        Configuration ymlConfigurationLocal = getConfigurationFile(appName + ".yml", YAMLConfiguration.class);
        addConfigurationToProperties(ymlConfigurationLocal);

        Configuration jsonConfiguration = getConfigurationFile(appName + ".json", JSONConfiguration.class);
        addConfigurationToProperties(jsonConfiguration);

        Configuration xmlConfiguration = getConfigurationFile(appName + ".xml", XMLConfiguration.class);
        addConfigurationToProperties(xmlConfiguration);

        Configuration iniConfiguration = getConfigurationFile(appName + ".ini", INIConfiguration.class);
        addConfigurationToProperties(iniConfiguration);
    }

    public static void load(String appName) throws ConfigurationException, URISyntaxException, MalformedURLException {
        readLocalConfiguration(appName);

        getConfigurationFromURL();

        readEnvironment();
    }

    public static Properties getProperties() {
        return properties;
    }

}
