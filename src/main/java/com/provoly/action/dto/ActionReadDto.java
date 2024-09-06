package com.provoly.action.dto;

import java.time.Instant;
import java.util.UUID;

import com.provoly.comment.CommentReadDto;
import com.provoly.event.Status;

public class ActionReadDto {
    private final UUID id;
    private final String type;
    private final Status status;
    private final Instant lastModificationDate;
    private final CommentReadDto lastComment;
    private final int commentsCount;

    public ActionReadDto(UUID id, String type, Status status, Instant lastModificationDate, CommentReadDto lastComment,
            int commentsCount) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.lastModificationDate = lastModificationDate;
        this.lastComment = lastComment;
        this.commentsCount = commentsCount;
    }

    public ActionReadDto(ActionReadDto dto) {
        this.id = dto.getId();
        this.type = dto.getType();
        this.status = dto.getStatus();
        this.lastModificationDate = dto.getLastModificationDate();
        this.lastComment = dto.getLastComment();
        this.commentsCount = dto.getCommentsCount();
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public CommentReadDto getLastComment() {
        return lastComment;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
}
