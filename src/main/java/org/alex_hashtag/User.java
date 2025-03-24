package org.alex_hashtag;

public class User {
    public int id;
    public String username;
    public String salt;
    public String hash;
    public String role;

    public User(int id, String username, String salt, String hash, String role) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.hash = hash;
        this.role = role;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}


