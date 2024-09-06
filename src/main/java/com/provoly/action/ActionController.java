package com.provoly.action;

import java.util.List;
import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.comment.CommentReadDto;
import com.provoly.comment.CommentService;
import com.provoly.comment.CommentWriteDto;
import com.provoly.user.Role;

@Path("/actions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActionController {

    private final CommentService commentService;

    public ActionController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Path("/id/{id}/comments")
    @PUT
    @RolesAllowed({ Role.STR_EVENT_PROC_WRITE })
    public void saveOrUpdateCommentForAction(UUID id, CommentWriteDto comment) {
        commentService.saveOrUpdateCommentForAction(id, comment);
    }

    @Path("/id/{id}/comments")
    @GET
    @RolesAllowed({ Role.STR_EVENT_READ })
    public List<CommentReadDto> getCommentsForAction(UUID id) {
        return commentService.getCommentsForAction(id);
    }

}
