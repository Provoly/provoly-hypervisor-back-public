package com.provoly.comment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.event.EventService;
import com.provoly.event.Status;

import io.quarkus.security.identity.SecurityIdentity;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CommentService {
    private final Logger logger;
    private final CommentDatabaseReader databaseReader;
    private final CommentMapper commentMapper;
    private final EventService eventService;
    private final SecurityIdentity securityIdentity;

    public CommentService(Logger logger,
            CommentDatabaseReader databaseReader,
            CommentMapper commentMapper,
            EventService eventService,
            SecurityIdentity securityIdentity) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.commentMapper = commentMapper;
        this.eventService = eventService;
        this.securityIdentity = securityIdentity;
    }

    @Transactional
    public void saveOrUpdateCommentForEvent(Integer eventId, CommentWriteDto dto) {
        logger.debugf("Save or update comment for event %d", eventId);

        var event = eventService.getEventDetails(eventId);
        if (event.getStatus() == Status.DONE) {
            throw new ForbiddenException("Event %s is done and can no longer be commented on".formatted(eventId));
        }

        databaseReader.getCommentById(dto.id()).ifPresentOrElse(
                comment -> updateComment(dto, comment, getCurrentUser()),
                () -> {
                    logger.debugf("Save comment %s", dto.id());
                    var comment = new Comment(dto.id(), dto.message(), getCurrentUser());
                    event.addComment(comment);
                    databaseReader.saveComment(comment);
                });
    }

    @Transactional
    public List<CommentReadDto> getCommentsForEvent(Integer eventId) {
        logger.debugf("Get comments for event %d", eventId);
        var event = eventService.getEventDetails(eventId);
        return commentMapper.mapToDto(event.getComments());
    }

    private void updateComment(CommentWriteDto dto, Comment comment, String currentUser) {
        if (!comment.getCreator().equals(currentUser)) {
            throw new ForbiddenException("Comment %s was created by another user.".formatted(comment.getId()));
        }
        logger.debugf("Update comment %s", dto.id());
        comment.setMessage(dto.message());
    }

    private String getCurrentUser() {
        return securityIdentity.getPrincipal().getName();
    }
}
