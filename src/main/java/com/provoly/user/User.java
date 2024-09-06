package com.provoly.user;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "provoly_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID subject;
    private String username;
    private String fullName;

    public User() {
    }

    public User(UUID subject, String username, String fullName) {
        this.subject = subject;
        this.username = username;
        this.fullName = fullName;
    }

    public User(UUID id, UUID subject, String username, String fullName) {
        this.id = id;
        this.subject = subject;
        this.username = username;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSubject() {
        return subject;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }
}
