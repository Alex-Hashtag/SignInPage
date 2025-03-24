package org.alex_hashtag;

import java.time.LocalDateTime;

public class User {
    public int id;
    public String username;
    public String salt;
    public String hash;
    public String role;
    public LocalDateTime registrationDate;

    public User(int id, String username, String salt, String hash, String role, LocalDateTime registrationDate) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.hash = hash;
        this.role = role;
        this.registrationDate = registrationDate;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}


