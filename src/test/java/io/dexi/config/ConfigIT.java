package io.dexi.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfigIT {

    @After
    public void clearProperties() {
        DexiConfig.getProperties().clear();
    }

    @Test
    public void testReadingLocalFiles() throws ConfigurationException, URISyntaxException, MalformedURLException {
        DexiConfig.setLocalConfigFile("/test-config.yml");

        DexiConfig.load();

        Properties properties = DexiConfig.getProperties();
        assertEquals(4, properties.keySet().size());
    }

    @Test
    public void testReadingFileFromURL() throws ConfigurationException, URISyntaxException, MalformedURLException {
        DexiConfig.setLocalConfigFile("/non-existing-local-config-file.json");
        System.setProperty(DexiConfig.ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME, "http://config.dexi.io:1080/dexi-config/test/ini/apps/app-service-s3.yml");

        DexiConfig.load();

        Properties properties = DexiConfig.getProperties();
        assertEquals(3, properties.keySet().size());
    }

    @Test
    public void testReadingLocalFilesAndFileFromURL() throws ConfigurationException, MalformedURLException, URISyntaxException {
        DexiConfig.setLocalConfigFile("/test-config.json");
        System.setProperty(DexiConfig.ENVIRONMENT_VARIABLE_DEXI_APP_CREDENTIALS_NAME, "http://config.dexi.io:1080/dexi-config/test/ini/apps/app-service-s3.yml");

        DexiConfig.load();

        Properties properties = DexiConfig.getProperties();
        assertEquals(4, properties.keySet().size());
    }

}
