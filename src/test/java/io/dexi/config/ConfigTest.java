package io.dexi.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

    @After
    public void clearProperties() {
        Config.getProperties().clear();
    }

    @Test
    public void testReadingLocalFiles() throws ConfigurationException, URISyntaxException, MalformedURLException {
        Config.setLocalConfigFile("/test-config.yml");

        Config.load();

        Properties properties = Config.getProperties();
        assertEquals(4, properties.keySet().size());
    }

    @Test
    public void testReadingFileFromURL() throws ConfigurationException, URISyntaxException, MalformedURLException {
        Config.setLocalConfigFile("/non-existing-local-config-file.json");
        System.setProperty(Config.DEXI_APP_CONFIG_URL, "http://config.dexi.io:1080/dexi-config/test/ini/apps/app-service-s3.yml");

        Config.load();

        Properties properties = Config.getProperties();
        assertEquals(3, properties.keySet().size());
    }

    @Test
    public void testReadingLocalFilesAndFileFromURL() throws ConfigurationException, MalformedURLException, URISyntaxException {
        Config.setLocalConfigFile("/test-config.json");
        System.setProperty(Config.DEXI_APP_CONFIG_URL, "http://config.dexi.io:1080/dexi-config/test/ini/apps/app-service-s3.yml");

        Config.load();

        Properties properties = Config.getProperties();
        assertEquals(4, properties.keySet().size());
    }

}
