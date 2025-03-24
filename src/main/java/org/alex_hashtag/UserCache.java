package org.alex_hashtag;


import java.util.HashMap;
import java.util.Map;


public class UserCache {
    public static Map<String, User> cache = new HashMap<>();

    public static void loadUsers() {
        try (var conn = Database.getConnection();
             var stmt = conn.prepareStatement("SELECT * FROM users");
             var rs = stmt.executeQuery()) {

            cache.clear();
            while (rs.next()) {
                var user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("salt"),
                        rs.getString("hash"),
                        rs.getString("role"),
                        rs.getTimestamp("registration_date").toLocalDateTime() // 🆕
                );
                cache.put(user.username, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}