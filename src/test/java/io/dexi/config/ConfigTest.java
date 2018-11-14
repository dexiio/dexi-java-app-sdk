package io.dexi.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

    @Test
    public void testReadingLocalFiles() throws ConfigurationException {
        Config.load("/test-app");
        Properties properties = Config.getProperties();
        assertEquals(3, properties.keySet().size());
    }

    @Test
    public void testReadingFileFromURL() throws ConfigurationException {
        System.setProperty(Config.DEXI_APP_CONFIG_URL, "http://config.dexi.io:1080/dexi-config/test/ini/apps/app-service-s3.yml");
        Config.load("/app-service-s3");
        Properties properties = Config.getProperties();
        assertEquals(3, properties.keySet().size());
    }

    @Test
    public void testReadingLocalFilesAndFileFromURL() throws ConfigurationException {
        // TODO: implement
    }

}
