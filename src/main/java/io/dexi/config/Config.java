package io.dexi.config;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *  This class supports reading configuration files like:
 *
 *  <b>YAML example</b>
 *  <code>
 *     dexi:
 *          base_url: http://localhost:3000/api/
 *          apikey: super-secret-key
 *  </code>
 *
 *  <b>JSON example</b>
 *  <code>
 *      {
 *          "dexi": {
 *              "base_url": "http://localhost:3000/api/",
 *              "apikey": "super-secret-key"
 *          }
 *      }
 *  </code>
 *
 *  Configuration is read in the following order:
 *  <ol>
 *      <li>If the environment variable named "DEXI_APP_CONFIG_URL_YML" is set, read a YAML (.yml) file from the
 *      specified URL.</li>
 *      <li>On the local disk, read a file named "dexi.&lt;app-name>" placed in "~/.dexi"
 *          <ul>
 *              <li>Supported file formats are YAML (.yml), JSON (.json), XML (.xml), Properties (.properties) and
 *              INI (.ini).</li>
 *          </ul>
 *      </li>
 *      <li>Read any environment variables with the following format: "DEXI_APP_&lt;section>_&lt;key> = &lt;value>"</li>
 *  </ol>
 *
 *  Configuration is read in the order specified above. Only the last value for a duplicate key within a section is
 *  stored.
 *
 */
public class Config {

    public static final String DEXI_APP_CONFIG_URL = "DEXI_APP_CONFIG_URL";

    private static final String ENVIRONMENT_VARIABLE_PREFIX = "DEXI_APP_";

    private static Properties properties = new Properties();

    /*
    private static File findConfigurationFile(String fileName) {
        String fullFileName = System.getProperty("user.home") + "/.dexi/" + fileName;
        File configurationFile = new File(fullFileName);
        if (configurationFile.exists()) {
            return configurationFile;
        }

        throw new RuntimeException("Could not find configuration file: " + fileName);
    }
    */

    /*
    private static String resolveEnvironmentVariables(final String value) {
        String[] tokens = value.split("%");
        if (tokens.length == 1) {
            return value;
        }

        // iterate over the tokens
        String result = new String(value);
        for (String token : tokens) {
            if (token == null || token.trim().length() == 0) {
                continue;
            }

            String envValue = System.getenv(token);
            if (envValue == null) {
                continue;
            }

            result = result.replaceAll("%" + token + "%", envValue);
        }

        return result;
    }
    */

    /*
    private static void readIniConfiguration(HierarchicalINIConfiguration configuration) {
        if (configuration != null) {
            for (String section : configuration.getSections()) {
                Iterator keys = configuration.getSection(section).getKeys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = configuration.getSection(section).getString(key);

                    value = resolveEnvironmentVariables(value);

                    properties.setProperty(
                            String.format("%s.%s", section, key),
                            value);
                }
            }
        }
    }
    */

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

    private static <T extends FileBasedConfiguration> Configuration getConfigurationFile(String uri, Class<T> clazz) throws ConfigurationException {
        T configuration = null;

        URL url = Config.class.getResource(uri);
        if (url != null) {
            Parameters parameters = new Parameters();
            FileBasedConfigurationBuilder<T> builder;
            if (uri.startsWith("http")) {
                builder = new FileBasedConfigurationBuilder<>(clazz).configure(parameters.properties().setURL(url));
            } else {
                builder = new FileBasedConfigurationBuilder<>(clazz).configure(parameters.properties().setFileName(url.getFile()));
            }
            configuration = builder.getConfiguration();
        }

        return configuration;
    }

    private static void addConfigurationToProperties(Configuration configuration) {
        if (configuration != null) {
            Iterator<String> keys = configuration.getKeys();
            while (keys.hasNext()) {
                String keyWithSection = keys.next();
                //String section = keyWithSection.substring(0, keyWithSection.indexOf("."));
                //String key = keyWithSection.substring(keyWithSection.indexOf(".") + 1);

                String existingValue = properties.getProperty(keyWithSection);
                if (existingValue == null) {
                    String value = configuration.getString(keyWithSection);
                    properties.put(keyWithSection, value);
                }
            }
        }
    }

    public static void load(String appName) throws ConfigurationException {
        String dexiAppConfigUrl = System.getenv(DEXI_APP_CONFIG_URL);
        if (StringUtils.isNotEmpty(dexiAppConfigUrl)) {
            Configuration ymlConfigurationURL = getConfigurationFile(dexiAppConfigUrl, YAMLConfiguration.class);
            addConfigurationToProperties(ymlConfigurationURL);
        }

        Configuration ymlConfigurationLocal = getConfigurationFile(appName + ".yml", YAMLConfiguration.class);
        addConfigurationToProperties(ymlConfigurationLocal);

        Configuration jsonConfiguration = getConfigurationFile(appName + ".json", JSONConfiguration.class);
        addConfigurationToProperties(jsonConfiguration);

        Configuration xmlConfiguration = getConfigurationFile(appName + ".xml", XMLConfiguration.class);
        addConfigurationToProperties(xmlConfiguration);

        Configuration propertiesConfiguration = getConfigurationFile(appName + ".properties", PropertiesConfiguration.class);
        addConfigurationToProperties(propertiesConfiguration);

        Configuration iniConfiguration = getConfigurationFile(appName + ".ini", INIConfiguration.class);
        addConfigurationToProperties(iniConfiguration);

        readEnvironment();
    }

    public static Properties getProperties() {
        return properties;
    }
}
