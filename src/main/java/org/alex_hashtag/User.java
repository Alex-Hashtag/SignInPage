package org.alex_hashtag;

import java.time.LocalDateTime;
public class User {
    public int id;
    public String username;
    public String salt;
    public String hash;
    public String role;
    public java.time.LocalDateTime registrationDate;
    public byte[] profileImage;
    public String profileBio;

    public User(int id, String username, String salt, String hash, String role,
                java.time.LocalDateTime registrationDate, byte[] profileImage, String profileBio) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.hash = hash;
        this.role = role;
        this.registrationDate = registrationDate;
        this.profileImage = profileImage;
        this.profileBio = profileBio;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}



