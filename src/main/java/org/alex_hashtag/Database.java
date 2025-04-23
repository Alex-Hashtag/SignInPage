package org.alex_hashtag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Database {
    private static final String URL = SecretManager.get("DATABASE_URL");
    private static final String USER = SecretManager.get("DATABASE_USERNAME");
    private static final String PASS = SecretManager.get("DATABASE_PASSWORD"); // Change this
    private static final Connection connection;

    static
    {
        try
        {
            connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(){
        return connection;
    }

    public static String generateSalt() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String text = password + salt;
            byte[] hashBytes = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
