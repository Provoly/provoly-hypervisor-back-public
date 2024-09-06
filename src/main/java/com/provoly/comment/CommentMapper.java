package com.provoly.comment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentMapper {

    public CommentReadDto mapToDto(Comment comment) {
        return new CommentReadDto(
                comment.getId(),
                comment.getUser().getSubject(),
                comment.getUser().getUsername(),
                comment.getMessage(),
                comment.getCreationDate(),
                comment.getLastModificationDate());
    }

    public List<CommentReadDto> mapToDto(List<Comment> comments) {
        return comments.stream().map(this::mapToDto).toList();
    }

    public CommentReadDto mapLastCommentToDto(List<Comment> comments) {
        return comments.isEmpty()
                ? null
                : mapToDto(comments.getFirst());
    }
}
