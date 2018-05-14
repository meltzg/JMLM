package org.meltzg;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TestConfig {
    private static Properties props;

    private static void readProps() throws IOException {
        props = new Properties();
        props.load(new FileReader("./src/test/resources/test-props.properties"));
    }

    public static Properties getProps() throws IOException {
        if (props == null) {
            readProps();
        }

        return props;
    }
}