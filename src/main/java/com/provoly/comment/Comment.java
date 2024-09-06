package com.provoly.comment;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import com.provoly.user.User;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class Comment {
    @Id
    private UUID id;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant creationDate;

    @UpdateTimestamp
    private Instant lastModificationDate;

    private String message;

    @ManyToOne
    private User user;

    public Comment() {
    }

    public Comment(UUID id, String message, User user) {
        this.id = id;
        this.message = message;
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }
}
