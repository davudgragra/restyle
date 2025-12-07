package com.example.restyle;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String password;
    private String avatarUrl;
    private double rating;
    private String location;
    private String createdAt;
    private boolean isAdmin;     // <-- НОВОЕ ПОЛЕ
    private boolean isBlocked;   // <-- НОВОЕ ПОЛЕ

    public User() {}

    // Конструктор для старых записей (без isAdmin и isBlocked)
    public User(String id, String name, String email, String password, String avatarUrl,
                double rating, String location, String createdAt) {
        this(id, name, email, password, avatarUrl, rating, location, createdAt, false, false);
    }

    // Полный конструктор
    public User(String id, String name, String email, String password, String avatarUrl,
                double rating, String location, String createdAt, boolean isAdmin, boolean isBlocked) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.rating = rating;
        this.location = location;
        this.createdAt = createdAt;
        this.isAdmin = isAdmin;
        this.isBlocked = isBlocked;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Новые геттеры и сеттеры
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
}