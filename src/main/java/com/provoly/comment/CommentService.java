package com.provoly.comment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.event.EventService;
import com.provoly.event.Status;
import com.provoly.user.UserService;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CommentService {
    private final Logger logger;
    private final CommentDatabaseReader databaseReader;
    private final CommentMapper commentMapper;
    private final EventService eventService;
    private final UserService userService;

    public CommentService(Logger logger,
            CommentDatabaseReader databaseReader,
            CommentMapper commentMapper,
            EventService eventService,
            UserService userService) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.commentMapper = commentMapper;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Transactional
    public void saveOrUpdateCommentForEvent(Integer eventId, CommentWriteDto dto) {
        logger.debugf("Save or update comment for event %d", eventId);

        var event = eventService.getEventDetails(eventId);
        if (event.getStatus() == Status.DONE) {
            throw new ForbiddenException("Event %s is done and can no longer be commented on".formatted(eventId));
        }

        databaseReader.getCommentById(dto.id()).ifPresentOrElse(
                comment -> updateComment(dto, comment),
                () -> {
                    logger.debugf("Get current user subject from database or create it");
                    var user = userService.getUserBySubject();
                    logger.debugf("Save comment %s", dto.id());
                    var comment = new Comment(dto.id(), dto.message(), user);
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

    private void updateComment(CommentWriteDto dto, Comment comment) {
        var currentUser = userService.getCurrentUserSubject();
        if (!comment.getUser().getSubject().equals(currentUser)) {
            throw new ForbiddenException("Comment %s was created by another user.".formatted(comment.getId()));
        }
        logger.debugf("Update comment %s", dto.id());
        comment.setMessage(dto.message());
    }
}
