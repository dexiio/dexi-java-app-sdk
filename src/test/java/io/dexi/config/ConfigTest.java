package io.dexi.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

    @Test
    public void testLocalFiles() throws ConfigurationException {
        Config.load("/test-app");
        Properties properties = Config.getProperties();
        assertEquals(3, properties.keySet().size());
    }

    @Test
    public void testURL() throws ConfigurationException {
        System.setProperty(Config.DEXI_APP_CONFIG_URL, "http://config.dexi.io:1080/test/ini/apps/app-service-s3.yml");
        Config.load("/test-app");

        // TODO: make assertions on the number of keys and values
    }

    @Test
    public void testLocalFilesAndURL() throws ConfigurationException {
        // TODO: implement
    }

}
