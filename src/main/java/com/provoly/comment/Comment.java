package com.provoly.comment;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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

    private String creator;

    public Comment() {
    }

    public Comment(UUID id, String message, String creator) {
        this.id = id;
        this.message = message;
        this.creator = creator;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
