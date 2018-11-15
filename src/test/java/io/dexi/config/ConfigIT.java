package io.dexi.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;

import static io.dexi.config.DexiConfig.ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX;
import static org.junit.Assert.assertEquals;

public class ConfigIT {

    @After
    public void clearProperties() {
        DexiConfig.getProperties().clear();
    }

    @Test
    public void testReadingLocalFile() throws ConfigurationException, URISyntaxException, MalformedURLException {
        System.setProperty(DexiConfig.ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME, "/test-config.yml");

        DexiConfig.load();

        Properties properties = DexiConfig.getProperties();
        assertEquals(4, properties.keySet().size());
    }

    @Test
    public void testReadingFileFromURL() throws ConfigurationException, URISyntaxException, MalformedURLException {
        System.setProperty(DexiConfig.ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME, "http://config.dexi.io:1080/dexi-config/test/ini/apps/app-service-s3.yml");

        DexiConfig.load();

        Properties properties = DexiConfig.getProperties();
        assertEquals(3, properties.keySet().size());
    }

    @Test
    public void testDuplicateKeysAreOverwritten() throws ConfigurationException, MalformedURLException, URISyntaxException {
        System.setProperty(DexiConfig.ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME, "/test-config.json");

        String section = "dexi";

        String baseUrlKeyWithSection = section + "_" + "baseUrl";
        String baseUrlPropertyName = ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX + baseUrlKeyWithSection;
        String baseUrlPropertyValue = "http://localhost:4000/api/";
        System.setProperty(baseUrlPropertyName, baseUrlPropertyValue);

        String accountKeyWithSection = section + "_" + "account";
        String accountPropertyName = ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX + accountKeyWithSection;
        String accountPropertyValue = "another-dexi-developer-account";
        System.setProperty(accountPropertyName, accountPropertyValue);

        String apiKeyWithSection = section + "_" + "apiKey";
        String apiKeyPropertyName = ENVIRONMENT_VARIABLE_DEXI_APP_PREFIX + apiKeyWithSection;
        String apiKeyPropertyValue = "another-secret-key";
        System.setProperty(apiKeyPropertyName, apiKeyPropertyValue);

        DexiConfig.load();

        Properties properties = DexiConfig.getProperties();
        Set<Object> keys = properties.keySet();
        assertEquals(4, keys.size());

        String baseUrlActual = properties.getProperty(section + "." + "baseUrl");
        assertEquals(baseUrlPropertyValue, baseUrlActual);

        String accountActual = properties.getProperty(section + "." + "account");
        assertEquals(accountPropertyValue, accountActual);

        String apiKeyActual = properties.getProperty(section + "." + "apiKey");
        assertEquals(apiKeyPropertyValue, apiKeyActual);
    }

}
