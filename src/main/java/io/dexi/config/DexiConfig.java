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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  This class contains functionality for reading a "hierarchical" configuration file containing sections like the
 *  examples below.
 *
 *  Specifically it can be used to get the configuration needed to connect to the Dexi API.
 *
 *  <b>YAML example</b>
 *  <pre>
 *  {@code
 *     dexi:
 *       baseUrl: http://localhost:3000/api/
 *       apiKey: super-secret-key
 *       account: dexi-developer-account-id
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
 *              "account": "dexi-developer-account-id"
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
 *      <li>If an environment variable or system property named {@code DEXI_APP_CREDENTIALS}
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
 *      <li>If {@code DEXI_APP_CREDENTIALS} is not set, read a default local configuration file in
 *          {@code ~/.dexi/configuration.yml}.</li>
 *      <li>Read any {@code DEXI_APP_} system property or environment variable. The format is:
 *          {@code DEXI_APP_<section>_<key> = <value>}.
 *          <ul>
 *              <li>Example: {@code DEXI_APP_dexi_account = my-other-account}</li>
 *          </ul>
 *      </li>
 *  </ol>
 *
 *  Values for duplicate keys within sections are overwritten by later keys.
 *
 */
public class DexiConfig {

    public static final String DEFAULT_BASE_URL = "https://api.dexi.io/";
    public static final String ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME = "DEXI_APP_CREDENTIALS";
    public static final String ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX = "DEXI_APP_";

    public static final String CONFIG_KEY_BASE_URL = "baseUrl";
    public static final String CONFIG_KEY_ACCOUNT = "account";
    public static final String CONFIG_KEY_API_KEY = "apiKey";

    private static String DEFAULT_CONFIG_FILE = System.getProperty("user.home") + "/.dexi/configuration.yml";
    private static Properties properties = new Properties();
    private static final Pattern urlPattern = Pattern.compile("https?://");

    private static void readEnvironment() {
        Properties systemProperties = System.getProperties();
        Map<String, String> environment = System.getenv();
        systemProperties.putAll(environment);

        Set<Object> keys = systemProperties.keySet();
        if (keys.size() > 0) {
            for (Object propertyKey : keys) {
                String propertyKeyString = String.valueOf(propertyKey);
                if (propertyKeyString.startsWith(ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX) && !propertyKeyString.startsWith(ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME)) {
                    String envKeyWithoutPrefix = propertyKeyString.substring(ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX.length());
                    if (envKeyWithoutPrefix.indexOf("_") == -1) {
                        continue;
                    }

                    String section = envKeyWithoutPrefix.substring(0, envKeyWithoutPrefix.indexOf("_"));
                    String key = envKeyWithoutPrefix.substring(envKeyWithoutPrefix.indexOf("_") + 1);
                    String value = (String) systemProperties.get(propertyKeyString);

                    String keyWithSection = String.format("%s.%s", section, key);
                    DexiConfig.properties.setProperty(keyWithSection, value);
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
        if (isUrl(fileLocation)) {
            builder = builder.configure(properties.setURL(uri.toURL()));
            configuration = builder.getConfiguration();
        } else {
            URL localFileURL;
            if (uri.isAbsolute()) {
                localFileURL = new URL(uri.toString());
            } else {
                localFileURL = DexiConfig.class.getResource(fileLocation);
            }

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

    private static void getConfigurationFromURL(String url) throws MalformedURLException, ConfigurationException, URISyntaxException {
        Configuration ymlConfigurationURL = getConfigurationFile(url, YAMLConfiguration.class);
        addConfigurationToProperties(ymlConfigurationURL);
    }

    private static void readLocalConfiguration(String fileLocation) throws MalformedURLException, ConfigurationException, URISyntaxException {
        String fileExtension = fileLocation.substring(fileLocation.lastIndexOf(".") + 1);

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

        Configuration ymlConfigurationLocal = getConfigurationFile(fileLocation, configurationClass);
        addConfigurationToProperties(ymlConfigurationLocal);
    }

    private static boolean isUrl(String fileLocation) {
        Matcher urlMatcher = urlPattern.matcher(fileLocation);
        return urlMatcher.find();
    }

    public static synchronized void load() throws ConfigurationException, URISyntaxException, MalformedURLException {
        String fileLocation = System.getenv(ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME);
        if (StringUtils.isEmpty(fileLocation)) {
            fileLocation = System.getProperty(ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME);
        }

        if (StringUtils.isNotEmpty(fileLocation)) {
            if (isUrl(fileLocation)) {
                getConfigurationFromURL(fileLocation);
            } else {
                readLocalConfiguration(fileLocation);
            }
        } else {
            readLocalConfiguration(DEFAULT_CONFIG_FILE);
        }

        readEnvironment();
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getBaseUrl() {
        return properties.getProperty(CONFIG_KEY_BASE_URL, DEFAULT_BASE_URL);
    }

    public static String getAccount() {
        return properties.getProperty(CONFIG_KEY_ACCOUNT);
    }

    public static String getApiKey() {
        return properties.getProperty(CONFIG_KEY_API_KEY);
    }

}
