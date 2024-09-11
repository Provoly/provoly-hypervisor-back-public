package com.provoly.comment;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.action.ActionService;
import com.provoly.event.EventService;
import com.provoly.event.Status;
import com.provoly.procedure.Procedure;
import com.provoly.procedure.ProcedureService;
import com.provoly.user.UserService;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CommentService {
    private final Logger logger;
    private final CommentDatabaseReader databaseReader;
    private final CommentMapper commentMapper;
    private final EventService eventService;
    private final ActionService actionService;
    private final ProcedureService procedureService;
    private final UserService userService;

    public CommentService(Logger logger,
            CommentDatabaseReader databaseReader,
            CommentMapper commentMapper,
            EventService eventService, ActionService actionService, ProcedureService procedureService,
            UserService userService) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.commentMapper = commentMapper;
        this.eventService = eventService;
        this.actionService = actionService;
        this.procedureService = procedureService;
        this.userService = userService;
    }

    @Transactional
    public void closeProcedureWithComment(Integer procedureId, CommentWriteDto dto) {
        logger.debugf("Close and comment procedure %d", procedureId);
        var procedure = procedureService.getProcedureDetails(procedureId);

        if (isProcedureDone(procedure)) {
            throw new ForbiddenException("Procedure %s is already done".formatted(procedureId));
        }

        var user = userService.getCurrentUser();
        var comment = new Comment(dto.id(), dto.message(), user);
        procedure.setCloseComment(comment);
        databaseReader.saveComment(comment);
        procedureService.closeAllProcedureEvent(procedure);
    }

    @Transactional
    public void closeEventWithComment(Integer eventId, CommentWriteDto dto) {
        logger.debugf("Close and comment event %d", eventId);

        var event = eventService.getEventDetails(eventId);
        if (event.getStatus() == Status.DONE) {
            throw new ForbiddenException("Event %s is already done".formatted(eventId));
        }

        var user = userService.getCurrentUser();
        logger.debugf("Save closing comment %s", dto.id());
        var comment = new Comment(dto.id(), dto.message(), user);
        event.addComment(comment);
        databaseReader.saveComment(comment);

        logger.debugf("Close event %d", eventId);
        eventService.closeEvent(event);
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
                    var user = userService.getCurrentUser();
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

    @Transactional
    public void saveOrUpdateCommentForAction(UUID id, CommentWriteDto dto) {
        logger.debugf("Save or update comment for action %s", id);

        var action = actionService.getActionById(id);
        var procedure = procedureService.getProcedureFromAction(id);

        if (isProcedureDone(procedure)) {
            throw new ForbiddenException("Procedure %s is done and can no longer be commented on".formatted(procedure.getId()));
        }

        databaseReader.getCommentById(dto.id()).ifPresentOrElse(
                comment -> updateComment(dto, comment),
                () -> {
                    logger.debugf("Get current user subject from database or create it");
                    var user = userService.getCurrentUser();
                    logger.debugf("Save comment %s", dto.id());
                    var comment = new Comment(dto.id(), dto.message(), user);
                    action.addComment(comment);
                    databaseReader.saveComment(comment);
                });
    }

    @Transactional
    public List<CommentReadDto> getCommentsForAction(UUID id) {
        logger.debugf("Get comments  for action %s", id);
        var action = actionService.getActionById(id);
        return commentMapper.mapToDto(action.getComments());
    }

    private boolean isProcedureDone(Procedure procedure) {
        return procedure.getEvents().stream().allMatch(event -> event.getStatus() == Status.DONE);
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
