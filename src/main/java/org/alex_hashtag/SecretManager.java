package org.alex_hashtag;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SecretManager {
    private static final Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream(".env"));
        } catch (IOException e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
