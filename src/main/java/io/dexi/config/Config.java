package io.dexi.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
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
 *  This class supports reading a "hierarchical" configuration file with sections like:
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
 *  The file can be read from a local disk or from a URL. The class also supports reading environment variables whose
 *  names start with "DEXI_APP_".
 *
 *  Configuration is read in the following order:
 *  <ol>
 *      <li>A local configuration file as specified by the "LOCAL_CONFIG_FILE" parameter. The default is
 *          "~/.dexi/my-app.yml".
 *          <ul>
 *              <li>Supported file formats are YAML (.yml), JSON (.json), XML (.xml) and INI (.ini).</li>
 *          </ul>
 *      </li>
 *      <li>If an environment variable or system property named "DEXI_APP_CONFIG_URL_YML" is set, read a YAML (.yml)
 *          file from the specified URL.</li>
 *      <li>Read any "DEXI_APP_" environment variable. The format is: "DEXI_APP_&lt;section>_&lt;key> = &lt;value>".</li>
 *  </ol>
 *
 *  Values for duplicate keys within sections are overwritten by later keys.
 *
 */
public class Config {

    public static final String DEXI_APP_CONFIG_URL = "DEXI_APP_CONFIG_URL";

    private static String LOCAL_CONFIG_FILE = System.getProperty("user.home") + "/.dexi/my-app.yml";
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

    private static void readLocalConfiguration() throws MalformedURLException, ConfigurationException, URISyntaxException {
        String extension = LOCAL_CONFIG_FILE.substring(LOCAL_CONFIG_FILE.lastIndexOf(".") + 1);

        Class<? extends FileBasedConfiguration> configurationClass;
        switch (extension) {
            case "yml":
                configurationClass = YAMLConfiguration.class;
                break;
            case "json":
                configurationClass = YAMLConfiguration.class;
                break;
            case "xml":
                configurationClass = YAMLConfiguration.class;
                break;
            case "ini":
                configurationClass = YAMLConfiguration.class;
                break;
            default:
                throw new IllegalArgumentException("Unsupported file extension " + extension);
        }

        Configuration ymlConfigurationLocal = getConfigurationFile(LOCAL_CONFIG_FILE, configurationClass);
        addConfigurationToProperties(ymlConfigurationLocal);
    }

    public static void load() throws ConfigurationException, URISyntaxException, MalformedURLException {
        readLocalConfiguration();

        getConfigurationFromURL();

        readEnvironment();
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setLocalConfigFile(String localConfigFile) {
        LOCAL_CONFIG_FILE = localConfigFile;
    }

}
