package io.dexi.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
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
 *  This class contains functionality for reading a "hierarchical" configuration file containing sections like the
 *  examples below.
 *
 *  Specifically it is possible to get the configuration needed to connect to the Dexi API.
 *
 *  <b>YAML example</b>
 *  <pre>
 *  {@code
 *     dexi:
 *       baseUrl: http://localhost:3000/api/
 *       apiKey: super-secret-key
 *       account: dexi-developer-account
 *     google:
 *       ...
 *  }
 *  </pre>
 *
 *  <b>JSON example</b>
 *  <pre>
 *  {@code
 *      {
 *          "dexi": {
 *              "baseUrl": "http://localhost:3000/api/",
 *              "apiKey": "super-secret-key",
 *              "account": "dexi-developer-account"
 *          },
 *          "google": {
 *              "...
 *          }
 *      }
 *  }
 *  </pre>
 *
 *  The file can be read from a local disk or from a URL. The class also supports reading environment variables whose
 *  names start with "DEXI_APP_".
 *
 *  Configuration is read as follows:
 *  <ol>
 *      <li>If an environment variable or system property named {@code DEXI_APP_CREDENTIALS_NAME}
 *          is set, check its value:
 *          <ul>
 *              <li>If its value is a URL, the configuration file is read from that URL.</li>
 *                  <ul>
 *                      <li>The URL most point to a YAML (.yml) file.</li>
 *                  </ul>
 *              <li>Otherwise, the configuration file is read from the local disk.</li>
 *                  <ul>
 *                      <li>Supported file formats are YAML (.yml), JSON (.json), XML (.xml) and INI (.ini).</li>
 *                  </ul>
 *          </ul>
 *      <li>If {@code DEXI_APP_CREDENTIALS_NAME} is not set, read a default local configuration file in
 *          {@code ~/.dexi/configuration.yml}.</li>
 *      <li>Read any {@code DEXI_APP_} environment variable. The format is:
 *          {@code DEXI_APP_&lt;section>_&lt;key> = &lt;value>}.</li>
 *  </ol>
 *
 *  Values for duplicate keys within sections are overwritten by later keys.
 *
 */
public class DexiConfig {

    public static final String ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME = "DEXI_APP_CREDENTIALS";
    public static final String DEFAULT_BASE_URL = "https://api.dexi.io/";

    public static final String CONFIG_KEY_BASE_URL = "baseUrl";
    public static final String CONFIG_KEY_API_KEY = "apiKey";
    public static final String CONFIG_KEY_ACCOUNT = "account";

    private static final String ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX = "DEXI_APP_";

    private static String defaultLocalConfigFile = System.getProperty("user.home") + "/.dexi/configuration.yml";
    private static Properties properties = new Properties();

    private static void readEnvironment() {
        Map<String, String> env = System.getenv();
        Set<String> envKeys = env.keySet();
        if (envKeys.size() > 0) {
            for (String envKey : envKeys) {
                if (envKey.startsWith(ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX) && !envKey.startsWith(ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME)) {
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

    private static <T extends FileBasedConfiguration> Configuration getConfigurationFile(String fileLocation, Class<T> filedBasedClazz) throws ConfigurationException, URISyntaxException, MalformedURLException {
        T configuration = null;

        Parameters parameters = new Parameters();
        PropertiesBuilderParameters properties = parameters.properties();
        FileBasedConfigurationBuilder<T> builder = new ReloadingFileBasedConfigurationBuilder<>(filedBasedClazz);

        URI uri = new URI(fileLocation);
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme)) {
            builder = builder.configure(properties.setURL(uri.toURL()));
            configuration = builder.getConfiguration();
        } else {
            URL localFileURL = DexiConfig.class.getResource(fileLocation);
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
        String dexiAppConfigUrl = System.getenv(ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME);
        if (StringUtils.isEmpty(dexiAppConfigUrl)) {
            dexiAppConfigUrl = System.getProperty(ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME);
        }

        if (StringUtils.isNotEmpty(dexiAppConfigUrl)) {
            Configuration ymlConfigurationURL = getConfigurationFile(dexiAppConfigUrl, YAMLConfiguration.class);
            addConfigurationToProperties(ymlConfigurationURL);
        }
    }

    private static void readLocalConfiguration() throws MalformedURLException, ConfigurationException, URISyntaxException {
        String fileExtension = defaultLocalConfigFile.substring(defaultLocalConfigFile.lastIndexOf(".") + 1);

        Class<? extends FileBasedConfiguration> configurationClass;
        switch (fileExtension) {
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
                throw new IllegalArgumentException("Unsupported file extension " + fileExtension);
        }

        Configuration ymlConfigurationLocal = getConfigurationFile(defaultLocalConfigFile, configurationClass);
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
        DexiConfig.defaultLocalConfigFile = localConfigFile;
    }

    public static String getBaseUrl() {
        return properties.getProperty(CONFIG_KEY_BASE_URL, DEFAULT_BASE_URL);
    }

    public static String getApiKey() {
        return properties.getProperty(CONFIG_KEY_API_KEY);
    }

    public static String getAccount() {
        return properties.getProperty(CONFIG_KEY_ACCOUNT);
    }
}
