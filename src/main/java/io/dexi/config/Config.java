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
 *  This class supports reading a "hierarchical" configuration file with sections like:
 *
 *  <b>YAML example</b>
 *  <pre>
 *  {@code
 *     dexi:
 *       baseUrl: http://localhost:3000/api/
 *       apiKey: super-secret-key
 *       account: dexi-developer-account
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
 *          }
 *      }
 *  }
 *  </pre>
 *
 *  The file can be read from a local disk or from a URL. The class also supports reading environment variables whose
 *  names start with "DEXI_APP_".
 *
 *  Configuration is read in the following order:
 *  <ol>
 *      <li>A local configuration file as specified by the {@code defaultLocalConfigFile} parameter. The default is
 *          {@code ~/.dexi/configuration.yml}.
 *          <ul>
 *              <li>Supported file formats are YAML (.yml), JSON (.json), XML (.xml) and INI (.ini).</li>
 *          </ul>
 *      </li>
 *      <li>If an environment variable or system property named {@code DEXI_APP_CONFIG_URL_YML} is set, read a YAML (.yml)
 *          file from the specified URL.</li>
 *      <li>Read any {@code DEXI_APP_} environment variable. The format is: {@code DEXI_APP_&lt;section>_&lt;key> = &lt;value>}.</li>
 *  </ol>
 *
 *  Values for duplicate keys within sections are overwritten by later keys.
 *
 */
public class Config {

    public static final String DEXI_APP_CONFIG_URL = "DEXI_APP_CONFIG_URL";
    public static final String DEFAULT_BASE_URL = "https://api.dexi.io/";

    private static final String DEXI_APP_ENVIRONMENT_VARIABLE_PREFIX = "DEXI_APP_";

    public static final String CONFIG_KEY_BASE_URL = "baseUrl";
    public static final String CONFIG_KEY_API_KEY = "apiKey";
    public static final String CONFIG_KEY_ACCOUNT = "account";

    private static String defaultLocalConfigFile = System.getProperty("user.home") + "/.dexi/configuration.yml";
    private static Properties properties = new Properties();

    private static void readEnvironment() {
        Map<String, String> env = System.getenv();
        Set<String> envKeys = env.keySet();
        if (envKeys.size() > 0) {
            for (String envKey : envKeys) {
                if (envKey.startsWith(DEXI_APP_ENVIRONMENT_VARIABLE_PREFIX) && !envKey.startsWith(DEXI_APP_CONFIG_URL)) {
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
        Config.defaultLocalConfigFile = localConfigFile;
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
