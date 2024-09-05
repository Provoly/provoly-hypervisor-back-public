package com.provoly.comment;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import com.provoly.DatabaseReader;

@ApplicationScoped
public class CommentDatabaseReader extends DatabaseReader {

    protected CommentDatabaseReader(EntityManager em) {
        super(em);
    }

    public void saveComment(Comment comment) {
        em.persist(comment);
    }

    public Optional<Comment> getCommentById(UUID id) {
        return Optional.ofNullable(em.find(Comment.class, id));
    }
}
